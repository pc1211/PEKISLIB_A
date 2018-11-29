package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import java.util.EnumMap;

import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.PointRectUtils.ALIGN_RIGHT_BOTTOM;
import static com.example.pgyl.pekislib_a.PointRectUtils.FULL_SIZE_COEFF;
import static com.example.pgyl.pekislib_a.PointRectUtils.getCircleBoundingRect;
import static com.example.pgyl.pekislib_a.PointRectUtils.getPointInCircle;
import static com.example.pgyl.pekislib_a.PointRectUtils.getSubRect;

//  Dessiner une roue avec les couleurs à éditer
//  ainsi qu'un marqueur SVG placé au Nord-Ouest, dirigé initialement vers le milieu de la couleur courante
public final class ColorWheelView extends View {
    public interface onColorIndexChangeListener {
        void onColorIndexChange(int colorIndex);
    }

    public void setOnColorIndexChangeListener(onColorIndexChangeListener listener) {
        mOnColorIndexChangeListener = listener;
    }

    private onColorIndexChangeListener mOnColorIndexChangeListener;

    //region Constantes
    private enum MARKER_STATES {
        PIN, UNPIN
    }

    private final float MARKER_ANGLE = 3 * (float) Math.PI / 4;   //  Angle de l'axe du marqueur
    private float ROTATION_ANGLE_DEFAULT_VALUE = 0;
    //endregion
    //region Variables
    private Bitmap viewBitmap;
    private Canvas viewCanvas;
    private PointF wheelCenter;
    private float wheelRadius;
    private RectF wheelRect;
    private boolean markerEnabled;
    private MARKER_STATES markerState;
    EnumMap<MARKER_STATES, PointF> markerDestPointsMap;
    private Picture markerPicture;
    private Bitmap markerBitmap;
    private PointF markerDestPoint;
    private RectF markerCellCanvasRect;
    private Paint fillPaint;
    private Paint outlinePaint;
    private boolean clickDownInZone;
    private Rect viewRect;
    private int[] colors;
    private int colorIndex;
    private boolean drawing;
    private float rotationAngle;
    private float startAngle;
    private float angleSpread;
    private ColorWheelViewUpdater colorWheelViewUpdater;
    //endregion

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final int COLOR_INDEX_DEFAULT_VALUE = 0;

        setupFillPaint();
        setupOutlinePaint();
        wheelCenter = new PointF();
        rotationAngle = ROTATION_ANGLE_DEFAULT_VALUE;
        colorIndex = COLOR_INDEX_DEFAULT_VALUE;
        markerEnabled = true;
        markerState = MARKER_STATES.PIN;
        markerDestPointsMap = new EnumMap<MARKER_STATES, PointF>(MARKER_STATES.class);
        markerDestPoint = new PointF();
        SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.redpin);
        markerPicture = svg.getPicture();
        drawing = false;
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return ColorWheelView.this.onTouch(v, event);
            }
        });
        colorWheelViewUpdater = new ColorWheelViewUpdater(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        colorWheelViewUpdater.close();
        colorWheelViewUpdater = null;
        fillPaint = null;
        outlinePaint = null;
        viewCanvas = null;
        viewBitmap.recycle();
        viewBitmap = null;
        markerDestPointsMap = null;
        markerBitmap = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final float MARKER_TIP_TO_WHEEL_CENTER_COEFF = 0.9f;    //  Distance relative de la pointe du marqueur jusqu'au centre de la roue (0..1) (1 quand la pointe du marqueur touche le bord de la roue)

        super.onSizeChanged(w, h, oldw, oldh);

        viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        wheelCenter.set(w / 2, h / 2);
        wheelRadius = Math.min(getWidth(), getHeight()) / 2;
        wheelRect = getCircleBoundingRect(wheelCenter, wheelRadius);
        PointF markerTipPoint = getPointInCircle(wheelCenter, wheelRadius, MARKER_ANGLE);
        markerCellCanvasRect = new RectF(0, 0, markerTipPoint.x - wheelRect.left, markerTipPoint.y - wheelRect.top);
        markerDestPointsMap.put(MARKER_STATES.UNPIN, new PointF(markerTipPoint.x - markerCellCanvasRect.width(), markerTipPoint.y - markerCellCanvasRect.height()));
        markerTipPoint = getPointInCircle(wheelCenter, wheelRadius * MARKER_TIP_TO_WHEEL_CENTER_COEFF, MARKER_ANGLE);
        markerDestPointsMap.put(MARKER_STATES.PIN, new PointF(markerTipPoint.x - markerCellCanvasRect.width(), markerTipPoint.y - markerCellCanvasRect.height()));
        markerDestPoint.set(markerDestPointsMap.get(markerState));
        markerBitmap = createMarkerBitmap(markerPicture);
        markerPicture = null;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void enableMarker() {
        markerEnabled = true;
    }

    public void disableMarker() {
        markerEnabled = false;
    }

    public void pinMarker() {
        setMarkerState(MARKER_STATES.PIN);
    }

    public void unpinMarker() {
        setMarkerState(MARKER_STATES.UNPIN);
    }

    public void setColors(String[] colors) {
        this.colors = new int[colors.length];
        for (int i = 0; i <= (colors.length - 1); i = i + 1) {
            this.colors[i] = ((colors[i] != null) ? Color.parseColor(COLOR_PREFIX + colors[i]) : Color.BLACK);
            angleSpread = 2 * (float) Math.PI / colors.length;
        }
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public void setColor(int colorIndex, String color) {
        colors[colorIndex] = Color.parseColor(COLOR_PREFIX + color);
    }

    public float getAngleSpread() {
        return angleSpread;
    }

    public void resetRotationAngle() {
        rotationAngle = ROTATION_ANGLE_DEFAULT_VALUE;
    }

    private boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clickDownInZone = true;
            startAngle = getAngle(v, x, y);
        }
        if ((action == MotionEvent.ACTION_MOVE) || (action == MotionEvent.ACTION_UP)) {
            if (clickDownInZone) {
                if (viewRect == null) {
                    viewRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if (viewRect.contains(v.getLeft() + x, v.getTop() + y)) {
                    if (action == MotionEvent.ACTION_UP) {
                        onTouchActionUp();
                    }
                    if (action == MotionEvent.ACTION_MOVE) {
                        if (!isDrawing()) {
                            float endAngle = getAngle(v, x, y);
                            if (endAngle != startAngle) {
                                unpinMarker();
                                rotate(endAngle - startAngle);
                                startAngle = endAngle;
                            }
                        }
                    }
                } else {
                    clickDownInZone = false;
                    onTouchActionUp();
                }
            }
        }
        return true;
    }

    private void onTouchActionUp() {
        if (rotationAngle != 0) {
            long nowm = System.currentTimeMillis();
            colorWheelViewUpdater.rotateAnimation(-rotationAngle, nowm);       //  Aller au centre
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int RECT_FILL_COLOR = Color.BLACK;
        final int WHEEL_OUTLINE_COLOR = Color.WHITE;
        final boolean DRAW_ARC_USE_CENTER = true;

        super.onDraw(canvas);

        drawing = true;
        fillPaint.setColor(RECT_FILL_COLOR);
        this.viewCanvas.drawRect(0, 0, (float) getWidth(), (float) getHeight(), fillPaint);
        int i = colorIndex;   //  On commence par la couleur d'indice colorIndex
        int j = 0;            //  On commence par le morceau de pizza en face du marqueur
        do {
            fillPaint.setColor(colors[i]);
            this.viewCanvas.drawArc(wheelRect, (float) (Math.toDegrees(-((MARKER_ANGLE + rotationAngle) - j * angleSpread + angleSpread / 2))), (float) (Math.toDegrees(angleSpread)), DRAW_ARC_USE_CENTER, fillPaint);   //  Dessiner un morceau de pizza
            j = j + 1;
            i = (i + 1) % colors.length;
        }
        while (i != colorIndex);
        outlinePaint.setColor(WHEEL_OUTLINE_COLOR);
        this.viewCanvas.drawCircle(wheelCenter.x, wheelCenter.y, wheelRadius, outlinePaint);
        if (markerEnabled) {
            this.viewCanvas.drawBitmap(markerBitmap, markerDestPoint.x, markerDestPoint.y, null);
        }
        canvas.drawBitmap(viewBitmap, 0, 0, null);    // Afficher tout
        drawing = false;
    }

    public void rotate(float angle) {
        int k;

        float newAngle = rotationAngle + normalizedAngle(angle);
        if (Math.abs(newAngle) > (angleSpread / 2)) {
            if (newAngle > 0) {
                rotationAngle = newAngle - angleSpread;
                k = colorIndex + 1;
                if (k >= colors.length) {
                    k = k - colors.length;
                }
            } else {
                rotationAngle = newAngle + angleSpread;
                k = colorIndex - 1;
                if (k < 0) {
                    k = k + colors.length;
                }
            }
            colorIndex = k;
            if (mOnColorIndexChangeListener != null) {
                mOnColorIndexChangeListener.onColorIndexChange(colorIndex);
            }
        } else {
            rotationAngle = newAngle;
        }
        invalidate();
    }

    private float getAngle(View v, int x, int y) {
        return (float) Math.atan2(v.getHeight() / 2 - y, x - v.getWidth() / 2);
    }

    private float normalizedAngle(float angle) {
        float ret;

        if (angle >= Math.PI) {
            ret = angle - 2 * (float) Math.PI;
        } else {
            if (angle <= -(float) Math.PI) {
                ret = angle + 2 * (float) Math.PI;
            } else {
                ret = angle;
            }
        }
        return ret;
    }

    private Bitmap createMarkerBitmap(Picture picture) {
        Bitmap ret = Bitmap.createBitmap((int) markerCellCanvasRect.width(), (int) markerCellCanvasRect.height(), Bitmap.Config.ARGB_8888);
        Canvas markerCanvas = new Canvas(ret);
        float markerAspectRatio = (float) picture.getHeight() / (float) picture.getWidth();
        markerCanvas.drawPicture(picture, getSubRect(markerCellCanvasRect, ALIGN_RIGHT_BOTTOM, markerAspectRatio, FULL_SIZE_COEFF));
        return ret;
    }

    private void setMarkerState(MARKER_STATES markerType) {
        if (markerEnabled) {
            if (!this.markerState.equals(markerType)) {
                this.markerState = markerType;
                markerDestPoint.set(markerDestPointsMap.get(markerState));
                invalidate();
            }
        }
    }

    private void setupFillPaint() {
        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);
    }

    private void setupOutlinePaint() {
        final int OUTLINE_WIDTH = 1;

        outlinePaint = new Paint();
        outlinePaint.setAntiAlias(true);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(OUTLINE_WIDTH);
    }

}