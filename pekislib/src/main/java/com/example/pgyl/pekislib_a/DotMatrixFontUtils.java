package com.example.pgyl.pekislib_a;

import java.nio.charset.StandardCharsets;

import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;

public class DotMatrixFontUtils {

    public static BiDimensions getFontTextDimensions(String text, DotMatrixFont dotMatrixFont) {
        return getFontTextDimensions(text, null, dotMatrixFont);
    }

    public static BiDimensions getFontTextDimensions(String text, DotMatrixFont extraFont, DotMatrixFont defaultFont) {   //  Spécifier extraFont différent de null si text mélange extraFont et defaultFont; extraFont a la priorité sur defaultFont
        String extraFontText = "";
        String defaultFontText = "";

        DotMatrixFont font = null;
        byte[] textBytes = text.getBytes(StandardCharsets.US_ASCII);   //  Conversion ASCII
        for (int i = 0; i <= (textBytes.length - 1); i = i + 1) {
            String textchar = text.substring(i, i + 1);
            DotMatrixSymbol symbol = null;
            if (extraFont != null) {
                symbol = extraFont.getSymbolByCode((int) textBytes[i]);
            }
            if (symbol != null) {
                extraFontText = extraFontText + textchar;
            } else {
                defaultFontText = defaultFontText + textchar;
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
