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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.module.biometric.api.contract.LocationResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.contract.LicenseRequest;
import org.openmrs.module.biometric.contract.sync.LastSyncUpdateRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Consists of unit tests for the ConfigController
 *
 * @see ConfigController
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigControllerTest {

  private static final String ADDRESS_HIERARCHY_ENDPOINT = "/rest/v1/biometric/addresshierarchy";
  private static final String BIOMETRIC_CONFIGURATION_ENDPOINT = "/rest/v1/biometric/config/{name}";
  private static final String VACCINE_SCHEDULE_ENDPOINT = "/rest/v1/biometric/config/vaccine-schedule";
  private static final String ENTRY_NAME_PARAM = "entryName";
  private static final String ADDRESS_FIELD_PARAM = "addressField";
  private static final String ENTRY_NAME_VALUE = "Belgium";
  private static final String ADDRESS_FIELD_VALUE = "COUNTRY";
  private static final String CREATE_SYNC_REQUEST = "create_request_sync_date.json";
  private static final String CREATE_SYNC_REQUEST_NULL = "create_request_sync_date_null.json";
  private static final String CREATE_SYNC_REQUEST_ZERO = "create_request_sync_date_zero.json";
  private static final String DEVICE_ID = "deviceId";
  private static final String CREATE_LICENSE_REQUEST = "create_license_request.json";
  private static final String CREATE_LICENSE_REQUEST_EMPTY = "create_license_request_empty.json";
  private static final String BIOMETRIC_VERSION_ENDPOINT = "/rest/v1/biometric/version";
  private static final String BIOMETRIC_LOCATION_ENDPOINT = "/rest/v1/biometric/location";
  private MockMvc mockMvc;

  @Mock
  private ConfigService configService;

  @Mock
  private BiometricModUtil util;

  @InjectMocks
  private ConfigController configController;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(configController).build();
  }

  @Test
  public void addressHierarchy_shouldReturnOkStatusIfEntryNameAndAddressFieldIsPassed()
      throws Exception {

    //When
    mockMvc.perform(get(ADDRESS_HIERARCHY_ENDPOINT).param(ENTRY_NAME_PARAM, ENTRY_NAME_VALUE)
        .param(ADDRESS_FIELD_PARAM, ADDRESS_FIELD_VALUE)
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    //Then
    verifyInteractions();
  }

  @Test
  public void addressHierarchy_shouldReturnOkStatusIfOnlyAddressFieldIsPassed() throws Exception {
    //Given
    String entryName = "";
    //When
    mockMvc.perform(get(ADDRESS_HIERARCHY_ENDPOINT).param(ENTRY_NAME_PARAM, entryName)
        .param(ADDRESS_FIELD_PARAM, ADDRESS_FIELD_VALUE)
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    //Then
    verifyInteractions();
  }

  @Test
  public void addressHierarchy_shouldReturnOkStatusIfOnlyEntryNameIsPassed() throws Exception {
    //Given
    String entryName = "Belgium";
    String addressField = "";
    //When
    mockMvc.perform(get(ADDRESS_HIERARCHY_ENDPOINT).param(ENTRY_NAME_PARAM, ENTRY_NAME_VALUE)
        .param(ADDRESS_FIELD_PARAM, addressField)
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    //Then
    verifyInteractions();
  }

  @Test
  public void addressHierarchy_shouldReturnOkStatusIfEntryNameAndAddressFieldAreEmpty()
      throws Exception {
    //Given
    String entryName = "";
    String addressField = "";
    //When
    mockMvc.perform(get(ADDRESS_HIERARCHY_ENDPOINT).param(ENTRY_NAME_PARAM, entryName)
        .param(ADDRESS_FIELD_PARAM, addressField)
        .contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    //Then
    verifyInteractions();
  }

    /*@Test
    public void config_shouldReturnOkStatusIfNameIsPassed() throws Exception {
        //Given
        String name = "config1";
        when(configService.retrieveConfig(name)).thenReturn(ControllerTestHelper.getConfiguratoins1());
        JsonNode jsonNode = new ObjectMapper().readTree(ControllerTestHelper.getConfiguratoins1());
        when(util.toJsonNode(ControllerTestHelper.getConfiguratoins1())).thenReturn(jsonNode);
        //When
        mockMvc.perform(get(BIOMETRIC_CONFIGURATION_ENDPOINT, name).contentType(MediaType.TEXT_PLAIN))
               .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
        //Then
        verify(configService, times(1)).retrieveConfig(name);
    }*/

  @Test
  public void config_shouldThrowAssertionErrorIfNameIsEmpty() throws Exception {
    //Given
    String name = "config1";

    when(configService.retrieveConfig(name)).thenThrow(EntityNotFoundException.class);

    //When
    mockMvc.perform(get(BIOMETRIC_CONFIGURATION_ENDPOINT, name))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());

    verify(configService, times(1)).retrieveConfig(name);
  }

  @Test
  public void config_shouldReturnStatus500IfInvalidNameIsPassed() throws Exception {
    //Given
    String name = "invalid";
    when(configService.retrieveConfig(name)).thenThrow(new APIException());
    //When
    try {
      mockMvc.perform(get(BIOMETRIC_CONFIGURATION_ENDPOINT, name).contentType(MediaType.TEXT_PLAIN))
          .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().is(500));
    } catch (APIException e) {
      //Then
      verify(configService, times(1)).retrieveConfig(name);
    }
  }

  private void verifyInteractions() throws BiometricApiException {
    verify(configService, times(1)).retrieveAddressHierarchy();
  }

  @Test
  public void sync_shouldReturn400IfNullLastSyncUpdateIsPassed() throws Exception {

    String lastSyncUpdateRequest = ControllerTestHelper.loadFile(CREATE_SYNC_REQUEST_NULL);
    LastSyncUpdateRequest request = new ObjectMapper()
        .readValue(lastSyncUpdateRequest, LastSyncUpdateRequest.class);
    when(util.jsonToObject(lastSyncUpdateRequest, LastSyncUpdateRequest.class)).thenReturn(request);

    mockMvc.perform(post("/rest/v1/biometric/sync").header(DEVICE_ID, "3new")
        .contentType(MediaType.parseMediaType("application/json"))
        .content(lastSyncUpdateRequest.getBytes("UTF-8")))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

  }

  @Test
  public void sync_shouldReturn400IfZeroLastSyncDateIsPassed() throws Exception {
    String lastSyncUpdateRequest = ControllerTestHelper.loadFile(CREATE_SYNC_REQUEST_ZERO);
    LastSyncUpdateRequest request = new ObjectMapper()
        .readValue(lastSyncUpdateRequest, LastSyncUpdateRequest.class);
    when(util.jsonToObject(lastSyncUpdateRequest, LastSyncUpdateRequest.class)).thenReturn(request);

    mockMvc.perform(post("/rest/v1/biometric/sync").header(DEVICE_ID, "3new")
        .contentType(MediaType.parseMediaType("application/json"))
        .content(lastSyncUpdateRequest.getBytes("UTF-8")))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void sync_shouldReturn200IfValidNewDeviceIdIsPassed() throws Exception {
    String lastSyncUpdateRequest = ControllerTestHelper.loadFile(CREATE_SYNC_REQUEST);
    LastSyncUpdateRequest request = new ObjectMapper()
        .readValue(lastSyncUpdateRequest, LastSyncUpdateRequest.class);
    when(util.jsonToObject(lastSyncUpdateRequest, LastSyncUpdateRequest.class)).thenReturn(request);

    mockMvc.perform(post("/rest/v1/biometric/sync").header(DEVICE_ID, "3new")
        .contentType(MediaType.parseMediaType("application/json"))
        .content(lastSyncUpdateRequest.getBytes("UTF-8")))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void license_shouldReturn200ValidLicenseRequest() throws Exception {
    String licenseRequest = ControllerTestHelper.loadFile(CREATE_LICENSE_REQUEST);

    LicenseRequest request = new ObjectMapper().readValue(licenseRequest, LicenseRequest.class);
    when(util.jsonToObject(licenseRequest, LicenseRequest.class)).thenReturn(request);

    mockMvc.perform(post("/rest/v1/biometric/license").header(DEVICE_ID, 1)
        .contentType(MediaType.parseMediaType("application/json"))
        .content(licenseRequest.getBytes(StandardCharsets.UTF_8)))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void license_shouldReturn400EmptyLicenseRequest() throws Exception {
    String licenseRequest = ControllerTestHelper.loadFile(CREATE_LICENSE_REQUEST_EMPTY);

    LicenseRequest request = new ObjectMapper().readValue(licenseRequest, LicenseRequest.class);
    when(util.jsonToObject(licenseRequest, LicenseRequest.class)).thenReturn(request);

    mockMvc.perform(post("/rest/v1/biometric/license").header(DEVICE_ID, 1)
        .contentType(MediaType.parseMediaType("application/json"))
        .content(licenseRequest.getBytes(StandardCharsets.UTF_8)))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void licenserequest_shouldReturn200ValidLicenseRequest() throws Exception {
    String licenseRequest = ControllerTestHelper.loadFile(CREATE_LICENSE_REQUEST);

    LicenseRequest request = new ObjectMapper().readValue(licenseRequest, LicenseRequest.class);
    when(util.jsonToObject(licenseRequest, LicenseRequest.class)).thenReturn(request);

    mockMvc.perform(post("/rest/v1/biometric/license/release").header(DEVICE_ID, 1)
        .contentType(MediaType.parseMediaType("application/json"))
        .content(licenseRequest.getBytes(StandardCharsets.UTF_8)))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void licenserequest_shouldReturn400EmptyLicenseRequest() throws Exception {
    String licenseRequest = ControllerTestHelper.loadFile(CREATE_LICENSE_REQUEST_EMPTY);

    LicenseRequest request = new ObjectMapper().readValue(licenseRequest, LicenseRequest.class);
    when(util.jsonToObject(licenseRequest, LicenseRequest.class)).thenReturn(request);

    mockMvc.perform(post("/rest/v1/biometric/license/release").header(DEVICE_ID, 1)
        .contentType(MediaType.parseMediaType("application/json"))
        .content(licenseRequest.getBytes(StandardCharsets.UTF_8)))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void version_shouldReturnOkStatus() throws Exception {

    Location location = new Location();
    location.setUuid("9dd05e68-4d77-4b61-b2bf-436857f35aa0");
    location.setName("kerala");
    location.setCountry("india");
    LocationResponse locationResponse = new LocationResponse();
    locationResponse.setUuid(location.getUuid());
    locationResponse.setName(location.getName());
    locationResponse.setCountry(location.getCountry());
    Map<String, List<LocationResponse>> map = new HashMap<String, List<LocationResponse>>();
    map.put("locations", Arrays.asList(locationResponse));
    when(configService.retrieveLocations()).thenReturn(map);

    //When
    mockMvc.perform(get(BIOMETRIC_LOCATION_ENDPOINT).contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    //Then
    verify(configService, times(1)).retrieveLocations();
  }

  @Test
  public void vaccineSchedule_shouldReturnNotFoundStatus() throws Exception {

    when(configService.retrieveVaccineSchedule()).thenThrow(EntityNotFoundException.class);

    mockMvc.perform(get(VACCINE_SCHEDULE_ENDPOINT)).andExpect(status().isNotFound());
  }

  @Test
  public void vaccineSchedule_shouldReturnOkStatus() throws Exception {

    when(configService.retrieveVaccineSchedule()).thenReturn("asdf");
    //When
    mockMvc.perform(get(VACCINE_SCHEDULE_ENDPOINT)).andExpect(status().isOk());
    verify(configService, times(1)).retrieveVaccineSchedule();
  }
}
