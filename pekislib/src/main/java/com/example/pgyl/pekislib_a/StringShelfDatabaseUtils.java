package com.example.pgyl.pekislib_a;

import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.TABLE_IDS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getActivityInfosStartStatusIndex;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getActivityInfosTableName;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getPekislibTableDataFieldsCount;

public class StringShelfDatabaseUtils {

    public static void createPekislibTableIfNotExists(StringShelfDatabase stringShelfDatabase, String tableName) {
        stringShelfDatabase.createTableIfNotExists(tableName, 1 + getPekislibTableDataFieldsCount(tableName));   //  Champ ID + Données
    }

    public static String[] getCurrentValuesInActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + activityName);
    }

    public static void setCurrentValuesInActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName, String[] values) {
        stringShelfDatabase.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString() + activityName, values);
    }

    public static String getCurrentValueInActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + activityName, index);
    }

    public static void setCurrentValueInActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName, int index, String value) {
        stringShelfDatabase.insertOrReplaceFieldById(tableName, TABLE_IDS.CURRENT.toString() + activityName, index, value);
    }

    public static String[] getCurrents(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString());
    }

    public static void setCurrents(StringShelfDatabase stringShelfDatabase, String tableName, String[] values) {
        stringShelfDatabase.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString(), values);
    }

    public static String getCurrent(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString(), index);
    }

    public static void setCurrent(StringShelfDatabase stringShelfDatabase, String tableName, int index, String value) {
        stringShelfDatabase.insertOrReplaceFieldById(tableName, TABLE_IDS.CURRENT.toString(), index, value);
    }

    public static String[] getLabels(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.LABEL.toString());
    }

    public static String getKeyboard(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.KEYBOARD.toString(), index);
    }

    public static String[] getKeyboards(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.KEYBOARD.toString());
    }

    public static String getTimeUnit(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.TIMEUNIT.toString(), index);
    }

    public static String[] getTimeUnits(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.TIMEUNIT.toString());
    }

    public static String getMin(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.MIN.toString(), index);
    }

    public static String getMax(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.MAX.toString(), index);
    }

    public static String getRegExp(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.REGEXP.toString(), index);
    }

    public static String[] getDefaults(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.DEFAULT.toString());
    }

    public static void setDefaults(StringShelfDatabase stringShelfDatabase, String tableName, String[] values) {
        stringShelfDatabase.insertOrReplaceRowById(tableName, TABLE_IDS.DEFAULT.toString(), values);
    }

    //region ACTIVITY_INFOS
    public static boolean isColdStartStatusInActivity(StringShelfDatabase stringShelfDatabase, String activityName) {
        return stringShelfDatabase.selectFieldByIdOrCreate(getActivityInfosTableName(), activityName, getActivityInfosStartStatusIndex()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusInActivity(StringShelfDatabase stringShelfDatabase, String activityName, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(getActivityInfosTableName(), activityName, getActivityInfosStartStatusIndex(), activityStartStatus.toString());
    }
    //endregion

}
