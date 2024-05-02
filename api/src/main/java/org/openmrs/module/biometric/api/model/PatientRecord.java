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

import java.util.List;
import java.util.Map;
import org.openmrs.module.biometric.api.contract.Gender;

/**
 * This class is used for building the participant details.
 *
 */
public class PatientRecord {

  private String participantId;

  private Gender gender;

  private String birthDate;

  private String nin;

  public String getNin() {
    return nin;
  }

  public void setNin(String nin) {
    this.nin = nin;
  }

  private Map<String, String> addresses;

  private List<AttributeData> attributes;

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
