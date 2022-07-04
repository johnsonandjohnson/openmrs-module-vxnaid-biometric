/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;

/**
 * Defines the services for sync calls.
 */
public interface SyncService {

  /**
   * Retrieves all the participant data registered in a program  bases on the sync scope at country
   * or at site level and the records added or modified after a specified date.
   *
   * @param lastModifiedDate date after which the records needs to be retrieved
   * @param maxResultsToFetch number of results to be returned
   * @param locations list of sites from where the participants needs to be retrieved
   * @return @see org.openmrs.Patient
   */
  List<Patient> getAllPatients(Date lastModifiedDate, int maxResultsToFetch,
      List<String> locations);

  /**
   * Get the patient count for the given sites.
   *
   * @param locations list of sites
   * @return the patient count
   */
  Map<String, Long> getPatientCount(List<String> locations);

  /**
   * Retrieves all the participant images registered in a program  bases on the sync scope at
   * country or at site level and the records added or modified after a specified date.
   *
   * @param lastModifiedDate date after which the records needs to be retrieved
   * @param maxResultsToFetch number of results to be returned
   * @param locations from where the images needs to be retrieved
   * @param deviceId the device from where the request was received
   * @param optimizeData true, excludes the images from the requested device and false, includes the
   * images from the requested device also
   * @return @see org.openmrs.module.biometric.api.contract.SyncImageResponse
   * @throws IOException in case if any issues while retrieving the participant images
   */
  List<SyncImageResponse> getAllParticipantImages(
      Date lastModifiedDate,
      int maxResultsToFetch,
      List<String> locations,
      String deviceId,
      boolean optimizeData)
      throws IOException;

  /**
   * To retrieve the image count of participants for the given params.
   *
   * @param locations from where the images needs to be retrieved
   * @param deviceId the device from where the request was received
   * @param optimizeFlag true, excludes the images from the requested device and false, includes the
   * images from the requested device also
   */
  Map<String, Long> getParticipantImagesCount(List<String> locations,
      String deviceId,
      boolean optimizeFlag)
      throws IOException;

  /**
   * Retrieves all the images of participants registered in a program  bases on the sync scope at
   * country at site level and records added or modified after a specified date.
   *
   * @param lastModifiedDate date after which the records needs to be retrieved
   * @param maxResultsToFetch number of results to be returned
   * @param locations from where the visits needs to be retrieved
   * @return @org.openmrs.Visit
   */
  List<Visit> getAllVisits(Date lastModifiedDate, int maxResultsToFetch, List<String> locations);

  /**
   * Retrieves all the biometric templates of the participants registered in a program  bases on the
   * sync scope at country or at site level and the records added or modified after a specified.
   * date
   *
   * @param lastModifiedDate date after which the records needs to be retrieved
   * @param deviceId the id of a device from which the request was received
   * @param country where the program is running
   * @param siteId the site where the program is running
   * @param locations list of locations to fetch the templates
   * @param optimize true, excludes the templates from the requested device and false includes the
   * templates from the requested device also
   * @param maxResultsToFetch number of results to be returned
   * @return @see org.openmrs.module.biometric.api.contract.SyncTemplateResponse
   */
  List<SyncTemplateResponse> getAllBiometricTemplates(Date lastModifiedDate, String deviceId,
      String country,
      String siteId, List<String> locations, boolean optimize, int maxResultsToFetch);

  /**
   * Get patient count with biometric templates for the given sites.
   *
   * @param deviceId device id from where the patient was enrolled
   * @param locations list of sites
   * @param optimize true, will skip the patient images captured on the device, false will include
   * @return patient count with biometric templates
   */
  Map<String, Long> getBiometricTemplatesCount(String deviceId, List<String> locations,
      boolean optimize);

  /**
   * Get visit count for the given sites.
   *
   * @param locations list of sites
   * @return count of dosing visits
   */
  Map<String, Long> getVisitsCount(List<String> locations);

  /**
   * Saves the sync errors in a mobile device.
   *
   * @param deviceId the id of a device whose error is logged
   * @param stackTrace the error trace
   * @param createdDate the error reported date
   * @param key the error key and type
   * @param meta meta data
   */
  void saveSyncError(String deviceId, String stackTrace, Date createdDate, String key, String meta);

  /**
   * Updates the sync errors resolved in a mobile device.
   *
   * @param deviceId ,the id of a device whose error is resolved
   * @param errorKeys ,the error keys resolved
   */
  void resolveSyncErrors(String deviceId, List<String> errorKeys) throws EntityNotFoundException;
}
