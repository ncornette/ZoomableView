package com.zoomableview.events;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

@TargetApi(Build.VERSION_CODES.FROYO)
public class ScaleHandlerFroyo implements MotionEventHandler {

    private ScaleGestureDetector mScaleDetector;
    private final ScaleListener mScaleListener;

    public ScaleHandlerFroyo(Context c, ScaleListener listener) {
        mScaleListener = listener;
        mScaleDetector = new android.view.ScaleGestureDetector(c, new OnScaleGestureListener() {

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                mScaleListener.onScaleEnd(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return mScaleListener.onScaleBegin(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                return mScaleListener.onScale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            }
        });
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        return mScaleDetector.onTouchEvent(event);
    }

}
