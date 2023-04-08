package com.example.pgyl.pekislib_a;

import java.util.HashMap;
import java.util.Map;

import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;

public class DotMatrixFont {
    private Map<Integer, DotMatrixSymbol> codeMap;
    private Map<Character, DotMatrixSymbol> charMap;
    private BiDimensions dimensions;   //  Dimensions de la fonte; Egales aux dimensions d'un symbole si tous les symboles ont les mêmes dimensions
    private int rightMargin;

    public DotMatrixFont() {
        init();
    }

    private void init() {
        final int RIGHT_MARGIN_DEFAULT = 0;

        codeMap = new HashMap<Integer, DotMatrixSymbol>();
        charMap = new HashMap<Character, DotMatrixSymbol>();
        rightMargin = RIGHT_MARGIN_DEFAULT;
    }

    public void close() {
        codeMap.clear();
        codeMap = null;
        charMap.clear();
        charMap = null;
    }

    public DotMatrixSymbol getSymbolByCode(Integer code) {
        return codeMap.get(code);
    }

    public DotMatrixSymbol getSymbolByChar(char ch) {
        return charMap.get(ch);
    }

    public void setSymbols(DotMatrixSymbol[] symbols) {
        dimensions = new BiDimensions(0, 0);
        for (int i = 0; i <= (symbols.length - 1); i = i + 1) {
            codeMap.put(symbols[i].getCode(), symbols[i]);
            charMap.put(symbols[i].getChar(), symbols[i]);
            symbols[i].setOverwrite(false);                 //  Symbole régulier par défaut (cad pas de surcharge)
            symbols[i].setPosOffset(0, 0);
            if (symbols[i].getDimensions().width > dimensions.width) {    //  Chercher la largeur max. d'un symbole
                dimensions.width = symbols[i].getDimensions().width;
            }
            if (symbols[i].getDimensions().height > dimensions.height) {    //  Chercher la hauteur max. d'un symbole
                dimensions.height = symbols[i].getDimensions().height;
            }
        }
    }

    public BiDimensions getDimensions() {
        return dimensions;
    }

    public void setRightMargin(int rightMargin) {   //  Marge droite pour chaque symbole (en nombre de carrés)
        this.rightMargin = rightMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public BiDimensions getTextDimensions(String text) {
        DotMatrixSymbol symbol;
        BiDimensions textDimensions;

        textDimensions = new BiDimensions(0, 0);
        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            symbol = codeMap.get((int) text.charAt(i));   //  Conversion ASCII
            if (symbol != null) {
                if (!symbol.isOverwrite()) {   //  Les symboles de surcharge ne sont pas comptés
                    textDimensions.width = textDimensions.width + symbol.getDimensions().width + rightMargin;
                    if (symbol.getDimensions().height > textDimensions.height) {
                        textDimensions.height = symbol.getDimensions().height;
                    }
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
