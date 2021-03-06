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
 * Root Exception class for thr application. All exceptions in Biometric Api must extend this.
 */
public class BiometricApiException extends Exception {

  private static final long serialVersionUID = -8683772631078875608L;

  public BiometricApiException(String message) {
    super(message);
  }

  public BiometricApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
