/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.util;

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.DEFAULT_PARTICIPANT_IMAGES_DIR;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.ENABLE_BIOMETRIC;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

/**
 * Utility class.
 */
@Component
public class BiometricApiUtil {

  private static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  private static final String DRIVER = "biometric.sql.driver";
  private static final String DATA_SOURCE_URL = "biometric.datasource.url";
  private static final String BIOMETRIC_DB_USER = "biometric.connection.username";
  private static final String BIOMETRIC_DB_PWD = "biometric.connection.password";
  private static final String ADDRESS_FIELDS = "addressFields";
  private static final String FIELD = "field";
  private static final String COUNTRY = "country";

  @Autowired
  private ConfigService configService;

  /**
   * Converts the date object to ISO8601 string format.
   *
   * @param date date object
   * @return ISO8601 string format
   */
  public String dateToISO8601(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
    return sdf.format(date);
  }

  /**
   * Retrieve the location for the given location uuid.
   */
  public Location getLocationByUuid(String uuid) throws EntityNotFoundException {
    Location location = Context.getLocationService().getLocationByUuid(uuid);
    if (null == location) {
      throw new EntityNotFoundException(String.format("Location %s not found", uuid));
    }
    return location;
  }

  /**
   * Get JDBC template.
   *
   * @param dataSource datasource details
   * @param fetchSize fetch size
   * @return JDBC template
   */
  public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource,
      int fetchSize) {
    if (null == dataSource) {
      throw new IllegalArgumentException("datasource can not be null");
    }
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.setFetchSize(fetchSize);
    return new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /**
   * Creates a datasource object.
   *
   * @param properties to create datasource object
   */
  public DriverManagerDataSource getDataSource(Properties properties) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(properties.getProperty(DRIVER));
    dataSource.setUrl(properties.getProperty(DATA_SOURCE_URL));
    dataSource.setUsername(properties.getProperty(BIOMETRIC_DB_USER));
    dataSource.setPassword(properties.getProperty(BIOMETRIC_DB_PWD));
    return dataSource;
  }

  /**
   * Get the person address property address details for the country are configured in config as
   * json in  Global property, parse config json to retrieve the address fields for the specified
   * country.
   *
   * @return person address property
   */
  public Map<String, String> getPersonAddressProperty(Person person)
      throws BiometricApiException, IOException {
    Map<String, String> addressMap = new HashMap<>();
    String country = getPersonAddressPropertyByField(person.getPersonAddress(), COUNTRY);
    Iterator<JsonNode> iter = new ObjectMapper()
        .readTree(configService.retrieveConfig(BiometricApiConstants.MAIN_CONFIG))
        .get(ADDRESS_FIELDS)
        .get(country).getElements();

    while (iter.hasNext()) {
      String fieldName = iter.next().get(FIELD).asText();
      String value = getPersonAddressPropertyByField(person.getPersonAddress(), fieldName);
      addressMap.put(fieldName, value);
    }
    addressMap.put(COUNTRY, person.getPersonAddress().getCountry());

    return addressMap;
  }

  /**
   * Gets a Path to a {@code dirRelative} relative to the {@code rootDir}. Creates any needed parent
   * directories if needed. The method checks if the result path actually starts with the {@code
   * rootDir}.
   * <p>
   * Given {@code rootDir} equal to `/app/data` and `dirRelative` equal to `/users/1234/images`, the
   * result path is going to be equal to `/app/data/users/1234/images` and any directory which did
   * not exist prior to the method execution is going to be created.
   * </p>
   *
   * @param rootDir the root directory, not null
   * @param dirRelative the directory which has to be placed reltivly to the {@code rootDir}, not
   * null
   * @return the directory as specified, never null
   * @throws BiometricApiException if the result path is not started by {@code rootDir}
   * @throws IOException if an I/O error occurs
   */
  public Path getRootedDirectorySafely(Path rootDir, Path dirRelative)
      throws BiometricApiException, IOException {
    final Path resultDirPath = rootDir.resolve(dirRelative).normalize();
    // makes sure we will write in a dir located in rootDir
    if (!resultDirPath.startsWith(rootDir)) {
      // This may indicate a path traversal attack attempt
      throw new BiometricApiException(
          "Invalid rooted path! Path: '" + resultDirPath.toString() + "' was expected to start by "
              +
              "directory: '" + rootDir.toString() + "'");
    }

    Files.createDirectories(resultDirPath);

    return resultDirPath;
  }

  public String getImageDirectory(Properties properties) throws BiometricApiException {
    String defaultDir = OpenmrsUtil.getApplicationDataDirectory() + DEFAULT_PARTICIPANT_IMAGES_DIR;
    String personImagesDir = properties
        .getProperty(BiometricApiConstants.PARTICIPANT_IMAGES_DIR, defaultDir);

    if (!Files.exists(getImageDirPath(personImagesDir))) {
      throw new BiometricApiException(
          String.format("Images directory %s missing", personImagesDir));
    }
    return personImagesDir;
  }

  /**
   * Get path from image location.
   *
   * @param imagePathStr image path
   * @return @see java.nio.file.Path
   */
  @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
  public Path getImageDirPath(String imagePathStr) {
    // the sonar warning can be suppressed as the path is not coming from the input
    return Paths.get(imagePathStr).normalize();
  }

  /**
   * Set person attribute value.
   *
   * @param patientUuid patient uuid
   * @param attributeType attribute type
   * @param attributeValue value of an attribute
   */
  public void setPersonAttributeValue(String patientUuid, String attributeType,
      String attributeValue) {
    PersonService personService = Context.getPersonService();
    Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);

    if (patient != null && patient.getPatientIdentifier() != null) {
      PersonAttributeType personAttributeType = personService
          .getPersonAttributeTypeByName(attributeType);
      PersonAttribute attribute = patient.getAttribute(attributeType);
      if (attribute == null) {
        attribute = new PersonAttribute();
        attribute.setAttributeType(personAttributeType);
        attribute.setValue(attributeValue);
        patient.addAttribute(attribute);
      } else {
        attribute.setValue(attributeValue);
      }
      personService.savePerson(patient);
    }
  }

  /**
   * Check if the biometric feature is enabled or not.
   *
   * @return true if it is enabled
   */
  public boolean isBiometricFeatureEnabled() {
    return Boolean.parseBoolean(Context.getAdministrationService().getGlobalProperty(ENABLE_BIOMETRIC));
  }

  private String getPersonAddressPropertyByField(PersonAddress address, String property)
      throws BiometricApiException {
    try {
      Class<?> personAddressClass = Context.loadClass("org.openmrs.PersonAddress");
      Method getPersonAddressProperty;
      getPersonAddressProperty = personAddressClass
          .getMethod("get" + property.substring(0, 1).toUpperCase() + property.substring(1));
      return SecurityUtil
          .sanitizeOutput((String) getPersonAddressProperty.invoke(address));
    } catch (InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
      throw new BiometricApiException("Invalid property name " + property, e);
    }
  }
}
