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

/**
 * Match response contract used in response to match the participant's.
 */

public class MatchResponse {

  private String uuid;

  private String participantId;

  // OPENMRS, BOTH
  private String matchWith;

  private int matchingScore;

  private String gender;

  private String birthDate;

  private List<ParticipantAttribute> attributes;

  private Map<String, String> addresses;

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

  public String getMatchWith() {
    return matchWith;
  }

  public void setMatchWith(String matchWith) {
    this.matchWith = matchWith;
  }

  public int getMatchingScore() {
    return matchingScore;
  }

  public void setMatchingScore(int matchingScore) {
    this.matchingScore = matchingScore;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(String birthDate) {
    this.birthDate = birthDate;
  }

  public List<ParticipantAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<ParticipantAttribute> attributes) {
    this.attributes = attributes;
  }

  public Map<String, String> getAddresses() {
    return addresses;
  }

  public void setAddresses(Map<String, String> addresses) {
    this.addresses = addresses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MatchResponse)) {
      return false;
    }
    MatchResponse that = (MatchResponse) o;
    return Objects.equals(getUuid(), that.getUuid());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUuid());
  }
}
