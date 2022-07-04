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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.common.ParticipantRecordsResponseBuilderTestUtil;
import org.openmrs.module.biometric.contract.sync.ParticipantData;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncResponse;
import org.openmrs.module.biometric.util.BiometricModUtil;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ParticipantRecordsResponseBuilderTest {

  @Mock
  private SyncResponseBuilder syncResponseBuilder;

  @Mock
  private ConfigService configService;

  @Mock
  private BiometricModUtil util;

  @InjectMocks
  private ParticipantRecordsResponseBuilder participantRecordsResponseBuilder;

  @Test
  public void createFrom_shouldReturnSyncResponse() throws EntityNotFoundException, IOException {
    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    String strDate = dateFormat.format(date);

    SyncRequest syncRequest = ParticipantRecordsResponseBuilderTestUtil.createSyncRequest();
    String json1 = "{\"addressFields\":{\"Belgium\":[{\"mappingPos\":3,\"name\":\"City\",\"field\":\"cityVillage\"}," +
        "{\"mappingPos\":4,\"name\":\"PostalCode\",\"field\":\"postalCode\"},{\"name\":\"Street\"," +
        "\"field\":\"address1\"},{\"name\":\"Number\",\"field\":\"address2\"}]}}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode1 = objectMapper.readTree(json1);
    Mockito.when(util.toJsonNode(anyString())).thenReturn(jsonNode1);
    when(util.dateToISO8601(any(Date.class))).thenReturn(strDate);
    when(util.getPersonAddressProperty(any(PersonAddress.class), anyString())).thenReturn("Belgium");
    when(syncResponseBuilder.createFrom(anyList(), anyLong(), anyLong(), anyLong(),
        any(SyncRequest.class))).thenCallRealMethod();

    long total = 4L;
    long voidCount = 3L;
    List<Patient> patientList = Collections.singletonList(ParticipantRecordsResponseBuilderTestUtil.createPatient());

    SyncResponse response = participantRecordsResponseBuilder.createFrom(patientList, total, voidCount, syncRequest);

    assertNotNull(response);
    assertTrue("Should containe ParticipantData record.", response.getRecords().get(0) instanceof ParticipantData);

    final ParticipantData voidedParticipant = (ParticipantData) response.getRecords().get(0);
    assertEquals(BiometricApiConstants.SYNC_UPDATE, voidedParticipant.getType());
    assertEquals("btest1", voidedParticipant.getParticipantId());
  }

  @Test
  public void createFrom_shouldReturnSyncResponseWithVoidedPatient() throws EntityNotFoundException, IOException {
    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    String strDate = dateFormat.format(date);

    SyncRequest syncRequest = ParticipantRecordsResponseBuilderTestUtil.createSyncRequest();
    String json1 = "{\"addressFields\":{\"Belgium\":[{\"mappingPos\":3,\"name\":\"City\",\"field\":\"cityVillage\"}," +
        "{\"mappingPos\":4,\"name\":\"PostalCode\",\"field\":\"postalCode\"},{\"name\":\"Street\"," +
        "\"field\":\"address1\"},{\"name\":\"Number\",\"field\":\"address2\"}]}}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode1 = objectMapper.readTree(json1);
    Mockito.when(util.toJsonNode(anyString())).thenReturn(jsonNode1);
    when(util.dateToISO8601(any(Date.class))).thenReturn(strDate);
    when(util.getPersonAddressProperty(any(PersonAddress.class), anyString())).thenReturn("Belgium");
    when(syncResponseBuilder.createFrom(anyList(), anyLong(), anyLong(), anyLong(),
        any(SyncRequest.class))).thenCallRealMethod();

    long total = 4L;
    long voidCount = 3L;
    Patient voidedPatient = ParticipantRecordsResponseBuilderTestUtil.createPatient();
    voidedPatient.setVoided(true);
    voidedPatient.getPatientIdentifier().setVoided(true);
    List<Patient> patientList = Collections.singletonList(voidedPatient);

    SyncResponse response = participantRecordsResponseBuilder.createFrom(patientList, total, voidCount, syncRequest);

    assertNotNull(response);
    assertTrue("Should containe ParticipantData record.", response.getRecords().get(0) instanceof ParticipantData);

    final ParticipantData voidedParticipant = (ParticipantData) response.getRecords().get(0);
    assertEquals(BiometricApiConstants.SYNC_DELETE, voidedParticipant.getType());
    assertEquals("btest1", voidedParticipant.getParticipantId());
  }
}
