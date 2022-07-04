/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.builder;

import java.util.List;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncResponse;
import org.openmrs.module.biometric.contract.sync.SyncStatus;
import org.springframework.stereotype.Component;

/**
 * SyncRepsonse Builder class.
 *
 */
@Component
public class SyncResponseBuilder {

  /**
   * Builds sync response for the given params.
   *
   * @param records patient/image/template records
   * @param totalCount total count
   * @param ignoredCount ignored count
   * @param voidedCount deactivated count
   * @param syncRequest syncrequest object
   * @return details of patient/images/templates
   */
  public SyncResponse createFrom(List<?> records, Long totalCount, Long ignoredCount,
      Long voidedCount,
      SyncRequest syncRequest) {

    SyncResponse syncResponse = new SyncResponse();

    if (!records.isEmpty()) {
      syncResponse.setSyncStatus(SyncStatus.OUT_OF_SYNC);
    } else {
      syncResponse.setSyncStatus(SyncStatus.OK);
    }
    syncResponse.setDateModifiedOffset(syncRequest.getDateModifiedOffset());
    syncResponse.setSyncScope(syncRequest.getSyncScope());
    syncResponse.setUuidsWithDateModifiedOffset(syncRequest.getUuidsWithDateModifiedOffset());
    syncResponse.setOptimize(syncRequest.getOptimize());
    syncResponse.setLimit(syncRequest.getLimit());
    syncResponse.setTableCount(totalCount);
    syncResponse.setIgnoredCount(ignoredCount);
    syncResponse.setVoidedCount(voidedCount);
    syncResponse.setRecords(records);
    return syncResponse;
  }
}
