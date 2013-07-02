package com.zoomableview;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * @author nic Scale & Translate animation
 */
public class MapScaleAnim extends AnimationSet {

    private float[] iMatrixs = new float[9];
    private float[] tMatrixs = new float[9];
    private ScaleAnimation scaleAnimation;
    private TranslateAnimation translateAnimation;

    public void set(float fromScale, float toScale, float fromX, float toX, float fromY, float toY, int duration) {
        if (!getAnimations().isEmpty()) {
            getAnimations().clear();
            reset();
        }

        scaleAnimation = new ScaleAnimation(
                fromScale, toScale, fromScale, toScale,
                Animation.RELATIVE_TO_SELF, 0.50f,
                Animation.RELATIVE_TO_SELF, 0.50f);
        scaleAnimation.setDuration(duration);
        addAnimation(scaleAnimation);

        translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);
        translateAnimation.setDuration(duration);
        addAnimation(translateAnimation);

        setFillAfter(true);
    }

    public void set(Matrix start, Matrix target, int duration) {
        start.getValues(iMatrixs);
        target.getValues(tMatrixs);

        set(iMatrixs[Matrix.MSCALE_X], tMatrixs[Matrix.MSCALE_X],
                iMatrixs[Matrix.MTRANS_X], tMatrixs[Matrix.MTRANS_X],
                iMatrixs[Matrix.MTRANS_Y], tMatrixs[Matrix.MTRANS_Y], duration);
    }

    /**
     * Apply Scale+Translate from matrix to matrix
     * 
     * @param start Matrix
     * @param target Matrix
     * @param duration in ms
     */
    public MapScaleAnim(Matrix start, Matrix target, int duration) {
        super(true);
        set(start, target, duration);
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
    public MapScaleAnim(Matrix start, float x, float y, float toX, float toY, float scale, int duration) {
        super(true);
        start.getValues(iMatrixs);

        Matrix target = new Matrix();
        tMatrixs[Matrix.MSCALE_X] = scale * iMatrixs[Matrix.MSCALE_X];
        tMatrixs[Matrix.MSCALE_Y] = scale * iMatrixs[Matrix.MSCALE_Y];
        tMatrixs[Matrix.MTRANS_X] = scale * iMatrixs[Matrix.MTRANS_X] + toX - x * scale;
        tMatrixs[Matrix.MTRANS_Y] = scale * iMatrixs[Matrix.MTRANS_Y] + toY - y * scale;
        target.setValues(tMatrixs);

        set(start, target, duration);
    }

}
