package com.rnfstudio.babytracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.db.EventProvider;
import com.rnfstudio.babytracker.utility.MilkPickerDialogFragment;
import com.rnfstudio.babytracker.utility.SwipeButton;
import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Roger on 2015/7/17.
 */
public class SwipeButtonHandler implements SwipeButton.Handler {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "SwipeButtonHandler";
    private static final boolean DEBUG = true;

    public static final String MENU_ITEM_SLEEP = "SLEEP";
    public static final String MENU_ITEM_MEAL_TYPE_BREAST_BOTH = "MEAL_TYPE_BREAST_BOTH";
    public static final String MENU_ITEM_MEAL_TYPE_BREAST_LEFT = "MEAL_TYPE_BREAST_LEFT";
    public static final String MENU_ITEM_MEAL_TYPE_BREAST_RIGHT = "MEAL_TYPE_BREAST_RIGHT";
    public static final String MENU_ITEM_MEAL_BOTTLED = "MEAL_TYPE_BOTTLED";
    public static final String MENU_ITEM_MEAL_MILK = "MEAL_TYPE_MILK";
    public static final String MENU_ITEM_DIAPER_PEEPEE = "DIAPER_PEEPEE";
    public static final String MENU_ITEM_DIAPER_POOPOO = "DIAPER_POOPOO";
    public static final String MENU_ITEM_DIAPER_BOTH = "DIAPER_BOTH";
    public static final String MENU_ITEM_MILESTONES = "MILESTONES";
    public static final String MENU_ITEM_MEDICATION = "MEDICATION";
    public static final String MENU_ITEM_GROW = "GROW";
    public static final String MENU_ITEM_ALARM = "ALARM";
    public static final String MENU_ITEM_STATS = "STATS";
    public static final String MENU_ITEM_SETTINGS = "SETTINGS";
    public static final String MENU_ITEM_ALL_RECORDS = "ALL_RECORDS";

    // asynchronous worker thread and handler
    private static final String sWorkerDisplayName = "SwipeButtonHandler worker";
    private static final HandlerThread sWorkerThread;
    private static final Handler sWorker;

    // refer to: http://stackoverflow.com/questions/4597690/android-timer-how
    // main thread timer for UI update
    private static long startTime;
    private static Handler sTimerHandler;
    private static Runnable sTimerRunnable;

    // state preserving
    private static final String SP_KEY_SWIPE_BUTTON_HANDLER_STATES = "sp_key_swipe_button_handler_states";
    private static final String STATE_KEY_TIMER_START_TIMES = "timer_start_times";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    static {
        sWorkerThread = new HandlerThread(sWorkerDisplayName, Process.THREAD_PRIORITY_BACKGROUND);
        sWorkerThread.start();
        sWorker = new Handler(sWorkerThread.getLooper());

        //runs without a timer by reposting this handler at the end of the runnable
        startTime = 0;
        sTimerHandler = new Handler();
    }

    // ------------------------------------------------------------------------
    // STATIC METHODSmil
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private Fragment mFragment;
    private final Context mContext;
    private List<SwipeButton> mButtons = new ArrayList<>();
    private Map<String, Calendar> mTimerMap = new HashMap<>();
    private Handler mMainHandler;
    private ViewGroup mLastInfoPanel;
    private SwipeButton mSleepButton;
    private SwipeButton mMealButton;
    private SwipeButton mDiaperButton;

    private LastInfo mLastInfo;
    private boolean mLastInfoDirty = false;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public SwipeButtonHandler(Fragment fragment) {
        mFragment = fragment;
        mContext = fragment.getActivity();
        mMainHandler = new Handler(fragment.getActivity().getMainLooper());
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    @Override
    public void OnClick(Context context, String id) {
        switch(id) {
            case MENU_ITEM_MEAL_BOTTLED:
            case MENU_ITEM_MEAL_MILK:
                if (isTimerRunning(id)) {
                    DialogFragment newFragment = new MilkPickerDialogFragment();

                    Bundle args = new Bundle();
                    args.putString(MilkPickerDialogFragment.EXTRA_FUNCTION_ID, id);
                    args.putInt(MilkPickerDialogFragment.EXTRA_DEFAULT_AMOUNT, MilkPickerDialogFragment.DEFAULT_AMOUNT);
                    newFragment.setArguments(args);

                    newFragment.setTargetFragment(mFragment, MainFragment.REQUEST_CODE_SET_AMOUNT);
                    newFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), MilkPickerDialogFragment.TAG);

                    break;
                }
            case MENU_ITEM_MEAL_TYPE_BREAST_LEFT:
            case MENU_ITEM_MEAL_TYPE_BREAST_RIGHT:
                // TODO: popup a dialog w/ 2 buttons: switch side or done
            case MENU_ITEM_MEAL_TYPE_BREAST_BOTH:
            case MENU_ITEM_SLEEP:
                if (isTimerRunning(id)) {
                    stopTimerForFuncId(id);
                } else {
                    startTimerForFuncId(id);
                }
                break;

            case MENU_ITEM_DIAPER_BOTH:
            case MENU_ITEM_DIAPER_PEEPEE:
            case MENU_ITEM_DIAPER_POOPOO:
                Calendar start = Calendar.getInstance();
                Calendar end = (Calendar) start.clone();
                end.add(Calendar.SECOND, 1);
                asyncWriteDB(context, id, start, end, EventContract.EventEntry.EMPTY_AMOUNT);
                Toast.makeText(mContext, R.string.add_successful, Toast.LENGTH_SHORT).show();
                break;

            case MENU_ITEM_SETTINGS:
                mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                break;

            case MENU_ITEM_ALL_RECORDS:
                mContext.startActivity(new Intent(mContext, RecordListActivity.class));
                break;

            default:
                break;
        }
    }

    private void startTimerForFuncId(String id) {
        Calendar startTime = Calendar.getInstance();
        startTimer(startTime, id);
        showCounter(id, true);

        if (DEBUG) Log.v(TAG, "startTime: " + TimeUtils.flattenEventTime(startTime));
    }

    private void stopTimerForFuncId(String id) {
        stopTimerForFuncId(id, EventContract.EventEntry.EMPTY_AMOUNT);
    }

    private void stopTimerForFuncId(String id, int amount) {
        Calendar start = getStartTime(id);
        Calendar end = Calendar.getInstance();
        stopTimer(id);
        asyncWriteDB(mContext, id, start, end, amount);
        showCounter(id, false);

        Toast.makeText(mContext, R.string.add_successful, Toast.LENGTH_SHORT).show();
        if (DEBUG) Log.v(TAG, "stopTime: " + TimeUtils.flattenEventTime(end));
        if (DEBUG) Log.v(TAG, "amount: " + amount);
    }

    private void asyncWriteDB(final Context context, final String eventType, final Calendar startTime, final Calendar endTime, final int amount) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                Bundle b = new Bundle();
                b.putLong(Event.EXTRA_EVENT_ID, -1);
                b.putInt(Event.EXTRA_EVENT_TYPE, EventContract.EventEntry.getMainType(eventType));
                b.putInt(Event.EXTRA_EVENT_SUBTYPE, EventContract.EventEntry.getSubType(eventType));
                b.putLong(Event.EXTRA_EVENT_START_TIME, startTime.getTimeInMillis());
                b.putLong(Event.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
                b.putLong(Event.EXTRA_EVENT_DURATION,
                        endTime.getTimeInMillis() - startTime.getTimeInMillis());
                b.putInt(Event.EXTRA_EVENT_AMOUNT, amount);

                Event.createFromBundle(b).writeDB(context, false);
            }
        });
    }

    private void stopTimer(String id) {
        mTimerMap.remove(id);
    }

    private void startTimer(Calendar startTime, String id) {
        mTimerMap.put(id, startTime);
    }

    private boolean isTimerRunning(String id) {
        return mTimerMap.get(id) != null;
    }

    private Set<String> getTimerIds() {
        return mTimerMap.keySet();
    }

    private Calendar getStartTime(String id) { return mTimerMap.get(id); }

    @Override
    public void addSwipeButton(SwipeButton btn) {
        final String id = btn.getMainFuncId();
        mButtons.add(btn);

        if (id.equals(MENU_ITEM_SLEEP)) {
            mSleepButton = btn;
        } else if (id.equals(MENU_ITEM_MEAL_TYPE_BREAST_BOTH)) {
            mMealButton = btn;
        } else if (id.equals(MENU_ITEM_DIAPER_BOTH)) {
            mDiaperButton = btn;
        }
    }

    private void showCounter(String id, boolean show) {
        SwipeButton btn = getSwipeButtonById(id);
        btn.showCounter(show);
        btn.setTimerFunc(show ? id : null);
        if (show) refreshCounters();
    }

    private SwipeButton getSwipeButtonById(String id) {
        SwipeButton btn = null;
        switch (id) {
            case MENU_ITEM_SLEEP:
                btn = mSleepButton;
                break;
            case MENU_ITEM_MEAL_TYPE_BREAST_BOTH:
            case MENU_ITEM_MEAL_TYPE_BREAST_LEFT:
            case MENU_ITEM_MEAL_TYPE_BREAST_RIGHT:
            case MENU_ITEM_MEAL_BOTTLED:
            case MENU_ITEM_MEAL_MILK:
                btn = mMealButton;
                break;
        }
        return btn;
    }

    public void setLastInfoPanel(ViewGroup panel) {
        mLastInfoPanel = panel;
    }

    public void asyncUpdateLastInfo() {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                mLastInfo =
                        new LastInfo(queryLatestTimeForMainType(mContext, EventContract.EventEntry.EVENT_TYPE_SLEEP),
                        queryLatestTimeForMainType(mContext, EventContract.EventEntry.EVENT_TYPE_MEAL),
                        queryLatestTimeForMainType(mContext, EventContract.EventEntry.EVENT_TYPE_DIAPER));

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshAll();
                    }
                });
            }
        });
    }

    /**
     * Query the latest occurrence time for events
     *
     * @param mainType: the query type for events
     * @return: the occurence time in milli-second since epoch
     */
    private long queryLatestTimeForMainType(Context context, int mainType) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(EventProvider.sMainUri,
                    new String[]{EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME},
                    EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + "=?",
                    new String[]{Integer.toString(mainType)},
                    EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " DESC");

            if (cursor != null && cursor.moveToNext()) return cursor.getLong(0);

        } finally {
            if (cursor != null) cursor.close();
        }

        return 0;
    }

    public void refreshLastInfo() {
        if (mLastInfo == null) return;

        if (mSleepButton != null) {
            mSleepButton.setDetail(mLastInfo.getLastSleepMessage(mContext));
        }
        if (mMealButton != null) {
            mMealButton.setDetail(mLastInfo.getLastMealMessage(mContext));
        }
        if (mDiaperButton != null) {
            mDiaperButton.setDetail(mLastInfo.getLastDiaperMessage(mContext));
        }

        if (mLastInfoPanel == null) return;

        TextView lastSleepDetail = (TextView) mLastInfoPanel.findViewById(R.id.last_sleep_detail);
        TextView lastMealDetail = (TextView) mLastInfoPanel.findViewById(R.id.last_meal_detail);
        TextView lastDiaperDetail = (TextView) mLastInfoPanel.findViewById(R.id.last_diaper_detail);

        if (lastSleepDetail != null) {
            lastSleepDetail.setText(mLastInfo.getLastSleepMessage(mContext));
        }
        if (lastMealDetail != null) {
            lastMealDetail.setText(mLastInfo.getLastMealMessage(mContext));
        }
        if (lastDiaperDetail != null) {
            lastDiaperDetail.setText(mLastInfo.getLastDiaperMessage(mContext));
        }
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

                    // trigger next time tick in main thread
                    sTimerHandler.postDelayed(this, 1000);

                    refreshAll();
                }
            };
        }
        // start the first time tick
        sTimerHandler.postDelayed(sTimerRunnable, 0);
    }

    private void refreshCounters() {
        for (String id : getTimerIds()) {
            long diffInSecs = TimeUtils.secondsUntilNow(getStartTime(id).getTimeInMillis());
            int hours = TimeUtils.getRemainHours(diffInSecs);
            int minutes = TimeUtils.getRemainMinutes(diffInSecs);
            int seconds = TimeUtils.getRemainSeconds(diffInSecs);

            getSwipeButtonById(id).setCounterText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    public void stopTimeTicker() {
        // remove timer
        sTimerHandler.removeCallbacks(sTimerRunnable);

        // reset runnable
        // this MUST be done to unbind old handler and views
        sTimerRunnable = null;
    }

    public void refreshAll() {
        refreshCounters();
        refreshLastInfo();
    }

    public void saveStates() {
        Set<String> flattenTimers = new HashSet<>();
        for (String id : getTimerIds()) {
            String time  = TimeUtils.flattenEventTime(getStartTime(id));
            flattenTimers.add(String.format("%s\t%s", id, time));
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putStringSet(STATE_KEY_TIMER_START_TIMES, flattenTimers);
        editor.commit();
    }

    public void restoreStates() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> startTimes = prefs.getStringSet(STATE_KEY_TIMER_START_TIMES, new HashSet<String>());

        for (String startTimeStr : startTimes) {
            String[] tokens = startTimeStr.split("\t");
            Calendar startTime = TimeUtils.unFlattenCalendarTimeSafely(tokens[1], EventContract.EventEntry.SIMPLE_DATE_TIME_FORMAT);
            String id = tokens[0];
            startTimer(startTime, id);
            showCounter(id, true);
        }
    }

    public void onMilkPickerResult(String id, int amount) {
        stopTimerForFuncId(id, amount);
    }
}
