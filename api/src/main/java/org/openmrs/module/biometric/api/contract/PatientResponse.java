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

import org.openmrs.module.biometric.api.model.PatientRecord;

/**
 * This class is used for building the participant details response.
 *
 */
public class PatientResponse extends PatientRecord {

  private String participantUuid;

  private Long dateModified;

  public String getParticipantUuid() {
    return participantUuid;
  }

  public void setParticipantUuid(String participantUuid) {
    this.participantUuid = participantUuid;
  }

  public Long getDateModified() {
    return dateModified;
  }

  public void setDateModified(Long dateModified) {
    this.dateModified = dateModified;
  }

}
