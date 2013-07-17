package com.zoomableview.events;

import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public abstract class TouchListenerFroyo implements ScaleListenerCompat, OnScaleGestureListener {

    @Override
    public abstract boolean onScale(float scaleFactor, float focusX, float focusY);

    @Override
    public abstract boolean onScaleBegin(float scaleFactor, float focusX, float focusY);
    
    @Override
    public abstract void onScaleEnd(float scaleFactor, float focusX, float focusY);

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return onScale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return onScaleBegin(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        onScaleEnd(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());

    }

}
