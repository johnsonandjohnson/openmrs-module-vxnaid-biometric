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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class VisitRequestBuilderTest {

  private static final String CREATE_VISIT_JSON = "create_visit.json";
  private static final String VISIT_UUID = "create-new-visit-uuid";

  private VisitRequest request;

  @Mock
  private PatientService patientService;

  @Mock
  private LocationService locationService;

  @Mock
  private BiometricModUtil util;

  @Mock
  private LocationUtil locationUtil;

  @Mock
  private VisitService visitService;

  @InjectMocks
  private VisitRequestBuilder visitBuilder;

  private Patient patient;
  private VisitType visitType;
  private List<VisitType> visitTypes;
  private Location location;
  private List<VisitAttributeType> visitAttributeTypes;
  private VisitAttributeType visitAttributeType;

  @Before
  public void setUp() throws IOException {
    PowerMockito.mockStatic(Context.class);

    String visitData = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    request = new ObjectMapper().readValue(visitData, VisitRequest.class);
    patient = new Patient();
    visitType = new VisitType();
    visitTypes = new ArrayList<>();
    visitTypes.add(visitType);
    visitAttributeType = new VisitAttributeType();
    visitAttributeType.setName("Visit Status");
    visitAttributeTypes = new ArrayList<>();
    visitAttributeTypes.add(visitAttributeType);
    location = new Location();
    when(visitService.getVisitTypes(request.getVisitType())).thenReturn(visitTypes);
  }

  @Test
  public void createFrom_shouldCreateVisitObject() throws Exception {
    //Given
    when(patientService.getPatientByUuid(request.getParticipantUuid())).thenReturn(patient);
    when(visitService.getAllVisitAttributeTypes()).thenReturn(visitAttributeTypes);
    when(locationService.getLocationByUuid(request.getLocationUuid())).thenReturn(location);
    //When
    Visit newVisit = visitBuilder.createFrom(request);
    //Then
    assertNotNull(newVisit);
    verifyInteractions();
  }

  @Test
  public void createFrom_shouldCreateInCompleteVisitObjectIfInvalidParticipantUuidIsPassed()
      throws Exception {
    //Given
    when(patientService.getPatientByUuid(request.getParticipantUuid())).thenReturn(null);
    when(locationUtil.getLocationByUuid(request.getLocationUuid())).thenReturn(location);
    when(visitService.getAllVisitAttributeTypes()).thenReturn(visitAttributeTypes);
    //When
    Visit newVisit = visitBuilder.createFrom(request);
    //Then
    assertNotNull(newVisit);
    assertNull(newVisit.getPatient());
    verifyInteractions();
  }

  @Test(expected = EntityNotFoundException.class)
  public void createFrom_shouldThrowExceptionIfInvalidLocationUuidIsPassed() throws Exception {
    //Given
    when(patientService.getPatientByUuid(request.getParticipantUuid())).thenReturn(patient);
    when(locationService.getLocationByUuid(request.getLocationUuid())).thenReturn(null);

    //When
    Visit newVisit = visitBuilder.createFrom(request);
    //Then
    assertNotNull(newVisit);
    assertNull(newVisit.getLocation());
    verifyInteractions();
  }

  @Test
  public void createFrom_shouldUpdateVisitObject() throws Exception {
    //Given
    request.setVisitUuid(VISIT_UUID);
    when(patientService.getPatientByUuid(request.getParticipantUuid())).thenReturn(patient);
    when(locationService.getLocationByUuid(request.getLocationUuid())).thenReturn(location);
    when(visitService.getAllVisitAttributeTypes()).thenReturn(visitAttributeTypes);
    //When
    Visit newVisit = visitBuilder.createFrom(request);
    //Then
    assertNotNull(newVisit);
    verifyInteractions();
  }

  private void verifyInteractions() throws EntityNotFoundException {
    verify(patientService, times(1)).getPatientByUuid(request.getParticipantUuid());
    verify(visitService, times(1)).getVisitTypes(request.getVisitType());
    verify(visitService, times(1)).getAllVisitAttributeTypes();
    verify(locationUtil, times(1)).getLocationByUuid(request.getLocationUuid());
  }


}
