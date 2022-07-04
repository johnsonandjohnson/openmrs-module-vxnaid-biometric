package org.openmrs.module.biometric.api.service.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.biometric.api.util.TestUtil.IDENTIFIER;
import static org.openmrs.module.biometric.api.util.TestUtil.LOCATION_UUID;
import static org.openmrs.module.biometric.api.util.TestUtil.createPatient;
import static org.openmrs.module.biometric.api.util.TestUtil.createPerson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.biometric.api.builder.ImageResponseBuilder;
import org.openmrs.module.biometric.api.constants.BiometricApiConstants;
import org.openmrs.module.biometric.api.contract.SyncImageResponse;
import org.openmrs.module.biometric.api.contract.SyncTemplateResponse;
import org.openmrs.module.biometric.api.db.SyncDao;
import org.openmrs.module.biometric.api.exception.BiometricApiException;
import org.openmrs.module.biometric.api.exception.EntityNotFoundException;
import org.openmrs.module.biometric.api.model.SyncImageData;
import org.openmrs.module.biometric.api.model.SyncTemplateData;
import org.openmrs.module.biometric.api.util.BiometricApiUtil;
import org.openmrs.module.biometric.api.util.TestUtil;
import org.openmrs.module.licensemanagement.Device;
import org.openmrs.module.licensemanagement.DeviceError;
import org.openmrs.module.licensemanagement.api.DeviceErrorService;
import org.openmrs.module.licensemanagement.api.DeviceService;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Context.class, OpenmrsUtil.class})
public class SyncServiceTest {

  private static final String PARTICIPANT_IMAGES_DIR = "biometric.images.dir";
  private static final String PERSON_IMAGE_ATTRIBUTE = "PersonImageAttribute";
  private static final String PERSON_TEMPLATE_ATTRIBUTE = "PersonTemplateAttribute";
  private static final String DRIVER = "biometric.sql.driver";
  private static final String DATA_SOURCE_URL = "biometric.datasource.url";
  private static final String BIOMETRIC_DB_USER = "biometric.connection.username";
  private static final String BIOMETRIC_DB_PWD = "biometric.connection.password";
  private static final String FETCH_SIZE = "biometric.database.fetchsize";
  private static final long LAST_DATE_MODIFIED = 1609517964000L;
  private static final String DEVICE_ID = "deviceId";
  private static final int MAX_RESULTS_TO_FETCH = 10;
  private static final String PERSON_UUID = "1232-fjfj-4343-43434-fsdsd";

  List<SyncImageData> results = new ArrayList<>();

  List<String> locationList = new ArrayList<>();
  private Properties properties = null;
  @Mock
  private DbSessionFactory sessionFactory;
  private DriverManagerDataSource dataSource;
  @Mock
  private PatientService patientService;
  @Mock
  private DeviceService deviceService;
  @Mock
  private DeviceErrorService deviceErrorService;
  @Mock
  private BiometricApiUtil util;
  @Mock
  private ImageResponseBuilder builder;
  @Mock
  private NamedParameterJdbcTemplate template;
  @Mock
  private SyncDao syncDAO;

  @InjectMocks
  private SyncServiceImpl syncService;

  @Before
  public void setUp() throws BiometricApiException {
    PowerMockito.mockStatic(OpenmrsUtil.class);
    PowerMockito.mockStatic(Context.class);
    when(Context.getPatientService()).thenReturn(patientService);

    results = new ArrayList<>();

    properties = new Properties();
    properties.put(DRIVER, "com.mysql.jdbc.Driver");
    properties.put(DATA_SOURCE_URL, "jdbc\\:mysql\\://localhost\\:3306/openmrs?serverTimezone");
    properties.put(BIOMETRIC_DB_USER, "test");
    properties.put(BIOMETRIC_DB_PWD, "test");
    properties.put(FETCH_SIZE, "100");
    properties.put(PARTICIPANT_IMAGES_DIR, "src/test/resources/images");

    dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(properties.getProperty(DRIVER));
    dataSource.setUrl(properties.getProperty(DATA_SOURCE_URL));
    dataSource.setUsername(properties.getProperty(BIOMETRIC_DB_USER));
    dataSource.setPassword(properties.getProperty(BIOMETRIC_DB_PWD));

    given(OpenmrsUtil.getRuntimeProperties(BiometricApiConstants.APP_PROPERTIES_FILE))
        .willReturn(properties);
    given(util.getDataSource(properties)).willReturn(dataSource);
    locationList.add(LOCATION_UUID);
    syncService.init();
  }

  @Test
  public void getAllPatients_shouldRetrieveAllPatients() {

    Date lastModifiedDate = new Date(1633590415000L);
    List<Patient> patients = Arrays.asList(TestUtil.createPatient(TestUtil.createPerson()));

    when(syncDAO
        .getAllPatientsByLocations(lastModifiedDate, MAX_RESULTS_TO_FETCH, locationList))
        .thenReturn(patients);
    List<Patient> dbPatients = syncService
        .getAllPatients(lastModifiedDate, MAX_RESULTS_TO_FETCH, locationList);

    assertNotNull(dbPatients);
    assertThat(dbPatients.size(), equalTo(1));
    verify(syncDAO, times(1))
        .getAllPatientsByLocations(lastModifiedDate, MAX_RESULTS_TO_FETCH, locationList);
  }

  @Test
  public void getAllVisits_shouldRetrieveAllVisits() {
    Date lastModifiedDate = new Date(1633590415000L);
    List<Visit> visits = Arrays.asList(TestUtil.createVisit());

    when(syncDAO
        .getAllVisits(lastModifiedDate, MAX_RESULTS_TO_FETCH, locationList))
        .thenReturn(visits);

    List<Visit> dbVisits = syncService
        .getAllVisits(lastModifiedDate, MAX_RESULTS_TO_FETCH, locationList);
    assertNotNull(dbVisits);
    assertThat(dbVisits.size(), equalTo(1));
    verify(syncDAO, times(1))
        .getAllVisits(lastModifiedDate, MAX_RESULTS_TO_FETCH, locationList);
  }

  @Test
  public void getAllBiometricTemplates_shouldRetrieveAllParticipantBiometricTemplates() {
    Map<String, Object> row = new HashMap<>();
    Date lastModifiedDate = new Date(LAST_DATE_MODIFIED);
    row.put("dbid", IDENTIFIER);
    row.put(BiometricApiConstants.VOIDED, false);
    row.put("template", "WQJDJKDKDOIDJDIJDI");
    row.put("modificationDate", lastModifiedDate);
    List<Map<String, Object>> rows = new ArrayList<>();
    rows.add(row);

    List<SyncTemplateData> patients = new ArrayList<>();
    SyncTemplateData templateData = new SyncTemplateData();
    templateData.setUuid(PERSON_UUID);
    templateData.setIdentifier(IDENTIFIER);
    templateData.setDateModified(lastModifiedDate);
    templateData.setVoided(false);
    patients.add(templateData);

    when(syncDAO
        .getPatientTemplateData(lastModifiedDate, DEVICE_ID, locationList, false,
            MAX_RESULTS_TO_FETCH)).thenReturn(patients);

    when(util.getNamedParameterJdbcTemplate(dataSource, 100)).thenReturn(template);
    when(template.queryForList(anyString(), any(MapSqlParameterSource.class))).thenReturn(rows);

    List<SyncTemplateResponse> responses = syncService
        .getAllBiometricTemplates(lastModifiedDate, DEVICE_ID, "India", "Site1", locationList,
            false, 10);
    assertNotNull(responses);
    assertTrue(!responses.isEmpty());
    verify(syncDAO, times(1))
        .getPatientTemplateData(lastModifiedDate, DEVICE_ID, locationList, false,
            MAX_RESULTS_TO_FETCH);
  }

  @Test
  public void getAllParticipantImages_shouldRetrieveAllParticipantImagesFromAllDevicesWithOptimizeFlagFalse()
      throws IOException {
    Date lastModifiedDate = new Date(LAST_DATE_MODIFIED);
    String device = "Device2";
    SyncImageData data = new SyncImageData();
    data.setPath(
        new File("src/test/resources/images/Device1/test1.jpeg").toPath());
    results.add(data);
    List<SyncImageResponse> responses = Stream
        .concat(buildResponse("uuid1").stream(), buildResponse("uuid2").stream())
        .collect(Collectors.toList());
    List<Patient> patients = new ArrayList<>();
    Patient patient1 = createPatient(createPerson());
    Patient patient2 = createPatient(createPerson());
    patients.add(patient1);
    patients.add(patient2);

    when(syncDAO
        .getPatientImageData(lastModifiedDate, DEVICE_ID, locationList, false,
            MAX_RESULTS_TO_FETCH)).thenReturn(patients);

    when(builder.createFrom(anyListOf(SyncImageData.class))).thenReturn(responses);

    List<SyncImageResponse> actualResponse = syncService
        .getAllParticipantImages(new Date(LAST_DATE_MODIFIED), 10, locationList, device, false);
    assertNotNull(responses);
    assertThat(actualResponse.size(), equalTo(2));
    verify(syncDAO, times(1))
        .getPatientImageData(lastModifiedDate, device, locationList, false,
            MAX_RESULTS_TO_FETCH);
  }


  @Test
  public void getAllParticipantImages_shouldRetrieveAllParticipantImagesFromAllDevicesExceptDevice2WithOptimizeFlagTrue()
      throws IOException {
    Date lastModifiedDate = new Date(LAST_DATE_MODIFIED);
    String device = "mydevice-mac";

    List<Patient> patients = new ArrayList<>();
    Patient patient1 = createPatient(createPerson());
    patients.add(patient1);

    SyncImageData data = new SyncImageData();
    data.setPath(
        new File("src/test/resources/images/mydevice-mac/" + patient1.getUuid() + ".jpeg")
            .toPath());
    data.setDateModified(patient1.getDateChanged().getTime());
    data.setVoided(false);
    results.add(data);
    List<SyncImageResponse> responses = buildResponse("test1");

    when(syncDAO
        .getPatientImageData(lastModifiedDate, device, locationList, true,
            MAX_RESULTS_TO_FETCH)).thenReturn(patients);
    when(builder.createFrom(anyListOf(SyncImageData.class))).thenReturn(responses);

    List<SyncImageResponse> actualResponse = syncService
        .getAllParticipantImages(lastModifiedDate, MAX_RESULTS_TO_FETCH, locationList, device,
            true);

    assertNotNull(responses);
    assertThat(actualResponse.size(), equalTo(1));

    verify(syncDAO, times(1))
        .getPatientImageData(lastModifiedDate, device, locationList, true, MAX_RESULTS_TO_FETCH);
  }

  @Test
  public void getParticipantImagesCount_shouldReturnImageCountWithTrueOptimizeFlag() {
    String device = "Device1";
    Long ignoredCount = 1L;
    when(syncDAO.getIgnoredCount(locationList, device, PERSON_IMAGE_ATTRIBUTE))
        .thenReturn(ignoredCount);

    List<Object[]> countList = new ArrayList<>();
    Object[] vt = {true, 1L};
    Object[] vf = {false, 9L};
    countList.add(vt);
    countList.add(vf);

    when(syncDAO
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_IMAGE_ATTRIBUTE))
        .thenReturn(countList);

    Map<String, Long> actualResponse = syncService
        .getParticipantImagesCount(locationList, device, true);
    assertThat(actualResponse.size(), equalTo(4));
    assertThat(actualResponse.get("ignoredCount"), equalTo(ignoredCount));
    verify(syncDAO, times(1)).getIgnoredCount(locationList, device, PERSON_IMAGE_ATTRIBUTE);
    verify(syncDAO, times(1))
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_IMAGE_ATTRIBUTE);
  }

  @Test
  public void getParticipantImagesCount_shouldReturnImageCountWithFalseOptimizeFlag()
      throws IOException {
    String device = "Device1";

    List<Object[]> countList = new ArrayList<>();
    Object[] vt = {true, 1L};
    Object[] vf = {false, 9L};
    countList.add(vt);
    countList.add(vf);

    when(syncDAO
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_IMAGE_ATTRIBUTE))
        .thenReturn(countList);

    Map<String, Long> actualResponse = syncService
        .getParticipantImagesCount(locationList, device, false);
    // assertNotNull(responses);
    assertThat(actualResponse.size(), equalTo(4));
    verify(syncDAO, never()).getIgnoredCount(locationList, device, PERSON_IMAGE_ATTRIBUTE);
    verify(syncDAO, times(1))
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_IMAGE_ATTRIBUTE);
  }

  @Test
  public void getBiometricTemplatesCount_ShouldReturnResultsWithFalseOptimizeFlag() {
    String device = "Device1";

    List<Object[]> countList = new ArrayList<>();
    Object[] vt = {true, 1L};
    Object[] vf = {false, 9L};
    countList.add(vt);
    countList.add(vf);

    when(syncDAO
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_TEMPLATE_ATTRIBUTE))
        .thenReturn(countList);

    Map<String, Long> actualResponse = syncService
        .getBiometricTemplatesCount(device, locationList, false);

    assertThat(actualResponse.size(), equalTo(4));
    verify(syncDAO, never()).getIgnoredCount(locationList, device, PERSON_TEMPLATE_ATTRIBUTE);
    verify(syncDAO, times(1))
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_TEMPLATE_ATTRIBUTE);
  }

  @Test
  public void getBiometricTemplatesCount_ShouldReturnResultsWithTrueOptimizeFlag() {
    String device = "Device1";

    List<Object[]> countList = new ArrayList<>();
    Object[] vt = {true, 1L};
    Object[] vf = {false, 9L};
    countList.add(vt);
    countList.add(vf);
    Long ignoredCount = 5L;
    when(syncDAO
        .getIgnoredCount(locationList, device, PERSON_TEMPLATE_ATTRIBUTE))
        .thenReturn(ignoredCount);

    when(syncDAO
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_TEMPLATE_ATTRIBUTE))
        .thenReturn(countList);

    Map<String, Long> actualResponse = syncService
        .getBiometricTemplatesCount(device, locationList, true);

    assertThat(actualResponse.size(), equalTo(4));
    assertThat(actualResponse.get("ignoredCount"), equalTo(ignoredCount));
    verify(syncDAO, times(1)).getIgnoredCount(locationList, device, PERSON_TEMPLATE_ATTRIBUTE);
    verify(syncDAO, times(1))
        .getPatientCountByLocationsAndAttribute(locationList, PERSON_TEMPLATE_ATTRIBUTE);
  }

  @Test
  public void saveSyncError_shouldCreateDeviceAndSaveDeviceErrorWhenDeviceNotExists() {
    String deviceId = "newDeviceId";
    String stackTrace = "Exception(test)";
    String key = "license:IRIS_CLIENT,GET_LICENSE_CALL";
    String meta = "{'type':'license','licenseType':'IRIS_CLIENT','action':'GET_LICENSE_CALL'}";
    Device device = TestUtil.createDevice(deviceId);
    DeviceError deviceError = TestUtil.createDeviceError();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    when(deviceService.saveDevice(any(Device.class))).thenReturn(device);
    when(deviceErrorService.saveDeviceError(any(DeviceError.class))).thenReturn(deviceError);

    syncService.saveSyncError(deviceId, stackTrace, new Date(), key, meta);
    verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
    verify(deviceService, times(1)).saveDevice(any(Device.class));
    verify(deviceErrorService, times(1)).saveDeviceError(any(DeviceError.class));
  }

  @Test
  public void saveSyncError_shouldCreateDeviceAndSaveDeviceErrorWhenDeviceExists() {
    String deviceId = "newDeviceId";
    String stackTrace = "Exception(test)";
    String key = "license:";
    String meta = "{'type':'license','licenseType':'IRIS_CLIENT','action':'GET_LICENSE_CALL'}";
    Device device = TestUtil.createDevice(deviceId);
    DeviceError deviceError = TestUtil.createDeviceError();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceErrorService.saveDeviceError(any(DeviceError.class))).thenReturn(deviceError);

    syncService.saveSyncError(deviceId, stackTrace, new Date(), key, meta);
    verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
    verify(deviceErrorService, times(1)).saveDeviceError(any(DeviceError.class));
  }

  @Test
  public void saveSyncError_shouldCreateDeviceAndSaveDeviceErrorWithoutSubTypeWhenDeviceExists() {
    String deviceId = "newDeviceId";
    String stackTrace = "Exception(test)";
    String key = "license";
    String meta = "{'type':'license','licenseType':'IRIS_CLIENT','action':'GET_LICENSE_CALL'}";
    Device device = TestUtil.createDevice(deviceId);
    DeviceError deviceError = TestUtil.createDeviceError();
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceErrorService.saveDeviceError(any(DeviceError.class))).thenReturn(deviceError);

    syncService.saveSyncError(deviceId, stackTrace, new Date(), key, meta);
    verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
    verify(deviceErrorService, times(1)).saveDeviceError(any(DeviceError.class));
  }

  @Test
  public void resolveSyncErrors_shouldVoidTheSyncErrorForTheDeviceAndGivenKey()
      throws EntityNotFoundException {
    String deviceId = "newDeviceId";
    List<String> errorKeys = new ArrayList<>();
    errorKeys.add("license:IRIS_CLIENT,GET_LICENSE_CALL");
    Device device = TestUtil.createDevice(deviceId);
    DeviceError deviceError = TestUtil.createDeviceError();
    DeviceError deviceErrorVoided = TestUtil.createDeviceErrorVoided();
    List<DeviceError> deviceErrorList = new ArrayList<>();
    deviceErrorList.add(deviceError);
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(device);
    when(deviceErrorService.getDeviceErrorsByKey(any(Device.class), anyString(), anyBoolean()))
        .thenReturn(deviceErrorList);
    when(deviceErrorService.saveDeviceError(any(DeviceError.class))).thenReturn(deviceErrorVoided);
    syncService.resolveSyncErrors(deviceId, errorKeys);
    verify(deviceService, times(1)).getDeviceByMAC(deviceId, false);
    verify(deviceErrorService, times(1))
        .getDeviceErrorsByKey(any(Device.class), anyString(), anyBoolean());
    verify(deviceErrorService, times(1)).saveDeviceError(any(DeviceError.class));
  }

  @Test(expected = EntityNotFoundException.class)
  public void resolveSyncErrors_shouldThrowEntityNotFoundExceptionWhenDeviceNotExist()
      throws EntityNotFoundException {
    String deviceId = "newDeviceId";
    List<String> errorKeys = new ArrayList<>();
    errorKeys.add("license:IRIS_CLIENT,GET_LICENSE_CALL");
    Device device = TestUtil.createDevice(deviceId);
    when(deviceService.getDeviceByMAC(deviceId, false)).thenReturn(null);
    syncService.resolveSyncErrors(deviceId, errorKeys);
    verify(deviceService, times(1)).getDeviceByMAC(anyString());
    verify(deviceErrorService, times(0))
        .getDeviceErrorsByKey(any(Device.class), deviceId, false);
    verify(deviceErrorService, times(0)).saveDeviceError(any(DeviceError.class));
  }

  private List<SyncImageResponse> buildResponse(String uuid) {
    List<SyncImageResponse> responses = new ArrayList<>();
    SyncImageResponse response = new SyncImageResponse();
    response.setParticipantUuid(uuid);
    response.setType("update");
    responses.add(response);
    return responses;
  }
}
