package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LEDCustomImageButton extends LinearLayout {
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    private CustomImageButton customImageButton;
    private LEDView ledView;

    public LEDCustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        final String BUTTON_XML_NAME = "BUTTON";
        final String STATE_XML_NAME = "STATE";

        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.statecustomimagebutton, this, true);
        Class rid = R.id.class;
        try {
            customImageButton = findViewById(rid.getField(BUTTON_XML_NAME).getInt(rid));
            ledView = findViewById(rid.getField(STATE_XML_NAME).getInt(rid));
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        customImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick();
            }
        });
    }

    public void setLight(boolean lightOn) {
        if (lightOn) {
            setLightOn();
        } else {
            setLightOff();
        }
    }

    public boolean getLight() {
        return (ledView.getState().equals(LEDView.STATES.ON) ? true : false);
    }

    public void setLightOn() {
        ledView.setState(LEDView.STATES.ON);
    }

    public void setLightOff() {
        ledView.setState(LEDView.STATES.OFF);
    }

    public void setLEDColorOn(String onColor) {
        ledView.setLEDColor(LEDView.STATES.ON, onColor);
    }

    public void setLEDColorOff(String offColor) {
        ledView.setLEDColor(LEDView.STATES.OFF, offColor);
    }

    public String getLEDColorOn() {
        return (String.format("%06X", ledView.getLEDColor(LEDView.STATES.ON)));
    }

    public String getLEDColorOff() {
        return (String.format("%06X", ledView.getLEDColor(LEDView.STATES.OFF)));
    }

    public void setImageResource(int resId) {
        customImageButton.setImageResource(resId);
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        customImageButton.setMinClickTimeInterval(minClickTimeInterval);
    }

    private void onButtonClick() {
        if (mOnCustomClickListener != null) {
            mOnCustomClickListener.onCustomClick();   //  Signaler le click du bouton
        }
    }

}
