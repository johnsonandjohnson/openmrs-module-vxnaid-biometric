/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.openmrs.api.APIException;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.exception.BiometricApiException;

/**
 * Uses Neurotechnology SDKs to register and match participants based on their biometric template.
 */
public interface BiometricService {

  /**
   * Register participant's biometrics.
   *
   * @param participantId id of a participant.
   * @param template participant's biometric template
   * @param registrationDate enrolment date of the participant
   * @param participantUuid openmrs person uuid of the participant
   * @return true, if the registration is success else false
   */
  boolean registerBiometricData(String participantId, byte[] template, String deviceId,
      String locationUuid,
      Date registrationDate, String participantUuid) throws APIException;

  /**
   * Match with participant's biometric data and return the participant id and matching score.
   *
   * @param template participant's biometric template
   * @param partcipantSet list of participants to be matched against the biometric template
   * @return the participant id and matching score, empty if there are no matching results
   */
  List<BiometricMatchingResult> matchBiometricData(byte[] template, Set<String> partcipantSet)
      throws APIException, BiometricApiException;

  /**
   * Delete participant's biometric template.
   *
   * @param participantId participant identifier
   * @return status of the template deletion
   */
  boolean voidBiometricData(String participantId);

  /**
   * Delete participant's biometric template.
   *
   * @param participantId participant identifier
   * @return status of the template deletion
   */
  boolean purgeBiometricData(String participantId);

}
