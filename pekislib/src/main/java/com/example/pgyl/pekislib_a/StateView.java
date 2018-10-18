package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;

public final class StateView extends View {
    //region Constantes
    private final String COLOR_ON_DEFAULT = "FF0000";
    private final String COLOR_OFF_DEFAULT = "808080";
    //endregion
    //region Variables
    private int colorOn;
    private int colorOff;
    private int color;
    private boolean state;
    private RectF canvasRect;
    private Paint paint;
    private int cornerRadius;
    //endregion

    public StateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        state = false;
        colorOn = Color.parseColor(COLOR_PREFIX + COLOR_ON_DEFAULT);
        colorOff = Color.parseColor(COLOR_PREFIX + COLOR_OFF_DEFAULT);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int CORNER_RADIUS = 70;     //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi

        canvasRect = new RectF(0, 0, w, h);
        cornerRadius = (Math.min(w, h) * CORNER_RADIUS) / 200;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        paint = null;
    }

    public void setStateOn() {
        state = true;
        invalidate();
    }

    public void setStateOff() {
        state = false;
        invalidate();
    }

    public void setColorON(String color) {
        colorOn = Color.parseColor(COLOR_PREFIX + color);
    }

    public void setColorOFF(String color) {
        colorOff = Color.parseColor(COLOR_PREFIX + color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (state) {
            color = colorOn;
        } else {
            color = colorOff;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        paint.setColor(color);
        canvas.drawRoundRect(canvasRect, cornerRadius, cornerRadius, paint);
    }

}