/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter intended for all /ws/sms calls that allows the user to authenticate via Basic
 * authentication. (It will not fail on invalid or missing credentials. We count on the API to throw
 * exceptions if an unauthenticated user tries to do something they are not allowed to do.)
 */
public class AuthorizationFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);
  private static final int BASIC_AUTH_BEGIN_INDEX = 6;

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig arg0) throws ServletException {
    LOGGER.info("Initializing Biometric Authorization filter");
  }

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    LOGGER.debug("Destroying Biometric Authorization filter");
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   * javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // skip if the session has timed out, we're already authenticated, or it's not an HTTP request
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;

      // If session is not valid
      if (!httpRequest.isRequestedSessionIdValid()) {
        sendError(response, HttpServletResponse.SC_FORBIDDEN, "Session timed out");
        return;
      }

      if (!Context.isAuthenticated()) {
        handleAuthentication(httpRequest, response);
      }
    }
    // continue with the filter chain in all circumstances
    chain.doFilter(request, response);
  }

  private void handleAuthentication(HttpServletRequest httpRequest, ServletResponse response)
      throws IOException {
    String basicAuth = httpRequest.getHeader("Authorization");

    if (StringUtils.isNotBlank(basicAuth)) {
      try {
        basicAuth = basicAuth.substring(BASIC_AUTH_BEGIN_INDEX); // remove the leading "Basic "
        String decoded = new String(Base64.decodeBase64(basicAuth), StandardCharsets.UTF_8);
        String[] userAndPass = decoded.split(":");
        Context.authenticate(userAndPass[0], userAndPass[1]);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("authenticated : {}", userAndPass[0]);
        }
      } catch (Exception ex) {
        // This filter never stops execution. If the user failed to
        // authenticate, that will be caught later.
        LOGGER.error(ex.getMessage());
        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
      }
    } else {
      // This sends 401 error if not authenticated
      sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
    }
  }

  private void sendError(ServletResponse response, final int errorCode, final String message)
      throws IOException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.sendError(errorCode, message);
  }
}
