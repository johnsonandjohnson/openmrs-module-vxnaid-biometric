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
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.PARTICIPANT_ID;
import static org.openmrs.module.biometric.web.helper.ControllerTestHelper.BASE_URL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.service.BiometricService;
import org.openmrs.module.biometric.api.service.ParticipantService;
import org.openmrs.module.biometric.builder.MatchResponseBuilder;
import org.openmrs.module.biometric.builder.ParticipantMatchResponseBuilder;
import org.openmrs.module.biometric.builder.PatientBuilder;
import org.openmrs.module.biometric.contract.ParticipantMatchResponse;
import org.openmrs.module.biometric.contract.RegisterRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Consists of unit tests for the PatientFormController
 *
 * @see ParticipantController
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class ParticipantControllerTest {

  private static final String BIOGRAPHIC_DATA_PARAM = "biographicData";

  private static final String REGISTER_ENDPOINT = BASE_URL + "/register";
  private static final String MATCH_ENDPOINT = BASE_URL + "/match";
  private static final String CREATE_PARTICIPANT_JSON = "create_participant.json";
  private static final String BIOGRAPHIC_DATA = "biographicData";
  private static final String PARTCIPANT_ID = "test1";

  private static final String PERSON_UUID = "person-image-uuid-value";
  private static final String PERSON_LOCATION = "9dd05e68-4d77-4b61-b2bf-436857f35aa0";
  private static final String DEVICE_HEADER_VALUE = "device1";
  private static final String DEVICE_HEADER_PARAM = "deviceId";
  private static final String LOCATION_UUID = "9dd05e68-4d77-4b61-b2bf-436857f35aa0";
  private static final String CREATE_REQUEST_BODY_JSON = "create_request_body.json";
  private static final String PARTICIPANT_UUIDS = "participantUuids";
  private static final String BIOMETRIC_UUIDS_ENDPOINT = BASE_URL + "/getBiometricTemplatesByUuids";
  private static final String PARTICIPANT_UUIDS_ENDPOINT = BASE_URL + "/getParticipantsByUuids";
  private static final String IMAGE_UUIDS_ENDPOINT = BASE_URL + "/getImagesByUuids";

  private MockMvc mockMvc;

  @Mock
  private PatientBuilder patientBuilder;

  @Mock
  private MatchResponseBuilder matchResponseBuilder;

  @Mock
  private ParticipantService participantService;

  @Mock
  private BiometricService biometricService;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private BiometricModUtil util;

  @Mock
  private LocationUtil locationUtil;

  @Mock
  private PatientService patientService;

  @Mock
  private ParticipantMatchResponseBuilder participantMatchResponseBuilder;

  @Mock
  private AdministrationService administrationService;

  @Mock
  private LocationService locationService;


  @InjectMocks
  private ParticipantController participantController;


  @Before
  public void setUp() {
    PowerMockito.mockStatic(Context.class);
    mockMvc = MockMvcBuilders.standaloneSetup(participantController).build();
  }

  @Test
  public void register_shouldRegisterParticipantWithBiometricData() throws Exception {
    //given
    String biographicData = ControllerTestHelper.loadFile(CREATE_PARTICIPANT_JSON);
    RegisterRequest request = new ObjectMapper().readValue(biographicData, RegisterRequest.class);
    Date date = new Date();
    when(util.jsonToObject(biographicData, RegisterRequest.class)).thenReturn(request);
    when(util.convertIsoStringToDate(request.getRegistrationDate())).thenReturn(date);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(biometricService
        .registerBiometricData(PARTICIPANT_ID, template.getBytes(), DEVICE_HEADER_VALUE,
            PERSON_LOCATION,
            new Date(), request.getParticipantUuid())).thenReturn(true);

    Person person = TestUtil.createPerson();
    Patient patient = TestUtil.createPatient(person);

    when(objectMapper.readValue(biographicData, RegisterRequest.class)).thenReturn(request);
    when(patientBuilder.createFrom(request)).thenReturn(patient);
    when(locationUtil.getLocationUuid(request.getAttributes())).thenReturn(LOCATION_UUID);
    when(util.removeWhiteSpaces(PARTICIPANT_ID)).thenReturn(PARTICIPANT_ID);
    when(participantService.registerParticipant(patient))
        .thenReturn(patient);

    //when
    mockMvc.perform(fileUpload(REGISTER_ENDPOINT).file(template)
        .header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .param(BIOGRAPHIC_DATA, biographicData)
        .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    //then
    //verifyRegisterInteractions();
    verify(biometricService, times(1))
        .registerBiometricData(PARTICIPANT_ID, template.getBytes(), DEVICE_HEADER_VALUE,
            PERSON_LOCATION, date,
            patient.getUuid());
  }

  @Test
  public void match_shouldReturnPatientListWhenMfaFalse() throws Exception {
    Map<String, String> addressMap = new HashMap<>();
    addressMap.put("country", "India");

    given(Context.getAdministrationService()).willReturn(administrationService);
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setAddresses(addressMap);

    PatientResponse patientResponse1 = new PatientResponse();
    patientResponse1.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse1.setDateModified(System.currentTimeMillis());
    patientResponse1.setParticipantId("participantIdNew");
    patientResponse1.setAddresses(addressMap);
    BiometricMatchingResult participant = new BiometricMatchingResult();
    participant.setId("123");
    participant.setMatchingScore(12);

    ParticipantMatchResponse participantMatchResponse = new ParticipantMatchResponse();
    participantMatchResponse.setParticipantId("123");
    participantMatchResponse.setMatchingScore(12);
    participantMatchResponse.setUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    participantMatchResponse.setAddresses(addressMap);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(participantService.findByPhone(anyString())).thenReturn(Arrays.asList(patientResponse));
    when(participantService.findByParticipantId(anyString()))
        .thenReturn(Arrays.asList(patientResponse1));
    when(util.mergePatients(anyListOf(PatientResponse.class), anyListOf(PatientResponse.class)))
        .thenReturn(Arrays.asList(patientResponse1));
    when(biometricService.matchBiometricData(any(byte[].class), anySetOf(String.class)))
        .thenReturn(Arrays.asList(participant));
    when(Context.getAdministrationService().getGlobalProperty(anyString())).thenReturn("false");
    mockMvc
        .perform(fileUpload(MATCH_ENDPOINT).file(template).param("participantId", "newParticipant")
            .param("phone", "1234567890").param("country", "India")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(participantService, times(1)).findByPhone(anyString());
    verify(participantService, times(1)).findByParticipantId(anyString());
    verify(biometricService, times(1))
        .matchBiometricData(any(byte[].class), anySetOf(String.class));
    verify(participantMatchResponseBuilder, times(1))
        .createFrom(anyListOf(BiometricMatchingResult.class), anyListOf(PatientResponse.class));

  }

  @Test
  public void match_shouldReturnPatientListWhenMfaTrue() throws Exception {
    Map<String, String> addressMap = new HashMap<>();
    addressMap.put("country", "India");
    Location location = new Location();
    location.setUuid(LOCATION_UUID);
    location.setCountry("India");
    given(Context.getAdministrationService()).willReturn(administrationService);
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setAddresses(addressMap);
    PatientResponse patientResponse1 = new PatientResponse();
    patientResponse1.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse1.setDateModified(System.currentTimeMillis());
    patientResponse1.setParticipantId("participantIdNew");
    patientResponse1.setAddresses(addressMap);
    BiometricMatchingResult participant = new BiometricMatchingResult();
    participant.setId("123");
    participant.setMatchingScore(12);

    ParticipantMatchResponse participantMatchResponse = new ParticipantMatchResponse();
    participantMatchResponse.setParticipantId("123");
    participantMatchResponse.setMatchingScore(12);
    participantMatchResponse.setUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(participantService.findByPhone(anyString())).thenReturn(Arrays.asList(patientResponse));
    when(participantService.findByParticipantId(anyString())).thenReturn(Collections.emptyList());
    when(util.mergePatients(anyListOf(PatientResponse.class), anyListOf(PatientResponse.class)))
        .thenReturn(Arrays.asList(patientResponse1));
    when(biometricService.matchBiometricData(any(byte[].class), anySetOf(String.class)))
        .thenReturn(Collections.emptyList());
    when(locationUtil.getLocationUuid(patientResponse1.getAttributes())).thenReturn(LOCATION_UUID);
    when(locationUtil.getLocationByUuid(LOCATION_UUID)).thenReturn(location);
    when(Context.getAdministrationService().getGlobalProperty(anyString())).thenReturn("true");
    mockMvc
        .perform(fileUpload(MATCH_ENDPOINT).file(template).param("participantId", "newParticipant")
            .param("phone", "1234567890").param("country", "india")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(participantService, times(1)).findByPhone(anyString());
    verify(participantService, times(1)).findByParticipantId(anyString());
    verify(biometricService, times(1))
        .matchBiometricData(any(byte[].class), anySetOf(String.class));
  }

  @Test
  public void findPatiensByUuids_ShouldReturnOkStatus() throws Exception {
    String body = ControllerTestHelper.loadFile(CREATE_REQUEST_BODY_JSON);
    TypeReference<Map<String, Set<String>>> typeRef = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, Set<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(
        fileUpload(PARTICIPANT_UUIDS_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
            .content(body.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(participantService, times(1)).findPatientsByUuids(map.get(PARTICIPANT_UUIDS));
  }

  @Test
  public void findPatiensByUuids_shouldReturnBadRequestWithEmptyUuids() throws Exception {
    String body = "{\"participantUuids\": []}";
    TypeReference<Map<String, Set<String>>> typeRef = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, Set<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(
        fileUpload(PARTICIPANT_UUIDS_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
            .content(body.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
    verify(participantService, times(0)).findPatientsByUuids(map.get(PARTICIPANT_UUIDS));
  }

  @Test
  public void findImagesByUuids_shouldReturnOkStatus() throws Exception {
    String body = ControllerTestHelper.loadFile(CREATE_REQUEST_BODY_JSON);
    TypeReference<Map<String, Set<String>>> typeRef = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, Set<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc
        .perform(fileUpload(IMAGE_UUIDS_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
            .content(body.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(participantService, times(1)).findImagesByUuids(map.get(PARTICIPANT_UUIDS));
  }

  @Test
  public void findImagesByUuids_shouldReturnBadRequestWithEmptyUuids() throws Exception {
    String body = "{\"participantUuids\": []}";
    TypeReference<Map<String, Set<String>>> typeRef = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, Set<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc
        .perform(fileUpload(IMAGE_UUIDS_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
            .content(body.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
    verify(participantService, times(0)).findImagesByUuids(map.get(PARTICIPANT_UUIDS));
  }

  @Test
  public void getBiometricDataByParticipantIds_shouldReturnOkStatus() throws Exception {
    String body = ControllerTestHelper.loadFile(CREATE_REQUEST_BODY_JSON);
    TypeReference<Map<String, Set<String>>> typeRef = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, Set<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(
        fileUpload(BIOMETRIC_UUIDS_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
            .content(body.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    verify(participantService, times(1))
        .getBiometricDataByParticipantIds(map.get(PARTICIPANT_UUIDS));
  }

  @Test
  public void getBiometricDataByParticipantIds_shouldReturnBadRequestWithEmptyUuids()
      throws Exception {
    String body = "{\"participantUuids\": []}";
    TypeReference<Map<String, Set<String>>> typeRef = new TypeReference<Map<String, Set<String>>>() {
    };
    Map<String, Set<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(
        fileUpload(BIOMETRIC_UUIDS_ENDPOINT).header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
            .content(body.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    verify(participantService, times(0))
        .getBiometricDataByParticipantIds(map.get(PARTICIPANT_UUIDS));

  }


  @Test
  public void match_shouldReturnPatientList() throws Exception {
    Map<String, String> addressMap = new HashMap<>();
    addressMap.put("country", "India");

    given(Context.getAdministrationService()).willReturn(administrationService);
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setAddresses(addressMap);

    PatientResponse patientResponse1 = new PatientResponse();
    patientResponse1.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse1.setDateModified(System.currentTimeMillis());
    patientResponse1.setParticipantId("participantIdNew");
    patientResponse1.setAddresses(addressMap);
    BiometricMatchingResult participant = new BiometricMatchingResult();
    participant.setId("123");
    participant.setMatchingScore(12);

    ParticipantMatchResponse participantMatchResponse = new ParticipantMatchResponse();
    participantMatchResponse.setParticipantId("123");
    participantMatchResponse.setMatchingScore(12);
    participantMatchResponse.setUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    participantMatchResponse.setAddresses(addressMap);
    Location location = new Location();
    location.setUuid(LOCATION_UUID);
    location.setCountry("India");
    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(participantService.findByPhone(anyString())).thenReturn(Arrays.asList(patientResponse));
    when(participantService.findByParticipantId(anyString()))
        .thenReturn(Arrays.asList(patientResponse1));
    when(util.mergePatients(anyListOf(PatientResponse.class), anyListOf(PatientResponse.class)))
        .thenReturn(Arrays.asList(patientResponse1));
    when(locationUtil.getLocationUuid(patientResponse1.getAttributes())).thenReturn(LOCATION_UUID);
    when(locationUtil.getLocationByUuid(LOCATION_UUID)).thenReturn(location);
    when(biometricService.matchBiometricData(any(byte[].class), anySetOf(String.class)))
        .thenReturn(Collections.emptyList());
    when(Context.getAdministrationService().getGlobalProperty(anyString())).thenReturn("true");
    mockMvc
        .perform(fileUpload(MATCH_ENDPOINT).file(template).param("participantId", "newParticipant")
            .param("phone", "1234567890").param("country", "India")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(participantService, times(1)).findByPhone(anyString());
    verify(participantService, times(1)).findByParticipantId(anyString());
    verify(biometricService, times(1))
        .matchBiometricData(any(byte[].class), anySetOf(String.class));
    verify(locationUtil, times(1)).getLocationUuid(patientResponse1.getAttributes());
    verify(locationUtil, times(1)).getLocationByUuid(LOCATION_UUID);

  }

  @Test
  public void match_shouldThrowErrorWhenCountryNull() throws Exception {
    Map<String, String> addressMap = new HashMap<>();
    addressMap.put("country", "India");

    given(Context.getAdministrationService()).willReturn(administrationService);
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setAddresses(addressMap);

    PatientResponse patientResponse1 = new PatientResponse();
    patientResponse1.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse1.setDateModified(System.currentTimeMillis());
    patientResponse1.setParticipantId("participantIdNew");
    patientResponse1.setAddresses(addressMap);
    BiometricMatchingResult participant = new BiometricMatchingResult();
    participant.setId("123");
    participant.setMatchingScore(12);

    ParticipantMatchResponse participantMatchResponse = new ParticipantMatchResponse();
    participantMatchResponse.setParticipantId("123");
    participantMatchResponse.setMatchingScore(12);
    participantMatchResponse.setUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    participantMatchResponse.setAddresses(addressMap);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(participantService.findByPhone(anyString())).thenReturn(Arrays.asList(patientResponse));
    when(participantService.findByParticipantId(anyString()))
        .thenReturn(Arrays.asList(patientResponse1));
    when(util.mergePatients(anyListOf(PatientResponse.class), anyListOf(PatientResponse.class)))
        .thenReturn(Arrays.asList(patientResponse1));
    when(biometricService.matchBiometricData(any(byte[].class), anySetOf(String.class)))
        .thenReturn(Collections.emptyList());
    when(Context.getAdministrationService().getGlobalProperty(anyString())).thenReturn("true");
    String nullValue = null;
    mockMvc
        .perform(fileUpload(MATCH_ENDPOINT).file(template).param("participantId", "newParticipant")
            .param("phone", "1234567890").param("country", nullValue)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
    verify(participantService, times(1)).findByPhone(anyString());
    verify(participantService, times(1)).findByParticipantId(anyString());

  }

  @Test
  public void register_shouldThrow500WhenUuidAlreadyExists() throws Exception {
    //given
    String biographicData = ControllerTestHelper.loadFile(CREATE_PARTICIPANT_JSON);
    RegisterRequest request = new ObjectMapper().readValue(biographicData, RegisterRequest.class);
    Date date = new Date();
    when(util.jsonToObject(biographicData, RegisterRequest.class)).thenReturn(request);
    when(util.convertIsoStringToDate(request.getRegistrationDate())).thenReturn(date);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(biometricService
        .registerBiometricData(PARTICIPANT_ID, template.getBytes(), DEVICE_HEADER_VALUE,
            PERSON_LOCATION,
            new Date(), request.getParticipantUuid())).thenReturn(true);

    Person person = TestUtil.createPerson();
    Patient patient = TestUtil.createPatient(person);
    PatientResponse patientResponse = TestUtil.createPatientResponse();
    when(objectMapper.readValue(biographicData, RegisterRequest.class)).thenReturn(request);
    when(patientBuilder.createFrom(request)).thenReturn(patient);
    when(locationUtil.getLocationUuid(request.getAttributes())).thenReturn(LOCATION_UUID);
    when(util.removeWhiteSpaces(PARTICIPANT_ID)).thenReturn(PARTICIPANT_ID);
    when(participantService.registerParticipant(patient))
        .thenReturn(patient);
    when(participantService.findPatientsByUuids(anySetOf(String.class)))
        .thenReturn(Arrays.asList(patientResponse));

    //when
    mockMvc.perform(fileUpload(REGISTER_ENDPOINT).file(template)
        .header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .param(BIOGRAPHIC_DATA, biographicData)
        .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());

  }

  @Test
  public void register_shouldThrow409WhenUuidAlreadyExists() throws Exception {
    //given
    String biographicData = ControllerTestHelper.loadFile(CREATE_PARTICIPANT_JSON);
    RegisterRequest request = new ObjectMapper().readValue(biographicData, RegisterRequest.class);
    Date date = new Date();
    when(util.jsonToObject(biographicData, RegisterRequest.class)).thenReturn(request);
    when(util.convertIsoStringToDate(request.getRegistrationDate())).thenReturn(date);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(biometricService
        .registerBiometricData(PARTICIPANT_ID, template.getBytes(), DEVICE_HEADER_VALUE,
            PERSON_LOCATION,
            new Date(), request.getParticipantUuid())).thenReturn(true);

    Person person = TestUtil.createPerson();
    Patient patient = TestUtil.createPatient(person);
    PatientResponse patientResponse = TestUtil.createPatientResponse();
    patientResponse.setParticipantId("btest1");
    when(objectMapper.readValue(biographicData, RegisterRequest.class)).thenReturn(request);
    when(patientBuilder.createFrom(request)).thenReturn(patient);
    when(locationUtil.getLocationUuid(request.getAttributes())).thenReturn(LOCATION_UUID);
    when(util.removeWhiteSpaces(PARTICIPANT_ID)).thenReturn(PARTICIPANT_ID);
    when(participantService.registerParticipant(patient))
        .thenReturn(patient);
    when(participantService.findPatientsByUuids(anySetOf(String.class)))
        .thenReturn(Arrays.asList(patientResponse));

    //when
    mockMvc.perform(fileUpload(REGISTER_ENDPOINT).file(template)
        .header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .param(BIOGRAPHIC_DATA, biographicData)
        .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isConflict());
  }

  @Test
  public void register_shouldThrow500WhenParticipantIdAlreadyExists() throws Exception {
    //given
    String biographicData = ControllerTestHelper.loadFile(CREATE_PARTICIPANT_JSON);
    RegisterRequest request = new ObjectMapper().readValue(biographicData, RegisterRequest.class);
    Date date = new Date();
    when(util.jsonToObject(biographicData, RegisterRequest.class)).thenReturn(request);
    when(util.convertIsoStringToDate(request.getRegistrationDate())).thenReturn(date);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(biometricService
        .registerBiometricData(PARTICIPANT_ID, template.getBytes(), DEVICE_HEADER_VALUE,
            PERSON_LOCATION,
            new Date(), request.getParticipantUuid())).thenReturn(true);

    Person person = TestUtil.createPerson();
    Patient patient = TestUtil.createPatient(person);
    PatientResponse patientResponse = TestUtil.createPatientResponse();
    patientResponse.setParticipantId("btest1");
    when(objectMapper.readValue(biographicData, RegisterRequest.class)).thenReturn(request);
    when(patientBuilder.createFrom(request)).thenReturn(patient);
    when(locationUtil.getLocationUuid(request.getAttributes())).thenReturn(LOCATION_UUID);
    when(util.removeWhiteSpaces(PARTICIPANT_ID)).thenReturn(PARTICIPANT_ID);
    when(participantService.registerParticipant(patient))
        .thenReturn(patient);
    when(participantService.findPatientsByUuids(anySetOf(String.class)))
        .thenReturn(Collections.emptyList());
    when(participantService.findByParticipantId(patient.getPatientIdentifier().getIdentifier()))
        .thenReturn(Arrays.asList(patientResponse));
    //when
    mockMvc.perform(fileUpload(REGISTER_ENDPOINT).file(template)
        .header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .param(BIOGRAPHIC_DATA, biographicData)
        .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void register_shouldThrowException() throws Exception {
    //given
    String biographicData = ControllerTestHelper.loadFile(CREATE_PARTICIPANT_JSON);
    RegisterRequest request = new ObjectMapper().readValue(biographicData, RegisterRequest.class);
    Date date = new Date();
    when(util.jsonToObject(biographicData, RegisterRequest.class)).thenReturn(request);
    when(util.convertIsoStringToDate(request.getRegistrationDate())).thenReturn(date);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(biometricService
        .registerBiometricData(PARTICIPANT_ID, template.getBytes(), DEVICE_HEADER_VALUE,
            PERSON_LOCATION,
            new Date(), request.getParticipantUuid())).thenThrow(Exception.class);

    Person person = TestUtil.createPerson();
    Patient patient = TestUtil.createPatient(person);
    PatientResponse patientResponse = TestUtil.createPatientResponse();
    patientResponse.setParticipantId("btest1");
    when(objectMapper.readValue(biographicData, RegisterRequest.class)).thenReturn(request);
    when(patientBuilder.createFrom(request)).thenReturn(patient);
    when(locationUtil.getLocationUuid(request.getAttributes())).thenReturn(LOCATION_UUID);
    when(util.removeWhiteSpaces(PARTICIPANT_ID)).thenReturn(PARTICIPANT_ID);
    when(participantService.registerParticipant(patient))
        .thenReturn(patient);
    when(participantService.findPatientsByUuids(anySetOf(String.class)))
        .thenReturn(Collections.emptyList());
    when(participantService.findByParticipantId(patient.getPatientIdentifier().getIdentifier()))
        .thenReturn(Collections.emptyList());
    //when
    mockMvc.perform(fileUpload(REGISTER_ENDPOINT).file(template)
        .header(DEVICE_HEADER_PARAM, DEVICE_HEADER_VALUE)
        .param(BIOGRAPHIC_DATA, biographicData)
        .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
  }

  @Test
  public void match_shouldThrowException() throws Exception {
    Map<String, String> addressMap = new HashMap<>();
    addressMap.put("country", "India");

    given(Context.getAdministrationService()).willReturn(administrationService);
    PatientResponse patientResponse = new PatientResponse();
    patientResponse.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse.setDateModified(System.currentTimeMillis());
    patientResponse.setAddresses(addressMap);

    PatientResponse patientResponse1 = new PatientResponse();
    patientResponse1.setParticipantUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    patientResponse1.setDateModified(System.currentTimeMillis());
    patientResponse1.setParticipantId("participantIdNew");
    patientResponse1.setAddresses(addressMap);
    BiometricMatchingResult participant = new BiometricMatchingResult();
    participant.setId("123");
    participant.setMatchingScore(12);

    ParticipantMatchResponse participantMatchResponse = new ParticipantMatchResponse();
    participantMatchResponse.setParticipantId("123");
    participantMatchResponse.setMatchingScore(12);
    participantMatchResponse.setUuid("867d1cff-d8c5-4645-91d5-0a773a33c83b");
    participantMatchResponse.setAddresses(addressMap);

    MockMultipartFile template = ControllerTestHelper.getTestTemplate();
    when(participantService.findByPhone(anyString())).thenReturn(Arrays.asList(patientResponse));
    when(participantService.findByParticipantId(anyString()))
        .thenReturn(Arrays.asList(patientResponse1));
    when(util.mergePatients(anyListOf(PatientResponse.class), anyListOf(PatientResponse.class)))
        .thenReturn(Arrays.asList(patientResponse1));
    when(biometricService.matchBiometricData(any(byte[].class), anySetOf(String.class)))
        .thenThrow(Exception.class);
    when(Context.getAdministrationService().getGlobalProperty(anyString())).thenReturn("false");
    mockMvc
        .perform(fileUpload(MATCH_ENDPOINT).file(template).param("participantId", "newParticipant")
            .param("phone", "1234567890").param("country", "India")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(participantService, times(1)).findByPhone(anyString());
    verify(participantService, times(1)).findByParticipantId(anyString());
    verify(biometricService, times(1))
        .matchBiometricData(any(byte[].class), anySetOf(String.class));
    verify(participantMatchResponseBuilder, times(1))
        .createFrom(anyListOf(BiometricMatchingResult.class), anyListOf(PatientResponse.class));

  }

  @Test
  public void retrievePersonImage_shouldReturnPersonImage() throws Exception {
    when(participantService.retrieveParticipantImage(anyString())).thenReturn("");
    mockMvc.perform(get(ControllerTestHelper.BASE_URL + "/personimage/{personUuid}", PERSON_UUID)
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void voidParticipant_shouldReturnOk() throws Exception {
    Patient patient = TestUtil.createPatient(TestUtil.createPerson());
    when(Context.getPatientService()).thenReturn(patientService);
    when(Context.getPatientService().getPatientByUuid(anyString())).thenReturn(patient);
    when(util.isBiometricFeatureEnabled()).thenReturn(false);
    mockMvc.perform(put(ControllerTestHelper.BASE_URL + "/participant/{personUuid}", PERSON_UUID)
        .param("reason", "Deleting participant")
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isOk());

  }

  @Test
  public void voidParticipant_shouldThrowNotFoundExceptionWhenPatientNull() throws Exception {
    Patient patient = TestUtil.createPatient(TestUtil.createPerson());
    when(Context.getPatientService()).thenReturn(patientService);
    when(Context.getPatientService().getPatientByUuid(anyString())).thenReturn(null);
    when(util.isBiometricFeatureEnabled()).thenReturn(false);
    mockMvc.perform(put(ControllerTestHelper.BASE_URL + "/participant/{personUuid}", PERSON_UUID)
        .param("reason", "Deleting participant")
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isNotFound());

  }

  @Test
  public void voidParticipant_shouldReturnOkWhenValidPatient() throws Exception {
    Patient patient = TestUtil.createPatient(TestUtil.createPerson());
    when(Context.getPatientService()).thenReturn(patientService);
    when(Context.getPatientService().getPatientByUuid(anyString())).thenReturn(patient);
    when(util.isBiometricFeatureEnabled()).thenReturn(true);
    mockMvc.perform(put(ControllerTestHelper.BASE_URL + "/participant/{personUuid}", PERSON_UUID)
        .param("reason", "Deleting participant")
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isOk());

  }

}
