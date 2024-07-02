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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.LocationResponse;
import org.openmrs.module.biometric.api.contract.SyncConfigResponse;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.util.TestUtil;
import org.openmrs.module.licensemanagement.Device;
import org.openmrs.module.licensemanagement.DeviceAttribute;
import org.openmrs.module.licensemanagement.DeviceAttributeType;
import org.openmrs.module.licensemanagement.License;
import org.openmrs.module.licensemanagement.api.DeviceService;
import org.openmrs.module.licensemanagement.api.LicenseService;
import org.openmrs.module.licensemanagement.api.LicenseTypeService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** Tests on the methods found in ConfigService */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Context.class})
public class ConfigServiceTest {

  private static final String INVALID_ADDRESS_FIELD_ERROR = "Addres hierarchy not found";
  private static final String APP_CONFIG_GP_PREFIX = "biometric.api.config";
  private static final String BIOMETRIC_CONFIGURATION_NOT_FOUND =
      "Could not found biometric configuration";
  private static final String DOT = ".";
  private static final String ERROR_KEY = "error_key";
  private static final String BIOMETRIC_EXTRACTOR_LICENSE = "biometric.extractor.license";
  private static final String SYNC_COMPLETED_DATE = "SYNC_COMPLETED_DATE";

  @Mock private AddressHierarchyService addressHierarchyService;

  @Mock private AdministrationService administrationService;

  @Mock private PatientService patientService;

  @Mock private LocationService locationService;

  @Mock private VisitService visitService;

  @Mock private MessageSourceService messageSourceService;

  @Mock private DeviceService deviceService;

  @Mock private LicenseService licenseService;

  @Mock private LicenseTypeService licenseTypeService;

  @InjectMocks private ConfigServiceImpl configService;

  private String name;

  @Before
  public void setup() {
    PowerMockito.mockStatic(Context.class);
    given(Context.getService(AddressHierarchyService.class)).willReturn(addressHierarchyService);
    given(Context.getAdministrationService()).willReturn(administrationService);
  }

  @Test
  public void retrieveConfig_shouldRetrieveBiometricConfiguration1IfValidNameIsPassed()
      throws EntityNotFoundException {
    // Given
    name = "config1";
    given(administrationService.getGlobalProperty(APP_CONFIG_GP_PREFIX + DOT + name))
        .willReturn(TestUtil.getConfiguratoins1());
    // When
    String configuration = configService.retrieveConfig(name);
    // Then
    Assert.assertTrue(configuration.contains("Kannada"));

    verifyConfigInteractions();
  }

  @Test
  public void retrieveConfig_shouldRetrieveBiometricConfiguration2IfValidNameIsPassed()
      throws EntityNotFoundException {
    // Given
    name = "config2";
    given(administrationService.getGlobalProperty(APP_CONFIG_GP_PREFIX + DOT + name))
        .willReturn(TestUtil.getConfiguratoins2());
    // When
    String configuration = configService.retrieveConfig(name);
    // Then
    Assert.assertTrue(configuration.contains("Covid 1D vaccine"));

    verifyConfigInteractions();
  }

  @Test
  public void retrieveConfig_shouldThrowAPIExceptionIfInvalidNameIsPassed() {
    // Given
    name = "invalid";
    given(administrationService.getGlobalProperty(APP_CONFIG_GP_PREFIX + DOT + name))
        .willReturn("");
    try {
      // When
      String configuration = configService.retrieveConfig(name);
      Assert.fail("should throw APIException");
    } catch (EntityNotFoundException e) {
      // Then
      verifyConfigInteractions();
    }
  }

  @Test
  public void updateLastSyncDate_shouldUpdateSyncDateWithNewDevice() throws BiometricApiException {
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    when(deviceService.getDeviceAttributeType(SYNC_COMPLETED_DATE)).thenReturn(type);
    when(deviceService.saveDevice(any(Device.class))).thenReturn(device);
    String siteId = "abcd-1234";
    Long dateSyncCompleted = System.currentTimeMillis();

    try {
      configService.updateLastSyncDate(deviceId, siteId, dateSyncCompleted);
    } finally {
      verify(deviceService, times(1)).getDeviceAttributeType(SYNC_COMPLETED_DATE);
      verify(deviceService, times(2)).saveDevice(any(Device.class));
      verify(deviceService, times(1)).getDeviceByMAC(device.getDeviceMac(), false);
      verify(deviceService, never()).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
    }
  }

  @Test
  public void updateLastSyncDate_shouldUpdateLastSyncDateForDeviceWithoutAttributeType()
      throws BiometricApiException {

    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDevice(deviceId);
    Device device1 = TestUtil.createDeviceWithAttributes(deviceId);
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute();
    when(deviceService.getDeviceAttributeType(SYNC_COMPLETED_DATE)).thenReturn(type);
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(null);
    when(deviceService.saveDevice(device)).thenReturn(device1);
    String siteId = "abcd-1234";
    Long dateSyncCompleted = System.currentTimeMillis();
    try {
      configService.updateLastSyncDate(deviceId, siteId, dateSyncCompleted);
    } finally {
      verify(deviceService, times(1)).saveDevice(device);
      verify(deviceService, times(1)).getDeviceAttributeType(SYNC_COMPLETED_DATE);
      verify(deviceService, times(1)).getDeviceByMAC(device.getDeviceMac(), false);
      verify(deviceService, times(1)).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
    }
  }

  @Test(expected = BiometricApiException.class)
  public void updateLastSyncDate_shouldUpdateSyncDateForDeviceWithoutAttributeType()
      throws BiometricApiException {

    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDevice(deviceId);
    Device device1 = TestUtil.createDeviceWithAttributes(deviceId);
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute();
    when(deviceService.getDeviceAttributeType(SYNC_COMPLETED_DATE)).thenReturn(null);
    when(deviceService.getDeviceByMAC(deviceId)).thenReturn(device);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(deviceAttribute);
    when(deviceService.saveDevice(device)).thenReturn(device1);
    String siteId = "abcd-1234";
    Long dateSyncCompleted = System.currentTimeMillis();
    try {
      configService.updateLastSyncDate(deviceId, siteId, dateSyncCompleted);
      fail("should throw APIException");
    } finally {

      verify(deviceService, times(1)).getDeviceAttributeType(SYNC_COMPLETED_DATE);
      verify(deviceService, never()).saveDevice(device);
      verify(deviceService, never()).getDeviceByMAC(device.getDeviceMac());
      verify(deviceService, never()).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
    }
  }

  @Test
  public void updateLastSyncDate_shouldUpdateLastSyncDateForDeviceWithAttributes()
      throws BiometricApiException {

    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    Device device1 = TestUtil.createDeviceWithAttributes(deviceId);
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute();
    when(deviceService.getDeviceAttributeType(SYNC_COMPLETED_DATE)).thenReturn(type);
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(deviceAttribute);
    when(deviceService.saveDevice(any(Device.class))).thenReturn(device1);
    String siteId = "abcd-1234";
    Long dateSyncCompleted = System.currentTimeMillis();
    try {
      configService.updateLastSyncDate(deviceId, siteId, dateSyncCompleted);
    } finally {

      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(SYNC_COMPLETED_DATE);
      verify(deviceService, times(1)).getDeviceByMAC(anyString(), anyBoolean());
    }
  }

  @Test(expected = BiometricApiException.class)
  public void releaseLicense_shouldThrowExceptionWhenNullDevice() throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Set<String> licenseTypes = new HashSet<>(Arrays.asList("IRIS_MATCHING", "IRIS_CLIENT"));
    Device device = TestUtil.createDevice(deviceId);
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    try {
      configService.releaseLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, never()).saveDevice(any(Device.class));
      verify(deviceService, never()).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
      verify(deviceService, never()).getDeviceAttributeType(licenseTypes.iterator().next());
    }
  }

  @Test(expected = BiometricApiException.class)
  public void releaseLicense_shouldThrowExceptionWhenNullAttributeType()
      throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDevice(deviceId);
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    Set<String> licenseTypes = new HashSet<>(Arrays.asList("IRIS_MATCHING"));
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(null);
    try {
      configService.releaseLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(0)).getDeviceAttributeType(licenseTypes.iterator().next());
      verify(deviceService, never()).saveDevice(any(Device.class));
      verify(deviceService, never()).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
    }
  }

  @Test(expected = BiometricApiException.class)
  public void releaseLicense_shouldThrowExceptionWhenNullAttribute() throws BiometricApiException {

    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute();
    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(type);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(null);
    try {
      configService.releaseLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
      verify(deviceService, times(1)).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
      verify(deviceService, never()).saveDevice(any(Device.class));
    }
  }

  @Test
  public void releaseLicense_shouldReleaseLicense() throws BiometricApiException {

    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute();
    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(type);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(deviceAttribute);
    try {
      configService.releaseLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
      verify(deviceService, times(1)).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
      verify(deviceService, times(1)).saveDevice(any(Device.class));
    }
  }

  @Test(expected = BiometricApiException.class)
  public void retrieveLicense_shouldThrowExceptionWithNullDeviceAttributeType()
      throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Device device = TestUtil.createDevice(deviceId);
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(null);
    try {
      configService.retrieveLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
      verify(deviceService, never()).saveDevice(any(Device.class));
      verify(deviceService, never()).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
    }
  }

  @Test
  public void retrieveLicense_shouldReturnLicenseWithNewDeviceAndNewLicense()
      throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    Device device = TestUtil.createDevice(deviceId);
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(type);
    when(licenseService.getAnyFreeLicense(
            licenseTypeService.getLicenseType(licenseTypes.iterator().next())))
        .thenReturn(null);
    when(deviceService.saveDevice(device)).thenReturn(device);
    try {
      configService.retrieveLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
      verify(deviceService, times(1)).saveDevice(any(Device.class));
      verify(licenseTypeService, times(1)).getLicenseType(licenseTypes.iterator().next());
      verify(licenseService, times(1))
          .getAnyFreeLicense(licenseTypeService.getLicenseType(licenseTypes.iterator().next()));
    }
  }

  @Test
  public void retrieveLicense_shouldRetrieveLicenseWithNewDeviceAndOldLicense()
      throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    License license = TestUtil.createLicense();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(type);
    when(licenseService.getAnyFreeLicense(
            licenseTypeService.getLicenseType(licenseTypes.iterator().next())))
        .thenReturn(license);
    when(deviceService.saveDevice(any(Device.class))).thenReturn(device);
    try {
      configService.retrieveLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
      verify(deviceService, times(2)).saveDevice(any(Device.class));
      verify(licenseTypeService, times(1)).getLicenseType(licenseTypes.iterator().next());
      verify(licenseService, times(1))
          .getAnyFreeLicense(licenseTypeService.getLicenseType(licenseTypes.iterator().next()));
    }
  }

  @Test
  public void retrieveLicense_shouldRetriveLicenseWithNoAttribute() throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    License license = TestUtil.createLicense();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(null);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(type);
    try {
      configService.retrieveLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
      verify(deviceService, times(1)).getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid());
      // verify(licenseTypeService, times(1)).getLicenseType(licenseTypes.iterator().next());
      verify(licenseService, times(1))
          .getAnyFreeLicense(licenseTypeService.getLicenseType(licenseTypes.iterator().next()));
    }
  }

  @Test
  public void retrieveLicense_shouldRetrieveLicenseWithDeviceAttributeAndLicense()
      throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute();
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    License license = TestUtil.createLicense();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(deviceAttribute);
    when(licenseService.getLicenseByUuid(anyString())).thenReturn(license);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(type);
    when(licenseService.getAnyFreeLicense(
            licenseTypeService.getLicenseType(licenseTypes.iterator().next())))
        .thenReturn(license);
    try {
      configService.retrieveLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
    }
  }

  @Test
  public void retrieveLicense_shouldRetieveLicense() throws BiometricApiException {

    String deviceId = "XYZ-1234";
    Set<String> licenseTypes = Collections.singleton("IRIS_MATCHING");
    DeviceAttributeType type = TestUtil.createDeviceAttributeType();
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute();
    Device device = TestUtil.createDeviceWithAttributes(deviceId);
    License license = TestUtil.createLicense();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    when(deviceService.getDeviceAttributeByDeviceAndTypeUuid(device, type.getUuid()))
        .thenReturn(deviceAttribute);
    when(deviceService.getDeviceAttributeType(licenseTypes.iterator().next())).thenReturn(type);
    when(licenseService.getLicenseByUuid(deviceAttribute.getValueReference())).thenReturn(license);
    try {
      configService.retrieveLicense(deviceId, licenseTypes);
    } finally {
      verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
      verify(deviceService, times(1)).getDeviceAttributeType(licenseTypes.iterator().next());
    }
  }

  /* @Test
  public void retrieveLicense_shouldRetrieveLicense() {
      //Given
      given(administrationService.getGlobalProperty(BIOMETRIC_EXTRACTOR_LICENSE)).willReturn(TestUtil.getLicense());
      //When
      String license = configService.retrieveLicense("", Collections.emptyList());
      //Then
      Assert.assertTrue(license.contains("IRIS"));
      verify(administrationService, times(1)).getGlobalProperty(BIOMETRIC_EXTRACTOR_LICENSE);
  }*/

  @Test
  public void retrieveLocations_shouldReturnAllLocations() {
    Location location = TestUtil.createLocation();
    List<Location> locationList = Arrays.asList(location);
    when(Context.getLocationService()).thenReturn(locationService);
    when(locationService.getAllLocations(anyBoolean())).thenReturn(locationList);
    Map<String, List<LocationResponse>> responses = configService.retrieveLocations();
    assertEquals(1, responses.size());
  }

  @Test
  public void retrieveAddressHierarchy_shouldReturnAddressHierarchy() throws BiometricApiException {
    when(Context.getService(AddressHierarchyService.class)).thenReturn(addressHierarchyService);
    List<AddressHierarchyEntry> addressHierarchyEntryList = TestUtil.getAddressHierarchyEntryList();
    when(addressHierarchyService.getAddressHierarchyEntriesAtTopLevel())
        .thenReturn(addressHierarchyEntryList);
    when(addressHierarchyService.getPossibleFullAddresses(any(AddressHierarchyEntry.class)))
        .thenReturn(Arrays.asList("Belgium"));
    Set<String> address = configService.retrieveAddressHierarchy();
    assertEquals(1, address.size());
    verify(addressHierarchyService, times(7))
        .getPossibleFullAddresses(any(AddressHierarchyEntry.class));
    verify(addressHierarchyService, times(1)).getAddressHierarchyEntriesAtTopLevel();
  }

  @Test(expected = BiometricApiException.class)
  public void retrieveAddressHierarchy_shouldThrowExceptionWhenAddressHierarchyEntryIsEmpty()
      throws BiometricApiException {
    when(Context.getService(AddressHierarchyService.class)).thenReturn(addressHierarchyService);
    List<AddressHierarchyEntry> addressHierarchyEntryList = TestUtil.getAddressHierarchyEntryList();
    when(addressHierarchyService.getAddressHierarchyEntriesAtTopLevel())
        .thenReturn(new ArrayList<>());
    Set<String> address = configService.retrieveAddressHierarchy();
    assertEquals(0, address.size());
    verify(addressHierarchyService, times(0))
        .getPossibleFullAddresses(any(AddressHierarchyEntry.class));
    verify(addressHierarchyService, times(1)).getAddressHierarchyEntriesAtTopLevel();
  }

  @Test(expected = BiometricApiException.class)
  public void retrieveAddressHierarchy_shouldThrowExceptionWhenAddressHierarchyEntryIsNull()
      throws BiometricApiException {
    when(Context.getService(AddressHierarchyService.class)).thenReturn(addressHierarchyService);
    List<AddressHierarchyEntry> addressHierarchyEntryList = TestUtil.getAddressHierarchyEntryList();
    when(addressHierarchyService.getAddressHierarchyEntriesAtTopLevel()).thenReturn(null);
    Set<String> address = configService.retrieveAddressHierarchy();
    assertEquals(0, address.size());
    verify(addressHierarchyService, times(0))
        .getPossibleFullAddresses(any(AddressHierarchyEntry.class));
    verify(addressHierarchyService, times(1)).getAddressHierarchyEntriesAtTopLevel();
  }

  @Test
  public void retrieveAllConfigUpdates_shouldRetrieveAllConfig()
      throws BiometricApiException, IOException {
    String configGp = "12345";
    given(
            Context.getAdministrationService()
                .getGlobalProperty(BiometricApiConstants.MAIN_CONFIG_GP))
        .willReturn("12345");
    given(
            Context.getAdministrationService()
                .getGlobalProperty(BiometricApiConstants.LOCALIZATION_GP))
        .willReturn("123");
    given(Context.getAdministrationService().getGlobalProperty(BiometricApiConstants.CFL_VACCINES))
        .willReturn("6789");
    given(
            Context.getAdministrationService()
                .getGlobalProperty(BiometricApiConstants.SUBSTANCES_CONFIG_GP))
        .willReturn("[{}]");
    given(
            Context.getAdministrationService()
                .getGlobalProperty(BiometricApiConstants.SUBSTANCE_GROUPS_GP))
        .willReturn("[{}]");
    given(Context.getLocationService()).willReturn(locationService);
    Location location = TestUtil.createLocation();
    when(locationService.getAllLocations(anyBoolean())).thenReturn(Arrays.asList(location));
    given(Context.getService(AddressHierarchyService.class)).willReturn(addressHierarchyService);
    List<AddressHierarchyEntry> addressHierarchyEntryList = TestUtil.getAddressHierarchyEntryList();
    when(addressHierarchyService.getAddressHierarchyEntriesAtTopLevel())
        .thenReturn(addressHierarchyEntryList);
    when(addressHierarchyService.getPossibleFullAddresses(any(AddressHierarchyEntry.class)))
        .thenReturn(Collections.singletonList("MyCity"));
    List<SyncConfigResponse> responseList = configService.retrieveAllConfigUpdates();

    assertThat(responseList, hasSize(7));
    verify(locationService, times(1)).getAllLocations(anyBoolean());
    verify(addressHierarchyService, times(1)).getAddressHierarchyEntriesAtTopLevel();
    verify(addressHierarchyService, times(7))
        .getPossibleFullAddresses(any(AddressHierarchyEntry.class));
  }

  @Test(expected = BiometricApiException.class)
  public void retrieveAllConfigUpdates_shouldThrowExceptionWhenAddressHierarchyIsNull()
      throws BiometricApiException, IOException {
    String configGp = "12345";
    given(
            Context.getAdministrationService()
                .getGlobalProperty(BiometricApiConstants.MAIN_CONFIG_GP))
        .willReturn("12345");
    given(
            Context.getAdministrationService()
                .getGlobalProperty(BiometricApiConstants.LOCALIZATION_GP))
        .willReturn("123");
    given(Context.getLocationService()).willReturn(locationService);
    Location location = TestUtil.createLocation();
    when(locationService.getAllLocations(anyBoolean()))
        .thenReturn(Collections.singletonList(location));
    given(Context.getService(AddressHierarchyService.class)).willReturn(addressHierarchyService);
    List<AddressHierarchyEntry> addressHierarchyEntryList = TestUtil.getAddressHierarchyEntryList();
    when(addressHierarchyService.getAddressHierarchyEntriesAtTopLevel()).thenReturn(null);
    when(addressHierarchyService.getPossibleFullAddresses(any(AddressHierarchyEntry.class)))
        .thenReturn(Collections.singletonList("MyCity"));
    List<SyncConfigResponse> responseList = configService.retrieveAllConfigUpdates();
  }

  @Test(expected = EntityNotFoundException.class)
  public void retrieveVaccineSchedule_shouldThrowAPIExceptionIfGPNotFound()
      throws EntityNotFoundException {

    given(administrationService.getGlobalProperty("cfl.vaccines")).willReturn("");
    String configuration = configService.retrieveVaccineSchedule();
    Assert.fail("should throw APIException");
  }

  @Test
  public void retrieveVaccineSchedule_shouldReturnVaccineScheduleGPNotFound()
      throws EntityNotFoundException {

    given(administrationService.getGlobalProperty("cfl.vaccines")).willReturn("1234");
    String configuration = configService.retrieveVaccineSchedule();
    Assert.assertNotNull(configuration);
  }

  private void verifyInteractions() {
    verifyStatic(times(1));
    Context.getService(AddressHierarchyService.class);
    verify(addressHierarchyService, times(1)).getAddressHierarchyEntriesAtTopLevel();
  }

  private void verifyConfigInteractions() {
    verify(administrationService, times(1)).getGlobalProperty(APP_CONFIG_GP_PREFIX + DOT + name);
  }
}
