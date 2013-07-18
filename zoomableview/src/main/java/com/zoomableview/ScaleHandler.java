package com.zoomableview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

/**
 * @author Nicolas CORNETTE
 *
 */
@TargetApi(Build.VERSION_CODES.DONUT)
public abstract class ScaleHandler {

    /**
     * Compatible interface for scale events
     * @author Nicolas CORNETTE
     *
     */
    public interface ScaleListener {

        public boolean onScale(float scaleFactor, float focusX, float focusY);

        public boolean onScaleBegin(float scaleFactor, float focusX, float focusY);

        public void onScaleEnd(float scaleFactor, float focusX, float focusY);

    }

    public static ScaleHandler getInstance(Context c, ScaleListener listener) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            return new ScaleHandlerFroyo(c, listener);
        } else {
            return new ScaleHandlerDonut();
        }
    }

    /**
     * @author Nicolas CORNETTE
     *
     */
    public static class ScaleHandlerDonut extends ScaleHandler {

        @Override
        public boolean handleTouchEvent(MotionEvent event) {
            return false;
        }

    }

    /**
     * @author Nicolas CORNETTE
     *
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static class ScaleHandlerFroyo extends ScaleHandler {

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

    public abstract boolean handleTouchEvent(MotionEvent event);

}
