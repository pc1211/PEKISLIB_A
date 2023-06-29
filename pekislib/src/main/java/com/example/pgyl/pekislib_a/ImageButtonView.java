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
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.larvalabs.svgandroid.SVGParser;

import static com.example.pgyl.pekislib_a.ButtonColorBox.COLOR_TYPES;
import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.MiscUtils.getPictureFromDrawable;
import static com.example.pgyl.pekislib_a.PointRectUtils.ALIGN_WIDTH_HEIGHT;

public final class ImageButtonView extends View {
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setCustomOnClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;
    //region Variables
    private long minClickTimeInterval;
    private int pcBackCornerRadius;
    private int backCornerRadius;
    private long lastClickUpTime;
    private BUTTON_STATES buttonState;
    private ButtonColorBox colorBox;
    private boolean clickDownInButtonZone;
    private RectF buttonZone;
    private Bitmap viewBitmap;
    private Canvas viewCanvas;
    private Paint backPaint;
    private RectF viewCanvasRect;
    private Bitmap symbolBitmap;
    private Picture picture;
    private float symbolAspectRatio;
    private RectF symbolRelativePositionCoeffs;
    private float symbolSizeCoeff;
    private Context context;
    //endregion

    public ImageButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        init();
    }

    public void init() {
        final float SIZE_COEFF_DEFAULT = 0.8f;   //  (0..1)
        final int PC_BACK_CORNER_RADIUS_DEFAULT = 35;    //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click
        final String UNPRESSED_FRONT_COLOR_DEFAULT = "000000";
        final String UNPRESSED_BACK_COLOR_DEFAULT = "A0A0A0";
        final String PRESSED_FRONT_COLOR_DEFAULT = UNPRESSED_FRONT_COLOR_DEFAULT;
        final String PRESSED_BACK_COLOR_DEFAULT = "FF9A22";

        colorBox = new ButtonColorBox();
        colorBox.setColor(COLOR_TYPES.UNPRESSED_FRONT_COLOR, UNPRESSED_FRONT_COLOR_DEFAULT);
        colorBox.setColor(COLOR_TYPES.UNPRESSED_BACK_COLOR, UNPRESSED_BACK_COLOR_DEFAULT);
        colorBox.setColor(COLOR_TYPES.PRESSED_FRONT_COLOR, PRESSED_FRONT_COLOR_DEFAULT);
        colorBox.setColor(COLOR_TYPES.PRESSED_BACK_COLOR, PRESSED_BACK_COLOR_DEFAULT);
        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        buttonZone = new RectF();
        buttonState = BUTTON_STATES.UNPRESSED;
        minClickTimeInterval = MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE;
        pcBackCornerRadius = PC_BACK_CORNER_RADIUS_DEFAULT;
        lastClickUpTime = 0;
        symbolSizeCoeff = SIZE_COEFF_DEFAULT;
        symbolRelativePositionCoeffs = ALIGN_WIDTH_HEIGHT;
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

        symbolBitmap = null;
        viewBitmap = null;
        viewCanvas = null;
        backPaint = null;
        colorBox.close();
        colorBox = null;
        picture = null;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        viewCanvasRect = new RectF(0, 0, w, h);

        symbolBitmap = Bitmap.createBitmap((int) viewCanvasRect.width(), (int) viewCanvasRect.height(), Bitmap.Config.ARGB_8888);
        Canvas symbolViewCanvas = new Canvas(symbolBitmap);
        symbolViewCanvas.drawPicture(picture, PointRectUtils.getMaxSubRect(viewCanvasRect, symbolRelativePositionCoeffs, symbolAspectRatio, symbolSizeCoeff));

        buttonZone.set(getLeft() + viewCanvasRect.left, getTop() + viewCanvasRect.top, getLeft() + viewCanvasRect.right, getTop() + viewCanvasRect.bottom);
        backCornerRadius = (Math.min(w, h) * pcBackCornerRadius) / 200;    //  Rayon pour coin arrondi (% appliqué à la moitié de la largeur ou hauteur)
    }

    public void setSVGImageResource(int resId) {
        picture = SVGParser.getSVGFromResource(getResources(), resId).getPicture();
        symbolAspectRatio = (float) picture.getHeight() / (float) picture.getWidth();
    }

    public void setPNGImageResource(int resId) {
        picture = getPictureFromDrawable((BitmapDrawable) getResources().getDrawable(resId, context.getTheme()));
        symbolAspectRatio = (float) picture.getHeight() / (float) picture.getWidth();
    }

    public void setSymbolRelativePositionCoeffs(RectF symbolRelativePositionCoeffs) {
        this.symbolRelativePositionCoeffs = symbolRelativePositionCoeffs;
    }

    public void setSymbolSizeCoeff(float symbolSizeCoeff) {
        this.symbolSizeCoeff = symbolSizeCoeff;
    }

    public void updateDisplayColors() {
        invalidate();
    }

    public ButtonColorBox getColorBox() {   //   On peut alors modifier les couleurs (colorBox.setColor...), puis faire updateDisplayColors() pour mettre à jour l'affichage
        return colorBox;
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    public void setPcBackCornerRadius(int pcBackCornerRadius) {
        this.pcBackCornerRadius = pcBackCornerRadius;
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

        int frontColor = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? colorBox.getColor(COLOR_TYPES.PRESSED_FRONT_COLOR).intValue : colorBox.getColor(COLOR_TYPES.UNPRESSED_FRONT_COLOR).intValue);
        int backColor = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? colorBox.getColor(COLOR_TYPES.PRESSED_BACK_COLOR).intValue : colorBox.getColor(COLOR_TYPES.UNPRESSED_BACK_COLOR).intValue);
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        viewCanvas.drawBitmap(symbolBitmap, 0, 0, null);
        viewCanvas.drawColor(frontColor, PorterDuff.Mode.SRC_IN);
        backPaint.setColor(backColor);
        viewCanvas.drawRoundRect(viewCanvasRect, backCornerRadius, backCornerRadius, backPaint);
        canvas.drawBitmap(viewBitmap, 0, 0, null);
    }

}