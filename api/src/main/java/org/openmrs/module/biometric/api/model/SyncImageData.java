/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.model;

import java.nio.file.Path;

/**
 * This class is used to build the repsonse for sync image data.
 *
 */
public class SyncImageData {

  private Path path;

  private boolean voided;

  private long dateModified;

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  public boolean isVoided() {
    return voided;
  }

  public void setVoided(boolean voided) {
    this.voided = voided;
  }

  public long getDateModified() {
    return dateModified;
  }

  public void setDateModified(long dateModified) {
    this.dateModified = dateModified;
  }
}
