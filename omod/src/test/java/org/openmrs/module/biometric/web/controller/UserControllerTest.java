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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.module.biometric.api.service.DeviceUserService;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({SanitizeUtil.class})
public class UserControllerTest {

  private static final String GET_ALL_USERS_ENDPOINT = ControllerTestHelper.BASE_URL + "/users";
  private static final String SAVE_DEVICENAME_JSON = "save_device_name.json";
  private static final String DEVICE_ID = "deviceId";

  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  @Mock
  private BiometricModUtil util;

  @Mock
  private LocationUtil locationUtil;

  @Mock
  private DeviceUserService deviceUserService;

  @InjectMocks
  private UserController userController;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  public void getAllUsers_shouldRetrieveAllUsersNotRetired() throws Exception {
    User user = TestUtil.createUser();
    when(userService.getAllUsers()).thenReturn(Arrays.asList(user));
    mockMvc.perform(get(GET_ALL_USERS_ENDPOINT).contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    verify(userService, times(1)).getAllUsers();
  }

  @Test
  public void getAllUsers_shouldRetrieveNoUsers() throws Exception {
    User user = TestUtil.createUser();
    user.setRetired(true);
    when(userService.getAllUsers()).thenReturn(Arrays.asList(user));
    mockMvc.perform(get(GET_ALL_USERS_ENDPOINT).contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
    verify(userService, times(1)).getAllUsers();
  }
  @Test
  public void saveDeviceName_shouldThrowBadRequestWhenSiteUuidIsNull() throws Exception {

    String body = "{ \"siteUuid\" : null }";
    TypeReference<Map<String, String>> typeRef
        = new TypeReference<Map<String, String>>() {
    };
    Map<String, String> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, String>>>any()))
        .thenReturn(map);

    mockMvc.perform(post(ControllerTestHelper.BASE_URL + "/devicename")
        .header(DEVICE_ID, "newDeviceId")
        .content(body.getBytes(StandardCharsets.UTF_8))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    verify(deviceUserService, times(0))
        .saveDeviceName(anyString(), anyString(), any(Location.class));
  }

  @Test
  public void saveDeviceName_shouldThrowBadRequestWhenSiteuUuidIsEmpty() throws Exception {

    String body = "{ \"siteUuid\" : \"\" }";
    TypeReference<Map<String, String>> typeRef
        = new TypeReference<Map<String, String>>() {
    };
    Map<String, String> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, String>>>any()))
        .thenReturn(map);

    mockMvc.perform(post(ControllerTestHelper.BASE_URL + "/devicename")
        .header(DEVICE_ID, "newDeviceId")
        .content(body.getBytes(StandardCharsets.UTF_8))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    verify(deviceUserService, times(0))
        .saveDeviceName(anyString(), anyString(), any(Location.class));
  }

  @Test
  public void saveDeviceName_shouldThrowBadRequestWhenLocationIsNull() throws Exception {

    String body = "{ \"siteUuid\" : \"asf\" }";
    TypeReference<Map<String, String>> typeRef
        = new TypeReference<Map<String, String>>() {
    };
    Map<String, String> map = new ObjectMapper().readValue(body, typeRef);
    assertThat(map.size(), is(1));
    when(util.jsonToObject(anyString(), Mockito.<TypeReference<Map<String, String>>>any()))
        .thenReturn(map);
    when(locationUtil.getLocationByUuid(anyString())).thenReturn(null);
    mockMvc.perform(post(ControllerTestHelper.BASE_URL + "/devicename")
        .header(DEVICE_ID, "newDeviceId")
        .content(body.getBytes(StandardCharsets.UTF_8))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    verify(deviceUserService, times(0))
        .saveDeviceName(anyString(), anyString(), any(Location.class));
  }
}
