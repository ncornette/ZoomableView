package com.zoomableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Transformation;

/**
 * 
 */
public class DepMapView extends View {

    protected static final String TAG = DepMapView.class.getSimpleName();

    protected static Boolean DEBUG = false;
    private Paint debugPaint;

    private Bitmap map;
    private Paint mapPaint;
    protected RectF rectMapOrigin = new RectF();
    protected RectF rectView = new RectF();
    protected Matrix matrixOrigin = new Matrix();
    protected Matrix matrix = new Matrix();
    protected Boolean scaling = null;
    private RectF tmpRect = new RectF();

    private boolean mAutoZoomFill;
    private float mAutoZoomLevel;
    private boolean mMaxZoomFill;
    private float mMaxZoomLevel;

    private Transformation transform;
    protected boolean zoomed;
    protected MapScaleAnim mapScaleAnim;

    Handler mapZoomHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                onAnimationStart();
            }
            if (!mapScaleAnim.hasEnded()) {
                this.sendEmptyMessageDelayed(1, 40);
                mapScaleAnim.getTransformation(System.currentTimeMillis(), transform);
                matrix.set(transform.getMatrix());
                invalidate();
            } else {
                onAnimationEnd();
                if (DEBUG) {
                    Log.v(TAG, "Animation End");
                    invalidate();
                }
            }
        }
    };

    public DepMapView(Context context) {
        this(context, null);
    }

    public DepMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DepMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mapPaint = new Paint();
        mapPaint.setFilterBitmap(true);
        transform = new Transformation();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.com_zoomableview_DepMapView, defStyle, 0);
        int resourceId = a.getResourceId(R.styleable.com_zoomableview_DepMapView_mapref, 0);
        a.recycle();

        if (resourceId != 0) {
            map = BitmapFactory.decodeResource(getResources(), resourceId);
        }

        mAutoZoomLevel = a.getFloat(R.styleable.com_zoomableview_DepMapView_autoZoomLevel, 2f);
        mAutoZoomFill = a.getBoolean(R.styleable.com_zoomableview_DepMapView_autoZoomFill, mAutoZoomLevel < 0);

        mMaxZoomLevel = a.getFloat(R.styleable.com_zoomableview_DepMapView_maxZoomLevel, 3f);
        mMaxZoomFill = a.getBoolean(R.styleable.com_zoomableview_DepMapView_maxZoomFill, mMaxZoomLevel < 0);

    }

    /**
     * Sets the image resource to be displayed for the map
     * 
     * @param resId id of a drawable
     */
    public void setMap(int resId) {
        this.map = BitmapFactory.decodeResource(getResources(), resId);
        requestLayout();
    }

    /**
     * Sets the image to be displayed for the map
     * 
     * @param bmp Bitmap to show
     */
    public void setMap(Bitmap bmp) {
        this.map = bmp;
        requestLayout();
    }

    /**
     * Returns whether a map is set to this view
     * 
     * @return whether a map is set to this view
     */
    public boolean hasMap() {
        return this.map != null;
    }

    public void zoomOnScreen(float x, float y) {
        mapScaleAnim = new MapScaleAnim(matrixOrigin, x, y, getWidth() / 2, getHeight() / 2, getAutoZoomLevel(), 500);
        mapScaleAnim.initialize((int) rectMapOrigin.width(), (int) rectMapOrigin.height(), getWidth(), getHeight());
        mapScaleAnim.start();
        Message.obtain(mapZoomHandler, 0).sendToTarget();
        zoomed = true;
    }

    public void zoomOut() {
        zoomOut(500);
    }

    public void zoomOut(int duration) {
        mapScaleAnim = new MapScaleAnim(matrix, matrixOrigin, duration);
        mapScaleAnim.initialize((int) rectMapOrigin.width(), (int) rectMapOrigin.height(), getWidth(), getHeight());
        mapScaleAnim.start();
        Message.obtain(mapZoomHandler, 0).sendToTarget();
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

    private float getFillBorderZoomLevel() {
        matrixOrigin.mapRect(tmpRect, rectMapOrigin);
        return rectView.width() / tmpRect.width() * rectView.height() / tmpRect.height();
    }

    /**
     * @return zoom level for auto zoom request or Max zoom allowed.
     */
    protected float getAutoZoomLevel() {
        if (mAutoZoomFill) {
            return Math.min(getMaxZoomLevel(), getFillBorderZoomLevel());
        } else {
            return mAutoZoomLevel;
        }
    }

    /**
     * @return max allowed zoom level.
     */
    protected float getMaxZoomLevel() {
        if (mMaxZoomFill) {
            return getFillBorderZoomLevel();
        } else {
            return mMaxZoomLevel;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (map == null)
            return;
        rectMapOrigin.set(0f, 0f, map.getWidth(), map.getHeight());
        rectView.set(0f, 0f, getWidth(), getHeight());
        matrixOrigin.setRectToRect(rectMapOrigin, rectView, ScaleToFit.CENTER);
        if (matrix.isIdentity()) {
            matrix.set(matrixOrigin);
        }
        if (DEBUG) debugPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.concat(matrix);

        // Draw full map
        if (map != null)
            canvas.drawBitmap(map, 0, 0, mapPaint);

    }
    
    protected void debugRect(Canvas canvas, RectF rect, int c) {
        debugPaint.setColor(c);
        debugPaint.setStyle(Style.STROKE);
        debugPaint.setStrokeWidth(5);
        canvas.drawRect(rect, debugPaint);
    }
    
    protected void debugRect(Canvas canvas, Rect rect, int c) {
        debugPaint.setColor(c);
        debugPaint.setStyle(Style.STROKE);
        debugPaint.setStrokeWidth(5);
        canvas.drawRect(rect, debugPaint);
    }

    /**
     * 
     * @return whether the left edge of the image view touches the left edge of its container
     * @deprecated use {@link DepMapViewTouchable#setOverScrollListener(com.zoomableview.DepMapViewTouchable.OverScrollListener)} instead
     */
    @Deprecated
    public boolean isFullLeft() {
        // TODO
        return false;
    }

    /**
     * 
     * @return whether the right edge of the image view touches the right edge of its container
     * @deprecated use {@link DepMapViewTouchable#setOverScrollListener(com.zoomableview.DepMapViewTouchable.OverScrollListener)} instead
     */
    @Deprecated
    public boolean isFullRight() {
        // TODO
        return true;
    }

}
