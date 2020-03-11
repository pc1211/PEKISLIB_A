package com.example.pgyl.pekislib_a;

import java.util.HashMap;
import java.util.Map;

import static com.example.pgyl.pekislib_a.PointRectUtils.RectDimensions;

public class DotMatrixFont {
    private Map<Character, DotMatrixSymbol> charMap;
    private RectDimensions symbolDimensions;
    private int rightMargin;

    public DotMatrixFont() {
        init();
    }

    private void init() {
        final int RIGHT_MARGIN_DEFAULT = 0;

        charMap = new HashMap<Character, DotMatrixSymbol>();
        rightMargin = RIGHT_MARGIN_DEFAULT;
    }

    public void close() {
        charMap.clear();
        charMap = null;
    }

    public DotMatrixSymbol getSymbol(Character ch) {
        return charMap.get(ch);
    }

    public void setSymbols(DotMatrixSymbol[] symbols) {
        symbolDimensions = new RectDimensions(0, 0);
        for (int i = 0; i <= (symbols.length - 1); i = i + 1) {
            charMap.put(symbols[i].getCh(), symbols[i]);
            symbols[i].setOverwrite(false);                 //  Symbole régulier par défaut (cad pas de surcharge)
            symbols[i].setPosOffset(0, 0);
            if (symbols[i].getDimensions().width > symbolDimensions.width) {    //  Chercher la largeur max. d'un symbole
                symbolDimensions.width = symbols[i].getDimensions().width;
            }
            if (symbols[i].getDimensions().height > symbolDimensions.height) {    //  Chercher la hauteur max. d'un symbole
                symbolDimensions.height = symbols[i].getDimensions().height;
            }
        }
    }

    public RectDimensions getSymbolDimensions() {
        return symbolDimensions;
    }

    public void setRightMargin(int rightMargin) {   //  Marge droite pour chaque symbole (en nombre de carrés)
        this.rightMargin = rightMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public RectDimensions getTextDimensions(String text) {
        DotMatrixSymbol symbol;
        RectDimensions textDimensions;

        textDimensions = new RectDimensions(0, 0);
        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            symbol = charMap.get(text.charAt(i));
            if (symbol != null) {
                int netSymbolWidth = (!symbol.isOverwrite() ? symbol.getDimensions().width + rightMargin : 0);  //  Les symboles de surcharge ne sont pas comptés
                textDimensions.width = textDimensions.width + netSymbolWidth;
                int netSymbolHeight = (!symbol.isOverwrite() ? symbol.getDimensions().height : 0);
                if (netSymbolHeight > textDimensions.height) {
                    textDimensions.height = netSymbolHeight;
                }
            } else {   //  Caractère inconnu dans cette font
                textDimensions = null;
                break;
            }
        }
        symbol = null;
        return textDimensions;
    }

}
