package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.db.EventContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roger on 2015/12/16.
 */
public class CircleWidget implements CircleView.OnCircleTouchListener {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[CircleWidget]";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private Context mContext;
    private CircleView mCircle;
    private TextView mInfoPanel;
    private List<Event> mEvents = new ArrayList<>();
    private long mTotalDuration;
    private int mTotalAmount;
    private int mType;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public CircleWidget(Context context) {
        mContext = context;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public void setCircle(CircleView circle) {
        if (circle != null) {
            mCircle = circle;
            circle.addListener(this);
        }
    }

    public void setInfoPanel(TextView infoPanel) {
        if (infoPanel != null) {
            mInfoPanel = infoPanel;
        }
    }

    public void setEvents(Cursor cursor) {
        if (cursor == null) {
            Log.w(TAG, "fail to query events for circle view");
            return;
        }

        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            events.add(Event.createFromCursor(cursor));
        }

        // reset cursor to head, this is necessary for we to read from cursor again
        cursor.moveToPosition(-1);

        setEvents(events);
    }

    public void setEvents(List<Event> events) {
        mEvents = events;

        // update data in CircleView
        setCircleData();

        // update total duration of events
        updateTotalDuration();

        // update info panel
        mInfoPanel.setText(getDefaultMessage());
    }

    public void setMainType(int type) {
        mType = type;
    }

    public int getMainType() {
        return mType;
    }

    @Override
    public void onCircleTouch(int index) {
        String message = index < 0 ? getDefaultMessage() : getHighlightEventInfo(index);
        mInfoPanel.setText(message);
    }

    public void setCircleData() {
        if (mCircle != null) {

            List<Pair<Float, Float>> dataPairs = new ArrayList<>();
            long queryStart = getQueryStartTime();
            long queryEnd = getQueryEndTime();

            for (Event event : mEvents) {
                long startTime = event.getStartTime();
                long endTime = event.getEndTime();

                // only interested in today's part
                if (startTime < queryStart) {
                    startTime = queryStart;
                }

                if (endTime > queryEnd) {
                    endTime = queryEnd;
                }

                Float startOffset = ((float) startTime - queryStart) / TimeUtils.HALF_DAY_IN_MILLIS * 360;
                Float endOffset = ((float) endTime - queryStart) / TimeUtils.HALF_DAY_IN_MILLIS * 360;

                dataPairs.add(new Pair<>(startOffset, endOffset));
            }

            mCircle.setData(dataPairs);
        }
    }

    public static long getQueryStartTime() {
        return TimeUtils.isNowAM() ? TimeUtils.getTodayAMStartMillis() :
                TimeUtils.getTodayPMStartMillis();
    }

    public static long getQueryEndTime() {
        return getQueryStartTime() + TimeUtils.HALF_DAY_IN_MILLIS;
    }

    public static long getQueryAheadTIme() {
        return getQueryStartTime() - TimeUtils.HALF_DAY_IN_MILLIS;
    }

    public String getDefaultMessage() {
        int cEvents = mEvents.size();

        if (cEvents > 0) {
            // sum of today's events
            String durationStr = Event.getDisplayDuration(mContext, mTotalDuration);
            String noonStr = TimeUtils.isNowAM() ? mContext.getString(R.string.circle_info_am) :
                    mContext.getString(R.string.circle_info_pm);

            switch (getMainType()) {
                case EventContract.EventEntry.EVENT_TYPE_MEAL:
                    return mContext.getString(R.string.circle_info_default_meal,
                            noonStr, cEvents, durationStr, mTotalAmount);

                case EventContract.EventEntry.EVENT_TYPE_DIAPER:
                    return mContext.getString(R.string.circle_info_default_diaper,
                            noonStr, cEvents);
                default:
                    return mContext.getString(R.string.circle_info_default_sleep,
                            noonStr, cEvents, durationStr);
            }

        } else {
            // add new records to start
            return mContext.getString(R.string.circle_info_default_add_data_hint);
        }

    }

    public String getHighlightEventInfo(int index) {
        // 1. look up associated event using data index
        Event highlight = mEvents.get(index);

        // 2. update info panel
        String type = highlight.getDisplayType(mContext);
        String duration = highlight.getDisplayDuration(mContext);
        String startTime = TimeUtils.flattenCalendarTimeSafely(highlight.getStartTimeCopy(),
                TimeUtils.TIME_FORMAT_HH_MM);
        String endTime = TimeUtils.flattenCalendarTimeSafely(highlight.getEndTimeCopy(),
                TimeUtils.TIME_FORMAT_HH_MM);

        switch (highlight.getEventType()) {
            case EventContract.EventEntry.EVENT_TYPE_MEAL:
                if (highlight.getAmount() == -1) {
                    return mContext.getString(R.string.highlight_message_meal,
                            type, duration, startTime, endTime);
                } else {
                    return mContext.getString(R.string.highlight_message_meal_with_amount,
                            type, duration, startTime, endTime, highlight.getAmount());
                }

            case EventContract.EventEntry.EVENT_TYPE_DIAPER:
                return mContext.getString(R.string.highlight_message_diaper, type, startTime);

            default:
                return mContext.getString(R.string.highlight_message_sleep,
                        type, duration, startTime, endTime);
        }
    }

    private void updateTotalDuration() {
        long totalDuration = 0;
        int totalAmount = 0;
        for (Event event : mEvents) {
            totalDuration += event.calculateDuration();
            if (event.getAmount() != -1) {
                totalAmount += event.getAmount();
            }
        }
        mTotalDuration = totalDuration;
        mTotalAmount = totalAmount;
    }
}
