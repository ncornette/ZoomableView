package com.zoomableview.events;


public interface ScaleListenerCompat {

    public boolean onScale(float scaleFactor, float focusX, float focusY);

    public boolean onScaleBegin(float scaleFactor, float focusX, float focusY);

    public void onScaleEnd(float scaleFactor, float focusX, float focusY);

}
