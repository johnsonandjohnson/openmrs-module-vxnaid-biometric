/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.model;

import org.openmrs.module.biometric.api.util.SecurityUtil;

/**
 * This class is used for sync api call.
 *
 */
public abstract class SyncData {

  private String type;

  private String participantUuid;

  private Long dateModified;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = SecurityUtil.sanitizeOutput(type);
  }

  public String getParticipantUuid() {
    return participantUuid;
  }

  public void setParticipantUuid(String participantUuid) {
    this.participantUuid = SecurityUtil.sanitizeOutput(participantUuid);
  }

  public Long getDateModified() {
    return dateModified;
  }

  public void setDateModified(Long dateModified) {
    this.dateModified = dateModified;
  }
}
