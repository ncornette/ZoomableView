package com.zoomableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import com.zoomableview.scroller.ScrollDelegate;
import com.zoomableview.scroller.ScrollOver;
import com.zoomableview.scroller.ScrollStop;

/**
 * @author Nicolas CORNETTE
 * Zoomable view that can be controlled by touch
 */
public class ZoomViewTouchable extends ZoomView implements OnDoubleTapListener, OnGestureListener {

    private static final String TAG = ZoomViewTouchable.class.getSimpleName();

    public interface OverScrollListener {

        /**
         * Overscroll in the X axis in pixels
         * can be positive or negative depending on the direction
         * @param f
         */
        void onOverscrollX(float overScrollX);

        /**
         * Overscroll in the Y axis in pixels
         * can be positive or negative depending on the direction
         * @param f
         */
        void onOverscrollY(float overScrollY);

    }

    private static final OverScrollListener NULL_OVERSCROLL_LISTENER = new OverScrollListener() {
        @Override
        public void onOverscrollX(float f) {
            if (DEBUG) Log.v(TAG, "OverScroll X: " + f);
        }

        @Override
        public void onOverscrollY(float f) {
            if (DEBUG) Log.v(TAG, "OverScroll Y: " + f);
        }
    };

    public static interface TouchMapListener {
    
        void onDoubleTap(float eventX, float eventY);
    
        void onTouchScale(float scaleFactor, float foxusX, float focusY);
    
        void onSingleTapConfirmed();
    
        void onTouch(float x, float y);
    
        void onSingleTapCancelled();
    }

    private static final TouchMapListener NULL_TOUCHMAP_LISTENER = new TouchMapListener() {

        @Override
        public void onTouchScale(float scaleFactor, float foxusX, float focusY) {
        }

        @Override
        public void onTouch(float x, float y) {
        }

        @Override
        public void onSingleTapConfirmed() {
        }

        @Override
        public void onSingleTapCancelled() {
        }

        @Override
        public void onDoubleTap(float eventX, float eventY) {
        }
    };

    private GestureDetector gestureScanner;
    protected TouchMapListener mapListener = NULL_TOUCHMAP_LISTENER;
    private OverScrollListener mOverScrollListener = NULL_OVERSCROLL_LISTENER;
    private RectF rectMap = new RectF();
    private RectF rectMapUpdate = new RectF();
    private boolean mDoubleTapZoom;
    private Matrix matrixTranslate = new Matrix();
    private float mflingScale;

    private ScrollDelegate mOverScroller;

    public ZoomViewTouchable(Context context) {
        this(context, null);
    }

    public ZoomViewTouchable(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomViewTouchable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        gestureScanner = new GestureDetector(getContext(), this);
        gestureScanner.setOnDoubleTapListener(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.com_zoomableview_DepMapView, defStyle, 0);

        mDoubleTapZoom = a.getBoolean(R.styleable.com_zoomableview_DepMapView_doubletabZoom, true);
        mflingScale = a.getFloat(R.styleable.com_zoomableview_DepMapView_flingScale, 3.0f);
        float overScrollRate = a.getFloat(R.styleable.com_zoomableview_DepMapView_overScrollTranslateFactor, 0.3f);

        if (overScrollRate == 0) {
            mOverScroller = new ScrollStop();
        } else {
            mOverScroller = new ScrollOver(overScrollRate);
        }

        a.recycle();
    }

    public void setOverScrollListener(OverScrollListener listener) {
        if (mOverScrollListener == null) {
            mOverScrollListener = NULL_OVERSCROLL_LISTENER;
        } else {
            mOverScrollListener = listener;
        }
    }

    public void setTouchMapListener(TouchMapListener mapListener) {
        if (mapListener == null) {
            this.mapListener = NULL_TOUCHMAP_LISTENER;
        } else {
            this.mapListener = mapListener;
        }
    }


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
    public boolean onDown(MotionEvent e) {
        if (DEBUG)
            Log.v(TAG, "onDown");
        mapListener.onTouch(e.getX(), e.getY());
        updateDiffRect();
        mapZoomHandler.removeMessages(1);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (DEBUG) Log.v(TAG, "Action_Up");
            if (!zooming() && (movedX || movedY)) {
                translateTargetPosition();
            }
        }
        return gestureScanner.onTouchEvent(event);
    }

    private void translateTargetPosition() {
        // Will start animation for the image to return in its bounds
        updateDiffRect();
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
            // Create matrix for translation from current rect to new rect
            matrixTranslate.setRectToRect(rectMapOrigin, rectMapUpdate, ScaleToFit.FILL);
            mapScaleAnim = new ZoomScaleAnim(matrix, matrixTranslate, 200);
            mapScaleAnim.initialize((int) rectMapOrigin.width(), (int) rectMapOrigin.height(), getWidth(), getHeight());
            mapScaleAnim.start();
            Message.obtain(mapZoomHandler, 0).sendToTarget();
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (DEBUG) Log.v(TAG, "onDoubleTap");
        mapListener.onDoubleTap(e.getX(), e.getY());
        if (mDoubleTapZoom) zoomToggle(e.getX(), e.getY());
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (DEBUG) Log.v(TAG, "onDoubleTapEvent");
        return false;
    }

    public void centerOnPoint(boolean zoomIfNeeded, float[] pointF) {
        if ((zoomed || zoomIfNeeded)) {
            matrix.mapPoints(pointF);
            if (!zoomed && zoomIfNeeded) {
                mapScaleAnim = new ZoomScaleAnim(matrix, pointF[0], pointF[1], getWidth() / 2, getHeight() / 2, getAutoZoomLevel(), 500);
                zoomed = true;
            } else {
                mapScaleAnim = new ZoomScaleAnim(matrix, pointF[0], pointF[1], getWidth() / 2, getHeight() / 2, 1, 500);
            }
            mapScaleAnim.initialize((int) rectMapOrigin.width(), (int) rectMapOrigin.height(), getWidth(), getHeight());
            mapScaleAnim.start();
            Message.obtain(mapZoomHandler, 0).sendToTarget();
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.i(getClass().getSimpleName(), "onSingleTapConfirmed");
        mapListener.onSingleTapConfirmed();
        return false;
    }

    protected void updateDiffRect() {
        // Put current map rect into rectMap
        matrix.mapRect(rectMap, rectMapOrigin);
        // Create copy of rectMap to hold transformation
        rectMapUpdate.set(rectMap);
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        updateDiffRect();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        if (DEBUG) Log.v(TAG, "onFling");
        if (!zooming() && (movedX || movedY)) {
            mapScaleAnim = new ZoomScaleAnim(matrix, 0, 0, velocityX / mflingScale, velocityY / mflingScale, 1, 1000);
            mapScaleAnim.setInterpolator(new DecelerateInterpolator(mflingScale));
            mapScaleAnim.initialize((int) rectMapOrigin.width(), (int) rectMapOrigin.height(), getWidth(), getHeight());
            mapScaleAnim.start();
            Message.obtain(mapZoomHandler, 0).sendToTarget();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (DEBUG) Log.v(TAG, "onLongPress");

    }

    boolean movedX;
    boolean movedY;

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        movedX = false;
        movedY = false;
        if (DEBUG)
            Log.v(TAG, String.format("onScroll : dist : %f, %f ", distanceX, distanceY));
        if (!zooming()) {
            matrix.mapRect(rectMap, rectMapOrigin);

            if (rectView.right + distanceX >= rectMap.right && distanceX > 0) {
                movedX = mOverScroller.onScrollX(rectMap, rectView, distanceX, matrix);
                mOverScrollListener.onOverscrollX(rectMap.right - Math.min(rectMapUpdate.right, rectView.right));
            } else if (rectView.left + distanceX <= rectMap.left && distanceX < 0) {
                movedX = mOverScroller.onScrollX(rectMap, rectView, distanceX, matrix);
                mOverScrollListener.onOverscrollX(rectMap.left - Math.max(rectMapUpdate.left, rectView.left));
            } else if (distanceX != 0) {
                matrix.postTranslate(-distanceX, 0);
                movedX = true;
            }

            if (rectView.bottom + distanceY >= rectMap.bottom && distanceY > 0) {
                movedY = mOverScroller.onScrollY(rectMap, rectView, distanceY, matrix);
                mOverScrollListener.onOverscrollY(rectMap.bottom - Math.min(rectMapUpdate.bottom, rectView.bottom));
            } else if (rectView.top + distanceY <= rectMap.top && distanceY < 0) {
                movedY = mOverScroller.onScrollY(rectMap, rectView, distanceY, matrix);
                mOverScrollListener.onOverscrollY(rectMap.top - Math.max(rectMapUpdate.top, rectView.top));
            } else if (distanceY != 0) {
                matrix.postTranslate(0, -distanceY);
                movedY = true;
            }

            if (movedX || movedY) {
                invalidate();
                return true;
            }
        }
        return false;
    }

    boolean zooming() {
        return mapZoomHandler.hasMessages(1);
    }

    @Override
    public void onShowPress(MotionEvent e) {
        if (DEBUG) Log.v(TAG, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean isZoomed() {
        return zoomed;
    }

    @Override
    protected void onDraw(Canvas canvas) {
         if (DEBUG) {
            // Debug Rects
            debugRect(canvas, rectView, Color.BLUE);
            debugRect(canvas, rectMap, Color.RED);
            debugRect(canvas, rectMapUpdate, Color.GREEN);
         }
        super.onDraw(canvas);
    }

    /**
     * Convenient method for use with ViewPager.
     * 
     * @return whether the left edge of the image view touches the left edge of its container
     * @see {@link ZoomViewTouchable#setOverScrollListener(com.zoomableview.ZoomViewTouchable.OverScrollListener)}
     */
    public boolean isFullLeft() {
        return rectMap.left >= rectView.left;
    }

    /**
     * Convenient method for use with ViewPager.
     * 
     * @return whether the right edge of the image view touches the right edge of its container
     * @see {@link ZoomViewTouchable#setOverScrollListener(com.zoomableview.ZoomViewTouchable.OverScrollListener)}
     */
    public boolean isFullRight() {
        return rectMap.right <= rectView.right;
    }

}
