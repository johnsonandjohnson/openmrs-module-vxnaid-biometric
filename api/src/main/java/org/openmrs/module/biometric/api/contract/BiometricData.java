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

import java.util.Date;

/**
 * Participant details used for sync templates.
 */
public class BiometricData {

  private String id;

  private String participantUuid;

  private String template;

  private String device;

  private String country;

  private String siteId;

  private Date creationDate;

  private Date modificationDate;

  private boolean voided;

  /**
   * Constructor.
   *
   * @param id participant identifier
   * @param participantUuid participant uuid
   * @param template biometric template
   * @param modificationDate last modification date
   * @param voided flag for participant status
   */
  public BiometricData(String id, String participantUuid, String template, Date modificationDate,
      boolean voided) {
    this.id = id;
    this.participantUuid = participantUuid;
    this.template = template;
    this.modificationDate = modificationDate;
    this.voided = voided;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParticipantUuid() {
    return participantUuid;
  }

  public void setParticipantUuid(String participantUuid) {
    this.participantUuid = participantUuid;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(String device) {
    this.device = device;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public boolean isVoided() {
    return voided;
  }

  public void setVoided(boolean voided) {
    this.voided = voided;
  }
}
