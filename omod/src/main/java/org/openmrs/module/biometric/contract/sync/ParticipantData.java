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

import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.biometric.api.model.AttributeData;

/**
 * ParticipantData class.
 *
 */
public class ParticipantData {

  private String type;

  private String participantUuid;

  private Long dateModified;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private String participantId;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private Gender gender;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private String birthDate;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private Map<String, String> addresses;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private List<AttributeData> attributes;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

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

  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public String getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(String birthDate) {
    this.birthDate = birthDate;
  }

  public Map<String, String> getAddresses() {
    return addresses;
  }

  public void setAddresses(Map<String, String> addresses) {
    this.addresses = addresses;
  }

  public List<AttributeData> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<AttributeData> attributes) {
    this.attributes = attributes;
  }
}
