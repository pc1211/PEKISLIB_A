package com.example.pgyl.pekislib_a;

import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

public class DotMatrixFont {
    private Map<Character, DotMatrixSymbol> charMap;
    private int MaxSymbolWidth;
    private int MaxSymbolHeight;
    private int rightMargin;

    public DotMatrixFont() {
        init();
    }

    private void init() {
        final int RIGHT_MARGIN_DEFAULT = 0;

        charMap = new HashMap<Character, DotMatrixSymbol>();
        rightMargin = RIGHT_MARGIN_DEFAULT;
        MaxSymbolWidth = 0;
        MaxSymbolHeight = 0;
    }

    public void close() {
        charMap.clear();
        charMap = null;
    }

    public DotMatrixSymbol getSymbol(Character ch) {
        return charMap.get(ch);
    }

    public int getMaxSymbolWidth() {
        return MaxSymbolWidth;
    }

    public int getMaxSymbolHeight() {
        return MaxSymbolHeight;
    }

    public void setSymbols(DotMatrixSymbol[] symbols) {
        for (int i = 0; i <= (symbols.length - 1); i = i + 1) {
            charMap.put(symbols[i].getCh(), symbols[i]);
            symbols[i].setPosInitialOffset(new Point(0, 0));
            symbols[i].setPosFinalOffset(new Point(symbols[i].getWidth() + rightMargin, 0));
            if (symbols[i].getWidth() > MaxSymbolWidth) {    //  Chercher la largeur max. d'un symbole
                MaxSymbolWidth = symbols[i].getWidth();
            }
            if (symbols[i].getHeight() > MaxSymbolHeight) {    //  Chercher la hauteur max. d'un symbole
                MaxSymbolHeight = symbols[i].getHeight();
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
            int symbolWidth = symbol.getPosInitialOffset().x + symbol.getPosFinalOffset().x;
            textWidth = textWidth + symbolWidth;
        }
        symbol = null;
        return textWidth;
    }

    public int getTextHeight(String text) {   // Hauteur nécessaire pour afficher un texte
        DotMatrixSymbol symbol;

        int textHeight = 0;
        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            symbol = charMap.get(text.charAt(i));
            int symbolHeight = symbol.getHeight() + symbol.getPosInitialOffset().y;
            if (symbolHeight > textHeight) {
                textHeight = symbolHeight;
            }
        }
        symbol = null;
        return textHeight;
    }

}
