package com.rnfstudio.babytracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.db.EventDB;
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

    private static final String TAG_MILK_PICKER_DIALOG = "MilkPickerDialogFragment";

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
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private final Context mContext;
    private List<SwipeButton> mButtons = new ArrayList<SwipeButton>();
    private Map<String, Calendar> mTimerMap = new HashMap<>();
    private TextView mLogView;
    private Handler mMainHandler;
    private ViewGroup mMenuPanel;
    private ViewGroup mCounterPanel;
    private Button mStopButton;
    private ViewGroup mLastInfoPanel;
    private ViewGroup mInfoPanel;
    private SwipeButton mSleepButton;
    private SwipeButton mMealButton;
    private SwipeButton mDiaperButton;

    private LastInfo mLastInfo;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public SwipeButtonHandler(Context context) {
        mContext = context;
        mMainHandler = new Handler(context.getMainLooper());
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
//                    Intent pickAmount = new Intent(context, MilkPicker.class);
//                    ((Activity) mContext).startActivityForResult(pickAmount, MainActivity.REQUEST_CODE_PICK_MILK_AMOUNT);
                    // start MilkPicker dialog
                    // this will later trigger onMilkPickerResult() if user confirms the amount
                    DialogFragment newFragment = MilkPickerDialogFragment.newInstance(id);
                    newFragment.show(((Activity) mContext).getFragmentManager(), TAG_MILK_PICKER_DIALOG);
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
                break;

            case MENU_ITEM_SETTINGS:
//                asyncClearDB(context);
                break;

            case MENU_ITEM_STATS:
                Intent viewRecords = new Intent(mContext, RecordListActivity.class);
                mContext.startActivity(viewRecords);
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

        if (DEBUG) Log.v(TAG, "stopTime: " + TimeUtils.flattenEventTime(end));
        if (DEBUG) Log.v(TAG, "amount: " + amount);
    }

    private void asyncClearDB(final Context context) {
        final EventDB db = MainApplication.getEventDatabase(context);
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                db.clearEvents();
                asyncRefreshLastInfo(context);
            }
        });
    }

    private void asyncWriteDB(final Context context, final String eventType, final Calendar startTime, final Calendar endTime, final int amount) {
        final EventDB db = MainApplication.getEventDatabase(context);
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                db.addEvent(eventType, startTime, endTime, amount);
                asyncRefreshLastInfo(context);
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

    public void setLogView(TextView logView) {
        mLogView = logView;
        mLogView.setText("LogView init complete");
    }

    public void refreshLogView(Context context) {
        Log.v(TAG, "refreshLogView called");
        if (mLogView != null) {
            EventDB db = MainApplication.getEventDatabase(context);
            List<String> outputs = db.queryLatestEvent(20);
            StringBuilder sb = new StringBuilder();
            for (String s : outputs) {
                sb.append(s);
            }
            mLogView.setText(sb.toString());
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

    public void setMenuPanel(ViewGroup menu) {
        mMenuPanel = menu;
    }

    public void setLastInfoPanel(ViewGroup panel) {
        mLastInfoPanel = panel;
    }

    public void asyncRefreshLastInfo(final Context context) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                EventDB db = MainApplication.getEventDatabase(context);

                Calendar lastSleep = db.queryLatestTimeForType(EventContract.EventEntry.EVENT_TYPE_SLEEP);
                Calendar lastMeal = db.queryLatestTimeForType(EventContract.EventEntry.EVENT_TYPE_MEAL);
                Calendar lastDiaper = db.queryLatestTimeForType(EventContract.EventEntry.EVENT_TYPE_DIAPER);
                mLastInfo = new LastInfo(lastSleep, lastMeal, lastDiaper);

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshLastInfo();
                    }
                });
            }
        });
    }

    public void refreshLastInfo() {
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

        if (mSleepButton != null) {
            mSleepButton.setDetail(mLastInfo.getLastSleepMessage(mContext));
        }
        if (mMealButton != null) {
            mMealButton.setDetail(mLastInfo.getLastMealMessage(mContext));
        }
        if (mDiaperButton != null) {
            mDiaperButton.setDetail(mLastInfo.getLastDiaperMessage(mContext));
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

                    asyncRefreshLastInfo(mContext);
                    refreshCounters();
                }
            };
        }
        // start the first time tick
        sTimerHandler.postDelayed(sTimerRunnable, 0);
    }

    private void refreshBirthDays() {
        TextView daysFromBirth = (TextView) mInfoPanel.findViewById(R.id.daysFromBirth);
        if (daysFromBirth != null) daysFromBirth.setText(getDaysFromBirthString());
    }

    private void refreshCounters() {
        Calendar now = Calendar.getInstance();

        for (String id : getTimerIds()) {
            long diffInSecs = TimeUtils.secondsBetween(getStartTime(id), now);
            int hours = TimeUtils.getRemainHours(diffInSecs);
            int minutes = TimeUtils.getRemainMinutes(diffInSecs);
            int seconds = TimeUtils.getRemainSeconds(diffInSecs);

            getSwipeButtonById(id).setCounterText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    public void stopTimeTicker() {
        // remove timer
        sTimerHandler.removeCallbacks(sTimerRunnable);
    }

    public void setInfoPanel(ViewGroup panel) {
        mInfoPanel = panel;
    }

    public void refreshAll() {
        refreshBirthDays();
        refreshCounters();
        asyncRefreshLastInfo(mContext);
    }

    private String getDaysFromBirthString() {
        Calendar birth = Calendar.getInstance();
        birth.set(Calendar.YEAR, 2015);
        birth.set(Calendar.MONTH, Calendar.MARCH );
        birth.set(Calendar.DAY_OF_MONTH, 18);

        int daysBetween = TimeUtils.daysBetween(birth, Calendar.getInstance());
        int days = TimeUtils.getRemainDaysInMonth(daysBetween);
        int months = TimeUtils.getRemainMonthsInYear(daysBetween);
        int years = TimeUtils.getRemainYears(daysBetween);

        if (years > 0) {
            return mContext.getResources().getQuantityString(R.plurals.info_years_since_birth, years, years, months, days);
        } else if (months > 0) {
            return mContext.getResources().getQuantityString(R.plurals.info_months_since_birth, months, months, days);
        } else if (days > 0) {
            return mContext.getResources().getQuantityString(R.plurals.info_days_since_birth, days, days);
        } else {
            return mContext.getResources().getString(R.string.last_info_default_message);
        }
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
