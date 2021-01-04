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
import static com.example.pgyl.pekislib_a.PointRectUtils.ALIGN_WIDTH_HEIGHT;
import static com.example.pgyl.pekislib_a.PointRectUtils.getSubRect;

public final class DotMatrixDisplayView extends View {  //  Affichage de caractères dans une grille de points avec coordonnées (x,y)  ((0,0) étant en haut à gauche de la grille)
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    public enum SCROLL_DIRECTIONS {LEFT, RIGHT, TOP, BOTTOM}

    private class StateColor {
        int pressed;
        int unpressed;
    }

    private class DimensionsSet {
        int width;               //  Largeur donnée pour l'affichage
        Rect internalMargins;    //  Marge autour de l'affichage proprement dit
        int dotCellSideSize;         //  Taille d'un carré + Espace entre 2 carrés, à calculer selon le nombre de carrés en largeur (cf displayRect)
        int dotSideSize;             //  Taille d'un carré (dotCellSideSize / (1 + Coefficient de taille de l'espace entre carrés))
        int slackWidth;          //  Compense les arrondis (dûs au calcul de dotCellSize et dotSize), de telle sorte que internalMargins.left + (displayRect.width -1) * dotCellSideSize + dotSideSize + internalMargins.right +slackWidth = width
        int height;              //  internalMargins.top + (displayRect.width -1) * dotCellSideSize + dotSideSize + internalMargins.bottom

        DimensionsSet() {
            internalMargins = new Rect();
        }
    }

    //region Variables
    private StateColor[][] gridStateColors;
    private RectF internalMarginCoeffs;
    private RectF externalMarginCoeffs;
    private DimensionsSet dimensionsSet;
    private DimensionsSet dimensionsSetTemp;
    private BiDimensions maxDimensions;
    private Rect gridRect;
    private Rect displayRect;
    private Rect scrollRect;
    private Point scrollOffset;
    private Point symbolPos;
    private float dotSpacingCoeff;
    private float dotCornerRadius;
    private float dotCornerRadiusCoeff;
    private PointF dotCellOrigin;
    private boolean inDrawing;
    private Canvas viewCanvas;
    private Bitmap viewBitmap;
    private Bitmap dotFormStencilBitmap;
    private Paint dotPaint;
    private Paint dotFormStencilPaint;
    private Paint dotFormStencilTransparentPaint;
    private Rect canvasRect;
    private RectF dotMatrixRect;
    private RectF dotRect;
    private float backCornerRadius;
    private float backCornerRadiusCoeff;
    private int backColor;
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
        final RectF INTERNAL_MARGIN_COEFFS_DEFAULT = new RectF(0.02f, 0.02f, 0.02f, 0.02f);   //  Marge autour de l'affichage proprement dit (% de largeur)
        final RectF EXTERNAL_MARGIN_COEFFS_DEFAULT = ALIGN_WIDTH_HEIGHT;   //  Positionnement par défaut de la grille dans le container parent
        final int DOT_SPACING_COEFF_DEFAULT = 20;    //  Taille de l'espace entre carrés (% de largeur d'un carré)
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Intervalle de temps (ms) minimum imposé entre 2 click
        final int BACK_CORNER_RADIUS_PERCENT_DEFAULT = 35;     //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi
        final int DOT_CORNER_RADIUS_COEFF_DEFAULT = 0;   //  (Points carrés par défaut)
        final Point SYMBOL_POS_DEFAULT = new Point(0, 0);   //  Position du prochain symbole à afficher (en coordonnées de la grille (x,y), (0,0) étant le carré en haut à gauche)
        final Point SCROLL_OFFSET_DEFAULT = new Point(0, 0);   //  Décalage à partir de scrollRect (la partie de la grille qui est à scroller)
        final String BACK_COLOR_DEFAULT = "000000";   //  Couleur du fond sur lequel repose la grille
        final BUTTON_STATES BUTTON_STATES_DEFAULT = BUTTON_STATES.UNPRESSED;

        setExternalMarginCoeffs(EXTERNAL_MARGIN_COEFFS_DEFAULT);
        setInternalMarginCoeffs(INTERNAL_MARGIN_COEFFS_DEFAULT);
        setDotSpacingCoeff(String.valueOf(DOT_SPACING_COEFF_DEFAULT));
        setDotCornerRadiusCoeff(String.valueOf(DOT_CORNER_RADIUS_COEFF_DEFAULT));
        backCornerRadiusCoeff = BACK_CORNER_RADIUS_PERCENT_DEFAULT * .01f;
        setMinClickTimeInterval(MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE);
        symbolPos = SYMBOL_POS_DEFAULT;
        scrollOffset = SCROLL_OFFSET_DEFAULT;
        buttonState = BUTTON_STATES_DEFAULT;
        scrollRect = null;
        dotRect = new RectF();
        dimensionsSet = new DimensionsSet();
        dimensionsSetTemp = new DimensionsSet();
        maxDimensions = new BiDimensions(0, 0);
        dotCellOrigin = new PointF();
        inDrawing = false;
        invertOn = false;
        lastClickUpTime = 0;

        setupDotPaint();
        setupDotFormStencilPaint();
        setupDotFormStencilTransparentPaint();
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
        gridStateColors = null;
        dotPaint = null;
        dotFormStencilTransparentPaint = null;
        dotFormStencilPaint = null;
        dotFormStencilBitmap.recycle();
        dotFormStencilBitmap = null;
        viewBitmap.recycle();
        viewBitmap = null;
        viewCanvas = null;
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
        super.onSizeChanged(w, h, oldw, oldh);

        viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        canvasRect = new Rect(0, 0, w, h);
        setupDimensions(dimensionsSet, getMaxDimensions(w, h).width);
        setupDrawParameters();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        inDrawing = true;
        viewCanvas.drawBitmap(dotFormStencilBitmap, displayRect.left, displayRect.top, dotFormStencilPaint);  //  Pochoir pour obtenir la forme de point souhaitée
        dotCellOrigin.x = dotMatrixRect.left + dimensionsSet.internalMargins.left;   //  Coordonnée x du 1er point d'une ligne
        for (int i = 0; i <= (displayRect.width() - 1); i = i + 1) {   //  Parcourir la ligne
            int gridX = displayRect.left + i;
            if (scrollRect != null) {
                if ((gridX >= scrollRect.left) && (gridX <= (scrollRect.right - 1))) {  //  On est dans une zone éventuellement en cours de scroll
                    gridX = gridX + scrollOffset.x;
                    if (gridX >= scrollRect.right) {
                        gridX = gridX - scrollRect.width();
                    }
                }
            }
            dotCellOrigin.y = dotMatrixRect.top + dimensionsSet.internalMargins.top;   //  Coordonnée y du 1er point d'une colonne
            for (int j = 0; j <= (displayRect.height() - 1); j = j + 1) {   //  Parcourir la colonne
                int gridY = displayRect.top + j;
                if (scrollRect != null) {
                    if ((gridY >= scrollRect.top) && (gridY <= (scrollRect.bottom - 1))) {   //  On est dans une zone éventuellement en cours de scroll
                        gridY = gridY + scrollOffset.y;
                        if (gridY >= scrollRect.bottom) {
                            gridY = gridY - scrollRect.height();
                        }
                    }
                }
                dotPaint.setColor(((buttonState.equals(BUTTON_STATES.PRESSED)) ^ invertOn) ? gridStateColors[gridY][gridX].pressed : gridStateColors[gridY][gridX].unpressed);
                viewCanvas.drawRect(dotCellOrigin.x, dotCellOrigin.y, dotCellOrigin.x + dimensionsSet.dotSideSize, dotCellOrigin.y + dimensionsSet.dotSideSize, dotPaint);   //  Dessiner un carré (dans ce qui reste comme espace pour lui dans le pochoir)
                dotCellOrigin.y = dotCellOrigin.y + dimensionsSet.dotCellSideSize;   //  Passer au prochain point de la colonne
            }
            dotCellOrigin.x = dotCellOrigin.x + dimensionsSet.dotCellSideSize;   //  Passer au prochain point de la ligne
        }
        canvas.drawBitmap(viewBitmap, 0, 0, null);
        inDrawing = false;
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

    public void setGridRect(Rect gridRect) {   //  Grille (carrés) sous-jacente de stockage des valeurs affichées  (left=0, top=0, right=width, bottom=height)
        this.gridRect = gridRect;
        gridStateColors = new StateColor[gridRect.height()][gridRect.width()];
        for (int i = 0; i <= (gridRect.width() - 1); i = i + 1) {
            for (int j = 0; j <= (gridRect.height() - 1); j = j + 1) {
                gridStateColors[j][i] = new StateColor();
            }
        }
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

    public Rect getGridRect() {
        return gridRect;
    }

    public void setDisplayRect(Rect displayRect) {   //  Emplacement de l'affichage (sous-rectangle de la grille gridRect) (left>=gridRect.left, top>=gridRect.top, right<=gridRect.right, bottom<=gridRect.bottom)
        this.displayRect = displayRect;
    }

    public void rebuildStructure() {
        setupDimensions(dimensionsSet, getWidth());
        setupDrawParameters();
    }

    public void setDotSpacingCoeff(String dotSpacingCoeff) {   //  Taille de l'espace entre chaque carré (en % de largeur d'un carré)
        this.dotSpacingCoeff = Float.parseFloat(dotSpacingCoeff) * .01f;
    }

    public void setDotCornerRadiusCoeff(String dotCornerRadiusCoeff) {
        this.dotCornerRadiusCoeff = Float.parseFloat(dotCornerRadiusCoeff) * .01f;
    }

    public void setInternalMarginCoeffs(RectF internalMarginCoeffs) {   //  Marges autour de l'affichage proprement dit (en % de largeur totale)
        this.internalMarginCoeffs = internalMarginCoeffs;
    }

    public void setExternalMarginCoeffs(RectF externalMarginCoeffs) {   //  Positionnement par rapport au parent
        this.externalMarginCoeffs = externalMarginCoeffs;
    }

    public void setBackColor(String color) {
        backColor = Color.parseColor(COLOR_PREFIX + color);
    }

    public void invert() {
        this.invertOn = !invertOn;
    }   //  Inverser les couleurs lors du onDraw; Peut être appelé plusieurs fois de suite avec la fréquence appropriée pour créer un effet de flash

    public void setInvertOn(boolean invertOn) {
        this.invertOn = !invertOn;
    }

    public boolean isDrawing() {
        return inDrawing;
    }

    public void scroll(SCROLL_DIRECTIONS scrollDirection, int dotNumber) {
        if (dotNumber > 0) {
            switch (scrollDirection) {
                case LEFT:
                    scrollLeft(dotNumber);
                    break;
                case TOP:
                    scrollTop(dotNumber);
                    break;
                case RIGHT:
                    scrollRight(dotNumber);
                    break;
                case BOTTOM:
                    scrollBottom(dotNumber);
                    break;
            }
        }
    }

    public void scrollLeft(int dotNumber) {
        scrollOffset.x = scrollOffset.x + dotNumber % scrollRect.width();
        if (scrollOffset.x >= scrollRect.width()) {
            scrollOffset.x = scrollOffset.x - scrollRect.width();
        }
    }

    public void scrollRight(int dotNumber) {
        scrollOffset.x = scrollOffset.x - dotNumber % scrollRect.width();
        if (scrollOffset.x < 0) {
            scrollOffset.x = scrollOffset.x + scrollRect.width();
        }
    }

    public void scrollTop(int dotNumber) {
        scrollOffset.y = scrollOffset.y + dotNumber % scrollRect.height();
        if (scrollOffset.y >= scrollRect.height()) {
            scrollOffset.y = scrollOffset.y - scrollRect.height();
        }
    }

    public void scrollBottom(int dotNumber) {
        scrollOffset.y = scrollOffset.y - dotNumber % scrollRect.height();
        if (scrollOffset.y < 0) {
            scrollOffset.y = scrollOffset.y + scrollRect.height();
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
        gridStateColors[y][x].pressed = Color.parseColor(COLOR_PREFIX + pressedColor);
        gridStateColors[y][x].unpressed = Color.parseColor(COLOR_PREFIX + unpressedColor);
    }

    public void fillRect(Rect rect, String pressedColor, String unpressedColor) {
        int pressedColValue = Color.parseColor(COLOR_PREFIX + pressedColor);
        int unpressedColValue = Color.parseColor(COLOR_PREFIX + unpressedColor);
        for (int i = rect.left; i <= (rect.right - 1); i = i + 1) {
            for (int j = rect.top; j <= (rect.bottom - 1); j = j + 1) {
                gridStateColors[j][i].pressed = pressedColValue;
                gridStateColors[j][i].unpressed = unpressedColValue;
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

    private void drawSymbol(DotMatrixSymbol symbol, int colValue) {   //  A partir de symbolPos; Seuls les points correspondant au traçage du symbole (valeurs 1) sont remplacés
        int[][] symbolData = symbol.getData();
        for (int i = 0; i <= (symbol.getDimensions().width - 1); i = i + 1) {
            int gridX = symbolPos.x + i;
            for (int j = 0; j <= (symbol.getDimensions().height - 1); j = j + 1) {
                int gridY = symbolPos.y + j;
                if (symbolData[j][i] == 1) {   //  Valeur 1 => Point à remplacer dans la grille
                    gridStateColors[gridY][gridX].pressed = gridStateColors[gridY][gridX].unpressed;
                    gridStateColors[gridY][gridX].unpressed = colValue;
                }
            }
        }
    }

    private void setupDotPaint() {
        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
    }

    private void setupDotFormStencilPaint() {
        dotFormStencilPaint = new Paint();
        dotFormStencilPaint.setAntiAlias(true);
        dotFormStencilPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private void setupDotFormStencilTransparentPaint() {
        dotFormStencilTransparentPaint = new Paint();
        dotFormStencilTransparentPaint.setAntiAlias(true);
        dotFormStencilTransparentPaint.setColor(Color.TRANSPARENT);
        dotFormStencilTransparentPaint.setStyle(Paint.Style.FILL);
        dotFormStencilTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void setupDrawParameters() {
        if (canvasRect != null) {
            dotMatrixRect = getSubRect(canvasRect, dimensionsSet.width - dimensionsSet.slackWidth, dimensionsSet.height, externalMarginCoeffs);  //  internalMargins incluses
            backCornerRadius = Math.min(dotMatrixRect.width(), dotMatrixRect.height()) * backCornerRadiusCoeff * .5f;
            dotCornerRadius = dimensionsSet.dotSideSize * dotCornerRadiusCoeff * .5f;
            createDotFormStencilBitmap();
        }
    }

    private void setupDimensions(DimensionsSet dimensionsSet, int viewWidth) {  //  Ajustement à un entier pour éviter le dessin d'une grille irrégulière dans la largeur ou hauteur de ses éléments
        final int SLACKWIDTH_TOL_COEFF = 20;   //  Tolérance d'excès de la largeur utilisée par rapport à la largeur disponible (en % du total des marges gauche et droite)

        dimensionsSet.width = viewWidth;
        dimensionsSet.internalMargins.set(getMarginSize(dimensionsSet.width, internalMarginCoeffs.left), getMarginSize(dimensionsSet.width, internalMarginCoeffs.top), getMarginSize(dimensionsSet.width, internalMarginCoeffs.right), getMarginSize(dimensionsSet.width, internalMarginCoeffs.bottom));
        dimensionsSet.dotCellSideSize = getDotCellSideSize(dimensionsSet);
        dimensionsSet.dotSideSize = getDotSideSize(dimensionsSet);
        if (getSlackWidth(dimensionsSet) < -(SLACKWIDTH_TOL_COEFF * (dimensionsSet.internalMargins.left + dimensionsSet.internalMargins.right) / 100)) {   //  Les calculs de dotCellSize et dotSize ont été trop optimistes
            dimensionsSet.dotCellSideSize = dimensionsSet.dotCellSideSize - 1;
            dimensionsSet.dotSideSize = getDotSideSize(dimensionsSet);
        }
        dimensionsSet.slackWidth = getSlackWidth(dimensionsSet);
        dimensionsSet.height = dimensionsSet.internalMargins.top + (displayRect.height() - 1) * dimensionsSet.dotCellSideSize + dimensionsSet.dotSideSize + dimensionsSet.internalMargins.bottom;
    }

    private int getMarginSize(int length, float marginCoeff) {
        return (int) (length * marginCoeff + 0.5f);
    }

    private int getDotCellSideSize(DimensionsSet dimensionsSet) {  //  Solution de l'équation telle que internalMargins.left + (displayRect.width -1) * dotCellSideSize + dotSideSize + internalMargins.right = width (si pas d'arrondis)
        return (int) ((dimensionsSet.width - dimensionsSet.internalMargins.left - dimensionsSet.internalMargins.right) * (1 + dotSpacingCoeff) / (displayRect.width() * (1 + dotSpacingCoeff) - dotSpacingCoeff) + .5f);
    }

    private int getDotSideSize(DimensionsSet dimensionsSet) {
        return (int) (dimensionsSet.dotCellSideSize / (1 + dotSpacingCoeff) + .5f);
    }

    private int getSlackWidth(DimensionsSet dimensionsSet) {
        return dimensionsSet.width - (dimensionsSet.internalMargins.left + (displayRect.width() - 1) * dimensionsSet.dotCellSideSize + dimensionsSet.dotSideSize + dimensionsSet.internalMargins.right);
    }

    private void createDotFormStencilBitmap() {   //  Préparer un pochoir avec des formes transparentes sur fond de backColor, pour donner aux futurs points la forme désirée
        if (dotFormStencilBitmap != null) {
            dotFormStencilBitmap.recycle();
        }
        dotFormStencilBitmap = Bitmap.createBitmap(canvasRect.width(), canvasRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dotFormStencilBitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        dotFormStencilPaint.setColor(backColor);
        canvas.drawRoundRect(dotMatrixRect, backCornerRadius, backCornerRadius, dotFormStencilPaint);   //  Maintenant on va faire des trous dedans :)

        dotCellOrigin.x = dotMatrixRect.left + dimensionsSet.internalMargins.left;   //  Coordonnée x du 1er point d'une ligne
        for (int i = 0; i <= (displayRect.width() - 1); i = i + 1) {   //  Parcourir la ligne
            dotCellOrigin.y = dotMatrixRect.top + dimensionsSet.internalMargins.top;   //  Coordonnée y du 1er point d'une colonne
            for (int j = 0; j <= (displayRect.height() - 1); j = j + 1) {   //  Parcourir la colonne
                dotRect.set(dotCellOrigin.x, dotCellOrigin.y, dotCellOrigin.x + dimensionsSet.dotSideSize, dotCellOrigin.y + dimensionsSet.dotSideSize);
                canvas.drawRoundRect(dotRect, dotCornerRadius, dotCornerRadius, dotFormStencilTransparentPaint);
                dotCellOrigin.y = dotCellOrigin.y + dimensionsSet.dotCellSideSize;   //  Passer au prochain point de la colonne
            }
            dotCellOrigin.x = dotCellOrigin.x + dimensionsSet.dotCellSideSize;   //  Passer au prochain point de la ligne
        }
    }

    private BiDimensions getMaxDimensions(int proposedWidth, int proposedHeight) {   //  Trouver les dimensions maximum d'un rectangle pouvant afficher la grille dans un rectangle de dimensions données
        int wMax = proposedWidth;
        int hMax = proposedHeight;
        int wMin = 0;
        int wBest = 0;
        int hBest = 0;
        int oldW = 0;
        int w = wMax;
        do {
            setupDimensions(dimensionsSetTemp, w);
            int h = dimensionsSetTemp.height;
            if (h <= hMax) {
                wBest = w;   //  On a un nouveau candidat !
                hBest = h;
                if ((w == wMax) || (h == hMax)) {   //  Parfait !
                    break;
                }
                wMin = w;   //  Examiner maintenant l'intervalle [w,wMax]
            } else {   //  h > hMax
                wMax = w;    // Examiner maintenant l'intervalle [wMin,w]
            }
            oldW = w;
            w = (wMin + wMax) / 2;
        } while (w != oldW);   //  Si w=oldW, on ne progresse plus => Accepter le dernier candidat

        maxDimensions.set(wBest, hBest);
        return maxDimensions;
    }

}
