package com.example.pgyl.pekislib_a;

import android.graphics.Point;

import static com.example.pgyl.pekislib_a.PointRectUtils.RectDimensions;

public class DotMatrixSymbol {
    private Character ch;             //  Caractère représenté
    private int[][] data;             //  Données de forme du symbole
    private boolean overwrite;        //  True si surcharge le symbole précédent
    private Point posOffset;          //  Si surcharge, Offset de repositionnement éventuel avant affichage, par rapport à la position du symbole précédent
    private RectDimensions dimensions; // Dimensions du symbole

    public DotMatrixSymbol(Character ch, int[][] data) {
        this.ch = ch;
        this.data = data;
        init();
    }

    private void init() {
        posOffset = new Point(0, 0);
        dimensions = new RectDimensions(0, 0);
        dimensions.height = data.length;   //  Hauteur du symbole
        for (int i = 0; i <= (dimensions.height - 1); i = i + 1) {
            if (data[i].length > dimensions.width) {      //   Chercher la largeur max. du symbole
                dimensions.width = data[i].length;
            }
        }
    }

    public Character getCh() {   //  Pas de set
        return ch;
    }

    public int[][] getData() {   //  Pour le set, cf constructor()
        return data;
    }

    public RectDimensions getDimensions() {    //  Pour le set, cf init()
        return dimensions;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public Point getPosOffset() {
        return posOffset;
    }

    public void setPosOffset(int x, int y) {
        posOffset.x = x;
        posOffset.y = y;
    }

}
