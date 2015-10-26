package com.rnfstudio.testticker;

import android.app.Activity;
import android.os.*;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity {
    private static long startTime;
    private static Handler sTimerHandler;
    private static Runnable sTimerRunnable;
    private static TextView mTv;

    static {
        //runs without a timer by reposting this handler at the end of the runnable
        startTime = 0;
        sTimerHandler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = (TextView) findViewById(R.id.tv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startTimeTicker() {
        if (sTimerRunnable == null) {
            sTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    long millis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;

                    mTv.setText(String.format("%02d:%02d", minutes, seconds));

                    // trigger next time tick in main thread
                    sTimerHandler.postDelayed(this, 1000);
                }
            };
        }
        // start the first time tick
        sTimerHandler.postDelayed(sTimerRunnable, 0);
    }

    public void stopTimeTicker() {
        // remove timer
        sTimerHandler.removeCallbacks(sTimerRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimeTicker();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeTicker();
    }
}
