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
    private BUTTON_STATES buttonState;
    private String[] colors;
    private int frontColorIndex;
    private int backColorIndex;
    private int alternateColorIndex;
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
        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        symbolPicture = null;  //  En attente d'appel de setSVGImageResource
        buttonZone = new RectF();
        buttonState = BUTTON_STATES.UNPRESSED;
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

    public void setColors(String[] colors) {      //  Couleurs dont 2 utilisées pour Front/Back (quand bouton non pressé)
        this.colors = colors;
    }

    public void setFrontColorIndex(int colorIndex) {    //  Bouton non pressé => Front/Back, Bouton pressé => Back/Alternate
        frontColorIndex = colorIndex;
    }

    public void setBackColorIndex(int colorIndex) {
        backColorIndex = colorIndex;
    }

    public void setAlternateColorIndex(int colorIndex) {   //  Index à utiliser si bouton pressé => Back/Alternate
        alternateColorIndex = colorIndex;
    }

    public boolean onButtonTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clickDownInButtonZone = true;
            buttonState = BUTTON_STATES.PRESSED;
            invalidate();
        }
        if ((action == MotionEvent.ACTION_MOVE) || (action == MotionEvent.ACTION_UP)) {
            if (clickDownInButtonZone) {
                if (buttonZone.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                    if (action == MotionEvent.ACTION_UP) {
                        buttonState = BUTTON_STATES.UNPRESSED;
                        invalidate();
                        if (mOnCustomClickListener != null) {
                            mOnCustomClickListener.onCustomClick();
                        }
                    }
                } else {
                    clickDownInButtonZone = false;
                    buttonState = BUTTON_STATES.UNPRESSED;
                    invalidate();
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int frontStateColorIndex = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? backColorIndex : frontColorIndex);
        int backStateColorIndex = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? alternateColorIndex : backColorIndex);
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        viewCanvas.drawBitmap(symbolBitmap, 0, 0, null);
        viewCanvas.drawColor(Color.parseColor(COLOR_PREFIX + colors[frontStateColorIndex]), PorterDuff.Mode.SRC_IN);
        backPaint.setColor(Color.parseColor(COLOR_PREFIX + colors[backStateColorIndex]));
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