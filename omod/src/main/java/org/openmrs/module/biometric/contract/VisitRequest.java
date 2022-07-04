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
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Visit request contract used in request for creating visits and encounters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitRequest {

  private String visitUuid;
  private String participantUuid;
  private String visitType;
  private String startDatetime;
  private String locationUuid;
  private List<ParticipantAttribute> attributes;
  private List<Observation> observations;

  public String getVisitUuid() {
    return visitUuid;
  }

  public void setVisitUuid(String visitUuid) {
    this.visitUuid = visitUuid;
  }

  public String getParticipantUuid() {
    return participantUuid;
  }

  public void setParticipantUuid(String participantUuid) {
    this.participantUuid = participantUuid;
  }

  public String getVisitType() {
    return visitType;
  }

  public void setVisitType(String visitType) {
    this.visitType = visitType;
  }

  public String getStartDatetime() {
    return startDatetime;
  }

  public void setStartDatetime(String startDatetime) {
    this.startDatetime = startDatetime;
  }

  public String getLocationUuid() {
    return locationUuid;
  }

  public void setLocationUuid(String locationUuid) {
    this.locationUuid = locationUuid;
  }

  public List<ParticipantAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<ParticipantAttribute> attributes) {
    this.attributes = attributes;
  }

  public List<Observation> getObservations() {
    return observations;
  }

  public void setObservations(List<Observation> observations) {
    this.observations = observations;
  }
}

