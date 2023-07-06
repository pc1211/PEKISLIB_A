package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import static com.example.pgyl.pekislib_a.ButtonColorBox.COLOR_TYPES;
import static com.example.pgyl.pekislib_a.ColorUtils.ColorDef;
import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;

public final class CustomButton extends Button {
    //region Constantes
    final String PRESSED_BACK_COLOR_DEFAULT = "FF9A22";
    //endregion
    //region Variables
    private long minClickTimeInterval;
    private long lastClickUpTime;
    private BUTTON_STATES buttonState;
    private ButtonColorBox colorBox;
    private boolean clickDownInButtonZone;
    private Rect buttonZone;
    //endregion

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;    //  Interval de temps (ms) minimum imposé entre 2 click

        colorBox = new ButtonColorBox();
        colorBox.setColor(COLOR_TYPES.UNPRESSED_BACK_COLOR, null);   //  Null => Couleur Android par défault
        colorBox.setColor(COLOR_TYPES.PRESSED_BACK_COLOR, PRESSED_BACK_COLOR_DEFAULT);
        buttonState = BUTTON_STATES.UNPRESSED;
        minClickTimeInterval = MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE;
        lastClickUpTime = 0;
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onButtonTouch(v, event);
            }
        });
    }

    public ButtonColorBox getColorBox() {   //   On peut alors modifier les couleurs (colorBox.setColor...), puis faire updateDisplayBackColors() pour mettre à jour l'affichage
        return colorBox;
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    public void updateDisplayBackColors() {
        ColorDef backColor = (buttonState.equals(BUTTON_STATES.PRESSED) ? colorBox.getColor(COLOR_TYPES.PRESSED_BACK_COLOR) : colorBox.getColor(COLOR_TYPES.UNPRESSED_BACK_COLOR));
        if (backColor != null) {
            getBackground().setColorFilter(backColor.RGBCode, PorterDuff.Mode.MULTIPLY);
        } else {   //  Null => Couleur par défaut
            if (buttonState.equals(BUTTON_STATES.PRESSED)) {
                colorBox.setColor(COLOR_TYPES.PRESSED_BACK_COLOR, PRESSED_BACK_COLOR_DEFAULT);
                backColor = colorBox.getColor(COLOR_TYPES.PRESSED_BACK_COLOR);
                getBackground().setColorFilter(backColor.RGBCode, PorterDuff.Mode.MULTIPLY);
            } else {
                getBackground().clearColorFilter();
            }
        }
        invalidate();
    }

    private boolean onButtonTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clickDownInButtonZone = true;
            buttonState = BUTTON_STATES.PRESSED;
            v.getParent().requestDisallowInterceptTouchEvent(true);   //  Une listView éventuelle (qui contient des items avec ce contrôle et voudrait scroller) ne pourra voler l'événement ACTION_MOVE de ce contrôle
            updateDisplayBackColors();
            return true;
        }
        if ((action == MotionEvent.ACTION_MOVE) || (action == MotionEvent.ACTION_UP)) {
            if (clickDownInButtonZone) {
                if (buttonZone == null) {
                    buttonZone = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if (buttonZone.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                    if (action == MotionEvent.ACTION_UP) {
                        long nowm = System.currentTimeMillis();
                        buttonState = BUTTON_STATES.UNPRESSED;
                        updateDisplayBackColors();
                        if ((nowm - lastClickUpTime) >= minClickTimeInterval) {   //  OK pour traiter le click
                            lastClickUpTime = nowm;
                            performClick();
                        } else {   //  Attendre pour pouvoir traiter un autre click
                            clickDownInButtonZone = false;
                        }
                    }
                } else {
                    clickDownInButtonZone = false;
                    buttonState = BUTTON_STATES.UNPRESSED;
                    updateDisplayBackColors();
                }
            }
            return (action == MotionEvent.ACTION_MOVE);
        }
        return false;
    }

}