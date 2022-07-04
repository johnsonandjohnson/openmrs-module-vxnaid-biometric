/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.contract.sync;

import java.util.List;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * SyncResponse class.
 */
public class SyncResponse {

  private Long dateModifiedOffset;

  private SyncScope syncScope;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private Boolean optimize;

  private SyncStatus syncStatus;

  private Set<String> uuidsWithDateModifiedOffset;

  private int limit;

  private Long tableCount;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  private Long ignoredCount;

  @JsonProperty("voidedTableCount")
  private Long voidedCount;

  private List<?> records;

  public Long getDateModifiedOffset() {
    return dateModifiedOffset;
  }

  public void setDateModifiedOffset(Long dateModifiedOffset) {
    this.dateModifiedOffset = dateModifiedOffset;
  }

  public SyncScope getSyncScope() {
    return syncScope;
  }

  public void setSyncScope(SyncScope syncScope) {
    this.syncScope = syncScope;
  }

  public Set<String> getUuidsWithDateModifiedOffset() {
    return uuidsWithDateModifiedOffset;
  }

  public void setUuidsWithDateModifiedOffset(Set<String> uuidsWithDateModifiedOffset) {
    this.uuidsWithDateModifiedOffset = uuidsWithDateModifiedOffset;
  }

  public Boolean getOptimize() {
    return optimize;
  }

  public void setOptimize(Boolean optimize) {
    this.optimize = optimize;
  }

  public SyncStatus getSyncStatus() {
    return syncStatus;
  }

  public void setSyncStatus(SyncStatus syncStatus) {
    this.syncStatus = syncStatus;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public Long getIgnoredCount() {
    return ignoredCount;
  }

  public void setIgnoredCount(Long ignoredCount) {
    this.ignoredCount = ignoredCount;
  }

  public Long getTableCount() {
    return tableCount;
  }

  public void setTableCount(Long tableCount) {
    this.tableCount = tableCount;
  }

  public Long getVoidedCount() {
    return voidedCount;
  }

  public void setVoidedCount(Long voidedCount) {
    this.voidedCount = voidedCount;
  }

  @SuppressWarnings("java:S1452")
  public List<?> getRecords() {
    return records;
  }

  public void setRecords(List<?> records) {
    this.records = records;
  }
}
