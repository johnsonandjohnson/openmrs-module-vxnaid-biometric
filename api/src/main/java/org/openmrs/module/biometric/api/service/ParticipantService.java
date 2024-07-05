/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.biometric.api.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.module.biometric.api.contract.PatientResponse;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.springframework.transaction.annotation.Transactional;

/** Defines the services to register and match participants. */
public interface ParticipantService {

  /**
   * Register a participant.
   *
   * @param patient @see org.openmrs.Patient
   * @return participant's uuid
   * @throws BiometricApiException if participant already exists with the identifier
   */
  Patient registerParticipant(Patient patient) throws BiometricApiException;

  /**
   * Update a patient.
   *
   * @param patient @see org.openmrs.Patient
   * @return updated patient details
   */
  Patient updateParticipant(Patient patient);

  /**
   * Deactivate a patient.
   *
   * @param patient patient to be deactivated
   * @param reason reason for deactivation
   */
  void voidPatient(Patient patient, String reason) throws BiometricApiException, IOException;

  /**
   * Retrieve the participant for a given identifier.
   *
   * @param patientIdentifier unique patient identifier
   * @return Participant details @see org.openmrs.Patient
   */
  @Transactional(readOnly = true)
  List<Patient> retrieveParticipantDetails(String patientIdentifier);

  /**
   * Save a participant's image.
   *
   * @param person @see Person
   * @param base64EncodedImage base 64 encoded string of an image
   * @param deviceId the id of the device from which the request was received
   */
  void saveParticipantImage(Person person, String base64EncodedImage, String deviceId)
      throws BiometricApiException;

  /**
   * Retrieve participant's image.
   *
   * @param personUuid unique identifier of a participant
   * @return participant's image in base64 encoded format
   * @throws IOException when there is an issue in reading the images from the file path
   * @throws BiometricApiException when failed to upload a participant's image
   */
  Optional<String> retrieveParticipantImage(String personUuid)
      throws IOException, BiometricApiException;

  /**
   * Retrieve a patient by his phone.
   *
   * @param phone phone number of a participant.
   * @return participants matched by phone
   * @throws BiometricApiException if there are any issues in fetching the participant's address
   *     details
   * @throws IOException if there are any issues in parsing main configuration defined in a global
   *     property. Config json is parsed to retrieve person address fields
   */
  List<PatientResponse> findByPhone(String phone) throws BiometricApiException, IOException;

  /**
   * Retrieve a patient by identifier.
   *
   * @param patientIdentifier unique identifier for a participant.
   * @return participants matched by unique identifier
   * @throws BiometricApiException if there are any issues in fetching the participant's address
   *     details
   * @throws IOException if there are any issues in parsing main configuration defined in a global
   *     property. Config json is parsed to retrieve person address fields.
   */
  List<PatientResponse> findByParticipantId(String patientIdentifier)
      throws IOException, BiometricApiException;

  /**
   * Retrieves the patient details using patient uuid.
   *
   * @param uuid of a patient
   * @return patient with given uuid
   */
  Patient findPatientByUuid(String uuid);

  /**
   * Retrieves patients by the specified list of person uuids.
   *
   * @param uuids list of person uuids
   * @return list of patients
   * @throws BiometricApiException in case of issues with address fields
   * @throws IOException in case of issues with address fields
   */
  List<PatientResponse> findPatientsByUuids(Set<String> uuids)
      throws BiometricApiException, IOException;

  /**
   * Retrieves participant images by the specified list of person uuids.
   *
   * @param uuids list of person uuids
   * @return participant images @see SyncImageResponse
   * @throws IOException in case of any issues with file operations
   */
  List<SyncImageResponse> findImagesByUuids(Set<String> uuids) throws IOException;

  /**
   * Retrieves biometric details of the participants by the specified list of person uuids.
   *
   * @param uuids list of person uuids
   * @return Biometric details of the participants
   */
  List<SyncTemplateResponse> getBiometricDataByParticipantIds(Set<String> uuids);

  /**
   * Retrieves all patient's identifiers of given type
   *
   * @param identifierName identifier type name
   * @return
   */
  List<PatientIdentifier> getAllIdentifiersByName(String identifierName);
}
