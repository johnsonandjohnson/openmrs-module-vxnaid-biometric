/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.web.controller;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.module.biometric.api.contract.PatientResponse;

public class TestUtil {

  public static final String LOCATION_UUID = "9dd05e68-4d77-4b61-b2bf-436857f35aa0";
  public static final String COUNTRY = "BELGIUM";

  static Person createPerson() {
    Person person = new Person();
    person.setGender("M");
    person.setBirthdate(new Date());
    PersonName name1 = new PersonName();
    name1.setGivenName("NA");
    person.addName(name1);
    return person;
  }


  static Patient createPatient(Person person) {
    Patient patient = new Patient(person);
    PatientIdentifier identifier = new PatientIdentifier();
    identifier.setIdentifier("btest1");
    identifier.setPreferred(true);
    PatientIdentifierType identifierType = new PatientIdentifierType();
    identifierType.setUuid("8dd05e68-4d77-4b61-b2bf-436857f35aa0");
    identifier.setIdentifierType(identifierType);
    Location location = new Location();
    location.setUuid("9dd05e68-4d77-4b61-b2bf-436857f35aa0");
    identifier.setLocation(location);
    patient.addIdentifier(identifier);
    return patient;
  }

  static Visit createVisit() {
    Visit visit = new Visit();
    visit.setUuid("99ecb524-9c5a-7822-a449-cab1be10ki95");
    visit.setPatient(new Patient());
    VisitType visitType = new VisitType("Other", "");
    visit.setVisitType(visitType);
    visit.setStartDatetime(new Date());
    Location location = new Location();
    location.setUuid("9dd05e68-4d77-4b61-b2bf-436857f35aa0");
    visit.setLocation(location);
    return visit;
  }

  static User createUser() {
    PersonName name1 = new PersonName();
    name1.setGivenName("NA");
    User user = new User();
    user.addName(name1);
    user.setName("myName");
    return user;
  }

  public static Location createLocation() {
    Location location = new Location();
    location.setCountry(COUNTRY);
    location.setUuid(LOCATION_UUID);
    return location;
  }

  public static PatientResponse createPatientResponse() {
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setParticipantId("8dd05e68-4d77-4b61-b2bf-436857f35aa0");
    return patientResponse;
  }

}
