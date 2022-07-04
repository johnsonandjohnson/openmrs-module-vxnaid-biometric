/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.contract;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openmrs.module.biometric.api.contract.Gender;
import org.openmrs.module.biometric.api.model.AttributeData;

/**
 * Match response contract used in response to match the participant's.
 */

public class ParticipantMatchResponse {

  private String uuid;

  private String participantId;

  private Gender gender;

  private String birthDate;

  // OPENMRS, BOTH
  private String matchWith;

  private int matchingScore;

  private Map<String, String> addresses;

  private List<AttributeData> attributes;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public int getMatchingScore() {
    return matchingScore;
  }

  public void setMatchingScore(int matchingScore) {
    this.matchingScore = matchingScore;
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

  public String getMatchWith() {
    return matchWith;
  }

  public void setMatchWith(String matchWith) {
    this.matchWith = matchWith;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParticipantMatchResponse)) {
      return false;
    }
    ParticipantMatchResponse that = (ParticipantMatchResponse) o;
    return Objects.equals(getUuid(), that.getUuid());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUuid());
  }
}
