package com.example.pgyl.pekislib_a;

public class DotMatrixFontUtils {

    public static int getTextWidth(String text, DotMatrixFont dotMatrixFont) {
        return getTextWidth(text, null, dotMatrixFont);
    }

    public static int getTextWidth(String text, DotMatrixFont extraFont, DotMatrixFont defaultFont) {    //  Spécifier extraFont différent de null si text mélange extraFont et defaultFont; extraFont a la priorité sur defaultFont
        int textWidth;
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
        textWidth = defaultFont.getTextWidth(defaultFontText);
        if (extraFont != null) {
            textWidth = textWidth + extraFont.getTextWidth(extraFontText);
        }
        return textWidth;
    }

    public static int getTextHeight(String text, DotMatrixFont dotMatrixFont) {
        return getTextHeight(text, null, dotMatrixFont);
    }

    public static int getTextHeight(String text, DotMatrixFont extraFont, DotMatrixFont defaultFont) {   //  Spécifier extraFont différent de null si text mélange extraFont et defaultFont; extraFont a la priorité sur defaultFont
        int textHeight;
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
        textHeight = defaultFont.getTextHeight(defaultFontText);
        if (extraFont != null) {
            textHeight = Math.max(extraFont.getTextHeight(extraFontText), textHeight);
        }
        return textHeight;
    }

    public static DotMatrixFont getDefaultFont() {
        DotMatrixFont defaultFont;

        final DotMatrixSymbol[] DEFAULT_FONT_SYMBOLS = {  //  En matrice 5x7 ou autre
                new DotMatrixSymbol(' ', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('!', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('\"', new int[][]{{0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('#', new int[][]{{0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}}),
                new DotMatrixSymbol('$', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 1, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 1, 0, 1}, {1, 1, 1, 1, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('%', new int[][]{{1, 1, 0, 0, 0}, {1, 1, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 1, 1}, {0, 0, 0, 1, 1}}),
                new DotMatrixSymbol('&', new int[][]{{0, 1, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 1, 0, 1}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 1}}),
                new DotMatrixSymbol('\'', new int[][]{{0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('(', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol(')', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('*', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {1, 0, 1, 0, 1}, {0, 1, 1, 1, 0}, {1, 0, 1, 0, 1}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('+', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol(',', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('-', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('.', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol('/', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('0', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('1', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('2', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('3', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('4', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 1, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('5', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('6', new int[][]{{0, 0, 1, 1, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('7', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('8', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('9', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol(':', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol(';', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('<', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('=', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('>', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('?', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('@', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('A', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('B', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
                new DotMatrixSymbol('C', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('D', new int[][]{{1, 1, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 0}, {1, 1, 1, 0, 0}}),
                new DotMatrixSymbol('E', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('F', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
                new DotMatrixSymbol('G', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
                new DotMatrixSymbol('H', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('I', new int[][]{{0, 1, 1, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('J', new int[][]{{0, 0, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol('K', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 1, 0}, {1, 0, 1, 0, 0}, {1, 1, 0, 0, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('L', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('M', new int[][]{{1, 0, 0, 0, 1}, {1, 1, 0, 1, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('N', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('O', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('P', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
                new DotMatrixSymbol('Q', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 1}}),
                new DotMatrixSymbol('R', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('S', new int[][]{{0, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
                new DotMatrixSymbol('T', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('U', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('V', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('W', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 1, 0, 1, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('X', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('Y', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('Z', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('[', new int[][]{{0, 1, 1, 1, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('\\', new int[][]{{0, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol(']', new int[][]{{0, 1, 1, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('^', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('_', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('`', new int[][]{{0, 1, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('a', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
                new DotMatrixSymbol('b', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('c', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('d', new int[][]{{0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
                new DotMatrixSymbol('e', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('f', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 1}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('g', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('h', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('i', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('j', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol('k', new int[][]{{0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 1, 1, 0, 0}, {0, 1, 0, 1, 0}, {0, 1, 0, 0, 1}}),
                new DotMatrixSymbol('l', new int[][]{{0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 1, 0}}),
                new DotMatrixSymbol('m', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 0, 1, 0}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}}),
                new DotMatrixSymbol('n', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('o', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('p', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
                new DotMatrixSymbol('q', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}}),
                new DotMatrixSymbol('r', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 0, 1, 1}, {0, 1, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('s', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
                new DotMatrixSymbol('t', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 1}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('u', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 1}, {0, 1, 1, 0, 1}}),
                new DotMatrixSymbol('v', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('w', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 0, 1, 0}}),
                new DotMatrixSymbol('x', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 0, 0, 1}, {0, 0, 1, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 0, 0}, {1, 0, 0, 1, 1}}),
                new DotMatrixSymbol('y', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 0, 0, 1}, {0, 0, 1, 1, 0}, {0, 0, 1, 0, 0}, {1, 1, 0, 0, 0}}),
                new DotMatrixSymbol('z', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('{', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('|', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('}', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('~', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}})
        };

        final int DEFAULT_FONT_RIGHT_MARGIN = 1;        //  1 colonne vide à droite de chaque symbole

        defaultFont = new DotMatrixFont();
        defaultFont.setSymbols(DEFAULT_FONT_SYMBOLS);
        defaultFont.setRightMargin(DEFAULT_FONT_RIGHT_MARGIN);
        return defaultFont;
    }

}
