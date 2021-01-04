package com.example.pgyl.pekislib_a;

import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;

public class DotMatrixFontUtils {

    public static BiDimensions getFontTextDimensions(String text, DotMatrixFont dotMatrixFont) {
        return getFontTextDimensions(text, null, dotMatrixFont);
    }

    public static BiDimensions getFontTextDimensions(String text, DotMatrixFont extraFont, DotMatrixFont defaultFont) {   //  Spécifier extraFont différent de null si text mélange extraFont et defaultFont; extraFont a la priorité sur defaultFont
        BiDimensions fontTextDimensions;
        DotMatrixSymbol symbol;

        String extraFontText = "";
        String defaultFontText = "";
        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            String t = text.substring(i, i + 1);
            Character ch = text.charAt(i);
            symbol = null;
            if (extraFont != null) {
                symbol = extraFont.getSymbol(ch);
            }
            if (symbol != null) {
                extraFontText = extraFontText + t;
            } else {
                defaultFontText = defaultFontText + t;
            }
        }
        fontTextDimensions = defaultFont.getTextDimensions(defaultFontText);
        if (extraFont != null) {
            fontTextDimensions.set(fontTextDimensions.width + extraFont.getTextDimensions(extraFontText).width, Math.max(extraFont.getTextDimensions(extraFontText).height, fontTextDimensions.height));
        }
        return fontTextDimensions;
    }

}
