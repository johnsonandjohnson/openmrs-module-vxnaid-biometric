/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.builder;

import static org.openmrs.module.biometric.api.constants.BiometricApiConstants.SYNC_UPDATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.model.SyncImageData;
import org.springframework.stereotype.Component;

/**
 * This class has methods to build the response for sync image api.
 */
@Component
public class ImageResponseBuilder {

  /**
   * Build response object for sync images api.
   *
   * @param results participants image path details
   * @return participants image details
   * @throws IOException in case of any error
   */
  public List<SyncImageResponse> createFrom(List<SyncImageData> results) throws IOException {
    List<SyncImageResponse> responses = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    for (SyncImageData image : results) {
      SyncImageResponse response = new SyncImageResponse();
      File file = image.getPath().toFile();
      response.setParticipantUuid(FilenameUtils.getBaseName(file.getName()));
      response.setDateModified(image.getDateModified());
      response.setType("delete");
      if (!image.isVoided()) {
        if (!file.exists()) {
          continue;
        }
        byte[] imageFileContent = FileUtils.readFileToByteArray(file);
        String base64EncodedImage = Base64.getEncoder().encodeToString(imageFileContent);

        if (null != base64EncodedImage) {
          response.setType(SYNC_UPDATE);
          response.setImage(base64EncodedImage);
        }
      }
      responses.add(response);
    }
    return responses;
  }

  /**
   * Build response object for sync images api.
   *
   * @param results participants image path details
   * @return participants image details
   * @throws IOException in case of any error
   */
  public List<SyncImageResponse> createFromPath(List<Path> results) throws IOException {
    List<SyncImageResponse> responses = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    for (Path filePath : results) {
      SyncImageResponse response = new SyncImageResponse();
      File file = filePath.toFile();
      response.setParticipantUuid(FilenameUtils.getBaseName(file.getName()));
      response.setDateModified(file.lastModified());
      response.setType("delete");
      if (file.getName().endsWith(BiometricApiConstants.IMAGE_EXTN)) {
        byte[] imageFileContent = FileUtils.readFileToByteArray(file);
        String base64EncodedImage = Base64.getEncoder().encodeToString(imageFileContent);

        if (null != base64EncodedImage) {
          response.setType(SYNC_UPDATE);
          response.setImage(base64EncodedImage);
        }
      }
      responses.add(response);
    }
    return responses;
  }
}
