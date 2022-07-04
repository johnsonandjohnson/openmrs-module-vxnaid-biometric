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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.service.ParticipantService;
import org.openmrs.module.biometric.common.ParticipantResponseTestUtil;
import org.openmrs.module.biometric.contract.ParticipantMatchResponse;

@RunWith(MockitoJUnitRunner.class)
public class ParticipantMatchResponseBuilderTest {

  @Mock
  private ParticipantService participantService;

  @InjectMocks
  private ParticipantMatchResponseBuilder participantMatchResponseBuilder;

  @Test
  public void createFrom_shouldReturnParticipantMatchResponseListWithBoth()
      throws BiometricApiException, IOException {

    BiometricMatchingResult biometricMatchingResult = ParticipantResponseTestUtil
        .createBiometricMatchingResultWithoutId();
    PatientResponse patientResponse = ParticipantResponseTestUtil.createPatientWithParticipantId();
    when(participantService.findByParticipantId(anyString()))
        .thenReturn(Arrays.asList(patientResponse));
    List<ParticipantMatchResponse> responseList = participantMatchResponseBuilder
        .createFrom(Arrays.asList(biometricMatchingResult), Arrays.asList(patientResponse));

    assertNotNull(responseList);
  }

  @Test
  public void createFrom_shouldReturnParticipantMatchResponseListWithOpenMrs()
      throws BiometricApiException, IOException {

    PatientResponse patientResponse = ParticipantResponseTestUtil.createPatientWithParticipantId();
    when(participantService.findByParticipantId(anyString()))
        .thenReturn(Arrays.asList(patientResponse));
    List<ParticipantMatchResponse> responseList = participantMatchResponseBuilder
        .createFrom(Collections.emptyList(), Arrays.asList(patientResponse));

    assertNotNull(responseList);
  }

  @Test
  public void createFrom_shouldReturnParticipantMatchResponseListWithId()
      throws BiometricApiException, IOException {

    BiometricMatchingResult biometricMatchingResult = ParticipantResponseTestUtil
        .createBiometricMatchingResultWithId();
    PatientResponse patientResponse = ParticipantResponseTestUtil.createPatientWithParticipantId();
    when(participantService.findByParticipantId(anyString()))
        .thenReturn(Arrays.asList(patientResponse));
    List<ParticipantMatchResponse> responseList = participantMatchResponseBuilder
        .createFrom(Arrays.asList(biometricMatchingResult), Arrays.asList(patientResponse));

    assertNotNull(responseList);
  }
}
