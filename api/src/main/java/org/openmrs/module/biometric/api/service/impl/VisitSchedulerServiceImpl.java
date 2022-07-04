/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service.impl;

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.DOSE_NUMBER_ATTRIBUTE_TYPE_NAME;
import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.DOSING_VISIT_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.VisitSchedulerService;
import org.springframework.transaction.annotation.Transactional;

/**
 * The implementation class for VisitSchedulerService.
 *
 */
public class VisitSchedulerServiceImpl implements VisitSchedulerService {

  private static final String VISIT_NOT_FOUND = "Visit not found";
  private static final String PERSON_NOT_FOUND = "Participant not found";

  private DbSessionFactory sessionFactory;
  private org.openmrs.api.VisitService visitService;
  private EncounterService encounterService;
  private PatientService patientService;

  public VisitSchedulerServiceImpl(
      VisitService visitService, EncounterService encounterService, PatientService patientService) {
    this.visitService = visitService;
    this.encounterService = encounterService;
    this.patientService = patientService;
  }

  @Transactional
  @Override
  public Visit createOrUpdateVisit(Visit visitObj) {
    return visitService.saveVisit(visitObj);
  }

  @Transactional(readOnly = true)
  @Override
  public Visit findVisitByVisitUuid(String visitUuid) throws EntityNotFoundException {
    Visit visit = visitService.getVisitByUuid(visitUuid);
    if (null == visit) {
      throw new EntityNotFoundException(VISIT_NOT_FOUND);
    }
    return visit;
  }

  @Transactional(readOnly = true)
  @Override
  public List<Visit> findVisitByPersonUuid(String personUuid) throws EntityNotFoundException {
    Patient patient = patientService.getPatientByUuid(personUuid);
    if (null == patient) {
      throw new EntityNotFoundException(PERSON_NOT_FOUND);
    }
    List<Visit> visits = buildDosingVisitCriteria(personUuid).list();
    if (null == visits || visits.isEmpty()) {
      return Collections.emptyList();
    }
    return visits;
  }

  @Transactional(readOnly = true)
  @Override
  public List<Visit> findVisitsByUuids(Set<String> uuids) {
    List<Visit> visits = new ArrayList<>();
    for (String uuid : uuids) {
      Visit visit = visitService.getVisitByUuid(uuid);
      if (null != visit) {
        visits.add(visit);
      }
    }
    return visits;
  }

  @Override
  public final Visit findNonVoidedDosingVisitByDoseNumber(Patient patient, String doseNumber) {

    List<Visit> visits = visitService.getVisitsByPatient(patient, false, false);

    for (Visit visit : visits) {
      if (DOSING_VISIT_TYPE.equalsIgnoreCase(visit.getVisitType().getName())) {
        Set<VisitAttribute> attributes = visit.getAttributes();
        for (VisitAttribute attribute : attributes) {
          if (attribute
              .getAttributeType()
              .getName()
              .equalsIgnoreCase(DOSE_NUMBER_ATTRIBUTE_TYPE_NAME)
              && attribute.getValue().toString().equalsIgnoreCase(doseNumber)) {
            return visit;
          }
        }
      }
    }
    return null;
  }

  @Transactional
  @Override
  public Encounter createEncounter(Visit updatedVisitObj, Encounter encounterObj, Set<Obs> obsSet) {
    // update the visit status to occurred
    Visit updatedVisit = createOrUpdateVisit(updatedVisitObj);
    encounterObj.setVisit(updatedVisit);
    Encounter newEncounter = encounterService.saveEncounter(encounterObj);

    // update each observation with the new encounter
    Set<Obs> observations = new HashSet<>(5);
    for (Obs obs : obsSet) {
      obs.setEncounter(newEncounter);
      observations.add(obs);
    }
    // Update the encounter with the observations like vaccine name, barcode etc.
    newEncounter.setObs(observations);
    return encounterService.saveEncounter(newEncounter);
  }

  public void setSessionFactory(DbSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Criteria buildDosingVisitCriteria(String personUuid) {
    final Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
    criteria.createAlias("patient", "person", JoinType.INNER_JOIN);
    criteria.createAlias("visitType", "visitType", JoinType.INNER_JOIN);
    criteria.add(Restrictions.eq("person.uuid", personUuid));
    criteria.add(Restrictions.eq("visitType.name", DOSING_VISIT_TYPE));

    return criteria;
  }
}
