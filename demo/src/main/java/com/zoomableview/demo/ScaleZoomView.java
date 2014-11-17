/**
 * 
 */
package com.zoomableview.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * @author nic
 * <p>
 * TODO: Use different {@link RectFEvaluator}s to check / modify rect before update
 *
 */
public class ScaleZoomView extends View {

    protected static final String TAG = ScaleZoomView.class.getSimpleName();
    private Paint mClipRectPaint = new Paint();
    private Paint mCanvasRectPaint = new Paint();
    private Rect mSourceImgRect = new Rect(); // Intrinsic bitmap Rect for points mapping
    private RectF mClipBounds = new RectF(); // Container clip rect
    private RectF mStartDisplayRect = new RectF(); // Initial on-screen image Rect
    private RectF mDisplayRect = new RectF(); // Current on-screen image Rect
    private GestureDetector mGestureScanner;
    private ScaleGestureDetector mScaleDetector;
    private GestureRectUpdater mGestureListener;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private Paint mDebugTextPaint = new Paint();
    
    public ScaleZoomView(Context context) {
        this(context, null);
    }

    public ScaleZoomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleZoomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDebugTextPaint.setTextSize(16 * mDisplayMetrics.scaledDensity);
        mDebugTextPaint.setColor(Color.RED);

        mClipRectPaint.setColor(Color.RED);
        mClipRectPaint.setStyle(Style.STROKE);
        mClipRectPaint.setStrokeWidth(4.0f * mDisplayMetrics.density);

        mCanvasRectPaint.setColor(Color.BLUE);
        mCanvasRectPaint.setStyle(Style.STROKE);
        mCanvasRectPaint.setStrokeWidth(4.0f * mDisplayMetrics.density);

        mGestureListener = new GestureRectUpdater(getContext(), new OnRectUpdateListener() {

            @Override
            public void onRectUpdate(GestureRectUpdater updater) {
                invalidate();
            }

            @Override
            public RectF onDoubleTapZoom(GestureRectUpdater rectUpdater, float eventX, float eventY) {
                RectF toRectF = new RectF(mDisplayRect);

                float scale;
                float zoomLevel = getCurrentZoomLevel();

                // Set target Scale level
                if (Math.round(zoomLevel * 3) != 3) {
                    // Zoom out to initial position
                    return mStartDisplayRect;
                } else {
                    // Set scale for zoom-in request
                    scale = getTargetScale(2.0f);
                }

                // Set target Rect
                GestureRectUpdater.scale(toRectF, scale, eventX, eventY);

                // Offset to center on Touch Point
                // toRectF.offset(mClipBounds.centerX() - eventX, mClipBounds.centerY() - eventY);

                return toRectF;
            }
        });

        mGestureScanner = new GestureDetector(getContext(), mGestureListener);
        mGestureScanner.setOnDoubleTapListener(mGestureListener);
        mScaleDetector = new android.view.ScaleGestureDetector(getContext(), mGestureListener);
        
        mBitmap = BitmapFactory.decodeResource(getResources(), com.zoomableview.demo.R.drawable.telefunken1536);
        mSourceImgRect.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mMatrix = new Matrix();
    }

    /**
     * Returns current scale level relative to initial default scale level
     * @return
     */
    public float getCurrentZoomLevel() {
        return mDisplayRect.width() / mStartDisplayRect.width();
    }

    /**
     * Returns current scale level relative to initial default scale level
     * @return
     */
    public void ZoomOnBitmapPoint(float x, float y) {
        mMatrix.setRectToRect(new RectF(mSourceImgRect), mDisplayRect, ScaleToFit.START);
        float[] pts = new float[]{
                x, y
        };

        // Map to screen coordinates
        mMatrix.mapPoints(pts);
    }

    private float getTargetScale(float targetScale) {
        return targetScale / getCurrentZoomLevel();
    }

    private float getStartZoomLevel() {
        return mStartDisplayRect.width() / mSourceImgRect.width();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return mGestureScanner.onTouchEvent(event);
        // return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String[] logs = {
                "Initial native Zoom : " + getStartZoomLevel(),
                "current relative Zoom : " + getCurrentZoomLevel(),
        };

        for (int i = 0; i < logs.length; i++) {
            canvas.drawText(logs[i],
                    15, mDebugTextPaint.getTextSize() * (i + 1), mDebugTextPaint);
        }

        canvas.clipRect(mClipBounds);
        canvas.drawRect(mDisplayRect, mCanvasRectPaint);
        canvas.drawRect(mClipBounds, mClipRectPaint);

        // mMatrix.setRectToRect(mSourceImgRect, mDisplayRect, ScaleToFit.START);
        canvas.drawBitmap(mBitmap, mSourceImgRect, mDisplayRect, null);

    }

    private Rect tmpClipRect = new Rect();
    private Rect tmpOutRect = new Rect();
    private DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {

            float margin = 32 * mDisplayMetrics.density;

            mClipBounds.set(left + margin, top + margin * 6, right - margin, bottom - margin);
            mClipBounds.round(tmpClipRect);
            

            // Apply Scale type

            // Center inside
            float scale = Math.min(
                    mClipBounds.width() / mBitmap.getWidth(),
                    mClipBounds.height() / mBitmap.getHeight());

            // Center crop
            // float scale = Math.max(
            // mClipBounds.width() / mBitmap.getWidth(),
            // mClipBounds.height() / mBitmap.getHeight());

            int imgScaledWidth = Math.round(scale * mBitmap.getWidth());
            int imgScaledHeight = Math.round(scale * mBitmap.getHeight());


            // Apply Gravity
            Gravity.apply(Gravity.CENTER, imgScaledWidth, imgScaledHeight, tmpClipRect, tmpOutRect);
            mStartDisplayRect.set(tmpOutRect);
            mDisplayRect.set(tmpOutRect);

            mGestureListener.setRect(mDisplayRect);
        }
    }

    public interface OnRectUpdateListener {
        public void onRectUpdate(GestureRectUpdater updater);

        public RectF onDoubleTapZoom(GestureRectUpdater updater, float eventX, float eventY);
    }
    
    private static class GestureRectUpdater implements OnGestureListener, OnDoubleTapListener, OnScaleGestureListener, AnimatorUpdateListener {

        private RectF mOutRect;
        private RectFEvaluator mRectFEvaluator;
        private Context mContext;
        private Rect tmpOutRect = new Rect();
        private Scroller mScroller;
        private OnRectUpdateListener mListener;
        private ValueAnimator mScrollAnimator;
        private ValueAnimator mRectAnimator;

        public GestureRectUpdater(Context context, OnRectUpdateListener listener) {

            mContext = context;
            mListener = listener;

            mScrollAnimator = ValueAnimator.ofInt(0, 1000).setDuration(1000);
            mScrollAnimator.setInterpolator(new LinearInterpolator());
            mScrollAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mScrollAnimator.setRepeatMode(ValueAnimator.RESTART);
            mScrollAnimator.addUpdateListener(this);
            mScroller = new Scroller(mContext);

        }

        private void setRect(RectF outRect) {
            mOutRect = outRect;
            mRectFEvaluator = new RectFEvaluator(mOutRect);
        }

        private void finishAnimations() {
            mScroller.forceFinished(true);
            if (mRectAnimator != null) {
                mRectAnimator.cancel();
            }
        }

        /* (non-Javadoc)
         * @see android.view.GestureDetector.OnGestureListener
         */

        @Override
        public boolean onDown(MotionEvent e) {
            Log.v(TAG, "onDown: ");
            finishAnimations();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mOutRect.offset(-distanceX, -distanceY);
            mListener.onRectUpdate(this);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mOutRect.round(tmpOutRect);
            mScroller.fling(tmpOutRect.left, tmpOutRect.top, Math.round(velocityX), Math.round(velocityY),
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

            mScrollAnimator.start();
            return true;
        }


        /* (non-Javadoc)
         * @see android.view.GestureDetector.OnDoubleTapListener
         */

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.v(TAG, "onDoubleTap: ");
            return true;
        }

        private boolean mDoubleTapConfirmed;

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.v(TAG, "onDoubleTapEvent: " + e);
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDoubleTapConfirmed = true;
                    break;
                case MotionEvent.ACTION_UP:
                    if (mDoubleTapConfirmed) {
                        RectF tapZoomRect = mListener.onDoubleTapZoom(this, e.getX(), e.getY());
                        zoomToRect(tapZoomRect);
                    }
                    break;
            }
            return true;
        }

        void zoomToRect(RectF toRect) {
            if (toRect == null)
                return;
            finishAnimations();
            mRectAnimator = ValueAnimator.ofObject(mRectFEvaluator, mOutRect, toRect);
            mRectAnimator.addUpdateListener(this);
            mRectAnimator.start();
        }

        /* (non-Javadoc)
         * @see android.view.ScaleGestureDetector.OnScaleGestureListener
         */

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale(mOutRect, detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            mListener.onRectUpdate(this);
            return true;
        }

        public static void scale(RectF rect, float scale, float focusX, float focusY) {
            Log.v(TAG, "scale: " + scale);
            rect.offset(-focusX, -focusY);
            rect.set(rect.left * scale,
                    rect.top * scale,
                    rect.right * scale,
                    rect.bottom * scale);
            rect.offset(focusX, focusY);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            Log.v(TAG, "onScaleBegin: ");
            mDoubleTapConfirmed = false;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }

        /* (non-Javadoc)
         * @see android.animation.ValueAnimator.AnimatorUpdateListener#onAnimationUpdate(android.animation.ValueAnimator)
         */

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            if (animation == mRectAnimator) {
                // mRectAnimator updates mOutRect directly
                // otherwise this is how to obtain the animated rect :
                // RectF updatedRect = (RectF) animation.getAnimatedValue();
            } else if (mScroller.computeScrollOffset()) {
                mOutRect.offsetTo(mScroller.getCurrX(), mScroller.getCurrY());
            } else {
                animation.cancel();
                return;
            }
            mListener.onRectUpdate(this);
        }

    }

}
