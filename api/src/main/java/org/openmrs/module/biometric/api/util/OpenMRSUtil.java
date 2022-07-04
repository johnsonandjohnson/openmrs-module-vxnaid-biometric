package org.openmrs.module.biometric.api.util;

import org.openmrs.BaseOpenmrsData;

import java.util.Date;

public class OpenMRSUtil {
  private OpenMRSUtil() {
  }

  /**
   * Returns last modification date of given OpenMRS data entity.
   * <p>
   * The last modification date is equal to DateChanged or DateCreated if the former is not set.
   *
   * @param openmrsData the OpenMRS data entity, not null
   * @return the last modification date, never null
   */
  public static Date getLastModificationDate(BaseOpenmrsData openmrsData) {
    return openmrsData.getDateChanged() != null ? openmrsData.getDateChanged() : openmrsData.getDateCreated();
  }
}
