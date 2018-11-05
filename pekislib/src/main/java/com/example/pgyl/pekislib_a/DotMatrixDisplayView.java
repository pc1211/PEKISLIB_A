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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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
    private enum SYMBOLS {  //  En matrice 5x7 ou autre
        ZERO('0', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 1, 0, 1}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ONE('1', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
        TWO('2', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 1, 1}}),
        THREE('3', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        FOUR('4', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 1, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}}),
        FIVE('5', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        SIX('6', new int[][]{{0, 0, 1, 1, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        SEVEN('7', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
        EIGHT('8', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        NINE('9', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
        DOT('.', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 1}}),
        DOUBLE_DOT(':', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}});

        private Character valueChar;
        private int[][] valueData;
        private static Map<Character, SYMBOLS> map;

        SYMBOLS(Character valueChar, int[][] valueData) {
            this.valueChar = valueChar;
            this.valueData = valueData;
        }

        public int[][] DATA() {
            return valueData;
        }

        public static SYMBOLS getSymbolByChar(Character ch) {
            if (map == null) {
                map = new HashMap<Character, SYMBOLS>();
                for (SYMBOLS symbol : SYMBOLS.values()) {
                    map.put(symbol.valueChar, symbol);
                }
            }
            return map.get(ch);
        }
    }

    //endregion
    //region Variables
    private EnumMap<SYMBOLS, Point> symbolPosInitialOffsetsMap;
    private EnumMap<SYMBOLS, Point> symbolPosFinalOffsetsMap;
    private int[][] grid;
    private int gridWidth;
    private int gridHeight;
    private RectF gridMarginCoeffs;
    private RectF gridMargins;
    private int gridStartX;
    private int symbolHeight;
    private int symbolWidth;
    private int symbolRightMargin;
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
        final Point SYMBOL_POS_DEFAULT = new Point(0, 0);  //  Position du prochain symbole à afficher (en coordonnées de la grille (x,y), (0,0) étant le carré en haut à gauche)
        final int SYMBOL_RIGHT_MARGIN_DEFAULT = 0;
        final float DOT_RIGHT_MARGIN_COEFF_DEFAULT = 0.2f;       //  Distance entre carrés (% de largeur d'un carré)

        symbolPosInitialOffsetsMap = new EnumMap<SYMBOLS, Point>(SYMBOLS.class);
        symbolPosFinalOffsetsMap = new EnumMap<SYMBOLS, Point>(SYMBOLS.class);
        gridMarginCoeffs = GRID_MARGIN_SIZE_COEFFS_DEFAULT;
        symbolWidth = SYMBOLS.ZERO.DATA()[0].length;
        symbolHeight = SYMBOLS.ZERO.DATA().length;
        symbolRightMargin = SYMBOL_RIGHT_MARGIN_DEFAULT;    // Distance entre symboles (en nombre de carrés)
        symbolPos = SYMBOL_POS_DEFAULT;
        dotRightMarginCoeff = DOT_RIGHT_MARGIN_COEFF_DEFAULT;
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

        symbolPosInitialOffsetsMap.clear();
        symbolPosInitialOffsetsMap = null;
        symbolPosFinalOffsetsMap.clear();
        symbolPosFinalOffsetsMap = null;
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
        int h = (int) (gridMargins.top + dotCellSize * ((float) gridHeight - 1) + dotSize + gridMargins.bottom + 0.5f);
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

    public void setGridDimensions(int gridWidth, int gridHeight) {   //  Largeur et hauteur de la grille (en nombre de carrés)
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        grid = new int[gridHeight][gridWidth];
        fillGridOff();
    }

    public void drawText(int x, int y, String text) {
        symbolPos.set(x, y);
        for (int j = 0; j <= (text.length() - 1); j = j + 1) {
            drawSymbol(SYMBOLS.getSymbolByChar(text.charAt(j)));
        }
    }

    public void fillGridOn() {
        fillRect(0, 0, gridWidth, gridHeight, 1);
    }

    public void fillGridOff() {
        fillRect(0, 0, gridWidth, gridHeight, 0);
    }

    public void fillRectOn(int x, int y, int width, int height) {
        fillRect(x, y, width, height, 1);
    }

    public void fillRectOff(int x, int y, int width, int height) {
        fillRect(x, y, width, height, 0);
    }

    public void setDotOn(int x, int y) {
        grid[y][x] = 1;
    }

    public void setDotOff(int x, int y) {
        grid[y][x] = 0;
    }

    public void setSymbolRightMargin(int symbolRightMargin) {   //  Marge droite pour chaque symbole (en nombre de carrés)
        this.symbolRightMargin = symbolRightMargin;
    }

    public void setGridMarginCoeffs(RectF gridMarginCoeffs) {   //  Marges autour de la grille (en % de largeur totale)
        this.gridMarginCoeffs = gridMarginCoeffs;
    }

    public void setDotRightMarginCoeff(int dotRightMarginCoeff) {   //  Marge droite pour chaque carré (en % de largeur d'un carré)
        this.dotRightMarginCoeff = dotRightMarginCoeff;
    }

    public void setAllSymbolCompressionsOn() {
        for (SYMBOLS symbol : SYMBOLS.values()) {
            setSymbolCompressionOnBySymbol(symbol);
        }
    }

    public void setAllSymbolCompressionsOff() {
        for (SYMBOLS symbol : SYMBOLS.values()) {
            setSymbolCompressionOffBySymbol(symbol);
        }
    }

    public void setSymbolCompressionOn(Character ch) {
        setSymbolCompressionOnBySymbol(SYMBOLS.getSymbolByChar(ch));
    }

    public void setSymbolCompressionOff(Character ch) {
        setSymbolCompressionOffBySymbol(SYMBOLS.getSymbolByChar(ch));
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

    public int getSymbolWidth() {   // Nombre de colonnes d'un symbole (en nombre de carrés)
        return symbolWidth;
    }

    public int getSymbolHeight() {   // Nombre de lignes d'un symbole (en nombre de carrés)
        return symbolHeight;
    }

    public boolean isDrawing() {
        return drawing;
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
        int fi;
        int bi;
        int ai;
        int colorIndex;

        super.onDraw(canvas);

        drawing = true;
        if (buttonState.equals(BUTTON_STATES.PRESSED)) {
            fi = backColorIndex;
            bi = frontColorIndex;
            ai = frontColorIndex;
        } else {
            fi = frontColorIndex;
            bi = backColorIndex;
            ai = alternateColorIndex;
        }
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        for (int i = 0; i <= (gridHeight - 1); i = i + 1) {
            for (int j = 0; j <= (gridWidth - 1); j = j + 1) {
                if (grid[i][j] == 1) {
                    colorIndex = fi;
                } else {
                    colorIndex = bi;
                }
                dotPaint.setColor(Color.parseColor(COLOR_PREFIX + colors[colorIndex]));
                dotPoint.set(gridMargins.left + (float) gridStartX + (float) j * dotCellSize, gridMargins.top + (float) i * dotCellSize);
                viewCanvas.drawRect(dotPoint.x, dotPoint.y, dotPoint.x + dotSize, dotPoint.y + dotSize, dotPaint);
            }
        }
        viewCanvasBackPaint.setColor(Color.parseColor(COLOR_PREFIX + colors[ai]));
        viewCanvas.drawRoundRect(viewCanvasRect, backCornerRadius, backCornerRadius, viewCanvasBackPaint);
        canvas.drawBitmap(viewBitmap, 0, 0, null);
        drawing = false;
    }

    private void drawSymbol(SYMBOLS symbol) {
        Point symbolPosInitialOffset = symbolPosInitialOffsetsMap.get(symbol);
        int[][] data = symbol.DATA();
        symbolPos.set(symbolPos.x + symbolPosInitialOffset.x, symbolPos.y + symbolPosInitialOffset.y);  //  Appliquer un décalage avant l'affichage du symbole
        for (int i = 0; i <= (symbolHeight - 1); i = i + 1) {
            for (int j = 0; j <= (symbolWidth - 1); j = j + 1) {
                if (data[i][j] == 1) {
                    grid[symbolPos.y + i][symbolPos.x + j] = 1;
                }
            }
        }
        Point symbolPosFinalOffset = symbolPosFinalOffsetsMap.get(symbol);
        symbolPos.set(symbolPos.x + symbolPosFinalOffset.x, symbolPos.y + symbolPosFinalOffset.y);  //  Prêt pour l'affichage du symbole suivant
    }

    private void calculateDimensions(int viewWidth) {  // Ajustement à un entier pour éviter le dessin d'une grille irrrégulière dans la largeur ou hauteur de ses éléments
        gridMargins = new RectF((int) ((float) viewWidth * gridMarginCoeffs.left + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.top + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.right + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.bottom + 0.5f));
        dotCellSize = (int) (((float) viewWidth - (gridMargins.left + gridMargins.right)) / (float) gridWidth);
        dotSize = (int) (dotCellSize / (1 + dotRightMarginCoeff) + 0.5f);
        gridStartX = (int) (((float) viewWidth - (gridMargins.left + (float) gridWidth * dotCellSize + gridMargins.right)) / 2 + 0.5f);
    }

    private void fillRect(int x, int y, int width, int height, int value) {
        for (int i = y; i <= (y + height - 1); i = i + 1) {
            for (int j = x; j <= (x + width - 1); j = j + 1) {
                grid[i][j] = value;
            }
        }
    }

    private void setSymbolCompressionOnBySymbol(SYMBOLS symbol) {
        if ((symbol.equals(SYMBOLS.DOT)) || (symbol.equals(SYMBOLS.DOUBLE_DOT))) {
            if (symbol.equals(SYMBOLS.DOT)) {
                setDotCompressionOn();
            }
            if (symbol.equals(SYMBOLS.DOUBLE_DOT)) {
                setDoubleDotCompressionOn();
            }
        } else {
            setSymbolCompressionOffBySymbol(symbol);
        }
    }

    private void setSymbolCompressionOffBySymbol(SYMBOLS symbol) {
        symbolPosInitialOffsetsMap.put(symbol, new Point(0, 0));
        symbolPosFinalOffsetsMap.put(symbol, new Point(symbolWidth + symbolRightMargin, 0));  //  Se déplacer d'une position vers la droite, marge droite comprise
    }

    private void setDotCompressionOn() {
        int dx = -symbolWidth + 1 - symbolRightMargin;
        int dy = 1;
        symbolPosInitialOffsetsMap.put(SYMBOLS.DOT, new Point(dx, dy));  //  Pour afficher '.' dans la marge droite du symbole précédent, sur une ligne supplémentaire
        dx = -dx;
        dy = -dy;
        symbolPosFinalOffsetsMap.put(SYMBOLS.DOT, new Point(dx, dy));    //  Pour l'affichage du prochain symbole
    }

    private void setDoubleDotCompressionOn() {
        int dx = -symbolWidth / 2;
        int dy = 0;
        symbolPosInitialOffsetsMap.put(SYMBOLS.DOUBLE_DOT, new Point(dx, dy));   //  Pour afficher ':' dans la 1e colonne disponible après le symbole précédent, avec une marge droite
        dx = -dx + 1 + symbolRightMargin;
        dy = -dy;
        symbolPosFinalOffsetsMap.put(SYMBOLS.DOUBLE_DOT, new Point(dx, dy));     //  Pour l'affichage du prochain symbole
    }

}
