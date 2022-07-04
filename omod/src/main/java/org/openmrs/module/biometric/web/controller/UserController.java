/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.service.DeviceUserService;
import org.openmrs.module.biometric.contract.UserResponse;
import org.openmrs.module.biometric.contract.sync.DeviceNameResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Consists of APIs to retrieve users.
 */

@Api(value = "User Details", tags = {
    "REST API for accessing User Information and saving operator device"})
@RequestMapping(value = "/rest/v1/biometric")
@Controller(value = "biometric.userController")
public class UserController extends BaseRestController {

  private static final String DEVICE_ID = "deviceId";
  private static final String SITECODE_ATTRIBUTE_TYPE = "siteCode";

  @Autowired
  private UserService userService;

  @Autowired
  private BiometricModUtil util;

  @Autowired
  private LocationUtil locationUtil;

  @Autowired
  private DeviceUserService deviceUserService;

  /**
   * Retrieve all users present in OpenMRS.
   *
   * @return list of users
   */
  @ApiOperation(value = "Get All User Details", notes = "Get All User Details", response = List.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful return of all user details"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to return user details")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/users", produces = {
      MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
  public List<UserResponse> getAllUsers() {
    List<User> users = userService.getAllUsers();
    List<UserResponse> userResponseList = new ArrayList<>();
    for (User user : users) {
      if (!Boolean.TRUE.equals(user.getRetired())) {
        UserResponse response = new UserResponse();
        response.setUuid(user.getUuid());
        response.setDisplay(user.getGivenName() + " " + user.getFamilyName());
        response.setUsername(user.getUsername());
        userResponseList.add(response);
      }
    }
    return userResponseList;
  }

  /**
   * save a device name for the device with id.
   *
   * @param deviceId the id of the device
   * @param body The json string with the location of the device with the given id
   * @throws IOException exception thrown if converting the json body to object fails
   * @throws EntityValidationException exception thrown when the device name is either empty or
   * null
   */
  @ApiOperation(value = "Save readable device name", notes = "Save readable device name", response = DeviceNameResponse.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful saving of device name"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to save device name"),
      @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error in request details shared")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/devicename",
      consumes = {
          MediaType.APPLICATION_JSON_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public DeviceNameResponse saveReadableDeviceName(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "body", value = "location details", required = false)
      @RequestBody String body)
      throws EntityValidationException, IOException, EntityNotFoundException {

    TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
    };
    Map<String, String> map = util.jsonToObject(body, typeRef);
    if (StringUtils.isBlank(map.get("siteUuid"))) {
      throw new EntityValidationException("siteUuid cannot be null or empty");
    }
    Location location = locationUtil.getLocationByUuid(map.get("siteUuid"));

    if (null == location) {
      throw new EntityValidationException("location is not present");
    }

    String siteCode = locationUtil.getLocationDetails(location, SITECODE_ATTRIBUTE_TYPE);

    String deviceName = deviceUserService
        .saveDeviceName(SanitizeUtil.sanitizeInputString(deviceId), siteCode, location);

    DeviceNameResponse deviceNameResponse = new DeviceNameResponse();
    deviceNameResponse.setDeviceName(SanitizeUtil.sanitizeOutput(deviceName));

    return deviceNameResponse;
  }
}
