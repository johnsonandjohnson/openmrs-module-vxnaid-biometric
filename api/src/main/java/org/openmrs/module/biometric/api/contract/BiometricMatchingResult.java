/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.contract;

/**
 * Match participants biometric data response contract.
 */
public class BiometricMatchingResult {

  private String id;

  private int matchingScore;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getMatchingScore() {
    return matchingScore;
  }

  public void setMatchingScore(int matchingScore) {
    this.matchingScore = matchingScore;
  }

}
