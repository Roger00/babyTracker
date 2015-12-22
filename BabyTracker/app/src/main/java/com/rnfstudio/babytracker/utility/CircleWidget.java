package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.util.Pair;
import android.widget.TextView;

import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.db.Event;

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

    public void setEvents(List<Event> events) {
        mEvents = events;

        // update data in CircleView
        setCircleData();

        // update total duration of events
        updateTotalDuration();

        // update info panel
        mInfoPanel.setText(getDefaultMessage());
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
            return TimeUtils.isNowAM() ? mContext.getString(R.string.circle_info_default_am, durationStr) :
                    mContext.getString(R.string.circle_info_default_pm, durationStr);
        } else {
            // add new records to start
            return mContext.getString(R.string.circle_info_default_add_data_hint);
        }

    }

    public String getHighlightEventInfo(int index) {
        // 1. look up associated event using data index
        Event highlight = mEvents.get(index);

        // 2. update info panel
        return String.format("%s\n%s", highlight.getDisplayType(mContext), highlight.getDisplayDuration(mContext));
    }

    private void updateTotalDuration() {
        long totalDuration = 0;
        for (Event event : mEvents) {
            totalDuration += event.calculateDuration();
        }
        mTotalDuration = totalDuration;
    }
}
