/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Address contract used in creating person entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

  private String countyDistrict;
  private String display;
  private boolean preferred;
  private String address1;
  private String address2;
  private String cityVillage;
  private String country;
  private String postalCode;
  private String stateProvince;

  public boolean isPreferred() {
    return preferred;
  }

  public void setPreferred(boolean preferred) {
    this.preferred = preferred;
  }

  public String getAddress1() {
    return address1;
  }

  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  public String getAddress2() {
    return address2;
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  public String getCityVillage() {
    return cityVillage;
  }

  public void setCityVillage(String cityVillage) {
    this.cityVillage = cityVillage;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getDisplay() {
    return display;
  }

  public void setDisplay(String display) {
    this.display = display;
  }

  public String getStateProvince() {
    return stateProvince;
  }

  public void setStateProvince(String stateProvince) {
    this.stateProvince = stateProvince;
  }

  public String getCountyDistrict() {
    return countyDistrict;
  }

  public void setCountyDistrict(String countyDistrict) {
    this.countyDistrict = countyDistrict;
  }
}
