/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.contract.sync;

import org.openmrs.module.biometric.util.SanitizeUtil;

public class SyncScope {

  private String siteUuid;

  private String cluster;

  private String country;

  public String getSiteUuid() {
    return siteUuid;
  }

  public void setSiteUuid(String siteUuid) {
    this.siteUuid = SanitizeUtil.sanitizeInputString(siteUuid);
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = SanitizeUtil.sanitizeInputString(cluster);
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = SanitizeUtil.sanitizeInputString(country);
  }
}
