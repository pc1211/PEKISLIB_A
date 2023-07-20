package com.example.pgyl.pekislib_a;

import android.graphics.Color;

import com.example.pgyl.pekislib_a.ColorUtils.ColorDef;

import java.util.ArrayList;

import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;

public class ColorBox {   //  Pour les ImageButtonView ou DotMatrixDisplayView

    private ArrayList<ColorDef> colors;

    public ColorBox() {
        init();
    }

    private void init() {
        colors = new ArrayList<ColorDef>();
    }

    public void close() {
        if (colors != null) {
            colors.clear();
            colors = null;
        }
    }

    public void setColor(int colorIndex, String color) {   //  color: RRGGBB (Hex), Null interdit
        if (colors == null) {
            init();   //  Maintenant colors est garanti non Null
        }
        int size = colors.size();
        if (colorIndex >= size) {   //  Augmenter l'arrayList jusque colorIndex inclus
            for (int i = 1; i <= (colorIndex - size + 1); i = i + 1) {
                colors.add(null);
            }
        }   //  Maintenant colors.get(colorIndex) est garanti existant (mais éventuellement Null)
        if (colors.get(colorIndex) == null) {
            colors.set(colorIndex, new ColorDef());   //  Maintenant colors.get(colorIndex) est garanti non Null
        }
        ColorDef colorDef = colors.get(colorIndex);
        colorDef.RGBString = color;
        colorDef.RGBInt = Color.parseColor(COLOR_PREFIX + color);
    }

    public ColorDef getColor(int colorIndex) {   //  Colordef est retourné, donc avec .RGBString et .RGBInt
        ColorDef colorDef = null;
        if (colors == null) {
            init();
        }
        if (colorIndex <= (colors.size() - 1)) {
            colorDef = colors.get(colorIndex);
        }
        return colorDef;
    }
}