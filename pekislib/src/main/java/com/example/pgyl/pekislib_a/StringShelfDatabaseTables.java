package com.example.pgyl.pekislib_a;

public class StringShelfDatabaseTables {

    public enum PEKISLIB_TABLES {   //  "Enum of enums"
        ACTIVITY_INFOS(PekislibTableAndFields.activityInfos.class);

        private PekislibTableAndFields[] tableFields;

        PEKISLIB_TABLES(Class<? extends PekislibTableAndFields> pekislibTableFields) {
            tableFields = pekislibTableFields.getEnumConstants();
        }

        public PekislibTableAndFields[] TABLE_FIELDS() {
            return tableFields;
        }
    }

    public interface PekislibTableAndFields {
        enum activityInfos implements PekislibTableAndFields {
            START_STATUS;

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur
        }
    }

    public static int getPekislibTableDataFieldsCount(String tableName) {
        return PEKISLIB_TABLES.valueOf(tableName).TABLE_FIELDS().length;
    }

    //region ACTIVITY_INFOS
    public static String getActivityInfosTableName() {
        return PEKISLIB_TABLES.ACTIVITY_INFOS.toString();
    }

    public static int getActivityInfosStartStatusIndex() {
        return PekislibTableAndFields.activityInfos.START_STATUS.INDEX();
    }
    //endregion


}
