package com.example.pgyl.pekislib_a;

import android.graphics.Color;

import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;

public class ButtonColorBox {   //  Pour les ImageButtonView (ou CustomButton mais uniquement unpressedBackColor et pressedBackColor)
    public enum COLOR_TYPES {
        UNPRESSED_FRONT_COLOR, UNPRESSED_BACK_COLOR, PRESSED_FRONT_COLOR, PRESSED_BACK_COLOR;

        public int INDEX() {
            return ordinal();
        }
    }

    public static class ColorDef {
        public String stringValue;   //  "RRGGBB"
        public int intValue;         //  RGB int value (avec préfixe FF)
    }

    private ColorDef[] colors;

    public ButtonColorBox() {
        init();
    }

    private void init() {
        colors = new ColorDef[COLOR_TYPES.values().length];
        for (COLOR_TYPES ct : COLOR_TYPES.values()) {
            colors[ct.INDEX()] = new ColorDef();
        }
    }

    public void close() {
        colors = null;
    }

    public void setColor(COLOR_TYPES colorType, String color) {   // color: Null interdit
        colors[colorType.INDEX()].stringValue = color;
        colors[colorType.INDEX()].intValue = Color.parseColor(COLOR_PREFIX + color);
    }

    public ColorDef getColor(COLOR_TYPES colorType) {   //  Colordef est retourné, donc avec .stringValue et .intValue
        return colors[colorType.INDEX()];
    }
}