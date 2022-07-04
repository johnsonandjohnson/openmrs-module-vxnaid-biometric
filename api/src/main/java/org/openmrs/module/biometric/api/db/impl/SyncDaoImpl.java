/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is
 * a trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.db.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.biometric.api.db.SyncDao;
import org.openmrs.module.biometric.api.helper.SyncQueryHelper;
import org.openmrs.module.biometric.api.model.SyncTemplateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * This class contains methods to retrieve information for sync api calls.
 *
 */
public class SyncDaoImpl implements SyncDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncDaoImpl.class);

  private static final String DATE_CREATED= "dateCreated";
  private static final String DATE_CHANGED = "dateChanged";
  private static final String DEVICE_ID = "deviceId";
  private static final String PATIENT_ID = "patientId";
  private static final String VISIT_ID = "visitId";
  private static final String PERSON_IMAGE_ATTRIBUTE = "PersonImageAttribute";
  private static final String PERSON_TEMPLATE_ATTRIBUTE = "PersonTemplateAttribute";
  private static final String LOCATIONS = "locations";
  private static final String ATTRIBUTE_TYPE_NAME = "attributeTypeName";

  private SessionFactory sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Patient> getAllPatientsByLocations(Date lastModifiedDate, int maxResultsToFetch, List<String> locations) {
    Criteria criteria = buildPatientLocationsCriteria(locations);
    if (null != lastModifiedDate) {
      criteria.add(
          Restrictions.or(Restrictions.ge(DATE_CHANGED, lastModifiedDate), Restrictions.ge(DATE_CREATED, lastModifiedDate)));
    }
    criteria.addOrder(Order.asc(DATE_CHANGED));
    criteria.addOrder(Order.asc(PATIENT_ID));
    criteria.setMaxResults(maxResultsToFetch);
    return criteria.list();
  }

  @Override
  public List<Object[]> getPatientCount(List<String> locations) {
    Criteria criteria = buildPatientLocationsCriteria(locations);

    @SuppressWarnings("unchecked")
    List<Object[]> list =
        criteria
            .setProjection(
                Projections.projectionList()
                    .add(Projections.groupProperty("voided"))
                    .add(Projections.rowCount()))
            .list();
    return list;
  }

  @Transactional(readOnly = true)
  @Override
  public Long getIgnoredCount(List<String> locations, String deviceId, String attributeType) {
    final Instant start = Instant.now();
    Query query = getQuery(SyncQueryHelper.getSyncIgnoredCountQuery());
    query.setParameterList(LOCATIONS, locations);
    query.setParameter(ATTRIBUTE_TYPE_NAME, attributeType);
    query.setParameter(DEVICE_ID, deviceId);
    Long ignoredCount = (Long) query.list().get(0);
    Instant end = Instant.now();
    LOGGER.debug("Ignored Count query completed : {}", Duration.between(start, end));
    return ignoredCount;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Patient> getPatientImageData(Date lastModifiedDate, String deviceId,
      List<String> locations,
      boolean optimize, int maxResultsToFetch) {
    Instant start = Instant.now();
    String sql = SyncQueryHelper.buildParticipantImageQuery();

    Query query = getSyncQuery(
        lastModifiedDate, deviceId, locations, optimize,
        maxResultsToFetch, sql, PERSON_IMAGE_ATTRIBUTE);

    List<Patient> patients = (List<Patient>) query.list();
    Instant end = Instant.now();
    LOGGER.debug("getPatientImageData {} : ", Duration.between(start, end));
    return patients;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SyncTemplateData> getPatientTemplateData(Date lastModifiedDate, String deviceId,
      List<String> locations,
      boolean optimize, int maxResultsToFetch) {
    Instant start = Instant.now();

    String sql = SyncQueryHelper.buildParticipantTemplatesQuery();

    Query query = getSyncQuery(lastModifiedDate, deviceId, locations, optimize, maxResultsToFetch,
        sql, PERSON_TEMPLATE_ATTRIBUTE);

    @SuppressWarnings("unchecked")
    List<SyncTemplateData> patients =
        query.setResultTransformer(Transformers.aliasToBean(SyncTemplateData.class)).list();

    Instant end = Instant.now();
    LOGGER.debug("getPatientTemplateData {} ", Duration.between(start, end));
    return patients;
  }

  @Override
  public List<Visit> getAllVisits(Date lastModifiedDate, int maxResultsToFetch,
      List<String> locations) {
    Criteria criteria = buildVisitLocationsCriteria(locations);
    if (null != lastModifiedDate) {
      criteria.add(Restrictions.ge(DATE_CHANGED, lastModifiedDate));
    }
    criteria.addOrder(Order.asc(DATE_CHANGED));
    criteria.addOrder(Order.asc(VISIT_ID));
    criteria.setMaxResults(maxResultsToFetch);
    return criteria.list();
  }

  @Override
  public List<Object[]> getVisitCount(List<String> locations) {
    Criteria criteria = buildVisitLocationsCriteria(locations);

    @SuppressWarnings("unchecked")
    List<Object[]> list =
        criteria
            .setProjection(
                Projections.projectionList()
                    .add(Projections.groupProperty("voided"))
                    .add(Projections.rowCount()))
            .list();
    return list;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Object[]> getPatientCountByLocationsAndAttribute(List<String> locations,
      String attributeType) {
    Instant start = Instant.now();
    Query query = getQuery(SyncQueryHelper.getSyncTotalCountQuery());
    query.setParameterList(LOCATIONS, locations);
    query.setParameter(ATTRIBUTE_TYPE_NAME, attributeType);
    Instant end = Instant.now();
    LOGGER.info(
        "getPatientCountByLocationsAndAttribute call execution time : {}",
        Duration.between(start, end));
    return query.list();
  }

  private Query getQuery(String query) {
    final Session session = this.sessionFactory.getCurrentSession();
    return session.createQuery(query);
  }

  private Query getSyncQuery(Date lastModifiedDate, String deviceId, List<String> locations,
      boolean optimize, int maxResultsToFetch, String baseSql, String attributeType) {
    String sql = baseSql;
    if (null != lastModifiedDate) {
      sql = sql + " and person.dateChanged >= :dateChanged";
    }
    if (optimize) {
      sql = sql + " and attribute.value <> :deviceId";
    }
    sql = sql + " order by person.dateChanged,person.id";
    Query query = getQuery(sql);
    if (null != lastModifiedDate) {
      query.setParameter(DATE_CHANGED, lastModifiedDate);
    }
    if (optimize) {
      query.setParameter(DEVICE_ID, deviceId);
    }
    query.setParameter(ATTRIBUTE_TYPE_NAME, attributeType);
    query.setParameterList(LOCATIONS, locations);
    query.setMaxResults(maxResultsToFetch);
    return query;
  }

  private Criteria buildPatientLocationsCriteria(List<String> locations) {
    final Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
    criteria.createAlias("attributes", "attribute", JoinType.INNER_JOIN);
    criteria.createAlias("attribute.attributeType", "attributeType", JoinType.INNER_JOIN);
    criteria.add(Restrictions.eq("attributeType.name", "LocationAttribute"));
    criteria.add(Restrictions.in("attribute.value", locations));
    return criteria;
  }

  private Criteria buildVisitLocationsCriteria(List<String> locations) {
    final Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
    criteria.createAlias("location", "location", JoinType.INNER_JOIN);
    criteria.createAlias("visitType", "visitType", JoinType.INNER_JOIN);
    criteria.add(Restrictions.eq("visitType.name", "Dosing"));
    criteria.add(Restrictions.in("location.uuid", locations));
    return criteria;
  }
}
