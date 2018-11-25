package com.example.pgyl.pekislib_a;

import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

public class DotMatrixFont {

    public static class Symbol {
        public int[][] data;             //  Données de forme du symbole
        public Point posInitialOffset;   //  Repositionnement du symbole avant affichage
        public Point posFinalOffset;     //  Repositionnement pour le prochain symbole à afficher
    }

    private Map<Character, Symbol> charMap;
    private int width;
    private int height;
    private int rightMargin;

    public DotMatrixFont() {
        init();
    }

    private void init() {
        final int SYMBOL_RIGHT_MARGIN_DEFAULT = 0;

        charMap = new HashMap<Character, Symbol>();
        rightMargin = SYMBOL_RIGHT_MARGIN_DEFAULT;
    }

    public void close() {
        charMap.clear();
        charMap = null;
    }

    public Map<Character, Symbol> getCharMap() {
        return charMap;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void addSymbol(Character ch, int[][] data) {
        Symbol symbol = new Symbol();
        symbol.data = data;
        symbol.posInitialOffset = new Point(0, 0);    //  Par défaut, pas de repositionnement avant affichage du symbole
        symbol.posFinalOffset = new Point(data[0].length + rightMargin, 0);   //  Par défault, après affichage du symbole, se déplacer d'une position vers la droite sans marge droite
        charMap.put(ch, symbol);
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {   //  Marge droite pour chaque symbole (en nombre de carrés)
        Symbol symbol;

        for (Map.Entry<Character, Symbol> entry : charMap.entrySet()) {
            symbol = entry.getValue();
            symbol.posFinalOffset.x = symbol.posFinalOffset.x + rightMargin - this.rightMargin;  //  S'adapter à la nouvelle marge droite
            entry.setValue(symbol);
        }
        symbol = null;
        this.rightMargin = rightMargin;
    }

}
