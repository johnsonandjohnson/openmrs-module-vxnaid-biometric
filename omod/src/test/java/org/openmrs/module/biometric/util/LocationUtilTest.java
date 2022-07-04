package org.openmrs.module.biometric.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.openmrs.module.biometric.constants.BiometricTestConstants.CLUSTER_ATTRIBUTE_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.exception.EntityValidationException;
import org.openmrs.module.biometric.api.model.AttributeData;
import org.openmrs.module.biometric.contract.sync.SyncRequest;
import org.openmrs.module.biometric.contract.sync.SyncScope;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Context.class})
public class LocationUtilTest {


  public static final String COUNTRY1_NAME = "India";
  private static final String COUNTRY1_LOCATION1_UUID = "10655fae-3b09-4b15-bb4b-296311546be4";
  private static final String COUNTRY1_LOCATION2_UUID = "113655fae-3b09-4b15-bb4b-296311546be4";
  private static final String COUNTRY1_CLUSTER1_NAME = "country1-cluster1";

  private static final String COUNTRY1_LOCATION3_UUID = "12655fae-3b09-4b15-bb4b-296311546be4";
  private static final String COUNTRY1_CLUSTER2_NAME = "country2-cluster1";

  private static final String COUNTRY1_LOCATION4_UUID = "13655fae-3b09-4b15-bb4b-296311546be4";

  private static final String COUNTRY2_NAME = "USA";
  private static final String COUNTRY2_LOCATION1_UUID = "20655fae-3b09-4b15-bb4b-296311546be4";

  private static final String PARTICIPANT_UUID = "45555fae-3b09-4b15-bb4b-296311546be4";

  @InjectMocks
  private LocationUtil locationUtil;

  @Mock
  private LocationService locationService;

  public static List<Location> createLocations() {
    List<Location> locations = new ArrayList<>();
    Location location1 = createLocation(1, COUNTRY1_NAME, COUNTRY1_LOCATION1_UUID,
        COUNTRY1_CLUSTER1_NAME);
    Location location2 = createLocation(2, COUNTRY1_NAME, COUNTRY1_LOCATION2_UUID,
        COUNTRY1_CLUSTER1_NAME);
    Location location3 = createLocation(3, COUNTRY1_NAME, COUNTRY1_LOCATION3_UUID,
        COUNTRY1_CLUSTER2_NAME);
    Location location4 = createLocation(4, COUNTRY1_NAME, COUNTRY1_LOCATION4_UUID, null);
    Location location5 = createLocation(5, COUNTRY2_NAME, COUNTRY2_LOCATION1_UUID, null);
    locations.add(location1);
    locations.add(location2);
    locations.add(location3);
    locations.add(location4);
    locations.add(location5);
    return locations;
  }

  public static Location createLocation(int locationId, String country, String locationUuid,
      String cluster) {
    Location location = new Location();
    location.setId(locationId);
    location.setCountry(country);
    location.setUuid(locationUuid);
    if (null != cluster) {
      LocationAttributeType type = new LocationAttributeType();
      type.setName(CLUSTER_ATTRIBUTE_TYPE);
      LocationAttribute attribute = new LocationAttribute();
      attribute.setAttributeType(type);
      attribute.setValue(cluster);
      location.addAttribute(attribute);
    }
    return location;
  }

  public static List<String> getLocationUuidsByCountry() {
    return Arrays.asList(COUNTRY1_LOCATION1_UUID, COUNTRY1_LOCATION2_UUID, COUNTRY1_LOCATION3_UUID,
        COUNTRY1_LOCATION4_UUID);
    //return locations.stream().map(Location::getUuid).collect(Collectors.toList());
  }

  @Before
  public final void setUp() {
    PowerMockito.mockStatic(Context.class);
    when(Context.getLocationService()).thenReturn(locationService);
  }

  @Test
  public final void findLocationsByCluster_shouldReturnLocationsByCluster() {
    //Given
    List<Location> locations = createLocations();
    //When
    when(locationService.getRootLocations(false)).thenReturn(locations);
    //should return 2 locations for cluster1
    //Then
    List<String> locationUuids = locationUtil
        .findLocationsByCluster(COUNTRY1_NAME, COUNTRY1_CLUSTER1_NAME);
    assertNotNull(locationUuids);
    assertThat(locationUuids.size(), equalTo(2));

    //should return 1 locations for cluster2
    List<String> locationUuids2 = locationUtil
        .findLocationsByCluster(COUNTRY1_NAME, COUNTRY1_CLUSTER2_NAME);
    assertNotNull(locationUuids2);
    assertThat(locationUuids2.size(), equalTo(1));
  }

  @Test
  public final void findLocationsByCountry_shouldReturnLocationsByCountry() {
    //Given
    List<Location> locations = createLocations();
    //When
    when(locationService.getRootLocations(false)).thenReturn(locations);
    //Then
    List<String> locationUuids1 = locationUtil.findLocationsByCountry(COUNTRY1_NAME);
    assertNotNull(locationUuids1);
    assertThat(locationUuids1.size(), equalTo(4));

    List<String> locationUuids2 = locationUtil.findLocationsByCountry(COUNTRY2_NAME);
    assertNotNull(locationUuids2);
    assertThat(locationUuids2.size(), equalTo(1));
  }

  @Test
  public final void validateSyncLocationData_shouldNotThrowValidationExceptionWhenCountryDoesNotExists()
      throws
      EntityValidationException {
    //Given
    SyncRequest syncRequest = new SyncRequest();
    Set<String> uuidsWithDateModifiedOffset = new HashSet<>();
    uuidsWithDateModifiedOffset.add(PARTICIPANT_UUID);
    SyncScope scope = new SyncScope();
    scope.setCountry(COUNTRY1_NAME);
    scope.setCluster(COUNTRY1_CLUSTER1_NAME);
    syncRequest.setSyncScope(scope);
    syncRequest.setUuidsWithDateModifiedOffset(uuidsWithDateModifiedOffset);
    syncRequest.setLimit(10);

    List<Location> locations = createLocations();
    Location location1 = locations.get(0);
    //When
    when(locationService.getRootLocations(false)).thenReturn(locations);
    when(locationService.getLocationByUuid(COUNTRY1_LOCATION1_UUID)).thenReturn(location1);
    //Then
    locationUtil.validateSyncLocationData(syncRequest);
  }

  @Test(expected = EntityValidationException.class)
  public final void validateSyncLocationData_shouldThrowValidationExceptionWhenCountryDoesNotExists()
      throws
      EntityValidationException {
    //Given
    SyncRequest syncRequest = new SyncRequest();
    SyncScope scope = new SyncScope();
    scope.setCountry("Country1");
    syncRequest.setSyncScope(scope);
    List<Location> locations = createLocations();
    //When
    when(locationService.getRootLocations(false)).thenReturn(locations);
    //Then
    locationUtil.validateSyncLocationData(syncRequest);
  }

  @Test
  public void getLocationUuid_nullAttribute() {
    AttributeData attributeData = new AttributeData();
    attributeData.setValue("123");
    attributeData.setType("type1");

    String data = locationUtil.getLocationUuid(Arrays.asList(attributeData));

    assertNull(data);
  }

  @Test
  public void getLocationUuid_getAttribute() {
    AttributeData attributeData = new AttributeData();
    attributeData.setValue("123");
    attributeData.setType("LocationAttribute");

    String data = locationUtil.getLocationUuid(Arrays.asList(attributeData));

    assertNotNull(data);
  }

  @Test(expected = EntityNotFoundException.class)
  public void getLocationByUuid_shouldThrowErrorWhenLocationNull() throws EntityNotFoundException {

    when(Context.getLocationService().getLocationByUuid(anyString())).thenReturn(null);
    Location location = locationUtil.getLocationByUuid("42655fae-3b09-4b15-bb4b-296311546be4");
    Assert.fail("should throw NullPointerException");
  }

  @Test
  public void getLocationByUuid_shouldReturnLocation() throws EntityNotFoundException {
    Location location = new Location();
    location.setUuid("42655fae-3b09-4b15-bb4b-296311546be4");
    location.setCountry("asdf");

    when(Context.getLocationService().getLocationByUuid(anyString())).thenReturn(location);
    Location location1 = locationUtil.getLocationByUuid("42655fae-3b09-4b15-bb4b-296311546be4");
    Assert.assertNotNull(location1);
  }
}
