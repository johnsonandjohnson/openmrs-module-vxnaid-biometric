/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.hibernate.Criteria;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.util.TestUtil;
import org.openmrs.module.licensemanagement.Device;
import org.openmrs.module.licensemanagement.DeviceAttribute;
import org.openmrs.module.licensemanagement.api.DeviceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class DeviceUserServiceTest {

  @Mock
  private DeviceService deviceService;

  @Mock
  private DbSessionFactory sessionFactory;

  @Mock
  private DbSession dbSession;

  @Mock
  private Criteria criteria;

  @Mock
  private LocationService locationService;

  @InjectMocks
  private DeviceUserServiceImpl deviceUserService;

  @Before
  public void setUp() throws IOException {
    PowerMockito.mockStatic(Context.class);
    when(Context.getLocationService()).thenReturn(locationService);
  }

  @Test
  public void saveDeviceName_whenDeviceHasNoAttributes() throws EntityNotFoundException {

    String deviceId = "asdfs";
    Device device = TestUtil.createDevice(deviceId);
    Location location = TestUtil.createLocation();
    LocationAttribute locationAttribute = TestUtil.createLocationAttribute("siteCode");
    locationAttribute.setVoided(true);
    location.addAttribute(locationAttribute);
    when(locationService.getLocationByUuid(anyString())).thenReturn(null);
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(Device.class)).thenReturn(criteria);
    when(criteria.uniqueResult()).thenReturn(0L);
    when(deviceService.getDeviceByMAC(anyString(), anyBoolean())).thenReturn(device);
    deviceUserService.saveDeviceName(deviceId, "newLocation", location);

    verify(deviceService, times(1)).saveDevice(any(Device.class));
  }

  @Test
  public void saveDeviceName_whenDeviceIsNull() throws EntityNotFoundException {

    String deviceId = "asdfs";
    Device device = TestUtil.createDevice(deviceId);
    device.setRetired(true);
    Location location = TestUtil.createLocation();
    LocationAttribute locationAttribute = TestUtil.createLocationAttribute("siteCode");
    location.addAttribute(locationAttribute);
    when(locationService.getLocationByUuid(anyString())).thenReturn(null);
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(Device.class)).thenReturn(criteria);
    when(criteria.uniqueResult()).thenReturn(0L);
    when(deviceService.getDeviceByMAC(anyString(), anyBoolean())).thenReturn(device);
    deviceUserService.saveDeviceName(deviceId, "newLocation", location);

    verify(deviceService, times(1)).saveDevice(any(Device.class));
  }

  @Test
  public void saveDeviceName_whenDeviceListIsEmpty() throws EntityNotFoundException {

    String deviceId = "asdfs";
    Device device = TestUtil.createDevice(deviceId);
    device.setRetired(true);
    Location location = TestUtil.createLocation();
    LocationAttribute locationAttribute = TestUtil.createLocationAttribute("siteCode");
    location.addAttribute(locationAttribute);
    when(locationService.getLocationByUuid(anyString())).thenReturn(location);
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(Device.class)).thenReturn(criteria);
    when(criteria.uniqueResult()).thenReturn(0L);
    when(deviceService.getDeviceByMAC(anyString(), anyBoolean())).thenReturn(device);
    deviceUserService.saveDeviceName(deviceId, "newLocation", location);

    verify(deviceService, times(1)).saveDevice(any(Device.class));
  }

  @Test
  public void saveDeviceName_whenDeviceIsNotNull() throws EntityNotFoundException {

    String deviceId = "efgh";
    Device device = TestUtil.createDevice(deviceId);
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute("LOCATION_UUID");
    device.addAttribute(deviceAttribute);
    Location location = TestUtil.createLocation();
    LocationAttribute locationAttribute = TestUtil.createLocationAttribute("siteCode");
    location.addAttribute(locationAttribute);
    for (DeviceAttribute attribute : device.getActiveAttributes()) {
      if ("LOCATION_UUID".equalsIgnoreCase(attribute.getAttributeType().getName())) {
        attribute.setValue("12345");
      }
    }
    when(locationService.getLocationByUuid(anyString())).thenReturn(null);
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(Device.class)).thenReturn(criteria);
    when(criteria.uniqueResult()).thenReturn(0L);
    when(deviceService.getDeviceByMAC(anyString(), anyBoolean())).thenReturn(device);
    when(deviceService.saveDevice(any(Device.class))).thenReturn(device);
    deviceUserService.saveDeviceName(deviceId, "newLocation", location);

    verify(deviceService, times(2)).saveDevice(any(Device.class));
  }

  @Test
  public void saveDeviceName_whenLocationMatches() throws EntityNotFoundException {

    String deviceId = "efgh";
    Device device = TestUtil.createDevice(deviceId);
    DeviceAttribute deviceAttribute = TestUtil.createDeviceAttribute("LOCATION_UUID");
    device.addAttribute(deviceAttribute);
    Location location = TestUtil.createLocation();
    LocationAttribute locationAttribute = TestUtil.createLocationAttribute("siteCode");
    location.addAttribute(locationAttribute);
    location.setUuid("12345");
    for (DeviceAttribute attribute : device.getActiveAttributes()) {
      if ("LOCATION_UUID".equalsIgnoreCase(attribute.getAttributeType().getName())) {
        attribute.setValue("12345");
      }
    }
    when(locationService.getLocationByUuid(anyString())).thenReturn(null);
    when(sessionFactory.getCurrentSession()).thenReturn(dbSession);
    when(dbSession.createCriteria(Device.class)).thenReturn(criteria);
    when(criteria.uniqueResult()).thenReturn(0L);
    when(deviceService.getDeviceByMAC(anyString(), anyBoolean())).thenReturn(device);
    deviceUserService.saveDeviceName(deviceId, "newLocation", location);

    verify(deviceService, times(0)).saveDevice(any(Device.class));
  }
}
