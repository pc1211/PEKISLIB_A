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

public final class DotMatrixDisplayView extends View {  //  Affichage de caractères dans une grille de carrés avec coordonnées (x,y)  ((0,0) étant en haut à gauche de la grille)
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    //region Constantes
    final int ON_VALUE = 1;
    final int OFF_VALUE = 0;
    //endregion
    //region Variables
    private DotMatrixFont defaultFont;
    private int[][] grid;
    private RectF gridMarginCoeffs;
    private RectF gridMargins;
    private int gridStartX;
    private Rect displayRect;
    private Rect extendedRect;
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
    private String[] colors;
    private BUTTON_STATES buttonState;
    private boolean clickDownInButtonZone;
    private Rect buttonZone;
    private int frontColorIndex;
    private int backColorIndex;
    private int alternateColorIndex;
    //endregion

    public DotMatrixDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final RectF GRID_MARGIN_SIZE_COEFFS_DEFAULT = new RectF(0.02f, 0.02f, 0.02f, 0.02f);   //  Marge autour de la grille (% de largeur totale)
        final float GRID_DOT_RIGHT_MARGIN_COEFF_DEFAULT = 0.2f;   //  Distance entre carrés (% de largeur d'un carré)
        final Point DEFAULT_FONT_SYMBOL_POS_DEFAULT = new Point(0, 0);   //  Position du prochain symbole à afficher (en coordonnées de la grille (x,y), (0,0) étant le carré en haut à gauche)

        gridMarginCoeffs = GRID_MARGIN_SIZE_COEFFS_DEFAULT;
        dotRightMarginCoeff = GRID_DOT_RIGHT_MARGIN_COEFF_DEFAULT;
        symbolPos = DEFAULT_FONT_SYMBOL_POS_DEFAULT;
        setupDefaultFont();
        dotPoint = new PointF();
        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        viewCanvasBackPaint = new Paint();
        viewCanvasBackPaint.setAntiAlias(true);
        viewCanvasBackPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        drawing = false;
        buttonState = BUTTON_STATES.UNPRESSED;
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

        defaultFont.close();
        defaultFont = null;
        grid = null;
        viewCanvasBackPaint = null;
        dotPaint = null;
        viewCanvas = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mw = MeasureSpec.getMode(widthMeasureSpec);
        int wm = MeasureSpec.getSize(widthMeasureSpec);
        int mh = MeasureSpec.getMode(heightMeasureSpec);
        int hm = MeasureSpec.getSize(heightMeasureSpec);

        int ws = wm;   // Largeur souhaitée = Largeur proposée

        calculateDimensions(wm);
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

        calculateDimensions(w);
        viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        viewCanvasRect = new RectF(0, 0, w, h);
        backCornerRadius = (Math.min(w, h) * BACK_CORNER_RADIUS) / 200;
    }

    public void setGridDimensions(Rect displayRect, Rect extendedRect) {   //  Largeur et hauteur de la grille affichée et au total(en nombre de carrés)
        this.displayRect = displayRect;
        this.extendedRect = extendedRect;
        grid = new int[extendedRect.height() + 1][extendedRect.width() + 1];  //  +1 ligne et colonne pour permettre stockage temporaire lors d'un scroll
        fillRectOff(extendedRect);
    }

    public void displayText(int x, int y, String text, DotMatrixFont dotMatrixFont) {
        symbolPos.set(x, y);
        appendText(text, dotMatrixFont);
    }

    public void appendText(String text, DotMatrixFont dotMatrixFont) {   //  A partir de symbolPos
        DotMatrixSymbol symbol;

        for (int i = 0; i <= (text.length() - 1); i = i + 1) {
            Character ch = text.charAt(i);
            symbol = dotMatrixFont.getSymbol(ch);
            if (symbol == null) {
                symbol = defaultFont.getSymbol(ch);
            }
            drawSymbol(symbol);    //  Afficher symbole à partir de SymbolPos
        }
        symbol = null;
    }

    public Point getSymbolPos() {
        return symbolPos;
    }

    public void setSymbolPos(int x, int y) {
        symbolPos.set(x, y);
    }

    public DotMatrixFont getDefautFont() {
        return defaultFont;
    }

    public void setGridMarginCoeffs(RectF gridMarginCoeffs) {   //  Marges autour de la grille (en % de largeur totale)
        this.gridMarginCoeffs = gridMarginCoeffs;
    }

    public void setDotRightMarginCoeff(int dotRightMarginCoeff) {   //  Marge droite pour chaque carré (en % de largeur d'un carré)
        this.dotRightMarginCoeff = dotRightMarginCoeff;
    }

    public void fillRectOn(Rect rect) {
        fillRect(rect, ON_VALUE);
    }

    public void fillRectOff(Rect rect) {
        fillRect(rect, OFF_VALUE);
    }

    public void setDotOn(int x, int y) {
        grid[y][x] = ON_VALUE;
    }

    public void setDotOff(int x, int y) {
        grid[y][x] = OFF_VALUE;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public void setFrontColorIndex(int frontColIndex) {
        this.frontColorIndex = frontColIndex;
    }

    public void setBackColorIndex(int backColIndex) {
        this.backColorIndex = backColIndex;
    }

    public void setAlternateColorIndex(int colorIndex) {   //  Index à utiliser si bouton pressé => Back/Alternate
        alternateColorIndex = colorIndex;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void scrollLeft(Rect scrollRect) {
        for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
            grid[j][extendedRect.width()] = grid[j][extendedRect.left];    //  Stockage temporaire dans la colonne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
                grid[j][i] = grid[j][i + 1];
            }
        }
        for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
            grid[j][scrollRect.right] = grid[j][extendedRect.width()];
        }
    }

    public void scrollRight(Rect scrollRect) {
        for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
            grid[j][extendedRect.width()] = grid[j][extendedRect.right];    //  Stockage temporaire dans la colonne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
                grid[j][i] = grid[j][i - 1];
            }
        }
        for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
            grid[j][scrollRect.left] = grid[j][extendedRect.width()];
        }
    }

    public void scrollTop(Rect scrollRect) {
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            grid[extendedRect.height()][i] = grid[extendedRect.top][i];    //  Stockage temporaire dans la ligne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
                grid[j][i] = grid[j + 1][i];
            }
        }
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            grid[scrollRect.bottom][i] = grid[extendedRect.height()][i];
        }
    }

    public void scrollBottom(Rect scrollRect) {
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            grid[extendedRect.height()][i] = grid[extendedRect.bottom][i];    //  Stockage temporaire dans la ligne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom - 1; j = j + 1) {
                grid[j][i] = grid[j - 1][i];
            }
        }
        for (int i = scrollRect.left; i <= scrollRect.right - 1; i = i + 1) {
            grid[scrollRect.top][i] = grid[extendedRect.height()][i];
        }
    }

    private boolean onButtonTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clickDownInButtonZone = true;
            buttonState = BUTTON_STATES.PRESSED;
            invalidate();
        }
        if ((action == MotionEvent.ACTION_MOVE) || (action == MotionEvent.ACTION_UP)) {
            if (clickDownInButtonZone) {
                if (buttonZone == null) {
                    buttonZone = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
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

        drawing = true;
        int frontStateColorIndex = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? backColorIndex : frontColorIndex);
        int backStateColorIndex = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? frontColorIndex : backColorIndex);
        int alternateStateColorIndex = ((buttonState.equals(BUTTON_STATES.PRESSED)) ? frontColorIndex : alternateColorIndex);
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        for (int i = 0; i <= (displayRect.width() - 1); i = i + 1) {
            for (int j = 0; j <= (displayRect.height() - 1); j = j + 1) {
                int dotColorIndex = ((grid[j][i] == ON_VALUE) ? frontStateColorIndex : backStateColorIndex);
                dotPaint.setColor(Color.parseColor(COLOR_PREFIX + colors[dotColorIndex]));
                dotPoint.set(gridMargins.left + (float) gridStartX + (float) i * dotCellSize, gridMargins.top + (float) j * dotCellSize);
                viewCanvas.drawRect(dotPoint.x, dotPoint.y, dotPoint.x + dotSize, dotPoint.y + dotSize, dotPaint);
            }
        }
        viewCanvasBackPaint.setColor(Color.parseColor(COLOR_PREFIX + colors[alternateStateColorIndex]));
        viewCanvas.drawRoundRect(viewCanvasRect, backCornerRadius, backCornerRadius, viewCanvasBackPaint);
        canvas.drawBitmap(viewBitmap, 0, 0, null);
        drawing = false;
    }

    private void drawSymbol(DotMatrixSymbol symbol) {
        int[][] symbolData = symbol.getData();
        symbolPos.x = symbolPos.x + symbol.getPosInitialOffset().x;   //  Appliquer un décalage avant l'affichage du symbole
        symbolPos.y = symbolPos.y + symbol.getPosInitialOffset().y;
        for (int i = 0; i <= (symbol.getWidth() - 1); i = i + 1) {
            int symbolDotX = symbolPos.x + i;
            for (int j = 0; j <= (symbol.getHeight() - 1); j = j + 1) {
                int symbolDotY = symbolPos.y + j;
                if (symbolData[j][i] == ON_VALUE) {
                    grid[symbolDotY][symbolDotX] = ON_VALUE;
                }
            }
        }
        symbolPos.x = symbolPos.x + symbol.getPosFinalOffset().x;   //  Prêt pour l'affichage du symbole suivant
        symbolPos.y = symbolPos.y + symbol.getPosFinalOffset().y;
    }

    private void fillRect(Rect rect, int value) {
        for (int i = rect.left; i <= rect.right; i = i + 1) {
            for (int j = rect.top; j <= rect.bottom; j = j + 1) {
                grid[j][i] = value;
            }
        }
    }

    private void calculateDimensions(int viewWidth) {  // Ajustement à un entier pour éviter le dessin d'une grille irrrégulière dans la largeur ou hauteur de ses éléments
        gridMargins = new RectF((int) ((float) viewWidth * gridMarginCoeffs.left + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.top + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.right + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.bottom + 0.5f));
        dotCellSize = (int) (((float) viewWidth - (gridMargins.left + gridMargins.right)) / (float) displayRect.width());
        dotSize = (int) (dotCellSize / (1 + dotRightMarginCoeff) + 0.5f);
        gridStartX = (int) (((float) viewWidth - (gridMargins.left + (float) displayRect.width() * dotCellSize + gridMargins.right)) / 2 + 0.5f);
    }

    private void setupDefaultFont() {
        final DotMatrixSymbol[] DEFAULT_FONT_SYMBOLS = {  //  En matrice 5x7 ou autre
                new DotMatrixSymbol(' ', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('!', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('\'', new int[][]{{0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('#', new int[][]{{0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}}),
                new DotMatrixSymbol('$', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 1, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 1, 0, 1}, {1, 1, 1, 1, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('%', new int[][]{{1, 1, 0, 0, 0}, {1, 1, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 1, 1}, {0, 0, 0, 1, 1}}),
                new DotMatrixSymbol('\"', new int[][]{{0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('(', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol(')', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('*', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {1, 0, 1, 0, 1}, {0, 1, 1, 1, 0}, {1, 0, 1, 0, 1}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('+', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol(',', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('-', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('.', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol('/', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('0', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 1, 0, 1}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('1', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('2', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('3', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('4', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 1, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('5', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('6', new int[][]{{0, 0, 1, 1, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('7', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('8', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('9', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol(':', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol(';', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('<', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('=', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('>', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('?', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('@', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('A', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('B', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
                new DotMatrixSymbol('C', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('D', new int[][]{{1, 1, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 0}, {1, 1, 1, 0, 0}}),
                new DotMatrixSymbol('E', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('F', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
                new DotMatrixSymbol('G', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
                new DotMatrixSymbol('H', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('I', new int[][]{{0, 1, 1, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('J', new int[][]{{0, 0, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol('K', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 1, 0}, {1, 0, 1, 0, 0}, {1, 1, 0, 0, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('L', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('M', new int[][]{{1, 0, 0, 0, 1}, {1, 1, 0, 1, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('N', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('O', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('P', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
                new DotMatrixSymbol('Q', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 1}}),
                new DotMatrixSymbol('R', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('S', new int[][]{{0, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
                new DotMatrixSymbol('T', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('U', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('V', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('W', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 0, 1, 0}}),
                new DotMatrixSymbol('X', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('Y', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('Z', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('[', new int[][]{{0, 1, 1, 1, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('\\', new int[][]{{0, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol(']', new int[][]{{0, 1, 1, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('^', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('_', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('`', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('a', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
                new DotMatrixSymbol('b', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
                new DotMatrixSymbol('c', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('d', new int[][]{{0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
                new DotMatrixSymbol('e', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('f', new int[][]{{0, 0, 1, 1, 0}, {0, 1, 0, 0, 1}, {0, 1, 0, 0, 0}, {1, 1, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('g', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('h', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('i', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('j', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
                new DotMatrixSymbol('k', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 1, 0, 0}, {1, 1, 0, 0, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}}),
                new DotMatrixSymbol('l', new int[][]{{0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('m', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 0, 1, 0}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('n', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('o', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('p', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
                new DotMatrixSymbol('q', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 1}, {1, 0, 0, 1, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}}),
                new DotMatrixSymbol('r', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
                new DotMatrixSymbol('s', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
                new DotMatrixSymbol('t', new int[][]{{0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 1}, {0, 0, 1, 1, 0}}),
                new DotMatrixSymbol('u', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 1}, {0, 1, 1, 0, 1}}),
                new DotMatrixSymbol('v', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('w', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 0, 1, 0}}),
                new DotMatrixSymbol('x', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}}),
                new DotMatrixSymbol('y', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
                new DotMatrixSymbol('z', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 1, 1}}),
                new DotMatrixSymbol('{', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
                new DotMatrixSymbol('|', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
                new DotMatrixSymbol('}', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
                new DotMatrixSymbol('~', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
                new DotMatrixSymbol('⌂', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}})
        };

        final int DEFAULT_FONT_RIGHT_MARGIN = 1;        //  1 colonne vide à droite de chaque symbole

        defaultFont = new DotMatrixFont();
        defaultFont.setSymbols(DEFAULT_FONT_SYMBOLS);
        defaultFont.setRightMargin(DEFAULT_FONT_RIGHT_MARGIN);
    }

}
