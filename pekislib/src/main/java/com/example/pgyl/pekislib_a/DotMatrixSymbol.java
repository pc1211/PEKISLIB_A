package com.example.pgyl.pekislib_a;

import android.graphics.Point;

import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;

public class DotMatrixSymbol {
    private int code;                 //  Code ASCII quand c'est possible
    private Character ch;             //  Caractère représenté  (Eventuellement "\<nnn>" (octal) (max 255 en décimal) si non imprimable)
    private int[][] data;             //  Données de forme du symbole
    private boolean overwrite;        //  True si surcharge le symbole précédent
    private Point posOffset;          //  Si surcharge, Offset de repositionnement éventuel avant affichage, par rapport à la position du symbole précédent
    private BiDimensions dimensions;  //  Dimensions du symbole

    public DotMatrixSymbol(int code, Character ch, int[][] data) {
        this.code = code;
        this.ch = ch;
        this.data = data;
        init();
    }

    private void init() {
        overwrite = false;
        posOffset = new Point(0, 0);
        dimensions = new BiDimensions(0, 0);
        dimensions.height = data.length;   //  Hauteur du symbole
        for (int i = 0; i <= (dimensions.height - 1); i = i + 1) {
            if (data[i].length > dimensions.width) {      //   Chercher la largeur max. du symbole
                dimensions.width = data[i].length;
            }
        }
    }

    public int getCode() {   //  Pas de set
        return code;
    }

    public Character getChar() {   //  Pas de set
        return ch;
    }

    public int[][] getData() {   //  Pour le set, cf constructor()
        return data;
    }

    public BiDimensions getDimensions() {    //  Pour le set, cf init()
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
