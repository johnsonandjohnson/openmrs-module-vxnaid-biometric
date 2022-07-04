/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.exception;

/**
 * Exception represents causes where resources/entities are not found in the application.
 */
public class EntityNotFoundException extends BiometricApiException {

  private static final long serialVersionUID = -6553269773892878457L;

  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
