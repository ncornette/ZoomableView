package com.zoomableview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.BounceInterpolator;

import com.zoomableview.events.MotionEventHandler;
import com.zoomableview.events.VoidScaleHandler;
import com.zoomableview.events.ScaleHandlerFroyo;
import com.zoomableview.events.ScaleListener;

/**
 * @author Nicolas CORNETTE
 * Zoomable View that can be controlled by touch and including PinchToZoom 
 * for multitouch devices only
 */
@TargetApi(Build.VERSION_CODES.DONUT)
public class DepMapViewScalable extends DepMapViewTouchable implements ScaleListener {

    private static final String TAG = DepMapViewScalable.class.getSimpleName();
    private float[] matrixValues = new float[9];
    private float[] matrixOriginValues = new float[9];
    Matrix savedMatrix = new Matrix();
    private MotionEventHandler mScaleHandler;

    public DepMapViewScalable(Context context) {
        super(context);
        init();
    }

    public DepMapViewScalable(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DepMapViewScalable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            mScaleHandler = new ScaleHandlerFroyo(getContext(), this);
        } else {
            mScaleHandler = new VoidScaleHandler();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        matrixOrigin.getValues(matrixOriginValues);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleHandler.handleTouchEvent(event);
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
    public boolean onScale(float scaleFactor, float focusX, float focusY) {
        if (DEBUG)
            Log.v(TAG, "Scale Begin");
        mapListener.onSingleTapCancelled();
        mapListener.onTouchScale(scaleFactor, focusX, focusY);
        scaling = true;
        zoomed = true;
        return true;
    }

    @Override
    public boolean onScaleBegin(float scaleFactor, float focusX, float focusY) {
        if (!zooming()) {
            savedMatrix.set(matrix);
            matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
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
    public void onScaleEnd(float scaleFactor, float focusX, float focusY) {
        if (DEBUG)
            Log.v(TAG, "Scale End");
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
        Message.obtain(mapZoomHandler, 0).sendToTarget();
    }

}
