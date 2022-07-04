/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.contract;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This class is used for building the response for sync config api calls.
 *
 */
public class SyncConfigResponse {

  private String name;

  @JsonProperty("hash")
  private String md5hash;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMd5hash() {
    return md5hash;
  }

  public void setMd5hash(String md5hash) {
    this.md5hash = md5hash;
  }
}
