package com.example.pgyl.pekislib_a;

import java.util.Arrays;

import static com.example.pgyl.pekislib_a.StringDBTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringDBTables.TABLE_IDS;
import static com.example.pgyl.pekislib_a.StringDBTables.getActivityInfosStartStatusIndex;
import static com.example.pgyl.pekislib_a.StringDBTables.getActivityInfosTableName;
import static com.example.pgyl.pekislib_a.StringDBTables.getPekislibTableDataFieldsCount;

public class StringDBUtils {

    public static void createPekislibTableIfNotExists(StringDB stringDB, String tableName) {
        stringDB.createTableIfNotExists(tableName, 1 + getPekislibTableDataFieldsCount(tableName));   //  Champ ID + Données
    }

    public static int getTableIndex(String[] tableNames, String tableName) {
        return Arrays.asList(tableNames).indexOf(tableName);
    }

    public static String[][] getCurrentsFromMultipleTablesFromActivity(StringDB stringDB, String activityName, String[] tableNames) {
        String values[][] = new String[tableNames.length][];
        for (int i = 0; i <= (tableNames.length - 1); i = i + 1) {
            values[i] = getCurrentsFromActivity(stringDB, activityName, tableNames[i]);
        }
        return values;
    }

    public static void setCurrentsForMultipleTablesForActivity(StringDB stringDB, String activityName, String[] tableNames, String[][] values) {
        for (int i = 0; i <= (tableNames.length - 1); i = i + 1) {
            setCurrentsForActivity(stringDB, activityName, tableNames[i], values[i]);
        }
    }

    public static String[][] getFieldLabelsFromMultipleTables(StringDB stringDB, String[] tableNames) {
        String values[][] = new String[tableNames.length][];
        for (int i = 0; i <= (tableNames.length - 1); i = i + 1) {
            values[i] = getLabels(stringDB, tableNames[i]);
        }
        return values;
    }

    public static String[] getCurrentsFromActivity(StringDB stringDB, String activityName, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + activityName);
    }

    public static void setCurrentsForActivity(StringDB stringDB, String activityName, String tableName, String[] values) {
        stringDB.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString() + activityName, values);
    }

    public static String getCurrentFromActivity(StringDB stringDB, String activityName, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + activityName, index);
    }

    public static void setCurrentForActivity(StringDB stringDB, String activityName, String tableName, int index, String value) {
        stringDB.insertOrReplaceFieldById(tableName, TABLE_IDS.CURRENT.toString() + activityName, index, value);
    }

    public static String[] getCurrents(StringDB stringDB, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString());
    }

    public static void setCurrents(StringDB stringDB, String tableName, String[] values) {
        stringDB.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString(), values);
    }

    public static String getCurrents(StringDB stringDB, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString(), index);
    }

    public static void setCurrent(StringDB stringDB, String tableName, int index, String value) {
        stringDB.insertOrReplaceFieldById(tableName, TABLE_IDS.CURRENT.toString(), index, value);
    }

    public static String[] getLabels(StringDB stringDB, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.LABEL.toString());
    }

    public static String getKeyboard(StringDB stringDB, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.KEYBOARD.toString(), index);
    }

    public static String[] getKeyboards(StringDB stringDB, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.KEYBOARD.toString());
    }

    public static String getTimeUnitPrecision(StringDB stringDB, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.TIME_UNIT_PRECISION.toString(), index);
    }

    public static String[] getTimeUnitPrecisions(StringDB stringDB, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.TIME_UNIT_PRECISION.toString());
    }

    public static String getMin(StringDB stringDB, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.MIN.toString(), index);
    }

    public static String getMax(StringDB stringDB, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.MAX.toString(), index);
    }

    public static String[] getMaxs(StringDB stringDB, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.MAX.toString());
    }

    public static String getRegExp(StringDB stringDB, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.REGEXP.toString(), index);
    }

    public static String getRegExpErrorMessage(StringDB stringDB, String tableName, int index) {
        return stringDB.selectFieldByIdOrCreate(tableName, TABLE_IDS.REGEXP_ERROR_MESSAGE.toString(), index);
    }

    public static String[] getDefaults(StringDB stringDB, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.DEFAULT.toString());
    }

    public static String[] getDefaultsBase(StringDB stringDB, String tableName) {
        return stringDB.selectRowByIdOrCreate(tableName, TABLE_IDS.DEFAULT_BASE.toString());
    }

    public static void setDefaults(StringDB stringDB, String tableName, String[] values) {
        stringDB.insertOrReplaceRowById(tableName, TABLE_IDS.DEFAULT.toString(), values);
    }

    //region ACTIVITY_INFOS
    public static boolean isColdStartStatusOfActivity(StringDB stringDB, String activityName) {
        return stringDB.selectFieldByIdOrCreate(getActivityInfosTableName(), activityName, getActivityInfosStartStatusIndex()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusOfActivity(StringDB stringDB, String activityName, ACTIVITY_START_STATUS activityStartStatus) {
        stringDB.insertOrReplaceFieldById(getActivityInfosTableName(), activityName, getActivityInfosStartStatusIndex(), activityStartStatus.toString());
    }
    //endregion

}
