/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.biometric.contract;

import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.module.biometric.api.model.AttributeData;

/** Register request contract used in request to register participant. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

  private String participantUuid;

  private String participantId;

  private String gender;

  private Boolean isBirthDateEstimated;

  private String birthdate;

  private String registrationDate;

  private int age;

  private String nin;

  private List<Address> addresses;

  private List<AttributeData> attributes;

  private String updateDate;

  /** base64 encoded format of the participant's image */
  private String image;

  public String getParticipantUuid() {
    return participantUuid;
  }

  public void setParticipantUuid(String participantUuid) {
    this.participantUuid = participantUuid;
  }

  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public Boolean getIsBirthDateEstimated() {
    return isBirthDateEstimated;
  }

  public void setIsBirthDateEstimated(Boolean isBirthDateEstimated) {
    this.isBirthDateEstimated = isBirthDateEstimated;
  }

  public String getBirthdate() {
    return birthdate;
  }

  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  public String getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(String registrationDate) {
    this.registrationDate = registrationDate;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getNin() {
    return nin;
  }

  public void setNin(String nin) {
    this.nin = nin;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public List<AttributeData> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<AttributeData> attributes) {
    this.attributes = attributes;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(String updateDate) {
    this.updateDate = updateDate;
  }
}
