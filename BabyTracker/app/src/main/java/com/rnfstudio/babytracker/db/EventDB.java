package com.rnfstudio.babytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.Calendar;
import java.util.Date;

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
    public boolean insertEvent(String type, Calendar startTime, Calendar endTime, int amount) {
        if (DEBUG) {
            Log.v(TAG, "[addCategoryID] package name: " + type + " category Id: " + startTime + " priority: " + endTime);
        }

        return insertEvent(EventContract.EventEntry.getMainType(type),
                EventContract.EventEntry.getSubType(type),
                startTime, endTime, amount);
    }

    public boolean insertEvent(int type, int subType, Calendar startTime, Calendar endTime, int amount) {
        return updateEvent(-1, type, subType, startTime, endTime, amount);
    }

    public boolean updateEvent(int id, int type, int subType, Calendar startTime, Calendar endTime, int amount) {
        long startTimeInMillis = startTime.getTimeInMillis();
        long endTimeInMillis = endTime.getTimeInMillis();
        return updateEvent(id, type, subType, startTimeInMillis, endTimeInMillis, amount);
    }

    public boolean updateEvent(int id, int type, int subType, long startTime, long endTime, int amount) {
        ContentValues values = new ContentValues();
        if (id != -1) values.put(EventContract.EventEntry.COLUMN_ID, id);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE, type);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_SUBTYPE, subType);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME, startTime);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME, endTime);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_DURATION, endTime - startTime);
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_AMOUNT, amount);

        Long ret = mDB.replace(EventContract.EventEntry.TABLE_NAME, null, values);
        return ret != -1;
    }

    /**
     * See <a href="http://stackoverflow.com/questions/7510219/deleting-row-in-sqlite-in-android">Delete row in SQLite</a>
     */
    public boolean deleteEvent(int id) {
        String table = EventContract.EventEntry.TABLE_NAME;
        String whereClause = "_id" + "=?";
        String[] whereArgs = new String[] { String.valueOf(id) };

        int cRowsAffected = mDB.delete(table, whereClause, whereArgs);
        return cRowsAffected != 0;
    }

    public void close() {
        if (DEBUG) {
            Log.v(TAG, "[close] called");
        }

        if (mDB != null) {
            mDB.close();
        }
    }

//    public Cursor queryEventsForMainType(Date date, int mainType) {
//        Cursor cursor = null;
//        cursor = mDB.query(true,
//                EventContract.EventEntry.TABLE_NAME,
//                new String[] {EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME},
//                EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + "=?", new String[] {Integer.toString(mainType)},
//                null, null, EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " DESC", null);
//        try {
//            cursor = mDB.query(true,
//                    EventContract.EventEntry.TABLE_NAME,
//                    new String[] {EventContract.EventEntry.COLUMN_ID,
//                            EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE,
//                            EventContract.EventEntry.COLUMN_NAME_EVENT_SUBTYPE,
//                            EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME,
//                            EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME,
//                            EventContract.EventEntry.COLUMN_NAME_EVENT_DURATION,
//                            EventContract.EventEntry.COLUMN_NAME_EVENT_AMOUNT},
//                    EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + "=? AND " +
//                            EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME +
//                            " BETWEEN ? AND ?", new String[] { Integer.toString(mainType) },
//                    null,
//                    null,
//                    EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " DESC", null);
//
//        } catch (SQLiteException e) {
//            Log.w(TAG, "[queryAllEvents] exception during db query");
//        }
//        return cursor;
//    }


    /**
     * Query all events in db, for RecordLoader.
     */
    public Cursor queryAllEvents() {
        Cursor cursor;
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

            return cursor;

        } catch (SQLiteException se) {
            Log.w(TAG, "[queryAllEvents] exception during db query: " + se.toString());
        }

        return null;
    }

    public Cursor queryEventsForMainTypeAndPeriod(int mainType, long startFrom, long startTo, long endFrom, long endTo) {
        Cursor cursor;

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
                    EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + "=? AND " +
                            EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " BETWEEN ? AND ? AND " +
                            EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME + " BETWEEN ? AND ?",
                    new String[] {Integer.toString(mainType), Long.toString(startFrom), Long.toString(startTo),
                            Long.toString(endFrom), Long.toString(endTo)},
                    null, null, EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " ASC", null);

            return cursor;

        } catch (SQLiteException e) {
            Log.w(TAG, "[queryLatestEvent] exception during db query");
        }

        return null;
    }


    public void dropTable() {
        mDB.execSQL(EventContract.SQL_DROP_TABLE);
    }

    public void clearAllEvents() {
        mDB.delete(EventContract.EventEntry.TABLE_NAME, null, null);
    }

    /**
     * Query the latest occurrence time for events
     *
     * @param mainType: the query type for events
     * @return: the occurence time in milli-second since epoch
     */
    public long queryLatestTimeForMainType(int mainType) {
        try (
            Cursor cursor = mDB.query(true,
                EventContract.EventEntry.TABLE_NAME,
                new String[] {EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME},
                EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + "=?", new String[] {Integer.toString(mainType)},
                null, null, EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " DESC", null)) {

            if (cursor != null && cursor.moveToNext()) {
                return cursor.getLong(0);
            }

        } catch (SQLiteException e) {
            Log.w(TAG, "[queryLatestEvent] exception during db query");
        }

        return 0;
    }
}
