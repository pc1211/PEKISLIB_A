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

    private class stateColors {
        int pressed;
        int unpressed;
    }

    public enum SCROLL_DIRECTIONS {LEFT, RIGHT, TOP, BOTTOM}

    //region Variables
    private stateColors[][] colorValues;
    private RectF displayMarginCoeffs;
    private RectF margins;
    private int gridStartX;
    private Rect gridRect;
    private Rect displayRect;
    private Rect scrollRect;
    private Point scrollOffset;
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
    private boolean invertOn;
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
        final Point SYMBOL_POS_DEFAULT = new Point(0, 0);   //  Position du prochain symbole à afficher (en coordonnées de la grille (x,y), (0,0) étant le carré en haut à gauche)
        final Point SCROLL_OFFSET_DEFAULT = new Point(0, 0);   //  Décalage à partir de scrollRect (la partie de la grille qui est à scroller)
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Interval de temps (ms) minimum imposé entre 2 click
        final String BACK_COLOR_DEFAULT = "000000";   //  Couleur du fond sur lequel repose la grille

        displayMarginCoeffs = DISPLAY_MARGIN_SIZE_COEFFS_DEFAULT;
        dotRightMarginCoeff = DISPLAY_DOT_RIGHT_MARGIN_COEFF_DEFAULT;
        symbolPos = SYMBOL_POS_DEFAULT;
        scrollOffset = SCROLL_OFFSET_DEFAULT;
        margins = new RectF();
        setupDotPaint();
        setupViewCanvasBackPaint();
        setBackColor(BACK_COLOR_DEFAULT);
        dotPoint = new PointF();
        drawing = false;
        buttonState = BUTTON_STATES.UNPRESSED;
        invertOn = false;
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

        setupDimensions(w);
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
                dotPoint.set(margins.left + (float) gridStartX + (float) i * dotCellSize, margins.top + (float) j * dotCellSize);
                viewCanvas.drawRect(dotPoint.x, dotPoint.y, dotPoint.x + dotSize, dotPoint.y + dotSize, dotPaint);
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
        colorValues = new stateColors[gridRect.height()][gridRect.width()];
        for (int i = 0; i <= (gridRect.width() - 1); i = i + 1) {
            for (int j = 0; j <= (gridRect.height() - 1); j = j + 1) {
                colorValues[j][i] = new stateColors();
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

    public void setDisplayMarginCoeffs(RectF displayMarginCoeffs) {   //  Marges autour de l'affichage (en % de largeur totale)
        this.displayMarginCoeffs = displayMarginCoeffs;
    }

    public void setDotRightMarginCoeff(int dotRightMarginCoeff) {   //  Marge droite pour chaque carré (en % de largeur d'un carré)
        this.dotRightMarginCoeff = dotRightMarginCoeff;
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

    private void setupDimensions(int viewWidth) {  // Ajustement à un entier pour éviter le dessin d'une grille irrrégulière dans la largeur ou hauteur de ses éléments
        margins.set((int) ((float) viewWidth * displayMarginCoeffs.left + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.top + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.right + 0.5f), (int) ((float) viewWidth * displayMarginCoeffs.bottom + 0.5f));
        dotCellSize = (int) (((float) viewWidth - (margins.left + margins.right)) / (float) displayRect.width());
        dotSize = (int) (dotCellSize / (1 + dotRightMarginCoeff) + 0.5f);
        gridStartX = (int) (((float) viewWidth - (margins.left + (float) displayRect.width() * dotCellSize + margins.right)) / 2 + 0.5f);
    }

    private int getHeightAfterSetupDimensions(int width) {
        setupDimensions(width);
        return (int) (margins.top + dotCellSize * ((float) displayRect.height() - 1) + dotSize + margins.bottom + 0.5f);
    }

    private BiDimensions getMaxDimensions(int proposedWidth, int proposedHeight) {
        double y;
        double old_y;

        double x = proposedWidth;
        y = getHeightAfterSetupDimensions((int) x);
        if (y > proposedHeight) {  //  Trop haut pour cette largeur => Trouver la largeur maximum (par la méthode de la sécante) telle que la hauteur correspondante ne dépasse pas proposedHeight
            double a = x * .75;     //  guess 1
            double b = a * .9;      //  guess 2
            double t = proposedHeight;
            x = a;
            double r = getHeightAfterSetupDimensions((int) x) - t;
            x = b;
            double s = getHeightAfterSetupDimensions((int) x) - t;
            do {
                old_y = y;
                double c = b - s * (b - a) / (s - r);
                x = c;
                y = getHeightAfterSetupDimensions((int) x) - t;
                b = a;
                s = r;
                a = c;
                r = y;
            } while (Math.abs(y - old_y) > 1);
        }
        return new BiDimensions((int) x, (int) (y + proposedHeight));
    }

}
