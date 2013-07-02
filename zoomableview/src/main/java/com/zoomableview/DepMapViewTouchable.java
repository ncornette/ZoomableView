package com.zoomableview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Transformation;

public class DepMapViewTouchable extends DepMapView implements OnDoubleTapListener, OnGestureListener {

    private Transformation transform;
    protected boolean zoomed;
    protected MapScaleAnim mapScaleAnim;
    private GestureDetector gestureScanner;
    protected TouchMapListener mapListener;

    public static interface TouchMapListener {

        void onDoubleTap(float eventX, float eventY);

        void onTouchScale(float scaleFactor, float foxusX, float focusY);

        void onSingleTapConfirmed();

        void onTouch(float x, float y);

        void onSingleTapCancelled();
    }

    public DepMapViewTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
        transform = new Transformation();
        gestureScanner = new GestureDetector(getContext(), this);
        gestureScanner.setOnDoubleTapListener(this);
    }

    public void setTouchMapListener(TouchMapListener mapListener) {
        this.mapListener = mapListener;
    }

    Handler mapZoomHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!mapScaleAnim.hasEnded()) {
                this.sendEmptyMessageDelayed(0, 40);
                mapScaleAnim.getTransformation(System.currentTimeMillis(), transform);
                matrix.set(transform.getMatrix());
                invalidate();
            }
        }
    };
    private RectF rectMap = new RectF();
    private RectF rectMapUpdate = new RectF();
    private boolean moved;

    public static float distance(PointF p, PointF q)
    {
        return distance(p.x, p.y, q.x, q.y);
    }

    public static float distance(float x0, float y0, float x1, float y1)
    {
        float dx = x0 - x1; // horizontal difference
        float dy = y0 - y1; // vertical difference
        return android.util.FloatMath.sqrt(dx * dx + dy * dy);
    }

    public static boolean equalsRect(RectF rect1, RectF rect2) {
        return rect1.left == rect2.left && rect1.right == rect2.right
                && rect1.top == rect2.top && rect1.bottom == rect2.bottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.i(getClass().getSimpleName(), "Action_Up");
            if (!zooming() && moved) {
                matrix.mapRect(rectMap, rectMapOrigin);
                rectMapUpdate.set(rectMap);

                if (rectMap.width() < getWidth()) {
                    rectMapUpdate.offset(-rectMap.centerX() + rectView.centerX(), 0);
                } else if (rectMap.left > 0) {
                    rectMapUpdate.offset(-rectMap.left, 0);
                } else if (rectMap.right < getWidth()) {
                    rectMapUpdate.offset(getWidth() - rectMap.right, 0);
                }

                if (rectMap.height() < getHeight()) {
                    rectMapUpdate.offset(0, -rectMap.centerY() + rectView.centerY());
                } else if (rectMap.top > 0) {
                    rectMapUpdate.offset(0, -rectMap.top);
                } else if (rectMap.bottom < getHeight()) {
                    rectMapUpdate.offset(0, getHeight() - rectMap.bottom);
                }

                if (!equalsRect(rectMapUpdate, rectMap)) {
                    Matrix matrix2 = new Matrix();
                    matrix2.setRectToRect(rectMapOrigin, rectMapUpdate, ScaleToFit.FILL);
                    mapScaleAnim = new MapScaleAnim(matrix, matrix2, 200);
                    mapScaleAnim.initialize(0, 0, getWidth(), getHeight());
                    mapScaleAnim.start();
                    mapZoomHandler.handleMessage(null);
                }
            }
        }
        return gestureScanner.onTouchEvent(event);
    }

    public void zoomOnScreen(float x, float y) {
        mapScaleAnim = new MapScaleAnim(matrixOrigin, x, y, getWidth() / 2, getHeight() / 2, 2f, 500);
        mapScaleAnim.initialize(0, 0, getWidth(), getHeight());
        mapScaleAnim.start();
        mapZoomHandler.handleMessage(null);
        zoomed = true;
    }

    public void zoomOut() {
        zoomOut(500);
    }

    public void zoomOut(int duration) {
        mapScaleAnim = new MapScaleAnim(matrix, matrixOrigin, duration);
        mapScaleAnim.initialize(0, 0, getWidth(), getHeight());
        mapScaleAnim.start();
        mapZoomHandler.handleMessage(null);
        zoomed = false;
    }

    /**
     * Switch to ZoomIn or ZoomOut
     * 
     * @param onX
     * @param onY
     */
    public void zoomToggle(float onX, float onY) {
        if (!zoomed) {
            zoomOnScreen(onX, onY);
        } else {
            zoomOut();
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i(getClass().getSimpleName(), "onDoubleTap");
        // zoomToggle(e.getX(), e.getY());
        // Use Listener
        if (mapListener != null) {
            mapListener.onDoubleTap(e.getX(), e.getY());
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.i(getClass().getSimpleName(), "onDoubleTapEvent");
        return false;
    }

    public void centerOnPoint(boolean zoomIfNeeded, float[] pointF) {
        if ((zoomed || zoomIfNeeded)) {
            matrix.mapPoints(pointF);
            if (!zoomed && zoomIfNeeded) {
                mapScaleAnim = new MapScaleAnim(matrix, pointF[0], pointF[1], getWidth() / 2, getHeight() / 2, 2, 500);
                zoomed = true;
            } else {
                mapScaleAnim = new MapScaleAnim(matrix, pointF[0], pointF[1], getWidth() / 2, getHeight() / 2, 1, 500);
            }
            mapScaleAnim.initialize(0, 0, getWidth(), getHeight());
            mapScaleAnim.start();
            mapZoomHandler.handleMessage(null);
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.i(getClass().getSimpleName(), "onSingleTapConfirmed");
        mapListener.onSingleTapConfirmed();
        // centerOnCurrentDep();
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.i(getClass().getSimpleName(), "onDown");

        // Use Listener
        if (mapListener != null) {
            mapListener.onTouch(e.getX(), e.getY());
        }

        moved = false;
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        // Log.i(getClass().getSimpleName(), "onFling");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // Log.i(getClass().getSimpleName(), "onLongPress");

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        // Log.i(getClass().getSimpleName(), String.format("onScroll : dist : %f, %f ", distanceX,
        // distanceY));
        if (!zooming()) {
            matrix.mapRect(rectMap, rectMapOrigin);

            if ((rectView.right > rectMap.right && distanceX > 0) ||
                    (rectView.left < rectMap.left && distanceX < 0)) {
                distanceX *= 0.3f;
            }

            if ((rectView.bottom > rectMap.bottom && distanceY > 0) ||
                    (rectView.top < rectMap.top && distanceY < 0)) {
                distanceY *= 0.3f;
            }

            moved = true;
            matrix.postTranslate(-distanceX, -distanceY);
            invalidate();
        }
        return true;
    }

    boolean zooming() {
        return mapZoomHandler.hasMessages(0);
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // Log.i(getClass().getSimpleName(), "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean isZoomed() {
        return zoomed;
    }
}
