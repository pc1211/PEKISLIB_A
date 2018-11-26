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
    private enum DEFAULT_SYMBOLS_DATA {  //  En matrice 5x7 ou autre
        ASCII_20(' ', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_21('!', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}}),
        ASCII_22('\'', new int[][]{{0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_23('#', new int[][]{{0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}, {0, 1, 0, 1, 0}}),
        ASCII_24('$', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 1, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 1, 0, 1}, {1, 1, 1, 1, 0}, {0, 0, 1, 0, 0}}),
        ASCII_25('%', new int[][]{{1, 1, 0, 0, 0}, {1, 1, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 1, 1}, {0, 0, 0, 1, 1}}),
        ASCII_27('\"', new int[][]{{0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_28('(', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
        ASCII_29(')', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
        ASCII_2A('*', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {1, 0, 1, 0, 1}, {0, 1, 1, 1, 0}, {1, 0, 1, 0, 1}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_2B('+', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_2C(',', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
        ASCII_2D('-', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_2E('.', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}}),
        ASCII_2F('/', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_30('0', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 1, 0, 1}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_31('1', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
        ASCII_32('2', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 1, 1}}),
        ASCII_33('3', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_34('4', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 1, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}}),
        ASCII_35('5', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_36('6', new int[][]{{0, 0, 1, 1, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_37('7', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}}),
        ASCII_38('8', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_39('9', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
        ASCII_3A(':', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_3B(';', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
        ASCII_3C('<', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
        ASCII_3D('=', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_3E('>', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
        ASCII_3F('?', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}}),
        ASCII_40('@', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_41('A', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_42('B', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
        ASCII_43('C', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_44('D', new int[][]{{1, 1, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 0}, {1, 1, 1, 0, 0}}),
        ASCII_45('E', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
        ASCII_46('F', new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
        ASCII_47('G', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
        ASCII_48('H', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_49('I', new int[][]{{0, 1, 1, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
        ASCII_4A('J', new int[][]{{0, 0, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
        ASCII_4B('K', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 1, 0}, {1, 0, 1, 0, 0}, {1, 1, 0, 0, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}}),
        ASCII_4C('L', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
        ASCII_4D('M', new int[][]{{1, 0, 0, 0, 1}, {1, 1, 0, 1, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_4E('N', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_4F('O', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_50('P', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
        ASCII_51('Q', new int[][]{{0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 1}}),
        ASCII_52('R', new int[][]{{1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 0, 0, 1}}),
        ASCII_53('S', new int[][]{{0, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
        ASCII_54('T', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
        ASCII_55('U', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_56('V', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}}),
        ASCII_57('W', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 0, 1, 0}}),
        ASCII_58('X', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_59('Y', new int[][]{{1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
        ASCII_5A('Z', new int[][]{{1, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
        ASCII_5B('[', new int[][]{{0, 1, 1, 1, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 1, 1, 0}}),
        ASCII_5C('\\', new int[][]{{0, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 0}}),
        ASCII_5D(']', new int[][]{{0, 1, 1, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 1, 1, 1, 0}}),
        ASCII_5E('^', new int[][]{{0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_5F('_', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}}),
        ASCII_60('`', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_61('a', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
        ASCII_62('b', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
        ASCII_63('c', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_64('d', new int[][]{{0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}}),
        ASCII_65('e', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}}),
        ASCII_66('f', new int[][]{{0, 0, 1, 1, 0}, {0, 1, 0, 0, 1}, {0, 1, 0, 0, 0}, {1, 1, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}}),
        ASCII_67('g', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_68('h', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_69('i', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
        ASCII_6A('j', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {1, 0, 0, 1, 0}, {0, 1, 1, 0, 0}}),
        ASCII_6B('k', new int[][]{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 1, 0, 0}, {1, 1, 0, 0, 0}, {1, 0, 1, 0, 0}, {1, 0, 0, 1, 0}}),
        ASCII_6C('l', new int[][]{{0, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 1, 1, 0}}),
        ASCII_6D('m', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 0, 1, 0}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_6E('n', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}}),
        ASCII_6F('o', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_70('p', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
        ASCII_71('q', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 0, 1}, {1, 0, 0, 1, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 1}}),
        ASCII_72('r', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 1, 1, 0}, {1, 1, 0, 0, 1}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}}),
        ASCII_73('s', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {1, 0, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 1}, {1, 1, 1, 1, 0}}),
        ASCII_74('t', new int[][]{{0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 1, 0, 0, 1}, {0, 0, 1, 1, 0}}),
        ASCII_75('u', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 1, 1}, {0, 1, 1, 0, 1}}),
        ASCII_76('v', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}}),
        ASCII_77('w', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 1, 0, 1, 0}}),
        ASCII_78('x', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}}),
        ASCII_79('y', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {0, 1, 1, 1, 1}, {0, 0, 0, 0, 1}, {0, 1, 1, 1, 0}}),
        ASCII_7A('z', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1, 1, 1, 1, 1}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 1, 1}}),
        ASCII_7B('{', new int[][]{{0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}}),
        ASCII_7C('|', new int[][]{{0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}}),
        ASCII_7D('}', new int[][]{{0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 0, 0}}),
        ASCII_7E('~', new int[][]{{0, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {1, 0, 1, 0, 1}, {1, 0, 1, 0, 1}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}}),
        ASCII_7F('⌂', new int[][]{{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 1, 0, 1, 0}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}});

        private Character valueChar;
        private int[][] valueData;

        DEFAULT_SYMBOLS_DATA(Character valueChar, int[][] valueData) {
            this.valueChar = valueChar;
            this.valueData = valueData;
        }

        public int[][] DATA() {
            return valueData;
        }
    }

    final int ON_VALUE = 1;
    final int OFF_VALUE = 0;
    //endregion
    //region Variables
    private DotMatrixFont defaultFont;
    private int[][] grid;
    private Rect displayRect;
    private Rect totalRect;
    private RectF gridMarginCoeffs;
    private RectF gridMargins;
    private int gridStartX;
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
        final int DEFAULT_FONT_SYMBOL_RIGHT_MARGIN = 1;        //  1 colonne vide à droite de chaque symbole

        gridMarginCoeffs = GRID_MARGIN_SIZE_COEFFS_DEFAULT;
        dotRightMarginCoeff = GRID_DOT_RIGHT_MARGIN_COEFF_DEFAULT;
        symbolPos = DEFAULT_FONT_SYMBOL_POS_DEFAULT;
        defaultFont = new DotMatrixFont();
        for (DEFAULT_SYMBOLS_DATA defaultSymbolData : DEFAULT_SYMBOLS_DATA.values()) {
            defaultFont.addSymbol(defaultSymbolData.valueChar, defaultSymbolData.DATA());
        }
        defaultFont.setRightMargin(DEFAULT_FONT_SYMBOL_RIGHT_MARGIN);

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

    public void setGridDimensions(Rect displayRect, Rect totalRect) {   //  Largeur et hauteur de la grille affichée et au total(en nombre de carrés)
        this.displayRect = displayRect;
        this.totalRect = totalRect;
        grid = new int[totalRect.height() + 1][totalRect.width() + 1];  //  +1 ligne et colonne pour permettre stockage temporaire lors d'un scroll
        fillRectOff(totalRect);
    }

    public void displayText(int x, int y, String text, DotMatrixFont dotMatrixFont) {
        symbolPos.set(x, y);
        appendText(text, dotMatrixFont);
    }

    public void appendText(String text, DotMatrixFont dotMatrixFont) {   //  A partir de symbolPos
        DotMatrixSymbol symbol;

        for (int j = 0; j <= (text.length() - 1); j = j + 1) {
            Character ch = text.charAt(j);
            symbol = dotMatrixFont.getCharMap().get(ch);
            if (symbol == null) {
                symbol = defaultFont.getCharMap().get(ch);
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

    public Rect getDisplayRect() {
        return displayRect;
    }

    public Rect getTotalRect() {
        return totalRect;
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
        for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
            grid[j][totalRect.width()] = grid[j][totalRect.left];    //  Stockage temporaire dans la colonne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
                grid[j][i] = grid[j][i + 1];
            }
        }
        for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
            grid[j][scrollRect.right] = grid[j][totalRect.width()];
        }
    }

    public void scrollRight(Rect scrollRect) {
        for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
            grid[j][totalRect.width()] = grid[j][totalRect.right];    //  Stockage temporaire dans la colonne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
                grid[j][i] = grid[j][i - 1];
            }
        }
        for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
            grid[j][scrollRect.left] = grid[j][totalRect.width()];
        }
    }

    public void scrollTop(Rect scrollRect) {
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            grid[totalRect.height()][i] = grid[totalRect.top][i];    //  Stockage temporaire dans la ligne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
                grid[j][i] = grid[j + 1][i];
            }
        }
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            grid[scrollRect.bottom][i] = grid[totalRect.height()][i];
        }
    }

    public void scrollBottom(Rect scrollRect) {
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            grid[totalRect.height()][i] = grid[totalRect.bottom][i];    //  Stockage temporaire dans la ligne supplémentaire
        }
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            for (int j = scrollRect.top; j <= scrollRect.bottom; j = j + 1) {
                grid[j][i] = grid[j - 1][i];
            }
        }
        for (int i = scrollRect.left; i <= scrollRect.right; i = i + 1) {
            grid[scrollRect.top][i] = grid[totalRect.height()][i];
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
        symbolPos.set(symbolPos.x + symbol.getPosInitialOffset().x, symbolPos.y + symbol.getPosInitialOffset().y);  //  Appliquer un décalage avant l'affichage du symbole
        for (int i = 0; i <= (symbol.getWidth() - 1); i = i + 1) {
            int symbolDotX = symbolPos.x + i;
            for (int j = 0; j <= (symbol.getHeight() - 1); j = j + 1) {
                int symbolDotY = symbolPos.y + j;
                if ((symbolDotX <= (totalRect.width() - 1)) && (symbolDotY <= (totalRect.height() - 1))) {   //  Clip
                    if (symbolData[j][i] == ON_VALUE) {
                        grid[symbolDotY][symbolDotX] = ON_VALUE;
                    }
                }
            }
        }
        symbolPos.set(symbolPos.x + symbol.getPosFinalOffset().x, symbolPos.y + symbol.getPosFinalOffset().y);  //  Prêt pour l'affichage du symbole suivant
    }

    private void calculateDimensions(int viewWidth) {  // Ajustement à un entier pour éviter le dessin d'une grille irrrégulière dans la largeur ou hauteur de ses éléments
        gridMargins = new RectF((int) ((float) viewWidth * gridMarginCoeffs.left + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.top + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.right + 0.5f), (int) ((float) viewWidth * gridMarginCoeffs.bottom + 0.5f));
        dotCellSize = (int) (((float) viewWidth - (gridMargins.left + gridMargins.right)) / (float) displayRect.width());
        dotSize = (int) (dotCellSize / (1 + dotRightMarginCoeff) + 0.5f);
        gridStartX = (int) (((float) viewWidth - (gridMargins.left + (float) displayRect.width() * dotCellSize + gridMargins.right)) / 2 + 0.5f);
    }

    private void fillRect(Rect rect, int value) {
        for (int i = rect.left; i <= rect.right; i = i + 1) {
            for (int j = rect.top; j <= rect.bottom; j = j + 1) {
                grid[j][i] = value;
            }
        }
    }

}
