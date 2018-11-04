package com.example.pgyl.pekislib_a;

import com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITIES;

public class StringShelfDatabaseUtils {

    private enum PEKISLIB_TABLES {ACTIVITY_INFOS}

    private enum TABLE_IDS {CURRENT, DEFAULT, LABEL, KEYBOARD, REGEXP, MIN, MAX, TIMEUNIT}

    private enum TABLE_ACTIVITY_INFOS_DATA_FIELDS {
        START_STATUS(1);

        private int valueIndex;

        TABLE_ACTIVITY_INFOS_DATA_FIELDS(int valueIndex) {
            this.valueIndex = valueIndex;
        }

        public int INDEX() {
            return valueIndex;
        }
    }

    public enum ACTIVITY_START_STATUS {COLD, HOT}

    public enum TABLE_EXTRA_KEYS {TABLE, INDEX}

    //region TABLES
    public static boolean createTableActivityInfosIfNotExists(StringShelfDatabase stringShelfDatabase) {
        boolean ret = false;
        if (!stringShelfDatabase.tableExists(PEKISLIB_TABLES.ACTIVITY_INFOS.toString())) {
            stringShelfDatabase.createTable(PEKISLIB_TABLES.ACTIVITY_INFOS.toString(), 1 + TABLE_ACTIVITY_INFOS_DATA_FIELDS.values().length);   //  Champ ID + Données
            ret = true;
        }
        return ret;
    }

    public static String getActivityInfosTableName() {
        return PEKISLIB_TABLES.ACTIVITY_INFOS.toString();
    }
    //endregion

    //region TABLE_IDS
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

    public static String getDefault(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.DEFAULT.toString(), index);
    }

    public static String getLabelIdName() {
        return TABLE_IDS.LABEL.toString();
    }

    public static String getKeyboardIdName() {
        return TABLE_IDS.KEYBOARD.toString();
    }

    public static String getRegexpIdName() {
        return TABLE_IDS.REGEXP.toString();
    }

    public static String getMaxIdName() {
        return TABLE_IDS.MAX.toString();
    }

    public static String getTimeUnitIdName() {
        return TABLE_IDS.TIMEUNIT.toString();
    }

    public static String getDefaultIdName() {
        return TABLE_IDS.DEFAULT.toString();
    }

    public static String getCurrentIdName() {
        return TABLE_IDS.CURRENT.toString();
    }

    public static int getActivityInfosStartStatusIndex() {
        return TABLE_ACTIVITY_INFOS_DATA_FIELDS.START_STATUS.INDEX();
    }
    //endregion

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
        return stringShelfDatabase.selectFieldByIdOrCreate(PEKISLIB_TABLES.ACTIVITY_INFOS.toString(), PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), TABLE_ACTIVITY_INFOS_DATA_FIELDS.START_STATUS.INDEX()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusInColorPickerActivity(StringShelfDatabase stringShelfDatabase, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(PEKISLIB_TABLES.ACTIVITY_INFOS.toString(), PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), TABLE_ACTIVITY_INFOS_DATA_FIELDS.START_STATUS.INDEX(), activityStartStatus.toString());
    }

    public static boolean isColdStartStatusInPresetsActivity(StringShelfDatabase stringShelfDatabase) {
        return stringShelfDatabase.selectFieldByIdOrCreate(PEKISLIB_TABLES.ACTIVITY_INFOS.toString(), PEKISLIB_ACTIVITIES.PRESETS.toString(), TABLE_ACTIVITY_INFOS_DATA_FIELDS.START_STATUS.INDEX()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusInPresetsActivity(StringShelfDatabase stringShelfDatabase, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(PEKISLIB_TABLES.ACTIVITY_INFOS.toString(), PEKISLIB_ACTIVITIES.PRESETS.toString(), TABLE_ACTIVITY_INFOS_DATA_FIELDS.START_STATUS.INDEX(), activityStartStatus.toString());
    }

    public static boolean isColdStartStatusInInputButtonsActivity(StringShelfDatabase stringShelfDatabase) {
        return stringShelfDatabase.selectFieldByIdOrCreate(PEKISLIB_TABLES.ACTIVITY_INFOS.toString(), PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), TABLE_ACTIVITY_INFOS_DATA_FIELDS.START_STATUS.INDEX()).equals(ACTIVITY_START_STATUS.COLD.toString());
    }

    public static void setStartStatusInInputButtonsActivity(StringShelfDatabase stringShelfDatabase, ACTIVITY_START_STATUS activityStartStatus) {
        stringShelfDatabase.insertOrReplaceFieldById(PEKISLIB_TABLES.ACTIVITY_INFOS.toString(), PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), TABLE_ACTIVITY_INFOS_DATA_FIELDS.START_STATUS.INDEX(), activityStartStatus.toString());
    }
    //endregion

    //region INPUT_BUTTONS
    public static String getCurrentStringInInputButtonsActivity(StringShelfDatabase stringShelfDatabase, String tableName, int index) {
        return stringShelfDatabase.selectFieldByIdOrCreate(tableName, TABLE_IDS.CURRENT.toString() + PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), index);
    }

    public static void setCurrentStringInInputButtonsActivity(StringShelfDatabase stringShelfDatabase, String tableName, int index, String value) {
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
