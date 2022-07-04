/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service;

import java.util.List;
import java.util.Set;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;

/**
 * Defines methods for visits and encounters.
 */
public interface VisitSchedulerService {

  /**
   * Retrieve the visit details using the visit uuid.
   *
   * @param visitUuid unique visit id
   * @return visit details of a participant
   */
  Visit findVisitByVisitUuid(String visitUuid) throws EntityNotFoundException;

  /**
   * Retrieve the visit details using the person uuid.
   *
   * @param personUuid unique person uuid
   * @return visit details of a participant
   */
  List<Visit> findVisitByPersonUuid(String personUuid) throws EntityNotFoundException;

  /**
   * Creates or update a given visit.
   *
   * @param visitObj @see Visit
   * @return new or updated visit details of a participant
   */
  Visit createOrUpdateVisit(Visit visitObj);

  /**
   * Creates an encounter.
   *
   * @param updatedVisitObj @see Visit
   * @param encounterObj @see Encounter
   * @param obsSet set of observations
   * @return new encounter details of a participant
   */
  Encounter createEncounter(Visit updatedVisitObj, Encounter encounterObj, Set<Obs> obsSet);

  /**
   * Retrieves the visits by the specified list of visit uuids.
   *
   * @param uuids list of visit uuids
   * @return list of visits
   */
  List<Visit> findVisitsByUuids(Set<String> uuids);

  /**
   * to retrieve the un voided dosing visits for a patient and for the given dose number.
   *
   * @param patient patient
   * @param doseNumber dose number
   * @return visit details
   */
  Visit findNonVoidedDosingVisitByDoseNumber(Patient patient, String doseNumber);

}
