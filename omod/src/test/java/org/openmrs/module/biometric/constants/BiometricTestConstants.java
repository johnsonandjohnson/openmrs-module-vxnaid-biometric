/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.constants;

import java.util.Date;
import org.openmrs.module.biometric.common.BiometricTestUtil;

/**
 * Test constants used by the Biometric API Module
 */
public final class BiometricTestConstants {

  public static final String PARTICIPANT_ID = "test1";
  public static final int PATIENT1_ID = 1;
  public static final String PERSON1_UUID = "11105e68-4d77-4b61-b2bf-436857f35aa0";
  public static final String PERSON1_GENDER = "M";
  public static final Date PERSON1_DOB = BiometricTestUtil
      .convertUTCToDate("2000-01-01T00:00:00.000Z");
  public static final String PERSON1_ADDRESS1 = "FLAT 111, ABC APT";
  public static final String PERSON1_ADDRESS2 = "WHITEFIELD";
  public static final String PERSON1_CITY_VILLAGE = "BANGALORE";
  public static final String PERSON1_POSTAL_CODE = "560066";
  public static final String PERSON1_COUNTRY = "India";

  public static final String PATIENT1_IDENTIFIER = "test1";
  public static final String PATIENT1_LOCATION = "1dd05e68-4d77-4b61-b2bf-436857f35aa0";
  public static final String PERSON1_LANGUAGE = "en";

  public static final String PERSON2_UUID = "22205e68-4d77-4b61-b2bf-436857f35aa0";
  public static final String PERSON2_GENDER = "F";
  public static final Date PERSON2_DOB = BiometricTestUtil
      .convertUTCToDate("2010-01-01T00:00:00.000Z");
  public static final String PERSON2_ADDRESS1 = "FLAT 222, DEF APT";
  public static final String PERSON2_ADDRESS2 = "Banjara Hills";
  public static final String PERSON2_CITY_VILLAGE = "HYDERABAD";
  public static final String PERSON2_POSTAL_CODE = "500001";
  public static final String PATIENT2_IDENTIFIER = "test12";
  public static final String PATIENT2_LOCATION = "2dd05e68-4d77-4b61-b2bf-436857f35aa0";
  public static final String PERSON2_COUNTRY = "India";


  public static final String LOCATION_ATTRIBUTE = "LocationAttribute";
  public static final String LANGUAGE_ATTRIBUTE = "personLanguage";
  public static final String VP_ATTRIBUTE = "Vaccination Program";
  public static final String STATUS_ATTRIBUTE = "Person status";
  public static final String VACCINE_PROGRAM = "VP1";
  public static final String PERSON_STATUS = "ACTIVE";

  public static final int LOW_MATCHING_SCORE = 50;
  public static final int HIGH_MATCHING_SCORE = 1111;
  public static final String COUNTRY = "country";

  public static final String CLUSTER_ATTRIBUTE_TYPE = "cluster";

  private BiometricTestConstants() {

  }
}
