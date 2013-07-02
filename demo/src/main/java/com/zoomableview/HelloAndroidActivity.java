package com.zoomableview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.zoomableview.DepMapViewTouchable.TouchMapListener;

public class HelloAndroidActivity extends Activity {

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DepMapViewScalable zoomable = (DepMapViewScalable) findViewById(R.id.zoomable);
        zoomable.setTouchMapListener(new TouchMapListener() {

            @Override
            public void onTouchScale(float arg0, float arg1, float arg2) {
            }

            @Override
            public void onTouchDep(float arg0, float arg1) {
            }

            @Override
            public void onSingleTapConfirmed() {
            }

            @Override
            public void onSingleTapCancelled() {
            }

            @Override
            public void onDoubleTap(float arg0, float arg1) {
                if (zoomable.isZoomed()) {
                    zoomable.zoomOut();
                } else {
                    zoomable.zoomOnScreen(arg0, arg1);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(com.zoomableview.R.menu.main, menu);
	return true;
    }

}

