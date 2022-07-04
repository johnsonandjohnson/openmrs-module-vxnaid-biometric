package org.openmrs.module.biometric.util;

import static org.openmrs.module.biometric.constants.BiometricModConstants.LOCATION_ATTRIBUTE;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocationUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocationUtil.class);
  private static final String CLUSTER_ATTRIBUTE_TYPE = "cluster";

  /**
   *
   */
  public final List<String> findLocationsByCluster(String country, String clusterName) {
    LOGGER.info("loading location data for country : {} and cluster : {} ", country, clusterName);
    Map<String, List<String>> clusterLocationMap =
        loadClusterMap(Context.getLocationService().getRootLocations(false));
    return clusterLocationMap.get(clusterName.toLowerCase());
  }

  /**
   *
   */
  public final List<String> findLocationsByCountry(String country) {
    LOGGER.info("loading location data for country : {} ", country);
    Map<String, List<String>> countryLocationMap =
        loadCountriesMap(Context.getLocationService().getRootLocations(false));
    return countryLocationMap.get(country.toLowerCase());
  }

  public void validateSyncLocationData(SyncRequest request) throws EntityValidationException {
    String countryParam = request.getSyncScope().getCountry();
    String clusterName = request.getSyncScope().getCluster();
    String siteId = request.getSyncScope().getSiteUuid();
    Set<String> uuidsWithDateModifiedOffset = request.getUuidsWithDateModifiedOffset();

    Map<String, List<String>> countryLocationMap =
        loadCountriesMap(Context.getLocationService().getRootLocations(false));

    boolean limitValidation = request.getLimit() <= 0 || null == uuidsWithDateModifiedOffset;
    boolean isCountryValid =
        null == countryParam || !countryLocationMap.containsKey(countryParam.toLowerCase());

    if (limitValidation || isCountryValid) {
      throw new EntityValidationException("Invalid request. Please verify the country");
    }

    // cluster validation
    if (null != siteId) {
      List<String> siteList = countryLocationMap.get(countryParam.toLowerCase());
      if (CollectionUtils.isEmpty(siteList) || !siteList.contains(siteId)) {
        throw new EntityValidationException(
            "Invalid request. Site details not correct or site not mapped for the given country");
      }
    } else if (null != clusterName) {
      validateCluster(countryParam, clusterName);
    }
  }

  /**
   *
   */
  public Location getLocationByUuid(String uuid) throws EntityNotFoundException {
    Location location = Context.getLocationService().getLocationByUuid(uuid);
    if (null == location) {
      throw new EntityNotFoundException(String.format("Location %s not found", uuid));
    }
    return location;
  }

  /**
   *
   */
  public String getLocationUuid(List<AttributeData> attributes) {
    for (AttributeData attribute : attributes) {
      if (attribute.getType().equals(LOCATION_ATTRIBUTE)) {
        return attribute.getValue();
      }
    }
    return null;
  }

  public String getLocationDetails(Location location, String attributeName)
      throws EntityNotFoundException {

    String siteCode = null;

    if (null == location) {
      throw new EntityNotFoundException("siteUuid is not valid");
    }
    Collection<LocationAttribute> attributes = location.getActiveAttributes();
    if (!attributes.isEmpty()) {
      for (LocationAttribute attribute : attributes) {
        if (attributeName.equalsIgnoreCase(attribute.getAttributeType().getName())) {
          siteCode = attribute.getValue().toString();
          break;
        }
      }
      if (StringUtils.isBlank(siteCode)) {
        throw new EntityNotFoundException("Location does not have site code attribute");
      }

    } else {
      throw new EntityNotFoundException("Location has no attributes :: " + location.getName());
    }
    return siteCode;
  }

  private void validateCluster(String countryParam, String clusterName)
      throws EntityValidationException {
    Map<String, List<String>> clusterLocationMap =
        loadClusterMap(Context.getLocationService().getRootLocations(false));
    if (StringUtils.isNotBlank(clusterName)) {
      String message = null;
      List<String> uuidList = clusterLocationMap.get(clusterName.toLowerCase());
      if (!CollectionUtils.isEmpty(uuidList)) {
        String country =
            Context.getLocationService().getLocationByUuid(uuidList.get(0)).getCountry();
        if (!countryParam.equalsIgnoreCase(country)) {
          message = "Invalid request. cluster not mapped to the country mentioned in the request";
        }
      } else {
        message =
            "Invalid request. cluster not found or no locations taggged for the given cluster";
      }

      if (null != message) {
        throw new EntityValidationException(message);
      }
    }
  }

  private Map<String, List<String>> loadClusterMap(List<Location> locations) {
    Instant start = Instant.now();
    Map<String, List<String>> clusterLocationMap = new HashMap<>();
    for (Location location : locations) {
      Collection<LocationAttribute> attributes = location.getActiveAttributes();
      for (LocationAttribute attribute : attributes) {
        if (CLUSTER_ATTRIBUTE_TYPE.equalsIgnoreCase(attribute.getAttributeType().getName())) {
          String clusterName = attribute.getValue().toString().toLowerCase();
          if (!clusterLocationMap.containsKey(clusterName)) {
            List<String> uuidList = new ArrayList<>();
            uuidList.add(location.getUuid());
            clusterLocationMap.put(clusterName, uuidList);
          } else {
            List<String> uuidList = clusterLocationMap.get(clusterName);
            uuidList.add(location.getUuid());
            clusterLocationMap.replace(clusterName, uuidList);
          }
        }
      }
    }

    Instant end = Instant.now();
    LOGGER.info("loadClusterMap call execution time : {}", Duration.between(start, end));
    return clusterLocationMap;
  }

  private Map<String, List<String>> loadCountriesMap(List<Location> locations) {
    Instant start = Instant.now();
    Map<String, List<String>> countryLocationMap = new HashMap<>();
    for (Location location : locations) {
      String country = location.getCountry().toLowerCase();
      if (!countryLocationMap.containsKey(country)) {
        List<String> uuids = new ArrayList<>();
        uuids.add(location.getUuid());
        countryLocationMap.put(country, uuids);
      } else {
        List<String> uuidList = countryLocationMap.get(country);
        uuidList.add(location.getUuid());
        countryLocationMap.replace(country, uuidList);
      }
    }
    Instant end = Instant.now();
    LOGGER.info("loadCountriesMap call execution time : {}", Duration.between(start, end));
    return countryLocationMap;
  }
}
