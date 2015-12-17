package com.rnfstudio.babytracker;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.db.EventDB;
import com.rnfstudio.babytracker.utility.CircleView;
import com.rnfstudio.babytracker.utility.CircleWidget;
import com.rnfstudio.babytracker.utility.MilkPickerDialogFragment;
import com.rnfstudio.babytracker.utility.SwipeButton;
import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roger on 2015/11/30.
 */
public class MainFragment extends Fragment {
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
    private CircleWidget mCircleWidget;

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

        ViewGroup infoPanel = (ViewGroup) rootView.findViewById(R.id.infoPanel);
        mManager.setInfoPanel(infoPanel);

        mCircleWidget = new CircleWidget(getActivity());
        mCircleWidget.setCircle((CircleView) rootView.findViewById(R.id.circle));
        mCircleWidget.setInfoPanel((TextView) rootView.findViewById(R.id.circleTitle));

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

        // TODO: 1. create inner class and use composition for data adapter
        // 2. create manager or loader class for this type of querying
        // 3. load event, store event, calculate time segments, and set data to circle view
        // 4. remember to use composition when i want to add features to the original circle view
        new AsyncTask<Void, Void, List<Pair<Float, Float>>>() {

            @Override
            protected List<Pair<Float, Float>> doInBackground(Void... params) {
                EventDB db = MainApplication.getEventDatabase(getActivity());
                List<Pair<Float, Float>> dataPairs = new ArrayList<>();


                long queryStart = TimeUtils.isNowAM() ? TimeUtils.getTodayAMStartMillis() :
                            TimeUtils.getTodayPMStartMillis();
                long queryEnd = queryStart + 43200000;
                long queryAhead = queryStart - 43200000;

                try (
                    Cursor cursor = db.queryEventsForMainTypeAndPeriod(EventContract.EventEntry.EVENT_TYPE_SLEEP,
                            queryAhead,
                            queryEnd)) {

                    Log.w(TAG, "start quering data");
                    if (cursor == null) {
                        Log.w(TAG, "fail to query events for circle view");
                        return null;
                    }

                    while (cursor.moveToNext()) {
                        long startTime = cursor.getLong(EventContract.EventQuery.EVENT_START_TIME);
                        long endTime = cursor.getLong(EventContract.EventQuery.EVENT_END_TIME);

                        // only interested in today's part
                        if (startTime < queryStart) {
                            startTime = queryStart;
                        }
                        if (endTime > queryEnd) {
                            endTime = queryEnd;
                        }

                        Float startOffset = ((float) startTime - queryStart) / 43200000 * 360;
                        Float endOffset = ((float) endTime - queryStart) / 43200000 * 360;

                        dataPairs.add(new Pair<>(startOffset, endOffset));
                    }
                }

                return dataPairs;
            }

            @Override
            protected void onPostExecute(List<Pair<Float, Float>> circleViewData) {
                super.onPostExecute(circleViewData);
                Log.w(TAG, "end quering data, " + circleViewData);
                mCircleWidget.setCircleData(circleViewData);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        Log.v(TAG, "[onActivityResult] requestCode: " + requestCode + ", resultCode: " + resultCode + "data: " + data);

        String functionId = data.getStringExtra(MilkPickerDialogFragment.EXTRA_FUNCTION_ID);
        int amountInML = data.getIntExtra(MilkPickerDialogFragment.EXTRA_MILLI_LITER, 0);

        Log.v(TAG, String.format("[onActivityResult] Receive function: %s, amount: %d", functionId, amountInML));

        if (requestCode == REQUEST_CODE_SET_AMOUNT) {
            mManager.onMilkPickerResult(functionId, amountInML);
        }
    }
}
