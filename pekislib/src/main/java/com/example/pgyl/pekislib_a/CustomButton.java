package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public final class CustomButton extends Button {
    //region Variables
    private BUTTON_STATES buttonState;
    private int unpressedColor;
    private int pressedColor;
    private boolean clickDownInButtonZone;
    private Rect buttonZone;
    private Drawable drawable;
    //endregion

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        drawable = getBackground().getConstantState().newDrawable().mutate();
        unpressedColor = UNDEFINED;
        pressedColor = UNDEFINED;
        buttonState = BUTTON_STATES.UNPRESSED;
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onButtonTouch(v, event);
            }
        });
    }

    public void setUnpressedColor(String color) {
        if (color != null) {
            unpressedColor = Color.parseColor(COLOR_PREFIX + color);
        } else {
            unpressedColor = UNDEFINED;
        }
    }

    public void setPressedColor(String color) {
        if (color != null) {
            pressedColor = Color.parseColor(COLOR_PREFIX + color);
        } else {
            pressedColor = UNDEFINED;
        }
    }

    public void updateColor() {
        int color;

        if (buttonState.equals(BUTTON_STATES.UNPRESSED)) {
            color = unpressedColor;
        } else {
            color = pressedColor;
        }
        if (color != UNDEFINED) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        } else {
            drawable.clearColorFilter();
        }
        setBackground(drawable);
        invalidate();
    }

    private boolean onButtonTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clickDownInButtonZone = true;
            buttonState = BUTTON_STATES.PRESSED;
            updateColor();
        }
        if ((action == MotionEvent.ACTION_MOVE) || (action == MotionEvent.ACTION_UP)) {
            if (clickDownInButtonZone) {
                if (buttonZone == null) {
                    buttonZone = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if (buttonZone.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                    if (action == MotionEvent.ACTION_UP) {
                        buttonState = BUTTON_STATES.UNPRESSED;
                        updateColor();
                        performClick();
                    }
                } else {
                    clickDownInButtonZone = false;
                    buttonState = BUTTON_STATES.UNPRESSED;
                    updateColor();
                }
            }
        }
        return true;
    }

}