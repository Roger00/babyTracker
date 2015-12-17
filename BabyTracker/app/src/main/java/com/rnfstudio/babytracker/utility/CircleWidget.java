package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.os.Vibrator;
import android.util.Pair;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.Event;

import java.util.List;
import java.util.Map;

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
    private List<Event> mEvents;
    private Map<Integer, Event> mEventMap;

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
    }

    @Override
    public void onCircleTouch(int index) {
        // 1. look up associated event using data index
        // 2. update info panel
        // 3. make circle draw highlights -> this can be done by circle itself
        mInfoPanel.setText(String.format("D:%.2f, A: %.2f", (float)index, 11.5));

        // send haptic feedback
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);
    }

    public void setCircleData(List<Pair<Float, Float>> circleViewData) {
        if (mCircle != null) {
            mCircle.setData(circleViewData);
        }
    }

}
