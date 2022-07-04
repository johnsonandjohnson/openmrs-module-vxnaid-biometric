/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.builder;

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.SYNC_DELETE;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.SYNC_UPDATE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.contract.NewVisitResponse;
import org.openmrs.module.biometric.contract.Observation;
import org.openmrs.module.biometric.contract.VisitResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.SanitizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visit Response Builder.
 */
@Component
public class VisitResponseBuilder {

  private static final String RFC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  @Autowired
  private BiometricModUtil util;

  /**
   * To create a visit response object from Visit entity.
   *
   * @return visit response
   */
  public NewVisitResponse createFrom(Visit visit) {
    NewVisitResponse newVisitResponse = new NewVisitResponse();
    newVisitResponse.setVisitUuid(visit.getUuid());
    return newVisitResponse;
  }

  /**
   * To create a list of visit response object from multiple Visit entities.
   *
   * @return list of visit response
   */
  public List<VisitResponse> createFrom(List<Visit> visits) {
    List<VisitResponse> responses = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    List<Visit> modifiedVisits = util.getOldestEncounterInVisit(visits);
    for (Visit visit : modifiedVisits) {
      VisitResponse visitResponse = new VisitResponse();
      visitResponse.setVisitUuid(SanitizeUtil.sanitizeOutput(visit.getUuid()));
      visitResponse.setParticipantUuid(SanitizeUtil.sanitizeOutput(visit.getPatient().getUuid()));
      if (null == visit.getLocation()) {
        continue;
      }
      if (Boolean.TRUE.equals(visit.getVoided())) {
        visitResponse.setType(SYNC_DELETE);
      } else {
        visitResponse.setType(SYNC_UPDATE);
      }

      Date lastModifiedDate =
          null == visit.getDateChanged() ? visit.getDateCreated() : visit.getDateChanged();
      visitResponse.setDateModified(lastModifiedDate.getTime());

      if (!Boolean.TRUE.equals(visit.getVoided())) {
        visitResponse.setLocationUuid(SanitizeUtil.sanitizeOutput(visit.getLocation().getUuid()));
        visitResponse
            .setStartDatetime(new SimpleDateFormat(RFC_FORMAT).format(visit.getStartDatetime()));
        visitResponse.setVisitType(SanitizeUtil.sanitizeOutput(visit.getVisitType().getName()));
        visitResponse.setAttributes(getAttributes(visit));
        visitResponse.setObservations(getObservations(visit));
      }
      responses.add(visitResponse);
    }
    return responses;
  }

  private List<AttributeData> getAttributes(Visit visit) {
    Collection<VisitAttribute> attributes = visit.getActiveAttributes();
    List<AttributeData> attributeList = new ArrayList<>(7);
    for (VisitAttribute visitAttribute : attributes) {
      AttributeData attribute = new AttributeData();
      attribute
          .setType(SanitizeUtil.sanitizeOutput(visitAttribute.getAttributeType().getName()));
      attribute.setValue(SanitizeUtil.sanitizeOutput(visitAttribute.getValueReference()));
      attributeList.add(attribute);
    }
    return attributeList;
  }

  private List<Observation> getObservations(Visit visit) {
    List<Observation> observations = new ArrayList<>();
    List<Encounter> encounters = visit.getNonVoidedEncounters();
    for (Encounter encounter : encounters) {
      Set<Obs> obsSet = encounter.getAllObs(false);
      for (Obs obs : obsSet) {
        Observation observation = new Observation();
        observation.setDatetime(
            new SimpleDateFormat(RFC_FORMAT).format(encounter.getEncounterDatetime()));
        observation.setName(SanitizeUtil.sanitizeOutput(obs.getConcept().getName().getName()));
        observation.setValue(SanitizeUtil.sanitizeOutput(obs.getValueText()));
        observations.add(observation);
      }
    }
    return observations;
  }
}
