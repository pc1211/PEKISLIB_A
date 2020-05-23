package com.example.pgyl.pekislib_a;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public class PointRectUtils {  //  Routines adaptées à des coordonnées (0,0) en haut à gauche de leur canvas

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
    public static RectF ALIGN_WIDTH_HEIGHT = new RectF(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
    public static RectF ALIGN_RIGHT_BOTTOM = new RectF(UNDEFINED, UNDEFINED, 1, 1);
    public static RectF ALIGN_LEFT_HEIGHT = new RectF(0, UNDEFINED, UNDEFINED, UNDEFINED);
    public static float FULL_SIZE_COEFF = 1;
    public static float SQUARE_ASPECT_RATIO = 1;

    public static RectF getMaxSubRect(RectF boundingRect, RectF relativePositionCoeffs, float aspectRatio, float sizeCoeff) {
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
        RectF maxSubRect = new RectF(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
        if (relativePositionCoeffs.left != UNDEFINED) {
            maxSubRect.left = boundingRect.left + boundingRect.width() * relativePositionCoeffs.left;
        }
        if (relativePositionCoeffs.top != UNDEFINED) {
            maxSubRect.top = boundingRect.top + boundingRect.height() * relativePositionCoeffs.top;
        }
        if (relativePositionCoeffs.right != UNDEFINED) {
            maxSubRect.right = boundingRect.left + boundingRect.width() * relativePositionCoeffs.right;
        }
        if (relativePositionCoeffs.bottom != UNDEFINED) {
            maxSubRect.bottom = boundingRect.top + boundingRect.height() * relativePositionCoeffs.bottom;
        }
        if ((maxSubRect.left == UNDEFINED) && (maxSubRect.right == UNDEFINED)) {
            maxSubRect.left = boundingRect.left + (boundingRect.width() - subWidth) / 2;   //  Rien de spécifié => Centrer horizontalement
            maxSubRect.right = maxSubRect.left + subWidth;
        } else {
            if (maxSubRect.left == UNDEFINED) {
                maxSubRect.left = maxSubRect.right - subWidth;
            }
            if (maxSubRect.right == UNDEFINED) {
                maxSubRect.right = maxSubRect.left + subWidth;
            }
        }
        if ((maxSubRect.top == UNDEFINED) && (maxSubRect.bottom == UNDEFINED)) {
            maxSubRect.top = boundingRect.top + (boundingRect.height() - subHeight) / 2;    //  Rien de spécifié => Centrer verticalement
            maxSubRect.bottom = maxSubRect.top + subHeight;
        } else {
            if (maxSubRect.top == UNDEFINED) {
                maxSubRect.top = maxSubRect.bottom - subHeight;
            }
            if (maxSubRect.bottom == UNDEFINED) {
                maxSubRect.bottom = maxSubRect.top + subHeight;
            }
        }
        return maxSubRect;
    }

    public static RectF getSubRect(Rect boundingRect, int subWidth, int subHeight, RectF relativePositionCoeffs) {
        RectF subRect = new RectF(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
        if (relativePositionCoeffs.left != UNDEFINED) {
            subRect.left = boundingRect.left + (int) (boundingRect.width() * relativePositionCoeffs.left);
        }
        if (relativePositionCoeffs.top != UNDEFINED) {
            subRect.top = boundingRect.top + (int) (boundingRect.height() * relativePositionCoeffs.top);
        }
        if (relativePositionCoeffs.right != UNDEFINED) {
            subRect.right = boundingRect.left + (int) (boundingRect.width() * relativePositionCoeffs.right);
        }
        if (relativePositionCoeffs.bottom != UNDEFINED) {
            subRect.bottom = boundingRect.top + (int) (boundingRect.height() * relativePositionCoeffs.bottom);
        }
        if ((subRect.left == UNDEFINED) && (subRect.right == UNDEFINED)) {
            subRect.left = boundingRect.left + (boundingRect.width() - subWidth) / 2;   //  Rien de spécifié => Centrer horizontalement
            subRect.right = subRect.left + subWidth;
        } else {
            if (subRect.left == UNDEFINED) {
                subRect.left = subRect.right - subWidth;
            }
            if (subRect.right == UNDEFINED) {
                subRect.right = subRect.left + subWidth;
            }
        }
        if ((subRect.top == UNDEFINED) && (subRect.bottom == UNDEFINED)) {
            subRect.top = boundingRect.top + (boundingRect.height() - subHeight) / 2;    //  Rien de spécifié => Centrer verticalement
            subRect.bottom = subRect.top + subHeight;
        } else {
            if (subRect.top == UNDEFINED) {
                subRect.top = subRect.bottom - subHeight;
            }
            if (subRect.bottom == UNDEFINED) {
                subRect.bottom = subRect.top + subHeight;
            }
        }
        return subRect;
    }
}
