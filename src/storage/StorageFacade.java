/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import sensum_bosted.Diary;
import sensum_bosted.Notation;
import sensum_bosted.Patient;
import sensum_bosted.User;
import sensum_bosted.UserRoles;

/**
 *
 * @author Jonas
 */
public class StorageFacade implements StorageInterface {

    private static StorageFacade instance;

    private StorageFacade() {
    }

    public static StorageFacade getInstance() {
        if (instance == null) {
            instance = new StorageFacade();
        }
        return instance;
    }

    CRUDInterface CRUD = CRUDFacade.getInstance();

    @Override
    public Patient getPatient(String cpr) {
        HashMap<Enum, String> patientMap = CRUD.readFromKey(Tables.PATIENTS, new String[]{cpr}, null)[0];
        HashMap<Enum, String> userMap = CRUD.readFromKey(Tables.USERS, new String[]{cpr}, null)[0];
        // public Patient(String name, String username, String password, UserRoles field, String cpr)
        String name = userMap.get(Fields.UserFields.NAME);
        String password = userMap.get(Fields.UserFields.PASSWORD);
        UserRoles field = UserRoles.valueOf(userMap.get(Fields.UserFields.USERROLES));
        cpr = patientMap.get(Fields.PatientFields.CPR);
        String info = patientMap.get(Fields.PatientFields.INFO);
        Patient patient = new Patient(name, password, field, cpr, info);
        return patient;
    }

    @Override
    public boolean setPatient(Patient data) {
        String cpr = data.getCpr();
        HashMap<Enum, String> userMap = new HashMap<>();
        userMap.put(Fields.UserFields.NAME, data.getName());
        userMap.put(Fields.UserFields.PASSWORD, data.getPassword());
        userMap.put(Fields.UserFields.USERNAME, cpr);
        userMap.put(Fields.UserFields.USERROLES, data.getField().toString());

        if (CRUD.readFromKey(Tables.USERS, new String[]{cpr}, null) == null) {
            CRUD.create(Tables.USERS, userMap, null);
        } else {
            CRUD.update(Tables.USERS, new String[]{cpr}, userMap, null);
        }

        HashMap<Enum, String> patientMap = new HashMap<>();
        patientMap.put(Fields.PatientFields.CPR, data.getCpr());
        patientMap.put(Fields.PatientFields.INFO, data.getInfo());

        if (CRUD.readFromKey(Tables.PATIENTS, new String[]{cpr}, null) == null) {
            CRUD.create(Tables.PATIENTS, patientMap, null);
        } else {
            CRUD.update(Tables.PATIENTS, new String[]{cpr}, patientMap, null);
        }
        return true;
    }

    @Override
    public User getUser(String username) {
        //    public User(String name, String username, String password, UserRoles field) {
        HashMap<Enum, String> userMap;
        try {
            userMap = CRUD.readFromKey(Tables.USERS, new String[]{username}, null)[0];
        } catch (NullPointerException ex) {
            sensum_bosted.PrintHandler.println(ex.toString(), true);
            return null;
        }
        String name = userMap.get(Fields.UserFields.NAME);
        username = userMap.get(Fields.UserFields.USERNAME);
        String password = userMap.get(Fields.UserFields.PASSWORD);
        UserRoles field = UserRoles.valueOf(userMap.get(Fields.UserFields.USERROLES));
        HashMap<Enum, String>[] assignments = CRUD.readFromKey(Tables.ASSIGNMENTS, new String[]{username}, null);
        ArrayList<String> list = new ArrayList<>();
        if (!(assignments == null)) {
            for (HashMap<Enum, String> assignment : assignments) {
                String patientId = assignment.get(Fields.AssignmentFields.PATIENT_ID);
                list.add(patientId);
            }
        }
        User user = new User(name, username, password, field, list);
        return user;
    }

    @Override
    public boolean setUser(User data) {
        HashMap<Enum, String> map = new HashMap<>();
        map.put(Fields.UserFields.NAME, data.getName());
        map.put(Fields.UserFields.PASSWORD, data.getPassword());
        map.put(Fields.UserFields.USERNAME, data.getUsername());
        map.put(Fields.UserFields.USERROLES, data.getField().toString());

        //if (CRUD.readFromKey(Tables.USERS, id, null) == null) {
        CRUD.create(Tables.USERS, map, null);
        return true;
        //} else {
        //   CRUD.update(Tables.USERS, id, map, null);
        // return true;
        //}
    }

    @Override
    public Diary getDiary(Patient patient) {
        //    public Notation(String content, Date date, Notation.Field field) {
        HashMap<Enum, String>[] array = CRUD.readFromKey(Tables.NOTATIONS, new String[]{patient.getCpr()}, null);
        ArrayList<Notation> notations = new ArrayList<>();
        if (!(array == null)) {
            for (HashMap<Enum, String> map : array) {
                if (patient.getCpr().equals(map.get(Fields.NotationFields.PATIENT_ID))) {
                    String content = map.get(Fields.NotationFields.CONTENT);
                    String sDate = map.get(Fields.NotationFields.DATE);
                    LocalDate date = LocalDate.parse(sDate);
                    Notation.Field field = Notation.Field.valueOf(map.get(Fields.NotationFields.FIELD));
                    String user = map.get(Fields.NotationFields.LAST_USER);
                    LocalDateTime timestamp = LocalDateTime.parse(map.get(Fields.NotationFields.TIME_STAMP));
                    Notation notation = new Notation(content, date, field, user, timestamp);
                    notations.add(notation);
                }

            }
        }
        Diary diary = new Diary(notations);

        return diary;

    }

    @Override
    public boolean setNotation(Patient patient, Notation data) {

        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        //String sDate = dateFormat.format(data.getDate());
        HashMap<Enum, String> map = new HashMap<>();
        map.put(Fields.NotationFields.CONTENT, data.getContent());
        map.put(Fields.NotationFields.DATE, data.getDate().toString());
        map.put(Fields.NotationFields.FIELD, data.getField().toString());
        map.put(Fields.NotationFields.PATIENT_ID, patient.getCpr());
        map.put(Fields.NotationFields.LAST_USER, data.getLastUser());
        map.put(Fields.NotationFields.TIME_STAMP, data.getTimestamp().toString());
        HashMap<Enum, String>[] readFromKey = CRUD.readFromKey(Tables.NOTATIONS, new String[]{data.getDate().toString(), patient.getCpr()}, null);
        if (readFromKey == null) {
            CRUD.create(Tables.NOTATIONS, map, null);
            return true;
        } else {
            CRUD.create(Tables.NOTATIONS_HISTORY, readFromKey[0], null);
            CRUD.update(Tables.NOTATIONS, new String[]{data.getDate().toString(), patient.getCpr()}, map, null);
            return true;
        }
    }

    @Override
    public boolean setAssignment(User user, Patient patient) {
        Map<Enum, String> map = new HashMap<>();
        map.put(Fields.AssignmentFields.PATIENT_ID, patient.getCpr());
        map.put(Fields.AssignmentFields.USER_ID, user.getUsername());
        sensum_bosted.PrintHandler.println(patient.getCpr() + " : " + user.getUsername());
        CRUD.create(Tables.ASSIGNMENTS, map, null);
        return true;
    }

    @Override
    public Diary getDiaryHistory(Patient patient, LocalDate date) {
        //    public Notation(String content, Date date, Notation.Field field) {
        HashMap<Enum, String>[] array = CRUD.readFromKey(Tables.NOTATIONS_HISTORY, new String[]{date.toString(), patient.getCpr()}, null);
        ArrayList<Notation> notations = new ArrayList<>();
        if (!(array == null)) {
            for (HashMap<Enum, String> map : array) {
                if (map == null) {
                    sensum_bosted.PrintHandler.print("map was null", true);
                    continue;
                }
                String content = map.get(Fields.NotationFields.CONTENT);
                String sDate = map.get(Fields.NotationFields.DATE);
                sensum_bosted.PrintHandler.print(sDate, true);
                LocalDate lDate;
                if (!(sDate == null)) {
                    lDate = LocalDate.parse(sDate);
                } else {
                    lDate = date;
                }
                String sField = map.get(Fields.NotationFields.FIELD);
                if (sField == null) {
                    continue;
                }
                Notation.Field field = Notation.Field.valueOf(sField);
                String user = map.get(Fields.NotationFields.LAST_USER);
                String sTime = map.get(Fields.NotationFields.TIME_STAMP);
                if (!(sTime == null)) {
                    LocalDateTime timestamp = LocalDateTime.parse(sTime);
                    Notation notation = new Notation(content, lDate, field, user, timestamp);
                    notations.add(notation);
                }
            }
        }
        Diary diary = new Diary(notations);

        return diary;
    }

}
