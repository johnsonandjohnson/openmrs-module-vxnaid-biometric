/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.biometric.api.contract;

public class IdentifierResponse {

  private String participantUuid;

  private String identifierTypeName;

  private String identifierValue;

  public IdentifierResponse(
      String participantUuid, String identifierTypeName, String identifierValue) {
    this.participantUuid = participantUuid;
    this.identifierTypeName = identifierTypeName;
    this.identifierValue = identifierValue;
  }

  public String getParticipantUuid() {
    return participantUuid;
  }

  public void setParticipantUuid(String participantUuid) {
    this.participantUuid = participantUuid;
  }

  public String getIdentifierTypeName() {
    return identifierTypeName;
  }

  public void setIdentifierTypeName(String identifierTypeName) {
    this.identifierTypeName = identifierTypeName;
  }

  public String getIdentifierValue() {
    return identifierValue;
  }

  public void setIdentifierValue(String identifierValue) {
    this.identifierValue = identifierValue;
  }
}
