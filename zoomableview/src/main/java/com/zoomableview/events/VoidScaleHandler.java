package com.zoomableview.events;

import android.view.MotionEvent;

public class VoidScaleHandler implements MotionEventHandler {

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        return false;
    }

}
