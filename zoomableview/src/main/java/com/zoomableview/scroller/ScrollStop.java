package com.zoomableview.scroller;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Stop scrolling when content is inside container
 * @author nic
 *
 */
public class ScrollStop implements ScrollDelegate {

    @Override
    public boolean onScrollX(RectF content, RectF container, float distance, Matrix matrix) {
        if (content.width() > container.width()) {
            if (distance < 0) {
                distance = content.left - container.left;
            } else {
                distance = content.right - container.right;
            }
            matrix.postTranslate(-distance, 0);
        }
        return false;
    }

    @Override
    public boolean onScrollY(RectF content, RectF container, float distance, Matrix matrix) {
        if (content.height() > container.height()) {
            if (distance < 0) {
                distance = content.top - container.top;
            } else {
                distance = content.bottom - container.bottom;
            }
            matrix.postTranslate(0, -distance);
        }
        return false;
    }

}
