package com.zoomableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author nic A View that draws a Map and can show departments on top of it, the department can
 *         blink and the related region can be displayed. Department capitals can be displayed too.
 */
public class DepMapView extends View {

    private Bitmap map;
    private Paint mapPaint;
    protected RectF rectMapOrigin = new RectF();
    protected RectF rectView = new RectF();
    protected Matrix matrixOrigin = new Matrix();
    protected Matrix matrix = new Matrix();

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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        rectMapOrigin.set(0f, 0f, map.getWidth(), map.getHeight());
        rectView.set(0f, 0f, getWidth(), getHeight());
        matrixOrigin.setRectToRect(rectMapOrigin, rectView, ScaleToFit.CENTER);
        if (matrix.isIdentity()) {
            matrix.set(matrixOrigin);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.concat(matrix);

        // Draw full map
        canvas.drawBitmap(map, 0, 0, mapPaint);

        // // Debug clip Bounds
        // Paint clipPaint = new Paint();
        // clipPaint.setColor(Color.RED);
        // clipPaint.setStyle(Style.STROKE);
        // clipPaint.setStrokeWidth(5);
        // canvas.drawRect(canvas.getClipBounds(), clipPaint);

    }

}
