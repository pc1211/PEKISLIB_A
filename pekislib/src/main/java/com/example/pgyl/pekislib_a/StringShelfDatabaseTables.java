package com.example.pgyl.pekislib_a;

public class StringShelfDatabaseTables {
    //  TABLES
    private enum PekisLibTables {
        ACTIVITY_INFOS(PekislibTableDataFields.activityInfos.class);

        private int dataFieldsCount;

        PekisLibTables(Class<? extends PekislibTableDataFields> pekislibTableFields) {
            dataFieldsCount = pekislibTableFields.getEnumConstants().length;
        }

        public int getDataFieldsCount() {
            return dataFieldsCount;
        }
    }

    //  CHAMPS de DATA
    private interface PekislibTableDataFields {
        enum activityInfos implements PekislibTableDataFields {
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
        return PekisLibTables.valueOf(tableName).getDataFieldsCount();
    }

    //region ACTIVITY_INFOS
    public static String getActivityInfosTableName() {
        return PekisLibTables.ACTIVITY_INFOS.toString();
    }

    public static int getActivityInfosStartStatusIndex() {
        return PekislibTableDataFields.activityInfos.START_STATUS.INDEX();
    }
    //endregion


}
