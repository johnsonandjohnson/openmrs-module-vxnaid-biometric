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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PARTICIPANT_ID;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PERSON1_COUNTRY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.api.service.ParticipantService;
import org.openmrs.module.biometric.common.BiometricTestUtil;
import org.openmrs.module.biometric.constants.BiometricTestConstants;
import org.openmrs.module.biometric.contract.MatchResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;

@RunWith(MockitoJUnitRunner.class)
public class MatchResponseBuilderTest {

  private static final String CONFIG1 = "main";
  private List<Patient> patients = new ArrayList<>();
  private Patient patient1;
  private List<BiometricMatchingResult> biometricMatchingResults = new ArrayList<>();
  @Mock
  private ParticipantService participantService;

  @Mock
  private ConfigService configService;

  @Mock
  private BiometricModUtil biometricModUtil;

  @InjectMocks
  private MatchResponseBuilder matchResponseBuilder;

  @Before
  public void setUp() {
    patient1 = BiometricTestUtil.createPatient();
    patients.add(patient1);
    biometricMatchingResults = BiometricTestUtil.buildSingleBiometricMatchingResponse();
  }

  @Test
  public void createFrom_shouldReturnSingleMatchIfParticipantIdRequestParamPresentInBiometricMatchResults()
      throws IOException, EntityNotFoundException {
    //Given
    JsonNode jsonNode = new ObjectMapper().readTree(ControllerTestHelper.loadFile("config.json"));
    when(biometricModUtil.toJsonNode(anyString())).thenReturn(jsonNode);
    when(biometricModUtil.containsParticipant(anyString(), anyListOf(MatchResponse.class)))
        .thenReturn(true);
    when(participantService.retrieveParticipantDetails(PARTICIPANT_ID)).thenReturn(patients);
    when(biometricModUtil.findIdentifierFromPatient(any(Patient.class))).thenReturn("test1");
    PersonAddress personAddress = patients.get(0).getPerson().getPersonAddress();
    when(biometricModUtil.getPersonAddressProperty(personAddress, BiometricTestConstants.COUNTRY))
        .thenReturn(PERSON1_COUNTRY);
    when(configService.retrieveConfig(CONFIG1))
        .thenReturn(ControllerTestHelper.loadFile("config.json"));

    List<MatchResponse> matchResults = matchResponseBuilder
        .createFrom(biometricMatchingResults, PARTICIPANT_ID);

    assertThat(matchResults.size(), equalTo(1));
    verify(participantService, times(1)).retrieveParticipantDetails(PARTICIPANT_ID);
    verify(configService, times(1)).retrieveConfig(anyString());
    verify(biometricModUtil, times(1))
        .containsParticipant(anyString(), anyListOf(MatchResponse.class));
    verify(biometricModUtil, times(1))
        .getPersonAddressProperty(personAddress, BiometricTestConstants.COUNTRY);
  }

  @Test
  public void createFrom_shouldReturnTwoeMatchesIfParticipantIdRequestParamNotPresentInBiometricMatchResults()
      throws IOException, EntityNotFoundException {
    //Given
    JsonNode jsonNode = new ObjectMapper().readTree(ControllerTestHelper.loadFile("config.json"));
    when(biometricModUtil.toJsonNode(anyString())).thenReturn(jsonNode);
    when(biometricModUtil.containsParticipant(anyString(), anyListOf(MatchResponse.class)))
        .thenReturn(false);
    when(participantService.retrieveParticipantDetails(PARTICIPANT_ID)).thenReturn(patients);

    PersonAddress personAddress = patients.get(0).getPerson().getPersonAddress();
    when(biometricModUtil.getPersonAddressProperty(personAddress, BiometricTestConstants.COUNTRY))
        .thenReturn(PERSON1_COUNTRY);
    when(configService.retrieveConfig(CONFIG1))
        .thenReturn(ControllerTestHelper.loadFile("config.json"));

    List<MatchResponse> matchResults = matchResponseBuilder
        .createFrom(biometricMatchingResults, PARTICIPANT_ID);

    assertThat(matchResults.size(), equalTo(2));
    verify(biometricModUtil, times(1))
        .containsParticipant(anyString(), anyListOf(MatchResponse.class));
    verify(participantService, times(2)).retrieveParticipantDetails(PARTICIPANT_ID);
    verify(configService, times(2)).retrieveConfig(CONFIG1);
    verify(biometricModUtil, times(2))
        .getPersonAddressProperty(personAddress, BiometricTestConstants.COUNTRY);
  }

  @Test
  public void createFrom_shouldReturnNoResultsWhenParticipantIsNotPresentInOpenMRS()
      throws IOException, EntityNotFoundException {
    //Given
    when(biometricModUtil.containsParticipant(anyString(), anyListOf(MatchResponse.class)))
        .thenReturn(false);
    when(participantService.retrieveParticipantDetails(PARTICIPANT_ID)).thenReturn(null);

    List<MatchResponse> matchResults = matchResponseBuilder
        .createFrom(biometricMatchingResults, PARTICIPANT_ID);

    assertThat(matchResults.size(), equalTo(0));
    verify(biometricModUtil, times(1))
        .containsParticipant(anyString(), anyListOf(MatchResponse.class));
    verify(participantService, times(2)).retrieveParticipantDetails(PARTICIPANT_ID);
    verifyZeroInteractions(configService);
    verify(biometricModUtil, never()).getPersonAddressProperty(anyObject(), anyString());
  }
}

