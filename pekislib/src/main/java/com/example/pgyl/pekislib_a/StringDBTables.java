package com.example.pgyl.pekislib_a;

public class StringDBTables {
    public enum TABLE_IDS {CURRENT, DEFAULT, DEFAULT_BASE, PRESET, LABEL, KEYBOARD, REGEXP, REGEXP_ERROR_MESSAGE, MIN, MAX, TIME_UNIT_PRECISION}   //  Identifiants utilisateur génériques

    public enum TABLE_EXTRA_KEYS {TABLE, INDEX, DESCRIPTION}

    //  TABLES
    private enum PEKISLIB_TABLES {
        ACTIVITY_INFOS(PekislibTableDataFields.activityInfos.class), APP_INFOS(PekislibTableDataFields.appInfos.class);

        private int dataFieldsCount;
        private String description;

        PEKISLIB_TABLES(Class<? extends PekislibTableDataFields> pekislibTableFields) {
            dataFieldsCount = pekislibTableFields.getEnumConstants().length;
        }

        public String DESCRIPTION() {
            return description;
        }

        public int INDEX() {
            return ordinal();
        }

        public int getDataFieldsCount() {
            return dataFieldsCount;
        }
    }

    public static int getPekislibTableDataFieldsCount(String tableName) {
        return PEKISLIB_TABLES.valueOf(tableName).getDataFieldsCount();
    }

    public static int getPekislibTableIndex(String tableName) {
        return PEKISLIB_TABLES.valueOf(tableName).INDEX();
    }

    public static String getPekislibTableDescription(String tableName) {
        return PEKISLIB_TABLES.valueOf(tableName).DESCRIPTION();
    }

    //  CHAMPS de DATA
    private interface PekislibTableDataFields {
        enum activityInfos implements PekislibTableDataFields {
            START_STATUS;

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur
        }

        enum appInfos implements PekislibTableDataFields {
            DATA_VERSION;

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur
        }
    }

    //region ACTIVITY_INFOS
    public enum ACTIVITY_START_STATUS {COLD, HOT}

    public static String getActivityInfosTableName() {
        return PEKISLIB_TABLES.ACTIVITY_INFOS.toString();
    }

    public static int getActivityInfosStartStatusIndex() {
        return PekislibTableDataFields.activityInfos.START_STATUS.INDEX();
    }
    //endregion

    //region APP_INFOS
    public static String getAppInfosTableName() {
        return PEKISLIB_TABLES.APP_INFOS.toString();
    }

    public static int getAppInfosDataVersionIndex() {
        return PekislibTableDataFields.appInfos.DATA_VERSION.INDEX();
    }
    //endregion

}
