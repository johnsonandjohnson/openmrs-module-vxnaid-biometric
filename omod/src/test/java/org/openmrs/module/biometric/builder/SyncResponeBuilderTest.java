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

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.common.SyncResponseTestUtil;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncResponse;
import org.openmrs.module.biometric.contract.sync.SyncStatus;

@RunWith(MockitoJUnitRunner.class)
public class SyncResponeBuilderTest {

  private static SyncRequest syncRequest;
  private static List<SyncImageResponse> syncImageResponseList;

  @InjectMocks
  private SyncResponseBuilder syncResponseBuilder;

  @Before
  public void setUp() {
    syncRequest = SyncResponseTestUtil.createSyncRequestObject();
    syncImageResponseList = SyncResponseTestUtil.createSyncImageResponseList();
  }

  @Test
  public void createFrom_shouldReturnSyncResponseWithSyncStatusOutOffSync() {
    Long totalCount = new Long(4);
    Long ignoredCount = new Long(7);
    Long voidedCount = 0L;

    SyncResponse syncResponse = syncResponseBuilder
        .createFrom(syncImageResponseList, totalCount, ignoredCount, voidedCount, syncRequest);

    assertNotNull(syncResponse);
    assertEquals(SyncStatus.OUT_OF_SYNC, syncResponse.getSyncStatus());
  }

  @Test
  public void createFrom_shouldReturnSyncResponseWithSyncStatusOk() {
    Long totalCount = new Long(4);
    Long ignoredCount = new Long(7);
    Long voidedCount = 0L;

    SyncResponse syncResponse = syncResponseBuilder
        .createFrom(Collections.emptyList(), totalCount, 0L, ignoredCount, syncRequest);

    assertNotNull(syncResponse);
    assertEquals(SyncStatus.OK, syncResponse.getSyncStatus());
  }
}
