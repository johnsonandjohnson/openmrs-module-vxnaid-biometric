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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.common.BiometricTestUtil;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class EncounterBuilderTest {

  private static final String CREATE_VISIT_JSON = "create_visit.json";
  private static final String ENCOUNTER_TYPE_DOSING = "Dosing";
  private static final String ENCOUNTER_TYPE_FOLLOWUP = "Follow up";
  private static final String LOCATION_UUID = "42655fae-3b09-4b15-bb4b-296311546be4";

  @Mock
  private LocationService locationService;

  @Mock
  private EncounterService encounterService;

  @Mock
  private BiometricModUtil util;

  @InjectMocks
  private EncounterBuilder encounterBuilder;

  private VisitRequest request;
  private Visit visit;
  private Location location;

  @Before
  public void setUp() throws IOException {
    PowerMockito.mockStatic(Context.class);

    String visitData = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    request = new ObjectMapper().readValue(visitData, VisitRequest.class);
    visit = BiometricTestUtil.createVisit();
    location = new Location();
    location.setUuid(LOCATION_UUID);
    when(Context.getLocationService()).thenReturn(locationService);
    when(locationService.getLocationByUuid(request.getLocationUuid())).thenReturn(location);
    when(Context.getEncounterService()).thenReturn(encounterService);
  }

  @Test
  public void createFrom_shouldCreateEncounterTypeDosing() throws ParseException {
    //Given
    VisitType visitType = new VisitType("Dosing", "");
    visit.setVisitType(visitType);
    when(encounterService.getEncounterType(ENCOUNTER_TYPE_DOSING)).thenReturn(new EncounterType());
    //When
    Encounter encounter = encounterBuilder.createFrom(request, visit);
    //Then
    assertNotNull(encounter);
    assertEquals(visit.getPatient().getUuid(), encounter.getPatient().getUuid());
    assertEquals(request.getLocationUuid(), encounter.getLocation().getUuid());
    verifyInteractions();
    verify(encounterService, times(1)).getEncounterType(ENCOUNTER_TYPE_DOSING);
  }

  @Test
  public void createFrom_shouldCreateEncounterTypeFollowUp() throws ParseException {
    //Given
    VisitType visitType = new VisitType("Other", "");
    visit.setVisitType(visitType);
    when(encounterService.getEncounterType(ENCOUNTER_TYPE_FOLLOWUP))
        .thenReturn(new EncounterType());
    //When
    Encounter encounter = encounterBuilder.createFrom(request, visit);
    //Then
    assertNotNull(encounter);
    verifyInteractions();
    verify(encounterService, times(1)).getEncounterType(ENCOUNTER_TYPE_FOLLOWUP);
  }

  private void verifyInteractions() {
    verify(locationService, times(1)).getLocationByUuid(request.getLocationUuid());
  }

}
