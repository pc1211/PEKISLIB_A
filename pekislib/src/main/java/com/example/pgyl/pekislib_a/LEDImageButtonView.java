package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LEDImageButtonView extends LinearLayout {
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    private ImageButtonView button;
    private LEDView ledView;

    public LEDImageButtonView(Context context, AttributeSet attrs) {
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
            button = findViewById(rid.getField(BUTTON_XML_NAME).getInt(rid));
            ledView = findViewById(rid.getField(LED_XML_NAME).getInt(rid));
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        button.setCustomOnClickListener(new ImageButtonView.onCustomClickListener() {
            @Override
            public void onCustomClick() {
                onButtonClick();
            }
        });
    }

    public ImageButtonView getButton() {
        return button;
    }

    public LEDView getLedView() {
        return ledView;
    }

    public void setButtonMinClickTimeInterval(long minClickTimeInterval) {
        button.setMinClickTimeInterval(minClickTimeInterval);
    }

    private void onButtonClick() {
        if (mOnCustomClickListener != null) {
            mOnCustomClickListener.onCustomClick();   //  Signaler le click du bouton
        }
    }

}
