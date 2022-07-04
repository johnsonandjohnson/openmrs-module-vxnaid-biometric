/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.web.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.openmrs.module.biometric.common.BiometricTestUtil.VISIT_UUID;
import static org.openmrs.module.biometric.common.BiometricTestUtil.createVisit;
import static org.openmrs.module.biometric.common.BiometricTestUtil.visitEncounter;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.APIException;
import org.openmrs.module.biometric.api.service.VisitSchedulerService;
import org.openmrs.module.biometric.builder.EncounterBuilder;
import org.openmrs.module.biometric.builder.ObservationBuilder;
import org.openmrs.module.biometric.builder.VisitRequestBuilder;
import org.openmrs.module.biometric.builder.VisitResponseBuilder;
import org.openmrs.module.biometric.common.BiometricTestUtil;
import org.openmrs.module.biometric.contract.NewVisitResponse;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Consists of unit tests for the VisitController
 *
 * @see VisitController
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({SanitizeUtil.class})
public class VisitControllerTest {

  private static final String CREATE_VISIT_ENDPOINT = ControllerTestHelper.BASE_URL + "/visit";
  private static final String RETRIEVE_VISIT_ENDPOINT =
      ControllerTestHelper.BASE_URL + "/visit/{personUuid}";
  private static final String CREATE_ENCOUNTER_ENDPOINT =
      ControllerTestHelper.BASE_URL + "/encounter";
  private static final String CREATE_VISIT_JSON = "create_visit.json";
  private static final String CREATE_VISIT_INVALID_REQUEST_JSON = "create_visit_invalid_request.json";
  private static final String CREATE_ENCOUNTER_JSON = "create_encounter.json";
  private static final String CREATE_ENCOUNTER_INVALID_REQUEST = "create_encounter_invalid_request.json";
  private static final String PERSON_UUID = "person-image-uuid-value";
  private static final String DEVICE_HEADER_VALUE = "device1";
  private static final String DEVICE_HEADER_PARAM = "deviceId";
  private static final String CREATE_VISIT_REQUEST_BODY_JSON = "create_visit_request.json";

  private MockMvc mockMvc;

  @Mock
  private VisitRequestBuilder visitRequestBuilder;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private VisitResponseBuilder visitResponseBuilder;

  @Mock
  private VisitSchedulerService visitSchedulerService;

  @Mock
  private ObservationBuilder observationBuilder;

  @Mock
  private EncounterBuilder encounterBuilder;

  @Mock
  private BiometricModUtil util;

  @InjectMocks
  private VisitController visitController;

  private Visit visit;

  private Visit newVisit;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(visitController).build();
  }

  @Test
  public void createVisit_shouldCreateVisitWithVisitData() throws Exception {
    //Given
    String visitRequest = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    visit = BiometricTestUtil.createVisit();
    newVisit = new Visit();
    NewVisitResponse newVisitResponse = new NewVisitResponse();
    newVisitResponse.setVisitUuid(visit.getUuid());
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    when(util.isParticipantExists(anyString())).thenReturn(true);
    when(visitSchedulerService.findVisitsByUuids(anySetOf(String.class))).thenReturn(Collections.emptyList());
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    when(util.isParticipantExists("867d1cff-d8c5-4645-91d5-0a773a33c83b")).thenReturn(true);
    when(visitRequestBuilder.createFrom(request)).thenReturn(visit);
    when(visitSchedulerService.createOrUpdateVisit(visit)).thenReturn(newVisit);
    when(visitResponseBuilder.createFrom(newVisit)).thenReturn(newVisitResponse);

    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    //Then
    verify(visitRequestBuilder, times(1)).createFrom(request);
    verify(visitSchedulerService, times(1)).createOrUpdateVisit(visit);
    verify(visitResponseBuilder, times(1)).createFrom(newVisit);
  }

  @Test
  public void createVisit_ShouldThrow404ErrorWhenInvalidRequestIsSent() throws Exception {
    //Given
    String visitRequest = ControllerTestHelper.loadFile(CREATE_VISIT_INVALID_REQUEST_JSON);
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    //Then
    verifyNoMoreInteractions(visitRequestBuilder);
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitResponseBuilder);
  }

  @Test
  public void retrieveVisit_shouldRetrieveVisitIfPersonUuidIsPassed() throws Exception {
    //Given
    List<Visit> visits = new ArrayList<>();
    visit = createVisit();
    Encounter encounter = visitEncounter(2013, Calendar.JANUARY, 9, 11, 11, 12);
    visit.addEncounter(encounter);
    Encounter encounter1 = visitEncounter(2013, Calendar.JANUARY, 11, 9, 11, 12);
    visit.addEncounter(encounter1);
    visits.add(visit);
    when(visitSchedulerService.findVisitByPersonUuid(PERSON_UUID)).thenReturn(visits);
    List<Visit> visitsNew = new ArrayList<>();
    visit = createVisit();
    visit.addEncounter(encounter1);
    visitsNew.add(visit);
    when(util.getOldestEncounterInVisit(anyListOf(Visit.class))).thenReturn(visitsNew);
    //When
    ResultActions r = mockMvc.perform(
        get(RETRIEVE_VISIT_ENDPOINT, PERSON_UUID)
            .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    //Then
    assertNotNull(r);
    verify(visitSchedulerService, times(1)).findVisitByPersonUuid(PERSON_UUID);
  }

  @Test
  public void retrieveVisit_shouldNotRetrieveVisitIfVistsAreNotThere() throws Exception {
    //Given
    List<Visit> visits = new ArrayList<>();
    when(visitSchedulerService.findVisitByPersonUuid(PERSON_UUID)).thenReturn(visits);
    //When
    ResultActions r = mockMvc.perform(
        get(RETRIEVE_VISIT_ENDPOINT, PERSON_UUID)
            .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    //Then
    assertNotNull(r);
    verify(visitSchedulerService, times(1)).findVisitByPersonUuid(PERSON_UUID);
  }

  @Test
  public void createEncounter_ShouldCreateANewEncounter() throws Exception {
    //Given
    String encounterRequest = ControllerTestHelper.loadFile(CREATE_ENCOUNTER_JSON);
    VisitRequest request = new ObjectMapper().readValue(encounterRequest, VisitRequest.class);

    Visit existingVisit = BiometricTestUtil.createVisit();
    Set<VisitAttribute> visitAttributes = new HashSet<>();
    Visit updatedVisit = BiometricTestUtil.createVisit();
    updatedVisit.setAttributes(visitAttributes);

    Set<Obs> obsSet = new HashSet<>();
    Encounter encounterObj = BiometricTestUtil.createEncounter();
    encounterObj.setObs(obsSet);
    Encounter newEncounter = BiometricTestUtil.updateEncounterWithObservations();

    when(util.jsonToObject(encounterRequest, VisitRequest.class)).thenReturn(request);
    when(visitSchedulerService.findVisitByVisitUuid(VISIT_UUID)).thenReturn(existingVisit);
    when(visitRequestBuilder.createFrom(request, existingVisit)).thenReturn(updatedVisit);
    when(encounterBuilder.createFrom(request, existingVisit)).thenReturn(encounterObj);
    when(observationBuilder.createFrom(request, existingVisit.getPatient().getPerson()))
        .thenReturn(obsSet);
    when(visitSchedulerService.createEncounter(updatedVisit, encounterObj, obsSet))
        .thenReturn(newEncounter);

    NewVisitResponse encounterResponse = new NewVisitResponse();
    encounterResponse.setVisitUuid(VISIT_UUID);

    when(visitResponseBuilder.createFrom(newEncounter.getVisit())).thenReturn(encounterResponse);

    //When
    mockMvc.perform(
        post(CREATE_ENCOUNTER_ENDPOINT).content(encounterRequest)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    //Then
    verify(visitSchedulerService, times(1)).createEncounter(updatedVisit, encounterObj, obsSet);
    verify(visitRequestBuilder, times(1)).createFrom(request, existingVisit);
    verify(encounterBuilder, times(1)).createFrom(request, existingVisit);
    verify(observationBuilder, times(1))
        .createFrom(request, existingVisit.getPatient().getPerson());
    verify(visitSchedulerService, times(1)).createEncounter(updatedVisit, encounterObj, obsSet);
    verify(visitResponseBuilder, times(1)).createFrom(newEncounter.getVisit());
  }

  @Test
  public void createEncounter_ShouldThrow404ErrorWhenInvalidRequestIsSent() throws Exception {
    //Given
    String encounterRequest = ControllerTestHelper.loadFile(CREATE_ENCOUNTER_INVALID_REQUEST);
    VisitRequest request = new ObjectMapper().readValue(encounterRequest, VisitRequest.class);

    when(util.jsonToObject(encounterRequest, VisitRequest.class)).thenReturn(request);

    //When
    mockMvc.perform(
        post(CREATE_ENCOUNTER_ENDPOINT).content(encounterRequest)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    //Then
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitRequestBuilder);
    verifyNoMoreInteractions(encounterBuilder);
    verifyNoMoreInteractions(observationBuilder);
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitResponseBuilder);
  }

  @Test
  public void createEncounter_ShouldThrow500ErrorWhenVisitNotFound() throws Exception {
    //Given
    String encounterRequest = ControllerTestHelper.loadFile(CREATE_ENCOUNTER_JSON);
    VisitRequest request = new ObjectMapper().readValue(encounterRequest, VisitRequest.class);

    when(util.jsonToObject(encounterRequest, VisitRequest.class)).thenReturn(request);
    when(visitSchedulerService.findVisitByVisitUuid(VISIT_UUID))
        .thenThrow(new APIException(String.format("Visit with UUID :%s not found.", VISIT_UUID)));

    //When
    mockMvc.perform(
        post(CREATE_ENCOUNTER_ENDPOINT).content(encounterRequest)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    //Then
    verify(visitSchedulerService, times(1)).findVisitByVisitUuid(VISIT_UUID);
    verifyNoMoreInteractions(visitRequestBuilder);
    verifyNoMoreInteractions(encounterBuilder);
    verifyNoMoreInteractions(observationBuilder);
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitResponseBuilder);
  }

  @Test
  public void createVisit_ShouldThrow404ErrorWhenInvalidRequestWithNullVisitTypeIsSent()
      throws Exception {
    //Given
    String visitRequest = "{\"participantUuid\":\"867d1cff-d8c5-4645-91d5-0a773a33c83b\",\"startDatetime\":\"2021-02-10T04:09:25.000Z\",\"locationUuid\":\"42655fae-3b09-4b15-bb4b-296311546be4\",\"observations\":[{  \"name\" : \"Barcode\",  \"value\": \"dose1 visit\"}],\"attributes\":[{  \"type\":\"Visit Status\",  \"value\":\"OCCURRED\"}]}";
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    //Then
    verifyNoMoreInteractions(visitRequestBuilder);
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitResponseBuilder);
  }

  @Test
  public void createVisit_ShouldThrow404ErrorWhenInvalidRequestWithNullStartDateTimeIsSent()
      throws Exception {
    //Given
    String visitRequest = "{\"participantUuid\":\"867d1cff-d8c5-4645-91d5-0a773a33c83b\",\"visitType\":\"Other\",\"locationUuid\":\"42655fae-3b09-4b15-bb4b-296311546be4\",\"observations\":[{  \"name\" : \"Barcode\",  \"value\": \"dose1 visit\"}],\"attributes\":[{  \"type\":\"Visit Status\",  \"value\":\"OCCURRED\"}]}";
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    //Then
    verifyNoMoreInteractions(visitRequestBuilder);
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitResponseBuilder);
  }

  @Test
  public void createVisit_ShouldThrow404ErrorWhenInvalidRequestWithParticipantUuidIsSent()
      throws Exception {
    //Given
    String visitRequest = "{\"visitType\":\"Other\",\"startDatetime\":\"2021-02-10T04:09:25.000Z\",\"locationUuid\":\"42655fae-3b09-4b15-bb4b-296311546be4\",\"observations\":[{  \"name\" : \"Barcode\",  \"value\": \"dose1 visit\"}],\"attributes\":[{  \"type\":\"Visit Status\",  \"value\":\"OCCURRED\"}]}";
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    //Then
    verifyNoMoreInteractions(visitRequestBuilder);
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitResponseBuilder);
  }

  @Test
  public void getVisitsByUuids_shouldReturnOkStatus() throws Exception {
    String body = "{\n"
        + "   \"visitUuids\": [\n"
        + "     \"1f9ceb3d-f045-459e-8bb4-2b1b44833896\",\n"
        + "     \"96d0674a-11e4-4d7a-80f1-29fa49d4bb99\"\n"
        + "   ]\n"
        + "}";
    TypeReference<Map<String, Set<String>>> typeRef
        = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, List<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(fileUpload("/rest/v1/biometric/getVisitsByUuids")
        .header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(body.getBytes(StandardCharsets.UTF_8))
        .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(visitSchedulerService, times(1)).findVisitsByUuids(anySetOf(String.class));
  }

  @Test
  public void getVisitsByUuids_shouldReturnBadRequestStatusWhenEmptyUuids() throws Exception {
    String body = "{\"visitUuids\": []}";
    TypeReference<Map<String, Set<String>>> typeRef
        = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, Set<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(fileUpload("/rest/v1/biometric/getVisitsByUuids")
        .header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(body.getBytes(StandardCharsets.UTF_8))
        .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
    verify(visitSchedulerService, times(0)).findVisitsByUuids(anySetOf(String.class));
  }

  @Test
  public void createVisit_shouldThrowErrorWhenVisitUuidIsEmpty() throws Exception {
    //Given
    String visitRequest = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    visit = BiometricTestUtil.createVisit();
    newVisit = new Visit();
    NewVisitResponse newVisitResponse = new NewVisitResponse();
    newVisitResponse.setVisitUuid(visit.getUuid());
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    request.setVisitUuid(null);
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    when(visitRequestBuilder.createFrom(request)).thenReturn(visit);
    when(visitSchedulerService.createOrUpdateVisit(visit)).thenReturn(newVisit);
    when(visitResponseBuilder.createFrom(newVisit)).thenReturn(newVisitResponse);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    //Then
    verify(visitRequestBuilder, times(1)).createFrom(request);
    verify(visitSchedulerService, times(0)).createOrUpdateVisit(visit);
    verify(visitResponseBuilder, times(0)).createFrom(newVisit);
  }

  @Test
  public void createVisit_shouldThrowConflictWhenVisitUuidIsRepeated() throws Exception {
    //Given
    String visitRequest = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    visit = BiometricTestUtil.createVisit();
    newVisit = new Visit();
    NewVisitResponse newVisitResponse = new NewVisitResponse();
    newVisitResponse.setVisitUuid(visit.getUuid());
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    visit.getPatient().setUuid(request.getParticipantUuid());
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    when(visitRequestBuilder.createFrom(request)).thenReturn(visit);
    when(visitSchedulerService.createOrUpdateVisit(visit)).thenReturn(newVisit);
    when(visitResponseBuilder.createFrom(newVisit)).thenReturn(newVisitResponse);
    when(visitSchedulerService.findVisitsByUuids(anySetOf(String.class))).thenReturn(Arrays.asList(visit));
    when(util.isParticipantExists(anyString())).thenReturn(true);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
    //Then
    verify(visitRequestBuilder, times(1)).createFrom(request);
    verify(visitSchedulerService, times(0)).createOrUpdateVisit(visit);
    verify(visitResponseBuilder, times(0)).createFrom(newVisit);
  }

  @Test
  public void createVisit_shouldThrowConflictWhenVisitUuidNotParticipantIsRepeated()
      throws Exception {
    //Given
    String visitRequest = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    visit = BiometricTestUtil.createVisit();
    newVisit = new Visit();
    NewVisitResponse newVisitResponse = new NewVisitResponse();
    newVisitResponse.setVisitUuid(visit.getUuid());
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    when(visitRequestBuilder.createFrom(request)).thenReturn(visit);
    when(visitSchedulerService.createOrUpdateVisit(visit)).thenReturn(newVisit);
    when(visitResponseBuilder.createFrom(newVisit)).thenReturn(newVisitResponse);
    when(visitSchedulerService.findVisitsByUuids(anySetOf(String.class))).thenReturn(Arrays.asList(visit));
    when(util.isParticipantExists(anyString())).thenReturn(true);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    //Then
    verify(visitRequestBuilder, times(1)).createFrom(request);
    verify(visitSchedulerService, times(0)).createOrUpdateVisit(visit);
    verify(visitResponseBuilder, times(0)).createFrom(newVisit);
  }

  @Test
  public void createVisit_shouldThrowParticipantNotExists() throws Exception {
    //Given
    String visitRequest = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    visit = BiometricTestUtil.createVisit();
    newVisit = new Visit();
    NewVisitResponse newVisitResponse = new NewVisitResponse();
    newVisitResponse.setVisitUuid(visit.getUuid());
    VisitRequest request = new ObjectMapper().readValue(visitRequest, VisitRequest.class);
    when(util.jsonToObject(visitRequest, VisitRequest.class)).thenReturn(request);
    when(visitRequestBuilder.createFrom(request)).thenReturn(visit);
    when(visitSchedulerService.createOrUpdateVisit(visit)).thenReturn(newVisit);
    when(visitResponseBuilder.createFrom(newVisit)).thenReturn(newVisitResponse);
    when(visitSchedulerService.findVisitsByUuids(anySetOf(String.class))).thenReturn(Arrays.asList(visit));
    when(util.isParticipantExists(anyString())).thenReturn(false);
    //When
    mockMvc.perform(post(CREATE_VISIT_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .content(visitRequest)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
    //Then
    verify(visitRequestBuilder, times(1)).createFrom(request);
    verify(visitSchedulerService, times(0)).createOrUpdateVisit(visit);
    verify(visitResponseBuilder, times(0)).createFrom(newVisit);
  }

  @Test
  public void createEncounter_ShouldThrow404ErrorWhenRequestIsInvalid() throws Exception {
    //Given
    String encounterRequest = ControllerTestHelper.loadFile(CREATE_ENCOUNTER_INVALID_REQUEST);
    VisitRequest request = new ObjectMapper().readValue(encounterRequest, VisitRequest.class);
    request.setObservations(null);
    request.setVisitUuid(null);
    request.setLocationUuid(null);
    when(util.jsonToObject(encounterRequest, VisitRequest.class)).thenReturn(request);

    //When
    mockMvc.perform(
        post(CREATE_ENCOUNTER_ENDPOINT).content(encounterRequest)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    //Then
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitRequestBuilder);
    verifyNoMoreInteractions(encounterBuilder);
    verifyNoMoreInteractions(observationBuilder);
    verifyNoMoreInteractions(visitSchedulerService);
    verifyNoMoreInteractions(visitResponseBuilder);
  }

}
