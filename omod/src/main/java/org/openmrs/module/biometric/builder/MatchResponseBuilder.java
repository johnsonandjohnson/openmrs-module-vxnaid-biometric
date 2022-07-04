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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonNode;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.api.service.ParticipantService;
import org.openmrs.module.biometric.contract.MatchResponse;
import org.openmrs.module.biometric.contract.ParticipantAttribute;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Match Response Builder.
 */
@Component
public class MatchResponseBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchResponseBuilder.class);
  private static final String ADDRESS_FIELDS = "addressFields";
  private static final String FIELD = "field";
  private static final String COUNTRY = "country";
  private static final String MAIN_CONFIG = "main";

  @Autowired
  private ParticipantService participantService;

  @Autowired
  private BiometricModUtil util;

  @Autowired
  private ConfigService configService;

  /**
   * To create list of match response objects from participants and participantId.
   *
   * @param participants list of participants with biometric matching
   * @param participantId participant id
   * @return list of matching responses
   */

  public List<MatchResponse> createFrom(List<BiometricMatchingResult> participants,
      String participantId)
      throws IOException, EntityNotFoundException {

    List<MatchResponse> responseList = new ArrayList<>();

    // biometric matching participant
    for (BiometricMatchingResult result : participants) {
      List<MatchResponse> matchResponses = buildResponse(result.getId(), result.getMatchingScore());

      if (null != matchResponses && !matchResponses.isEmpty()) {
        responseList.addAll(matchResponses);
      }
    }
    LOGGER.info("No. of matching results with template {} ", responseList.size());

    // retrieve participant based on participant parameter received from the request
    if (null != participantId && !util.containsParticipant(participantId, responseList)) {
      LOGGER.info("Adding the results based on participant Id passed in the request");
      List<MatchResponse> matchResponses = buildResponse(participantId, 0);
      // to check
      if (null != matchResponses && !matchResponses.isEmpty()) {
        responseList.addAll(matchResponses);
      }
    }
    return responseList;
  }

  /**
   * To build response from participantId and score
   *
   * @param participantId participant id
   * @param score score of matched participant
   * @return list of matching response
   */
  private List<MatchResponse> buildResponse(String participantId, int score)
      throws IOException, EntityNotFoundException {
    List<MatchResponse> responses = new ArrayList<>(10);

    //list of patient with like search
    List<Patient> patients = participantService.retrieveParticipantDetails(participantId);
    if (null == patients || patients.isEmpty()) {
      return Collections.emptyList();
    }

    for (Patient p : patients) {
      String patientIdentifier = util.findIdentifierFromPatient(p);
      MatchResponse matchResponse = new MatchResponse();
      matchResponse.setParticipantId(patientIdentifier);
      matchResponse.setGender(p.getGender());
      matchResponse.setBirthDate(p.getBirthdate().toString());

      if (score > 0 && participantId.equalsIgnoreCase(patientIdentifier)) {
        matchResponse.setMatchWith("BOTH");

      } else {
        matchResponse.setMatchWith("OPENMRS");
      }
      matchResponse.setMatchingScore(score);
      matchResponse.setUuid(p.getUuid());

      List<ParticipantAttribute> attributes = new ArrayList<>(10);
      for (PersonAttribute personAttribute : p.getPerson().getActiveAttributes()) {
        ParticipantAttribute attribute = new ParticipantAttribute();
        attribute.setType(personAttribute.getAttributeType().getName());
        attribute.setValue(personAttribute.getValue());
        attributes.add(attribute);
      }
      matchResponse.setAttributes(attributes);

      Map<String, String> addressMap = new HashMap<>();
      String country = util.getPersonAddressProperty(p.getPerson().getPersonAddress(), COUNTRY);

      if (null != country) {
        Iterator<JsonNode> iter = util.toJsonNode(configService.retrieveConfig(MAIN_CONFIG))
            .get(ADDRESS_FIELDS)
            .get(country).getElements();

        while (iter.hasNext()) {
          String fieldName = iter.next().get(FIELD).asText();
          String value = util.getPersonAddressProperty(p.getPerson().getPersonAddress(), fieldName);
          addressMap.put(fieldName, value);
        }
      }
      matchResponse.setAddresses(addressMap);
      responses.add(matchResponse);
    }
    return responses;
  }

}
