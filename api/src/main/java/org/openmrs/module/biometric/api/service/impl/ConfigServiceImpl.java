/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.biometric.api.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.LicenseResponse;
import org.openmrs.module.biometric.api.contract.LocationResponse;
import org.openmrs.module.biometric.api.contract.SyncConfigResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.service.ConfigService;
import org.openmrs.module.biometric.api.util.SecurityUtil;
import org.openmrs.module.licensemanagement.Device;
import org.openmrs.module.licensemanagement.DeviceAttribute;
import org.openmrs.module.licensemanagement.DeviceAttributeType;
import org.openmrs.module.licensemanagement.License;
import org.openmrs.module.licensemanagement.api.DeviceService;
import org.openmrs.module.licensemanagement.api.LicenseService;
import org.openmrs.module.licensemanagement.api.LicenseTypeService;

/** The implementation class for ConfigService. */
public class ConfigServiceImpl implements ConfigService {

  private static final String APP_CONFIG_GP_PREFIX = "biometric.api.config";
  private static final String ADDRESS_HIERARCHY_NOT_FOUND =
      "The address hierarchy can not be found";

  private static final String DOT = ".";
  private static final String CONFIG = "config";
  private static final String ADDRESS_HIERARCHY = "addressHierarchy";
  private static final String LOCALIZATION = "localization";
  private static final String LOCATIONS = "locations";
  private static final String LICENCE_TYPE_ERROR =
      "The license type %s not configured " + "correctly in the backend";
  private static final String SYNC_COMPLETED_DATE = "SYNC_COMPLETED_DATE";
  private static final String SYNC_COMPLETED_DATE_ERROR =
      "No device attribute of type " + "SYNC_COMPLETED_DATE found";
  private static final String VACCINE_SCHEDULE = "vaccineSchedule";
  private static final String CLUSTER_ATTRIBUTE_TYPE = "cluster";
  private static final String ISOCODE_ATTRIBUTE_TYPE = "countryCode";
  private static final String SITE_CODE_ATTRIBUTE_TYPE = "siteCode";
  private static final String SUBSTANCES_CONFIG_ALIAS = "substancesConfig";

  private DeviceService deviceService;

  private LicenseService licenseService;

  private LicenseTypeService licenseTypeService;

  @Override
  public Set<String> retrieveAddressHierarchy() throws BiometricApiException {

    AddressHierarchyService addressHierarchyService =
        Context.getService(AddressHierarchyService.class);
    List<AddressHierarchyEntry> entries =
        addressHierarchyService.getAddressHierarchyEntriesAtTopLevel();

    if (entries == null || entries.isEmpty()) {
      throw new BiometricApiException(ADDRESS_HIERARCHY_NOT_FOUND);
    }
    Set<String> addresses = new HashSet<>();
    for (AddressHierarchyEntry entry : entries) {
      addresses.addAll(addressHierarchyService.getPossibleFullAddresses(entry));
    }
    return addresses;
  }

  @Override
  public String retrieveConfig(String name) throws EntityNotFoundException {
    String gpName = APP_CONFIG_GP_PREFIX + DOT + name;
    String configValue = Context.getAdministrationService().getGlobalProperty(gpName);
    if (null == configValue || configValue.isEmpty()) {
      throw new EntityNotFoundException(
          String.format("The configuration %s cannot be found", gpName));
    }
    return configValue;
  }

  @Override
  public Map<String, List<LocationResponse>> retrieveLocations() {
    LocationService locationService = Context.getLocationService();
    List<Location> locations = locationService.getAllLocations(false);
    List<LocationResponse> responses = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    for (Location location : locations) {
      LocationResponse response = new LocationResponse();
      Collection<LocationAttribute> attributes = location.getActiveAttributes();
      for (LocationAttribute attribute : attributes) {
        if (CLUSTER_ATTRIBUTE_TYPE.equalsIgnoreCase(attribute.getAttributeType().getName())) {
          response.setCluster(attribute.getValue().toString());
        }
        if (ISOCODE_ATTRIBUTE_TYPE.equalsIgnoreCase(attribute.getAttributeType().getName())) {
          response.setCountryCode(attribute.getValue().toString());
        }
        if (SITE_CODE_ATTRIBUTE_TYPE.equalsIgnoreCase(attribute.getAttributeType().getName())) {
          response.setSiteCode(attribute.getValue().toString());
        }
      }
      response.setUuid(location.getUuid());
      response.setName(location.getName());
      response.setCountry(location.getCountry());
      responses.add(response);
    }
    return Collections.singletonMap("results", responses);
  }

  @Override
  public List<SyncConfigResponse> retrieveAllConfigUpdates()
      throws IOException, BiometricApiException {
    // TO-DO configurations can be loaded once and added to a map
    List<SyncConfigResponse> responseList = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    SyncConfigResponse configResponse = new SyncConfigResponse();
    AdministrationService administrationService = Context.getAdministrationService();
    ObjectMapper objectMapper = new ObjectMapper();

    String configGp = administrationService.getGlobalProperty(BiometricApiConstants.MAIN_CONFIG_GP);
    configResponse.setName(CONFIG);
    String md5HashConfig = SecurityUtil.getMd5Hash(objectMapper.readTree(configGp).toString());
    configResponse.setHash(md5HashConfig);
    responseList.add(configResponse);

    SyncConfigResponse locationResponse = new SyncConfigResponse();
    locationResponse.setName(LOCATIONS);
    String locationResponseInJson = objectMapper.writeValueAsString(retrieveLocations());
    locationResponse.setHash(SecurityUtil.getMd5Hash(locationResponseInJson));
    responseList.add(locationResponse);

    SyncConfigResponse addressHierarchyResponse = new SyncConfigResponse();
    addressHierarchyResponse.setName(ADDRESS_HIERARCHY);
    String addressHResponse = objectMapper.writeValueAsString(retrieveAddressHierarchy());
    addressHierarchyResponse.setHash(SecurityUtil.getMd5Hash(addressHResponse));
    responseList.add(addressHierarchyResponse);

    SyncConfigResponse localizationResponse = new SyncConfigResponse();
    String localizationGp =
        administrationService.getGlobalProperty(BiometricApiConstants.LOCALIZATION_GP);
    localizationResponse.setName(LOCALIZATION);
    String md5HashLocalization =
        SecurityUtil.getMd5Hash((objectMapper.readTree(localizationGp).toString()));
    localizationResponse.setHash(md5HashLocalization);
    responseList.add(localizationResponse);

    SyncConfigResponse vaccineScheduleResponse = new SyncConfigResponse();
    String vaccineScheduleGp =
        administrationService.getGlobalProperty(BiometricApiConstants.CFL_VACCINES);
    vaccineScheduleResponse.setName(VACCINE_SCHEDULE);
    String md5HashVaccineSchedule =
        SecurityUtil.getMd5Hash(objectMapper.readTree(vaccineScheduleGp).toString());
    vaccineScheduleResponse.setHash(md5HashVaccineSchedule);
    responseList.add(vaccineScheduleResponse);

    SyncConfigResponse substancesConfigResponse = new SyncConfigResponse();
    String substancesConfigGp =
        administrationService.getGlobalProperty(BiometricApiConstants.SUBSTANCES_CONFIG_GP);
    substancesConfigResponse.setName(SUBSTANCES_CONFIG_ALIAS);
    String md5HashSubstancesConfig =
        SecurityUtil.getMd5Hash(objectMapper.readTree(substancesConfigGp).toString());
    substancesConfigResponse.setHash(md5HashSubstancesConfig);
    responseList.add(substancesConfigResponse);

    return responseList;
  }

  @Override
  public List<LicenseResponse> retrieveLicense(String deviceId, Set<String> licenseTypes)
      throws BiometricApiException {

    List<LicenseResponse> responses = new ArrayList<>(BiometricApiConstants.INITIAL_SIZE);
    Device device = deviceService.getDeviceByMAC(deviceId, Boolean.FALSE);

    for (String licenseType : licenseTypes) {
      LicenseResponse response = new LicenseResponse();

      // check for null
      DeviceAttributeType type = deviceService.getDeviceAttributeType(licenseType);

      if (null == type) {
        throw new BiometricApiException(String.format(LICENCE_TYPE_ERROR, licenseType));
      }
      response.setType(type.getName());
      // existing device
      if (null != device) {
        DeviceAttribute deviceAttribute =
            deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());

        if (null != deviceAttribute) {
          // license already tagged to the device so return existing license
          License license = licenseService.getLicenseByUuid(deviceAttribute.getValueReference());
          response.setValue(license.getSerialNo());
        } else {
          setLicense(response, type, device);
        }
      } else {
        // New device then create device
        device = saveDevice(deviceId);
        setLicense(response, type, device);
      }
      responses.add(response);
    }
    return responses;
  }

  @Override
  public boolean releaseLicense(String deviceId, Set<String> licenseTypes)
      throws BiometricApiException {

    Device device = deviceService.getDeviceByMAC(deviceId, Boolean.FALSE);
    if (null == device) {
      throw new EntityNotFoundException("The device cannot be found");
    }

    for (String licenseType : licenseTypes) {
      DeviceAttributeType type = deviceService.getDeviceAttributeType(licenseType);

      if (null == type) {
        throw new BiometricApiException(String.format(LICENCE_TYPE_ERROR, licenseType));
      }

      DeviceAttribute deviceAttribute =
          deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());

      if (null == deviceAttribute) {
        throw new EntityNotFoundException(
            String.format("No license of type %s assigned to this device", licenseType));
      }
      voidAttribute(
          device, deviceAttribute, "Request received from the device to release the license");
    }
    return true;
  }

  @Override
  public void updateLastSyncDate(String deviceId, String siteId, Long dateSyncCompleted)
      throws BiometricApiException {

    DeviceAttributeType type = deviceService.getDeviceAttributeType(SYNC_COMPLETED_DATE);
    if (type == null) {
      throw new BiometricApiException(SYNC_COMPLETED_DATE_ERROR);
    }
    // check if device already exists, if not create a new one
    Device device = deviceService.getDeviceByMAC(deviceId, Boolean.FALSE);
    if (device == null) {
      device = saveDevice(deviceId);
    } else {
      // check if the pre existing device has any attributes
      DeviceAttribute deviceAttribute =
          deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());

      // if the device has any active attribute, then make the existing attribute void.
      if (null != deviceAttribute) {
        voidAttribute(
            device, deviceAttribute, "Updating the new sync date,making the old data void");
      }
    }
    addDeviceAttribute(device, type, String.valueOf(dateSyncCompleted));
  }

  public void setDeviceService(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  public void setLicenseService(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  public void setLicenseTypeService(LicenseTypeService licenseTypeService) {
    this.licenseTypeService = licenseTypeService;
  }

  private void setLicense(LicenseResponse response, DeviceAttributeType type, Device device) {
    License license =
        licenseService.getAnyFreeLicense(licenseTypeService.getLicenseType(type.getName()));
    // device exists but no license available
    if (null == license) {
      response.setValue(null);
    } else {
      // if license is available, set device attribute with license
      addDeviceAttribute(device, type, license.getUuid());
      response.setValue(license.getSerialNo());
    }
  }

  private void addDeviceAttribute(
      Device device, DeviceAttributeType deviceAttributeType, String value) {
    DeviceAttribute deviceAttribute = new DeviceAttribute();
    deviceAttribute.setAttributeType(deviceAttributeType);
    deviceAttribute.setValueReferenceInternal(value);
    device.addAttribute(deviceAttribute);
    deviceService.saveDevice(device);
  }

  private Device saveDevice(String deviceId) {
    Device newDeviceObj = new Device();
    newDeviceObj.setDeviceMac(deviceId);
    newDeviceObj.setName(deviceId);
    return deviceService.saveDevice(newDeviceObj);
  }

  private void voidAttribute(Device device, DeviceAttribute deviceAttribute, String reason) {
    deviceAttribute.setVoided(Boolean.TRUE);
    deviceAttribute.setVoidedBy(Context.getAuthenticatedUser());
    deviceAttribute.setVoidReason(reason);
    device.addAttribute(deviceAttribute);
    deviceService.saveDevice(device);
  }

  @Override
  public String retrieveVaccineSchedule() throws EntityNotFoundException {
    String gpName = BiometricApiConstants.CFL_VACCINES;
    String configValue = Context.getAdministrationService().getGlobalProperty(gpName);
    if (StringUtils.isBlank(configValue)) {
      throw new EntityNotFoundException(
          String.format("The configuration %s is either empty or not present", gpName));
    }
    return configValue;
  }
}
