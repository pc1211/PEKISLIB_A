package com.example.pgyl.pekislib_a;

import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;

public class DotMatrixFontUtils {

    public static BiDimensions getFontTextDimensions(String text, DotMatrixFont dotMatrixFont) {
        return getFontTextDimensions(text, null, dotMatrixFont);
    }

    public static BiDimensions getFontTextDimensions(String text, DotMatrixFont extraFont, DotMatrixFont defaultFont) {   //  Spécifier extraFont différent de null si text mélange extraFont et defaultFont; extraFont a la priorité sur defaultFont
        String extraFontText = "";
        String defaultFontText = "";

        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            char ch = text.charAt(i);
            DotMatrixSymbol symbol = null;
            if (extraFont != null) {
                symbol = extraFont.getSymbolByCode((int) ch);   //  Conversion ASCII
            }
            if (symbol != null) {
                extraFontText = extraFontText + ch;
            } else {
                defaultFontText = defaultFontText + ch;
            }
        }

        BiDimensions fontTextDimensions = new BiDimensions(0, 0);
        BiDimensions defaultFontTextDimensions = defaultFont.getTextDimensions(defaultFontText);
        if (extraFont != null) {
            BiDimensions extraFontTextDimensions = extraFont.getTextDimensions(extraFontText);
            fontTextDimensions.set(defaultFontTextDimensions.width + extraFontTextDimensions.width, Math.max(defaultFontTextDimensions.height, extraFontTextDimensions.height));
        } else {
            fontTextDimensions.set(defaultFontTextDimensions.width, defaultFontTextDimensions.height);
        }

        return fontTextDimensions;
    }

}
