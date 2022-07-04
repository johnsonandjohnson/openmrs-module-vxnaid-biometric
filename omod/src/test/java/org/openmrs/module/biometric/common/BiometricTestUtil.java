/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.common;

import static org.openmrs.module.biometric.constants.BiometricTestConstants.HIGH_MATCHING_SCORE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.LANGUAGE_ATTRIBUTE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.LOCATION_ATTRIBUTE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PARTICIPANT_ID;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PATIENT1_IDENTIFIER;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PATIENT1_LOCATION;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PATIENT2_IDENTIFIER;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PATIENT2_LOCATION;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_ADDRESS1;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_ADDRESS2;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_CITY_VILLAGE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_DOB;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_GENDER;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_LANGUAGE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_POSTAL_CODE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_UUID;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON2_ADDRESS1;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON2_ADDRESS2;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON2_CITY_VILLAGE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON2_DOB;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON2_GENDER;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON2_POSTAL_CODE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON2_UUID;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON_STATUS;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.STATUS_ATTRIBUTE;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.VACCINE_PROGRAM;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.VP_ATTRIBUTE;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.contract.MatchResponse;
import org.openmrs.util.OpenmrsUtil;

/**
 * Test Utility class
 */
public class BiometricTestUtil {

  public static final String VISIT_UUID = "0d68106e-8753-4e31-b65d-11d322a7e81d";
  public static final String LOCATION_UUID = "9dd05e68-4d77-4b61-b2bf-436857f35aa0";
  private static final String PATIENT_IDENTIFIER_TYPE = "8dd05e68-4d77-4b61-b2bf-436857f35aa0";
  private static final String ENCOUNTER_TYPE_DOSING = "Dosing";

  public static Person createPerson() {
    Person person1 = new Person();
    person1.setUuid(PERSON1_UUID);
    person1.setGender(PERSON1_GENDER);
    person1.setBirthdate(PERSON1_DOB);
    PersonName name1 = new PersonName();
    name1.setGivenName("NA");
    person1.addName(name1);

    PersonAddress personAddress = new PersonAddress();
    personAddress.setAddress1(PERSON1_ADDRESS1);
    personAddress.setAddress2(PERSON1_ADDRESS2);
    personAddress.setCityVillage(PERSON1_CITY_VILLAGE);
    personAddress.setPostalCode(PERSON1_POSTAL_CODE);
    Set<PersonAddress> addressSet = new HashSet<>();
    addressSet.add(personAddress);
    person1.setAddresses(addressSet);

    //add attributes
    PersonAttributeType locationType = new PersonAttributeType();
    locationType.setName(LOCATION_ATTRIBUTE);
    locationType.setUuid(OpenmrsUtil.generateUid());
    PersonAttribute locationAttribute = new PersonAttribute();
    locationAttribute.setAttributeType(locationType);
    locationAttribute.setValue(PATIENT1_LOCATION);
    person1.addAttribute(locationAttribute);

    PersonAttributeType personLanguageType = new PersonAttributeType();
    personLanguageType.setName(LANGUAGE_ATTRIBUTE);
    personLanguageType.setUuid(OpenmrsUtil.generateUid());
    PersonAttribute languageAttribute = new PersonAttribute();
    languageAttribute.setAttributeType(locationType);
    languageAttribute.setValue(PERSON1_LANGUAGE);
    person1.addAttribute(languageAttribute);

    PersonAttributeType vaccinationProgramType = new PersonAttributeType();
    vaccinationProgramType.setName(VP_ATTRIBUTE);
    vaccinationProgramType.setUuid(OpenmrsUtil.generateUid());
    PersonAttribute vaccinationAttribute = new PersonAttribute();
    vaccinationAttribute.setAttributeType(locationType);
    vaccinationAttribute.setValue(VACCINE_PROGRAM);
    person1.addAttribute(vaccinationAttribute);

    PersonAttributeType statusAttributeType = new PersonAttributeType();
    statusAttributeType.setName(STATUS_ATTRIBUTE);
    statusAttributeType.setUuid(OpenmrsUtil.generateUid());
    PersonAttribute statusAttribute = new PersonAttribute();
    statusAttribute.setAttributeType(locationType);
    statusAttribute.setValue(PERSON_STATUS);
    person1.addAttribute(statusAttribute);
    return person1;
  }

  public static Person createAnotherPerson() {
    Person person2 = new Person();
    person2.setUuid(PERSON2_UUID);
    person2.setGender(PERSON2_GENDER);
    person2.setBirthdate(PERSON2_DOB);
    PersonName name1 = new PersonName();
    name1.setGivenName("NA");
    person2.addName(name1);

    PersonAddress personAddress = new PersonAddress();
    personAddress.setAddress1(PERSON2_ADDRESS1);
    personAddress.setAddress2(PERSON2_ADDRESS2);
    personAddress.setCityVillage(PERSON2_CITY_VILLAGE);
    personAddress.setPostalCode(PERSON2_POSTAL_CODE);
    person2.addAddress(personAddress);

    return person2;
  }

  public static Patient createPatient() {
    Patient patient = new Patient(createPerson());
    PatientIdentifier identifier = new PatientIdentifier();
    identifier.setIdentifier(PATIENT1_IDENTIFIER);
    identifier.setPreferred(true);
    PatientIdentifierType identifierType = new PatientIdentifierType();
    identifierType.setUuid(PATIENT_IDENTIFIER_TYPE);
    identifier.setIdentifierType(identifierType);
    Location location = new Location();
    location.setUuid(PATIENT1_LOCATION);
    identifier.setLocation(location);
    patient.addIdentifier(identifier);
    return patient;
  }

  public static Patient createAnotherPatient() {
    Patient patient = new Patient(createAnotherPerson());
    PatientIdentifier identifier = new PatientIdentifier();
    identifier.setIdentifier(PATIENT2_IDENTIFIER);
    identifier.setPreferred(true);
    PatientIdentifierType identifierType = new PatientIdentifierType();
    identifierType.setUuid(PATIENT_IDENTIFIER_TYPE);
    identifier.setIdentifierType(identifierType);
    Location location = new Location();
    location.setUuid(PATIENT2_LOCATION);
    identifier.setLocation(location);
    patient.addIdentifier(identifier);
    return patient;
  }

  public static List<Patient> createMultiplePatients() {
    List<Patient> patients = new ArrayList<>();
    patients.add(createPatient());
    patients.add(createAnotherPatient());
    return patients;
  }

  public static List<BiometricMatchingResult> buildSingleBiometricMatchingResponse() {
    List<BiometricMatchingResult> participants = new ArrayList<>();
    BiometricMatchingResult result1 = new BiometricMatchingResult();
    result1.setId(PARTICIPANT_ID);
    result1.setMatchingScore(50);
    participants.add(result1);
    return participants;
  }

  public static List<BiometricMatchingResult> buildMultipleBiometricMatchingResponse() {
    List<BiometricMatchingResult> participants = new ArrayList<>();
    BiometricMatchingResult result1 = new BiometricMatchingResult();
    result1.setId(PARTICIPANT_ID);
    result1.setMatchingScore(50);
    participants.add(result1);

    BiometricMatchingResult result2 = new BiometricMatchingResult();
    result2.setId(PARTICIPANT_ID);
    result2.setMatchingScore(HIGH_MATCHING_SCORE);
    participants.add(result2);
    return participants;
  }

  public static void buildSingleOpenMRSResponse() {
    MatchResponse matchResponse = new MatchResponse();
    matchResponse.setParticipantId(PARTICIPANT_ID);
    matchResponse.setGender("M");
    matchResponse.setMatchingScore(HIGH_MATCHING_SCORE);
    matchResponse.setMatchWith("BOTH");
    List<MatchResponse> matchResponses = new ArrayList<>();
    matchResponses.add(matchResponse);
  }

  public static void buildMultipleOpenMRSResponse() {
    List<BiometricMatchingResult> participants = new ArrayList<>();

    BiometricMatchingResult result1 = new BiometricMatchingResult();
    result1.setId(PARTICIPANT_ID);
    result1.setMatchingScore(50);
    participants.add(result1);

    BiometricMatchingResult result2 = new BiometricMatchingResult();
    result2.setId(PARTICIPANT_ID);
    result2.setMatchingScore(HIGH_MATCHING_SCORE);
    participants.add(result2);

    MatchResponse matchResponse = new MatchResponse();
    matchResponse.setParticipantId(PARTICIPANT_ID);
    matchResponse.setGender("M");
    matchResponse.setMatchingScore(result2.getMatchingScore());
    matchResponse.setMatchWith("BOTH");

    List<MatchResponse> matchResponses = new ArrayList<>();
    matchResponses.add(matchResponse);
  }

  public static Date convertUTCToDate(String date) {
    if (!StringUtils.isBlank(date)) {
      try {
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return utcFormat.parse(date);
      } catch (ParseException e) {
        throw new RuntimeException(
            "Date format needs to be in UTC format. Incorrect Date:" + date + ".", e);
      }
    }
    return null;
  }

  public static Visit createVisit() {
    Visit visit = new Visit();
    visit.setVisitId(1);
    visit.setStartDatetime(new Date());
    VisitAttribute visitAttribute = new VisitAttribute();
    VisitAttributeType visitAttributeType = new VisitAttributeType();
    visitAttributeType.setName("SCHEDULED");
    visitAttribute.setAttributeType(visitAttributeType);
    visitAttribute.setValueReferenceInternal("SCHEDULED");
    visit.setLocation(new Location(1));
    visit.addAttribute(visitAttribute);
    visit.setPatient(createPatient());
    visit.setDateCreated(new Date());
    return visit;
  }

  public static Visit updateVisitStatusAttriute(Visit updatedVisitObj) {
    VisitAttribute visitAttribute = new VisitAttribute();
    VisitAttributeType visitAttributeType = new VisitAttributeType();
    visitAttributeType.setName("OCCURRED");
    visitAttribute.setAttributeType(visitAttributeType);
    updatedVisitObj.setAttribute(visitAttribute);
    return updatedVisitObj;
  }

  public static Encounter createEncounter() {
    Encounter encounter = new Encounter();
    EncounterType type = new EncounterType();
    type.setName(ENCOUNTER_TYPE_DOSING);
    Patient patient = createPatient();
    patient.setId(1);
    encounter.setPatient(patient);
    encounter.setEncounterType(type);
    encounter.setEncounterDatetime(new Date());
    encounter.setLocation(createLocation());
    encounter.setVisit(updateVisitStatusAttriute(createVisit()));
    return encounter;
  }

  public static Encounter visitEncounter(int year, int month, int date, int hour, int min,
      int sec) {
    Encounter encounter = new Encounter();
    encounter.setEncounterDatetime(new Date());
    Calendar cal = Calendar.getInstance();
    cal.set(year, month, date, hour, min,
        sec); //Year, month, day of month, hours, minutes and seconds
    Date date1 = cal.getTime();
    encounter.setDateCreated(date1);
    return encounter;
  }

  public static Encounter updateEncounterWithObservations() throws ParseException {
    Encounter encounter = createEncounter();
    Concept concept = new Concept();
    ConceptName conceptName = new ConceptName();
    conceptName.setName("Vaccine Manufacture");
    conceptName.setLocale(Locale.ENGLISH);
    concept.setShortName(conceptName);
    ConceptDatatype conceptDatatype = new ConceptDatatype();
    conceptDatatype.setHl7Abbreviation("ST");
    concept.setDatatype(conceptDatatype);

    Obs obs = new Obs();
    obs.setEncounter(encounter);
    obs.setConcept(concept);
    obs.setPerson(encounter.getPatient().getPerson());
    obs.setObsDatetime(new Date());

    obs.setValueAsString("Pfizer");
    Set<Obs> obsSet = new HashSet<>();
    obsSet.add(obs);
    encounter.setObs(obsSet);
    return encounter;
  }


  public static Location createLocation() {
    Location location = new Location();
    location.setUuid(LOCATION_UUID);
    return location;
  }
}
