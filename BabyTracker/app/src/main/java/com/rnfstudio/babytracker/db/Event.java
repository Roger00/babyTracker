package com.rnfstudio.babytracker.db;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.rnfstudio.babytracker.MainApplication;
import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.utility.TimeUtils;
import com.rnfstudio.babytracker.utility.Utilities;

import java.util.Calendar;

/**
 * Event object which carries basic information
 *
 * Created by Roger on 2015/8/11.
 */
public class Event {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String TAG = "[Event]";

    public static final String EXTRA_EVENT_ID = "event.id";
    public static final String EXTRA_EVENT_TYPE = "event.type";
    public static final String EXTRA_EVENT_SUBTYPE = "event.subtype";
    public static final String EXTRA_EVENT_START_TIME = "event.start.time";
    public static final String EXTRA_EVENT_END_TIME = "event.end.time";
    public static final String EXTRA_EVENT_DURATION = "event.duration";
    public static final String EXTRA_EVENT_AMOUNT = "event.amount";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private int mId;
    private int mType;
    private int mSubType;
    private Calendar mStartTime;
    private Calendar mEndTime;
    private long mDuration;
    private int mAmount;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------
    public static Event createFromCursor(Cursor c) {
        int id = c.getInt(EventContract.EventQuery.EVENT_ID);
        int type = c.getInt(EventContract.EventQuery.EVENT_TYPE);
        int subType = c.getInt(EventContract.EventQuery.EVENT_SUBTYPE);
        Calendar startTime = TimeUtils.unflattenEventTime((c.getString(EventContract.EventQuery.EVENT_START_TIME)));
        Calendar endTime = TimeUtils.unflattenEventTime((c.getString(EventContract.EventQuery.EVENT_END_TIME)));
        long duration = c.getLong(EventContract.EventQuery.EVENT_DURATION);
        int amount = c.getInt(EventContract.EventQuery.EVENT_AMOUNT);

        return new Event(id, type, subType, startTime, endTime, duration, amount);
    }

    public static Event createFromBundle(Bundle extras) {
        int id = extras.getInt(EXTRA_EVENT_ID, 0);
        int type = extras.getInt(EXTRA_EVENT_TYPE, 0);
        int subType = extras.getInt(EXTRA_EVENT_SUBTYPE, 0);
        Calendar startTime = TimeUtils.unflattenEventTime(extras.getString(EXTRA_EVENT_START_TIME));
        Calendar endTime = TimeUtils.unflattenEventTime(extras.getString(EXTRA_EVENT_END_TIME));
        long duration = extras.getLong(EXTRA_EVENT_DURATION, 0);
        int amount = extras.getInt(EXTRA_EVENT_AMOUNT, 0);

        return new Event(id, type, subType, startTime, endTime, duration, amount);
    }
    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    private Event(int id, int type, int subType, Calendar startTime, Calendar endTime, long durationInMilliSec, int amountInMilliLiter) {
        mId = id;
        mType = type;
        mSubType = subType;
        mStartTime = startTime;
        mEndTime = endTime;
        mDuration = durationInMilliSec;
        mAmount = amountInMilliLiter;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_EVENT_ID, mId);
        bundle.putInt(EXTRA_EVENT_TYPE, mType);
        bundle.putInt(EXTRA_EVENT_SUBTYPE, mSubType);
        bundle.putString(EXTRA_EVENT_START_TIME, TimeUtils.flattenEventTime(mStartTime));
        bundle.putString(EXTRA_EVENT_END_TIME, TimeUtils.flattenEventTime(mEndTime));
        bundle.putLong(EXTRA_EVENT_DURATION, mDuration);
        bundle.putInt(EXTRA_EVENT_AMOUNT, mAmount);

        return bundle;
    }

    public String getDisplayType(Context context) {
        return Utilities.getDisplayCmd(context, EventContract.EventEntry.getTypeStr(mType, mSubType));
    }

    public String getDisplayDuration(Context context) {
        Resources res = context.getResources();

        int durationInSec = (int) (mDuration / 1000);
        int secs = TimeUtils.getRemainSeconds(durationInSec);
        int mins = TimeUtils.getRemainMinutes(durationInSec);
        int hours = TimeUtils.getRemainHours(durationInSec);
        int days = TimeUtils.getRemainDays(durationInSec);

        String duration = "";
        if (days > 0) {
            duration =  res.getQuantityString(R.plurals.duration_info_days, days, days, hours);
        } else if (hours > 0) {
            duration = res.getQuantityString(R.plurals.duration_info_hours, hours, hours, mins);
        } else if (mins > 0) {
            duration = res.getQuantityString(R.plurals.duration_info_minutes, mins, mins);
        } else if (secs > 0) {
            duration = res.getQuantityString(R.plurals.duration_info_seconds, secs, secs);
        } else {
            duration = res.getString(R.string.duration_info_pretty_short);
        }
        return duration;
    }

    public int getId() {
        return mId;
    }

    public String getTypeStr() {
        return EventContract.EventEntry.getTypeStr(mType, mSubType);
    }

    public Calendar getStartTimeCopy() {
        return (Calendar) mStartTime.clone();
    }

    public Calendar getEndTimeCopy() {
        return (Calendar) mEndTime.clone();
    }

    public int getAmount() {
        return mAmount;
    }

    public String getDisplayAmount(Context context) {
        Resources res = context.getResources();
        if (mAmount < 0) {
            return res.getString(R.string.record_edit_empty_data);
        } else {
            return res.getString(R.string.record_edit_amount_ml, mAmount);
        }
    }

    public boolean writeDB(Context context) {
        return writeDB(context, true);
    }

    public boolean writeDB(Context context, boolean createEndTime) {
        EventDB db = MainApplication.getEventDatabase(context);
        if (createEndTime) mEndTime = Calendar.getInstance();

        Log.v(TAG, "[writeDB] startTime: " + mStartTime);
        Log.v(TAG, "[writeDB] endTime: " + mEndTime);

        return db.updateEvent(mId, mType, mSubType, mStartTime, mEndTime, mAmount);
    }

    public void setEventType(String typeStr) {
        setEventType(EventContract.EventEntry.getMainType(typeStr), EventContract.EventEntry.getSubType(typeStr));
    }

    public void setEventType(int type, int subType) {
        mType = type;
        mSubType = subType;
    }

    public void setStartDate(int year, int month, int day) {
        mStartTime.set(year, month, day);
        setDuration();
    }

    public void setStartTime(int hour, int minute) {
        mStartTime.set(Calendar.HOUR_OF_DAY, hour);
        mStartTime.set(Calendar.MINUTE, minute);
        setDuration();
    }

    public void setEndDate(int year, int month, int day) {
        mEndTime.set(year, month, day);
        setDuration();
    }

    public void setEndTime(int hour, int minute) {
        mEndTime.set(Calendar.HOUR_OF_DAY, hour);
        mEndTime.set(Calendar.MINUTE, minute);
        setDuration();
    }

    public long calculateDuration() {
        return mEndTime.getTimeInMillis() - mStartTime.getTimeInMillis();
    }

    private void setDuration() {
        setDuration(calculateDuration());
    }

    private void setDuration(long millis) {
        mDuration = millis;
    }

    public void setAmount(int amount) {
        mAmount = amount;
    }
}
