package com.rnfstudio.babytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
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
    private static final boolean DEBUG = true;

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
    private long mId;
    private int mType;
    private int mSubType;
    private long mStartTime;
    private long mEndTime;
    private long mDuration;
    private int mAmount;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------
    public static Event createFromCursor(Cursor c) {
        long id = c.getLong(EventContract.EventQuery.EVENT_ID);
        int type = c.getInt(EventContract.EventQuery.EVENT_TYPE);
        int subType = c.getInt(EventContract.EventQuery.EVENT_SUBTYPE);
        long startTime = c.getLong(EventContract.EventQuery.EVENT_START_TIME);
        long endTime = c.getLong(EventContract.EventQuery.EVENT_END_TIME);
        long duration = c.getLong(EventContract.EventQuery.EVENT_DURATION);
        int amount = c.getInt(EventContract.EventQuery.EVENT_AMOUNT);

        return new Event(id, type, subType, startTime, endTime, duration, amount);
    }

    public static Event createFromBundle(Bundle extras) {
        long id = extras.getLong(EXTRA_EVENT_ID, 0);
        int type = extras.getInt(EXTRA_EVENT_TYPE, 0);
        int subType = extras.getInt(EXTRA_EVENT_SUBTYPE, 0);
        long startTime = extras.getLong(EXTRA_EVENT_START_TIME);
        long endTime = extras.getLong(EXTRA_EVENT_END_TIME);
        long duration = extras.getLong(EXTRA_EVENT_DURATION, 0);
        int amount = extras.getInt(EXTRA_EVENT_AMOUNT, 0);

        return new Event(id, type, subType, startTime, endTime, duration, amount);
    }

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    private Event(Long id, int type, int subType, long startTime, long endTime,
                  long durationInMilliSec, int amountInMilliLiter) {
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
        bundle.putLong(EXTRA_EVENT_ID, mId);
        bundle.putInt(EXTRA_EVENT_TYPE, mType);
        bundle.putInt(EXTRA_EVENT_SUBTYPE, mSubType);
        bundle.putLong(EXTRA_EVENT_START_TIME, mStartTime);
        bundle.putLong(EXTRA_EVENT_END_TIME, mEndTime);
        bundle.putLong(EXTRA_EVENT_DURATION, mDuration);
        bundle.putInt(EXTRA_EVENT_AMOUNT, mAmount);

        return bundle;
    }

    public String getDisplayType(Context context) {
        return Utilities.getDisplayCmd(context, EventContract.EventEntry.getTypeStr(mType, mSubType));
    }

    public String getDisplayDuration(Context context) {
        return getDisplayDuration(context, mDuration);
    }

    public static String getDisplayDuration(Context context, long durationInMillis) {
        Resources res = context.getResources();

        int durationInSec = (int) (durationInMillis / 1000);
        int secs = TimeUtils.getRemainSeconds(durationInSec);
        int mins = TimeUtils.getRemainMinutes(durationInSec);
        int hours = TimeUtils.getRemainHours(durationInSec);
        int days = TimeUtils.getRemainDays(durationInSec);

        String duration;
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

    public long getId() {
        return mId;
    }

    public String getTypeStr() {
        return EventContract.EventEntry.getTypeStr(mType, mSubType);
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public Calendar getStartTimeCopy() {
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(mStartTime);
        return startTime;
    }

    public Calendar getEndTimeCopy() {
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(mEndTime);
        return endTime;
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

    public boolean writeDB(Context context, boolean createEndTime) {
        if (createEndTime) mEndTime = Calendar.getInstance().getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(EventContract.EventEntry.COLUMN_ID, mId);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE, mType);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_SUBTYPE, mSubType);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME, mStartTime);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME, mEndTime);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_DURATION, mEndTime - mStartTime);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_AMOUNT, mAmount);

        if (mId == -1) {
            values.remove(EventContract.EventEntry.COLUMN_ID);
            Uri insertUri = context.getContentResolver().insert(EventProvider.sMainUri, values);
            return insertUri != null;

        } else {
            int rowsAffected = context.getContentResolver().update(
                    EventProvider.sMainUri,
                    values,
                    EventContract.EventEntry.COLUMN_ID + "=?",
                    new String[] {String.valueOf(mId)});

            return rowsAffected == 1;
        }
    }

    public boolean removeFromDB(Context context) {
        return context.getContentResolver().delete(
                EventProvider.sMainUri,
                EventContract.EventEntry.COLUMN_ID + "=?",
                new String[]{String.valueOf(mId)}) == 1;
    }

    public void setEventType(String typeStr) {
        setEventType(EventContract.EventEntry.getMainType(typeStr), EventContract.EventEntry.getSubType(typeStr));
    }

    public void setEventType(int type, int subType) {
        mType = type;
        mSubType = subType;
    }

    public void setStartDate(int year, int month, int day) {
        Calendar newTime = Calendar.getInstance();
        newTime.setTimeInMillis(mStartTime);
        newTime.set(year, month, day);

        mStartTime = newTime.getTimeInMillis();

        updateDuration();
    }

    public void setStartTime(int hour, int minute) {
        Calendar newTime = Calendar.getInstance();
        newTime.setTimeInMillis(mStartTime);
        newTime.set(Calendar.HOUR_OF_DAY, hour);
        newTime.set(Calendar.MINUTE, minute);

        mStartTime = newTime.getTimeInMillis();

        updateDuration();
    }

    public void setEndDate(int year, int month, int day) {
        Calendar newTime = Calendar.getInstance();
        newTime.setTimeInMillis(mEndTime);
        newTime.set(year, month, day);

        mEndTime = newTime.getTimeInMillis();

        updateDuration();
    }

    public void setEndTime(int hour, int minute) {
        Calendar newTime = Calendar.getInstance();
        newTime.setTimeInMillis(mEndTime);
        newTime.set(Calendar.HOUR_OF_DAY, hour);
        newTime.set(Calendar.MINUTE, minute);

        mEndTime = newTime.getTimeInMillis();

        updateDuration();
    }

    public long calculateDuration() {
        return mEndTime - mStartTime;
    }

    private void updateDuration() {
        setDuration(calculateDuration());
    }

    private void setDuration(long millis) {
        mDuration = millis;
    }

    public void setAmount(int amount) {
        mAmount = amount;
    }
}
