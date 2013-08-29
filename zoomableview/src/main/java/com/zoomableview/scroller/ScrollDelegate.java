package com.zoomableview.scroller;

import android.graphics.Matrix;
import android.graphics.RectF;

public interface ScrollDelegate {
    public boolean onScrollX(RectF content, RectF container, float distance, Matrix matrix);
    public boolean onScrollY(RectF content, RectF container, float distance, Matrix matrix);
}
