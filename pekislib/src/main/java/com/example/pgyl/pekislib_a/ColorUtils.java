package com.example.pgyl.pekislib_a;

import android.graphics.Color;

import static com.example.pgyl.pekislib_a.Constants.COLOR_MASK;
import static com.example.pgyl.pekislib_a.Constants.HEX_RADIX;

public class ColorUtils {

    public static class ColorDef {
        public String RGBString;   //  "RRGGBB" Hex
        public int RGBInt;     //  RGB int code
    }

    public static enum BUTTON_COLOR_TYPES {   //  Destiné à ImageButtonView
        UNPRESSED_FRONT, UNPRESSED_BACK, UNPRESSED_OUTLINE, PRESSED_FRONT, PRESSED_BACK, PRESSED_OUTLINE, BACK_SCREEN, TEXT;   //  Pour la couleur du texte, utiliser setTextColor(int color) de son ancêtre TextView

        public int INDEX() {
            return ordinal();
        }
    }

    public static enum DOT_MATRIX_COLOR_TYPES {   //  Destiné à DotMatrixDisplayView
        UNPRESSED_FRONT, UNPRESSED_BACK, PRESSED_FRONT, PRESSED_BACK, BACK_SCREEN;

        public int INDEX() {
            return ordinal();
        }
    }

    public static class StateColorCode {
        int pressed;
        int unpressed;
    }

    public static String RGBToHSV(String RGBColorText) {   //  RRGGBB -> HHSSVV  (HSV dégradé, en particulier H, ramené sur 255 au lieu de 360)
        float[] hsvStruc = new float[3];

        int red = Integer.parseInt(RGBColorText.substring(0, 2), HEX_RADIX);  //  0..255
        int green = Integer.parseInt(RGBColorText.substring(2, 4), HEX_RADIX);
        int blue = Integer.parseInt(RGBColorText.substring(4, 6), HEX_RADIX);
        Color.RGBToHSV(red, green, blue, hsvStruc);
        String HSVColorText = String.format("%02X", (int) (hsvStruc[0] / 360f * 255f + 0.5f)) +
                String.format("%02X", (int) (hsvStruc[1] * 255f + 0.5f)) +
                String.format("%02X", (int) (hsvStruc[2] * 255f + 0.5f));
        hsvStruc = null;
        return HSVColorText;
    }

    public static String HSVToRGB(String HSVColorText) {   //  HHSSVV -> RRGGBB  (HSV dégradé, en particulier H, ramené sur 255 au lieu de 360)
        float[] hsvStruc = new float[3];

        int h = Integer.parseInt(HSVColorText.substring(0, 2), HEX_RADIX);  //  0..255
        int s = Integer.parseInt(HSVColorText.substring(2, 4), HEX_RADIX);
        int v = Integer.parseInt(HSVColorText.substring(4, 6), HEX_RADIX);
        hsvStruc[0] = (float) h * 360f / 255f;
        hsvStruc[1] = (float) s / 255f;
        hsvStruc[2] = (float) v / 255f;
        String RGBColorText = String.format("%06X", Color.HSVToColor(hsvStruc) & COLOR_MASK);
        hsvStruc = null;
        return RGBColorText;
    }

}
