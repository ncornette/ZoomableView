package com.zoomableview.scroller;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Continue scrolling according to a translate factor
 * @author nic
 *
 */
public class ScrollOver implements ScrollDelegate {

    private final float mOverScrollRate;

    public ScrollOver(float overScrollRate) {
        this.mOverScrollRate = overScrollRate;
    }

    @Override
    public boolean onScrollX(RectF rectMap, RectF rectView, float distance, Matrix matrix) {
        distance *= mOverScrollRate;
        matrix.postTranslate(-distance, 0);
        return true;
    }

    @Override
    public boolean onScrollY(RectF rectMap, RectF rectView, float distance, Matrix matrix) {
        distance *= mOverScrollRate;
        matrix.postTranslate(0, -distance);
        return true;
    }

}
