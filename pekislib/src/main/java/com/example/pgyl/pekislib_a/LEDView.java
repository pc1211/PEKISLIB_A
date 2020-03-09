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

import java.util.EnumMap;

import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.Constants.COLOR_MASK_AND;

public final class LEDView extends View {
    //region Constantes
    public enum STATES {
        ON("FF0000"), OFF("808080");

        private String ledColorDefaultValue;

        STATES(String ledColorDefaultValue) {
            this.ledColorDefaultValue = ledColorDefaultValue;
        }

        public String DEFAULT_LED_COLOR() {
            return ledColorDefaultValue;
        }
    }

    //endregion
    //region Variables
    private EnumMap<STATES, Integer> ledColorsMap;
    private STATES state;
    private RectF canvasRect;
    private Paint paint;
    private int cornerRadius;
    //endregion

    public LEDView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        state = STATES.OFF;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        setupLEDColorsMap();
        invalidate();
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
        ledColorsMap.clear();
        ledColorsMap = null;
    }

    public void setState(STATES state) {
        this.state = state;
        invalidate();
    }

    public STATES getState() {
        return state;
    }

    public void setLEDColor(STATES state, String color) {
        ledColorsMap.put(state, Color.parseColor(COLOR_PREFIX + color));
        invalidate();
    }

    public String getLEDColor(STATES state) {
        return (String.format("%06X", ledColorsMap.get(state) & COLOR_MASK_AND));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        paint.setColor(ledColorsMap.get(state));
        canvas.drawRoundRect(canvasRect, cornerRadius, cornerRadius, paint);
    }

    private void setupLEDColorsMap() {
        ledColorsMap = new EnumMap<STATES, Integer>(STATES.class);
        for (STATES stateValue : STATES.values()) {
            ledColorsMap.put(stateValue, Color.parseColor(COLOR_PREFIX + stateValue.DEFAULT_LED_COLOR()));
        }
    }

}