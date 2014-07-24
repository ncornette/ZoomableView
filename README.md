ZoomableView
============

A base view implementing Zoom, Scale, Scroll, Fling

![ZoomView Anim](https://github.com/ncornette/ZoomableView/raw/master/demo/anim.gif)


### How-to : 

* Insert the view in your layout : 

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res/com.zoomableview"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >

    <com.zoomableview.ZoomViewScalable
        android:id="@+id/zoomable"
        app:mapref="@drawable/chicken_cartoon_500"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</RelativeLayout>
```

* Define a static picture to display by using `mapref` attribute : 

```xml
app:mapref="@drawable/chicken_cartoon_500"
```

* Grab the instance of `ZoomView` from you activity to control the zoom level, etc...

```java
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ZoomView zoomable = (ZoomViewTouchable) findViewById(R.id.zoomable);
    }
```

* Instead of using `mapref` attribute, you can provide your own bitmap, so you can draw through its canvas : 

```java
Bitmap bmp;
...
zoomable.setMap(bmp)
```

### Attributes :

Zoom view can also handle some extra attributes : 

```xml
        <!-- Attributes for ZoomView -->
        <!-- Can only be controlled by java code, no touch events handled-->
        <attr name="mapref" format="reference" /> <!-- background picture, needed to set view size -->
        <attr name="autoZoomFill" format="boolean" /> <!-- set default zoom to fill view port -->
        <attr name="autoZoomLevel" format="float" /> <!-- set default zoom to custom level -->
        <attr name="maxZoomFill" format="boolean" /> <!-- set max zoom to fill view port -->
        <attr name="maxZoomLevel" format="float" /> <!-- set max zoom to custom level -->

        <!-- Attributes for ZoomViewTouchable -->
        <!-- Only double tab and fling events are handled, no multi-touch events handled -->
        <attr name="doubletabZoom" format="boolean" /> <!-- Use to disable doubleTap Zoom -->
        <attr name="flingScale" format="float" /> <!-- 1: very long fling, 10: very short fling -->
        <attr name="overScrollTranslateFactor" format="float" /> <!-- 1: keep scroll, 0: no overscroll, 0.3: default -->

        <!-- Attributes for ZoomViewScalable -->
        <!-- Multi-touch scale events handled -->
        <attr name="minScaleFactor" format="float" /> <!-- min scalable zoom level -->
        <attr name="maxScaleFactor" format="float" /> <!-- max scalable zoom level -->

```