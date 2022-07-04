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

import org.openmrs.api.APIException;
import org.openmrs.module.biometric.api.exception.EntityConflictException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.error.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Base Rest Controller All controllers in this module extend this for easy error handling */
@RequestMapping(value = "/rest/v1/biometric")
public abstract class BaseRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseRestController.class);

  /**
   * Exception handler for bad request - Http status code of 400
   *
   * @param e the exception throw
   * @return a error response
   */
  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public ApiError handleEntityNotFoundException(EntityNotFoundException e) {
    LOGGER.error(e.getMessage(), e);
    return new ApiError(HttpStatus.NOT_FOUND.value(), e.getMessage());
  }

  /**
   * Exception handler for bad request - Http status code of 400
   *
   * @param e the exception throw
   * @return a error response
   */
  @ExceptionHandler(EntityValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ApiError handleEntityValidationException(EntityValidationException e) {
    LOGGER.error(e.getMessage(), e);
    return new ApiError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
  }

  /**
   * Exception handler for bad request - Http status code of 400
   *
   * @param e the exception throw
   * @return a error response
   */
  @ExceptionHandler({MissingServletRequestParameterException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ApiError handleIllegalArgumentException(MissingServletRequestParameterException e) {
    LOGGER.error(e.getMessage(), e);
    return new ApiError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
  }

  /**
   * Exception handler for conflict - Http status code of 409
   *
   * @param e the exception throw
   * @return a error response
   */
  @ExceptionHandler({EntityConflictException.class})
  @ResponseStatus(HttpStatus.CONFLICT)
  @ResponseBody
  public ApiError handleConflictArgumentException(EntityConflictException e) {
    LOGGER.error(e.getMessage(), e);
    return new ApiError(HttpStatus.CONFLICT.value(), e.getMessage());
  }

  /**
   * Exception handler for anything not covered above - Http status code of 500
   *
   * @param e the exception throw
   * @return a error response
   */
  @ExceptionHandler({APIException.class, Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ApiError handleApiException(Exception e) {
    LOGGER.error(e.getMessage(), e);
    return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
  }
}
