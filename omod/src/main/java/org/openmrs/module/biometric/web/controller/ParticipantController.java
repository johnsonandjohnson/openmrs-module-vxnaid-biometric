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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityConflictException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.api.service.BiometricService;
import org.openmrs.module.biometric.api.service.ParticipantService;
import org.openmrs.module.biometric.builder.ParticipantMatchResponseBuilder;
import org.openmrs.module.biometric.builder.PatientBuilder;
import org.openmrs.module.biometric.contract.ParticipantMatchResponse;
import org.openmrs.module.biometric.contract.RegisterRequest;
import org.openmrs.module.biometric.error.ApiError;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.PERSON_TEMPLATE_ATTRIBUTE;

/**
 * Consists of APIs to register and match participants.
 */
@Api(
    value = "Participant",
    tags = {"REST API for manage Participant information(add,retrieve and delete)"})
@RequestMapping(value = "/rest/v1/biometric")
@Controller
public class ParticipantController extends BaseRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantController.class);

  private static final String PERSON_UUID = "uuid";

  private static final String IRIS_STATUS_PARAM_NAME = "isIrisRegistered";
  private static final String GP_BIOMETRIC_ENABLE_MFA = "biometric.enable.mfa";
  private static final String DEVICE_ID = "deviceId";
  private static final String TEMPLATE = "template";
  private static final String BIOGRAPHIC_DATA = "biographicData";
  private static final String PARTICIPANT_UUIDS = "participantUuids";
  private static final String CROSS_COUNTRY_PARTICIPANTS = "isCrossCountryImplementation";
  private static final String PATIENT_ALREADY_EXISTS = "duplicate request";
  private static final String PATIENT_ALREADY_EXISTS_WITH_DIFF_ID =
      "Participant already exists with the same uuid";
  private static final String PARTICIPANT_ID_ALREADY_EXISTS = "Participant id already in use";

  @Autowired
  private PatientBuilder patientBuilder;

  @Autowired
  private ParticipantService participantService;

  @Autowired
  private ParticipantMatchResponseBuilder builder;

  @Autowired
  private BiometricModUtil util;

  @Autowired
  private LocationUtil locationUtil;

  @Autowired
  @Qualifier("biometric.biometricService")
  private BiometricService biometricService;

  /**
   * Register participant in OpenMRS system with minimal biographic data.
   *
   * @param template iris template of a participant
   * @param biographicData biographic information of a participant in json
   * @return participant uuid and status of iris registration in biometric server
   * @throws BiometricApiException if there are any issues in fetching the participant's address
   * details
   * @throws IOException if there are any issues in parsing main configuration defined in a global
   * property. Config json is parsed to retrieve person address fieldsl
   */
  @ApiOperation(
      value = "Register a new participant",
      notes = "Register a new participant",
      response = Map.class)
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful creation of the Participant"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to create a Participant")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/register", consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST)
  public Map<String, Object> register(@RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(hidden = true)
      @RequestParam(value = TEMPLATE, required = false) MultipartFile template,
      @ApiParam(name = "biographicData", value = "data of a participant", required = true)
      @RequestParam(BIOGRAPHIC_DATA) String biographicData)
      throws IOException, BiometricApiException, ParseException {

    RegisterRequest request = util.jsonToObject(biographicData, RegisterRequest.class);
    Patient patient = patientBuilder.createFrom(request);

    validateParticipantIdAndUuid(patient);
    //Register participant in OpenMRS
    Patient registeredPatient = participantService.registerParticipant(patient);

    if (null == registeredPatient) {
      throw new BiometricApiException("Participant registration failed");
    }

    String base64EncodedImage = request.getImage();
    if (null != base64EncodedImage) {
      LOGGER.debug("Participant registered in OpenMRS with UUID : {}", registeredPatient.getUuid());
      participantService
          .saveParticipantImage(patient.getPerson(), base64EncodedImage,
              SanitizeUtil.sanitizeInputString(deviceId));
    }

    boolean isIrisRegistered = false;
    if (null != template) {
      String locationUuid = locationUtil.getLocationUuid(request.getAttributes());

      try {
        Date registrationDate = util.convertIsoStringToDate(request.getRegistrationDate());
        isIrisRegistered = biometricService
            .registerBiometricData(util.removeWhiteSpaces(request.getParticipantId()),
                template.getBytes(),
                deviceId, locationUuid, registrationDate, patient.getUuid());
      } catch (Exception ex) {
        // participant registration should not be failed if there is an issue with the biometric server
        LOGGER.error("Issue with Biometric Server", ex);
      }
    }

    if (isIrisRegistered) {
      util.setPersonAttributeValue(registeredPatient.getUuid(), PERSON_TEMPLATE_ATTRIBUTE,
          deviceId);
    }

    Map<String, Object> responseMap = new HashMap<>();
    responseMap
        .put(PERSON_UUID, SanitizeUtil.sanitizeOutput(registeredPatient.getUuid()));
    responseMap.put(IRIS_STATUS_PARAM_NAME, isIrisRegistered);

    return responseMap;
  }

  /**
   * Stores participant's biometric template.
   *
   * @param deviceId the id of the device from which the request was received
   * @param personUuid person uuid in openmts
   * @param template biometric template of the participant
   * @throws BiometricApiException in case of any errors in enrollment
   * @throws IOException in case of any errors in parsing the template
   */
  @ApiOperation(
      value = "Store participant biometric template",
      notes = "Store participant biometric template"
  )
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful enrollment of participant biometric template"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to store participant template")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/persontemplate/{personUuid}", consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST)
  public final void registerTemplate(@RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "personUuid", value = "person uuid", required = true)
      @PathVariable("personUuid")
          String personUuid,
      @ApiParam(hidden = true)
      @RequestParam(value = TEMPLATE) MultipartFile template)
      throws BiometricApiException, IOException {

    //check if participant exists
    Patient patient = participantService.findPatientByUuid(personUuid);
    if (null == patient || null == patient.getPatientIdentifier()) {
      throw new EntityNotFoundException("Participant not found");
    }

    //check if template is already present
    Set<String> participantSet = new HashSet<>();
    participantSet.add(patient.getPatientIdentifier().getIdentifier());
    List<BiometricMatchingResult> biometricResults = biometricService
        .matchBiometricData(template.getBytes(), participantSet);

    if (!CollectionUtils.isEmpty(biometricResults)) {
      throw new BiometricApiException("Template already exists for this participant");
    }

    biometricService
        .registerBiometricData(
            util.removeWhiteSpaces(patient.getPatientIdentifier().getIdentifier()),
            template.getBytes(),
            deviceId, patient.getPatientIdentifier().getLocation().getUuid(),
            new Date(patient.getDateCreated().getTime()), patient.getUuid());
  }

  /**
   * Match a participant using an identifier or phone or biometric template or combination of them.
   *
   * <p>when a combination of phone and identifier is used then find all the participants with
   * phone and find all participants with identifier and combine the results.
   *
   * <p>when a combination of biographic and biometric data is used for matching then find all the
   * participants with phone and find all participants with identifier and combine the results and
   * return the results if the biometric template matches.
   *
   * <p>If Multi factor search is enabled then the biometric template matching will be done only
   * for the participants matched with biographic criteria.
   *
   * <p>If Multi factor search is not enabled then the biometric results and biographic results are
   * combined.
   *
   * @param template biometric template to search a participant
   * @param participantId participant's id
   * @param phone participant's phone
   * @param country participant's country
   * @return list of matched participants details by biometric template/phone/identifier ot
   * combination of these
   */
  @ApiOperation(
      value = "Retrieves list of matched participants",
      notes = "Retrieves list of matched participants",
      response = ParticipantMatchResponse.class,
      responseContainer = "List")
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful match of the Participant"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Invalid or incomplete request passed"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to match a Participant")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/match", consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
  public List<ParticipantMatchResponse> match(

      @ApiParam(name = "template", value = "Iris template", required = false)
      @RequestParam(value = TEMPLATE, required = false)
          MultipartFile template,
      @ApiParam(name = "phone", value = "Phone number of a participant", required = false)
      @RequestParam(value = "phone", required = false)
          String phone,
      @ApiParam(name = "participantId", value = "Participant unique id", required = false)
      @RequestParam(value = "participantId", required = false)
          String participantId,
      @ApiParam(name = "country", value = "Country, participant belongs to", required = false)
      @RequestParam(value = "country", required = false)
          String country)
      throws IOException, BiometricApiException {

    if (null == template && StringUtils.isEmpty(participantId) && StringUtils.isEmpty(phone)) {
      throw new EntityValidationException(
          "template/ParticipantId/Phone is required for match a participant");
    }

    boolean isMFAEnabled = Boolean
        .parseBoolean(
            Context.getAdministrationService().getGlobalProperty(GP_BIOMETRIC_ENABLE_MFA));

    List<PatientResponse> patients = findBiographicData(
        SanitizeUtil.sanitizeInputString(participantId), SanitizeUtil.sanitizeInputString(phone));
    List<PatientResponse> patientsWithCountry = findPatientsWithCountry(country, patients);

    Set<String> participantSet = new HashSet<>(10);
    for (PatientResponse participant : patientsWithCountry) {
      participantSet.add(participant.getParticipantId());
    }

    List<BiometricMatchingResult> biometricResults = new ArrayList<>();
    List<ParticipantMatchResponse> responseList;

    if (null != template) {
      if (isMFAEnabled) {
        biometricResults = findByBiometricData(template, participantSet);
      } else {
        biometricResults = findByBiometricData(template, Collections.emptySet());
      }
    }
    responseList = builder.createFrom(biometricResults, patientsWithCountry);
    return responseList;
  }


  /**
   * Void a participant (soft delete).
   *
   * @param personUuid person uuid
   * @param reason reason for voiding a participant
   * @throws EntityNotFoundException if participant does not exists
   */
  @ApiOperation(value = "Soft delete a participant", notes = "Soft delete a participant")
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful deletion of the Participant"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to delete Participant"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_NOT_FOUND,
              message = "Given participant not found")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/participant/{personUuid}", method = RequestMethod.PUT)
  public void voidParticipant(
      @ApiParam(name = "personUuid", value = "person uuid", required = true)
      @PathVariable("personUuid")
          String personUuid,
      @ApiParam(name = "reason", value = "reason for deleting", required = true)
      @RequestParam("reason")
          String reason)
      throws BiometricApiException, IOException {

    Patient patient = Context.getPatientService().getPatientByUuid(personUuid);
    if (null == patient || patient.getVoided()) {
      throw new EntityNotFoundException("Participant does not exists or already de-activated");
    }
    participantService.voidPatient(patient, reason);
  }


  /**
   * Retrieves the participants by participant uuids.
   *
   * @param deviceId the id of the device from which the request was received
   * @param body request body which contains list of person uuids
   * @return list of matched participants
   * @throws IOException in case of any issues with the address fields or input data
   * @throws BiometricApiException in case of any issues with the address fields
   */
  @ApiOperation(
      value = "Retrieves list of matched participants by uuids",
      notes = "Retrieves list of matched participants by uuids",
      response = PatientResponse.class,
      responseContainer = "List")
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful of the Participants with Uuids"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to participants"),
          @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error in request uuids")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/getParticipantsByUuids", consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
  public List<PatientResponse> getParticipantsByUuids(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "body", value = "Participant uuids", required = true)
      @RequestBody String body)
      throws IOException, BiometricApiException {

    LOGGER.debug("getParticipantsByUuids request triggered at : {}", new Date());

    Map<String, Set<String>> map =
        util.jsonToObject(body, new TypeReference<Map<String, Set<String>>>() {
        });

    util.validateUuids(map.get(PARTICIPANT_UUIDS));
    return participantService
        .findPatientsByUuids(SanitizeUtil.sanitizeStringList(map.get(PARTICIPANT_UUIDS)));
  }

  /**
   * Retrieves the biometric templates by participant uuids.
   *
   * @param deviceId the id of the device from which the request was received
   * @param body request body which contains list of person uuids
   * @return list of matched participants details by biometric template/phone/identifier ot
   * combination of these
   * @throws IOException in case of any issues with input data
   * @throws EntityValidationException in case of any issues with the input data
   */
  @ApiOperation(
      value = "Retrieves list of biometric templates by uuids",
      notes = "Retrieves list of biometric templates by uuids",
      response = SyncTemplateResponse.class,
      responseContainer = "List")
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful return of the biometric templates"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to return templates"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Error in the request uuids")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/getBiometricTemplatesByUuids", consumes = {
      MediaType.APPLICATION_JSON_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST)
  public List<SyncTemplateResponse> getBiometricTemplatesByUuids(
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "body", value = "template uuids", required = true)
      @Valid @RequestBody String body)
      throws IOException, EntityValidationException {

    LOGGER.debug("getBiometricTemplatesByUuids request triggered at : {}", new Date());

    Map<String, Set<String>> map =
        util.jsonToObject(body, new TypeReference<Map<String, Set<String>>>() {
        });
    util.validateUuids(map.get(PARTICIPANT_UUIDS));
    return participantService.getBiometricDataByParticipantIds(
        SanitizeUtil.sanitizeStringList(map.get(PARTICIPANT_UUIDS)));
  }

  /**
   * Retrieve participant's image.
   *
   * @param personUuid unique identifier of a participant
   * @return participant's image in base64 encoded format
   * @throws IOException when there is an issue in reading the images from the file path
   * @throws BiometricApiException when failed to upload a participant's image
   */
  @ApiOperation(
      value = "Retrieves participant's image in base64 encoded format by uuid",
      notes = "Retrieves participant's image in base64 encoded format by uuid",
      response = String.class,
      responseContainer = "String")
  @ApiResponses(
      value = {
        @ApiResponse(
            code = HttpURLConnection.HTTP_OK,
            message = "On successful return of the person image"),
        @ApiResponse(
            code = HttpURLConnection.HTTP_INTERNAL_ERROR,
            message = "Failure to return person image"),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Person image not found")
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(
      value = "/personimage/{personUuid}",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.GET)
  public Object retrievePersonImage(
      @ApiParam(name = "personUuid", value = "uuid of person", required = true)
          @PathVariable("personUuid")
          String personUuid)
      throws IOException, BiometricApiException {
    Optional<String> imageString = participantService.retrieveParticipantImage(personUuid);
    if (imageString.isPresent()) {
      return imageString.get();
    } else {
      return new ApiError(
          HttpStatus.NOT_FOUND.value(),
          "Unable to retrieve image for person with uuid: " + personUuid);
    }
  }

  /**
   * Retrieves the person images by the participant uuids.
   *
   * @param deviceId the id of the device from which the request was received
   * @param body request body which contains list of person uuids
   * @return list of matched participants details by biometric template/phone/identifier ot
   * combination of these
   * @throws IOException in case of any issues with the file operations
   * @throws EntityValidationException in case of any issues with the input data
   */

  @ApiOperation(
      value = "Retrieves images by uuids",
      notes = "Retrieves images by uuids",
      response = SyncImageResponse.class,
      responseContainer = "List")
  @ApiResponses(
      value = {
          @ApiResponse(
              code = HttpURLConnection.HTTP_OK,
              message = "On successful return of the images with uuids"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_INTERNAL_ERROR,
              message = "Failure to return images"),
          @ApiResponse(
              code = HttpURLConnection.HTTP_BAD_REQUEST,
              message = "Error in the request uuids")
      })

  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @RequestMapping(value = "/getImagesByUuids", consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
  public List<SyncImageResponse> getImagesByUuids(
      @ApiParam(name = "deviceId", value = "Id of the device", required = true)
      @RequestHeader(value = DEVICE_ID) String deviceId,
      @ApiParam(name = "body", value = "Image uuids", required = true)
      @RequestBody String body)
      throws IOException, EntityValidationException {

    LOGGER.debug("getImagesByUuids request triggered at : {}", new Date());
    Map<String, Set<String>> map = util
        .jsonToObject(body, new TypeReference<Map<String, Set<String>>>() {
        });
    util.validateUuids(map.get(PARTICIPANT_UUIDS));
    return participantService
        .findImagesByUuids(SanitizeUtil.sanitizeStringList(map.get(PARTICIPANT_UUIDS)));
  }

  private List<PatientResponse> findByParticipantId(String participantId)
      throws IOException, BiometricApiException {
    List<PatientResponse> patients = participantService.findByParticipantId(participantId);
    if (null == patients || patients.isEmpty()) {
      return Collections.emptyList();
    }
    return patients;
  }

  private List<PatientResponse> findByPhone(String phone)
      throws IOException, BiometricApiException {
    return participantService.findByPhone(phone);
  }

  private List<BiometricMatchingResult> findByBiometricData(
      MultipartFile template, Set<String> participantSet) {
    List<BiometricMatchingResult> participants = new ArrayList<>();
    try {
      participants = biometricService.matchBiometricData(template.getBytes(), participantSet);
    } catch (Exception ex) {
      // response should be returned if biometric server has some issues
      LOGGER.error("Issue with Biometric Server", ex);
    }
    return participants;
  }

  private List<PatientResponse> findBiographicData(String participantId, String phone)
      throws IOException, BiometricApiException {
    List<PatientResponse> patientMatchesWithPhone = new ArrayList<>();
    List<PatientResponse> patientMatchesWithParticipantId = new ArrayList<>();

    if (null != phone) {
      patientMatchesWithPhone = findByPhone(phone);
    }
    if (null != participantId) {
      patientMatchesWithParticipantId = findByParticipantId(participantId);
    }

    // combine participants by phone and by participant id
    return util.mergePatients(patientMatchesWithPhone, patientMatchesWithParticipantId);
  }

  private List<PatientResponse> findPatientsWithCountry(
      String country, List<PatientResponse> patients) throws BiometricApiException {
    List<PatientResponse> patientMatchesWithCountry = new ArrayList<>();
    if (Boolean.parseBoolean(
        Context.getAdministrationService().getGlobalProperty(CROSS_COUNTRY_PARTICIPANTS))) {
      if (null == country || country.isEmpty()) {
        throw new EntityValidationException(
            "template/ParticipantId/Phone/Country is required for match a participant");
      } else {
        for (PatientResponse patientResponse : patients) {
          List<AttributeData> attributeList = patientResponse.getAttributes();
          String personCountry = getCountryFromPersonAttributes(attributeList);
          if (personCountry.equalsIgnoreCase(country)) {
            patientMatchesWithCountry.add(patientResponse);
          }
        }
        return patientMatchesWithCountry;
      }
    } else {
      return patients;
    }
  }

  private String getCountryFromPersonAttributes(List<AttributeData> attributeList)
      throws BiometricApiException {
    String locationUuid = locationUtil.getLocationUuid(attributeList);
    if (null == locationUuid) {
      throw new BiometricApiException("Participant does not have a valid location");
    }
    Location location = locationUtil.getLocationByUuid(locationUuid);
    if (null == location) {
      throw new BiometricApiException("Participant does not have a valid location");
    }
    return location.getCountry();
  }

  private void validateParticipantIdAndUuid(Patient patient)
      throws BiometricApiException, IOException {
    if (null != patient.getUuid()) {
      List<PatientResponse> existingPatientList =
          participantService.findPatientsByUuids(Collections.singleton(patient.getUuid()));
      if (!existingPatientList.isEmpty()) {
        for (PatientResponse patient1 : existingPatientList) {
          if (!patient1
              .getParticipantId()
              .equalsIgnoreCase(patient.getPatientIdentifier().getIdentifier())) {
            throw new BiometricApiException(PATIENT_ALREADY_EXISTS_WITH_DIFF_ID);
          }
        }
        throw new EntityConflictException(PATIENT_ALREADY_EXISTS);
      } else {
        List<PatientResponse> existingPatientIdList =
            participantService.findByParticipantId(patient.getPatientIdentifier().getIdentifier());
        if (!existingPatientIdList.isEmpty()) {
          throw new BiometricApiException(PARTICIPANT_ID_ALREADY_EXISTS);
        }
      }
    }
  }
}
