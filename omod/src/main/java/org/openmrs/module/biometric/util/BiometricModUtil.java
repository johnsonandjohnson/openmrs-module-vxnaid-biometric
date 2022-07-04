/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.util;

import static org.openmrs.module.biometric.constants.BiometricModConstants.ENABLE_BIOMETRIC;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.contract.MatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility methods for Biometric module.
 */
@Component
public class BiometricModUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(BiometricModUtil.class);
  private static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  private static final String SPACE_CHARCTER = "\\s+";
  private static final String EMPTY_STRING = "";

  @Autowired
  private ObjectMapper mapper;

  /**
   * Converts json string to pojo.
   *
   * @param json json string
   * @param type class details
   * @param <T>  class type
   * @return pojo
   * @throws IOException in case of bad input
   */
  public <T> T jsonToObject(final String json, final Class<T> type) throws IOException {
    return mapper.readValue(json, type);
  }

  /**
   * Converts json string to pojo.
   *
   * @param json json string
   * @param type class details
   * @param <T>  class type
   * @return pojo
   * @throws IOException in case of bad input
   */
  public <T> T jsonToObject(final String json, TypeReference type) throws IOException {
    return mapper.readValue(json, type);
  }

  /**
   * Converts json string to Object.
   *
   * @param json json string
   * @return @ see JsonNode
   * @throws IOException in case of any exceptions
   */
  public JsonNode toJsonNode(final String json) throws IOException {
    return mapper.readTree(json);
  }

  public String toJsonString(final Object object) throws IOException {
    return mapper.writer().writeValueAsString(object);
  }

  /**
   * Find the identifier from Patient.
   *
   * @param patient @see org.openmrs.Patient
   * @return patient identifier
   */
  public String findIdentifierFromPatient(Patient patient) {
    //To-Do
    //assuming there is one patient identifier in the application
    return patient.getPatientIdentifier().getIdentifier();
  }

  /**
   * Checks whether the given participantId is present in list of matched response or not.
   *
   * @param participantId unique participantId
   * @param responseList  list of matched response
   * @return true if participantId is present or false if participantId is not present
   */
  public boolean containsParticipant(String participantId, List<MatchResponse> responseList) {
    for (MatchResponse response : responseList) {
      LOGGER.info("Compare participant Ids :: {} , {}", participantId, response.getParticipantId());
      if (response.getParticipantId().equalsIgnoreCase(participantId)) {
        return true;
      }
    }
    LOGGER.info("Participant Ids not matched so returning false ");
    return false;
  }

  /**
   * Get the person address property.
   *
   * @param address  @see org.openmrs.PersonAddress
   * @param property property name
   * @return person address property
   */
  public String getPersonAddressProperty(PersonAddress address, String property) {
    if (address == null) {
      return null;
    }

    try {
      Class<?> personAddressClass = Context.loadClass("org.openmrs.PersonAddress");
      Method getPersonAddressProperty;
      getPersonAddressProperty =
          personAddressClass.getMethod("get" + property.substring(0, 1).toUpperCase() + property.substring(1));
      return (String) getPersonAddressProperty.invoke(address);
    } catch (Exception e) {
      throw new APIException("Invalid property name " + property + " passed to getPersonAddressProperty", e);
    }
  }

  /**
   * Converts UTC format to date.
   *
   * @param date UTC format date
   * @return date
   */
  public Date convertUTCToDate(String date) {
    if (!StringUtils.isBlank(date)) {
      try {
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return utcFormat.parse(date);
      } catch (ParseException e) {
        throw new APIException("Date format needs to be in UTC format. Incorrect Date:" + date + ".", e);
      }
    }
    return null;
  }

  /**
   * Converts ISO string to date object.
   *
   * @param date date in string format
   * @return date object
   */
  public Date convertIsoStringToDate(String date) throws ParseException {
    DateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
    return sdf.parse(date);
  }

  /**
   * Converts date object to ISO8601 string format.
   *
   * @return ISO8601 string format
   */
  public String dateToISO8601(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
    return sdf.format(date);
  }

  /**
   * Merges two list of patients to one list of patients.
   *
   * @param list1 first list of patints
   * @param list2 second list of patients
   * @return meerged list of patients
   */
  public List<PatientResponse> mergePatients(List<PatientResponse> list1, List<PatientResponse> list2) {
    return new ArrayList<>(Stream
        .of(list1, list2)
        .flatMap(List::stream)
        .collect(Collectors.toMap(PatientResponse::getParticipantId, Function.identity(),
            (PatientResponse x, PatientResponse y) -> x))
        .values());
  }

  /**
   * Remove white spaces in a string.
   *
   * @param str input string
   * @return string without white spaces
   */
  public String removeWhiteSpaces(String str) {
    return str.replaceAll(SPACE_CHARCTER, EMPTY_STRING);
  }

  /**
   * Get the oldest encounter for the given list of visits.
   *
   * @param visits list of visits
   * @return oldest encounter
   */
  public List<Visit> getOldestEncounterInVisit(List<Visit> visits) {

    for (Visit visit : visits) {
      List<Encounter> encounterListForVisit = visit.getNonVoidedEncounters();
      if (null != encounterListForVisit && !encounterListForVisit.isEmpty() && encounterListForVisit.size() > 1) {
        encounterListForVisit.sort(Comparator.comparingLong(v -> v.getDateCreated().getTime()));
        visit.setEncounters(Collections.emptySet());
        visit.setEncounters(Collections.singleton(encounterListForVisit.get(0)));
      }
    }
    return visits;
  }

  /**
   * Check if a location exists with the specified location UUID.
   *
   * @param locationUuid location UUID
   * @return true, if location exists false, if location does not exists
   */
  public boolean isLocationExists(String locationUuid) {
    return null != Context.getLocationService().getLocationByUuid(locationUuid);
  }

  /**
   * Checks if a participant exists with the given uuid.
   *
   * @param personUuid person uuid
   * @return true if participant exists otherwise false
   */
  public boolean isParticipantExists(String personUuid) {
    return null != Context.getPersonService().getPersonByUuid(personUuid);
  }

  /**
   * Check if the biometric feature is enabled or not.
   *
   * @return true if it is enabled
   */
  public boolean isBiometricFeatureEnabled() {
    return Boolean.parseBoolean(Context.getAdministrationService().getGlobalProperty(ENABLE_BIOMETRIC));

  }

  /**
   * Creates a PersonAttribute object..
   *
   * @param attributeType  attribute type
   * @param attributeValue attribute value
   * @return AttributeData obejct
   */
  public final AttributeData createAttribute(String attributeType, String attributeValue) {
    AttributeData attributeData = new AttributeData();
    attributeData.setType(attributeType);
    attributeData.setValue(attributeValue);
    return attributeData;
  }

  /**
   * Set person attribute value.
   *
   * @param patientUuid    patient uuid
   * @param attributeType  attribute type
   * @param attributeValue attribute value
   */
  public final void setPersonAttributeValue(String patientUuid, String attributeType, String attributeValue) {
    PersonService personService = Context.getPersonService();
    Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);

    if (patient != null && patient.getPatientIdentifier() != null) {
      PersonAttributeType personAttributeType = personService.getPersonAttributeTypeByName(attributeType);
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

  public final void validateUuids(Set<String> uuids) throws EntityValidationException {
    if (uuids.isEmpty()) {
      throw new EntityValidationException("Uuid list cannot be empty");
    }
  }

}
