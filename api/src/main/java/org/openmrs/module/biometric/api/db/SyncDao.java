/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.db;

import java.util.Date;
import java.util.List;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.biometric.api.model.SyncTemplateData;

/**
 * Defines the methods to retrieve sync data information.
 */
public interface SyncDao {

  /**
   * Retrieve all the patients for the given sites, modified date.
   *
   * @param lastModifiedDate retrieve records after the modified date
   * @param maxResultsToFetch max results to fetch
   * @param locations list of sites
   * @return list of patients
   */
  List<Patient> getAllPatientsByLocations(Date lastModifiedDate, int maxResultsToFetch,
      List<String> locations);

  /**
   * Retrieve the patients count for the given sites.
   *
   * @param locations list of sites
   * @return patient count based on the match criteria
   */
  List<Object[]> getPatientCount(List<String> locations);

  /**
   * Get the count of patient for the given attribute.
   *
   * @param locations list of sites
   * @param deviceId device id from where the patient was enrolled
   * @param attributeType attribute value contains the device id
   * @return patient count based on the match criteria
   */
  Long getIgnoredCount(List<String> locations, String deviceId, String attributeType);

  /**
   * Retrieves patients with images.
   *
   * @param lastModifiedDate retrieve records after the modified date
   * @param deviceId device id from where the patient was enrolled
   * @param locations list of sites
   * @param optimize true, will skip the patient images captured on the device, false will include
   * @param maxResultsToFetch maximum results to fetch
   * @return list of patient images
   */
  List<Patient> getPatientImageData(Date lastModifiedDate, String deviceId, List<String> locations,
      boolean optimize, int maxResultsToFetch);

  /**
   * Get patient count for the given sites and attribute type.
   *
   * @param locations list of sites
   * @param attributeType attribute type name
   * @return patient count
   */
  List<Object[]> getPatientCountByLocationsAndAttribute(List<String> locations,
      String attributeType);

  /**
   * Retrieves patients with biometric template.
   *
   * @param lastModifiedDate retrieve records after the modified date
   * @param deviceId device id from where the patient was enrolled
   * @param locations list of sites
   * @param optimize true will skip the patient images captured on the device, false will include
   * @param maxResultsToFetch maximum results to fetch
   * @return patients with biometric template
   */
  List<SyncTemplateData> getPatientTemplateData(Date lastModifiedDate, String deviceId,
      List<String> locations,
      boolean optimize, int maxResultsToFetch);

  /**
   * Retrieves all the dosing visits based on the given criteria.
   *
   * @param lastModifiedDate retrieve records after the modified date
   * @param maxResultsToFetch maximum results to fetch
   * @param locations list of sites
   * @return dosing visits
   */
  List<Visit> getAllVisits(Date lastModifiedDate, int maxResultsToFetch, List<String> locations);

  /**
   * Retrieves the dosing visit count for th given sites.
   *
   * @param locations list of sites
   * @return dosing visit count
   */
  List<Object[]> getVisitCount(List<String> locations);
}
