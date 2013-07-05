package com.zoomableview;

import android.graphics.Matrix;
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
    private final int mDuration;

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
    public MapScaleAnim(Matrix start, Matrix target, int duration) {
        super(true);
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
    public MapScaleAnim(Matrix start, float x, float y, float toX, float toY, float scale, int duration) {
        super(true);
        this.mDuration = duration;
        start.getValues(iMatrixs);

        tMatrixs[Matrix.MSCALE_X] = scale * iMatrixs[Matrix.MSCALE_X];
        tMatrixs[Matrix.MSCALE_Y] = scale * iMatrixs[Matrix.MSCALE_Y];

        setTranslate(Matrix.MTRANS_X, x, toX, scale);
        setTranslate(Matrix.MTRANS_Y, y, toY, scale);
    }

    private void setTranslate(int direction, float from, float to, float scale) {
        float scaledfrom = scale * iMatrixs[direction];
        float shift = to - from * scale;
        tMatrixs[direction] = scaledfrom + shift;
	}
    
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {

        // TODO: Correct translation here, to avoid screen overflow

        set(iMatrixs[Matrix.MSCALE_X], tMatrixs[Matrix.MSCALE_X],
                iMatrixs[Matrix.MTRANS_X], tMatrixs[Matrix.MTRANS_X],
                iMatrixs[Matrix.MTRANS_Y], tMatrixs[Matrix.MTRANS_Y]);

        super.initialize(width, height, parentWidth, parentHeight);
    }

}
