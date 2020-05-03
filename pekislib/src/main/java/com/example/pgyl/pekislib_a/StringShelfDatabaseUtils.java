package com.example.pgyl.pekislib_a;

import java.util.Arrays;

import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.TABLE_IDS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getActivityInfosStartStatusIndex;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getActivityInfosTableName;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getPekislibTableDataFieldsCount;

public class StringShelfDatabaseUtils {

    public static void createPekislibTableIfNotExists(StringShelfDatabase stringShelfDatabase, String tableName) {
        stringShelfDatabase.createTableIfNotExists(tableName, 1 + getPekislibTableDataFieldsCount(tableName));   //  Champ ID + Données
    }

    public static int getTableIndex(String[] tableNames, String tableName) {
        return Arrays.asList(tableNames).indexOf(tableName);
    }

    public static String[][] getCurrentsFromMultipleTablesFromActivity(StringShelfDatabase stringShelfDatabase, String[] tableNames, String activityName) {
        String values[][] = new String[tableNames.length][];
        for (int i = 0; i <= (tableNames.length - 1); i = i + 1) {
            values[i] = getCurrentsFromActivity(stringShelfDatabase, activityName, tableNames[i]);
        }
        return values;
    }

    public static void setCurrentsForMultipleTablesForActivity(StringShelfDatabase stringShelfDatabase, String[] tableNames, String activityName, String[][] values) {
        for (int i = 0; i <= (tableNames.length - 1); i = i + 1) {
            setCurrentsForActivity(stringShelfDatabase, activityName, tableNames[i], values[i]);
        }
    }

    public static String[][] getFieldLabelsFromMultipleTables(StringShelfDatabase stringShelfDatabase, String[] tableNames) {
        String values[][] = new String[tableNames.length][];
        for (int i = 0; i <= (tableNames.length - 1); i = i + 1) {
            values[i] = getLabels(stringShelfDatabase, tableNames[i]);
        }
        return values;
    }

    public static String[] getCurrentsFromActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + activityName);
    }

    public static void setCurrentsForActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName, String[] values) {
        stringShelfDatabase.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString() + activityName, values);
    }

    public static String getCurrentFromActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + activityName, index);
    }

    public static void setCurrentForActivity(StringShelfDatabase stringShelfDatabase, String activityName, String tableName, int index, String value) {
        stringShelfDatabase.insertOrReplaceFieldById(tableName, TABLE_IDS.CURRENT.toString() + activityName, index, value);
    }

    public static String[] getCurrents(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString());
    }

    public static void setCurrents(StringShelfDatabase stringShelfDatabase, String tableName, String[] values) {
        stringShelfDatabase.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString(), values);
    }

    public static String getCurrents(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
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
    public static boolean isColdStartStatusOfActivity(StringShelfDatabase stringShelfDatabase, String activityName) {
        return stringShelfDatabase.selectFieldByIdOrCreate(getActivityInfosTableName(), activityName, getActivityInfosStartStatusIndex()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusOfActivity(StringShelfDatabase stringShelfDatabase, String activityName, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(getActivityInfosTableName(), activityName, getActivityInfosStartStatusIndex(), activityStartStatus.toString());
    }
    //endregion

}
