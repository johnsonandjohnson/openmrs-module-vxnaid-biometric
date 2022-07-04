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
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Encounter Builder.
 */
@Component
public class EncounterBuilder {

  private static final String ENCOUNTER_TYPE_DOSING = "Dosing";
  private static final String ENCOUNTER_TYPE_FOLLOWUP = "Follow up";
  @Autowired
  private BiometricModUtil util;

  /**
   * Creates Encounter entity from @code (VisitRequest, Visit).
   *
   * @param request Visit Request
   * @param visit   @see Visit
   * @return Encounter @see org.openmrs.Encounter
   */
  public Encounter createFrom(VisitRequest request, Visit visit) throws ParseException {
    Encounter encounter = new Encounter();
    String visitType = visit.getVisitType().getName();
    String encounterType = ENCOUNTER_TYPE_DOSING;
    if (!visitType.equals(ENCOUNTER_TYPE_DOSING)) {
      encounterType = ENCOUNTER_TYPE_FOLLOWUP;
    }
    encounter.setPatient(visit.getPatient());
    encounter.setEncounterType(Context.getEncounterService().getEncounterType(encounterType));
    encounter.setEncounterDatetime(util.convertIsoStringToDate(request.getStartDatetime()));
    Location location = Context.getLocationService().getLocationByUuid(request.getLocationUuid());
    encounter.setLocation(location);
    encounter.setVisit(visit);
    return encounter;
  }
}
