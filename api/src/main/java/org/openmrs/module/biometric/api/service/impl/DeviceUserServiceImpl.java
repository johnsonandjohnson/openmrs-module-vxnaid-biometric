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

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.service.DeviceUserService;
import org.openmrs.module.licensemanagement.Device;
import org.openmrs.module.licensemanagement.DeviceAttribute;
import org.openmrs.module.licensemanagement.DeviceAttributeType;
import org.openmrs.module.licensemanagement.api.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The implementation class for DeviceUserService.
 */
public class DeviceUserServiceImpl implements DeviceUserService {

  private static final String LOCATION_UUID = "LOCATION_UUID";

  @Autowired
  private DeviceService deviceService;

  @Override
  public String saveDeviceName(String deviceId, String siteCode, Location location) {

    long deviceCount = deviceService.getDeviceCount(true);
    long siteDeviceSequeneNumber = deviceCount + 1;
    String deviceName = siteCode + '-' + siteDeviceSequeneNumber;

    Device device = deviceService.getDeviceByMAC(deviceId, Boolean.FALSE);

    // check if the device with the same id has different location than the one currently passed ,
    // if the location is different, retire the existing device and create a new device with the same
    // device id and new location passed.
    if (null != device) {
      for (DeviceAttribute attribute : device.getActiveAttributes()) {
        if (LOCATION_UUID.equalsIgnoreCase(attribute.getAttributeType().getName())
            && !attribute.getValue().toString().equalsIgnoreCase(location.getUuid())) {
          retireDevice(device, "Device registered in a new location");
          return saveDevice(null, location, deviceName, deviceId);
        } else if (LOCATION_UUID.equalsIgnoreCase(attribute.getAttributeType().getName())
            && attribute.getValue().toString().equalsIgnoreCase(location.getUuid())) {
          /* if device id and location match then return the existing device name */
          return device.getName();
        }
      }
    }
    //New device
    return saveDevice(device, location, deviceName, deviceId);
  }

  private void retireDevice(Device device, String reason) {
    device.setRetired(Boolean.TRUE);
    device.setRetireReason(reason);
    device.setRetiredBy(Context.getAuthenticatedUser());
    device.setDateRetired(new Date());
    deviceService.saveDevice(device);
  }

  private String saveDevice(Device device1, Location location, String deviceName, String deviceId) {
    Device device = device1;
    if (null == device) {
      device = new Device();
      device.setDeviceMac(deviceId);
    }
    device.setName(deviceName);
    DeviceAttributeType type = deviceService.getDeviceAttributeType(LOCATION_UUID);
    DeviceAttribute deviceAttribute = new DeviceAttribute();
    deviceAttribute.setAttributeType(type);
    deviceAttribute.setValueReferenceInternal(location.getUuid());
    device.addAttribute(deviceAttribute);
    deviceService.saveDevice(device);
    return deviceName;
  }
}
