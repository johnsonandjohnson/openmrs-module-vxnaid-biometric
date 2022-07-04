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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openmrs.module.biometric.api.contract.LicenseResponse;
import org.openmrs.module.biometric.api.contract.LocationResponse;
import org.openmrs.module.biometric.api.contract.SyncConfigResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;

/**
 * Defines methods to retrieve the configurations required.
 */
public interface ConfigService {

  /**
   * Retrieves address hierarchy.
   *
   * @return address hierarchy
   */
  Set<String> retrieveAddressHierarchy() throws BiometricApiException;

  /**
   * Retrieve a configuration by name.
   *
   * @param name configuration global property name suffix
   * @return the configuration value for a give configuration
   */
  String retrieveConfig(String name) throws EntityNotFoundException;

  /**
   * Retrieves all locations configured in the OpenMRS.
   *
   * @return list of locations @see org.openmrs.module.biometric.api.contract.LocationResponse
   */
  Map<String, List<LocationResponse>> retrieveLocations();

  /**
   * retrieves the md5 hash value of all the configurations required to check if there is any
   * update.
   *
   * @return list of configuration update details
   */
  List<SyncConfigResponse> retrieveAllConfigUpdates() throws IOException, BiometricApiException;

  /**
   * Retrieves a free license for the specified license types if available.
   *
   * @param deviceId the id of a device requested for a license
   * @param licenseTypes license types requested by a device like IRIS_CLIENT, IRIS_MATCHING
   * @return the list of licenses required for a device, 1) If the license is not available then it
   * returns a null value 2) If the license is already tagged to the device then it return the same
   * license
   * @throws BiometricApiException if the license types not configured correctly in the backend
   */
  List<LicenseResponse> retrieveLicense(String deviceId, Set<String> licenseTypes)
      throws BiometricApiException;

  /**
   * Release the licenses assigned to a devices.
   *
   * @param deviceId the id of a device requested for a license
   * @param licenseTypes license types requested for release by a device like IRIS_CLIENT,
   * IRIS_MATCHING
   * @return the status of the license release request
   * @throws BiometricApiException if the license types not configured correctly in the backend
   */
  boolean releaseLicense(String deviceId, Set<String> licenseTypes) throws BiometricApiException;

  /**
   * Updates the sync completed date.
   *
   * @param deviceId the id of a device whose sync date is to be updated
   * @param siteId the site id of the device
   * @param dateSyncCompleted the date on which sync is completed
   */
  void updateLastSyncDate(String deviceId, String siteId, Long dateSyncCompleted)
      throws BiometricApiException;

  /**
   * Retrieve vaccine schedule details.
   *
   * @return the vaccine schedule details
   */
  String retrieveVaccineSchedule() throws EntityNotFoundException;
}
