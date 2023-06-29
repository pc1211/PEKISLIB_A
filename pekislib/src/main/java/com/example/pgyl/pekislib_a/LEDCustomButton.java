package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LEDCustomButton extends LinearLayout {
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    private CustomButton customButton;
    private LEDView ledView;

    public LEDCustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        final String BUTTON_XML_NAME = "BUTTON";
        final String LED_XML_NAME = "LED";

        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.ledcustombutton, this, true);
        Class rid = R.id.class;
        try {
            customButton = findViewById(rid.getField(BUTTON_XML_NAME).getInt(rid));
            ledView = findViewById(rid.getField(LED_XML_NAME).getInt(rid));
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick();
            }
        });
    }

    public boolean getState() {
        return ledView.getState().equals(LEDView.STATES.ON);
    }

    public void setStateOn() {
        ledView.setState(LEDView.STATES.ON);
    }

    public void setStateOff() {
        ledView.setState(LEDView.STATES.OFF);
    }

    public void setLEDOnColor(String onColor) {    //  RRGGBB
        ledView.setLEDColor(LEDView.STATES.ON, onColor);
    }

    public void setLEDOffColor(String offColor) {    //  RRGGBB
        ledView.setLEDColor(LEDView.STATES.OFF, offColor);
    }

    public void updateDisplayButtonBackColors() {
        customButton.updateDisplayBackColors();
    }

    public ButtonColorBox getButtonColorBox() {
        return customButton.getColorBox();
    }

    public void setButtonMinClickTimeInterval(long minClickTimeInterval) {
        customButton.setMinClickTimeInterval(minClickTimeInterval);
    }

    private void onButtonClick() {
        if (mOnCustomClickListener != null) {
            mOnCustomClickListener.onCustomClick();   //  Signaler le click du bouton
        }
    }

}
