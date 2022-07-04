/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class BioMetricApiUtilTest {

  private static final String DRIVER = "biometric.sql.driver";
  private static final String DATA_SOURCE_URL = "biometric.datasource.url";
  private static final String BIOMETRIC_DB_USER = "biometric.connection.username";
  private static final String BIOMETRIC_DB_PWD = "biometric.connection.password";
  private static final String PARTICIPANT_IMAGES_DIR = "biometric.images.dir";
  private static final String FETCH_SIZE = "biometric.database.fetchsize";
  private static final String COUNTRY = "country";

  @InjectMocks
  private BiometricApiUtil biometricApiUtil;

  @Mock
  private LocationService locationService;

  @Mock
  private ConfigService configService;

  private Properties properties = null;

  private DriverManagerDataSource dataSource;

  @Before
  public void setUp() throws IOException {
    PowerMockito.mockStatic(Context.class);

    properties = new Properties();
    properties.put(DRIVER, "com.mysql.jdbc.Driver");
    properties.put(DATA_SOURCE_URL, "jdbc\\:mysql\\://localhost\\:3306/openmrs?serverTimezone");
    properties.put(BIOMETRIC_DB_USER, "test");
    properties.put(BIOMETRIC_DB_PWD, "test");
    properties.put(FETCH_SIZE, "100");
    properties.put(PARTICIPANT_IMAGES_DIR, "src/test/resources/images");
  }

  @Test
  public void dateToISO8601_shouldReturnDateInString() {
    String str = biometricApiUtil.dateToISO8601(new Date());
    assertNotNull(str);
  }

  @Test
  public void getLocationByUuid_shouldReturnLocation() throws EntityNotFoundException {
    String uuid = "867d1cff-d8c5-4645-91d5-0a773a33c83b";
    Location location = TestUtil.createLocation();
    given(Context.getLocationService()).willReturn(locationService);
    when(Context.getLocationService().getLocationByUuid(anyString())).thenReturn(location);
    Location location1 = biometricApiUtil.getLocationByUuid(uuid);
    verify(locationService, times(1)).getLocationByUuid(anyString());
    assertNotNull(location1);
  }

  @Test(expected = EntityNotFoundException.class)
  public void getLocationByUuid_shouldThrowEntityNotFoundExceptionWhenNullLocation()
      throws EntityNotFoundException {
    String uuid = "867d1cff-d8c5-4645-91d5-0a773a33c83b";
    Location location = TestUtil.createLocation();
    given(Context.getLocationService()).willReturn(locationService);
    when(Context.getLocationService().getLocationByUuid(anyString())).thenReturn(null);
    Location location1 = biometricApiUtil.getLocationByUuid(uuid);
    verify(locationService, times(1)).getLocationByUuid(anyString());

  }

  @Test
  public void getDataSource() {
    dataSource = biometricApiUtil.getDataSource(properties);
    assertNotNull(dataSource);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getNamedParameterJdbcTemplate_ThrowsIllegalArgumentExceptionWhenDataSourceNull() {
    NamedParameterJdbcTemplate namedParameterJdbcTemplate = biometricApiUtil
        .getNamedParameterJdbcTemplate(null, 100);
    assertNull(namedParameterJdbcTemplate);
  }

  @Test
  public void getNamedParameterJdbcTemplate_shouldReturnNamedParameterJdbcTemplate() {
    dataSource = biometricApiUtil.getDataSource(properties);
    NamedParameterJdbcTemplate namedParameterJdbcTemplate = biometricApiUtil
        .getNamedParameterJdbcTemplate(dataSource, 20);
    assertNotNull(namedParameterJdbcTemplate);
  }

  @Test
  public void getPersonAddressProperty_shouldReturnAddressMap()
      throws IOException, BiometricApiException, ClassNotFoundException {
    Person person = TestUtil.createPerson();
    String filePath = "src" + File.separator + "test" + File.separator + "resources" +
        File.separator + "address.json";
    String targetStream = new String(Files.readAllBytes(Paths.get(filePath)));

    when(configService.retrieveConfig(anyString())).thenReturn(targetStream);
    when(Context.loadClass(anyString())).thenReturn((Class) PersonAddress.class);

    Map<String, String> map = biometricApiUtil.getPersonAddressProperty(person);

    verify(configService, times(1)).retrieveConfig(anyString());
  }

  @Test
  public void getPersonAddressProperty_shouldReturnAddressMapWithCountry()
      throws IOException, BiometricApiException, ClassNotFoundException {
    Person person = TestUtil.createPerson();
    String filePath = "src/test/resources/address.json";
    String targetStream = new String(Files.readAllBytes(Paths.get(filePath)));

    when(configService.retrieveConfig(anyString())).thenReturn(targetStream);
    when(Context.loadClass(anyString())).thenReturn((Class) PersonAddress.class);

    Map<String, String> map = biometricApiUtil.getPersonAddressProperty(person);

    verify(configService, times(1)).retrieveConfig(anyString());
    assertEquals(person.getPersonAddress().getCountry(), map.get(COUNTRY));
  }
}
