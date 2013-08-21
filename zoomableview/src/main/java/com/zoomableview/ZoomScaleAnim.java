package com.zoomableview;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * @author nic Scale & Translate animation
 */
public class ZoomScaleAnim extends AnimationSet {

    private float[] iMatrixs = new float[9];
    private float[] tMatrixs = new float[9];
    private ScaleAnimation scaleAnimation;
    private TranslateAnimation translateAnimation;
    private final int mDuration;
    private final Matrix mTargetMatrix;

    /**
     * Split transformation into 2 animations
     * @param fromScale
     * @param toScale
     * @param fromX
     * @param toX
     * @param fromY
     * @param toY
     */
    private void set(float fromScale, float toScale, float fromX, float toX, float fromY, float toY) {
        if (!getAnimations().isEmpty()) {
            getAnimations().clear();
            reset();
        }

        scaleAnimation = new ScaleAnimation(fromScale, toScale, fromScale, toScale);
        scaleAnimation.setDuration(mDuration);
        addAnimation(scaleAnimation);

        translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);
        translateAnimation.setDuration(mDuration);
        addAnimation(translateAnimation);

        setFillAfter(true);
    }

    /**
     * Apply Scale+Translate from matrix to matrix
     * 
     * @param start Matrix
     * @param target Matrix
     * @param duration in ms
     */
    public ZoomScaleAnim(Matrix start, Matrix target, int duration) {
        super(true);
        this.mTargetMatrix = target;
        this.mDuration = duration;
        start.getValues(iMatrixs);
        target.getValues(tMatrixs);
    }

    /**
     * Apply Scale+Translate from matrix to screen coordinates, scale rate and duration
     * 
     * @param start Matrix
     * @param x on start Matrix
     * @param y on start Matrix
     * @param toX on Screen
     * @param toY on Screen
     * @param scale rate
     * @param duration in ms
     */
    public ZoomScaleAnim(Matrix start, float x, float y, float toX, float toY, float scale, int duration) {
        super(true);
        this.mDuration = duration;
        start.getValues(iMatrixs);

        translateAxis(Matrix.MTRANS_X, x, toX, scale);
        translateAxis(Matrix.MTRANS_Y, y, toY, scale);

        this.mTargetMatrix = new Matrix();
        mTargetMatrix.setScale(scale * iMatrixs[Matrix.MSCALE_X], scale * iMatrixs[Matrix.MSCALE_Y]);
        mTargetMatrix.postTranslate(tMatrixs[Matrix.MTRANS_X], tMatrixs[Matrix.MTRANS_Y]);
    }

    private void translateAxis(int direction, float from, float to, float scale) {
        float scaledfrom = scale * iMatrixs[direction];
        float shift = to - from * scale;
        tMatrixs[direction] = scaledfrom + shift;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {

        // Map transformation into Rect
        RectF mapRect = new RectF(0, 0, width, height);
        mTargetMatrix.mapRect(mapRect);

        /*
         * Do not strictly center to the point
         * Adjust transformation :
         * if axis is greater than parent: do not center borders, stick them
         * if axis is smaller than parent: keep centered
         */
        if (mapRect.width() > parentWidth) {
            // Align x borders
            if (mapRect.left > 0) {
                mTargetMatrix.postTranslate(-mapRect.left, 0);
            } else if (mapRect.right < parentWidth) {
                mTargetMatrix.postTranslate(parentWidth - mapRect.right, 0);
            }
        } else {
            // Keep centered on x axis
            mTargetMatrix.postTranslate(parentWidth / 2 - mapRect.left - mapRect.width() / 2, 0);
        }

        if (mapRect.height() > parentHeight) {
            // Align y borders
            if (mapRect.top > 0) {
                mTargetMatrix.postTranslate(0, -mapRect.top);
            } else if (mapRect.bottom < parentHeight) {
                mTargetMatrix.postTranslate(0, parentHeight - mapRect.bottom);
            }
        } else {
            // Keep centered on y axis
            mTargetMatrix.postTranslate(0, parentHeight / 2 - mapRect.top - mapRect.height() / 2);
        }

        // Export matrix values
        mTargetMatrix.getValues(tMatrixs);

        set(iMatrixs[Matrix.MSCALE_X], tMatrixs[Matrix.MSCALE_X],
                iMatrixs[Matrix.MTRANS_X], tMatrixs[Matrix.MTRANS_X],
                iMatrixs[Matrix.MTRANS_Y], tMatrixs[Matrix.MTRANS_Y]);

        super.initialize(width, height, parentWidth, parentHeight);
    }

}
