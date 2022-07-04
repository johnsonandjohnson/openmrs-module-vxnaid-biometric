/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

import java.util.HashSet;
import java.util.Set;

public final class SanitizeUtil {

  private static final String SINGLE_QUOTE = "'";
  private static final String DOUBLE_SINGLE_QUOTE = "''";

  private SanitizeUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Remove escape characters like Html/Js scripts from input if present
   *
   * @param str Input string
   * @return sanitizeInputString string
   */
  public static String sanitizeInputString(String str) {
    if (null == str) {
      return null;
    }
    return Jsoup.clean(StringEscapeUtils.escapeHtml4(
        StringEscapeUtils.escapeEcmaScript(StringUtils.replace(str, SINGLE_QUOTE, DOUBLE_SINGLE_QUOTE))), Whitelist.basic());
  }

  public static String sanitizeOutput(String str) {
    if (null == str) {
      return null;
    }
    return Parser.unescapeEntities(Jsoup.clean(StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeEcmaScript(str)),
        Whitelist.basic()), false);
  }

  public static Set<String> sanitizeStringList(Set<String> params) {
    Set<String> cleanList = new HashSet<>(10);
    for (String uuid : params) {
      cleanList.add(sanitizeInputString(uuid));
    }
    return cleanList;
  }
}
