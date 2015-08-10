package com.rnfstudio.babytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Roger on 2015/7/22.
 */
public class EventDB {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[EventDB]";
    private static final boolean DEBUG = false;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private final SQLiteDatabase mDB;
    private final Context mContext;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    // @TODO: maybe we need to move getWritableDatabase() into worker thread
    public EventDB(Context ctx) {
        if (DEBUG) {
            Log.v(TAG, "[Ctor] called");
        }

        // open user database
        EventDBHelper dbHelper = new EventDBHelper(ctx);
        mDB = dbHelper.getWritableDatabase();

        mContext = ctx;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public boolean addEvent(String type, Calendar startTime, Calendar endTime, int amount) {
        if (DEBUG) {
            Log.v(TAG, "[addCategoryID] package name: " + type + " category Id: " + startTime + " priority: " + endTime);
        }
        String startTimeStr = TimeUtils.flattenEventTime(startTime);
        String endTimeStr = TimeUtils.flattenEventTime(endTime);
        long durationMilliSec = endTime.getTimeInMillis() - startTime.getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE, EventContract.EventEntry.getMainType(type));
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_SUBTYPE, EventContract.EventEntry.getSubType(type));
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME, startTimeStr);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME, endTimeStr);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_DURATION, durationMilliSec);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_AMOUNT, amount);

        Long ret = mDB.replace(EventContract.EventEntry.TABLE_NAME, null, values);
        return ret != -1;
    }

    
    public void close() {
        if (DEBUG) {
            Log.v(TAG, "[close] called");
        }

        if (mDB != null) {
            mDB.close();
        }
    }

    public List<String> queryLatestEvent(int max) {
        List<String> results = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mDB.query(true,
                    EventContract.EventEntry.TABLE_NAME,
                    new String[] {EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_SUBTYPE,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_DURATION,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_AMOUNT},
                    null, null,
                    null, null, EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME + " DESC", null);
        } catch (SQLiteException e) {
            Log.w(TAG, "[queryLatestEvent] exception during db query");
        }

        if (cursor == null) return results;
        try {
            int count = 0;
            while (count < max && cursor.moveToNext()) {
                int mainType = cursor.getInt(0);
                int subType = cursor.getInt(1);
                String typeStr = EventContract.EventEntry.getTypeStr(mainType, subType);
                String startTimeStr = cursor.getString(2);
                String endTimeStr = cursor.getString(3);
                int duration = cursor.getInt(4);
                int amount = cursor.getInt(5);
                Calendar startTime = TimeUtils.unflattenEventTime(startTimeStr);
                Calendar endTime = TimeUtils.unflattenEventTime(endTimeStr);

                final String OUTPUT_DATE_TIME_FORMAT = "HH:mm:ss.SSS";
                String outStartTimeStr = TimeUtils.flattenCalendarTimeSafely(startTime, OUTPUT_DATE_TIME_FORMAT);
                String outEndTimeStr = TimeUtils.flattenCalendarTimeSafely(endTime, OUTPUT_DATE_TIME_FORMAT);
                String logText = String.format("Event: %s, start: %s, end: %s, duration: %d", typeStr, startTimeStr, endTimeStr, duration);
                String display = String.format("%s:\t%s - %s (%dms, amt:%d)\n", typeStr, outStartTimeStr, outEndTimeStr, duration, amount);
                Log.d(TAG, logText);
                results.add(display);
                count++;
            }
        } finally {
            cursor.close();
        }

        return results;
    }

    public Cursor listRecords() {
        Cursor cursor = null;
        try {
            cursor = mDB.query(true,
                    EventContract.EventEntry.TABLE_NAME,
                    new String[] {EventContract.EventEntry.COLUMN_ID,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_SUBTYPE,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_DURATION,
                            EventContract.EventEntry.COLUMN_NAME_EVENT_AMOUNT},
                    null, null,
                    null, null, EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME + " DESC", null);
        } catch (SQLiteException e) {
            Log.w(TAG, "[listRecords] exception during db query");
        }
        return cursor;
    }

    public void dropTable() {
        mDB.execSQL(EventContract.SQL_DROP_TABLE);
    }

    public void clearEvents() {
        mDB.delete(EventContract.EventEntry.TABLE_NAME, null, null);
    }

    /**
     * Query the latest occurrence time for events
     *
     * @param mainType: the query type for events
     * @return: the Calendar object representing the occurence time
     */
    public Calendar queryLatestTimeForType(int mainType) {
        Calendar result = null;

        Cursor cursor = null;
        try {
            cursor = mDB.query(true,
                    EventContract.EventEntry.TABLE_NAME,
                    new String[] {EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME},
                    EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + "=?", new String[] {Integer.toString(mainType)},
                    null, null, EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " DESC", null);
        } catch (SQLiteException e) {
            Log.w(TAG, "[queryLatestEvent] exception during db query");
        }

        if (cursor != null && cursor.moveToNext()) {
            try {
                String endTimeStr = cursor.getString(0);
                result = TimeUtils.unflattenEventTime(endTimeStr);
            } finally {
                cursor.close();
            }
        }

        return result;
    }
}
