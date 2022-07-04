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
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.api.model.SyncData;

/**
 * Visit response contract used in response for visits.
 */
public class VisitResponse extends SyncData {

  private String visitUuid;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private String locationUuid;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private String startDatetime;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private String visitType;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private List<AttributeData> attributes;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private List<Observation> observations;

  public String getLocationUuid() {
    return locationUuid;
  }

  public void setLocationUuid(String locationUuid) {
    this.locationUuid = locationUuid;
  }

  public String getVisitUuid() {
    return visitUuid;
  }

  public void setVisitUuid(String visitUuid) {
    this.visitUuid = visitUuid;
  }

  public String getStartDatetime() {
    return startDatetime;
  }

  public void setStartDatetime(String startDatetime) {
    this.startDatetime = startDatetime;
  }

  public String getVisitType() {
    return visitType;
  }

  public void setVisitType(String visitType) {
    this.visitType = visitType;
  }

  public List<AttributeData> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<AttributeData> attributes) {
    this.attributes = attributes;
  }

  public List<Observation> getObservations() {
    return observations;
  }

  public void setObservations(List<Observation> observations) {
    this.observations = observations;
  }
}
