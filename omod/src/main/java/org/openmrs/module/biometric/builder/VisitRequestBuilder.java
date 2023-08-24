/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.biometric.builder;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.contract.ParticipantAttribute;
import org.openmrs.module.biometric.contract.VisitRequest;
import org.openmrs.module.biometric.util.BiometricModUtil;
import org.openmrs.module.biometric.util.LocationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Visit Request Builder. */
@Component
public class VisitRequestBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(VisitRequestBuilder.class);

  @Autowired private BiometricModUtil util;

  @Autowired private LocationUtil locationUtil;

  @Autowired private VisitService visitService;

  @Autowired private PatientService patientService;

  /**
   * Creates visit entity from visit request.
   *
   * @param request visit request
   * @return Visit @see org.openmrs.Visit
   */
  public Visit createFrom(VisitRequest request) throws BiometricApiException, ParseException {
    Visit newVisit = new Visit();
    newVisit.setUuid(request.getVisitUuid());
    Patient patient = patientService.getPatientByUuid(request.getParticipantUuid());
    newVisit.setPatient(patient);
    newVisit.setVisitType(getVisitType(request.getVisitType()));

    Date startDatetime = util.convertIsoStringToDate(request.getStartDatetime());
    newVisit.setStartDatetime(startDatetime);
    newVisit.setDateCreated(new Date());
    newVisit.setDateChanged(new Date());
    return createFrom(request, newVisit);
  }

  /**
   * Creates visit entity from visit request and visit.
   *
   * @param request visit request
   * @param visit @see Visit
   * @return Visit @see org.openmrs.Visit
   */
  public Visit createFrom(VisitRequest request, Visit visit) throws EntityNotFoundException {
    visit.setLocation(locationUtil.getLocationByUuid(request.getLocationUuid()));

    // update visit attributes. if not exists then create otherwise update.
    if (null != request.getAttributes() && !request.getAttributes().isEmpty()) {
      LOGGER.debug("No. of attributes : {}", request.getAttributes().size());
      Set<VisitAttribute> attributes = setVisitAttributes(request, visit);
      if (null == request.getVisitUuid()) {
        visit.setAttributes(attributes);
      }
    }
    return visit;
  }

  private Set<VisitAttribute> setVisitAttributes(VisitRequest request, Visit visit)
      throws EntityNotFoundException {
    List<VisitAttributeType> visitAttributeTypes = visitService.getAllVisitAttributeTypes();
    Set<VisitAttribute> attributes = new HashSet<>();
    for (ParticipantAttribute attribute : request.getAttributes()) {
      VisitAttribute visitAttribute = new VisitAttribute();
      VisitAttributeType visitAttributeType =
          getVisitAttributeTypeByName(visitAttributeTypes, attribute.getType());
      if (null == visitAttributeType) {
        throw new EntityNotFoundException(
            String.format("Invalid visit attribute type : %s", attribute.getType()));
      }

      visitAttribute.setAttributeType(visitAttributeType);
      visitAttribute.setValue(attribute.getValue());
      visitAttribute.setOwner(visit);
      visitAttribute.setCreator(Context.getAuthenticatedUser());
      // for update request
      if (null != request.getVisitUuid()) {
        visit.setAttribute(visitAttribute);
        visit.setDateChanged(new Date());
        visit.setChangedBy(Context.getAuthenticatedUser());
      } else {
        attributes.add(visitAttribute);
      }
    }
    return attributes;
  }

  /**
   * Fetches visit attribute type by name
   *
   * @param visitAttributeTypes list of visit attribute types
   * @param name name of the attribute type
   * @return VisitAttributeType @see org.openmrs.VisitAttributeType
   */
  private VisitAttributeType getVisitAttributeTypeByName(
      List<VisitAttributeType> visitAttributeTypes, String name) {
    for (VisitAttributeType attributeType : visitAttributeTypes) {
      if (attributeType.getName().equalsIgnoreCase(name)) {
        return attributeType;
      }
    }
    return null;
  }

  private VisitType getVisitType(String visitTypeName) throws BiometricApiException {
    List<VisitType> visitTypesForName = visitService.getVisitTypes(visitTypeName);

    if (visitTypesForName.isEmpty()) {
      throw new BiometricApiException("Missing Visit Type for name: " + visitTypeName);
    } else if (visitTypesForName.size() > 1) {
      throw new BiometricApiException("More then one Visit Type for name: " + visitTypeName);
    }

    return visitTypesForName.get(0);
  }
}
