package com.rnfstudio.babytracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridView;
import android.widget.TextView;

import com.rnfstudio.babytracker.utility.SwipeButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String PACKAGE_NAME = "com.rnfstudio.babytracker";
    public static final int REQUEST_CODE_PICK_MILK_AMOUNT = 0;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private SwipeButtonHandler mManager;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mManager = new SwipeButtonHandler(this);
        ViewGroup menu = (ViewGroup) findViewById(R.id.main_menu);
        mManager.setMenuPanel(menu);
        for (SwipeButton btn : getSwipeButtons(menu)) {
            btn.setHandler(mManager);
        }

//        ViewGroup counterPanel = (ViewGroup) findViewById(R.id.counterPanel);
//        mManager.setCounterPanel(counterPanel);

//        TextView logView = (TextView) findViewById(R.id.logView);
//        mManager.setLogView(logView);
//        mManager.refreshLogView(this);

        ViewGroup lastInfoPanel = (ViewGroup) findViewById(R.id.lastInfoPanel);
        mManager.setLastInfoPanel(lastInfoPanel);

        ViewGroup infoPanel = (ViewGroup) findViewById(R.id.infoPanel);
        mManager.setInfoPanel(infoPanel);
    }

    private List<SwipeButton> getSwipeButtons(View v) {
        List<SwipeButton> ret = new ArrayList<>();

        if (v instanceof SwipeButton) {
            ret.add((SwipeButton) v);

        } else if (v instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                ret.addAll(getSwipeButtons(((ViewGroup) v).getChildAt(i)));
            }
        }
        return ret;
    }

    @Override
    public void onResume() {
        super.onResume();
        mManager.startTimeTicker();
        mManager.refreshAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        mManager.stopTimeTicker();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager.restoreStates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mManager.saveStates();
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

    public void onMilkPickerResult(String id, int amount) {
        mManager.onMilkPickerResult(id, amount);
    }
}
