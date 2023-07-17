/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service.impl;

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.PERSON_IMAGE_ATTRIBUTE;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.SYNC_DELETE;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.SYNC_UPDATE;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.biometric.api.builder.ImageResponseBuilder;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.BiometricData;
import org.openmrs.module.biometric.api.contract.Gender;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.api.service.ParticipantService;
import org.openmrs.module.biometric.api.util.BiometricApiUtil;
import org.openmrs.module.biometric.api.util.OpenMRSUtil;
import org.openmrs.module.biometric.api.util.SecurityUtil;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.Transactional;


/**
 * The implementation class for ParticipantService.
 */
public class ParticipantServiceImpl extends BaseOpenmrsService implements ParticipantService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantServiceImpl.class);

  private static final String ATTR_VALUE = "value";
  private static final String VOID_FLAG = "voided";
  private static final String OPEN_MRS_ID = "OpenMRS ID";
  private static final String DOT = ".";
  private static final String[] FILE_EXTENSIONS = {"jpeg"};
  private static final int FETCH_SIZE = 100;

  private DriverManagerDataSource dataSource;

  @Autowired
  private DbSessionFactory sessionFactory;

  @Autowired
  private PatientService patientService;

  @Autowired
  private PersonService personService;

  @Autowired
  private BiometricApiUtil util;

  @Autowired
  private ImageResponseBuilder builder;

  private String personImagesDir;

  /**
   * Initialize person image directory an data source.
   */
  @PostConstruct
  public final void init() throws BiometricApiException {
    Properties properties = OpenmrsUtil
        .getRuntimeProperties(BiometricApiConstants.APP_PROPERTIES_FILE);
    personImagesDir = util.getImageDirectory(properties);
    dataSource = util.getDataSource(properties);
  }

  @Transactional(rollbackFor = BiometricApiException.class)
  @Override
  public final Patient registerParticipant(Patient patientObj) throws
      BiometricApiException {

    if (null != patientService.getPatientByUuid(patientObj.getUuid())) {
      throw new BiometricApiException("Participant already exists with the same uuid");
    }
    Patient patient = patientService.savePatient(patientObj);
    Context.evictFromSession(patient);
    return patient;
  }

  @Transactional
  @Override
  public final Patient updateParticipant(Patient patient) {
    return patientService.savePatient(patient);
  }

  @Transactional
  @Override
  public final void voidPatient(Patient patient, String reason) {
    Context.getPatientService().voidPatient(patient, reason);
  }

  @Transactional(readOnly = true)
  @Override
  public final List<Patient> retrieveParticipantDetails(String patientIdentifier) {
    return patientService.getPatients(null, patientIdentifier, null, false);
  }

  @Transactional(readOnly = true)
  @Override
  public final List<PatientResponse> findByPhone(String phone)
      throws BiometricApiException, IOException {
    Criteria criteria = getSession().createCriteria(PersonAttribute.class);
    criteria.add(Restrictions.like(ATTR_VALUE, phone, MatchMode.EXACT));
    criteria.add(Restrictions.eq(VOID_FLAG, Boolean.FALSE));
    List<PersonAttribute> personAttributes = criteria.list();
    LOGGER.debug("findByPhone results count : {}", personAttributes.size());
    List<Patient> patients = new ArrayList<>(5);
    for (PersonAttribute personAttribute : personAttributes) {
      Integer personId = personAttribute.getPerson().getPersonId();
      Patient patient = patientService.getPatient(personId);
      patients.add(patient);
    }
    return buildPatientResponse(patients);
  }

  @Override
  @Transactional(readOnly = true)
  public final List<PatientResponse> findByParticipantId(String participantId) throws IOException,
      BiometricApiException {
    PatientIdentifierType type = patientService.getPatientIdentifierTypeByName(OPEN_MRS_ID);
    List<PatientIdentifierType> types = new ArrayList<>();
    types.add(type);
    //exact match by patient identifier
    List<Patient> patients = patientService.getPatients(null, participantId, types, true);
    return buildPatientResponse(patients);
  }

  @Override
  public final Patient findPatientByUuid(String uuid) {
    return patientService.getPatientByUuid(uuid);
  }

  @Override
  public final void saveParticipantImage(Person person, String base64EncodedImage,
      String deviceId) throws BiometricApiException {

    try {
      Path rootDirPath = util.getImageDirPath(personImagesDir);
      Path relDirPath = util.getImageDirPath(deviceId);
      final Path participantImageDirPath = util.getRootedDirectorySafely(rootDirPath, relDirPath);

      final File imageFile = participantImageDirPath
          .resolve(String.format("%s.%s", person.getUuid(), BiometricApiConstants.IMAGE_EXTN))
          .toFile();

      final byte[] decodedBytes = DatatypeConverter.parseBase64Binary(base64EncodedImage);
      final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
      ImageIO.write(bufferedImage, BiometricApiConstants.IMAGE_EXTN, imageFile);
      LOGGER.debug("Participant : {} image uploaded successfully", person.getUuid());

      util.setPersonAttributeValue(person.getUuid(), PERSON_IMAGE_ATTRIBUTE, deviceId);
    } catch (IOException e) {
      LOGGER.error("Participant : {} image upload failed : ", person.getUuid());
    }
  }

  @Override
  public final Optional<String> retrieveParticipantImage(String personUuid)
      throws IOException, BiometricApiException {
    Optional<File> imageFile = retrievePersonImage(personUuid);
    return imageFile.map(this::convertFileToBase64String);
  }

  @Override
  public final List<PatientResponse> findPatientsByUuids(Set<String> uuids)
      throws BiometricApiException,
      IOException {
    return buildPatientResponse(getPatients(uuids));
  }

  @Override
  public final List<SyncImageResponse> findImagesByUuids(Set<String> uuids) throws IOException {
    List<Path> results;
    Path path = util.getImageDirPath(personImagesDir);
    try (Stream<Path> walk = Files.walk(path)) {
      results = walk.filter(Files::isRegularFile).filter(p -> Arrays.stream(FILE_EXTENSIONS)
          .anyMatch(entry -> p.getFileName().toString().endsWith(entry)))
          .filter(p -> uuids.contains(FilenameUtils.getBaseName(p.getFileName().toString())))
          .collect(Collectors.toList());
    }
    return builder.createFromPath(results);
  }

  @Override
  @Transactional(readOnly = true)
  public final List<SyncTemplateResponse> getBiometricDataByParticipantIds(Set<String> uuids) {

    NamedParameterJdbcTemplate template = util
        .getNamedParameterJdbcTemplate(dataSource, FETCH_SIZE);
    SqlParameterSource params = new MapSqlParameterSource("uuids", uuids);

    String query =
        "SELECT dbid, participantUuid, TO_BASE64(template) template,modificationDate,voided FROM iris_templates "
            +
            "where participantUuid IN (:uuids)";

    List<BiometricData> biometricDataList = template.query(query, params,
        (rs, rowNum) -> new BiometricData(rs.getString("dbid"), rs.getString("participantUuid"),
            rs.getString("template"), rs.getDate("modificationDate"), rs.getBoolean(VOID_FLAG)));
    LOGGER.info("participants from biometric db : {}", biometricDataList.size());
    List<SyncTemplateResponse> responseList = new ArrayList<>(5);

    for (BiometricData data : biometricDataList) {
      SyncTemplateResponse response = new SyncTemplateResponse();
      response.setParticipantUuid(SecurityUtil.sanitizeOutput(data.getParticipantUuid()));
      response.setBiometricsTemplate(SecurityUtil.sanitizeOutput(data.getTemplate()));
      response.setDateModified(data.getModificationDate().getTime());
      response.setType(SYNC_DELETE);
      if (!data.isVoided()) {
        response.setType(SYNC_UPDATE);
      }
      responseList.add(response);
    }
    return responseList;
  }

  private DbSession getSession() {
    return sessionFactory.getCurrentSession();
  }

  private Optional<File> retrievePersonImage(String personUuid) throws BiometricApiException, IOException {
    Person person = personService.getPersonByUuid(personUuid);
    if (person == null) {
      LOGGER.error("Person with UUID: {} not found", personUuid);
      throw new EntityNotFoundException(
          String.format("Person with this UUID: %s not found", personUuid));
    }
    Path path = util.getImageDirPath(personImagesDir);
    String file = personUuid + DOT + BiometricApiConstants.IMAGE_EXTN;
    List<Path> results;
    try (Stream<Path> walk = Files.walk(path)) {
      results = walk.filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().endsWith(file))
          .collect(Collectors.toList());
    }

    if (results.size() > 1) {
      LOGGER.info("Multiple images found for the participant: {} ", personUuid);
      throw new BiometricApiException("Multiple images found for the participant");
    }

     return results.stream().findFirst().map(Path::toFile);
  }

  private List<PatientResponse> buildPatientResponse(List<Patient> patients)
      throws BiometricApiException,
      IOException {
    List<PatientResponse> responseList = new ArrayList<>();
    for (Patient patient : patients) {
      if (!Boolean.TRUE.equals(patient.getVoided())) {
        PatientResponse response = new PatientResponse();
        response.setParticipantUuid(patient.getUuid());
        response.setDateModified(OpenMRSUtil.getLastModificationDate(patient).getTime());
        response.setParticipantId(patient.getPatientIdentifier().getIdentifier());
        response.setGender(Gender.valueOf(patient.getGender()));
        response.setBirthDate(util.dateToISO8601(patient.getBirthdate()));

        List<AttributeData> attributes = new ArrayList<>(10);
        for (PersonAttribute personAttribute : patient.getPerson().getActiveAttributes()) {
          AttributeData attribute = new AttributeData();
          attribute.setType(personAttribute.getAttributeType().getName());
          attribute.setValue(personAttribute.getValue());
          attributes.add(attribute);
        }
        response.setAttributes(attributes);
        response.setAddresses(util.getPersonAddressProperty(patient.getPerson()));
        responseList.add(response);
      }
    }
    return responseList;
  }

  private List<Patient> getPatients(Set<String> uuids) {
    List<Patient> patients = new ArrayList<>();
    for (String uuid : uuids) {
      Patient patient = patientService.getPatientByUuid(uuid);
      if (null != patient && !patient.getVoided()) {
        patients.add(patient);
      }
    }
    LOGGER.info("No of participants : {}", patients.size());
    return patients;
  }

  private String convertFileToBase64String(File file) {
    try {
      byte[] imageFileContent = FileUtils.readFileToByteArray(file);
      return Base64.getEncoder().encodeToString(imageFileContent);
    } catch (IOException e) {
      throw new APIException("Error converting file to Base64 String.", e);
    }
  }
}
