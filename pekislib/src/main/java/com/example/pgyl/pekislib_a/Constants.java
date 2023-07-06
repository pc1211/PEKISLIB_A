package com.example.pgyl.pekislib_a;

public class Constants {
    public enum PEKISLIB_ACTIVITIES {
        PRESETS, INPUT_BUTTONS, COLOR_PICKER, HELP;

        public int INDEX() {
            return ordinal();
        }
    }

    public enum ACTIVITY_EXTRA_KEYS {TITLE}

    public enum BUTTON_STATES {UNPRESSED, PRESSED}

    public enum SWITCHES {ON, OFF}

    public static final String REGEXP_SIX_CHARS = ".{6}";  //  6 caractères
    public static final String REGEXP_SIX_CHARS_ERROR_MESSAGE = "Required: 6 chars";
    public static final String REGEXP_PERCENT = "^[0]*(100|[1-9]?[0-9])$";  //  Entier de 0 à 100 (avec 1 ou plusieurs 0 au début)
    public static final String REGEXP_PERCENT_ERROR_MESSAGE = "Required: Integer from 0 to 100";
    public static final String REGEXP_POSITIVE_INTEGER = "^[0]*[1-9][0-9]*$";   //  Entier positif (>0) (avec 1 ou plusieurs 0 au début)
    public static final String REGEXP_POSITIVE_INTEGER_ERROR_MESSAGE = "Required: Positive integer";
    public static final String REGEXP_INTEGER_FROM_0 = "^[0-9]+";   //  Entier (>=0)
    public static final String REGEXP_INTEGER_FROM_0_ERROR_MESSAGE = "Required: integer (From 0)";
    public static final String REGEXP_MIN_ONE_CHAR = ".+";  //  Minimum 1 char
    public static final String REGEXP_MIN_ONE_CHAR_ERROR_MESSAGE = "Required: At least 1 char";  //  Minimum 1 char
    public static final int DUMMY_VALUE = -1;
    public static final int NOT_FOUND = -1;
    public static final int UNDEFINED = -1;
    public static final int ERROR_VALUE = -1;
    public static final String CRLF = "\r\n";
    public static final int HEX_RADIX = 16;
    public static final String COLOR_PREFIX = "#FF";
    public static final int COLOR_MASK = 0x00FFFFFF;
    public static final int COLOR_INVERTER = 0x808080;
    public static String SHP_FILE_NAME_SUFFIX = "_preferences";

}
