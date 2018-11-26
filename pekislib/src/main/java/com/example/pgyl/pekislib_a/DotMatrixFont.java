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
        final int SYMBOL_RIGHT_MARGIN_DEFAULT = 0;

        charMap = new HashMap<Character, DotMatrixSymbol>();
        rightMargin = SYMBOL_RIGHT_MARGIN_DEFAULT;
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
    }    //  Pas de set car dépend des symboles

    public int getHeight() {
        return height;
    }    //  Pas de set car dépend des symboles

    public void addSymbol(Character ch, int[][] data) {
        DotMatrixSymbol symbol = new DotMatrixSymbol();
        symbol.setData(data);
        charMap.put(ch, symbol);
        if (symbol.getWidth() > width) {   //  Chercher la largeur max. des symboles
            width = symbol.getWidth();
        }
        if (symbol.getHeight() > height) {   //  Chercher la hauteur max. des symboles
            height = symbol.getHeight();
        }
        symbol = null;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {   //  Marge droite pour chaque symbole (en nombre de carrés)
        DotMatrixSymbol symbol;
        Point posFinalOffset;

        for (Map.Entry<Character, DotMatrixSymbol> entry : charMap.entrySet()) {
            symbol = entry.getValue();
            posFinalOffset = symbol.getPosFinalOffset();
            posFinalOffset.x = posFinalOffset.x + rightMargin - this.rightMargin;  //  S'adapter à la nouvelle marge droite
            entry.setValue(symbol);
        }
        symbol = null;
        this.rightMargin = rightMargin;
    }

}
