package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import static com.example.pgyl.pekislib_a.ColorUtils.ButtonColorBox;
import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;

public final class CustomImageButton extends ImageButton {
    //region Variables
    private long minClickTimeInterval;
    private long lastClickUpTime;
    private BUTTON_STATES buttonState;
    private int unpressedFrontColor;
    private int unpressedBackColor;
    private int pressedFrontColor;
    private int pressedBackColor;
    private boolean clickDownInButtonZone;
    private Rect buttonZone;
    private Drawable backgroundDrawable;
    //endregion

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click

        backgroundDrawable = getBackground().getConstantState().newDrawable().mutate();
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

    public void setColors(ButtonColorBox colorBox) {
        if (colorBox != null) {
            unpressedFrontColor = (colorBox.unpressedFrontColor != null) ? Color.parseColor(COLOR_PREFIX + colorBox.unpressedFrontColor) : Integer.MAX_VALUE;   //  7F FF FF FF
            unpressedBackColor = (colorBox.unpressedBackColor != null) ? Color.parseColor(COLOR_PREFIX + colorBox.unpressedBackColor) : Integer.MAX_VALUE;
            pressedFrontColor = (colorBox.pressedFrontColor != null) ? Color.parseColor(COLOR_PREFIX + colorBox.pressedFrontColor) : Integer.MAX_VALUE;
            pressedBackColor = (colorBox.pressedBackColor != null) ? Color.parseColor(COLOR_PREFIX + colorBox.pressedBackColor) : Integer.MAX_VALUE;
            updateDisplayColors();
        }
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    private void updateDisplayColors() {
        int frontColor = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? pressedFrontColor : unpressedFrontColor);
        if (frontColor != Integer.MAX_VALUE) {
            setColorFilter(frontColor, PorterDuff.Mode.SRC_ATOP);
        } else {
            clearColorFilter();
        }

        int backColor = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? pressedBackColor : unpressedBackColor);
        if (backColor != Integer.MAX_VALUE) {
            backgroundDrawable.setColorFilter(backColor, PorterDuff.Mode.SRC_IN);
        } else {
            backgroundDrawable.clearColorFilter();
        }
        setBackground(backgroundDrawable);
        invalidate();
    }

    private boolean onButtonTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clickDownInButtonZone = true;
            buttonState = BUTTON_STATES.PRESSED;
            v.getParent().requestDisallowInterceptTouchEvent(true);   //  Une listView éventuelle (qui contient des items avec ce contrôle et voudrait scroller) ne pourra voler l'événement ACTION_MOVE de ce contrôle
            updateDisplayColors();
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
                        updateDisplayColors();
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
                    updateDisplayColors();
                }
            }
            return (action == MotionEvent.ACTION_MOVE);
        }
        return false;
    }

}