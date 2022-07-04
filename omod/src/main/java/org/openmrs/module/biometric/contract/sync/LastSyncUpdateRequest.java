/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.contract.sync;

/**
 * LastSyncUpdateRequest class.
 *
 */
public class LastSyncUpdateRequest {

  private String siteUuid;

  private Long dateSyncCompleted;

  public String getSiteUuid() {
    return siteUuid;
  }

  public void setSiteUuid(String siteUuid) {
    this.siteUuid = siteUuid;
  }

  public Long getDateSyncCompleted() {
    return dateSyncCompleted;
  }

  public void setDateSyncCompleted(Long dateSyncCompleted) {
    this.dateSyncCompleted = dateSyncCompleted;
  }
}
