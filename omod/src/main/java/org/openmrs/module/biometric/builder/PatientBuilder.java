/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.biometric.builder;

import static org.openmrs.module.biometric.constants.BiometricModConstants.OPEN_MRS_ID;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.contract.Address;
import org.openmrs.module.biometric.contract.RegisterRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Patient Builder */
@Component
public class PatientBuilder {

  private static final String GIVEN_NAME = "NA";
  private static final String FIRST_SYNC_DATE = "firstSyncDate";

  @Autowired private BiometricModUtil util;

  @Autowired private LocationUtil locationUtil;

  @Autowired private PatientService patientService;

  @Autowired private PersonService personService;

  /**
   * Creates Patient entity from @code {RegistrationRequest}
   *
   * @param request Registration request
   * @return Patient @see org.openmrs.Patient
   */
  public Patient createFromRegisterRequest(RegisterRequest request)
      throws EntityNotFoundException, ParseException {
    Patient patient = new Patient();
    patient.setUuid(request.getParticipantUuid());
    patient.setDateCreated(util.convertIsoStringToDate(request.getRegistrationDate()));
    patient.setPersonDateCreated(util.convertIsoStringToDate(request.getRegistrationDate()));
    patient.setDateChanged(new Date());
    patient.setPersonDateChanged(new Date());
    patient.setGender(request.getGender());
    patient.setBirthdateEstimated(request.getIsBirthDateEstimated());
    patient.setBirthdate(util.convertIsoStringToDate(request.getBirthdate()));

    String locationUuid = locationUtil.getLocationUuid(request.getAttributes());
    Location location = locationUtil.getLocationByUuid(locationUuid);

    if (StringUtils.isNotBlank(request.getParticipantId())) {
      setIdentifier(patient, OPEN_MRS_ID, request.getParticipantId(), location);
    }

    if (StringUtils.isNotBlank(request.getNin())) {
      setIdentifier(patient, BiometricApiConstants.NIN_IDENTIFIER_NAME, request.getNin(), location);
    }

    // store first sync date of a participant, actual registration date will be stored in
    // dateCreated field
    List<AttributeData> attributes = request.getAttributes();
    attributes.add(util.createAttribute(FIRST_SYNC_DATE, util.dateToISO8601(new Date())));
    setAttributes(patient, attributes);
    setAddresses(patient, request.getAddresses());

    PersonName personName = new PersonName();
    // currently hardcoded,  as the name is currently not captured
    personName.setGivenName(GIVEN_NAME);
    patient.addName(personName);
    return patient;
  }

  public Patient createFromUpdateRequest(RegisterRequest request)
      throws EntityNotFoundException, ParseException {
    Patient patient = patientService.getPatientByUuid(request.getParticipantUuid());
    Date updateDate = util.convertIsoStringToDate(request.getUpdateDate());
    patient.setDateChanged(updateDate);
    patient.setPersonDateCreated(updateDate);
    patient.setGender(request.getGender());
    patient.setBirthdateEstimated(request.getIsBirthDateEstimated());
    patient.setBirthdate(util.convertIsoStringToDate(request.getBirthdate()));

    Location location =
        locationUtil.getLocationByUuid(locationUtil.getLocationUuid(request.getAttributes()));
    if (StringUtils.isNotBlank(request.getParticipantId())) {
      setIdentifier(patient, OPEN_MRS_ID, request.getParticipantId(), location);
    }

    if (StringUtils.isNotBlank(request.getNin())) {
      setIdentifier(patient, BiometricApiConstants.NIN_IDENTIFIER_NAME, request.getNin(), location);
    }

    setAttributes(patient, request.getAttributes());
    setAddresses(patient, request.getAddresses());

    return patient;
  }

  private void setIdentifier(
      Patient patient, String identifierTypeName, String identifierValue, Location location) {
    PatientIdentifierType patientIdentifierType =
        patientService.getPatientIdentifierTypeByName(identifierTypeName);
    if (patientIdentifierType == null) {
      throw new IllegalStateException(
          String.format("Missing identifier type name with name: %s", identifierTypeName));
    }

    PatientIdentifier patientIdentifier = patient.getPatientIdentifier(patientIdentifierType);
    if (patientIdentifier == null) {
      patientIdentifier = new PatientIdentifier();
      patientIdentifier.setIdentifierType(patientIdentifierType);
      patientIdentifier.setIdentifier(util.removeWhiteSpaces(identifierValue));
      patientIdentifier.setPreferred(
          StringUtils.equals(identifierTypeName, OPEN_MRS_ID) ? Boolean.TRUE : Boolean.FALSE);
      patientIdentifier.setLocation(location);
      patient.addIdentifier(patientIdentifier);
    } else {
      patientIdentifier.setIdentifier(identifierValue);
    }
  }

  private void setAttributes(Patient patient, List<AttributeData> attributes)
      throws EntityNotFoundException {
    for (AttributeData attr : attributes) {
      PersonAttribute attribute = new PersonAttribute();
      attribute.setValue(attr.getValue());
      PersonAttributeType personAttributeType =
          personService.getPersonAttributeTypeByName(attr.getType());
      if (null == personAttributeType) {
        throw new EntityNotFoundException(
            String.format("Attribute type : %s does not exists", attr.getType()));
      }
      attribute.setAttributeType(personAttributeType);
      patient.addAttribute(attribute);
    }
  }

  private void setAddresses(Patient patient, List<Address> addresses) {
    patient.getAddresses().stream()
        .filter(personAddress -> !personAddress.getVoided())
        .forEach(personAddress -> personService.voidPersonAddress(personAddress, "Old address"));

    Set<PersonAddress> personAddresses = patient.getAddresses();
    for (Address address : addresses) {
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
  }
}
