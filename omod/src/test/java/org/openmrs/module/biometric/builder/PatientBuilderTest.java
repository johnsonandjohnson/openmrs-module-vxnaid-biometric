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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.constants.BiometricTestConstants;
import org.openmrs.module.biometric.contract.RegisterRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class PatientBuilderTest {

  private static final String CREATE_PARTICIPANT_JSON = "create_participant.json";
  private static final String LOCATION_ATTRIBUTE = "LocationAttribute";
  private static final String OPEN_MRS_ID = "OpenMRS ID";
  private static final String PARTICIPANT_ID = "testparticipant_1";
  private static final String LOCATION_UUID = "9dd05e68-4d77-4b61-b2bf-436857f35aa0";

  private RegisterRequest request;
  private PatientIdentifierType patientIdentifierType;


  @Mock
  private PatientService patientService;

  @Mock
  private PersonService personService;

  @Mock
  private BiometricModUtil util;

  @Mock
  private LocationUtil locationUtil;

  @Mock
  private LocationService locationService;

  @InjectMocks
  private PatientBuilder patientBuilder;

  @Before
  public void setUp() throws IOException {
    PowerMockito.mockStatic(Context.class);
    String biographicData = ControllerTestHelper.loadFile(CREATE_PARTICIPANT_JSON);
    request = new ObjectMapper().readValue(biographicData, RegisterRequest.class);
    when(Context.getPatientService()).thenReturn(patientService);
    when(Context.getLocationService()).thenReturn(locationService);
  }

  @Test
  public void createFrom_shouldCreatePatientObject()
      throws EntityNotFoundException, ParseException {
    patientIdentifierType = new PatientIdentifierType();
    patientIdentifierType.setUuid(OpenmrsUtil.generateUid());
    patientIdentifierType.setName(OPEN_MRS_ID);

    Location location = new Location();
    location.setUuid(LOCATION_UUID);
    when(patientService.getPatientIdentifierTypeByName(OPEN_MRS_ID))
        .thenReturn(patientIdentifierType);
    when(locationUtil.getLocationUuid(request.getAttributes())).thenReturn(LOCATION_UUID);
    when(locationUtil.getLocationByUuid(LOCATION_UUID)).thenReturn(location);
    when(util.removeWhiteSpaces(BiometricTestConstants.PARTICIPANT_ID))
        .thenReturn(BiometricTestConstants.PARTICIPANT_ID);
    when(personService.getPersonAttributeTypeByName(anyString()))
        .thenReturn(new PersonAttributeType());
    Patient patient = patientBuilder.createFrom(request);
    assertNotNull(patient);
    assertThat(patient.getPatientIdentifier().getIdentifierType().getName(), equalTo(OPEN_MRS_ID));
    assertThat(patient.getPatientIdentifier().getIdentifier(),
        equalTo(BiometricTestConstants.PARTICIPANT_ID));
    assertThat(patient.getPatientIdentifier().getLocation().getUuid(), equalTo(LOCATION_UUID));

    verify(patientService, times(1)).getPatientIdentifierTypeByName(anyString());
    verify(personService, times(4)).getPersonAttributeTypeByName(anyString());
    verify(locationUtil, times(1)).getLocationUuid(request.getAttributes());
    verify(locationUtil, times(2)).getLocationByUuid(LOCATION_UUID);
  }

}
