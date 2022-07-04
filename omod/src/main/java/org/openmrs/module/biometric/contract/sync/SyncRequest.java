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

import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.module.biometric.util.SanitizeUtil;

/**
 * SyncRequest class.
 */
@JsonIgnoreProperties("offset")
public class SyncRequest {

  private Long dateModifiedOffset;

  private SyncScope syncScope;

  private Set<String> uuidsWithDateModifiedOffset;

  private int offset;

  private int limit;

  private Boolean optimize;

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
    this.uuidsWithDateModifiedOffset = SanitizeUtil
        .sanitizeStringList(uuidsWithDateModifiedOffset);
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public Boolean getOptimize() {
    return optimize;
  }

  public void setOptimize(Boolean optimize) {
    this.optimize = optimize;
  }
}

