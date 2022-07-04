/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ImageResponseBuilderTest {

  @InjectMocks
  private ImageResponseBuilder imageResponseBuilder;

  @Test
  public void createFrom_shouldReturnSyncRepsonseList() throws IOException {
    String filePath = "src/test/resources/images/Device1/test1.jpeg";
    Path p1 = Paths.get(filePath);
    List<Path> results = Arrays.asList(p1);

    List<SyncImageResponse> responseList = imageResponseBuilder.createFromPath(results);

    assertNotNull(responseList);
    assertEquals(1, responseList.size());

  }

}
