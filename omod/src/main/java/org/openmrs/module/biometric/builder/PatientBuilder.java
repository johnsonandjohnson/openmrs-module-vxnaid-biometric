/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.builder;

import static org.openmrs.module.biometric.constants.BiometricModConstants.OPEN_MRS_ID;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.contract.Address;
import org.openmrs.module.biometric.contract.RegisterRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Patient Builder
 */
@Component
public class PatientBuilder {

  private static final String GIVEN_NAME = "NA";
  private static final String FIRST_SYNC_DATE = "firstSyncDate";

  @Autowired
  private BiometricModUtil util;

  @Autowired
  private LocationUtil locationUtil;

  @Autowired
  private PatientService patientService;

  @Autowired
  private PersonService personService;

  /**
   * Creates Patient entity from @code {RegistrationRequest}
   *
   * @param request Registration request
   * @return Patient @see org.openmrs.Patient
   */
  public Patient createFrom(RegisterRequest request)
      throws EntityNotFoundException, ParseException {
    Patient patient = new Patient();
    patient.setUuid(request.getParticipantUuid());
    patient.setDateCreated(util.convertIsoStringToDate(request.getRegistrationDate()));
    patient.setPersonDateCreated(util.convertIsoStringToDate(request.getRegistrationDate()));
    patient.setDateChanged(new Date());
    patient.setPersonDateChanged(new Date());
    patient.setGender(request.getGender());
    patient.setBirthdate(util.convertIsoStringToDate(request.getBirthdate()));
    PatientIdentifierType patientIdentifierType = patientService
        .getPatientIdentifierTypeByName(OPEN_MRS_ID);
    PatientIdentifier patientIdentifier = new PatientIdentifier();

    patientIdentifier.setIdentifierType(patientIdentifierType);
    //remove all white space characters in the participant id
    String participantId = util.removeWhiteSpaces(request.getParticipantId());
    patientIdentifier.setIdentifier(participantId);
    patientIdentifier.setPreferred(Boolean.TRUE);

    String locationUuid = locationUtil.getLocationUuid(request.getAttributes());
    patientIdentifier.setLocation(locationUtil.getLocationByUuid(locationUuid));
    patient.addIdentifier(patientIdentifier);

    // store first sync date of a participant, actual registration date will be stored in dateCreated field
    List<AttributeData> attributes = request.getAttributes();
    attributes.add(util.createAttribute(FIRST_SYNC_DATE, util.dateToISO8601(new Date())));

    for (AttributeData attr : attributes) {
      PersonAttribute attribute = new PersonAttribute();
      attribute.setValue(attr.getValue());
      PersonAttributeType personAttributeType = personService
          .getPersonAttributeTypeByName(attr.getType());
      if (null == personAttributeType) {
        throw new EntityNotFoundException(
            String.format("Attribute type : %s does not exists", attr.getType()));
      }
      attribute.setAttributeType(personAttributeType);
      patient.addAttribute(attribute);
    }

    Set<PersonAddress> personAddresses = new HashSet<>(10);
    for (Address address : request.getAddresses()) {
      PersonAddress personAddress = new PersonAddress();
      personAddress.setAddress1(address.getAddress1());
      personAddress.setAddress2(address.getAddress2());
      personAddress.setCityVillage(address.getCityVillage());
      personAddress.setCountyDistrict(address.getCountyDistrict());
      personAddress.setStateProvince(address.getStateProvince());
      personAddress.setPostalCode(address.getPostalCode());
      personAddress.setCountry(address.getCountry());
      personAddresses.add(personAddress);
    }
    patient.setAddresses(personAddresses);
    PersonName personName = new PersonName();
    //currently hardcoded,  as the name is currently not captured
    personName.setGivenName(GIVEN_NAME);
    patient.addName(personName);
    return patient;
  }
}
