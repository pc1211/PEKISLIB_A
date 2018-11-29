package com.example.pgyl.pekislib_a;

import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

public class DotMatrixFont {
    private Map<Character, DotMatrixSymbol> charMap;
    private int width;
    private int height;
    private int rightMargin;

    public DotMatrixFont() {
        init();
    }

    private void init() {
        final int RIGHT_MARGIN_DEFAULT = 0;

        charMap = new HashMap<Character, DotMatrixSymbol>();
        rightMargin = RIGHT_MARGIN_DEFAULT;
        width = 0;
        height = 0;
    }

    public void close() {
        charMap.clear();
        charMap = null;
    }

    public Map<Character, DotMatrixSymbol> getCharMap() {
        return charMap;
    }

    public int getWidth() {
        return width;
    }    //  Pour le set, cf setSymbols

    public int getHeight() {
        return height;
    }    //  Pour le set, cf setSymbols

    public void setSymbols(DotMatrixSymbol[] symbols) {
        for (int i = 0; i <= (symbols.length - 1); i = i + 1) {
            charMap.put(symbols[i].getCh(), symbols[i]);
            symbols[i].setPosInitialOffset(new Point(0, 0));
            symbols[i].setPosFinalOffset(new Point(symbols[i].getWidth() + rightMargin, 0));
            if (symbols[i].getWidth() > width) {    //  Chercher la largeur max. de la fonte
                width = symbols[i].getWidth();
            }
            if (symbols[i].getHeight() > height) {    //  Chercher la hauteur max. de la fonte
                height = symbols[i].getHeight();
            }
        }
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {   //  Marge droite pour chaque symbole (en nombre de carrés)
        DotMatrixSymbol symbol;

        for (Map.Entry<Character, DotMatrixSymbol> entry : charMap.entrySet()) {
            symbol = entry.getValue();
            symbol.getPosFinalOffset().x = symbol.getWidth() + rightMargin;  //  Adapter chaque symbole à la nouvelle marge droite
        }
        symbol = null;
        this.rightMargin = rightMargin;
    }

    public int getTextWidth(String text) {   // Largeur nécessaire pour afficher un texte (symboles avec marge droite comprise)
        DotMatrixSymbol symbol;

        int textWidth = 0;
        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            symbol = charMap.get(text.charAt(i));
            textWidth = textWidth + symbol.getPosInitialOffset().x + symbol.getPosFinalOffset().x;
        }
        symbol = null;
        return textWidth;
    }

}
