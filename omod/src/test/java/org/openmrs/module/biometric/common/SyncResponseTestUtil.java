/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.common;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncScope;

public class SyncResponseTestUtil {

  public static SyncScope createSyncScope() {
    SyncScope syncScope = new SyncScope();
    syncScope.setSiteUuid("8gi19999-h1af-9899-b684-851abfbac4d9");
    syncScope.setCountry("BELGIUM");
    return syncScope;
  }

  public static SyncRequest createSyncRequestObject() {
    SyncRequest syncRequest = new SyncRequest();
    syncRequest.setDateModifiedOffset(System.currentTimeMillis());
    syncRequest.setSyncScope(createSyncScope());
    syncRequest.setOptimize(false);
    syncRequest.setOffset(3);
    syncRequest.setLimit(10);
    return syncRequest;
  }

  public static List<SyncImageResponse> createSyncImageResponseList() {
    List<SyncImageResponse> records = new ArrayList<>();
    SyncImageResponse syncImageResponse = new SyncImageResponse();
    syncImageResponse.setDateModified(System.currentTimeMillis());
    syncImageResponse.setImage("myimage.jpeg");
    syncImageResponse.setParticipantUuid("8gi19999-h1af-9899-b684-851abfbac89ie");
    records.add(syncImageResponse);
    syncImageResponse.setDateModified(System.currentTimeMillis());
    syncImageResponse.setImage("myimage1.jpeg");
    syncImageResponse.setParticipantUuid("8gi19999-h1af-9899-b684-851abfbac8e3e");
    records.add(syncImageResponse);
    return records;
  }
}
