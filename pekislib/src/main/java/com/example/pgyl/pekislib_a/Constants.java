package com.example.pgyl.pekislib_a;

public class Constants {
    public enum PEKISLIB_ACTIVITIES {
        PRESETS, INPUT_BUTTONS, COLOR_PICKER, HELP;

        public int INDEX() {
            return ordinal();
        }
    }

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

    public static final int DUMMY_VALUE = -1;
    public static final int NOT_FOUND = -1;
    public static final int UNDEFINED = -1;
    public static final int ERROR_VALUE = -1;
    public static final String CRLF = "\r\n";
    public static final int HEX_RADIX = 16;
    public static final String COLOR_PREFIX = "#FF";
    public static final int COLOR_INVERTER_XOR = 0x00FFFFFF;
    public static final int COLOR_DARKENER_AND = 0xFF808080;
    public static final int COLOR_MASK_AND = 0x00FFFFFF;
    public static String SHP_FILE_NAME_SUFFIX = "_preferences";

}
