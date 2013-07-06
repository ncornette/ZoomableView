package com.zoomableview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.animation.BounceInterpolator;

@TargetApi(value = 8)
public class DepMapViewScalable extends DepMapViewTouchable implements OnScaleGestureListener {

    private static final String TAG = DepMapViewScalable.class.getSimpleName();
    private android.view.ScaleGestureDetector scaleDetector;
    private float[] matrixValues = new float[9];
    private float[] matrixOriginValues = new float[9];
    Matrix savedMatrix = new Matrix();

    public DepMapViewScalable(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleDetector = new android.view.ScaleGestureDetector(this.getContext(), this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        matrixOrigin.getValues(matrixOriginValues);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {

        if (Boolean.FALSE.equals(scaling)) {
            scaling = null;
            return false;
        }
        if (scaling != null)
            return false;
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onScale(android.view.ScaleGestureDetector detector) {
        if (!zooming()) {
            savedMatrix.set(matrix);
            matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            matrix.getValues(matrixValues);
            if (matrixValues[Matrix.MSCALE_X] < matrixOriginValues[Matrix.MSCALE_X] / 1.5f ||
                    matrixValues[Matrix.MSCALE_X] > matrixOriginValues[Matrix.MSCALE_X] * 5f) {
                matrix.set(savedMatrix);
            } else {
                invalidate();
            }
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(android.view.ScaleGestureDetector detector) {
        if (DEBUG) Log.v(TAG, "Scale Begin");
        mapListener.onSingleTapCancelled();
        mapListener.onTouchScale(detector.getScaleFactor(),
                detector.getFocusX(), detector.getFocusY());
        scaling = true;
        zoomed = true;
        return true;
    }

    @Override
    public void onScaleEnd(android.view.ScaleGestureDetector detector) {
        if (DEBUG) Log.v(TAG, "Scale End");
        updateDiffRect();
        matrix.getValues(matrixValues);
        scaling = false;

        if (matrixValues[Matrix.MSCALE_X] < matrixOriginValues[Matrix.MSCALE_X]) {
            mapScaleAnim = new MapScaleAnim(matrix, matrixOrigin, 200);
            zoomed = false;
        } else if (matrixValues[Matrix.MSCALE_X] > matrixOriginValues[Matrix.MSCALE_X] * getMaxZoomLevel()) {
            mapScaleAnim = new MapScaleAnim(matrix, getWidth() / 2, getHeight() / 2, getWidth() / 2, getHeight() / 2,
                    matrixOriginValues[Matrix.MSCALE_X] / matrixValues[Matrix.MSCALE_X] * getMaxZoomLevel(), 200);
        } else {
            return;
        }

        mapScaleAnim.initialize((int) rectMapOrigin.width(), (int) rectMapOrigin.height(), getWidth(), getHeight());
        mapScaleAnim.setInterpolator(new BounceInterpolator());
        mapScaleAnim.start();
        mapZoomHandler.handleMessage(null);
    }

}
