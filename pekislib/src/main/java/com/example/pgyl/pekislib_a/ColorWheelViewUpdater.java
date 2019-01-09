package com.example.pgyl.pekislib_a;

import android.os.Handler;

import static com.example.pgyl.pekislib_a.TimeDateUtils.TIMEUNITS;

public class ColorWheelViewUpdater {
    //region Constantes
    private final double WHEEL_ROTATION_ANGULAR_SPEED = 8;   //  Radians par seconde
    //endregion
    //region Variables
    private long updateInterval;
    private float rotationAngle;
    private long duration;
    private float nowAngle;
    private float prevAngle;
    private long initm;
    private boolean inAutomatic;
    private ColorWheelView colorWheelView;
    private Handler handlerTime;
    private Runnable runnableTime;
    //endregion

    public ColorWheelViewUpdater(ColorWheelView colorWheelView) {
        this.colorWheelView = colorWheelView;
        init();
    }

    private void init() {
        setupRunnableTime();
    }

    public void close() {
        runnableTime = null;
        handlerTime = null;
        colorWheelView = null;
    }

    public void rotateAnimation(float rotationAngle, long nowm) {
        final int FPS = 50;

        this.rotationAngle = rotationAngle;
        duration = (long) (Math.abs(rotationAngle) / (WHEEL_ROTATION_ANGULAR_SPEED / TIMEUNITS.SEC.MS()));
        updateInterval = TIMEUNITS.SEC.MS() / FPS;
        initm = nowm;
        prevAngle = 0;
        inAutomatic = false;
        colorWheelView.unpinMarker();
        handlerTime.postDelayed(runnableTime, updateInterval);
    }

    private void automatic() {
        long nowm = System.currentTimeMillis();
        if (nowm < (initm + duration)) {
            handlerTime.postDelayed(runnableTime, updateInterval);
            if ((!inAutomatic) && (!colorWheelView.isDrawing())) {
                inAutomatic = true;
                nowAngle = ((nowm - initm) / (float) duration) * rotationAngle;
                colorWheelView.rotate(nowAngle - prevAngle);
                prevAngle = nowAngle;
                inAutomatic = false;
            }
        } else {   //  La fête est finie
            handlerTime.removeCallbacks(runnableTime);
            colorWheelView.resetRotationAngle();        //  Recalibration (les animations se terminent toujours au milieu d'une zone de couleur)
            colorWheelView.pinMarker();
            colorWheelView.invalidate();
        }
    }

    private void setupRunnableTime() {
        handlerTime = new Handler();
        runnableTime = new Runnable() {
            @Override
            public void run() {
                automatic();
            }
        };
    }
}
