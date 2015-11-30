package com.rnfstudio.babytracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;


public class MainActivity extends Activity {
    public static final String TAG = "[MainActivity]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate called");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
    }

}
