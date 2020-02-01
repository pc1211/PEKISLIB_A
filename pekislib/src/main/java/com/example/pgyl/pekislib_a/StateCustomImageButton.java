package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StateCustomImageButton extends LinearLayout {
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    private CustomImageButton customImageButton;
    private StateView stateView;

    public StateCustomImageButton(Context context, AttributeSet attrs) {
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
            stateView = findViewById(rid.getField(STATE_XML_NAME).getInt(rid));
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

    // Pour le CustomImageButton
    public void setImageResource(int resId) {    //  Faites passer
        customImageButton.setImageResource(resId);
    }

    public void setUnpressedColor(String color) {    //  Faites passer
        customImageButton.setUnpressedColor(color);
    }

    public void setPressedColor(String color) {    //  Faites passer
        customImageButton.setPressedColor(color);
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {    //  Faites passer
        customImageButton.setMinClickTimeInterval(minClickTimeInterval);
    }

    public void updateColor() {    //  Faites passer
        customImageButton.updateColor();
    }

    private void onButtonClick() {
        if (mOnCustomClickListener != null) {
            mOnCustomClickListener.onCustomClick();   //  Signaler le click du bouton
        }
    }

    // Pour la StateView
    public void setState(StateView.STATES state) {    //  Faites passer
        stateView.setState(state);
    }

    public void setStateColor(StateView.STATES state, String color) {    //  Faites passer
        stateView.setStateColor(state, color);
    }

    public void updateStateColor() {
        stateView.invalidate();
    }

}
