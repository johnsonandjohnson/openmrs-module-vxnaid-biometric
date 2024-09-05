/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.biometric.web.controller;

import static org.openmrs.module.biometric.constants.BiometricModConstants.DEVICE_ID;
import static org.openmrs.module.biometric.constants.BiometricModConstants.OPEN_MRS_ID;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityConflictException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.service.VisitSchedulerService;
import org.openmrs.module.biometric.builder.EncounterBuilder;
import org.openmrs.module.biometric.builder.ObservationBuilder;
import org.openmrs.module.biometric.builder.VisitRequestBuilder;
import org.openmrs.module.biometric.builder.VisitResponseBuilder;
import org.openmrs.module.biometric.constants.BiometricModConstants;
import org.openmrs.module.biometric.contract.NewVisitResponse;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.contract.VisitResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/** Consists of APIs to create/retrieve visit/encounter for a participant. */
@Api(
    value = "Visit Information",
    tags = {"REST API for managing dosing and follow up visits"})
@RequestMapping(value = "/rest/v1/biometric")
@Controller(value = "biometric.VisitController")
public class VisitController extends BaseRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(VisitController.class);
  private static final String VISIT_VALIDATION_MSG =
      "Please check for the mandatory parameters in the request";
  private static final String VISITUUID_COLLISION_SAME_PART_UUID =
      "VisitUuid collision with same participantUuid";
  private static final String VISITUUID_COLLISION_DIFF_PART_UUID =
      "VisitUuid collision with different participantUuid";
  private static final String PARTICIPANT_NOT_FOUND_ERROR =
      "Participant with the given Uuid does not exists";

  private static final String DOSE_NUMBER_ATTRIBUTE_TYPE_NAME = "Dose number";

  @Autowired private VisitSchedulerService visitSchedulerService;

  @Autowired private VisitRequestBuilder visitRequestBuilder;

  @Autowired private VisitResponseBuilder visitResponseBuilder;

  @Autowired private EncounterBuilder encounterBuilder;

  @Autowired private ObservationBuilder observationBuilder;

  @Autowired private BiometricModUtil util;

  /**
   * Create visit for a participant.
   *
   * @param visitRequest, visit information of a participant
   * @return new visit uuid of a participant
   * @throws IOException if the request is invalid
   * @throws EntityNotFoundException if the location is not exists
   * @throws EntityValidationException if the request is invalid
   * @throws ParseException if the date is not valid
   */
  @ApiOperation(
      value = "Create Visit for a patient",
      notes = "Create visit for a patient",
      response = NewVisitResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "On successful creation of visit"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_INTERNAL_ERROR,
            message = "Failure to create a visit"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_CONFLICT,
            message = "Visit id conflicts with another visit"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_BAD_REQUEST,
            message = "Invalid details shared for visit creation")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(
      value = "/visit",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public NewVisitResponse createVisit(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "visitRequest", value = "Details of visit to create") @RequestBody
          String visitRequest)
      throws IOException, BiometricApiException, ParseException {
    VisitRequest request = util.jsonToObject(visitRequest, VisitRequest.class);

    // check for mandatory fields
    if (StringUtils.isBlank(request.getParticipantUuid())
        || StringUtils.isBlank(request.getVisitType())
        || StringUtils.isBlank(request.getStartDatetime())
        || StringUtils.isBlank(request.getLocationUuid())) {
      throw new EntityValidationException(VISIT_VALIDATION_MSG);
    }

    Visit visit = visitRequestBuilder.createFrom(request);
    // check if visit already exists
    validateVisitParticipantUuids(request.getParticipantUuid(), request.getVisitUuid());
    Visit newVisit = visitSchedulerService.createOrUpdateVisit(visit);
    return visitResponseBuilder.createFrom(newVisit);
  }

  /**
   * Retrieve the visit details using the participant's uuid.
   *
   * @param personUuid unique participant's uuid
   * @return visit details of a participant
   * @throws EntityNotFoundException if participant does not exist with the given uuid
   */
  @ApiOperation(
      value = "Get Visit Details Of a Patient",
      notes = "Get Visit Details Of a Patient",
      response = VisitResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = HttpURLConnection.HTTP_OK,
            message = "On successful return of visit details"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_INTERNAL_ERROR,
            message = "Failure to return visit details"),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Patient not found")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(
      value = "/visit/{personUuid}",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.GET)
  public List<VisitResponse> retrieveVisit(
      @ApiParam(
              name = "personUuid",
              value = "Person whose visits are to be retrieved",
              required = true)
          @PathVariable("personUuid")
          String personUuid)
      throws EntityNotFoundException {
    List<Visit> visits =
        visitSchedulerService.findVisitByPersonUuid(SanitizeUtil.sanitizeInputString(personUuid));
    return visitResponseBuilder.createFrom(visits);
  }

  /**
   * Creates a new encounter with the given observations.
   *
   * @param visitRequest details to create a new dosing visit
   * @return visit uuid
   * @throws IOException if an invalid request is passed
   */
  @ApiOperation(
      value = "Create encounter details",
      notes = "Create encounter details",
      response = NewVisitResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = HttpURLConnection.HTTP_OK,
            message = "On successful creation of encounter for visit"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_INTERNAL_ERROR,
            message = "Failure to create encounter"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_BAD_REQUEST,
            message = "Request details to save encounter is not valid"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_NOT_FOUND,
            message = "Visit for creating encounter not found")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(
      value = "/encounter",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public NewVisitResponse createEncounter(
      @ApiParam(name = "visitRequest", value = "Visit details to create encounter", required = true)
          @RequestBody
          String visitRequest)
      throws IOException, ParseException, BiometricApiException {

    VisitRequest request = util.jsonToObject(visitRequest, VisitRequest.class);

    // check for mandatory fields
    if (null == request.getObservations()
        || request.getObservations().isEmpty()
        || null == request.getVisitUuid()
        || null == request.getLocationUuid()) {
      throw new EntityValidationException(
          "Please check the mandatory params VisitId/LocationId/Observations");
    }

    Visit visitToAddEncounter = findVisitToAddEncounter(request.getVisitUuid());

    // Build updated visit object with status as occurred
    Visit updatedVisitObj = visitRequestBuilder.createFrom(request, visitToAddEncounter);

    // Build Encounter object
    Encounter encounterObj = encounterBuilder.createFrom(request, visitToAddEncounter);

    // Build observations like vaccine barcode, manufacture etc
    Set<Obs> obsSet =
        observationBuilder.createFrom(request, visitToAddEncounter.getPatient().getPerson());

    // Create a new encounter
    Encounter encounter =
        visitSchedulerService.createEncounter(updatedVisitObj, encounterObj, obsSet);
    LOGGER.info("Encounter created for visit id : {}", request.getVisitUuid());

    // build the response object
    return visitResponseBuilder.createFrom(encounter.getVisit());
  }

  /**
   * Retrieves visits by list of uuids.
   *
   * @param body request body
   * @return list of matched participants details by biometric template/phone/identifier or
   *     combination of these
   */
  @ApiOperation(
      value = "Get Visit Details By Uuids",
      notes = "Get Visit Details by Uuids",
      response = VisitResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = HttpURLConnection.HTTP_OK,
            message = "On successful return visit details"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_INTERNAL_ERROR,
            message = "Failure to return visit details"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_BAD_REQUEST,
            message = "Error in request uuids used")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(
      value = "/getVisitsByUuids",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      method = RequestMethod.POST)
  public List<VisitResponse> getVisitsByUuids(
      @ApiParam(name = "deviceId", value = "Id of the device") @RequestHeader(value = DEVICE_ID)
          String deviceId,
      @ApiParam(name = "body", value = "visit uuid list", required = false) @RequestBody
          String body)
      throws IOException, BiometricApiException {

    LOGGER.debug("getVisitsByUuids request triggered at : {}", new Date());

    Map<String, Set<String>> map =
        util.jsonToObject(body, new TypeReference<Map<String, Set<String>>>() {});
    util.validateUuids(map.get("visitUuids"));
    List<Visit> visits =
        visitSchedulerService.findVisitsByUuids(
            SanitizeUtil.sanitizeStringList(map.get("visitUuids")));
    return visitResponseBuilder.createFrom(visits);
  }

  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/updateEncounterObservations/{visitUuid}", method = RequestMethod.POST)
  public void updateEncounterObservationByVisit(
      @PathVariable("visitUuid") String visitUuid, @RequestBody Map<String, String> obsToAdd)
      throws EntityNotFoundException, ParseException {
    Visit visit = Context.getVisitService().getVisitByUuid(visitUuid);
    if (visit == null) {
      throw new EntityNotFoundException(String.format("Visit with uuid %s not found", visitUuid));
    }

    EncounterService encounterService = Context.getEncounterService();
    Encounter encounter;
    List<Encounter> encounters = encounterService.getEncountersByVisit(visit, false);
    if (CollectionUtils.isEmpty(encounters)) {
      encounter = new Encounter();
      encounter.setEncounterType(
          Context.getEncounterService().getEncounterType(BiometricApiConstants.DOSING_VISIT_TYPE));
      encounter.setPatient(visit.getPatient());
      encounter.setEncounterDatetime(new Date());
      encounter.setLocation(visit.getLocation());
      encounter.setVisit(visit);
      encounterService.saveEncounter(encounter);
    } else {
      encounter = encounters.get(0);
    }

    ObsService obsService = Context.getObsService();
    for (Map.Entry<String, String> entry : obsToAdd.entrySet()) {
      Concept concept = Context.getConceptService().getConcept(entry.getKey());

      if (null == concept) {
        LOGGER.warn(
            "Concept with name {} does not exist. Observation will not be saved!", entry.getKey());
      } else {
        Obs obs = new Obs();
        obs.setConcept(concept);
        obs.setPerson(encounter.getPatient());
        obs.setObsDatetime(new Date());
        obs.setValueAsString(entry.getValue());

        obs.setEncounter(encounter);
        obsService.saveObs(obs, "");
      }
    }
  }

  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(
      value = "/updateParticipantLocation/{participantUuid}",
      method = RequestMethod.POST)
  public void updateParticipantLocation(
      @PathVariable("participantUuid") String participantUuid, @RequestBody String newLocationUuid)
      throws EntityNotFoundException {
    PatientService patientService = Context.getPatientService();
    Patient participant = patientService.getPatientByUuid(participantUuid);
    if (participant == null) {
      throw new EntityNotFoundException(
          String.format("Participant with uuid: %s not found", participantUuid));
    }

    String escapedNewLocationUuid = StringUtils.strip(newLocationUuid, "\"");

    PersonAttribute locationAttribute =
        participant.getAttribute(BiometricModConstants.LOCATION_ATTRIBUTE);
    if (locationAttribute != null) {
      PersonAttribute newLocationAttribute = new PersonAttribute();
      newLocationAttribute.setAttributeType(locationAttribute.getAttributeType());
      newLocationAttribute.setValue(escapedNewLocationUuid);
      participant.addAttribute(newLocationAttribute);

      PatientIdentifier patientIdentifier = participant.getPatientIdentifier(OPEN_MRS_ID);
      if (patientIdentifier != null) {
        patientIdentifier.setVoided(Boolean.TRUE);
        patientIdentifier.setVoidReason("Voided because of creating new one with updated location");

        PatientIdentifier identifierWithNewLocation = new PatientIdentifier();
        identifierWithNewLocation.setIdentifierType(patientIdentifier.getIdentifierType());
        identifierWithNewLocation.setIdentifier(patientIdentifier.getIdentifier());
        identifierWithNewLocation.setPreferred(patientIdentifier.getPreferred());
        identifierWithNewLocation.setLocation(
            Context.getLocationService().getLocationByUuid(escapedNewLocationUuid));
        participant.addIdentifier(identifierWithNewLocation);
      }

      patientService.savePatient(participant);
    }
  }

  private void validateVisitParticipantUuids(String participantUuid, String visitUuid)
      throws BiometricApiException {
    if (null == visitUuid) {
      throw new EntityValidationException(VISIT_VALIDATION_MSG);
    }
    // check if a participant exists with the given participant uuid
    if (!util.isParticipantExists(participantUuid)) {
      throw new EntityNotFoundException(PARTICIPANT_NOT_FOUND_ERROR);
    }

    List<Visit> existingVisitList =
        visitSchedulerService.findVisitsByUuids(Collections.singleton(visitUuid));
    if (!existingVisitList.isEmpty()) {
      if (!existingVisitList.get(0).getPatient().getUuid().equalsIgnoreCase(participantUuid)) {
        throw new BiometricApiException(VISITUUID_COLLISION_DIFF_PART_UUID);
      }
      throw new EntityConflictException(VISITUUID_COLLISION_SAME_PART_UUID);
    }
  }

  private Visit findVisitToAddEncounter(String visitUuid) throws BiometricApiException {

    Visit existingVisit = visitSchedulerService.findVisitByVisitUuid(visitUuid);
    Visit visitToAddEncounter = null;

    if (Boolean.TRUE.equals(existingVisit.getVoided())) {
      // get dose number
      String doseNumOfExistingVisit = null;
      for (VisitAttribute attribute : existingVisit.getAttributes()) {
        if (attribute
            .getAttributeType()
            .getName()
            .equalsIgnoreCase(DOSE_NUMBER_ATTRIBUTE_TYPE_NAME)) {
          doseNumOfExistingVisit = attribute.getValue().toString();
        }
      }

      visitToAddEncounter =
          visitSchedulerService.findNonVoidedDosingVisitByDoseNumber(
              existingVisit.getPatient(), doseNumOfExistingVisit);
      if (null == visitToAddEncounter) {
        throw new BiometricApiException("No active visit found to add an encounter");
      }
    } else {
      visitToAddEncounter = existingVisit;
    }
    return visitToAddEncounter;
  }
}
