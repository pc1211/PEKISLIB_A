package com.example.pgyl.pekislib_a;

import android.graphics.Point;

public class DotMatrixSymbol {
    private int[][] data;             //  Données de forme du symbole
    private Point posInitialOffset;   //  Repositionnement du symbole avant affichage
    private Point posFinalOffset;     //  Repositionnement pour le prochain symbole à afficher
    private int width;                //  Largeur du symbole
    private int height;               //  Hauteur du symbole

    public DotMatrixSymbol() {
        init();
    }

    private void init() {
        width = 0;
        height = 0;
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

    public void setData(int[][] data) {
        height = data.length;
        for (int i = 0; i <= (height - 1); i = i + 1) {
            if (data[i].length > width) {      //   Chercher la largeur max. du symbole
                width = data[i].length;
            }
        }
        this.data = data;
        posInitialOffset = new Point(0, 0);    //  Par défaut, pas de repositionnement avant affichage du symbole
        posFinalOffset = new Point(width, 0);     //  Par défault, après affichage du symbole, se déplacer d'une position vers la droite sans marge droite
    }

    public int[][] getData() {
        return data;
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
