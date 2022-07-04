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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.JsonNode;
import org.openmrs.module.biometric.api.contract.LicenseResponse;
import org.openmrs.module.biometric.api.contract.LocationResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.contract.LicenseRequest;
import org.openmrs.module.biometric.contract.sync.LastSyncUpdateRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Biometric API configuration controller
 */
@Api(value = "Configuration", tags = {"REST API for accessing Configuration information"})
@RequestMapping(value = "/rest/v1/biometric")
@Controller(value = "biometric.configController")
public class ConfigController extends BaseRestController {

  private static final String DEVICE_ID = "deviceId";
  private static final String INVALID_REQUEST_BODY = "Invalid request body";

  @Autowired
  @Qualifier("biometric.configService")
  private ConfigService configService;

  @Autowired
  private BiometricModUtil util;

  /**
   * API to fetch address hierarchy for a given entryName and an addressField.
   *
   * @return address hierarchy
   */
  @ApiOperation(value = "Get Address Hierarchy", notes = "Get Address Hierarchy", response = Set.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful return of the address hierarchy"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to return address hierarchy")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/addresshierarchy", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public Set<String> getAddressHierarchy() throws BiometricApiException {
    return configService.retrieveAddressHierarchy();
  }

  @ApiOperation(value = "Retrieve locations", notes = "Retrieve locations", response = Map.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful return of location details"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to return location details")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/location", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public Map<String, List<LocationResponse>> getLocations() throws BiometricApiException {
    return configService.retrieveLocations();
  }

  /**
   * API to fetch biometric configurations.
   *
   * @param name configuration name
   * @return biometric configurations
   */
  @ApiOperation(value = "Retrieves configuration details by name", notes = "Retrieves config details by name",
      response = JsonNode.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful return of config details by name"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to return config details"),
      @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Config details not found")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/config/{name}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public JsonNode getConfig(
      @ApiParam(name = "name", value = "name", required = true)
      @PathVariable("name") String name) throws IOException, EntityNotFoundException {
    return util
        .toJsonNode(configService.retrieveConfig(SanitizeUtil.sanitizeInputString(name)));
  }

  /**
   * Retrieves the current version of the android app.
   *
   * @return the current version of the android app
   */
  @ApiOperation(value = "Retrieve configuration version details", notes = "Retrieve config version details",
      response = JsonNode.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful return of version details"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to return version details"),
      @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Version details not found")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public JsonNode getVersionInfo() throws IOException, EntityNotFoundException {
    return util.toJsonNode(configService.retrieveConfig("version"));
  }

  /**
   * Retrieve an available license.
   *
   * @param deviceId the id of the device from which the request was received
   * @param licenseRequest contains the details of license to be retrieved
   * @return the license details
   */
  @ApiOperation(value = "Get license", notes = "Get license", response = Map.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful retrieval of license"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to get license"),
      @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error in request details")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/license",
      consumes = {
          MediaType.APPLICATION_JSON_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public Map<String, List<LicenseResponse>> getLicense(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "licenseRequest", value = "License request types")
      @RequestBody String licenseRequest)
      throws IOException, BiometricApiException {

    LicenseRequest request = util.jsonToObject(licenseRequest, LicenseRequest.class);
    if (request.getLicenseTypes().isEmpty()) {
      throw new EntityValidationException(INVALID_REQUEST_BODY);
    }
    List<LicenseResponse> list = configService
        .retrieveLicense(SanitizeUtil.sanitizeInputString(deviceId),
            SanitizeUtil.sanitizeStringList(request.getLicenseTypes()));
    return Collections.singletonMap("licenses", list);
  }

  /**
   * Releases the given license.
   *
   * @param deviceId the id of the device from which the request was received
   * @param licenseRequest contains the license details to be released
   */
  @ApiOperation(value = "Release of license", notes = "Release of license")
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful license release"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to release license"),
      @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error in request details")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/license/release", method = RequestMethod.POST)
  public void releaseLicense(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "licenseRequest", value = "License request types")
      @RequestBody String licenseRequest)
      throws IOException, BiometricApiException {

    LicenseRequest request = util.jsonToObject(licenseRequest, LicenseRequest.class);

    if (request.getLicenseTypes().isEmpty()) {
      throw new EntityValidationException(INVALID_REQUEST_BODY);
    }

    configService.releaseLicense(deviceId, request.getLicenseTypes());
  }

  /**
   * Updated the last sync date of a device.
   *
   * @param deviceId the id of the device from which the request was received
   * @param lastSyncUpdate last sync updated date of a device
   */
  @ApiOperation(value = "Last sync date updated", notes = "Last sync date updated", response = Map.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful updation of sync date"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Sync date updation failed"),
      @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Last sync date not proper in request")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/sync", method = RequestMethod.POST)
  public void updateLastSyncDate(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "lastSyncUpdate", value = "Sync update details", required = true)
      @RequestBody String lastSyncUpdate) throws IOException, BiometricApiException {
    LastSyncUpdateRequest request = util.jsonToObject(lastSyncUpdate, LastSyncUpdateRequest.class);
    if (null == request.getDateSyncCompleted() || request.getDateSyncCompleted() == 0) {
      throw new EntityValidationException(INVALID_REQUEST_BODY);
    }
    configService
        .updateLastSyncDate(deviceId, request.getSiteUuid(), request.getDateSyncCompleted());
  }

  /**
   * Health check endpoint.
   *
   * @return status of the server
   */
  @ApiOperation(value = "Health Status ", notes = "Health Status", response = Map.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Health status UP returned")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/health", method = RequestMethod.GET)
  public Map<String, String> health() {
    return Collections.singletonMap("status", "UP");
  }

  /**
   * API to fetch vaccine schedule.
   *
   * @return vaccine schedule
   */
  @ApiOperation(value = "Retrieves cfl vaccines", notes = "Retrieves cfl vaccines",
      response = JsonNode.class)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful return of vaccine details"),
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failure to return vaccine details"),
      @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Vaccine details not found")})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/config/vaccine-schedule", produces = MediaType.APPLICATION_JSON_VALUE,
      method = RequestMethod.GET)
  public JsonNode getVaccineSchedule() throws IOException, EntityNotFoundException {
    return util.toJsonNode(configService.retrieveVaccineSchedule());
  }
}
