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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.service.ParticipantService;
import org.openmrs.module.biometric.contract.ParticipantMatchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Match Response Builder
 */
@Component
public class ParticipantMatchResponseBuilder {

  private static final String MATCH_BOTH = "BOTH";
  private static final String MATCH_OPENMRS = "OPENMRS";

  @Autowired
  private ParticipantService participantService;

  /**
   * To create list of match response objects from participants and participantId.
   */
  public final List<ParticipantMatchResponse> createFrom(
      List<BiometricMatchingResult> biometricResults,
      List<PatientResponse> patients) throws IOException, BiometricApiException {

    List<ParticipantMatchResponse> responseList = new ArrayList<>();

    Map<String, BiometricMatchingResult> biometricResultsMap = convertBiometricListToMap(
        biometricResults);
    // search params contains only biometric template
    if (patients.isEmpty()) {
      for (BiometricMatchingResult result : biometricResults) {
        String participantId = result.getId();
        List<PatientResponse> responses = participantService.findByParticipantId(participantId);
        PatientResponse response = null;
        // include the participant only if the participant data is present in openmrs
        if (!CollectionUtils.isEmpty(responses)) {
          response = responses.get(0);
          ParticipantMatchResponse matchResponse = buildParticipantResponse(response,
              result.getMatchingScore(),
              MATCH_BOTH);
          responseList.add(matchResponse);
        }
      }
    } else {
      // search params contains participant info and/or biometric template
      for (PatientResponse patient : patients) {
        int matchingScore = 0;
        String matchWIth = MATCH_OPENMRS;
        if (biometricResultsMap.containsKey(patient.getParticipantId())) {
          matchingScore = biometricResultsMap.get(patient.getParticipantId()).getMatchingScore();
          matchWIth = MATCH_BOTH;
        }
        ParticipantMatchResponse matchResponse = buildParticipantResponse(patient,
            matchingScore, matchWIth);
        responseList.add(matchResponse);
      }
    }
    return responseList;
  }

  private ParticipantMatchResponse buildParticipantResponse(PatientResponse response, int score,
      String matchWith) {
    ParticipantMatchResponse matchResponse = new ParticipantMatchResponse();
    matchResponse.setUuid(response.getParticipantUuid());
    matchResponse.setGender(response.getGender());
    matchResponse.setParticipantId(response.getParticipantId());
    matchResponse.setBirthDate(response.getBirthDate());
    matchResponse.setAddresses(response.getAddresses());
    matchResponse.setAttributes(response.getAttributes());
    matchResponse.setMatchWith(matchWith);
    matchResponse.setMatchingScore(score);
    return matchResponse;
  }

  private Map<String, BiometricMatchingResult> convertBiometricListToMap(
      List<BiometricMatchingResult> biometricMatchResults) {
    return biometricMatchResults.stream().collect(Collectors.toMap(BiometricMatchingResult::getId,
        Function.identity(), (id1, id2) -> id1));
  }
}
