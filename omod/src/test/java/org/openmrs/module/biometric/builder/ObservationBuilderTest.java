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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.common.BiometricTestUtil;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.web.helper.ControllerTestHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class ObservationBuilderTest {

  private static final String CREATE_VISIT_JSON = "create_visit.json";
  private static final String HL7_ABBREVIATION_TEXT = "ST";
  private static final String CREATE_VISIT_INVALID_REQUEST_JSON = "create_visit_invalid_request.json";

  @Mock
  private ConceptService conceptService;

  @Mock
  private BiometricModUtil util;

  @InjectMocks
  private ObservationBuilder observationBuilder;

  private VisitRequest request;
  private Person person;

  @Before
  public void setUp() {
    PowerMockito.mockStatic(Context.class);

    person = BiometricTestUtil.createPerson();

    ConceptDatatype conceptDatatype = new ConceptDatatype();
    conceptDatatype.setHl7Abbreviation(HL7_ABBREVIATION_TEXT);

    Concept barcodeConcept = new Concept();
    barcodeConcept.setDatatype(conceptDatatype);

    Concept manufacturerConcept = new Concept();
    manufacturerConcept.setDatatype(conceptDatatype);

    when(Context.getConceptService()).thenReturn(conceptService);
    when(conceptService.getConcept("Barcode")).thenReturn(barcodeConcept);
    when(conceptService.getConcept("Vaccine Manufacturer")).thenReturn(manufacturerConcept);
  }

  @Test
  public void createFrom_shouldCreateSetOfObservationsIfValidRequestAndPersonIsPassed()
      throws ParseException, IOException, EntityNotFoundException {
    //Given
    String visitData = ControllerTestHelper.loadFile(CREATE_VISIT_JSON);
    request = new ObjectMapper().readValue(visitData, VisitRequest.class);

    //When
    Set<Obs> obsSet = observationBuilder.createFrom(request, person);
    //Then
    assertNotNull(obsSet);
    assertEquals(1, obsSet.size());
    verify(conceptService, times(1)).getConcept(Mockito.matches("Vaccine Manufacturer"));
  }

  @Test
  public void createFrom_shouldNotCreateSetOfObservationIfRequestHasNoObservations()
      throws ParseException, IOException, EntityNotFoundException {
    //Given
    String visitData = ControllerTestHelper.loadFile(CREATE_VISIT_INVALID_REQUEST_JSON);
    request = new ObjectMapper().readValue(visitData, VisitRequest.class);
    //When
    Set<Obs> obsSet = observationBuilder.createFrom(request, person);
    //Then
    assertNotNull(obsSet);
    assertTrue(obsSet.isEmpty());
    verifyZeroInteractions(conceptService);
  }

}
