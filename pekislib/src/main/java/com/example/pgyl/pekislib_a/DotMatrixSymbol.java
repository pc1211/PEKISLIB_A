package com.example.pgyl.pekislib_a;

import android.graphics.Point;

public class DotMatrixSymbol {
    private Character ch;             //  Caractère représenté
    private int[][] data;             //  Données de forme du symbole
    private Point posInitialOffset;   //  Repositionnement du symbole avant affichage
    private Point posFinalOffset;     //  Repositionnement pour le prochain symbole à afficher
    private int width;                //  Largeur du symbole
    private int height;               //  Hauteur du symbole

    public DotMatrixSymbol(Character ch, int[][] data) {
        this.ch = ch;
        this.data = data;
        init();
    }

    private void init() {
        height = data.length;   //  Hauteur du symbole
        width = 0;
        for (int i = 0; i <= (height - 1); i = i + 1) {
            if (data[i].length > width) {      //   Chercher la largeur max. du symbole
                width = data[i].length;
            }
        }
    }

    public Character getCh() {   //  Pas de set
        return ch;
    }

    public int[][] getData() {   //  Pour le set, cf constructor()
        return data;
    }

    public int getWidth() {    //  Pour le set, cf init()
        return width;
    }

    public int getHeight() {   //  Pour le set, cf init()
        return height;
    }

    public Point getPosInitialOffset() {
        return posInitialOffset;
    }

    public void setPosInitialOffset(Point posInitialOffset) {
        this.posInitialOffset = posInitialOffset;
    }

    public Point getPosFinalOffset() {
        return posFinalOffset;
    }

    public void setPosFinalOffset(Point posFinalOffset) {
        this.posFinalOffset = posFinalOffset;
    }

}
