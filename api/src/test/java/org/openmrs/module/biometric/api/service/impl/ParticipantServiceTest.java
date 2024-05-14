/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.FileUtils;
import org.hibernate.Criteria;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.biometric.api.builder.ImageResponseBuilder;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.BiometricData;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.api.util.BiometricApiUtil;
import org.openmrs.module.biometric.api.util.TestUtil;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Participant Service Tests
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Context.class, OpenmrsUtil.class, DatatypeConverter.class,
    ImageIO.class,
    FileUtils.class, Base64.class})
public class ParticipantServiceTest {

  private static final String BASE_64_ENCODED_IMAGE = "base64Image";
  private static final String GP_IMAGE_DIR = "/app/person_image";
  private static final String PERSON_IMAGE_DIR = "src/test/resources/";
  private static final String PARTCIPANT_ID = "test1";
  private static final String PERSON_UUID = "valid-person-uuid-value";
  private static final String PARTICIPANT_IMAGES_DIR = "biometric.images.dir";
  private static final String DEVICE_ID = "";

  @Mock
  private PersonService personService;
  @Mock
  private PatientService patientService;
  @Mock
  private AdministrationService administrationService;
  @Mock
  private BiometricApiUtil biometricApiUtil;
  @Mock
  private DbSessionFactory sessionFactory;
  @Mock
  private Criteria criteria;
  @Mock
  private DbSession dbSession;
  @Mock
  private NamedParameterJdbcTemplate template;
  @Mock
  private ImageResponseBuilder builder;

  @InjectMocks
  private ParticipantServiceImpl participantService;


  private Person person;
  private Patient patient;
  private Properties properties = null;
  private DriverManagerDataSource dataSource;
  private RowMapper<BiometricData> rowMapper;

  @Before
  public void setUp() throws Exception {
    PowerMockito.mockStatic(OpenmrsUtil.class);
    PowerMockito.mockStatic(Context.class);
    PowerMockito.mockStatic(DatatypeConverter.class);
    PowerMockito.mockStatic(ImageIO.class);
    PowerMockito.mockStatic(FileUtils.class);
    PowerMockito.mockStatic(Base64.class);

    given(Context.getAdministrationService()).willReturn(administrationService);
    given(OpenmrsUtil.getApplicationDataDirectory()).willReturn(PERSON_IMAGE_DIR);
    given(OpenmrsUtil.getRuntimeProperties(BiometricApiConstants.APP_PROPERTIES_FILE))
        .willReturn(new Properties());

    // Just return the combined path, no checks or creation
    when(biometricApiUtil.getRootedDirectorySafely(Mockito.any(), Mockito.any())).then(
        invocationOnMock -> ((Path) invocationOnMock.getArguments()[0]).resolve(
            (Path) invocationOnMock.getArguments()[1]));
    when(biometricApiUtil.getImageDirPath(Mockito.anyString())).thenCallRealMethod();
    when(biometricApiUtil.getImageDirectory(Mockito.any())).thenCallRealMethod();

    participantService.init();

    person = TestUtil.createPerson();
    patient = TestUtil.createPatient(person);
  }

  @Test
  public void registerParticipant_shouldCreateParticipantWithoutPersonImage() throws Exception {
    //Given
    given(administrationService.getGlobalProperty(PARTICIPANT_IMAGES_DIR)).willReturn(GP_IMAGE_DIR);
    doNothing().when(Context.class, "evictFromSession", anyObject());
    given(patientService.savePatient(patient)).willReturn(patient);
    Patient registerParticipant = participantService.registerParticipant(patient);
    assertNotNull(registerParticipant.getUuid());
    assertThat(person.getUuid(), equalTo(registerParticipant.getUuid()));

    verifyStatic(times(1));
    Context.evictFromSession(anyObject());

    verifyNoMoreInteractions();
    OpenmrsUtil.getApplicationDataDirectory();
  }

  @Test
  public void saveParticipantImage_shouldNotThrowExceptionIfTheBase64EncodedImageIsInvalid()
      throws Exception {
    //Given
    byte[] decodedBytes = "".getBytes();
    given(DatatypeConverter.parseBase64Binary(BASE_64_ENCODED_IMAGE)).willReturn(decodedBytes);
    given(ImageIO.read(any(ByteArrayInputStream.class))).willThrow(IOException.class);
    when(biometricApiUtil.getLocationByUuid(TestUtil.LOCATION_UUID)).thenReturn(TestUtil.createLocation());

    try {
      participantService.saveParticipantImage(patient, BASE_64_ENCODED_IMAGE, DEVICE_ID);
    } catch(Exception any) {
      fail("Should not throw any exception, but was: " + any.toString());
    } finally {

      //Then
      verifyStatic(times(1));
      DatatypeConverter.parseBase64Binary(BASE_64_ENCODED_IMAGE);
      verifyStatic(times(1));
      ImageIO.read(any(ByteArrayInputStream.class));
    }
  }

  @Test
  public void retrieveParticipantDetails_shouldRetrieveParticipantDetails() {
    //Given
    List<Patient> patientList = new ArrayList<>();
    patientList.add(patient);
    when(patientService.getPatients(null, PARTCIPANT_ID, null, false)).thenReturn(patientList);

    //When
    List<Patient> patients = participantService.retrieveParticipantDetails(PARTCIPANT_ID);

    //Then
    assertNotNull(patient);
    assertThat(patients.size(), equalTo(1));
    verify(patientService, times(1)).getPatients(null, PARTCIPANT_ID, null, false);
  }

  @Test(expected = BiometricApiException.class)
  public void retrieveParticipantImage_shouldThrowAPIExceptionIfPersonIsNull()
      throws IOException, BiometricApiException {
    //Given
    given(personService.getPersonByUuid(PERSON_UUID)).willReturn(null);
    try {
      //When
      participantService.retrieveParticipantImage(PERSON_UUID);
      fail("should throw APIException");
    } finally {
      //Then
      verify(personService, times(1)).getPersonByUuid(PERSON_UUID);
    }
  }
  //TBD
/*    @Test
    public void retrieveParticipantImage_shouldRetrievePersonImageIfPersonUuidIsPassed()
            throws IOException, BiometricApiException {
        //Given
        byte[] decodedBytes = BASE_64_ENCODED_IMAGE.getBytes();
        Encoder encoder = PowerMockito.mock(Encoder.class);
        given(personService.getPersonByUuid(PERSON_UUID)).willReturn(person);
        given(FileUtils.readFileToByteArray(any())).willReturn(decodedBytes);
        given(Base64.getEncoder()).willReturn(encoder);
        given(Base64.getEncoder().encodeToString(decodedBytes)).willReturn(BASE_64_ENCODED_IMAGE);
        //When
        String responseBase64EncodedImage = participantService.retrieveParticipantImage(PERSON_UUID);
        //Then
        assertNotNull(responseBase64EncodedImage);
        assertEquals(BASE_64_ENCODED_IMAGE1, responseBase64EncodedImage);
    }*/

  /*@Test(expected = BiometricApiException.class)
  public void retrieveParticipantImage_shouldThrowAPIExceptionIfImageNotFoundForGivenPerson()
      throws IOException, BiometricApiException {
    //Given
    byte[] decodedBytes = "".getBytes();
    Encoder encoder = PowerMockito.mock(Encoder.class);
    given(personService.getPersonByUuid(PERSON_UUID)).willReturn(person);
    given(FileUtils.readFileToByteArray(any())).willReturn(decodedBytes);
    given(Base64.getEncoder()).willReturn(encoder);
    given(Base64.getEncoder().encodeToString(decodedBytes)).willReturn("");
    try {
      //When
      participantService.retrieveParticipantImage(PERSON_UUID);
      fail("should throw APIException");
    } finally {
      //Then
      verify(personService, times(1)).getPersonByUuid(PERSON_UUID);
    }
  }*/

  @Test
  public void findByParticipantId_shouldReturnPatientList()
      throws BiometricApiException, IOException {
    String participantId = "myParticipantId";
    PatientIdentifierType patientIdentifierType = TestUtil.createPatientIdentifierType();
    when(patientService.getPatientIdentifierTypeByName(anyString()))
        .thenReturn(patientIdentifierType);
    List<Patient> patientList = new ArrayList<>();
    patientList.add(patient);
    when(patientService
        .getPatients(anyString(), anyString(), anyListOf(PatientIdentifierType.class),
            anyBoolean())).thenReturn(patientList);

    List<PatientResponse> patientResponseList = participantService
        .findByParticipantId(participantId);

    assertEquals(1, patientResponseList.size());
  }

  @Test
  public void findByPhone_shouldReturnPatient() throws BiometricApiException, IOException {
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(PersonAttribute.class)).thenReturn(criteria);

    List<PatientResponse> patientResponseList = participantService.findByPhone("12345");

    assertNotNull(patientResponseList);
    assertTrue(patientResponseList.isEmpty());
  }

  @Test
  public void getBiometricDataByParticipantIds_shouldReturnDataWithParticipantId() {
    given(biometricApiUtil.isBiometricFeatureEnabled()).willReturn(true);
    Set<String> uuidList = Collections.singleton("8gi19999-h1af-9899-b684-851abfbac4d9");
    when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
    when(biometricApiUtil.getNamedParameterJdbcTemplate(dataSource, 100)).thenReturn(template);

    SqlParameterSource params = new MapSqlParameterSource("uuids", uuidList);
    BiometricData biometricData = TestUtil.createBiometricData();
    List<BiometricData> biometricDataList = new ArrayList<>();
    biometricDataList.add(biometricData);

    when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
        .thenReturn(biometricDataList);

    List<SyncTemplateResponse> syncTemplateResponseList = participantService
        .getBiometricDataByParticipantIds(uuidList);

    assertThat(biometricDataList, hasSize(1));
    assertThat(syncTemplateResponseList, hasSize(1));
  }

  @Test
  public void getBiometricDataByParticipantIds_shouldReturnResult() {
    given(biometricApiUtil.isBiometricFeatureEnabled()).willReturn(true);
    Set<String> uuidList = Collections.singleton("8gi19999-h1af-9899-b684-851abfbac4d9");
    when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
    when(biometricApiUtil.getNamedParameterJdbcTemplate(dataSource, 100)).thenReturn(template);

    SqlParameterSource params = new MapSqlParameterSource("uuids", uuidList);
    BiometricData biometricData = TestUtil.createBiometricData();
    biometricData.setId("1");
    List<BiometricData> biometricDataList = new ArrayList<>();
    biometricDataList.add(biometricData);

    when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
        .thenReturn(biometricDataList);

    List<SyncTemplateResponse> syncTemplateResponseList = participantService
        .getBiometricDataByParticipantIds(uuidList);

    assertThat(biometricDataList, hasSize(1));
    assertThat(syncTemplateResponseList, hasSize(1));
  }

  @Test
  public void findPatiensByUuids_shouldReturnAllPatientsWithUuids()
      throws BiometricApiException, IOException {
    Set<String> uuids = Collections.singleton("8gi19999-h1af-9899-b684-851abfbac4d9");
    when(patientService.getPatientByUuid(uuids.iterator().next())).thenReturn(patient);
    List<PatientResponse> patientResponses = new ArrayList<>();
    try {
      patientResponses = participantService.findPatientsByUuids(uuids);
    } finally {
      verify(patientService, times(1)).getPatientByUuid(anyString());
      assertThat(patientResponses, hasSize(1));
    }
  }

  @Test
  public void findPatiensByUuids_shouldReturnNoPatientwithUuidNull()
      throws BiometricApiException, IOException {
    Set<String> uuids = Collections.singleton("8gi19999-h1af-9899-b684-851abfbac4d9");
    when(patientService.getPatientByUuid(uuids.iterator().next())).thenReturn(null);
    List<PatientResponse> patientResponses = new ArrayList<>();
    try {
      patientResponses = participantService.findPatientsByUuids(uuids);
    } finally {
      verify(patientService, times(1)).getPatientByUuid(anyString());
      assertThat(patientResponses, hasSize(0));
    }
  }

/*  @Test
  public void findImagesByUuids_ShouldReturnImagesWithUuids() throws IOException {
    List<String> uuids = Arrays.asList("8gi19999-h1af-9899-b684-851abfbac4d9");
    results
        .add(new File("src/test/resources/images/Device1/India/Site1/test1.jpeg").toPath());
    List<SyncImageResponse> responses = buildResponse("test1");
    when(builder.createFrom(any(ArrayList.class))).thenReturn(responses);

    List<SyncImageResponse> actualResponse = participantService.findImagesByUuids(uuids);
    assertNotNull(responses);
    assertThat(actualResponse.size(), Matchers.equalTo(1));
  }*/

  private List<SyncImageResponse> buildResponse(String uuid) {
    List<SyncImageResponse> responses = new ArrayList<>();
    SyncImageResponse response = new SyncImageResponse();
    response.setParticipantUuid(uuid);
    responses.add(response);
    return responses;
  }
}
