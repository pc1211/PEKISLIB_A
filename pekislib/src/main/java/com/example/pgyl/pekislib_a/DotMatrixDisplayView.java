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
import static com.example.pgyl.pekislib_a.Constants.COLOR_CONTRASTER;
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
    private int[][] grid;
    private RectF displayMarginCoeffs;
    private RectF gridMargins;
    private int gridStartX;
    private Rect gridRect;
    private Rect displayRect;
    private Rect scrollRect;
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
    private int onColor;
    private int offColor;
    private int backColor;
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

        displayMarginCoeffs = DISPLAY_MARGIN_SIZE_COEFFS_DEFAULT;
        dotRightMarginCoeff = DISPLAY_DOT_RIGHT_MARGIN_COEFF_DEFAULT;
        symbolPos = DEFAULT_FONT_SYMBOL_POS_DEFAULT;
        scrollStart = new Point();
        setupDotPaint();
        setupViewCanvasBackPaint();
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

        grid = null;
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
        int h = (int) (gridMargins.top + dotCellSize * ((float) displayRect.height() - 1) + dotSize + gridMargins.bottom + 0.5f);
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

    public void setGridRect(Rect gridRect) {   //  Grille sous-jacente de stockage des valeurs affichées  (left=0, top=0, right=width, bottom=height)
        this.gridRect = gridRect;
        grid = new int[gridRect.height()][gridRect.width()];
    }

    public Rect getGridRect() {
        return gridRect;
    }

    public void setDisplayRect(Rect displayRect) {   //  Emplacement de l'affichage (sous-rectangle de la grille gridRect) (left>=gridRect.left, top>=gridRect.top, right<=gridRect.right, bottom<=gridRect.bottom)
        this.displayRect = displayRect;
    }

    public Rect getDisplayRect() {
        return displayRect;
    }

    public void setScrollRect(Rect scrollRect) {   //   Zone à scroller (sous-rectangle de la grille gridRect) (left>=gridRect.left, top>=gridRect.top, right<=gridRect.right, bottom<=gridRect.bottom)
        this.scrollRect = scrollRect;
        noScroll();
    }

    public Rect getScrollRect() {
        return scrollRect;
    }

    public Point getSymbolPos() {
        return symbolPos;
    }

    public void setSymbolPos(int x, int y) {
        symbolPos.set(x, y);
    }

    public void setDisplayMarginCoeffs(RectF displayMarginCoeffs) {   //  Marges autour de l'affichage (en % de largeur totale)
        this.displayMarginCoeffs = displayMarginCoeffs;
    }

    public void setDotRightMarginCoeff(int dotRightMarginCoeff) {   //  Marge droite pour chaque carré (en % de largeur d'un carré)
        this.dotRightMarginCoeff = dotRightMarginCoeff;
    }

    public void fillRectOn(Rect rect) {
        fillRect(rect, onColor);
    }

    public void fillRectOff(Rect rect) {
        fillRect(rect, offColor);
    }

    public void setDotOn(int x, int y) {
        grid[y][x] = onColor;
    }

    public void setDotOff(int x, int y) {
        grid[y][x] = offColor;
    }

    public void setOnColor(String onColor) {
        this.onColor = Color.parseColor(COLOR_PREFIX + onColor);
    }

    public void setOffColor(String offColor) {
        this.offColor = Color.parseColor(COLOR_PREFIX + offColor);
    }

    public void setBackColor(String backColor) {
        this.backColor = Color.parseColor(COLOR_PREFIX + backColor);
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void scrollLeft() {
        scrollStart.x = scrollStart.x + 1;
        if (scrollStart.x >= scrollRect.right) {
            scrollStart.x = scrollRect.left;
        }
    }

    public void scrollRight() {
        scrollStart.x = scrollStart.x - 1;
        if (scrollStart.x < scrollRect.left) {
            scrollStart.x = scrollRect.right - 1;
        }
    }

    public void scrollTop() {
        scrollStart.y = scrollStart.y + 1;
        if (scrollStart.y >= scrollRect.bottom) {
            scrollStart.y = scrollRect.top;
        }
    }

    public void scrollBottom() {
        scrollStart.y = scrollStart.y - 1;
        if (scrollStart.y < scrollRect.top) {
            scrollStart.y = scrollRect.bottom - 1;
        }
    }

    public void noScroll() {
        scrollStart.set(scrollRect.left, scrollRect.top);
    }

    public void writeText(String text, DotMatrixFont dotMatrixFont) {
        writeText(text, null, dotMatrixFont);
    }

    public void writeText(String text, DotMatrixFont extraFont, DotMatrixFont defaultFont) {   //  A partir de symbolPos; Spécifier extraFont différent de null si text mélange extraFont et defaultFont; extraFont a la priorité sur defaultFont
        DotMatrixSymbol symbol;

        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            Character ch = text.charAt(i);
            symbol = null;
            if (extraFont != null) {
                symbol = extraFont.getSymbol(ch);
            }
            if (symbol == null) {
                symbol = defaultFont.getSymbol(ch);
            }
            drawSymbol(symbol);
        }
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    public void updateDisplay() {
        invalidate();
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawing = true;
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        for (int i = 0; i <= (displayRect.width() - 1); i = i + 1) {
            int gridX = displayRect.left + i;
            if ((gridX >= scrollRect.left) && (gridX <= (scrollRect.right - 1))) {  //  On est dans une zone éventuellement en cours de scroll
                gridX = gridX + scrollStart.x - scrollRect.left;
                if (gridX >= scrollRect.right) {
                    gridX = gridX - scrollRect.width();
                }
            }
            for (int j = 0; j <= (displayRect.height() - 1); j = j + 1) {
                int gridY = displayRect.top + j;
                if ((gridY >= scrollRect.top) && (gridY <= (scrollRect.bottom - 1))) {   //  On est dans une zone éventuellement en cours de scroll
                    gridY = gridY + scrollStart.y - scrollRect.top;
                    if (gridY >= scrollRect.bottom) {
                        gridY = gridY - scrollRect.height();
                    }
                }
                int unpressedColor = grid[gridY][gridX];
                int pressedColor = unpressedColor ^ COLOR_CONTRASTER;
                dotPaint.setColor((buttonState.equals(BUTTON_STATES.PRESSED)) ? pressedColor : unpressedColor);
                dotPoint.set(gridMargins.left + (float) gridStartX + (float) i * dotCellSize, gridMargins.top + (float) j * dotCellSize);
                viewCanvas.drawRect(dotPoint.x, dotPoint.y, dotPoint.x + dotSize, dotPoint.y + dotSize, dotPaint);
            }
        }
        viewCanvasBackPaint.setColor(backColor);
        viewCanvas.drawRoundRect(viewCanvasRect, backCornerRadius, backCornerRadius, viewCanvasBackPaint);
        canvas.drawBitmap(viewBitmap, 0, 0, null);
        drawing = false;
    }

    private void drawSymbol(DotMatrixSymbol symbol) {   //  A partir de symbolPos
        int[][] symbolData = symbol.getData();
        symbolPos.offset(symbol.getPosInitialOffset().x, symbol.getPosInitialOffset().y);   //  Appliquer un décalage avant l'affichage du symbole
        for (int i = 0; i <= (symbol.getWidth() - 1); i = i + 1) {
            int gridX = symbolPos.x + i;
            for (int j = 0; j <= (symbol.getHeight() - 1); j = j + 1) {
                int gridY = symbolPos.y + j;
                if (symbolData[j][i] == 1) {
                    grid[gridY][gridX] = onColor;
                }
            }
        }
        symbolPos.offset(symbol.getPosFinalOffset().x, symbol.getPosFinalOffset().y);   //  Prêt pour l'affichage du symbole suivant
    }

    private void fillRect(Rect rect, int value) {
        for (int i = rect.left; i <= (rect.right - 1); i = i + 1) {
            for (int j = rect.top; j <= (rect.bottom - 1); j = j + 1) {
                grid[j][i] = value;
            }
        }
    }

    private void calcInternalDimensions(int viewWidth) {  // Ajustement à un entier pour éviter le dessin d'une grille irrrégulière dans la largeur ou hauteur de ses éléments
        gridMargins = new RectF((int) ((float) viewWidth * displayMarginCoeffs.left + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.top + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.right + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.bottom + 0.5f));
        dotCellSize = (int) (((float) viewWidth - (gridMargins.left + gridMargins.right)) / (float) displayRect.width());
        dotSize = (int) (dotCellSize / (1 + dotRightMarginCoeff) + 0.5f);
        gridStartX = (int) (((float) viewWidth - (gridMargins.left + (float) displayRect.width() * dotCellSize + gridMargins.right)) / 2 + 0.5f);
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
