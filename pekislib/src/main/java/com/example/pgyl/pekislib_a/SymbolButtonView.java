package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.larvalabs.svgandroid.SVGParser;

import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.PointRectUtils.CENTER_X_Y;
import static com.example.pgyl.pekislib_a.PointRectUtils.FULL_SIZE_COEFF;
import static com.example.pgyl.pekislib_a.PointRectUtils.SQUARE_ASPECT_RATIO;
import static com.example.pgyl.pekislib_a.PointRectUtils.getSubRect;

public final class SymbolButtonView extends View {
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setCustomOnClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    //region Constantes
    private final float SIZE_COEFF_DEFAULT = 0.9f;   //  (0..1)
    //endregion
    //region Variables
    private long minClickTimeInterval;
    private long lastClickUpTime;
    private BUTTON_STATES buttonState;
    private int frontColor;
    private int backColor;
    private int extraColor;
    private boolean clickDownInButtonZone;
    private RectF buttonZone;
    private Bitmap viewBitmap;
    private Canvas viewCanvas;
    private Paint backPaint;
    private float backCornerRadius;
    private RectF viewCanvasRect;
    private RectF symbolCellCanvasRect;
    private Bitmap symbolBitmap;
    private Picture symbolPicture;
    private float symbolAspectRatio;
    private RectF symbolRelativePositionCoeffs;
    private float symbolSizeCoeff;
    //endregion

    public SymbolButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click

        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        symbolPicture = null;  //  En attente d'appel de setSVGImageResource
        buttonZone = new RectF();
        buttonState = BUTTON_STATES.UNPRESSED;
        minClickTimeInterval = MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE;
        lastClickUpTime = 0;
        symbolSizeCoeff = SIZE_COEFF_DEFAULT;
        symbolRelativePositionCoeffs = CENTER_X_Y;
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onButtonTouch(v, event);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        symbolPicture = null;
        symbolBitmap = null;
        viewBitmap = null;
        viewCanvas = null;
        backPaint = null;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int BACK_CORNER_RADIUS = 35;  //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi

        super.onSizeChanged(w, h, oldw, oldh);

        viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        viewCanvasRect = new RectF(0, 0, w, h);
        symbolCellCanvasRect = getSubRect(viewCanvasRect, CENTER_X_Y, SQUARE_ASPECT_RATIO, FULL_SIZE_COEFF);
        symbolBitmap = createSymbolBitmap(symbolPicture);
        buttonZone.set(getLeft() + symbolCellCanvasRect.left, getTop() + symbolCellCanvasRect.top, getLeft() + symbolCellCanvasRect.right, getTop() + symbolCellCanvasRect.bottom);
        backCornerRadius = (Math.min(w, h) * BACK_CORNER_RADIUS) / 200;    //  Rayon pour coin arrondi (% appliqué à la moitié de la largeur ou hauteur)
    }

    public void setSVGImageResource(int resId) {
        symbolPicture = SVGParser.getSVGFromResource(getResources(), resId).getPicture();
        symbolAspectRatio = (float) symbolPicture.getHeight() / (float) symbolPicture.getWidth();
    }

    public void setSymbolRelativePositionCoeffs(RectF symbolRelativePositionCoeffs) {
        this.symbolRelativePositionCoeffs = symbolRelativePositionCoeffs;
    }

    public void setSymbolSizeCoff(float symbolSizeCoeff) {
        this.symbolSizeCoeff = symbolSizeCoeff;
    }

    public void setFrontColor(String frontColor) {
        this.frontColor = Color.parseColor(COLOR_PREFIX + frontColor);
    }

    public void setBackColor(String backColor) {
        this.backColor = Color.parseColor(COLOR_PREFIX + backColor);
    }

    public void setExtraColor(String extraColor) {
        this.extraColor = Color.parseColor(COLOR_PREFIX + extraColor);
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    public void updateDisplay() {
        invalidate();
    }

    public boolean onButtonTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clickDownInButtonZone = true;
            buttonState = BUTTON_STATES.PRESSED;
            v.getParent().requestDisallowInterceptTouchEvent(true);   //  Une listView éventuelle (qui contient des items avec ce contrôle et voudrait scroller) ne pourra voler l'événement ACTION_MOVE de ce contrôle
            invalidate();
            return true;
        }
        if ((action == MotionEvent.ACTION_MOVE) || (action == MotionEvent.ACTION_UP)) {
            if (clickDownInButtonZone) {
                if (buttonZone.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                    if (action == MotionEvent.ACTION_UP) {
                        long nowm = System.currentTimeMillis();
                        buttonState = BUTTON_STATES.UNPRESSED;
                        invalidate();
                        if ((nowm - lastClickUpTime) >= minClickTimeInterval) {   //  OK pour traiter le click
                            lastClickUpTime = nowm;
                            if (mOnCustomClickListener != null) {
                                mOnCustomClickListener.onCustomClick();
                            }
                        } else {   //  Attendre pour pouvoir traiter un autre click
                            clickDownInButtonZone = false;
                        }
                    }
                } else {
                    clickDownInButtonZone = false;
                    buttonState = BUTTON_STATES.UNPRESSED;
                    invalidate();
                }
            }
            return (action == MotionEvent.ACTION_MOVE);
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int stateFrontColor = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? backColor : frontColor);
        int stateBackColor = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? extraColor : backColor);
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        viewCanvas.drawBitmap(symbolBitmap, 0, 0, null);
        viewCanvas.drawColor(stateFrontColor, PorterDuff.Mode.SRC_IN);
        backPaint.setColor(stateBackColor);
        viewCanvas.drawRoundRect(symbolCellCanvasRect, backCornerRadius, backCornerRadius, backPaint);
        canvas.drawBitmap(viewBitmap, 0, 0, null);
    }

    private Bitmap createSymbolBitmap(Picture picture) {
        Bitmap ret = Bitmap.createBitmap((int) viewCanvasRect.width(), (int) viewCanvasRect.height(), Bitmap.Config.ARGB_8888);
        Canvas viewCanvas = new Canvas(ret);
        viewCanvas.drawPicture(picture, getSubRect(symbolCellCanvasRect, symbolRelativePositionCoeffs, symbolAspectRatio, symbolSizeCoeff));
        return ret;
    }

}