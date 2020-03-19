package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.Constants.COLOR_MASK_AND;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;

public final class DotMatrixDisplayView extends View {  //  Affichage de caractères dans une grille de carrés avec coordonnées (x,y)  ((0,0) étant en haut à gauche de la grille)
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    //region Variables
    private int[][] gridColorValues;
    private RectF displayMarginCoeffs;
    private RectF gridMargins;
    private int gridStartX;
    private Rect gridRect;
    private Rect gridDisplayRect;
    private Rect gridScrollRect;
    private Point scrollStart;
    private Point symbolPos;
    private float dotCellSize;
    private float dotSize;
    private float dotRightMarginCoeff;
    private Paint dotPaint;
    private PointF dotPoint;
    private boolean drawing;
    private Bitmap viewBitmap;
    private Canvas viewCanvas;
    private RectF viewCanvasRect;
    private Paint viewCanvasBackPaint;
    private float backCornerRadius;
    private long minClickTimeInterval;
    private long lastClickUpTime;
    private BUTTON_STATES buttonState;
    private boolean clickDownInButtonZone;
    private Rect buttonZone;
    //endregion

    public DotMatrixDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final RectF DISPLAY_MARGIN_SIZE_COEFFS_DEFAULT = new RectF(0.02f, 0.02f, 0.02f, 0.02f);   //  Marge autour de la grille (% de largeur totale)
        final float DISPLAY_DOT_RIGHT_MARGIN_COEFF_DEFAULT = 0.2f;   //  Distance entre carrés (% de largeur d'un carré)
        final Point DEFAULT_FONT_SYMBOL_POS_DEFAULT = new Point(0, 0);   //  Position du prochain symbole à afficher (en coordonnées de la grille (x,y), (0,0) étant le carré en haut à gauche)
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click
        final String BACK_COLOR_DEFAULT = "000000";

        displayMarginCoeffs = DISPLAY_MARGIN_SIZE_COEFFS_DEFAULT;
        dotRightMarginCoeff = DISPLAY_DOT_RIGHT_MARGIN_COEFF_DEFAULT;
        symbolPos = DEFAULT_FONT_SYMBOL_POS_DEFAULT;
        scrollStart = new Point();
        setupDotPaint();
        setupViewCanvasBackPaint();
        setBackColor(BACK_COLOR_DEFAULT);
        dotPoint = new PointF();
        drawing = false;
        buttonState = BUTTON_STATES.UNPRESSED;
        minClickTimeInterval = MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE;
        lastClickUpTime = 0;
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

        gridColorValues = null;
        viewCanvasBackPaint = null;
        viewCanvas = null;
        dotPaint = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mw = MeasureSpec.getMode(widthMeasureSpec);
        int wm = MeasureSpec.getSize(widthMeasureSpec);
        int mh = MeasureSpec.getMode(heightMeasureSpec);
        int hm = MeasureSpec.getSize(heightMeasureSpec);

        int ws = wm;   // Largeur souhaitée = Largeur proposée

        calcInternalDimensions(wm);
        int h = (int) (gridMargins.top + dotCellSize * ((float) gridDisplayRect.height() - 1) + dotSize + gridMargins.bottom + 0.5f);
        int hs = h;    // Hauteur souhaitée

        int w = ws;
        if (mw == MeasureSpec.EXACTLY) {
            w = wm;
        }
        if (mw == MeasureSpec.AT_MOST) {
            w = Math.min(ws, wm);
        }
        h = hs;
        if (mh == MeasureSpec.EXACTLY) {
            h = hm;
        }
        if (mh == MeasureSpec.AT_MOST) {
            h = Math.min(hs, hm);
        }
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int BACK_CORNER_RADIUS = 35;     //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi

        super.onSizeChanged(w, h, oldw, oldh);

        calcInternalDimensions(w);
        viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        viewCanvasRect = new RectF(0, 0, w, h);
        backCornerRadius = (Math.min(w, h) * BACK_CORNER_RADIUS) / 200;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawing = true;
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        for (int i = 0; i <= (gridDisplayRect.width() - 1); i = i + 1) {
            int gridX = gridDisplayRect.left + i;
            if ((gridX >= gridScrollRect.left) && (gridX <= (gridScrollRect.right - 1))) {  //  On est dans une zone éventuellement en cours de scroll
                gridX = gridX + scrollStart.x - gridScrollRect.left;
                if (gridX >= gridScrollRect.right) {
                    gridX = gridX - gridScrollRect.width();
                }
            }
            for (int j = 0; j <= (gridDisplayRect.height() - 1); j = j + 1) {
                int gridY = gridDisplayRect.top + j;
                if ((gridY >= gridScrollRect.top) && (gridY <= (gridScrollRect.bottom - 1))) {   //  On est dans une zone éventuellement en cours de scroll
                    gridY = gridY + scrollStart.y - gridScrollRect.top;
                    if (gridY >= gridScrollRect.bottom) {
                        gridY = gridY - gridScrollRect.height();
                    }
                }
                dotPaint.setColor((buttonState.equals(BUTTON_STATES.PRESSED)) ? rgbContrast(gridColorValues[gridY][gridX]) : gridColorValues[gridY][gridX]);
                dotPoint.set(gridMargins.left + (float) gridStartX + (float) i * dotCellSize, gridMargins.top + (float) j * dotCellSize);
                viewCanvas.drawRect(dotPoint.x, dotPoint.y, dotPoint.x + dotSize, dotPoint.y + dotSize, dotPaint);
            }
        }
        viewCanvas.drawRoundRect(viewCanvasRect, backCornerRadius, backCornerRadius, viewCanvasBackPaint);
        canvas.drawBitmap(viewBitmap, 0, 0, null);
        drawing = false;
    }

    private int rgbContrast(int colorValue) {  //  Rotation des couleurs pour faire un contraste
        int c = colorValue & COLOR_MASK_AND;   //  FFRRGGBB AND 00FFFFFF => 00RRGGBB;
        return ((c >> 8) | (c << 16)) | (~COLOR_MASK_AND);   //  (0000RRGG  OR  GGBB0000) => GGBBRRGG; OR  FF000000 => FFBBRRGG  (cad rotation à droite)
    }

    private boolean onButtonTouch(View v, MotionEvent event) {
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
                if (buttonZone == null) {
                    buttonZone = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
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

    public void setGridRect(Rect gridRect) {   //  Grille sous-jacente de stockage des valeurs affichées  (left=0, top=0, right=width, bottom=height)
        this.gridRect = gridRect;
        gridColorValues = new int[gridRect.height()][gridRect.width()];
    }

    public Rect getGridRect() {
        return gridRect;
    }

    public void setGridDisplayRect(Rect gridDisplayRect) {   //  Emplacement de l'affichage (sous-rectangle de la grille gridRect) (left>=gridRect.left, top>=gridRect.top, right<=gridRect.right, bottom<=gridRect.bottom)
        this.gridDisplayRect = gridDisplayRect;
    }

    public Rect getGridDisplayRect() {
        return gridDisplayRect;
    }

    public void setGridScrollRect(Rect gridScrollRect) {   //   Zone à scroller (sous-rectangle de la grille gridRect) (left>=gridRect.left, top>=gridRect.top, right<=gridRect.right, bottom<=gridRect.bottom)
        this.gridScrollRect = gridScrollRect;
        noScroll();
    }

    public Rect getGridScrollRect() {
        return gridScrollRect;
    }

    public void setSymbolPos(int x, int y) {
        symbolPos.set(x, y);
    }

    public void setSymbolPos(Point sp) {
        symbolPos.set(sp.x, sp.y);
    }

    public Point getSymbolPos() {
        return symbolPos;
    }

    public void setDisplayMarginCoeffs(RectF displayMarginCoeffs) {   //  Marges autour de l'affichage (en % de largeur totale)
        this.displayMarginCoeffs = displayMarginCoeffs;
    }

    public void setDotRightMarginCoeff(int dotRightMarginCoeff) {   //  Marge droite pour chaque carré (en % de largeur d'un carré)
        this.dotRightMarginCoeff = dotRightMarginCoeff;
    }

    public void setBackColor(String color) {
        viewCanvasBackPaint.setColor(Color.parseColor(COLOR_PREFIX + color));
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void scrollLeft() {
        scrollStart.x = scrollStart.x + 1;
        if (scrollStart.x >= gridScrollRect.right) {
            scrollStart.x = gridScrollRect.left;
        }
    }

    public void scrollRight() {
        scrollStart.x = scrollStart.x - 1;
        if (scrollStart.x < gridScrollRect.left) {
            scrollStart.x = gridScrollRect.right - 1;
        }
    }

    public void scrollTop() {
        scrollStart.y = scrollStart.y + 1;
        if (scrollStart.y >= gridScrollRect.bottom) {
            scrollStart.y = gridScrollRect.top;
        }
    }

    public void scrollBottom() {
        scrollStart.y = scrollStart.y - 1;
        if (scrollStart.y < gridScrollRect.top) {
            scrollStart.y = gridScrollRect.bottom - 1;
        }
    }

    public void noScroll() {
        scrollStart.set(gridScrollRect.left, gridScrollRect.top);
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    public void updateDisplay() {
        invalidate();
    }

    public void setDot(int x, int y, String color) {
        gridColorValues[y][x] = Color.parseColor(COLOR_PREFIX + color);
    }

    public void fillRect(Rect rect, String color) {
        int colValue = Color.parseColor(COLOR_PREFIX + color);
        for (int i = rect.left; i <= (rect.right - 1); i = i + 1) {
            for (int j = rect.top; j <= (rect.bottom - 1); j = j + 1) {
                gridColorValues[j][i] = colValue;
            }
        }
    }

    public void writeText(String text, String color, DotMatrixFont dotMatrixFont) {
        writeText(text, color, null, dotMatrixFont);
    }

    public void writeText(String text, String color, DotMatrixFont extraFont, DotMatrixFont defaultFont) {   //  A partir de symbolPos; Spécifier extraFont différent de null si text mélange extraFont et defaultFont; extraFont a la priorité sur defaultFont
        DotMatrixFont font = null;

        int colValue = Color.parseColor(COLOR_PREFIX + color);
        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            Character ch = text.charAt(i);
            DotMatrixSymbol symbol = null;
            if (extraFont != null) {
                font = extraFont;
                symbol = font.getSymbol(ch);
            }
            if (symbol == null) {
                font = defaultFont;
                symbol = font.getSymbol(ch);
            }
            //  Le symbole et sa fonte ont été déterminés
            symbolPos.offset(symbol.getPosOffset().x, symbol.getPosOffset().y);   //  Appliquer un décalage éventuel avant l'affichage (si symbole de surcharge)
            drawSymbol(symbol, colValue);
            symbolPos.offset(-symbol.getPosOffset().x, -symbol.getPosOffset().y);   //  Retour au bercail
            if (!symbol.isOverwrite()) {
                symbolPos.offset(symbol.getDimensions().width + font.getRightMargin(), 0);   //  Prêt pour l'affichage du symbole suivant, sur la même ligne
            }
        }
        font = null;
    }

    private void drawSymbol(DotMatrixSymbol symbol, int colValue) {   //  A partir de symbolPos
        int[][] symbolData = symbol.getData();
        for (int i = 0; i <= (symbol.getDimensions().width - 1); i = i + 1) {
            int gridX = symbolPos.x + i;
            for (int j = 0; j <= (symbol.getDimensions().height - 1); j = j + 1) {
                int gridY = symbolPos.y + j;
                if (symbolData[j][i] == 1) {
                    gridColorValues[gridY][gridX] = colValue;
                }
            }
        }
    }

    private void calcInternalDimensions(int viewWidth) {  // Ajustement à un entier pour éviter le dessin d'une grille irrrégulière dans la largeur ou hauteur de ses éléments
        gridMargins = new RectF((int) ((float) viewWidth * displayMarginCoeffs.left + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.top + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.right + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.bottom + 0.5f));
        dotCellSize = (int) (((float) viewWidth - (gridMargins.left + gridMargins.right)) / (float) gridDisplayRect.width());
        dotSize = (int) (dotCellSize / (1 + dotRightMarginCoeff) + 0.5f);
        gridStartX = (int) (((float) viewWidth - (gridMargins.left + (float) gridDisplayRect.width() * dotCellSize + gridMargins.right)) / 2 + 0.5f);
    }

    private void setupDotPaint() {
        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private void setupViewCanvasBackPaint() {
        viewCanvasBackPaint = new Paint();
        viewCanvasBackPaint.setAntiAlias(true);
        viewCanvasBackPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
    }

}
