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

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.IGNORED_COUNT;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.TABLE_COUNT;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.VOIDED_COUNT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.biometric.api.contract.SyncConfigResponse;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.api.service.SyncService;
import org.openmrs.module.biometric.builder.ParticipantRecordsResponseBuilder;
import org.openmrs.module.biometric.builder.SyncResponseBuilder;
import org.openmrs.module.biometric.builder.VisitResponseBuilder;
import org.openmrs.module.biometric.contract.VisitResponse;
import org.openmrs.module.biometric.contract.sync.SyncError;
import org.openmrs.module.biometric.contract.sync.SyncErrorRequest;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncResponse;
import org.openmrs.module.biometric.contract.sync.SyncScope;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * Consists of APIs to retrieve participants and their biometric templates, images and visits. These
 * APIs are used to synchronize the data.
 */
@Api(
    value = "Sync Controller",
    tags = {"REST APIs used by Mobile or Tablet devices to synchronize with the backend data"})
@RequestMapping(value = "/rest/v1/biometric")
@Controller
public class SyncController extends BaseRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncController.class);
  private static final String DEVICE_ID = "deviceId";
  private static final String INVALID_REQUEST_BODY = "Invalid request body";
  private static final String LOCATION_NOT_FOUND = "Location not found";

  @Autowired
  private ParticipantRecordsResponseBuilder participantRecordsResponseBuilder;

  @Autowired
  private SyncResponseBuilder syncResponseBuilder;

  @Autowired
  private VisitResponseBuilder visitResponseBuilder;

  @Autowired
  private ConfigService configService;

  @Autowired
  private BiometricModUtil util;

  @Autowired
  private LocationUtil locationUtil;

  @Autowired
  @Qualifier("biometric.syncService")
  private SyncService syncService;

  /**
   * Retrieves all the participant data registered in a program bases on the sync scope at country
   * or at site level and the records added or modified after a specified date.
   *
   * @param syncRequest contains the details of sync request like sync scope, last modified date,
   * off set and number of results to be retrieved
   * @return @see org.openmrs.module.biometric.contract.sync.SyncResponse
   * @throws IOException if the request is invalid
   * @throws EntityNotFoundException if the site id is not found
   * @throws EntityValidationException, if the input data is not valid
   */
  @ApiOperation(
      value = "Get All Participant Details for the given sync scope and last modified date",
      notes = "Get All Participant Details for the given sync scope and last modified date",
      response = SyncResponse.class)
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "Successfully fetched all participant details"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failed to get all participant details"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Sync Error request to fetch details is not proper")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/sync/getAllParticipants",
      consumes = {
          MediaType.APPLICATION_JSON_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public SyncResponse getAllParticipants(
      @ApiParam(
          name = "syncRequest",
          value = "Request details to fetch participant details",
          required = false)
      @RequestBody
          String syncRequest)
      throws IOException, EntityNotFoundException, EntityValidationException {

    final Instant start = Instant.now();
    SyncRequest request = util.jsonToObject(syncRequest, SyncRequest.class);
    locationUtil.validateSyncLocationData(request);
    LOGGER.info(
        "Sync request is triggered for the country : {} and the site : {} to retrieve participant data",
        request.getSyncScope().getCountry(),
        request.getSyncScope().getSiteUuid());

    List<String> locations = getLocations(request.getSyncScope());
    if (locations.isEmpty()) {
      throw new EntityNotFoundException("Location not found for the given sync scope");
    }
    Date dateModified = null;
    if (null != request.getDateModifiedOffset()) {
      dateModified = new Date(request.getDateModifiedOffset());
    }
    int maxResultsToFetch = request.getLimit() + request.getUuidsWithDateModifiedOffset().size();
    List<Patient> patients = syncService.getAllPatients(dateModified, maxResultsToFetch, locations);

    for (String uuid : request.getUuidsWithDateModifiedOffset()) {
      patients.removeIf(
          e ->
              uuid.equals(e.getUuid())
                  && request.getDateModifiedOffset() == e.getDateChanged().getTime());
    }

    Map<String, Long> map = syncService.getPatientCount(locations);
    Instant end = Instant.now();
    LOGGER.info("Sync-Participants call execution time : {}", Duration.between(start, end));
    return participantRecordsResponseBuilder.createFrom(
        patients, map.get(TABLE_COUNT), map.get(VOIDED_COUNT), request);
  }

  /**
   * Retrieves all the visits created for a participant bases on the sync scope at country or at
   * site level and the records added or modified after a specified date.
   *
   * @param syncRequest contains the details of sync request like sync scope, last modified date,
   * off set and number * of results to be retrieved
   * @return @see org.openmrs.module.biometric.contract.sync.SyncResponse
   * @throws IOException if the request is invalid
   * @throws EntityNotFoundException if the site id is not found
   * @throws EntityValidationException, if the input data is not valid
   */
  @ApiOperation(
      value = "Get All Visit Details for the given sync scope and last modified date",
      notes = "Get All Visit Details for the given sync scope and last modified date",
      response = SyncResponse.class)
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "Successfully fetched visit details"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failed to fetch visit details"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Sync Error request to fetch details is not proper")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/sync/getAllVisits",
      consumes = {
          MediaType.APPLICATION_JSON_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public SyncResponse getAllVisits(@RequestBody String syncRequest)
      throws IOException, EntityNotFoundException, EntityValidationException {

    final Instant start = Instant.now();
    SyncRequest request = util.jsonToObject(syncRequest, SyncRequest.class);
    locationUtil.validateSyncLocationData(request);
    LOGGER.info(
        "Sync request is triggered for the country : {} and the site : {} to retrieve visits data",
        request.getSyncScope().getCountry(),
        request.getSyncScope().getSiteUuid());
    List<String> locations = getLocations(request.getSyncScope());

    if (locations.isEmpty()) {
      throw new EntityNotFoundException(LOCATION_NOT_FOUND);
    }

    Date dateModified = null;
    if (null != request.getDateModifiedOffset()) {
      dateModified = new Date(request.getDateModifiedOffset());
    }
    int maxResultsToFetch = request.getLimit() + request.getUuidsWithDateModifiedOffset().size();

    List<Visit> visits = syncService.getAllVisits(dateModified, maxResultsToFetch, locations);

    for (String uuid : request.getUuidsWithDateModifiedOffset()) {
      visits.removeIf(
          e ->
              uuid.equals(e.getUuid())
                  && request.getDateModifiedOffset() == e.getDateChanged().getTime());
    }
    List<VisitResponse> visitResponses = visitResponseBuilder.createFrom(visits);
    Map<String, Long> map = syncService.getVisitsCount(locations);

    Instant end = Instant.now();
    LOGGER.info("Sync-Visits call execution time : {}", Duration.between(start, end));
    return syncResponseBuilder.createFrom(
        visitResponses, map.get(TABLE_COUNT), null, map.get(VOIDED_COUNT), request);
  }

  /**
   * Retrieves all the participant images registered in a program bases on the sync scope at country
   * or at site level and the records added or modified after a specified date.
   *
   * @param deviceMac the device from where the request was received
   * @param syncRequest contains the details of sync request like sync scope, last modified date,
   * off set and number of results to be retrieved
   * @return @see org.openmrs.module.biometric.contract.sync.SyncResponse
   * @throws IOException if the request is invalid
   * @throws EntityValidationException, if the input data is not valid
   */
  @ApiOperation(
      value = "Get All Participant Image Details for the given sync scope and last modified date",
      notes = "Get All Participant Details for the given sync scope and last modified date",
      response = SyncResponse.class)
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful retrieval of participant images"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to fetch participant images"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Sync Error request to fetch details is not proper")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/sync/getAllParticipantImages", method = RequestMethod.POST)
  public SyncResponse getAllParticipantImages(
      @RequestHeader(value = DEVICE_ID) String deviceMac,
      @ApiParam(
          name = "syncRequest",
          value = "Request details to fetch participant images"
      )
      @RequestBody
          String syncRequest)
      throws IOException, EntityValidationException, EntityNotFoundException {

    final Instant start = Instant.now();
    SyncRequest request = util.jsonToObject(syncRequest, SyncRequest.class);
    locationUtil.validateSyncLocationData(request);
    String deviceId = SanitizeUtil.sanitizeInputString(deviceMac);
    LOGGER.info(
        "Sync-Images request is triggered from the device : {} with scope : {} ",
        deviceId, request.getSyncScope().getSiteUuid());

    if (null == request.getOptimize()) {
      throw new EntityValidationException("Optimize flag is missing");
    }

    List<String> locations = getLocations(request.getSyncScope());

    Date dateModified = null;
    if (null != request.getDateModifiedOffset()) {
      dateModified = new Date(request.getDateModifiedOffset());
    }

    if (locations.isEmpty()) {
      throw new EntityNotFoundException(LOCATION_NOT_FOUND);
    }
    int maxResultsToFetch = request.getLimit() + request.getUuidsWithDateModifiedOffset().size();

    List<SyncImageResponse> records =
        syncService.getAllParticipantImages(
            dateModified,
            maxResultsToFetch,
            locations,
            deviceId,
            request.getOptimize());

    for (String uuid : request.getUuidsWithDateModifiedOffset()) {
      records.removeIf(
          e ->
              uuid.equals(e.getParticipantUuid())
                  && request.getDateModifiedOffset() == e.getDateModified().longValue());
    }
    Map<String, Long> map =
        syncService.getParticipantImagesCount(locations, deviceId, request.getOptimize());
    Instant end = Instant.now();
    LOGGER.info("Sync-ParticipantImages call execution time : {}", Duration.between(start, end));
    return syncResponseBuilder.createFrom(
        records, map.get("tableCount"), map.get("ignoredCount"), map.get("voidedCount"), request);
  }

  /**
   * Retrieves all the biometric templates of the participants registered in a program bases on the
   * sync scope at country or at site level and the records added or modified after a specified
   * date.
   *
   * @param deviceMac the id of a device from which the request was received
   * @param syncRequest contains the details of sync request like sync scope, last modified date,
   * off set and number of results to be retrieved
   * @return the participant biometric templates which satisfied the specified request criteria
   * @throws IOException if the request is invalid
   * @throws EntityValidationException, if the input data is not valid
   * @see org.openmrs.module.biometric.contract.sync.SyncResponse
   */
  @ApiOperation(
      value = "Get All Biometric Template Details for the given sync scope and last modified date",
      notes = "Get All Biometric Template Details for the given sync scope and last modified date",
      response = SyncResponse.class)
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful fetching of biometric templates of participants"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to fetch participant biometric templates"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Sync Error request to fetch details is not proper")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/sync/getAllParticipantBiometricsTemplates",
      consumes = {
          MediaType.APPLICATION_JSON_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public SyncResponse getAllParticipantBiometricsTemplates(
      @RequestHeader(value = DEVICE_ID) String deviceMac,
      @ApiParam(
          name = "syncRequest",
          value = "Request details to fetch participant biometrics template"
      )
      @RequestBody
          String syncRequest)
      throws IOException, EntityValidationException, EntityNotFoundException {

    final Instant start = Instant.now();
    String deviceId = SanitizeUtil.sanitizeInputString(deviceMac);
    SyncRequest request = util.jsonToObject(syncRequest, SyncRequest.class);
    locationUtil.validateSyncLocationData(request);
    LOGGER.info(
        "Sync-Templates request is triggered from the device : {} with scope : {} ",
        deviceId, request.getSyncScope().getSiteUuid());

    if (null == request.getOptimize()) {
      throw new EntityValidationException("Optimize flag is missing");
    }

    List<String> locations = getLocations(request.getSyncScope());

    if (locations.isEmpty()) {
      throw new EntityNotFoundException(LOCATION_NOT_FOUND);
    }

    Date dateModified = null;
    if (null != request.getDateModifiedOffset()) {
      dateModified = new Date(request.getDateModifiedOffset());
    }
    int maxResultsToFetch = request.getLimit() + request.getUuidsWithDateModifiedOffset().size();

    List<SyncTemplateResponse> templates =
        syncService.getAllBiometricTemplates(
            dateModified,
            deviceId,
            request.getSyncScope().getCountry(),
            request.getSyncScope().getSiteUuid(),
            locations,
            request.getOptimize(),
            maxResultsToFetch);

    for (String uuid : request.getUuidsWithDateModifiedOffset()) {
      templates.removeIf(
          e ->
              uuid.equals(e.getParticipantUuid())
                  && request.getDateModifiedOffset().longValue()
                  == e.getDateModified().longValue());
    }

    Map<String, Long> map =
        syncService.getBiometricTemplatesCount(deviceId, locations, request.getOptimize());

    Long tableCount = map.get(TABLE_COUNT);
    Long ignoredCount = null;
    if (request.getOptimize()) {
      ignoredCount = map.get(IGNORED_COUNT);
    }
    Long voidedCount = map.get(VOIDED_COUNT);
    Instant end = Instant.now();
    LOGGER.info("Sync-Templates call execution time : {}", Duration.between(start, end));
    return syncResponseBuilder.createFrom(
        templates, tableCount, ignoredCount, voidedCount, request);
  }

  /**
   * Retrieves the md5 checksum of all configurations.
   *
   * @return org.openmrs.module.biometric.api.contract.SyncConfigResponse
   * @throws BiometricApiException if any exception is thrown while retrieving the address hierarchy
   * md5 checksum
   * @throws IOException if any exception is thrown while building the md5 checksum response
   */
  @ApiOperation(
      value = "Get all the configuration details",
      notes = "Get all the configuration details",
      response = SyncConfigResponse.class)
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful retrieval of configuration details"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to get configuration details")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(
      value = "/sync/config-updates",
      produces = MediaType.APPLICATION_JSON_VALUE,
      method = RequestMethod.GET)
  public List<SyncConfigResponse> getAllConfigUpdates() throws BiometricApiException, IOException {
    return configService.retrieveAllConfigUpdates();
  }

  /**
   * Save device sync errors.
   *
   * @param deviceId the id of a device from which the request was received
   * @param syncErrorRequest Sync error details
   */
  @ApiOperation(value = "Save device sync error", notes = "Save device sync error")
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful saving of device error"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to save the device error"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Error in sync error details shared for saving")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/sync/error", method = RequestMethod.POST)
  public void saveSyncError(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(
          name = "syncErrorRequest",
          value = "sync error details to be saved",
          required = false)
      @RequestBody
          String syncErrorRequest)
      throws IOException, BiometricApiException {

    SyncErrorRequest request = util.jsonToObject(syncErrorRequest, SyncErrorRequest.class);
    List<SyncError> syncErrors = request.getSyncErrors();
    if (null == syncErrors || syncErrors.isEmpty()) {
      throw new EntityValidationException(INVALID_REQUEST_BODY);
    }
    for (SyncError syncError : syncErrors) {
      syncService.saveSyncError(
          deviceId,
          syncError.getStackTrace(),
          syncError.getDateCreated(),
          syncError.getKey(),
          util.toJsonString(syncError.getMetadata()));
    }
  }

  /**
   * Update sync error status to resolved.
   *
   * @param deviceId the id of a device from which the request was received
   * @param body sync error details to be resolved
   */
  @ApiOperation(
      value = "Resolve Sync Errors for the keys provided",
      notes = "Resolve Sync Errors for the keys provided")
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful resolving of all device sync errors"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to resolve all device sync errors"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Sync Error Keys list is empty")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/sync/error/resolved", method = RequestMethod.POST)
  public void resolveSyncError(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "body", value = "sync error keys list", required = false) @RequestBody
          String body)
      throws IOException, BiometricApiException {

    LOGGER.debug("resolveSyncError request triggered at : {}", new Date());
    Map<String, List<String>> map =
        util.jsonToObject(body, new TypeReference<Map<String, List<String>>>() {
        });
    List<String> errorKeys = map.get("syncErrorKeys");
    if (errorKeys.isEmpty()) {
      throw new EntityValidationException("syncErrorKeys cannot be empty");
    }
    syncService.resolveSyncErrors(deviceId, errorKeys);
  }

  private List<String> getLocations(SyncScope syncScope) {

    String country = syncScope.getCountry();
    String cluster = syncScope.getCluster();
    String siteId = syncScope.getSiteUuid();

    List<String> locations = new ArrayList<>();

    if (StringUtils.isNotBlank(siteId)) {
      locations.add(siteId);
    } else if (StringUtils.isNotBlank(cluster)) {
      locations = locationUtil.findLocationsByCluster(country, cluster);
    } else {
      locations = locationUtil.findLocationsByCountry(country);
    }
    return locations;
  }
}
