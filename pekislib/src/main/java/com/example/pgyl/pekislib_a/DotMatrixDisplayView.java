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

public final class DotMatrixDisplayView extends View {  //  Affichage de caractères dans une grille de carrés avec coordonnées (x,y)  ((0,0) étant en haut à gauche de la grille)
    public interface onCustomClickListener {
        void onCustomClick();
    }

    public void setOnCustomClickListener(onCustomClickListener listener) {
        mOnCustomClickListener = listener;
    }

    private onCustomClickListener mOnCustomClickListener;

    public enum DOT_FORM {SQUARE, ROUND}   //  points carrés ou ronds  (Pour obtenir des points de la forme souhaitée, un overlay (bitmap) "troué" sera posé sur des carrés)

    public enum SCROLL_DIRECTIONS {LEFT, RIGHT, TOP, BOTTOM}

    private class StateColor {
        int pressed;
        int unpressed;
    }

    private class DimensionsSet {
        int width;               //  Largeur donnée pour l'affichage
        Rect internalMargins;    //  Marge autour de l'affichage proprement dit
        int dotCellSize;         //  Taille d'un carré + Espace entre 2 carrés, à calculer selon le nombre de carrés en largeur (cf displayRect)
        int dotSize;             //  Taille d'un carré (dotCellSize / (1 + Coefficient de taille de l'espace entre carrés))
        int slackWidth;          //  Compense les arrondis (dûs au calcul de dotCellSize et dotSize), de telle sorte que internalMargins.left + (displayRect.width -1) * dotCellSize + dotSize + internalMargins.right +slackWidth = width
        int height;              //  internalMargins.top + (displayRect.width -1) * dotCellSize + dotSize + internalMargins.bottom

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
    private DOT_FORM dotForm;
    private Paint dotPaint;
    private PointF dotCellOrigin;
    private boolean drawing;
    private Bitmap viewBitmap;
    private Canvas viewCanvas;
    private Bitmap dotFormOverlayBitmap;
    private Paint dotFormOverlayPaint;
    private Paint dotFormOverlayTransparentPaint;
    private Paint dotFormOverlayOpaquePaint;
    private Rect canvasRect;
    private RectF dotMatrixRect;
    private float backCornerRadius;
    private int backCornerRadiusPercent;
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
        final DOT_FORM DOT_FORM_DEFAULT = DOT_FORM.SQUARE;   //  Points carrés par défaut
        final long MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE = 0;   //   Intervalle de temps (ms) minimum imposé entre 2 click
        final int BACK_CORNER_RADIUS_PERCENT_DEFAULT = 35;     //  % appliqué à 1/2 largeur ou hauteur pour déterminer le rayon du coin arrondi
        final Point SYMBOL_POS_DEFAULT = new Point(0, 0);   //  Position du prochain symbole à afficher (en coordonnées de la grille (x,y), (0,0) étant le carré en haut à gauche)
        final Point SCROLL_OFFSET_DEFAULT = new Point(0, 0);   //  Décalage à partir de scrollRect (la partie de la grille qui est à scroller)
        final String BACK_COLOR_DEFAULT = "000000";   //  Couleur du fond sur lequel repose la grille
        final BUTTON_STATES BUTTON_STATES_DEFAULT = BUTTON_STATES.UNPRESSED;

        setExternalMarginCoeffs(EXTERNAL_MARGIN_COEFFS_DEFAULT);
        setInternalMarginCoeffs(INTERNAL_MARGIN_COEFFS_DEFAULT);
        setDotSpacingCoeff(String.valueOf(DOT_SPACING_COEFF_DEFAULT));
        setDotForm(DOT_FORM_DEFAULT);
        setMinClickTimeInterval(MIN_CLICK_TIME_INTERVAL_DEFAULT_VALUE);
        backCornerRadiusPercent = BACK_CORNER_RADIUS_PERCENT_DEFAULT;
        symbolPos = SYMBOL_POS_DEFAULT;
        scrollOffset = SCROLL_OFFSET_DEFAULT;
        buttonState = BUTTON_STATES_DEFAULT;
        scrollRect = null;
        dimensionsSet = new DimensionsSet();
        dimensionsSetTemp = new DimensionsSet();
        maxDimensions = new BiDimensions(0, 0);
        dotCellOrigin = new PointF();
        drawing = false;
        invertOn = false;
        lastClickUpTime = 0;

        setupDotPaint();
        setupDotFormOverlayPaint();
        setupDotFormOverlayOpaquePaint();
        setupDotFormOverlayTransparentPaint();
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
        dotFormOverlayBitmap.recycle();
        dotFormOverlayBitmap = null;
        dotFormOverlayPaint = null;
        dotFormOverlayOpaquePaint = null;
        dotFormOverlayTransparentPaint = null;
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

        drawing = true;
        viewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        viewCanvas.drawBitmap(dotFormOverlayBitmap, displayRect.left, displayRect.top, dotFormOverlayPaint);  //  Overlay posé sur les carrés pour leur donner la forme de point souhaitée
        dotCellOrigin.x = dotMatrixRect.left + dimensionsSet.internalMargins.left;   //  Coordonnée x du 1er carré d'une ligne
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
            dotCellOrigin.y = dotMatrixRect.top + dimensionsSet.internalMargins.top;   //  Coordonnée y du 1er carré d'une colonne
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
                viewCanvas.drawRect(dotCellOrigin.x, dotCellOrigin.y, dotCellOrigin.x + dimensionsSet.dotSize, dotCellOrigin.y + dimensionsSet.dotSize, dotPaint);   //  Le carré
                dotCellOrigin.y = dotCellOrigin.y + dimensionsSet.dotCellSize;   //  Passer au prochain point de la colonne
            }
            dotCellOrigin.x = dotCellOrigin.x + dimensionsSet.dotCellSize;   //  Passer au prochain point de la ligne
        }
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

    public void setDotForm(DOT_FORM dotForm) {
        this.dotForm = dotForm;     //  SQUARE ou ROUND
    }

    public void setDotSpacingCoeff(String dotSpacingCoeff) {   //  Taille de l'espace entre chaque carré (en % de largeur d'un carré)
        this.dotSpacingCoeff = Float.parseFloat(dotSpacingCoeff) / 100;
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

    private void setupDotFormOverlayPaint() {
        dotFormOverlayPaint = new Paint();
        dotFormOverlayPaint.setAntiAlias(true);
        dotFormOverlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private void setupDotFormOverlayOpaquePaint() {
        dotFormOverlayOpaquePaint = new Paint();
        dotFormOverlayOpaquePaint.setAntiAlias(true);
        dotFormOverlayOpaquePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private void setupDotFormOverlayTransparentPaint() {
        dotFormOverlayTransparentPaint = new Paint();
        dotFormOverlayTransparentPaint.setAntiAlias(true);
        dotFormOverlayTransparentPaint.setColor(Color.TRANSPARENT);
        dotFormOverlayTransparentPaint.setStyle(Paint.Style.FILL);
        dotFormOverlayTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void setupDrawParameters() {
        if (canvasRect != null) {
            dotMatrixRect = getSubRect(canvasRect, dimensionsSet.width - dimensionsSet.slackWidth, dimensionsSet.height, externalMarginCoeffs);  //  internalMargins incluses
            backCornerRadius = (Math.min(dotMatrixRect.width(), dotMatrixRect.height()) * backCornerRadiusPercent) / 200;
            createDotFormOverlayBitmap();
        }
    }

    private void setupDimensions(DimensionsSet dimensionsSet, int viewWidth) {  //  Ajustement à un entier pour éviter le dessin d'une grille irrégulière dans la largeur ou hauteur de ses éléments
        final int SLACKWIDTH_TOL_COEFF = 20;   //  Tolérance d'excès de la largeur utilisée par rapport à la largeur disponible (en % du total des marges gauche et droite)

        dimensionsSet.width = viewWidth;
        dimensionsSet.internalMargins.set(getMarginSize(dimensionsSet.width, internalMarginCoeffs.left), getMarginSize(dimensionsSet.width, internalMarginCoeffs.top), getMarginSize(dimensionsSet.width, internalMarginCoeffs.right), getMarginSize(dimensionsSet.width, internalMarginCoeffs.bottom));
        dimensionsSet.dotCellSize = getDotCellSize(dimensionsSet);
        dimensionsSet.dotSize = getDotSize(dimensionsSet);
        if (getSlackWidth(dimensionsSet) < -(SLACKWIDTH_TOL_COEFF * (dimensionsSet.internalMargins.left + dimensionsSet.internalMargins.right) / 100)) {   //  Les calculs de dotCellSize et dotSize ont été trop optimistes
            dimensionsSet.dotCellSize = dimensionsSet.dotCellSize - 1;
            dimensionsSet.dotSize = getDotSize(dimensionsSet);
        }
        dimensionsSet.slackWidth = getSlackWidth(dimensionsSet);
        dimensionsSet.height = dimensionsSet.internalMargins.top + (displayRect.height() - 1) * dimensionsSet.dotCellSize + dimensionsSet.dotSize + dimensionsSet.internalMargins.bottom;
    }

    private int getMarginSize(int length, float marginCoeff) {
        return (int) (length * marginCoeff + 0.5f);
    }

    private int getDotCellSize(DimensionsSet dimensionsSet) {  //  Solution de l'équation telle que internalMargins.left + (displayRect.width -1) * dotCellSize + dotSize + internalMargins.right = width (si pas d'arrondis)
        return (int) ((dimensionsSet.width - dimensionsSet.internalMargins.left - dimensionsSet.internalMargins.right) * (1 + dotSpacingCoeff) / (displayRect.width() * (1 + dotSpacingCoeff) - dotSpacingCoeff) + .5f);
    }

    private int getDotSize(DimensionsSet dimensionsSet) {
        return (int) (dimensionsSet.dotCellSize / (1 + dotSpacingCoeff) + .5f);
    }

    private int getSlackWidth(DimensionsSet dimensionsSet) {
        return dimensionsSet.width - (dimensionsSet.internalMargins.left + (displayRect.width() - 1) * dimensionsSet.dotCellSize + dimensionsSet.dotSize + dimensionsSet.internalMargins.right);
    }

    private void createDotFormOverlayBitmap() {   //  Créer un pochoir avec des formes transparentes sur fond de backColor, qui sera posée sur les carrés pour leur donner la forme de point désirée
        if (dotFormOverlayBitmap != null) {
            dotFormOverlayBitmap.recycle();
        }
        dotFormOverlayBitmap = Bitmap.createBitmap((int) dotMatrixRect.width(), (int) dotMatrixRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dotFormOverlayBitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);   //  Maintenant on va faire des trous dedans :)
        dotFormOverlayOpaquePaint.setColor(backColor);
        canvas.drawRoundRect(dotMatrixRect, backCornerRadius, backCornerRadius, dotFormOverlayOpaquePaint);

        dotCellOrigin.x = dotMatrixRect.left + dimensionsSet.internalMargins.left;   //  Coordonnée x du 1er carré d'une ligne
        for (int i = 0; i <= (displayRect.width() - 1); i = i + 1) {   //  Parcourir la ligne
            dotCellOrigin.y = dotMatrixRect.top + dimensionsSet.internalMargins.top;   //  Coordonnée y du 1er carré d'une colonne
            for (int j = 0; j <= (displayRect.height() - 1); j = j + 1) {   //  Parcourir la colonne
                if (dotForm.equals(DOT_FORM.SQUARE)) {   //  Point carré
                    canvas.drawRect(dotCellOrigin.x, dotCellOrigin.y, dotCellOrigin.x + dimensionsSet.dotSize, dotCellOrigin.y + dimensionsSet.dotSize, dotFormOverlayTransparentPaint);  //  Carré transparent
                } else {  //  Point rond
                    float d = dimensionsSet.dotSize * .5f;
                    canvas.drawCircle(dotCellOrigin.x + d, dotCellOrigin.y + d, d, dotFormOverlayTransparentPaint);  //  Disque transparent
                }
                dotCellOrigin.y = dotCellOrigin.y + dimensionsSet.dotCellSize;   //  Passer au prochain point de la colonne
            }
            dotCellOrigin.x = dotCellOrigin.x + dimensionsSet.dotCellSize;   //  Passer au prochain point de la ligne
        }
    }

    private BiDimensions getMaxDimensions(int proposedWidth, int proposedHeight) {  // Trouver les dimensions maximum d'un rectangle pouvant afficher displayRect dans un rectangle de dimensions données
        int xTop = proposedWidth;
        int hMax = proposedHeight;
        int xMin = 0;
        int xBest = 0;
        int yBest = 0;
        int oldX = 0;
        int x = xTop;
        do {
            setupDimensions(dimensionsSetTemp, x);
            int y = dimensionsSetTemp.height;
            if (y <= hMax) {
                xBest = x;   //  On a un nouveau candidat !
                yBest = y;
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

        maxDimensions.set(xBest, yBest);
        return maxDimensions;
    }

}
