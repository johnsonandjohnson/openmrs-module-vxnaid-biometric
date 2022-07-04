/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service.impl;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Criteria;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.util.TestUtil;

/**
 * Visit Scheduler Service Tests
 */
@RunWith(MockitoJUnitRunner.class)
public class VisitSchedulerServiceTest {

  private static final String VISIT_UUID = "shr05e68-89lo-4b61-b2bf-visit7f35aa0";
  private static final String INVALID_VISIT_UUID = "abc05e68-89lo-4b61-invl-visit7f35aa0";

  @Mock
  private DbSessionFactory sessionFactory;

  @Mock
  private DbSession dbSession;

  @Mock
  private Criteria criteria;

  private VisitSchedulerServiceImpl visitSchedulerService;

  @Mock
  private VisitService visitService;

  @Mock
  private EncounterService encounterService;

  @Mock
  private PatientService patientService;

  private Visit visitObj;
  private Visit responseVisit;
  private Person person;
  private Patient patient;

  @Before
  public void setUp() {
    visitSchedulerService = new VisitSchedulerServiceImpl(visitService, encounterService,patientService);
    visitObj = TestUtil.createVisit();
  }

  @Test
  public void createOrUpdateVisit_shouldCreateVisit() {
    //Given
    responseVisit = new Visit();
    responseVisit.setUuid(VISIT_UUID);
    when(visitService.saveVisit(visitObj)).thenReturn(responseVisit);
    //When
    Visit newVisit = visitSchedulerService.createOrUpdateVisit(visitObj);
    //Then
    assertNotNull(newVisit);
    assertEquals(VISIT_UUID, newVisit.getUuid());
    verifyInteractions();
  }

  @Test
  public void createOrUpdateVisit_shouldNotCreateVisitIfPatientNotFound() {
    //Given
    responseVisit = new Visit();
    visitObj.setPatient(null);
    responseVisit.setUuid(null);
    when(visitService.saveVisit(visitObj)).thenReturn(responseVisit);
    //When
    Visit newVisit = visitSchedulerService.createOrUpdateVisit(visitObj);
    //Then
    assertNotNull(newVisit);
    assertNull(newVisit.getUuid());
    verifyInteractions();
  }

  @Test
  public void findVisitByVisitUuid_shouldFindVisitIfValidVisitUuidIsPassed()
      throws EntityNotFoundException {
    //Given
    responseVisit = new Visit();
    responseVisit.setUuid(VISIT_UUID);
    when(visitService.getVisitByUuid(visitObj.getUuid())).thenReturn(responseVisit);
    //When
    Visit newVisit = visitSchedulerService.findVisitByVisitUuid(visitObj.getUuid());
    //Then
    assertNotNull(newVisit);
    assertEquals(VISIT_UUID, newVisit.getUuid());
    verify(visitService, times(1)).getVisitByUuid(visitObj.getUuid());
  }

  @Test
  public void findVisitByVisitUuid_shouldNotFindVisitIfInvalidVisitUuidIsPassed()
      throws EntityNotFoundException {
    //Given
    Visit newVisit = null;
    when(visitService.getVisitByUuid(INVALID_VISIT_UUID)).thenReturn(responseVisit);
    //When
    try {
      newVisit = visitSchedulerService.findVisitByVisitUuid(INVALID_VISIT_UUID);
      Assert.fail("should throw EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      //Then
      assertNull(newVisit);
      verify(visitService, times(1)).getVisitByUuid(INVALID_VISIT_UUID);
    }

  }

  @Test
  public void findVisitByPersonUuid_shouldFindDosingVisitIfValidPersonUuidIsPassed()
      throws EntityNotFoundException {
    //Given
    List<Visit> visits = new ArrayList<>();
    person = TestUtil.createPerson();
    patient = TestUtil.createPatient(person);
    visits.add(TestUtil.createAnotherVisit());
    when(patientService.getPatientByUuid(person.getUuid())).thenReturn(patient);
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(Visit.class)).thenReturn(criteria);
    when(criteria.list()).thenReturn(visits);
    visitSchedulerService.setSessionFactory(sessionFactory);
    //When
    List<Visit> newVisits = visitSchedulerService.findVisitByPersonUuid(person.getUuid());
    //Then
    assertNotNull(newVisits);
    assertEquals(1, newVisits.size());
    assertEquals(1, newVisits.get(0).getVisitId().intValue());
    assertEquals("Dosing", newVisits.get(0).getVisitType().getName());
    verify(patientService, times(1)).getPatientByUuid(person.getUuid());
    verify(sessionFactory, times(1)).getCurrentSession();
    verify(dbSession, times(1)).createCriteria(Visit.class);
    verify(criteria, times(1)).list();
  }

  @Test
  public void findVisitByPersonUuid_shouldNotFindDosingVisitIfEmptyPersonUuidIsPassed() {
    //Given
    String personUuid = "";
    when(patientService.getPatientByUuid(personUuid)).thenReturn(patient);
    try {
      //When
      visitSchedulerService.findVisitByPersonUuid(personUuid);
      fail("Participant not found with UUID: " + personUuid);
    } catch (EntityNotFoundException e) {
      //Then
      verify(patientService, times(1)).getPatientByUuid(personUuid);
      verifyZeroInteractions(sessionFactory);
      verifyZeroInteractions(dbSession);
      verifyZeroInteractions(criteria);
    }
  }

  @Test
  public void findVisitByPersonUuid_shouldNotFindDosingVisit() throws EntityNotFoundException {
    //Given
    List<Visit> visits = null;
    person = TestUtil.createPerson();
    patient = TestUtil.createPatient(person);
    when(patientService.getPatientByUuid(person.getUuid())).thenReturn(patient);
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(Visit.class)).thenReturn(criteria);
    when(criteria.list()).thenReturn(visits);
    visitSchedulerService.setSessionFactory(sessionFactory);
    //When
    List<Visit> newVisits = visitSchedulerService.findVisitByPersonUuid(person.getUuid());
    //Then
    assertNotNull(newVisits);
    assertTrue(newVisits.isEmpty());
    verify(patientService, times(1)).getPatientByUuid(person.getUuid());
    verify(sessionFactory, times(1)).getCurrentSession();
    verify(dbSession, times(1)).createCriteria(Visit.class);
    verify(criteria, times(1)).list();
  }

  //Encounter test cases
  @Test
  public void createEncounter_shouldCreateANewEncounter() throws ParseException {
    //Given
    Visit visit = TestUtil.createVisit();
    Visit updatedVisitObj = TestUtil.updateVisitStatusAttriute(visit);
    Visit updatedVisit = TestUtil.updateVisitStatusAttriute(visit);
    updatedVisit.setDateChanged(new Date());
    when(visitService.saveVisit(updatedVisitObj)).thenReturn(updatedVisit);
    Encounter encounter = TestUtil.createEncounter();
    when(encounterService.saveEncounter(encounter)).thenReturn(encounter);
    Encounter encounterWithObs = TestUtil.updateEncounterWithObservations();
    Encounter newEncounterWithObs = TestUtil.updateEncounterWithObservations();
    newEncounterWithObs.setId(1);
    when(encounterService.saveEncounter(encounterWithObs)).thenReturn(newEncounterWithObs);
    Obs obs = new Obs();
    obs.setEncounter(newEncounterWithObs);
    Set<Obs> obsSet = new HashSet<>();
    obsSet.add(obs);
    Encounter newEncounter = visitSchedulerService
        .createEncounter(updatedVisitObj, encounter, obsSet);
    //Then
    assertNotNull(newEncounter);
    verify(visitService, times(1)).saveVisit(updatedVisitObj);
    verify(encounterService, times(2)).saveEncounter(encounter);
  }

  @Test
  public void findVisitsByUuids_shouldReturnAllVisitsWithUuids() {
    Visit visit = TestUtil.createVisit();
    Set<String> uuids = new HashSet<>();
    uuids.add("06379cf7-e246-41a3-adff-fa9d68b28926");
    when(visitService.getVisitByUuid(anyString())).thenReturn(visit);
    List<Visit> visits = visitSchedulerService.findVisitsByUuids(uuids);
    verify(visitService, times(uuids.size())).getVisitByUuid(anyString());
    assertEquals(uuids.size(), visits.size());
  }

  @Test
  public void findVisitsByUuids_shouldReturnEmptySetWhenThereIsNoVisitWithUuid() {
    Visit visit = TestUtil.createVisit();
    Set<String> uuids = new HashSet<>();
    uuids.add("06379cf7-e246-41a3-adff-fa9d68b28926");
    when(visitService.getVisitByUuid(anyString())).thenReturn(null);
    List<Visit> visits = visitSchedulerService.findVisitsByUuids(uuids);
    verify(visitService, times(uuids.size())).getVisitByUuid(anyString());
    assertEquals(0, visits.size());
  }


  @Test
  public void findVisitsByUuids_ShouldReturnVisitsWithUuids() throws Exception {
    Set<String> uuids = Collections.singleton("bde22f30-9b45-642c-92d2-6180399ceeb1");
    when(visitService.getVisitByUuid(anyString())).thenReturn(visitObj);
    List<Visit> visitList = new ArrayList<>();
    try {
      visitList = visitSchedulerService.findVisitsByUuids(uuids);
    } finally {
      verify(visitService, times(1)).getVisitByUuid(anyString());
      assertThat(visitList, hasSize(1));
    }
  }

   /* @Test
    public void findVisitsByUuids_shouldReturnNullWhenNullVisitWithUuid() {
        Visit visit = TestUtil.createVisit();
        List<String> uuids = new ArrayList<>();
    }*/

  private void verifyInteractions() {
    verify(visitService, times(1)).saveVisit(visitObj);
  }

}
