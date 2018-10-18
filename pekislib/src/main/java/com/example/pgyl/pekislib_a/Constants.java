package com.example.pgyl.pekislib_a;

public class Constants {
    public enum PEKISLIB_ACTIVITIES {PRESETS, INPUTBUTTONS, COLORPICKER, HELP}

    public enum PEKISLIB_TABLES {ACTIVITY_INFOS}

    public enum TABLE_IDS {CURRENT, PRESET, DEFAULT, LABEL, KEYBOARD, REGEXP, MIN, MAX, TIMEUNIT}

    public enum ACTIVITY_START_TYPE {COLD, HOT}

    public enum TABLE_ACTIVITY_INFOS_DATA_FIELDS {
        START_TYPE(1);

        private int valueIndex;

        TABLE_ACTIVITY_INFOS_DATA_FIELDS(int valueIndex) {
            this.valueIndex = valueIndex;
        }

        public int INDEX() {
            return valueIndex;
        }
    }

    public enum TABLE_EXTRA_KEYS {TABLE, INDEX}

    public enum ACTIVITY_EXTRA_KEYS {TITLE}

    public enum BUTTON_STATES {
        UNPRESSED(null), PRESSED("FF9A22");

        private String valueDefaultColor;

        BUTTON_STATES(String valueDefaultColor) {
            this.valueDefaultColor = valueDefaultColor;
        }

        public String DEFAULT_COLOR() {
            return valueDefaultColor;
        }
    }

    public static final int OFF_VALUE = 0;
    public static final int ON_VALUE = 1;
    public static final int DUMMY_VALUE = -1;
    public static final int NOT_FOUND = -1;
    public static final int UNDEFINED = -1;
    public static final int ERROR_VALUE = -1;
    public static final String CRLF = "\r\n";
    public static final String COLOR_PREFIX = "#FF";
    public static final String TABLE_COLORS_REGEXP_HEX_DEFAULT = ".{6}";  // Pour valider 6 caractères HEX dans INPUTBUTTONS pour la table COLORS (RRGGBB)
    public static String SHP_FILE_NAME_SUFFIX = "_preferences";
    //region Constantes

}
