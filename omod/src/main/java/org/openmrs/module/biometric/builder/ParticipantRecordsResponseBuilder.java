/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.builder;

import org.codehaus.jackson.JsonNode;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.api.util.OpenMRSUtil;
import org.openmrs.module.biometric.api.util.SecurityUtil;
import org.openmrs.module.biometric.contract.sync.Gender;
import org.openmrs.module.biometric.contract.sync.ParticipantData;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.SYNC_DELETE;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.SYNC_UPDATE;

/**
 * Builder class.
 */
@Component
public class ParticipantRecordsResponseBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantRecordsResponseBuilder.class);
  private static final String ADDRESS_FIELDS = "addressFields";
  private static final String FIELD = "field";
  private static final String COUNTRY = "country";

  @Autowired
  private BiometricModUtil util;

  @Autowired
  private SyncResponseBuilder syncResponseBuilder;

  @Autowired
  @Qualifier("biometric.configService")
  private ConfigService configService;

  /**
   * to build response for sync participant calls.
   *
   * @param patients    list of patients
   * @param totalCount  total count of patients
   * @param voidedCount deactivated count of patients
   * @param syncRequest sync request object
   * @return patient details
   */
  public SyncResponse createFrom(List<Patient> patients, long totalCount, long voidedCount, SyncRequest syncRequest)
      throws EntityNotFoundException, IOException {
    List<ParticipantData> participants = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);

    for (Patient patient : patients) {
      ParticipantData participantData = new ParticipantData();
      if (Boolean.TRUE.equals(patient.getVoided())) {
        participantData.setType(SYNC_DELETE);
      } else {
        participantData.setType(SYNC_UPDATE);
      }
      participantData.setParticipantUuid(SecurityUtil.sanitizeOutput(patient.getUuid()));
      participantData.setDateModified(OpenMRSUtil.getLastModificationDate(patient).getTime());
      participantData.setParticipantId(getSafePatientIdentifier(patient));
      if (Boolean.FALSE.equals(patient.getVoided())) {
        participantData.setBirthDate(util.dateToISO8601(patient.getBirthdate()));
        participantData.setGender(Gender.valueOf(patient.getGender()));

        List<AttributeData> attributes = new ArrayList<>(10);
        for (PersonAttribute personAttribute : patient.getPerson().getActiveAttributes()) {
          AttributeData attribute = new AttributeData();
          attribute.setType(SecurityUtil.sanitizeOutput(personAttribute.getAttributeType().getName()));
          attribute.setValue(SecurityUtil.sanitizeOutput(personAttribute.getValue()));
          attributes.add(attribute);
        }
        participantData.setAttributes(attributes);
        participantData.setAddresses(getAddress(patient));
      }
      participants.add(participantData);
    }

    return syncResponseBuilder.createFrom(participants, totalCount, null, voidedCount, syncRequest);
  }

  private String getSafePatientIdentifier(Patient patient) {
    PatientIdentifier result = null;

    for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
      if (result == null ||
          (Boolean.TRUE.equals(result.getVoided()) && Boolean.FALSE.equals(patientIdentifier.getVoided())) ||
          (Boolean.FALSE.equals(patientIdentifier.getVoided()) && Boolean.TRUE.equals(patientIdentifier.getPreferred()))) {
        result = patientIdentifier;
      }
    }

    return result != null ? result.getIdentifier() : "";
  }

  private Map<String, String> getAddress(Patient patient) throws IOException, EntityNotFoundException {
    Map<String, String> addressMap = new HashMap<>();
    String country = util.getPersonAddressProperty(patient.getPerson().getPersonAddress(), COUNTRY);

    if (null != country) {
      final JsonNode countryJsonNode = getPersonAddressConfiguration(country);

      if (countryJsonNode == null) {
        LOGGER.error("Missing Address Configuration for country: {}", country);
      } else {
        Iterator<JsonNode> iter = countryJsonNode.getElements();

        while (iter.hasNext()) {
          String fieldName = iter.next().get(FIELD).asText();
          String value = util.getPersonAddressProperty(patient.getPerson().getPersonAddress(), fieldName);
          addressMap.put(SecurityUtil.sanitizeOutput(fieldName), SecurityUtil.sanitizeOutput(value));
        }
        addressMap.put(COUNTRY, SecurityUtil.sanitizeOutput(patient.getPerson().getPersonAddress().getCountry()));
      }
    }
    return addressMap;
  }

  private JsonNode getPersonAddressConfiguration(String country) throws EntityNotFoundException, IOException {
    return util.toJsonNode(configService.retrieveConfig(BiometricApiConstants.MAIN_CONFIG)).get(ADDRESS_FIELDS).get(country);
  }
}
