/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.error;

/**
 * API Error response.
 */
public class ApiError {

  private int statusCode;

  private String message;

  /**
   * API Error response
   *
   * @param statusCode the HTTP status code
   * @param message    the error message associated with exception
   */

  public ApiError(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;

  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getMessage() {
    return message;
  }
}
