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
import android.util.AttributeSet;
import android.view.View;

/**
 * @author nic A View that draws a Map and can show departments on top of it, the department can
 *         blink and the related region can be displayed. Department capitals can be displayed too.
 */
public class DepMapView extends View {

    protected static Boolean DEBUG = false;

    private Bitmap map;
    private Paint mapPaint;
    protected RectF rectMapOrigin = new RectF();
    protected RectF rectView = new RectF();
    protected Matrix matrixOrigin = new Matrix();
    protected Matrix matrix = new Matrix();
    protected Boolean scaling = null;
    private Paint debugPaint;

    public DepMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mapPaint = new Paint();
        mapPaint.setFilterBitmap(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.com_zoomableview_DepMapView);
        int resourceId = a.getResourceId(R.styleable.com_zoomableview_DepMapView_mapref, 0);
        a.recycle();

        if (resourceId != 0) {
            map = BitmapFactory.decodeResource(getResources(), resourceId);
        }
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
