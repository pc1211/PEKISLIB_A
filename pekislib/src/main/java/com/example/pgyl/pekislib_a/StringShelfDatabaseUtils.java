package com.example.pgyl.pekislib_a;

import com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITIES;

import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.TABLE_IDS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getActivityInfosStartStatusIndex;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getActivityInfosTableName;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.getPekislibTableDataFieldsCount;

public class StringShelfDatabaseUtils {

    public static void createPekislibTableIfNotExists(StringShelfDatabase stringShelfDatabase, String tableName) {
        stringShelfDatabase.createTableIfNotExists(tableName, 1 + getPekislibTableDataFieldsCount(tableName));   //  Champ ID + Données
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

    //region COLORS
    public static String[] getCurrentColorsInColorPickerActivity(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + PEKISLIB_ACTIVITIES.COLOR_PICKER.toString());
    }

    public static void setCurrentColorsInColorPickerActivity(StringShelfDatabase stringShelfDatabase, String tableName, String[] colors) {
        stringShelfDatabase.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString() + PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), colors);
    }
    //endregion

    //region START_STATUS
    public static boolean isColdStartStatusInColorPickerActivity(StringShelfDatabase stringShelfDatabase) {
        return stringShelfDatabase.selectFieldByIdOrCreate(getActivityInfosTableName(), PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), getActivityInfosStartStatusIndex()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusInColorPickerActivity(StringShelfDatabase stringShelfDatabase, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(getActivityInfosTableName(), PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), getActivityInfosStartStatusIndex(), activityStartStatus.toString());
    }

    public static boolean isColdStartStatusInPresetsActivity(StringShelfDatabase stringShelfDatabase) {
        return stringShelfDatabase.selectFieldByIdOrCreate(getActivityInfosTableName(), PEKISLIB_ACTIVITIES.PRESETS.toString(), getActivityInfosStartStatusIndex()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusInPresetsActivity(StringShelfDatabase stringShelfDatabase, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(getActivityInfosTableName(), PEKISLIB_ACTIVITIES.PRESETS.toString(), getActivityInfosStartStatusIndex(), activityStartStatus.toString());
    }

    public static boolean isColdStartStatusInInputButtonsActivity(StringShelfDatabase stringShelfDatabase) {
        return stringShelfDatabase.selectFieldByIdOrCreate(getActivityInfosTableName(), PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), getActivityInfosStartStatusIndex()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusInInputButtonsActivity(StringShelfDatabase stringShelfDatabase, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(getActivityInfosTableName(), PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), getActivityInfosStartStatusIndex(), activityStartStatus.toString());
    }
    //endregion

    //region INPUT_BUTTONS
    public static String getCurrentEntryInInputButtonsActivity(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), index);
    }

    public static void setCurrentEntryInInputButtonsActivity(StringShelfDatabase stringShelfDatabase, String tableName, int index, String value) {
        stringShelfDatabase.insertOrReplaceFieldById(tableName, TABLE_IDS.CURRENT.toString() + PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), index, value);
    }
    //endregion

    //region PRESETS
    public static String[] getCurrentPresetInPresetsActivity(StringShelfDatabase stringShelfDatabase, String tableName) {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + PEKISLIB_ACTIVITIES.PRESETS.toString());
    }

    public static void setCurrentPresetInPresetsActivity(StringShelfDatabase stringShelfDatabase, String tableName, String[] values) {
        stringShelfDatabase.insertOrReplaceRowById(tableName, TABLE_IDS.CURRENT.toString() + PEKISLIB_ACTIVITIES.PRESETS.toString(), values);
    }
    //endregion

}
