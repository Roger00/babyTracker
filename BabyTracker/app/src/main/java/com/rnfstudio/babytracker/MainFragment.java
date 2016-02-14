package com.rnfstudio.babytracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rnfstudio.babytracker.utility.MilkPickerDialogFragment;
import com.rnfstudio.babytracker.utility.SwipeButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roger on 2015/11/30.
 */
public class MainFragment extends Fragment implements OnEventChangedListener {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------


    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String TAG = "[MainFragment]";

    public static final int REQUEST_CODE_SET_AMOUNT = 0;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView called");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mManager = new SwipeButtonHandler(this);
        ViewGroup menu = (ViewGroup) rootView.findViewById(R.id.main_menu);

        for (SwipeButton btn : getSwipeButtons(menu)) {
            btn.setHandler(mManager);
        }

        ViewGroup lastInfoPanel = (ViewGroup) rootView.findViewById(R.id.lastInfoPanel);
        mManager.setLastInfoPanel(lastInfoPanel);

        return rootView;
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
    public void onStart() {
        super.onStart();
        mManager.restoreStates();
    }

    @Override
    public void onStop() {
        super.onStop();
        mManager.saveStates();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(TAG, "[onActivityResult] requestCode: " + requestCode + ", resultCode: " + resultCode
                + "data: " + data);

        String functionId = data.getStringExtra(MilkPickerDialogFragment.EXTRA_FUNCTION_ID);
        int amountInML = data.getIntExtra(MilkPickerDialogFragment.EXTRA_MILLI_LITER, 0);

        Log.v(TAG, String.format("[onActivityResult] Receive function: %s, amount: %d",
                functionId, amountInML));

        if (requestCode == REQUEST_CODE_SET_AMOUNT) {
            mManager.onMilkPickerResult(functionId, amountInML);
        }
    }

    @Override
    public void onEventChanged(int mainType) {
        Log.v(TAG, "[onEventChanged] refresh all for event change, mainType: " + mainType);
        mManager.refreshAll();
    }

    public void notifyEventChanged(int mainType) {
        Log.v(TAG, "[notifyEventChanged] notify event change, mainType: " + mainType);
        ((MainActivity)getActivity()).onEventChanged(mainType);
    }
}
