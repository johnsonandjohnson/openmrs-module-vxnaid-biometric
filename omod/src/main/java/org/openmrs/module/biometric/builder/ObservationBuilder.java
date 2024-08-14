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

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.contract.Observation;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Observation Builder.
 */
@Component
public class ObservationBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ObservationBuilder.class);

  @Autowired
  private BiometricModUtil util;

  /**
   * To create set of observations from visit request and person.
   *
   * @param request @see VisitRequest
   * @param person  @see Person
   * @return set of observations
   * @throws ParseException when it was not possible to parse an Observation value
   */
  public Set<Obs> createFrom(VisitRequest request, Person person) throws ParseException, EntityNotFoundException {
    Set<Obs> obsSet = new HashSet<>();
    if (null != request.getObservations() && !request.getObservations().isEmpty()) {
      LOGGER.info("No. of observations : {}", request.getObservations().size());

      for (Observation observation : request.getObservations()) {
        if (StringUtils.isBlank(observation.getValue())) {
          continue;
        }

        Concept concept = Context.getConceptService().getConcept(observation.getName());
        if (null == concept) {
          LOGGER.warn("Concept with name {} does not exist. Observation will not be saved!", observation.getName());
        } else {
          Obs obs = new Obs();
          obs.setConcept(concept);
          obs.setPerson(person);
          obs.setObsDatetime(util.convertIsoStringToDate(request.getStartDatetime()));
          obs.setValueAsString(observation.getValue());

          obsSet.add(obs);
        }
      }
    }
    return obsSet;
  }
}
