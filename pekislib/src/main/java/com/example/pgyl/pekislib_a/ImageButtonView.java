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

import static com.example.pgyl.pekislib_a.ColorUtils.BUTTON_COLOR_TYPES;
import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.MiscUtils.DpToPixels;
import static com.example.pgyl.pekislib_a.MiscUtils.getPictureFromDrawable;
import static com.example.pgyl.pekislib_a.PointRectUtils.ALIGN_WIDTH_HEIGHT;
import static com.example.pgyl.pekislib_a.PointRectUtils.getMaxSubRect;

public final class ImageButtonView extends TextView {

    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
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
    private ColorBox colorBox;
    private ColorBox defaultColorBox;
    private boolean hasFrontColorFilter;
    private boolean hasBackColorFilter;
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

    public void init() {   //  Le bouton par défaut est un rectangle arrondi gris, entouré de gris clair, devenant orange si pressé :), avec un texte noir
        final int OUTLINE_STROKE_WIDTH_DP_DEFAULT = 2;   //  dp
        final float SIZE_COEFF_DEFAULT = 0.8f;   //  (0..1)
        final String TEXT_DEFAULT = "";
        final int PC_BACK_CORNER_RADIUS_DEFAULT = 35;    //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click

        buttonZone = new RectF();
        buttonState = BUTTON_STATES.UNPRESSED;
        pcBackCornerRadius = PC_BACK_CORNER_RADIUS_DEFAULT;
        imageSizeCoeff = SIZE_COEFF_DEFAULT;
        outlineStrokeWidthPx = (int) DpToPixels(OUTLINE_STROKE_WIDTH_DP_DEFAULT, context);
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
        setupImageBackPaint();
        setupButtonBackPaint();
        setupButtonOutlinePaint();
        setText(TEXT_DEFAULT);
        setupDefaultColorBox();
        setupColorBox();
        setTextColor(defaultColorBox.getColor(BUTTON_COLOR_TYPES.TEXT.INDEX()).RGBInt);   //  Nécessaire ici car seul invalidate() est ensuite appelé (et non pas ImageButtonView.updateDisplay())
    }

    public void setSVGImageResource(int resId) {
        picture = SVGParser.getSVGFromResource(getResources(), resId).getPicture();
        imageAspectRatio = (float) picture.getHeight() / (float) picture.getWidth();
    }

    public void setPNGImageResource(int resId) {
        picture = getPictureFromDrawable((BitmapDrawable) getResources().getDrawable(resId, context.getTheme()));
        imageAspectRatio = (float) picture.getHeight() / (float) picture.getWidth();
    }

    public ColorBox getColorBox() {   //   On peut alors modifier les couleurs (colorBox.setColor...), puis faire updateDisplayColors() pour mettre à jour l'affichage
        return colorBox;
    }

    public ColorBox getDefaultColorBox() {   //   On peut alors modifier les couleurs (colorBox.setColor...), puis faire updateDisplayColors() pour mettre à jour l'affichage
        return defaultColorBox;
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
        int frontColor = (buttonState.equals(BUTTON_STATES.PRESSED)) ? colorBox.getColor(BUTTON_COLOR_TYPES.PRESSED_FRONT.INDEX()).RGBInt : colorBox.getColor(BUTTON_COLOR_TYPES.UNPRESSED_FRONT.INDEX()).RGBInt;
        int backColor = (buttonState.equals(BUTTON_STATES.PRESSED)) ? colorBox.getColor(BUTTON_COLOR_TYPES.PRESSED_BACK.INDEX()).RGBInt : colorBox.getColor(BUTTON_COLOR_TYPES.UNPRESSED_BACK.INDEX()).RGBInt;
        int outlineColor = colorBox.getColor(BUTTON_COLOR_TYPES.OUTLINE.INDEX()).RGBInt;

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
        } else {   //  Pas de Picture => Un ractangle simple suffit; Le texte (avec sa couleur) viendra au-dessus
            if (hasBackColorFilter) {
                buttonBackPaint.setColor(backColor);
                viewCanvas.drawRoundRect(viewCanvasRectExceptOutline, backCornerRadius, backCornerRadius, buttonBackPaint);
            }
        }
        if (outlineStrokeWidthPx != 0) {
            buttonOutlinePaint.setStrokeWidth(outlineStrokeWidthPx);
            buttonOutlinePaint.setColor(outlineColor);
            viewCanvas.drawRoundRect(viewCanvasRectExceptOutline, backCornerRadius, backCornerRadius, buttonOutlinePaint);
        }
        canvas.drawBitmap(viewBitmap, 0, 0, null);

        super.onDraw(canvas);   //  Dessinera le texte (avec sa couleur) au-dessus
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

    public void updateDisplay() {   //  A appeler à chaque mise à jour de colorBox (ou le cas échéant de defaultColorBow)
        setTextColor(colorBox.getColor(BUTTON_COLOR_TYPES.TEXT.INDEX()).RGBInt);   //  TextView.setTextColor ne devrait pas être appelé dans onDraw() car TextView.setTextColor appelle déjà lui-même invalidate() et donc onDraw() => Boucle infinie
        invalidate();   //  Semble obligatoire dans certains cas même après TextView.SetTextColor()
    }

    private void setupDefaultColorBox() {
        final String UNPRESSED_FRONT_COLOR_DEFAULT = "000000";
        final String UNPRESSED_BACK_COLOR_DEFAULT = "C0C0C0";
        final String PRESSED_FRONT_COLOR_DEFAULT = UNPRESSED_FRONT_COLOR_DEFAULT;
        final String PRESSED_BACK_COLOR_DEFAULT = "FF9A22";
        final String OUTLINE_COLOR_DEFAULT = "A0A0A0";
        final String TEXT_COLOR_DEFAULT = "000000";

        defaultColorBox = new ColorBox();
        defaultColorBox.setColor(BUTTON_COLOR_TYPES.UNPRESSED_FRONT.INDEX(), UNPRESSED_FRONT_COLOR_DEFAULT);
        defaultColorBox.setColor(BUTTON_COLOR_TYPES.UNPRESSED_BACK.INDEX(), UNPRESSED_BACK_COLOR_DEFAULT);
        defaultColorBox.setColor(BUTTON_COLOR_TYPES.PRESSED_FRONT.INDEX(), PRESSED_FRONT_COLOR_DEFAULT);
        defaultColorBox.setColor(BUTTON_COLOR_TYPES.PRESSED_BACK.INDEX(), PRESSED_BACK_COLOR_DEFAULT);
        defaultColorBox.setColor(BUTTON_COLOR_TYPES.OUTLINE.INDEX(), OUTLINE_COLOR_DEFAULT);
        defaultColorBox.setColor(BUTTON_COLOR_TYPES.TEXT.INDEX(), TEXT_COLOR_DEFAULT);
    }

    private void setupColorBox() {
        final boolean HAS_FRONT_COLOR_FILTER_DEFAULT = true;
        final boolean HAS_BACK_COLOR_FILTER_DEFAULT = true;

        colorBox = new ColorBox();
        hasFrontColorFilter = HAS_FRONT_COLOR_FILTER_DEFAULT;
        hasBackColorFilter = HAS_BACK_COLOR_FILTER_DEFAULT;
        colorBox.setColor(BUTTON_COLOR_TYPES.UNPRESSED_FRONT.INDEX(), defaultColorBox.getColor(BUTTON_COLOR_TYPES.UNPRESSED_FRONT.INDEX()).RGBString);
        colorBox.setColor(BUTTON_COLOR_TYPES.UNPRESSED_BACK.INDEX(), defaultColorBox.getColor(BUTTON_COLOR_TYPES.UNPRESSED_BACK.INDEX()).RGBString);
        colorBox.setColor(BUTTON_COLOR_TYPES.PRESSED_FRONT.INDEX(), defaultColorBox.getColor(BUTTON_COLOR_TYPES.PRESSED_FRONT.INDEX()).RGBString);
        colorBox.setColor(BUTTON_COLOR_TYPES.PRESSED_BACK.INDEX(), defaultColorBox.getColor(BUTTON_COLOR_TYPES.PRESSED_BACK.INDEX()).RGBString);
        colorBox.setColor(BUTTON_COLOR_TYPES.OUTLINE.INDEX(), defaultColorBox.getColor(BUTTON_COLOR_TYPES.OUTLINE.INDEX()).RGBString);
        colorBox.setColor(BUTTON_COLOR_TYPES.TEXT.INDEX(), defaultColorBox.getColor(BUTTON_COLOR_TYPES.TEXT.INDEX()).RGBString);
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