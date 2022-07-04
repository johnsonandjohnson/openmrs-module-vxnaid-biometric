/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.web.helper;


import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;


public final class ControllerTestHelper {

  public static final String BASE_URL = "/rest/v1/biometric";

  private ControllerTestHelper() {
  }

  /**
   * This method parses the file and returns the file content in String format
   *
   * @param filename filename to be parsed
   * @return file content in String format
   * @throws IOException
   */

  public static String loadFile(String filename) throws IOException {
    try (InputStream in = ControllerTestHelper.class.getClassLoader()
        .getResourceAsStream(filename)) {
      return IOUtils.toString(in);
    }
  }

  /**
   *
   * @return
   */
  public static MockMultipartFile getTestTemplate() {
    return new MockMultipartFile("template", "test_template", "text/plain", "templa".getBytes());
  }

  public static String getConfiguratoins1() {
    return "{ \"personLanguages\": [" +
        "{ \"name\": \"English\" }," +
        "{ \"name\": \"Hindi\" }," +
        "{ \"name\": \"Kannada\" }" +
        "]}";
  }
}

