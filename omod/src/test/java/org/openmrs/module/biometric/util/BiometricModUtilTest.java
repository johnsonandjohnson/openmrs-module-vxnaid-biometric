/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.openmrs.module.biometric.common.BiometricTestUtil.createVisit;
import static org.openmrs.module.biometric.common.BiometricTestUtil.visitEncounter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.common.BiometricTestUtil;
import org.openmrs.module.biometric.constants.BiometricTestConstants;
import org.openmrs.module.biometric.contract.MatchResponse;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Context.class})
public class BiometricModUtilTest {

  private static final String PARTICIPANT_ID = "1000AB";
  private static final String INVALID_PARTICIPANT_ID = "1000ZZ";
  private static final String UTC_DATE = "2020-02-18T00:00:00.000Z";
  private static final String DATE_PATTERN = "E MMM dd HH:mm:ss Z yyyy";

  @InjectMocks
  private BiometricModUtil biometricModUtil;

  @Mock
  private LocationService locationService;

  @Mock
  private PersonService personService;

  @Mock
  private LocationUtil locationUtil;

  @Mock
  private ObjectMapper mapper;

  private Patient patient;
  private Person person;

  @Before
  public void setUp() throws ClassNotFoundException {
    mapper = new ObjectMapper();
    PowerMockito.mockStatic(Context.class);
    when(Context.loadClass(anyString())).thenReturn((Class)PersonAddress.class);
    when(Context.getLocationService()).thenReturn(locationService);
    when(Context.getPersonService()).thenReturn(personService);

  }

  @Test
  public void findIdentifierFromPatient_shouldFindIdentifierIfPatientIsPassed() {
    //Given
    patient = BiometricTestUtil.createPatient();
    //When
    String result = biometricModUtil.findIdentifierFromPatient(patient);
    //Then
    Assert.assertEquals(patient.getPatientIdentifier().getIdentifier(), result);
  }

  @Test
  public void findIdentifierFromPatient_shouldThrowNullPointerExceptionIfNullValueIsPasssed() {
    //Given
    String result = null;
    try {
      //When
      result = biometricModUtil.findIdentifierFromPatient(null);
      Assert.fail("should throw NullPointerException");
    } catch (NullPointerException e) {
      //Then
      assertNull(result);
    }
  }

  @Test
  public void containsParticipant_shouldReturnTrueIfMatchingParticipantIsPassed() {
    //When
    boolean result = biometricModUtil.containsParticipant(PARTICIPANT_ID, createResponseList());
    //Then
    Assert.assertEquals(true, result);
  }

  @Test
  public void containsParticipant_shouldReturnFalseIfParticipantIdIsNotFound() {
    //When
    boolean result = biometricModUtil.containsParticipant(INVALID_PARTICIPANT_ID, createResponseList());
    //Then
    Assert.assertEquals(false, result);
  }

  @Test
  public void getPersonAddressProperty_shouldReturnAddress1ValuesIfAddress1PropertyIsPassed() {
    //Given
    person = BiometricTestUtil.createPerson();
    //When
    String result = biometricModUtil
        .getPersonAddressProperty(person.getPersonAddress(), "Address1");
    //Then
    Assert.assertEquals(BiometricTestConstants.PERSON1_ADDRESS1, result);
  }

  @Test
  public void getPersonAddressProperty_shouldReturnCityVillageValuesIfCityVillagePropertyIsPassed() {
    //Given
    person = BiometricTestUtil.createPerson();
    //When
    String result = biometricModUtil
        .getPersonAddressProperty(person.getPersonAddress(), "CityVillage");
    //Then
    Assert.assertEquals(BiometricTestConstants.PERSON1_CITY_VILLAGE, result);
  }

  @Test(expected = APIException.class)
  public void getPersonAddressProperty_shouldThrowAPIExceptionIfInvalidPropertyIsPassed() {
    //Given
    person = BiometricTestUtil.createPerson();
    //When
    String result = biometricModUtil
        .getPersonAddressProperty(person.getPersonAddress(), "InvalidProperty");
    //Then
    Assert.fail("should throw APIException");
  }

  @Test
  public void getPersonAddressProperty_shouldReturnNullForMissingPersonAddress() {
    //Given
    person = BiometricTestUtil.createPerson();
    person.getAddresses().clear();
    //When
    String result = biometricModUtil
        .getPersonAddressProperty(person.getPersonAddress(), "country");
    //Then
    Assert.assertNull(result);
  }

  @Test(expected = ParseException.class)
  public void convertUTCToDate_shouldThrowParseExceptionIfEmptyDateIsPassed()
      throws ParseException {
    //Given
    String date = "";
    Date date1 = new SimpleDateFormat(DATE_PATTERN).parse(date);
    //When
    Date result = biometricModUtil.convertUTCToDate(UTC_DATE);
    //Then
    Assert.assertEquals(date1, result);
  }

  @Test
  public void isoStringToDate_shouldCOnvertStringToUTCDate() throws ParseException {
    String date = "1985-02-04T10:11:12.123+01:00";
    Date utcDate = biometricModUtil.convertIsoStringToDate(date);
    assertNotNull(utcDate);
  }

  @Test(expected = APIException.class)
  public void convertUTCToDate_getDateThrowsParseException() {
    String date = "1985-02-04T10:11:12.123+01:00";
    Date date1 = biometricModUtil.convertUTCToDate(date);
    assertNotNull(date1);
  }

  @Test
  public void convertUTCToDate_nullDate() {
    Date date1 = biometricModUtil.convertUTCToDate(null);
    assertNull(date1);
  }

  @Test
  public void convertUTCToDate_getDate() {
    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    String strDate = dateFormat.format(date);
    Date date1 = biometricModUtil.convertUTCToDate(strDate);
    assertNotNull(date1);
  }

  @Test
  public void mergePatients_allPatients() {

    PatientResponse patientResponse1 = new PatientResponse();
    patientResponse1.setParticipantId("abcdef");
    patientResponse1.setDateModified(System.currentTimeMillis());

    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setParticipantUuid("ghijkl");
    List<PatientResponse> patientResponseList = biometricModUtil
        .mergePatients(Arrays.asList(patientResponse1), Arrays.asList(patientResponse));

    assertEquals(2, patientResponseList.size());
  }

  @Test
  public void dateToISO8601_shouldReturnDateInString() {
    String str = biometricModUtil.dateToISO8601(new Date());
    assertNotNull(str);
  }

  @Test
  public void removeWhiteSpaces_shouldRemoveWhiteSPaceCharacters() {
    String str = "test participant";
    assertThat(biometricModUtil.removeWhiteSpaces(str), equalTo("testparticipant"));
  }

  @Test
  public void getOldestEncounterInVisit_removeMultipleEncounterForOneVisit() {
    List<Visit> visits = new ArrayList<>();
    Visit visit = createVisit();
    Encounter encounter = visitEncounter(2013, Calendar.JANUARY, 9, 11, 11, 12);
    visit.addEncounter(encounter);
    encounter = visitEncounter(2013, Calendar.JANUARY, 11, 9, 11, 12);
    visit.addEncounter(encounter);
    visits.add(visit);
    visits = biometricModUtil.getOldestEncounterInVisit(visits);
    assertEquals(1, visits.get(0).getEncounters().size());
  }

  @Test
  public void getOldestEncounterInVisit_skipFunctionWhenOneEncounterOnly() {
    List<Visit> visits = new ArrayList<>();
    Visit visit = createVisit();
    Encounter encounter = visitEncounter(2013, Calendar.JANUARY, 9, 11, 11, 12);
    visit.addEncounter(encounter);
    visits.add(visit);
    visits = biometricModUtil.getOldestEncounterInVisit(visits);
    assertEquals(1, visits.get(0).getEncounters().size());
  }

  @Test
  public void isLocationExists_shouldReturnFalseWhenLocationNull() {
    when(Context.getLocationService().getLocationByUuid(anyString())).thenReturn(null);
    boolean flag = biometricModUtil.isLocationExists("dfd");
    Assert.assertFalse(flag);
  }

  @Test
  public void isLocationExists_shouldReturnTrueWhenLocationNotNull() {
    Location location = new Location();
    location.setUuid("42655fae-3b09-4b15-bb4b-296311546be4");
    location.setCountry("asdf");
    when(Context.getLocationService().getLocationByUuid(anyString())).thenReturn(location);
    boolean flag = biometricModUtil.isLocationExists("dfd");
    Assert.assertTrue(flag);
  }

  @Test
  public void isParticipantExists_shouldReturnFalseWhenLocationNull() {
    when(Context.getPersonService().getPersonByUuid(anyString())).thenReturn(null);
    boolean flag = biometricModUtil.isParticipantExists("dfd");
    Assert.assertFalse(flag);
  }

  @Test
  public void isParticipantExists_shouldReturnTrueWhenLocationNotNull() {
    patient = BiometricTestUtil.createPatient();
    when(Context.getPersonService().getPersonByUuid(anyString())).thenReturn(patient);
    boolean flag = biometricModUtil.isParticipantExists("dfd");
    Assert.assertTrue(flag);
  }

  private List<MatchResponse> createResponseList() {
    MatchResponse matchResponse = new MatchResponse();
    matchResponse.setParticipantId(PARTICIPANT_ID);
    List<MatchResponse> matchResponses = new ArrayList<>();
    matchResponses.add(matchResponse);
    return matchResponses;
  }
}
