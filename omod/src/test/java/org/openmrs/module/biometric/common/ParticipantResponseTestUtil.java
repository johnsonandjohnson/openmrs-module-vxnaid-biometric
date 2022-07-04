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

import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.contract.PatientResponse;

public class ParticipantResponseTestUtil {

  public static BiometricMatchingResult createBiometricMatchingResultWithoutId() {
    BiometricMatchingResult biometricMatchingResult = new BiometricMatchingResult();
    biometricMatchingResult.setMatchingScore(12);
    return biometricMatchingResult;
  }

  public static BiometricMatchingResult createBiometricMatchingResultWithId() {
    BiometricMatchingResult biometricMatchingResult = new BiometricMatchingResult();
    biometricMatchingResult.setMatchingScore(34);
    biometricMatchingResult.setId("newId1");
    return biometricMatchingResult;
  }

  public static PatientResponse createPatientWithoutParticipantId() {
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setParticipantUuid("8gi19999-h1af-9899-b684-851abfbac89ie");
    return patientResponse;
  }

  public static PatientResponse createPatientWithParticipantId() {
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setParticipantUuid("8gi19999-h1af-9899-b684-851abfbac89ie");
    patientResponse.setParticipantId("newId1");
    return patientResponse;
  }

}
