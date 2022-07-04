/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service.impl;

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.client.NClusterBiometricConnection;
import com.neurotec.io.NBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.BiometricMatchingResult;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.service.BiometricService;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements BiometricService.
 */

public class BiometricServiceImpl extends BaseOpenmrsService implements BiometricService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BiometricServiceImpl.class);

  private static final String SERVER_URL = "biometric.server.url";
  private static final String ADMIN_PORT = "biometric.admin.port";
  private static final String CLIENT_PORT = "biometric.client.port";
  private static final String MATCHING_THRESHOLD = "biometric.matching.threshold";
  private static final String ENABLE_BIOMETRIC = "biometric.enable.biometric.feature";
  private static final String DEVICE_ID = "deviceId";
  private static final String COUNTRY = "country";
  private static final String SITE_ID = "siteId";

  private NBiometricClient biometricClient;

  private Integer matchingThreshold;

  /**
   * Default constructor.
   */
  public BiometricServiceImpl() {

    Properties properties = OpenmrsUtil
        .getRuntimeProperties(BiometricApiConstants.APP_PROPERTIES_FILE);

    boolean enableBiometricFeature = Boolean.parseBoolean(properties.getProperty(ENABLE_BIOMETRIC));
    if (enableBiometricFeature) {
      biometricClient = new NBiometricClient();
      NClusterBiometricConnection connection = new NClusterBiometricConnection();
      connection.setHost(properties.getProperty(SERVER_URL));
      connection.setAdminPort(Integer.parseInt(properties.getProperty(ADMIN_PORT)));
      connection.setPort(Integer.parseInt(properties.getProperty(CLIENT_PORT)));
      matchingThreshold = Integer.valueOf(properties.getProperty(MATCHING_THRESHOLD));

      biometricClient.getRemoteConnections().add(connection);

      LOGGER.info("****Biometric Server configurations****");
      LOGGER.info("Biometric Server Host : {}", connection.getHost());
      LOGGER.info("Admin Port : {}", connection.getAdminPort());
      LOGGER.info("Client Port : {} ", connection.getPort());
      LOGGER.info("Iris Matching threshold : {}", matchingThreshold);
    }
  }

  @Transactional
  @Override
  public boolean registerBiometricData(String participantId, byte[] template, String deviceId,
      String locationUuid,
      Date registrationDate, String participantUuid) throws APIException {
    NSubject subject = null;
    NBiometricTask enrollTask = null;
    try {
      NBuffer nbuffer = NBuffer.fromArray(template);
      subject = createSubject(nbuffer, participantId, deviceId, locationUuid, registrationDate,
          participantUuid);
      enrollTask = biometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL), subject);
      biometricClient.performTask(enrollTask);

      LOGGER.debug("Enrollment Status: {} for participant {}", enrollTask.getStatus(),
          subject.getId());
      if (enrollTask.getStatus() != NBiometricStatus.OK) {
        if (null != enrollTask.getError()) {
          LOGGER.error("Enrollment failed with exception :", enrollTask.getError());
          throw new BiometricApiException("Enrollment failed", enrollTask.getError());
        }
        return false;
      }
    } catch (Exception e) {
      throw new APIException(ExceptionUtils.getRootCauseMessage(e), ExceptionUtils.getRootCause(e));
    } finally {
      if (subject != null) {
        subject.dispose();
      }
      if (enrollTask != null) {
        enrollTask.dispose();
      }
    }
    return true;
  }

  @Transactional(readOnly = true)
  @Override
  public List<BiometricMatchingResult> matchBiometricData(byte[] template,
      Set<String> participantSet)
      throws APIException {

    List<BiometricMatchingResult> matchList = new ArrayList<>();

    try (NSubject nSubject = new NSubject()) {
      nSubject.setTemplateBuffer(NBuffer.fromArray(template));
      biometricClient.setMatchingThreshold(matchingThreshold);

      String queryString = null;
      if (!participantSet.isEmpty()) {
        String inCond = participantSet.stream().collect(Collectors.joining("','", "'", "'"));
        queryString = "ID in (" + inCond + " )";
      }

      if (null != queryString) {
        nSubject.setQueryString(queryString);
      }

      NBiometricStatus status = biometricClient.identify(nSubject);

      if (status == NBiometricStatus.OK) {
        for (NMatchingResult result : nSubject.getMatchingResults()) {
          BiometricMatchingResult biometricMatchingResult = new BiometricMatchingResult();
          biometricMatchingResult.setId(result.getId());
          biometricMatchingResult.setMatchingScore(result.getScore());
          matchList.add(biometricMatchingResult);
        }
      }
    } catch (Exception e) {
      // biographic information should be registered even though biometric server is not working
      LOGGER.error("Message : {}", ExceptionUtils.getRootCauseMessage(e));
      // As NEOFException cannot be caught so checking the exception message
      // when an invalid template is processed.
      if (ExceptionUtils.getRootCauseMessage(e).contains("Unexpected end of stream")) {
        throw new APIException("Invalid Template", ExceptionUtils.getRootCause(e));
      }
      throw new APIException(ExceptionUtils.getRootCauseMessage(e), ExceptionUtils.getRootCause(e));
    }
    return matchList;
  }

  @Override
  public final boolean purgeBiometricData(String participantId) {
    boolean status = false;
    NSubject subject = new NSubject();
    subject.setId(participantId);
    NBiometricTask task = biometricClient
        .createTask(EnumSet.of(NBiometricOperation.DELETE), subject);

    biometricClient.performTask(task);
    if (task.getStatus() != NBiometricStatus.OK) {
      LOGGER.debug("Delete template was unsuccessful. Status: {}.\n", task.getStatus());
    } else {
      status = true;
    }
    subject.dispose();
    return status;
  }

  @Override
  public final boolean voidBiometricData(String participantId) {
    NSubject subject = new NSubject();
    subject.setId(participantId);
    NBiometricTask task = biometricClient.createTask(EnumSet.of(NBiometricOperation.GET), subject);

    if (task.getStatus() != NBiometricStatus.OK) {
      task.getSubjects().forEach(nSubject -> {
        LOGGER.info("Void request for biometric template for Participant : {} ", nSubject.getId());
        if (nSubject.getId().equalsIgnoreCase(participantId)) {
          nSubject.setProperty("voided", Boolean.TRUE);
          NBiometricTask updateTask = biometricClient
              .createTask(EnumSet.of(NBiometricOperation.UPDATE), nSubject);
          biometricClient.performTask(updateTask);
          if (updateTask.getStatus() != NBiometricStatus.OK) {
            LOGGER.debug("Void participant template was unsuccessful. Status: {}.\n",
                task.getStatus());
          }
        }
        nSubject.dispose();
      });
      subject.dispose();
    }
    return true;
  }

  private NSubject createSubject(NBuffer nbuffer, String participantId, String deviceId,
      String locationUuid,
      Date registrationDate, String participantUuid) {
    NSubject subject = new NSubject();
    subject.setTemplateBuffer(nbuffer);
    subject.setId(participantId);
    if (null != deviceId) {
      subject.setProperty(DEVICE_ID, deviceId);
    }
    LocationService locationService = Context.getLocationService();
    Location location = locationService.getLocationByUuid(locationUuid);
    if (null != location) {
      if (null != location.getCountry()) {
        subject.setProperty(COUNTRY, location.getCountry());
      }
      if (null != location.getUuid()) {
        subject.setProperty(SITE_ID, location.getUuid());
      }
    }
    subject.setProperty("participantUuid", participantUuid);
    subject.setProperty("creationDate", registrationDate);
    subject.setProperty("modificationDate", new Date());
    subject.setProperty("voided", Boolean.FALSE);
    return subject;
  }
}
