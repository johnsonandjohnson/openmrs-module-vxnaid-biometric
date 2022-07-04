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

import org.openmrs.Location;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;

/**
 * Defines services to save device.
 */
public interface DeviceUserService {

  /**
   * Save the given device name for the given device id.
   *
   * @param deviceId the id of the device whose name is to be saved
   * @param siteUuid the location uuid
   */
  String saveDeviceName(String deviceId, String siteUuid, Location location)
      throws EntityNotFoundException;


}
