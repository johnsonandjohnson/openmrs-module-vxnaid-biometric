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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.TABLE_COUNT;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.VOIDED_COUNT;
import static org.openmrs.module.biometric.util.LocationUtilTest.COUNTRY1_NAME;
import static org.openmrs.module.biometric.util.LocationUtilTest.createLocations;
import static org.openmrs.module.biometric.util.LocationUtilTest.getLocationUuidsByCountry;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.api.service.SyncService;
import org.openmrs.module.biometric.builder.ParticipantRecordsResponseBuilder;
import org.openmrs.module.biometric.builder.SyncResponseBuilder;
import org.openmrs.module.biometric.builder.VisitResponseBuilder;
import org.openmrs.module.biometric.common.BiometricTestUtil;
import org.openmrs.module.biometric.contract.sync.SyncErrorRequest;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Consists of unit tests for the PatientFormController
 *
 * @see ParticipantController
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Date.class, Context.class, SanitizeUtil.class})
public class SyncControllerTest {

  private static final String SYNCREQUEST_JSON = "syncrequest.json";
  private static final String SYNCERROREQUESTRESOLVED_JSON = "sync_error_resolved_request.json";
  private static final String SYNCERRORREQUESTRESOLVEDEMPTY_JSON = "sync_error_resolved_empty_request.json";
  private static final String SYNCERROREQUEST_JSON = "sync_error_request.json";
  private static final String SYNCERRORREQUESTEMPTY_JSON = "sync_error_request_empty.json";
  private static final String SYNCERRORREQUESTNULL_JSON = "sync_error_request_null.json";
  private static final String DEVICE_ID = "deviceId";
  private static final String LOCATION_UUID = "9dd05e68-4d77-4b61-b2bf-436857f35aa0";

  private MockMvc mockMvc;

  List<String> locationList = new ArrayList<>();

  @Mock
  private SyncService syncService;

  @Mock
  private BiometricModUtil util;

  @Mock
  private LocationUtil locationUtil;

  @Mock
  private ConfigService configService;

  @Mock
  private VisitResponseBuilder visitResponseBuilder;

  @Mock
  private SyncResponseBuilder syncResponseBuilder;

  @Mock
  private ParticipantRecordsResponseBuilder builder;

  @Mock
  private LocationService locationService;

  @InjectMocks
  private SyncController syncController;

  @Before
  public void setUp() {
    locationList.add(LOCATION_UUID);
    PowerMockito.mockStatic(Date.class);
    PowerMockito.mockStatic(Context.class);
    when(Context.getLocationService()).thenReturn(locationService);
    mockMvc = MockMvcBuilders.standaloneSetup(syncController).build();
  }

  @Test
  public void getAllParticipants_shouldReturnAllParticipants() throws Exception {
    //given
    String syncRequest = ControllerTestHelper.loadFile(SYNCREQUEST_JSON);
    SyncRequest request = new ObjectMapper().readValue(syncRequest, SyncRequest.class);
    assertNotNull(request);

    when(util.jsonToObject(syncRequest, SyncRequest.class)).thenReturn(request);

    List<Location> locations = createLocations();
    doNothing().when(locationUtil).validateSyncLocationData(request);
    when(locationService.getRootLocations(false)).thenReturn(locations);
    when(locationUtil.findLocationsByCountry(COUNTRY1_NAME))
        .thenReturn(getLocationUuidsByCountry());

    List<Patient> patientList = new ArrayList<>();
    Patient patient = BiometricTestUtil.createPatient();
    patient.setUuid(String.valueOf(request.getUuidsWithDateModifiedOffset().stream().findFirst()));
    request.setDateModifiedOffset(new Date().getTime());
    patient.setDateChanged(new Date());
    patient.setVoided(true);
    patientList.add(patient);
    patient = BiometricTestUtil.createPatient();
    patientList.add(patient);
    Map<String, Long> map = new HashMap<>();
    map.put(TABLE_COUNT, 10L);
    map.put(VOIDED_COUNT, 1L);
    when(syncService.getAllPatients(any(Date.class), anyInt(), anyListOf(String.class)))
        .thenReturn(patientList);
    when(syncService.getPatientCount(anyListOf(String.class))).thenReturn(map);
    final Date date = Mockito.mock(Date.class);
    PowerMockito.whenNew(Date.class).withAnyArguments().thenReturn(date);
    //when
    mockMvc.perform(
        post(ControllerTestHelper.BASE_URL + "/sync/getAllParticipants").content(syncRequest)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  public void getAllParticipantImages_shouldReturnAllParticipantImages() throws Exception {
    //given
    String syncRequest = ControllerTestHelper.loadFile(SYNCREQUEST_JSON);
    SyncRequest request = new ObjectMapper().readValue(syncRequest, SyncRequest.class);
    assertNotNull(request);
    when(util.jsonToObject(syncRequest, SyncRequest.class)).thenReturn(request);
    when(util.isLocationExists(anyString())).thenReturn(true);
    List<Location> locations = createLocations();
    doNothing().when(locationUtil).validateSyncLocationData(request);
    when(locationService.getRootLocations(false)).thenReturn(locations);
    when(locationUtil.findLocationsByCountry(COUNTRY1_NAME))
        .thenReturn(getLocationUuidsByCountry());
    final Date date = Mockito.mock(Date.class);
    PowerMockito.whenNew(Date.class).withAnyArguments().thenReturn(date);
    //when
    mockMvc.perform(
        post(ControllerTestHelper.BASE_URL + "/sync/getAllParticipantImages")
            .header("deviceId", "device1")
            .content(syncRequest).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void getAllVisits_shouldReturnAllParticipantVisits() throws Exception {
    //given
    String syncRequest = ControllerTestHelper.loadFile(SYNCREQUEST_JSON);
    SyncRequest request = new ObjectMapper().readValue(syncRequest, SyncRequest.class);
    assertNotNull(request);
    when(util.jsonToObject(syncRequest, SyncRequest.class)).thenReturn(request);
    List<Location> locations = createLocations();
    doNothing().when(locationUtil).validateSyncLocationData(request);
    when(locationService.getRootLocations(false)).thenReturn(locations);
    when(locationUtil.findLocationsByCountry(COUNTRY1_NAME))
        .thenReturn(getLocationUuidsByCountry());
    final Date date = Mockito.mock(Date.class);
    PowerMockito.whenNew(Date.class).withAnyArguments().thenReturn(date);
    List<Visit> visitList = new ArrayList<>();
    Visit visit = TestUtil.createVisit();
    visit.setVoided(true);
    visitList.add(visit);
    when(syncService.getAllVisits(any(Date.class), anyInt(), anyListOf(String.class)))
        .thenReturn(visitList);
    //when
    mockMvc.perform(post(ControllerTestHelper.BASE_URL + "/sync/getAllVisits").content(syncRequest)
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  public void syncErrorResolved_shouldVoidTheErrorKeysForDevice() throws Exception {
    String body = ControllerTestHelper.loadFile(SYNCERROREQUESTRESOLVED_JSON);
    TypeReference<Map<String, List<String>>> typeRef = new TypeReference<Map<String, List<String>>>() {
    };
    Map<String, List<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(post(ControllerTestHelper.BASE_URL + "/sync/error/resolved")
        .header(DEVICE_ID, "newDeviceId")
        .content(body.getBytes(StandardCharsets.UTF_8)).contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(syncService, times(1)).resolveSyncErrors(anyString(), anyListOf(String.class));
  }

  @Test
  public void syncErrorResolved_shouldThrowBadRequestWhenErrorKeysIsEmpty() throws Exception {
    String body = ControllerTestHelper.loadFile(SYNCERRORREQUESTRESOLVEDEMPTY_JSON);
    TypeReference<Map<String, List<String>>> typeRef = new TypeReference<Map<String, List<String>>>() {
    };
    Map<String, List<String>> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, List<String>>>>any()))
        .thenReturn(map);
    mockMvc.perform(post(ControllerTestHelper.BASE_URL + "/sync/error/resolved")
        .header(DEVICE_ID, "newDeviceId")
        .content(body.getBytes(StandardCharsets.UTF_8)).contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
    verify(syncService, times(0)).resolveSyncErrors(anyString(), anyListOf(String.class));

  }

  @Test
  public void saveSyncError_shouldSaveAllSyncErrorsForTheDevice() throws Exception {
    String syncErrorRequest = ControllerTestHelper.loadFile(SYNCERROREQUEST_JSON);
    SyncErrorRequest request = new ObjectMapper()
        .readValue(syncErrorRequest, SyncErrorRequest.class);
    assertNotNull(request);
    when(util.jsonToObject(syncErrorRequest, SyncErrorRequest.class)).thenReturn(request);
    mockMvc.perform(
        post(ControllerTestHelper.BASE_URL + "/sync/error").header(DEVICE_ID, "newDeviceId")
            .content(syncErrorRequest.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
    verify(syncService, times(3))
        .saveSyncError(anyString(), anyString(), any(Date.class), anyString(), anyString());
  }

  @Test
  public void saveSyncError_shouldThrowBadRequestWhenSyncErrorIsEmpty() throws Exception {
    String syncErrorRequest = ControllerTestHelper.loadFile(SYNCERRORREQUESTEMPTY_JSON);
    SyncErrorRequest request = new ObjectMapper()
        .readValue(syncErrorRequest, SyncErrorRequest.class);
    assertNotNull(request);
    when(util.jsonToObject(syncErrorRequest, SyncErrorRequest.class)).thenReturn(request);
    mockMvc.perform(
        post(ControllerTestHelper.BASE_URL + "/sync/error").header(DEVICE_ID, "newDeviceId")
            .content(syncErrorRequest.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
    verify(syncService, times(0))
        .saveSyncError(anyString(), anyString(), any(Date.class), anyString(), anyString());
  }

  @Test
  public void saveSyncError_shouldThrowBadRequestWhenSyncErrorIsNull() throws Exception {
    String syncErrorRequest = ControllerTestHelper.loadFile(SYNCERRORREQUESTNULL_JSON);
    SyncErrorRequest request = new ObjectMapper()
        .readValue(syncErrorRequest, SyncErrorRequest.class);
    assertNotNull(request);
    when(util.jsonToObject(syncErrorRequest, SyncErrorRequest.class)).thenReturn(request);
    mockMvc.perform(
        post(ControllerTestHelper.BASE_URL + "/sync/error").header(DEVICE_ID, "newDeviceId")
            .content(syncErrorRequest.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
    verify(syncService, times(0))
        .saveSyncError(anyString(), anyString(), any(Date.class), anyString(), anyString());
  }

  @Test
  public void getAllParticipants_shouldThrowBadRequest() throws Exception {
    //given
    String syncRequest = "{\"dateModifiedOffset\" : 1616650594000,\"optimize\": false,\"syncScope\": {},\"offset\": 5,\"limit\": 10}";
    SyncRequest request = new ObjectMapper().readValue(syncRequest, SyncRequest.class);
    assertNotNull(request);
    when(util.jsonToObject(syncRequest, SyncRequest.class)).thenReturn(request);
    doThrow(EntityValidationException.class).when(locationUtil).validateSyncLocationData(request);

    //when
    mockMvc.perform(
        post(ControllerTestHelper.BASE_URL + "/sync/getAllParticipants").content(syncRequest)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
  }


  @Test
  public void getAllParticipantBiometricsTemplates_shouldReturnAllBiometricTemplates()
      throws Exception {
    String syncRequest = ControllerTestHelper.loadFile(SYNCREQUEST_JSON);
    SyncRequest request = new ObjectMapper().readValue(syncRequest, SyncRequest.class);
    request.setDateModifiedOffset(null);
    assertNotNull(request);
    when(util.jsonToObject(syncRequest, SyncRequest.class)).thenReturn(request);
    when(util.isLocationExists(anyString())).thenReturn(true);
    List<Location> locations = createLocations();
    doNothing().when(locationUtil).validateSyncLocationData(request);
    when(locationService.getRootLocations(false)).thenReturn(locations);
    when(locationUtil.findLocationsByCountry(COUNTRY1_NAME))
        .thenReturn(getLocationUuidsByCountry());
    Map<String, Long> row = new HashMap<>();
    Date lastModifiedDate = new Date();
    //row.put("dbid", "id1");
    //row.put("template", "WQJDJKDKDOIDJDIJDI");
    row.put("modificationDate", lastModifiedDate.getTime());

    row.put("tableCount", 15L);
    row.put("ignoredCount", 9L);
    row.put("voidedCount", 0L);
    List<Map<String, Long>> rows = new ArrayList<>();
    rows.add(row);

    when(
        syncService.getBiometricTemplatesCount(anyString(), anyList(), anyBoolean()))
        .thenReturn(row);

    SyncResponse syncResponse = new SyncResponse();
    syncResponse.setSyncScope(request.getSyncScope());
    syncResponse.setDateModifiedOffset(request.getDateModifiedOffset());
    syncResponse.setOptimize(request.getOptimize());

    when(syncResponseBuilder
        .createFrom(anyListOf(SyncTemplateResponse.class), anyLong(), anyLong(), anyLong(),
            any(SyncRequest.class))).thenReturn(syncResponse);

    mockMvc
        .perform(post(ControllerTestHelper.BASE_URL + "/sync/getAllParticipantBiometricsTemplates")
            .header("deviceId", "newDeviceId").content(syncRequest.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    verify(syncResponseBuilder, times(1))
        .createFrom(anyListOf(SyncTemplateResponse.class), anyLong(), anyLong(), anyLong(),
            any(SyncRequest.class));
    verify(syncService, times(1))
        .getBiometricTemplatesCount(anyString(), anyList(), anyBoolean());
  }


  @Test
  public void getAllParticipantBiometricsTemplates_shouldReturnAllBiometricTemplatesWithNonNullDateAndOptimise()
      throws
      Exception {
    String syncRequest = ControllerTestHelper.loadFile(SYNCREQUEST_JSON);
    SyncRequest request = new ObjectMapper().readValue(syncRequest, SyncRequest.class);
    assertNotNull(request);
    request.setOptimize(true);
    request.setDateModifiedOffset(System.currentTimeMillis());
    when(util.jsonToObject(syncRequest, SyncRequest.class)).thenReturn(request);
    when(util.isLocationExists(anyString())).thenReturn(true);
    List<Location> locations = createLocations();
    doNothing().when(locationUtil).validateSyncLocationData(request);
    when(locationService.getRootLocations(false)).thenReturn(locations);
    when(locationUtil.findLocationsByCountry(COUNTRY1_NAME))
        .thenReturn(getLocationUuidsByCountry());
    Map<String, Long> row = new HashMap<>();
    Date lastModifiedDate = new Date();
    //row.put("dbid", "id1");
    //row.put("template", "WQJDJKDKDOIDJDIJDI");
    row.put("modificationDate", lastModifiedDate.getTime());

    row.put("tableCount", 15L);
    row.put("ignoredCount", 9L);
    row.put("voidedCount", 0L);
    List<Map<String, Long>> rows = new ArrayList<>();
    rows.add(row);

    when(
        syncService.getBiometricTemplatesCount(anyString(), anyList(), anyBoolean()))
        .thenReturn(row);

    SyncResponse syncResponse = new SyncResponse();
    syncResponse.setSyncScope(request.getSyncScope());
    syncResponse.setDateModifiedOffset(request.getDateModifiedOffset());
    syncResponse.setOptimize(request.getOptimize());

    when(syncResponseBuilder
        .createFrom(anyListOf(SyncTemplateResponse.class), anyLong(), anyLong(), anyLong(),
            any(SyncRequest.class))).thenReturn(syncResponse);

    mockMvc
        .perform(post(ControllerTestHelper.BASE_URL + "/sync/getAllParticipantBiometricsTemplates")
            .header("deviceId", "newDeviceId").content(syncRequest.getBytes(StandardCharsets.UTF_8))
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    verify(syncResponseBuilder, times(1))
        .createFrom(anyListOf(SyncTemplateResponse.class), anyLong(), anyLong(), anyLong(),
            any(SyncRequest.class));
    verify(syncService, times(1))
        .getBiometricTemplatesCount(anyString(), anyList(), anyBoolean());
  }

  @Test
  public void getAllVisits_shouldReturnVisitsWithRootLocations() throws Exception {
    //given
    String syncRequest = "{\"dateModifiedOffset\" : 0,\"optimize\": false,\"syncScope\": {  \"siteUuid\":\"\",  \"country\": \"BELGIUM\"},\"offset\": 5,\"limit\": 10}";
    SyncRequest request = new ObjectMapper().readValue(syncRequest, SyncRequest.class);
    request.setUuidsWithDateModifiedOffset(Collections.emptySet());
    assertNotNull(request);
    Location location = TestUtil.createLocation();
    when(util.jsonToObject(syncRequest, SyncRequest.class)).thenReturn(request);
    when(util.isLocationExists(anyString())).thenReturn(true);
    //   when(locationUtil.isCountryExists(anyString())).thenReturn(true);
    //    when(locationUtil.isLocationCountryValid(anyString(), anyString())).thenReturn(true);
    when(Context.getLocationService()).thenReturn(locationService);
    when(Context.getLocationService().getRootLocations(anyBoolean()))
        .thenReturn(Collections.singletonList(location));
    final Date date = Mockito.mock(Date.class);
    PowerMockito.whenNew(Date.class).withAnyArguments().thenReturn(date);
    List<Visit> visitList = new ArrayList<>();
    Visit visit = TestUtil.createVisit();
    visit.setVoided(true);
    visitList.add(visit);
    visit = TestUtil.createVisit();
    VisitType dosingVisit = new VisitType("Dosing", "dosing visit");
    visit.setVisitType(dosingVisit);
    visitList.add(visit);
    when(syncService.getAllVisits(any(Date.class), anyInt(), anyListOf(String.class)))
        .thenReturn(visitList);
    //when
    mockMvc.perform(post(ControllerTestHelper.BASE_URL + "/sync/getAllVisits").content(syncRequest)
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  public void getAllConfigUpdates_shouldReturnConfigurations() throws Exception {
    when(configService.retrieveAllConfigUpdates()).thenReturn(Collections.emptyList());
    mockMvc.perform(get(ControllerTestHelper.BASE_URL + "/sync/config-updates")
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }
}
