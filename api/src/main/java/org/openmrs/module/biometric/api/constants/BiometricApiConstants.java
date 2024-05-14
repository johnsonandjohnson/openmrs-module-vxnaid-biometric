/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.constants;

import java.io.File;

/**
 * Constants used by the Biometric API module.
 */

public final class BiometricApiConstants {

  public static final int INITIAL_SIZE = 100;
  public static final String APP_PROPERTIES_FILE = "biometric";
  public static final String PARTICIPANT_IMAGES_DIR = "biometric.images.dir";
  public static final String DEFAULT_PARTICIPANT_IMAGES_DIR = File.separator + "person_images";
  public static final String IMAGE_EXTN = "jpeg";
  public static final String MAIN_CONFIG = "main";
  public static final String TABLE_COUNT = "tableCount";
  public static final String IGNORED_COUNT = "ignoredCount";
  public static final String VOIDED_COUNT = "voidedCount";
  public static final String SYNC_DELETE = "delete";
  public static final String VOIDED = "voided";
  public static final String SYNC_UPDATE = "update";
  public static final String ACTIVE_COUNT = "activeCount";
  public static final String CFL_VACCINES = "cfl.vaccines";
  public static final String DOSING_VISIT_TYPE = "Dosing";
  public static final String DOSE_NUMBER_ATTRIBUTE_TYPE_NAME = "Dose number";
  private static final String GP_PREFIX = "biometric.api.config";
  public static final String MAIN_CONFIG_GP = GP_PREFIX + ".main";
  public static final String LOCALIZATION_GP = GP_PREFIX + ".localization";
  public static final String PERSON_IMAGE_ATTRIBUTE = "PersonImageAttribute";
  public static final String PERSON_TEMPLATE_ATTRIBUTE = "PersonTemplateAttribute";
  public static final String NIN_IDENTIFIER_NAME = "National ID";
  public static final String ENABLE_BIOMETRIC = "biometric.enable.biometric.feature";

  private BiometricApiConstants() {
  }
}
