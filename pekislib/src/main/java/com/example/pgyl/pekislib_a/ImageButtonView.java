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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.larvalabs.svgandroid.SVGParser;

import java.util.HashMap;
import java.util.Map;

import static com.example.pgyl.pekislib_a.ButtonColorBox.COLOR_TYPES;
import static com.example.pgyl.pekislib_a.ColorUtils.ColorDef;
import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.MiscUtils.DpToPixels;
import static com.example.pgyl.pekislib_a.MiscUtils.getPictureFromDrawable;
import static com.example.pgyl.pekislib_a.PointRectUtils.ALIGN_WIDTH_HEIGHT;
import static com.example.pgyl.pekislib_a.PointRectUtils.getMaxSubRect;

public final class ImageButtonView extends TextView {
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
    private float outlineStrokeWidthPx;
    private String outlineColor;
    private ButtonColorBox colorBox;
    private boolean hasFrontColorFilter;
    private boolean hasBackColorFilter;
    private Map<COLOR_TYPES, String> defaultColorsMap;
    private boolean clickDownInButtonZone;
    private RectF buttonZone;
    private Bitmap viewBitmap;
    private Canvas viewCanvas;
    private Paint imageBackPaint;
    private Paint buttonBackPaint;
    private Paint buttonOutlinePaint;
    private RectF viewCanvasRect;
    private RectF viewCanvasRectExceptOutline;
    private Bitmap imageBitmap;
    private Picture picture;
    private float imageAspectRatio;
    private RectF imageRelativePositionCoeffs;
    private float imageSizeCoeff;
    private Context context;
    //endregion

    public ImageButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        init();
    }

    public void init() {
        final float SIZE_COEFF_DEFAULT = 0.8f;   //  (0..1)
        final String TEXT_DEFAULT = "";
        final String TEXT_COLOR_DEFAULT = "000000";
        final int OUTLINE_STROKE_WIDTH_DP_DEFAULT = 2;   //  dp
        final String OUTLINE_COLOR_DEFAULT = "A0A0A0";
        final int PC_BACK_CORNER_RADIUS_DEFAULT = 35;    //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click

        buttonZone = new RectF();
        buttonState = BUTTON_STATES.UNPRESSED;
        pcBackCornerRadius = PC_BACK_CORNER_RADIUS_DEFAULT;
        imageSizeCoeff = SIZE_COEFF_DEFAULT;
        outlineStrokeWidthPx = (int) DpToPixels(OUTLINE_STROKE_WIDTH_DP_DEFAULT, context);
        outlineColor = OUTLINE_COLOR_DEFAULT;
        minClickTimeInterval = MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE;
        imageRelativePositionCoeffs = ALIGN_WIDTH_HEIGHT;
        picture = null;   //  En attendant l'affectation éventuelle via setSVGImageResource ou setPNGImageResource
        lastClickUpTime = 0;
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onButtonTouch(v, event);
            }
        });
        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        setText(TEXT_DEFAULT);
        setTextColor(Color.parseColor(COLOR_PREFIX + TEXT_COLOR_DEFAULT));
        setupColorBox();
        setupImageBackPaint();
        setupButtonBackPaint();
        setupButtonOutlinePaint();
    }

    public void setSVGImageResource(int resId) {
        picture = SVGParser.getSVGFromResource(getResources(), resId).getPicture();
        imageAspectRatio = (float) picture.getHeight() / (float) picture.getWidth();
    }

    public void setPNGImageResource(int resId) {
        picture = getPictureFromDrawable((BitmapDrawable) getResources().getDrawable(resId, context.getTheme()));
        imageAspectRatio = (float) picture.getHeight() / (float) picture.getWidth();
    }

    public ButtonColorBox getColorBox() {   //   On peut alors modifier les couleurs (colorBox.setColor...), puis faire updateDisplayColors() pour mettre à jour l'affichage
        return colorBox;
    }

    public void setHasFrontColorFilter(boolean hasFrontColorFilter) {
        this.hasFrontColorFilter = hasFrontColorFilter;
    }

    public void setHasBackColorFilter(boolean hasBackColorFilter) {
        this.hasBackColorFilter = hasBackColorFilter;
    }

    public void setImageRelativePositionCoeffs(RectF imageRelativePositionCoeffs) {
        this.imageRelativePositionCoeffs = imageRelativePositionCoeffs;
    }

    public void setImageSizeCoeff(float imageSizeCoeff) {
        this.imageSizeCoeff = imageSizeCoeff;
    }

    public void setOutlineColor(String outlineColor) {
        this.outlineColor = outlineColor;
    }

    public void setOutlineStrokeWidthDp(int outlineStrokeWidthDp) {
        outlineStrokeWidthPx = (int) DpToPixels(outlineStrokeWidthDp, context);
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    public void setPcBackCornerRadius(int pcBackCornerRadius) {
        this.pcBackCornerRadius = pcBackCornerRadius;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        imageBitmap = null;
        viewBitmap = null;
        viewCanvas = null;
        imageBackPaint = null;
        buttonBackPaint = null;
        buttonOutlinePaint = null;
        colorBox.close();
        colorBox = null;
        defaultColorsMap.clear();
        defaultColorsMap = null;
        picture = null;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        viewCanvasRect = new RectF(0, 0, w, h);
        viewCanvasRectExceptOutline = new RectF(outlineStrokeWidthPx, outlineStrokeWidthPx, w - outlineStrokeWidthPx, h - outlineStrokeWidthPx);

        imageBitmap = Bitmap.createBitmap((int) viewCanvasRectExceptOutline.width(), (int) viewCanvasRectExceptOutline.height(), Bitmap.Config.ARGB_8888);
        if (picture != null) {
            Canvas imageViewCanvas = new Canvas(imageBitmap);
            imageViewCanvas.drawPicture(picture, getMaxSubRect(viewCanvasRectExceptOutline, imageRelativePositionCoeffs, imageAspectRatio, imageSizeCoeff));
        }
        buttonZone.set(getLeft() + viewCanvasRect.left, getTop() + viewCanvasRect.top, getLeft() + viewCanvasRect.right, getTop() + viewCanvasRect.bottom);
        backCornerRadius = (Math.min(w, h) * pcBackCornerRadius) / 200;    //  Rayon pour coin arrondi (% appliqué à la moitié de la largeur ou hauteur)
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int frontColor = (buttonState.equals(BUTTON_STATES.PRESSED)) ? getColor(COLOR_TYPES.PRESSED_FRONT_COLOR).RGBCode : getColor(COLOR_TYPES.UNPRESSED_FRONT_COLOR).RGBCode;
        int backColor = (buttonState.equals(BUTTON_STATES.PRESSED)) ? getColor(COLOR_TYPES.PRESSED_BACK_COLOR).RGBCode : getColor(COLOR_TYPES.UNPRESSED_BACK_COLOR).RGBCode;

        if (picture != null) {
            viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
            viewCanvas.drawBitmap(imageBitmap, 0, 0, null);
            if (hasFrontColorFilter) {
                viewCanvas.drawColor(frontColor, PorterDuff.Mode.SRC_IN);
            }
            if (hasBackColorFilter) {
                imageBackPaint.setColor(backColor);
                viewCanvas.drawRoundRect(viewCanvasRectExceptOutline, backCornerRadius, backCornerRadius, imageBackPaint);
            }
        } else {   //  Pas de Picture => Ractangle simple suffit (Back); le Front sera le texte (avec sa couleur)
            if (hasBackColorFilter) {
                buttonBackPaint.setColor(backColor);
                viewCanvas.drawRoundRect(viewCanvasRectExceptOutline, backCornerRadius, backCornerRadius, buttonBackPaint);
            }
        }
        if (outlineStrokeWidthPx != 0) {
            buttonOutlinePaint.setStrokeWidth(outlineStrokeWidthPx);
            buttonOutlinePaint.setColor(Color.parseColor(COLOR_PREFIX + outlineColor));
            viewCanvas.drawRoundRect(viewCanvasRectExceptOutline, backCornerRadius, backCornerRadius, buttonOutlinePaint);
        }
        canvas.drawBitmap(viewBitmap, 0, 0, null);

        super.onDraw(canvas);   //  Dessinera le texte au-dessus de l'image
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

    public void updateDisplayColors() {
        invalidate();
    }

    private ColorDef getColor(COLOR_TYPES colorType) {   //  Si Null rencontré (cad souhait de couleur par défaut lors du ColorBox.setColor), alors renvoyer couleur par défaut
        ColorDef colorDef = colorBox.getColor(colorType);
        if (colorDef == null) {
            colorBox.setColor(colorType, defaultColorsMap.get(colorType));
            colorDef = colorBox.getColor(colorType);
        }
        return colorDef;
    }

    private void setupColorBox() {
        final boolean HAS_FRONT_COLOR_FILTER_DEFAULT = true;
        final boolean HAS_BACK_COLOR_FILTER_DEFAULT = true;
        final String UNPRESSED_FRONT_COLOR_DEFAULT = "000000";
        final String UNPRESSED_BACK_COLOR_DEFAULT = "C0C0C0";
        final String PRESSED_FRONT_COLOR_DEFAULT = UNPRESSED_FRONT_COLOR_DEFAULT;
        final String PRESSED_BACK_COLOR_DEFAULT = "FF9A22";

        colorBox = new ButtonColorBox();
        hasFrontColorFilter = HAS_FRONT_COLOR_FILTER_DEFAULT;
        hasBackColorFilter = HAS_BACK_COLOR_FILTER_DEFAULT;
        defaultColorsMap = new HashMap<COLOR_TYPES, String>();
        defaultColorsMap.put(COLOR_TYPES.UNPRESSED_FRONT_COLOR, UNPRESSED_FRONT_COLOR_DEFAULT);
        defaultColorsMap.put(COLOR_TYPES.UNPRESSED_BACK_COLOR, UNPRESSED_BACK_COLOR_DEFAULT);
        defaultColorsMap.put(COLOR_TYPES.PRESSED_FRONT_COLOR, PRESSED_FRONT_COLOR_DEFAULT);
        defaultColorsMap.put(COLOR_TYPES.PRESSED_BACK_COLOR, PRESSED_BACK_COLOR_DEFAULT);
        for (COLOR_TYPES ct : COLOR_TYPES.values()) {
            colorBox.setColor(ct, defaultColorsMap.get(ct));
        }
    }

    private void setupImageBackPaint() {
        imageBackPaint = new Paint();
        imageBackPaint.setAntiAlias(true);
        imageBackPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        imageBackPaint.setStyle(Paint.Style.FILL);
    }

    private void setupButtonBackPaint() {
        buttonBackPaint = new Paint();
        buttonBackPaint.setAntiAlias(true);
        buttonBackPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        buttonBackPaint.setStyle(Paint.Style.FILL);
    }

    private void setupButtonOutlinePaint() {
        buttonOutlinePaint = new Paint();
        buttonOutlinePaint.setAntiAlias(true);
        buttonOutlinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        buttonOutlinePaint.setStyle(Paint.Style.STROKE);
    }
}