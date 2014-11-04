package com.zoomableview.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class HelloAndroidActivity extends Activity {

    protected static final String TAG = HelloAndroidActivity.class.getSimpleName();

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

        // @SuppressWarnings("unused")
//        final ZoomViewTouchable zoomable = (ZoomViewTouchable) findViewById(R.id.zoomable);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
