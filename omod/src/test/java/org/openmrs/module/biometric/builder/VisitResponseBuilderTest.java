/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.common.BiometricTestUtil;
import org.openmrs.module.biometric.contract.NewVisitResponse;
import org.openmrs.module.biometric.contract.VisitResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class VisitResponseBuilderTest {

  @Mock
  private AdministrationService administrationService;

  @Mock
  private BiometricModUtil util;

  @InjectMocks
  private VisitResponseBuilder visitResponseBuilder;

  private Visit visit;

  private Encounter encounter;

  @Before
  public void setUp() {
    PowerMockito.mockStatic(Context.class);
    visit = BiometricTestUtil.createVisit();
  }

  @Test
  public void createFrom_shouldCreateNewVisitResponseForSingleVisit() {
    //When
    NewVisitResponse newVisitResponse = visitResponseBuilder.createFrom(visit);
    //Then
    assertNotNull(newVisitResponse);
    assertEquals(newVisitResponse.getVisitUuid(), visit.getUuid());
  }

  @Test
  public void createFrom_shouldNotCreateNewVisitResponseForSingleVisitIfVisitUuidIsNotPassed() {
    //When
    visit.setUuid(null);
    NewVisitResponse newVisitResponse = visitResponseBuilder.createFrom(visit);
    //Then
    assertNotNull(newVisitResponse);
    assertNull(newVisitResponse.getVisitUuid());
  }

  @Test
  public void createFrom_shouldCreateVisitResponseForMultipleVisits() {
    //Given
    List<Visit> visits = new ArrayList<>();
    VisitType visitType = new VisitType("Other", "");
    Set<Encounter> encounters = new HashSet<>();
    visit.setVisitType(visitType);
    encounter = BiometricTestUtil.createEncounter();
    encounters.add(encounter);
    visit.setEncounters(encounters);
    visits.add(visit);

    //When
    when(util.getOldestEncounterInVisit(anyListOf(Visit.class))).thenReturn(visits);
    List<VisitResponse> visitResponses = visitResponseBuilder.createFrom(visits);
    //Then
    assertNotNull(visitResponses);
    assertFalse(visitResponses.get(0).getVisitUuid().isEmpty());
    assertEquals(visitType.getName(), visitResponses.get(0).getVisitType());
  }

}
