package org.openmrs.module.biometric.api.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public final class SecurityUtil {

  private SecurityUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Sanitizes the output string to prevent XSS attacks
   *
   * @param str string to be sanitized
   * @return sanitized output of the given string
   */
  public static String sanitizeOutput(String str) {
    if (null == str) {
      return null;
    }
    return Jsoup.clean(StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeEcmaScript(str)),
        Whitelist.basic());
  }

  /**
   * Generates MD5 hash of a specified string.
   *
   * @param str string for which the MD5 hash to be generated
   * @return MD5 hash code
   */
  @SuppressWarnings("findsecbugs:WEAK_MESSAGE_DIGEST_MD5")
  public static String getMd5Hash(String str) {
    // this is not a sensitive data, hence this warning can be ignored
    return DigestUtils.md5Hex(str);
  }
}
