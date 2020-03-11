package com.example.pgyl.pekislib_a;

import android.graphics.PointF;
import android.graphics.RectF;

import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public class PointRectUtils {  //  Routines adaptées à des coordonnées (0,0) en haut à gauche de leur canvas

    public static class RectDimensions {
        public int width;
        public int height;

        RectDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static RectF getCircleBoundingRect(PointF centerPoint, float radius) {
        return new RectF(centerPoint.x - radius, centerPoint.y - radius, centerPoint.x + radius, centerPoint.y + radius);
    }

    public static PointF getPointInCircle(PointF centerPoint, float radius, float angle) {
        return new PointF(centerPoint.x + radius * (float) Math.cos(angle), centerPoint.y - radius * (float) Math.sin(angle));
    }

    //  getSubRect: Dans un rectangle donné, calculer le rectangle maximum pouvant contenir un objet en respectant son aspect ratio, en lui appliquant un coefficient de taille et en le positionnant à l'endroit souhaité
    //      Positionnement souhaité via relativePositionCoeffs:
    //          Centrer horizontalement et verticalement =>  (UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED)
    //          Aligner à gauche, Centrer verticalement  =>  (0, UNDEFINED, UNDEFINED, UNDEFINED)
    //          Aligner à gauche et en haut              =>  (0, 0, UNDEFINED, UNDEFINED)
    //          Aligner à droite et en bas               =>  (UNDEFINED, UNDEFINED, 1, 1)
    //          Tout occuper (perte aspectRatio)         =>  (0, 0, 1, 1)
    //          Aligner en bas et occuper tout la largeur (perte aspectRatio) =>  (0, UNDEFINED, 1, 1)
    //          ...
    public static RectF CENTER_X_Y = new RectF(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
    public static RectF ALIGN_RIGHT_BOTTOM = new RectF(UNDEFINED, UNDEFINED, 1, 1);
    public static float FULL_SIZE_COEFF = 1;
    public static float SQUARE_ASPECT_RATIO = 1;

    public static RectF getSubRect(RectF boundingRect, RectF relativePositionCoeffs, float aspectRatio, float sizeCoeff) {
        float subWidth = boundingRect.width();
        float subHeight = boundingRect.height();
        if (aspectRatio != UNDEFINED) {    //  Tenir compte de l'aspect ratio
            subHeight = subWidth * aspectRatio;
            if (subHeight > boundingRect.height()) {
                subHeight = boundingRect.height();
                subWidth = subHeight / aspectRatio;
            }
        }
        subWidth = subWidth * sizeCoeff;
        subHeight = subHeight * sizeCoeff;
        RectF ret = new RectF(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
        if (relativePositionCoeffs.left != UNDEFINED) {
            ret.left = boundingRect.left + boundingRect.width() * relativePositionCoeffs.left;
        }
        if (relativePositionCoeffs.top != UNDEFINED) {
            ret.top = boundingRect.top + boundingRect.height() * relativePositionCoeffs.top;
        }
        if (relativePositionCoeffs.right != UNDEFINED) {
            ret.right = boundingRect.left + boundingRect.width() * relativePositionCoeffs.right;
        }
        if (relativePositionCoeffs.bottom != UNDEFINED) {
            ret.bottom = boundingRect.top + boundingRect.height() * relativePositionCoeffs.bottom;
        }
        if ((ret.left == UNDEFINED) && (ret.right == UNDEFINED)) {
            ret.left = boundingRect.left + (boundingRect.width() - subWidth) / 2;   //  Rien de spécifié => Centrer horizontalement
            ret.right = ret.left + subWidth;
        } else {
            if (ret.left == UNDEFINED) {
                ret.left = ret.right - subWidth;
            }
            if (ret.right == UNDEFINED) {
                ret.right = ret.left + subWidth;
            }
        }
        if ((ret.top == UNDEFINED) && (ret.bottom == UNDEFINED)) {
            ret.top = boundingRect.top + (boundingRect.height() - subHeight) / 2;    //  Rien de spécifié => Centrer verticalement
            ret.bottom = ret.top + subHeight;
        } else {
            if (ret.top == UNDEFINED) {
                ret.top = ret.bottom - subHeight;
            }
            if (ret.bottom == UNDEFINED) {
                ret.bottom = ret.top + subHeight;
            }
        }
        return ret;
    }

}
