/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric;

import org.openmrs.module.BaseModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the logic that is run every time this module is either started or shutdown.
 */
public class BiometricModuleActivator extends BaseModuleActivator {

  private static final Logger LOGGER = LoggerFactory.getLogger(BiometricModuleActivator.class);

  /**
   * Triggered when biometric module is started
   *
   * @see #started()
   */
  @Override
  public void started() {
    LOGGER.info("Biometric module started");
  }

  /**
   * Triggered when biometric module is stopped
   *
   * @see #stopped()
   */
  @Override
  public void stopped() {
    LOGGER.info("Biometric module Stopped");
  }

}
