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
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;

public final class DotMatrixDisplayView extends View {  //  Affichage de caractères dans une grille de carrés avec coordonnées (x,y)  ((0,0) étant en haut à gauche de la grille)
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    private class StateColors {
        int pressed;
        int unpressed;
    }

    private class DimensionsSet {
        int width;
        int height;
        RectF margins;
        float dotCellSize;
        float dotSize;
        int gridStartX;

        DimensionsSet() {
            margins = new RectF();
        }
    }

    public enum SCROLL_DIRECTIONS {LEFT, RIGHT, TOP, BOTTOM}

    //region Variables
    private StateColors[][] colorValues;
    private RectF marginCoeffs;
    private DimensionsSet dimensionsSet;
    private DimensionsSet dimensionsSetTemp;
    private BiDimensions maxDimensions;
    private Rect gridRect;
    private Rect displayRect;
    private Rect scrollRect;
    private Point scrollOffset;
    private Point symbolPos;
    private float interDotDistanceCoeff;
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
    private boolean invertOn;
    private boolean clickDownInButtonZone;
    private Rect buttonZone;
    //endregion

    public DotMatrixDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final RectF MARGIN_SIZE_COEFFS_DEFAULT = new RectF(0.02f, 0.02f, 0.02f, 0.02f);   //  Marge autour de la grille (% de largeur totale)
        final float INTER_DOT_DISTANCE_COEFF_DEFAULT = 0.2f;   //  Distance entre carrés (% de largeur d'un carré)
        final Point SYMBOL_POS_DEFAULT = new Point(0, 0);   //  Position du prochain symbole à afficher (en coordonnées de la grille (x,y), (0,0) étant le carré en haut à gauche)
        final Point SCROLL_OFFSET_DEFAULT = new Point(0, 0);   //  Décalage à partir de scrollRect (la partie de la grille qui est à scroller)
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click
        final String BACK_COLOR_DEFAULT = "000000";   //  Couleur du fond sur lequel repose la grille

        marginCoeffs = MARGIN_SIZE_COEFFS_DEFAULT;
        interDotDistanceCoeff = INTER_DOT_DISTANCE_COEFF_DEFAULT;
        symbolPos = SYMBOL_POS_DEFAULT;
        scrollOffset = SCROLL_OFFSET_DEFAULT;
        dimensionsSet = new DimensionsSet();
        dimensionsSetTemp = new DimensionsSet();
        maxDimensions = new BiDimensions(0, 0);
        dotPoint = new PointF();
        drawing = false;
        buttonState = BUTTON_STATES.UNPRESSED;
        invertOn = false;
        minClickTimeInterval = MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE;
        lastClickUpTime = 0;

        setupDotPaint();
        setupViewCanvasBackPaint();
        setBackColor(BACK_COLOR_DEFAULT);
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

        dimensionsSet = null;
        dimensionsSetTemp = null;
        maxDimensions = null;
        colorValues = null;
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

        BiDimensions maxDimensions = getMaxDimensions(wm, hm);
        int ws = maxDimensions.width;
        int hs = maxDimensions.height;
        setupDimensions(dimensionsSet, ws);

        int w = ws;
        if (mw == MeasureSpec.EXACTLY) {
            w = wm;
        }
        if (mw == MeasureSpec.AT_MOST) {
            w = Math.min(ws, wm);
        }
        int h = hs;
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

        setupDimensions(dimensionsSet, getMaxDimensions(w, h).width);
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
        for (int i = 0; i <= (displayRect.width() - 1); i = i + 1) {
            int gridX = displayRect.left + i;
            if ((gridX >= scrollRect.left) && (gridX <= (scrollRect.right - 1))) {  //  On est dans une zone éventuellement en cours de scroll
                gridX = gridX + scrollOffset.x;
                if (gridX >= scrollRect.right) {
                    gridX = gridX - scrollRect.width();
                }
            }
            for (int j = 0; j <= (displayRect.height() - 1); j = j + 1) {
                int gridY = displayRect.top + j;
                if ((gridY >= scrollRect.top) && (gridY <= (scrollRect.bottom - 1))) {   //  On est dans une zone éventuellement en cours de scroll
                    gridY = gridY + scrollOffset.y;
                    if (gridY >= scrollRect.bottom) {
                        gridY = gridY - scrollRect.height();
                    }
                }
                dotPaint.setColor(((buttonState.equals(BUTTON_STATES.PRESSED)) ^ invertOn) ? colorValues[gridY][gridX].pressed : colorValues[gridY][gridX].unpressed);
                dotPoint.set(dimensionsSet.margins.left + (float) dimensionsSet.gridStartX + (float) i * dimensionsSet.dotCellSize, dimensionsSet.margins.top + (float) j * dimensionsSet.dotCellSize);
                viewCanvas.drawRect(dotPoint.x, dotPoint.y, dotPoint.x + dimensionsSet.dotSize, dotPoint.y + dimensionsSet.dotSize, dotPaint);
            }
        }
        viewCanvas.drawRoundRect(viewCanvasRect, backCornerRadius, backCornerRadius, viewCanvasBackPaint);
        canvas.drawBitmap(viewBitmap, 0, 0, null);
        drawing = false;
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
        colorValues = new StateColors[gridRect.height()][gridRect.width()];
        for (int i = 0; i <= (gridRect.width() - 1); i = i + 1) {
            for (int j = 0; j <= (gridRect.height() - 1); j = j + 1) {
                colorValues[j][i] = new StateColors();
            }
        }
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

    public void setScrollRect(Rect scrollRect) {   //   Zone à scroller (partie de la grille gridRect)
        this.scrollRect = scrollRect;
        resetScrollOffset();
    }

    public Rect getScrollRect() {
        return scrollRect;
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

    public void setMarginCoeffs(RectF marginCoeffs) {   //  Marges autour de l'affichage (en % de largeur totale)
        this.marginCoeffs = marginCoeffs;
    }

    public void setInterDotDistanceCoeff(int interDotDistanceCoeff) {   //  Distance entre chaque carré (en % de largeur d'un carré)
        this.interDotDistanceCoeff = interDotDistanceCoeff;
    }

    public void setBackColor(String color) {
        viewCanvasBackPaint.setColor(Color.parseColor(COLOR_PREFIX + color));
    }

    public void invert() {
        this.invertOn = !invertOn;
    }   //  Inverser les couleurs lors du onDraw; Peut être appelé plusieurs fois de suite avec le bon intervalle de temps pour créer un effet de flash

    public void setInvertOn(boolean invertOn) {
        this.invertOn = !invertOn;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void scroll(SCROLL_DIRECTIONS scrollDirection) {
        switch (scrollDirection) {
            case LEFT:
                scrollLeft();
                break;
            case TOP:
                scrollTop();
                break;
            case RIGHT:
                scrollRight();
                break;
            case BOTTOM:
                scrollBottom();
                break;
        }
    }

    public void scrollLeft() {
        scrollOffset.x = scrollOffset.x + 1;
        if (scrollOffset.x >= scrollRect.width()) {
            scrollOffset.x = 0;
        }
    }

    public void scrollRight() {
        scrollOffset.x = scrollOffset.x - 1;
        if (scrollOffset.x < 0) {
            scrollOffset.x = scrollRect.width() - 1;
        }
    }

    public void scrollTop() {
        scrollOffset.y = scrollOffset.y + 1;
        if (scrollOffset.y >= scrollRect.height()) {
            scrollOffset.y = 0;
        }
    }

    public void scrollBottom() {
        scrollOffset.y = scrollOffset.y - 1;
        if (scrollOffset.y < 0) {
            scrollOffset.y = scrollRect.height() - 1;
        }
    }

    public void resetScrollOffset() {
        scrollOffset.set(0, 0);
    }

    public void setMinClickTimeInterval(long minClickTimeInterval) {
        this.minClickTimeInterval = minClickTimeInterval;
    }

    public void updateDisplay() {
        invalidate();
    }

    public void setDot(int x, int y, String pressedColor, String unpressedColor) {
        colorValues[y][x].pressed = Color.parseColor(COLOR_PREFIX + pressedColor);
        colorValues[y][x].unpressed = Color.parseColor(COLOR_PREFIX + unpressedColor);
    }

    public void fillRect(Rect rect, String pressedColor, String unpressedColor) {
        int pressedColValue = Color.parseColor(COLOR_PREFIX + pressedColor);
        int unpressedColValue = Color.parseColor(COLOR_PREFIX + unpressedColor);
        for (int i = rect.left; i <= (rect.right - 1); i = i + 1) {
            for (int j = rect.top; j <= (rect.bottom - 1); j = j + 1) {
                colorValues[j][i].pressed = pressedColValue;
                colorValues[j][i].unpressed = unpressedColValue;
            }
        }
    }

    public void writeText(String text, String onColor, DotMatrixFont dotMatrixFont) {
        writeText(text, onColor, null, dotMatrixFont);
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

    private void drawSymbol(DotMatrixSymbol symbol, int ColValue) {   //  A partir de symbolPos; Seuls les points correspondant au traçage du symbole (valeurs 1) sont remplacés
        int[][] symbolData = symbol.getData();
        for (int i = 0; i <= (symbol.getDimensions().width - 1); i = i + 1) {
            int gridX = symbolPos.x + i;
            for (int j = 0; j <= (symbol.getDimensions().height - 1); j = j + 1) {
                int gridY = symbolPos.y + j;
                if (symbolData[j][i] == 1) {   //  Valeur 1 => Point à remplacer dans la grille
                    colorValues[gridY][gridX].pressed = colorValues[gridY][gridX].unpressed;
                    colorValues[gridY][gridX].unpressed = ColValue;
                }
            }
        }
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

    private void setupDimensions(DimensionsSet gridDimensions, int viewWidth) {  // Ajustement à un entier pour éviter le dessin d'une grille irrégulière dans la largeur ou hauteur de ses éléments
        gridDimensions.width = viewWidth;
        gridDimensions.margins.set(getMarginSize(gridDimensions.width, marginCoeffs.left), getMarginSize(gridDimensions.width, marginCoeffs.top), getMarginSize(gridDimensions.width, marginCoeffs.right), getMarginSize(gridDimensions.width, marginCoeffs.bottom));
        gridDimensions.dotCellSize = (int) (((float) viewWidth - (gridDimensions.margins.left + gridDimensions.margins.right)) / (float) displayRect.width());
        gridDimensions.dotSize = (int) (gridDimensions.dotCellSize / (1 + interDotDistanceCoeff) + 0.5f);
        gridDimensions.gridStartX = (int) (((float) viewWidth - (gridDimensions.margins.left + (float) displayRect.width() * gridDimensions.dotCellSize + gridDimensions.margins.right)) / 2 + 0.5f);
        gridDimensions.height = (int) (dimensionsSetTemp.margins.top + dimensionsSetTemp.dotCellSize * ((float) displayRect.height() - 1) + dimensionsSetTemp.dotSize + dimensionsSetTemp.margins.bottom + 0.5f);
    }

    private int getMarginSize(int length, float marginCoeff) {
        return (int) ((float) length * marginCoeff + 0.5f);
    }

    private BiDimensions getMaxDimensions(int proposedWidth, int proposedHeight) {
        int xTop = proposedWidth;
        int hMax = proposedHeight;
        int xMin = 0;
        int xCand = 0;
        int yCand = 0;
        int oldX = 0;
        int x = xTop;
        do {
            setupDimensions(dimensionsSetTemp, x);
            int y = dimensionsSetTemp.height;
            if (y <= hMax) {
                xCand = x;   //  On a un nouveau candidat !
                yCand = y;
                if ((x == xTop) || (y == hMax)) {   //  Parfait !
                    break;
                }
                xMin = x;   //  Examiner maintenant l'intervalle [x,xTop]
            } else {   //  y > hMax
                xTop = x;    // Examiner maintenant l'intervalle [xMin,x]
            }
            oldX = x;
            x = (xMin + xTop) / 2;
        } while (x != oldX);   //  Si x=oldX, on ne progresse plus => Accepter le dernier candidat

        maxDimensions.width = xCand;
        maxDimensions.height = yCand;
        return maxDimensions;
    }

}
