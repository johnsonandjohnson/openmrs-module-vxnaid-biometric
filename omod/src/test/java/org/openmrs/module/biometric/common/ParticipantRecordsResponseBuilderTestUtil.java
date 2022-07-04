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


import java.util.Date;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncResponse;
import org.openmrs.module.biometric.contract.sync.SyncScope;
import org.openmrs.module.biometric.contract.sync.SyncStatus;

public class ParticipantRecordsResponseBuilderTestUtil {

  public static SyncScope createSyncScope() {
    SyncScope syncScope = new SyncScope();
    syncScope.setSiteUuid("8gi19999-h1af-9899-b684-851abfbac4d9");
    syncScope.setCountry("BELGIUM");
    return syncScope;
  }

  public static SyncRequest createSyncRequest() {
    SyncRequest syncRequest = new SyncRequest();
    syncRequest.setDateModifiedOffset(System.currentTimeMillis());
    syncRequest.setSyncScope(createSyncScope());
    syncRequest.setOptimize(false);
    syncRequest.setOffset(3);
    syncRequest.setLimit(10);
    return syncRequest;
  }

  public static PersonAttribute createPersonAttribute() {
    PersonAttribute personAttribute = new PersonAttribute();
    personAttribute.setValue("aertr");
    personAttribute.setId(456);
    PersonAttributeType personAttributeType = new PersonAttributeType();
    personAttributeType.setName("aseee");
    personAttribute.setAttributeType(personAttributeType);
    return personAttribute;
  }

  public static PersonAddress createPersonAddress() {
    PersonAddress personAddress = new PersonAddress();
    personAddress.setCountry("Belgium");
    return personAddress;
  }

  public static Person createPerson() {
    Person person = new Person();
    person.setGender("M");
    person.setBirthdate(new Date());
    PersonName name1 = new PersonName();
    name1.setGivenName("NA");
    person.addName(name1);
    person.addAttribute(createPersonAttribute());
    person.addAddress(createPersonAddress());
    return person;
  }

  public static Patient createPatient() {
    Patient patient = new Patient(createPerson());
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
    patient.setDateCreated(new Date());
    patient.setDateChanged(new Date());
    return patient;
  }

  public static SyncResponse createSyncResponse() {
    SyncResponse syncResponse = new SyncResponse();
    syncResponse.setOptimize(true);
    syncResponse.setSyncScope(createSyncScope());
    syncResponse.setSyncStatus(SyncStatus.OK);
    syncResponse.setLimit(3);
    return syncResponse;
  }
}
