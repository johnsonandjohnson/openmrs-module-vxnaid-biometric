/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */

package org.openmrs.module.biometric.api.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.customdatatype.datatype.BooleanDatatype;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.biometric.api.contract.BiometricData;
import org.openmrs.module.licensemanagement.Device;
import org.openmrs.module.licensemanagement.DeviceAttribute;
import org.openmrs.module.licensemanagement.DeviceAttributeType;
import org.openmrs.module.licensemanagement.DeviceError;
import org.openmrs.module.licensemanagement.License;
import org.openmrs.util.OpenmrsUtil;

/**
 * Test Util
 */
public class TestUtil {

    public static final String IDENTIFIER = "btest1";
    private static final String ENCOUNTER_TYPE_DOSING = "Dosing";
    private static final String PERSON1_ADDRESS1 = "FLAT 111, ABC APT";
    private static final String PERSON1_ADDRESS2 = "WHITEFIELD";
    private static final String PERSON1_CITY_VILLAGE = "BANGALORE";
    private static final String PERSON1_POSTAL_CODE = "560066";
    private static final String PERSON1_COUNTRY = "India";
    private static final String LOCATION_ATTRIBUTE = "LocationAttribute";
    private static final String PATIENT1_LOCATION = "1dd05e68-4d77-4b61-b2bf-436857f35aa0";
    public static final String LOCATION_UUID = "9dd05e68-4d77-4b61-b2bf-436857f35aa0";
    public static final String COUNTRY = "BELGIUM";
    private static final String OPEN_MRS_ID = "OpenMRS ID";
    private static final String PERSON_IMAGE_ATTRIBUTE = "PersonImageAttribute";

    public static Person createPerson() {
        Person person = new Person();
        person.setGender("M");
        person.setBirthdate(new Date());
        PersonName name1 = new PersonName();
        name1.setGivenName("NA");
        person.addName(name1);
        PersonAddress personAddress = new PersonAddress();
        personAddress.setAddress1(PERSON1_ADDRESS1);
        personAddress.setAddress2(PERSON1_ADDRESS2);
        personAddress.setCityVillage(PERSON1_CITY_VILLAGE);
        personAddress.setPostalCode(PERSON1_POSTAL_CODE);
        personAddress.setCountry(PERSON1_COUNTRY);
        Set<PersonAddress> addressSet = new HashSet<>();
        addressSet.add(personAddress);

        Set<PersonAttribute> attributes = new HashSet<>();
        PersonAttributeType locationType = new PersonAttributeType();
        locationType.setName(LOCATION_ATTRIBUTE);
        locationType.setUuid(OpenmrsUtil.generateUid());
        PersonAttribute locationAttribute = new PersonAttribute();
        locationAttribute.setAttributeType(locationType);
        locationAttribute.setValue(LOCATION_UUID);
        attributes.add(locationAttribute);

        PersonAttributeType imageAttributeType = new PersonAttributeType();
        imageAttributeType.setName(PERSON_IMAGE_ATTRIBUTE);
        imageAttributeType.setUuid(OpenmrsUtil.generateUid());
        PersonAttribute imageAttribute = new PersonAttribute();
        imageAttribute.setAttributeType(imageAttributeType);
        imageAttribute.setValue("mydevice-mac");
        attributes.add(imageAttribute);

        person.setAttributes(attributes);
        person.setAddresses(addressSet);
        return person;
    }

    public static Patient createPatient(Person person) {
        Patient patient = new Patient(person);
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier(IDENTIFIER);
        identifier.setPreferred(true);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid("8dd05e68-4d77-4b61-b2bf-436857f35aa0");
        identifier.setIdentifierType(identifierType);
        identifier.setLocation(createLocation());
        patient.addIdentifier(identifier);
        patient.setDateCreated(new Date());
        patient.setPersonDateChanged(new Date());
        return patient;
    }

    public static Location createLocation() {
        Location location = new Location();
        location.setCountry(COUNTRY);
        location.setUuid(LOCATION_UUID);
        return location;
    }

    public static Visit createVisit() {
        Visit visit = new Visit();
        visit.setVisitId(1);
        visit.setStartDatetime(new Date());
        visit.setPatient(createPatient(createPerson()));
        VisitAttribute visitAttribute = new VisitAttribute();
        VisitAttributeType visitAttributeType = new VisitAttributeType();
        visitAttributeType.setName("SCHEDULED");
        visitAttribute.setAttributeType(visitAttributeType);
        visit.addAttribute(visitAttribute);
        return visit;
    }

    public static Visit createAnotherVisit() {
        Visit visit = new Visit();
        visit.setVisitId(1);
        visit.setStartDatetime(new Date());
        visit.setPatient(createPatient(createPerson()));
        VisitType visitType = new VisitType();
        visitType.setName(ENCOUNTER_TYPE_DOSING);
        visit.setVisitType(visitType);
        VisitAttribute visitAttribute = new VisitAttribute();
        VisitAttributeType visitAttributeType = new VisitAttributeType();
        visitAttributeType.setName("SCHEDULED");
        visitAttribute.setAttributeType(visitAttributeType);
        visit.addAttribute(visitAttribute);
        return visit;
    }

    public static Visit updateVisitStatusAttriute(Visit updatedVisitObj) {
        VisitAttribute visitAttribute = new VisitAttribute();
        VisitAttributeType visitAttributeType = new VisitAttributeType();
        visitAttributeType.setName("OCCURRED");
        visitAttribute.setAttributeType(visitAttributeType);
        updatedVisitObj.setAttribute(visitAttribute);
        return updatedVisitObj;
    }

    public static Encounter createEncounter() {
        Encounter encounter = new Encounter();
        EncounterType type = new EncounterType();
        type.setName(ENCOUNTER_TYPE_DOSING);
        Patient patient = TestUtil.createPatient(TestUtil.createPerson());
        patient.setId(1);
        encounter.setPatient(patient);
        encounter.setEncounterType(type);
        encounter.setEncounterDatetime(new Date());
        encounter.setLocation(TestUtil.createLocation());
        encounter.setVisit(updateVisitStatusAttriute(TestUtil.createVisit()));
        return encounter;
    }

    public static Encounter updateEncounterWithObservations() throws ParseException {
        Encounter encounter = createEncounter();
        Concept concept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName("Vaccine Manufacture");
        conceptName.setLocale(Locale.ENGLISH);
        concept.setShortName(conceptName);
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setHl7Abbreviation("ST");
        concept.setDatatype(conceptDatatype);

        Obs obs = new Obs();
        obs.setEncounter(encounter);
        obs.setConcept(concept);
        obs.setPerson(encounter.getPatient().getPerson());
        obs.setObsDatetime(new Date());

        obs.setValueAsString("Pfizer");
        Set<Obs> obsSet = new HashSet<>();
        obsSet.add(obs);
        encounter.setObs(obsSet);
        return encounter;
    }

    public static List<AddressHierarchyLevel> getAddressHierarchyLevelList() {
        List<AddressHierarchyLevel> addressHierarchyLevelList = new ArrayList<>();
        AddressHierarchyLevel addressHierarchyLevelCountry = new AddressHierarchyLevel();
        AddressHierarchyLevel addressHierarchyLevelState = new AddressHierarchyLevel();
        AddressHierarchyLevel addressHierarchyLevelCounty = new AddressHierarchyLevel();

        addressHierarchyLevelCountry.setLevelId(1);
        addressHierarchyLevelCountry.setName("country");
        addressHierarchyLevelCountry.setAddressField(AddressField.COUNTRY);
        addressHierarchyLevelList.add(addressHierarchyLevelCountry);

        addressHierarchyLevelState.setLevelId(2);
        addressHierarchyLevelState.setParent(addressHierarchyLevelCountry);
        addressHierarchyLevelState.setName("stateProvince");
        addressHierarchyLevelState.setAddressField(AddressField.STATE_PROVINCE);
        addressHierarchyLevelList.add(addressHierarchyLevelState);

        addressHierarchyLevelCounty.setLevelId(3);
        addressHierarchyLevelCounty.setParent(addressHierarchyLevelState);
        addressHierarchyLevelCounty.setName("countyDistrict");
        addressHierarchyLevelCounty.setAddressField(AddressField.COUNTY_DISTRICT);
        addressHierarchyLevelList.add(addressHierarchyLevelCounty);

        return addressHierarchyLevelList;
    }

    public static List<AddressHierarchyEntry> getAddressHierarchyEntryList() {
        List<AddressHierarchyEntry> addressHierarchyEntryList = new ArrayList<>();
        AddressHierarchyEntry addressHierarchyEntryForBelgium = new AddressHierarchyEntry();
        AddressHierarchyEntry addressHierarchyEntryForAntwerp = new AddressHierarchyEntry();
        AddressHierarchyEntry addressHierarchyEntryForBrussels = new AddressHierarchyEntry();
        AddressHierarchyEntry addressHierarchyEntryForPortOfAntwerp = new AddressHierarchyEntry();
        AddressHierarchyEntry addressHierarchyEntryForGlobalCity = new AddressHierarchyEntry();
        AddressHierarchyEntry addressHierarchyEntryForBrusselsCounty = new AddressHierarchyEntry();
        AddressHierarchyEntry addressHierarchyEntryForShipyardCounty = new AddressHierarchyEntry();

        addressHierarchyEntryForBelgium.setAddressHierarchyEntryId(1);
        addressHierarchyEntryForBelgium.setName("Belgium");
        addressHierarchyEntryForBelgium.setAddressHierarchyLevel(getAddressHierarchyLevelList().get(0));
        addressHierarchyEntryList.add(addressHierarchyEntryForBelgium);

        addressHierarchyEntryForAntwerp.setAddressHierarchyEntryId(2);
        addressHierarchyEntryForAntwerp.setName("Antwerp");
        addressHierarchyEntryForAntwerp.setAddressHierarchyLevel(getAddressHierarchyLevelList().get(1));
        addressHierarchyEntryForAntwerp.setParent(addressHierarchyEntryForBelgium);
        addressHierarchyEntryList.add(addressHierarchyEntryForAntwerp);

        addressHierarchyEntryForBrussels.setAddressHierarchyEntryId(3);
        addressHierarchyEntryForBrussels.setName("Brussels");
        addressHierarchyEntryForBrussels.setAddressHierarchyLevel(getAddressHierarchyLevelList().get(1));
        addressHierarchyEntryForBrussels.setParent(addressHierarchyEntryForBelgium);
        addressHierarchyEntryList.add(addressHierarchyEntryForBrussels);

        addressHierarchyEntryForPortOfAntwerp.setAddressHierarchyEntryId(4);
        addressHierarchyEntryForPortOfAntwerp.setName("PortOfAntwerp");
        addressHierarchyEntryForPortOfAntwerp.setAddressHierarchyLevel(getAddressHierarchyLevelList().get(2));
        addressHierarchyEntryForPortOfAntwerp.setParent(addressHierarchyEntryForAntwerp);
        addressHierarchyEntryList.add(addressHierarchyEntryForPortOfAntwerp);

        addressHierarchyEntryForGlobalCity.setAddressHierarchyEntryId(5);
        addressHierarchyEntryForGlobalCity.setName("GlobalCity");
        addressHierarchyEntryForGlobalCity.setAddressHierarchyLevel(getAddressHierarchyLevelList().get(2));
        addressHierarchyEntryForGlobalCity.setParent(addressHierarchyEntryForAntwerp);
        addressHierarchyEntryList.add(addressHierarchyEntryForGlobalCity);

        addressHierarchyEntryForBrusselsCounty.setAddressHierarchyEntryId(6);
        addressHierarchyEntryForBrusselsCounty.setName("BrusselsCounty");
        addressHierarchyEntryForBrusselsCounty.setAddressHierarchyLevel(getAddressHierarchyLevelList().get(2));
        addressHierarchyEntryForBrusselsCounty.setParent(addressHierarchyEntryForBrussels);
        addressHierarchyEntryList.add(addressHierarchyEntryForBrusselsCounty);

        addressHierarchyEntryForShipyardCounty.setAddressHierarchyEntryId(7);
        addressHierarchyEntryForShipyardCounty.setName("ShipyardCounty");
        addressHierarchyEntryForShipyardCounty.setAddressHierarchyLevel(getAddressHierarchyLevelList().get(2));
        addressHierarchyEntryForShipyardCounty.setParent(addressHierarchyEntryForBrussels);
        addressHierarchyEntryList.add(addressHierarchyEntryForShipyardCounty);

        return addressHierarchyEntryList;
    }

    public static List<String> getPossibleFullAddress() {
        List<String> stringList = new ArrayList<>();
        String str1 = "Belgium|Antwerp|PortOfAntwerp";
        String str2 = "Belgium|Antwerp|GlobalCity";
        String str3 = "Belgium|Brussels|BrusselsCounty";
        String str4 = "Belgium|Brussels|ShipyardCounty";

        stringList.add(str1);
        stringList.add(str2);
        stringList.add(str3);
        stringList.add(str4);

        return stringList;
    }

    public static String getConfiguratoins1() {
        return "[ " + "\"personLanguages\": [" + "{ \"name\": \"English\" }," + "{ \"name\": \"Hindi\" }," +
                "{ \"name\": \"Kannada\" }" + "]" + "]";
    }

    public static String getConfiguratoins2() {
        return "[ " + "\"vaccine\": [" + "{ \"name\": \"Covid 1D vaccine\" }," + "{ \"name\": \"Covid 2D vaccine\" }," +
                "{ \"name\": \"Covid 3D vaccine\" }" + "]" + "]";
    }

    public static String getLicense() {
        return "{ " + " \"licenses\": [" + " \"type\": \"IRIS," + " \"value\": \"license string" + " }" + " ]" + "}";
    }

    public static Device createDevice(String deviceId) {
        Device device = new Device();
        device.setName(deviceId);
        device.setDeviceMac(deviceId);
        return device;
    }

    public static DeviceAttribute createDeviceAttribute() {
        DeviceAttributeType deviceAttributeType = createDeviceAttributeType();
        Long dateSyncCompleted = System.currentTimeMillis();
        DeviceAttribute deviceAttribute = new DeviceAttribute();
        deviceAttribute.setAttributeType(deviceAttributeType);
        deviceAttribute.setValueReferenceInternal(String.valueOf(dateSyncCompleted));
        return deviceAttribute;
    }

    public static Device createDeviceWithAttributes(String deviceId) {
        Device device = new Device();
        device.setName(deviceId);
        device.setDescription(deviceId);
        device.setDeviceMac(deviceId);
        DeviceAttribute deviceAttribute = createDeviceAttribute();
        device.setAttribute(deviceAttribute);
        return device;
    }

    public static DeviceAttributeType createDeviceAttributeType(){
        DeviceAttributeType deviceAttributeType = new DeviceAttributeType();
        deviceAttributeType.setName("SYNC_COMPLETED_DATE");
        deviceAttributeType.setDescription("Sync completed date");
        deviceAttributeType.setDatatypeClassname(BooleanDatatype.class.getName());
        deviceAttributeType.setUuid("0d773b11-e0a4-4f49-a21e-232d719bbes4");
        return deviceAttributeType;
    }

    public static License createLicense(){
        License license = new License();
        license.setSerialNo("New-license-1234");
        license.setOnline("true");
        return license;
    }

    public static DeviceError createDeviceError(){
        DeviceError deviceError = new DeviceError();
        Device device = createDevice("newDeviceId");
        deviceError.setDevice(device);
        deviceError.setReportedDate(new Date());
        deviceError.setMetaType("license");
        deviceError.setMetaSubType("IRIS_CLIENT");
        deviceError.setKey("license:IRIS_CLIENT,GET_LICENSE_CALL");
        deviceError.setStackTrace("Exception(test)");
        deviceError.setMeta("{'type':'license','licenseType':'IRIS_CLIENT','action':'GET_LICENSE_CALL'}");
        return deviceError;
    }

    public static DeviceError createDeviceErrorVoided(){
        DeviceError deviceError = new DeviceError();
        Device device = createDevice("newDeviceId");
        deviceError.setDevice(device);
        deviceError.setReportedDate(new Date());
        deviceError.setMetaType("license");
        deviceError.setMetaSubType("IRIS_CLIENT");
        deviceError.setKey("license:IRIS_CLIENT,GET_LICENSE_CALL");
        deviceError.setStackTrace("Exception(test)");
        deviceError.setVoided(true);
        deviceError.setVoidReason("Error Resolved");
        deviceError.setDateVoided(new Date());
        deviceError.setMeta("{'type':'license','licenseType':'IRIS_CLIENT','action':'GET_LICENSE_CALL'}");
        return deviceError;
    }

    public static PersonAttribute createPersonAttribute()
    {
        PersonAttributeType locationType = new PersonAttributeType();
        locationType.setName(LOCATION_ATTRIBUTE);
        locationType.setUuid(OpenmrsUtil.generateUid());
        PersonAttribute locationAttribute = new PersonAttribute();
        locationAttribute.setAttributeType(locationType);
        locationAttribute.setValue(LOCATION_UUID);
        locationAttribute.setValue("1234567890");
        Person person = createPerson();
        locationAttribute.setPerson(person);
        return locationAttribute;
    }

    public static PatientIdentifierType createPatientIdentifierType() {
        PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
        patientIdentifierType.setUuid(OpenmrsUtil.generateUid());
        patientIdentifierType.setName(OPEN_MRS_ID);
        return patientIdentifierType;
    }

    public static BiometricData createBiometricData() {
        return new BiometricData("btest1", "p1", "WQJDJKDKDOIDJDIJDI", new Date(),
                false);
    }

    public static LocationAttributeType createLocationAttributeType(String name) {

        LocationAttributeType locationAttributeType = new LocationAttributeType();
        locationAttributeType.setName(name);
        locationAttributeType.setUuid(OpenmrsUtil.generateUid());
        return locationAttributeType;

    }

    public static LocationAttribute createLocationAttribute(String name) {

        LocationAttributeType locationAttributeType = createLocationAttributeType(name);

        LocationAttribute locationAttribute = new LocationAttribute();
        locationAttribute.setAttributeType(locationAttributeType);
        locationAttribute.setValue("abcd");
        return locationAttribute;

    }

    public static DeviceAttributeType createDeviceAttributeType(String name) {

        DeviceAttributeType deviceAttributeType = new DeviceAttributeType();
        deviceAttributeType.setName(name);
        deviceAttributeType.setUuid(OpenmrsUtil.generateUid());
        return deviceAttributeType;

    }

    public static DeviceAttribute createDeviceAttribute(String name) {

        DeviceAttributeType deviceAttributeType = createDeviceAttributeType(name);

        DeviceAttribute deviceAttribute = new DeviceAttribute();
        deviceAttribute.setAttributeType(deviceAttributeType);
        deviceAttribute.setValue("abcd");
        return deviceAttribute;

    }

}
