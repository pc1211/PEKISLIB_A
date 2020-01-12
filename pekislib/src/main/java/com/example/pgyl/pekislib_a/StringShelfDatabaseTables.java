package com.example.pgyl.pekislib_a;

public class StringShelfDatabaseTables {

    private enum PEKISLIB_TABLES {   //  "Enum of enums"
        ACTIVITY_INFOS(PekislibTableFields.activityInfos.class);

        private PekislibTableFields[] tableFields;

        PEKISLIB_TABLES(Class<? extends PekislibTableFields> pekislibTableFields) {
            tableFields = pekislibTableFields.getEnumConstants();
        }

        public PekislibTableFields[] TABLE_FIELDS() {
            return tableFields;
        }
    }

    private interface PekislibTableFields {
        enum activityInfos implements PekislibTableFields {
            START_STATUS;

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur
        }
    }

    public enum TABLE_IDS {CURRENT, DEFAULT, PRESET, LABEL, KEYBOARD, REGEXP, MIN, MAX, TIMEUNIT}   //  Identifiants utilisateur génériques

    public enum ACTIVITY_START_STATUS {COLD, HOT}

    public enum TABLE_EXTRA_KEYS {TABLE, INDEX}

    public static int getPekislibTableDataFieldsCount(String tableName) {
        return PEKISLIB_TABLES.valueOf(tableName).TABLE_FIELDS().length;
    }

    //region ACTIVITY_INFOS
    public static String getActivityInfosTableName() {
        return PEKISLIB_TABLES.ACTIVITY_INFOS.toString();
    }

    public static int getActivityInfosStartStatusIndex() {
        return PekislibTableFields.activityInfos.START_STATUS.INDEX();
    }
    //endregion


}
