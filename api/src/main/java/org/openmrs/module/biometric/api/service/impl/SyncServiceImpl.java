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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.builder.ImageResponseBuilder;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.db.SyncDao;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.helper.SyncQueryHelper;
import org.openmrs.module.biometric.api.model.SyncImageData;
import org.openmrs.module.biometric.api.model.SyncTemplateData;
import org.openmrs.module.biometric.api.service.SyncService;
import org.openmrs.module.biometric.api.util.BiometricApiUtil;
import org.openmrs.module.biometric.api.util.SecurityUtil;
import org.openmrs.module.licensemanagement.Device;
import org.openmrs.module.licensemanagement.DeviceError;
import org.openmrs.module.licensemanagement.api.DeviceErrorService;
import org.openmrs.module.licensemanagement.api.DeviceService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * The implementation class for SyncService.
 */
public class SyncServiceImpl implements SyncService {

  private static final String TEMPLATE = "template";
  private static final String FETCH_SIZE = "biometric.database.fetchsize";
  private static final String VOIDED_COUNT = "voidedCount";
  private static final String PERSON_IMAGE_ATTRIBUTE = "PersonImageAttribute";
  private static final String PERSON_TEMPLATE_ATTRIBUTE = "PersonTemplateAttribute";
  private DriverManagerDataSource dataSource;
  private String personImagesDir;
  private int fetchSize;

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private DeviceErrorService deviceErrorService;

  @Autowired
  private BiometricApiUtil util;

  @Autowired
  private ImageResponseBuilder builder;

  private SyncDao syncDao;

  public void setSyncDao(SyncDao syncDao) {
    this.syncDao = syncDao;
  }

  @PostConstruct
  public final void init() throws BiometricApiException {
    Properties properties =
        OpenmrsUtil.getRuntimeProperties(BiometricApiConstants.APP_PROPERTIES_FILE);
    personImagesDir = util.getImageDirectory(properties);
    dataSource = util.getDataSource(properties);
    fetchSize = Integer.parseInt(properties.getProperty(FETCH_SIZE, "100"));
  }

  @Override
  public final List<Patient> getAllPatients(
      Date lastModifiedDate, int maxResultsToFetch, List<String> locations) {

    return syncDao
        .getAllPatientsByLocations(lastModifiedDate, maxResultsToFetch, locations);
  }

  @Override
  public final Map<String, Long> getPatientCount(List<String> locations) {
    return getCounts(syncDao.getPatientCount(locations));
  }

  @Override
  public final List<SyncImageResponse> getAllParticipantImages(
      Date lastModifiedDate,
      int maxResultsToFetch,
      List<String> locations,
      String deviceId,
      boolean optimizeData)
      throws IOException {

    List<Patient> patients = syncDao
        .getPatientImageData(lastModifiedDate, deviceId, locations, optimizeData,
            maxResultsToFetch);

    List<SyncImageData> results = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    for (Patient p : patients) {
      SyncImageData data = new SyncImageData();
      data.setVoided(p.getVoided());
      String imageName = String.format("%s.%s", p.getUuid(), BiometricApiConstants.IMAGE_EXTN);
      String imagePathStr = personImagesDir
          + File.separator
          + p.getAllAttributeMap().get(PERSON_IMAGE_ATTRIBUTE).getValue()
          + File.separator + imageName;
      Path imagePath = util.getImageDirPath(imagePathStr);
      data.setPath(imagePath);
      data.setDateModified(p.getDateChanged().getTime());
      results.add(data);
    }
    return builder.createFrom(results);
  }

  @Override
  public final Map<String, Long> getParticipantImagesCount(
      List<String> locations,
      String deviceId,
      boolean optimizeFlag) {
    // ignored count wont't be shown in the response if the optimize flag is false
    Long ignoredCount = null;
    if (optimizeFlag) {
      ignoredCount = syncDao
          .getIgnoredCount(locations, deviceId, PERSON_IMAGE_ATTRIBUTE);
    }
    List<Object[]> list = syncDao
        .getPatientCountByLocationsAndAttribute(locations, PERSON_IMAGE_ATTRIBUTE);
    Map<String, Long> countMap = getCounts(list);
    countMap.put(BiometricApiConstants.IGNORED_COUNT, ignoredCount);
    return countMap;
  }

  @Override
  @Transactional
  public List<Visit> getAllVisits(
      Date lastModifiedDate, int maxResultsToFetch, List<String> locations) {
    return syncDao.getAllVisits(lastModifiedDate, maxResultsToFetch, locations);
  }

  @Override
  @Transactional
  public final Map<String, Long> getVisitsCount(List<String> locations) {
    return getCounts(syncDao.getVisitCount(locations));
  }

  @Override
  @Transactional(readOnly = true)
  public final List<SyncTemplateResponse> getAllBiometricTemplates(
      Date lastModifiedDate,
      String deviceId,
      String country,
      String siteId,
      List<String> locations,
      boolean optimize,
      int maxResultsToFetch) {

    List<SyncTemplateData> patients =
        syncDao
            .getPatientTemplateData(lastModifiedDate, deviceId, locations, optimize,
                maxResultsToFetch);

    List<String> patientIdentifierList = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    for (SyncTemplateData templateData : patients) {
      patientIdentifierList.add(templateData.getIdentifier());
    }
    Map<String, String> templatesMap = new HashMap<>();
    List<SyncTemplateResponse> responseList = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);

    if (!patientIdentifierList.isEmpty()) {
      NamedParameterJdbcTemplate template =
          util.getNamedParameterJdbcTemplate(dataSource, fetchSize);
      Map<String, Object> params = new HashMap<>();
      params.put("patientIdentifierList", patientIdentifierList);

      SqlParameterSource namedParameters = new MapSqlParameterSource(params);
      String templatesQuery = SyncQueryHelper.buildSyncTemplatesQuery();
      List<Map<String, Object>> rows = template.queryForList(templatesQuery, namedParameters);

      for (Map<String, Object> map : rows) {
        templatesMap.put((String) map.get("dbid"), (String) map.get(TEMPLATE));
      }
    }

    for (SyncTemplateData data : patients) {
      SyncTemplateResponse response = new SyncTemplateResponse();
      response.setParticipantUuid(SecurityUtil.sanitizeOutput(data.getUuid()));

      boolean voided = data.isVoided();
      if (!voided) {
        String irisTemplate = templatesMap.get(data.getIdentifier());
        response.setBiometricsTemplate(SecurityUtil.sanitizeOutput(irisTemplate));
        response.setType(BiometricApiConstants.SYNC_UPDATE);
      } else {
        response.setType(BiometricApiConstants.SYNC_DELETE);
      }
      Date modificationDate = data.getDateModified();
      response.setDateModified(modificationDate.getTime());
      responseList.add(response);
    }
    return responseList;
  }

  @Transactional(readOnly = true)
  @Override
  public final Map<String, Long> getBiometricTemplatesCount(
      String deviceId, List<String> locations, boolean optimize) {

    // ignored count wont't be shown in the response if the optimize flag is false
    Long ignoredCount = null;

    //calculate ignored templates count when the optimize flag is true
    if (optimize) {
      ignoredCount = syncDao
          .getIgnoredCount(locations, deviceId, PERSON_TEMPLATE_ATTRIBUTE);
    }
    //calculate total templates count
    List<Object[]> list = syncDao
        .getPatientCountByLocationsAndAttribute(locations, PERSON_TEMPLATE_ATTRIBUTE);
    Map<String, Long> countMap = getCounts(list);
    countMap.put(BiometricApiConstants.IGNORED_COUNT, ignoredCount);
    return countMap;
  }

  @Transactional
  @Override
  public final void saveSyncError(
      String deviceId, String stackTrace, Date createdDate, String key, String meta) {

    Device device = deviceService.getDeviceByMAC(deviceId, Boolean.FALSE);
    if (null == device) {
      device = new Device();
      device.setDeviceMac(deviceId);
      device.setName(deviceId);
      device = deviceService.saveDevice(device);
    }

    DeviceError deviceError = new DeviceError();
    String[] metaArray = key.split(":", 2);
    deviceError.setMetaType(metaArray.length > 0 ? metaArray[0] : null);
    if (metaArray.length == 2) {
      String[] subTypeArray = metaArray[1].split(",", 2);
      deviceError.setMetaSubType(subTypeArray.length > 0 ? subTypeArray[0] : metaArray[1]);
    }
    deviceError.setStackTrace(stackTrace);
    deviceError.setReportedDate(createdDate);
    deviceError.setKey(key);
    deviceError.setMeta(meta);
    deviceError.setDevice(device);
    deviceErrorService.saveDeviceError(deviceError);
  }

  @Transactional(rollbackFor = EntityNotFoundException.class)
  @Override
  public void resolveSyncErrors(String deviceId, List<String> errorKeys)
      throws EntityNotFoundException {
    Device device = deviceService.getDeviceByMAC(deviceId, Boolean.FALSE);
    if (null == device) {
      throw new EntityNotFoundException("Device not found");
    }
    for (String key : errorKeys) {
      List<DeviceError> deviceErrors = deviceErrorService.getDeviceErrorsByKey(device, key, false);
      for (DeviceError deviceError : deviceErrors) {
        deviceError.setVoided(Boolean.TRUE);
        deviceError.setVoidReason("Error resolved");
        deviceError.setVoidedBy(Context.getAuthenticatedUser());
        deviceErrorService.saveDeviceError(deviceError);
      }
    }
  }

  @SuppressWarnings("fb-contrib:CLI_CONSTANT_LIST_INDEX")
  private Map<String, Long> getCounts(List<Object[]> objArr) {
    Map<String, Long> map = new HashMap<>(3);
    map.put(BiometricApiConstants.TABLE_COUNT, 0L);
    map.put(BiometricApiConstants.ACTIVE_COUNT, 0L);
    map.put(BiometricApiConstants.VOIDED_COUNT, 0L);

    Map<Boolean, Long> countMap = objArr.stream()
        .collect(Collectors.toMap (a -> (Boolean) a[0], a -> (Long) a[1]));

    Long voidedCount = countMap.get(Boolean.TRUE);
    if (null != voidedCount) {
      map.put(BiometricApiConstants.VOIDED_COUNT, voidedCount);
    }

    Long activeCount = countMap.get(Boolean.FALSE);
    if (null != activeCount) {
      map.put(BiometricApiConstants.ACTIVE_COUNT, activeCount);
    }

    long total;
    if (map.containsKey(VOIDED_COUNT)) {
      total = map.get(BiometricApiConstants.ACTIVE_COUNT) + map.get(BiometricApiConstants.VOIDED_COUNT);
    } else {
      total = map.get(BiometricApiConstants.ACTIVE_COUNT);
    }
    map.put(BiometricApiConstants.TABLE_COUNT, total);
    return map;
  }
}
