package com.rnfstudio.babytracker.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Roger on 2016/2/12.
 */
public class EventProvider extends ContentProvider {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final UriMatcher sUriMatcher;

    private static final String TAG = "[EventProvider]";
    public static final String AUTHORITY = "com.rnfstudio.babytracker.provider";

    public static final Uri sMainUri;

    private static final int ROOT = -1;
    private static final int EVENT = 0;
    private static final int EVENT_ID = 1;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    static {
        sUriMatcher = new UriMatcher(ROOT);
        sUriMatcher.addURI(AUTHORITY, "event", EVENT);
        sUriMatcher.addURI(AUTHORITY, "event/#", EVENT_ID);

        sMainUri = Uri.parse("content://com.rnfstudio.babytracker.provider/event");
    }

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private SQLiteOpenHelper mOpenHelper;

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
    public boolean onCreate() {
        Log.v(TAG, "[onCreate]");

        /*
         * Creates a new helper object. This method always returns quickly.
         * Notice that the database itself isn't created or opened
         * until SQLiteOpenHelper.getWritableDatabase is called
         */
        mOpenHelper = new EventDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor;
        try {
            cursor = db.query(true,
                    EventContract.EventEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder,
                    null);

            return cursor;

        } catch (SQLiteException se) {
            Log.w(TAG, "[queryAllEvents] exception during db query: " + se.toString());
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case EVENT:
                return "vnd.android.cursor.dir/event";
            case EVENT_ID:
                return "vnd.android.cursor.item/event";
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String table = EventContract.EventEntry.TABLE_NAME;
        long rowId = db.insert(table, null, values);
        return rowId == -1 ? null : Uri.withAppendedPath(sMainUri, String.valueOf(rowId));
    }

    /**
     * See <a href="http://stackoverflow.com/questions/7510219/deleting-row-in-sqlite-in-android">
     *     Delete row in SQLite</a>
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String table = EventContract.EventEntry.TABLE_NAME;
        return db.delete(table, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String table = EventContract.EventEntry.TABLE_NAME;
        return db.update(table, values, selection, selectionArgs);
    }

    /**
     * Query the latest occurrence time for events
     *
     * @param mainType: the query type for events
     * @return: the occurence time in milli-second since epoch
     */
    public long queryLatestTimeForMainType(int mainType) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        try (
                Cursor cursor = db.query(true,
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
