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

    private static final String AUTHORITY = "com.rnfstudio.babytracker.provider";
    private static final String PATH_EVENT = "event";
    private static final String PATH_EVENT_ID = "event/#";
    private static final String PATH_USER = "user";
    private static final String PATH_USER_ID = "user/#";

    public static final Uri sBaseUri;
    public static final Uri sNotifyUriForEvent;
    public static final Uri sNotifyUriForUser;

    private static final int ROOT = -1;
    private static final int EVENT = 0;
    private static final int EVENT_ID = 1;
    private static final int USER = 2;
    private static final int USER_ID = 3;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    static {
        sUriMatcher = new UriMatcher(ROOT);
        sUriMatcher.addURI(AUTHORITY, PATH_EVENT, EVENT);
        sUriMatcher.addURI(AUTHORITY, PATH_EVENT_ID, EVENT_ID);
        sUriMatcher.addURI(AUTHORITY, PATH_USER, USER);
        sUriMatcher.addURI(AUTHORITY, PATH_USER_ID, USER_ID);

        sBaseUri = Uri.parse("content://" + AUTHORITY);
        sNotifyUriForEvent = Uri.withAppendedPath(sBaseUri, PATH_EVENT);
        sNotifyUriForUser = Uri.withAppendedPath(sBaseUri, PATH_USER);
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

        String table;

        switch(sUriMatcher.match(uri)) {
            case EVENT:
            case EVENT_ID:
                table = EventContract.EventEntry.TABLE_NAME;
                break;
            case USER:
            case USER_ID:
                table = ProfileContract.UserEntry.TABLE_NAME;
                break;
            default:
                return null;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor;
        try {
            cursor = db.query(true,
                    table,
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
            case USER:
                return "vnd.android.cursor.dir/user";
            case USER_ID:
                return "vnd.android.cursor.item/user";
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table;
        Uri notifyUri;

        switch(sUriMatcher.match(uri)) {
            case EVENT:
            case EVENT_ID:
                table = EventContract.EventEntry.TABLE_NAME;
                notifyUri = sNotifyUriForEvent;
                break;
            case USER:
            case USER_ID:
                table = ProfileContract.UserEntry.TABLE_NAME;
                notifyUri = sNotifyUriForUser;
                break;
            default:
                return null;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(table, null, values);

        if (rowId != -1) {
            getContext().getContentResolver().notifyChange(notifyUri, null);
        }

        return rowId == -1 ? null : Uri.withAppendedPath(notifyUri, String.valueOf(rowId));
    }

    /**
     * See <a href="http://stackoverflow.com/questions/7510219/deleting-row-in-sqlite-in-android">
     *     Delete row in SQLite</a>
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        Uri notifyUri;

        switch(sUriMatcher.match(uri)) {
            case EVENT:
            case EVENT_ID:
                table = EventContract.EventEntry.TABLE_NAME;
                notifyUri = sNotifyUriForEvent;
                break;
            case USER:
            case USER_ID:
                table = ProfileContract.UserEntry.TABLE_NAME;
                notifyUri = sNotifyUriForUser;
                break;
            default:
                return 0;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int cRowsAffected = db.delete(table, selection, selectionArgs);

        if (cRowsAffected > 0) {
            getContext().getContentResolver().notifyChange(notifyUri, null);
        }

        return cRowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table;
        Uri notifyUri;

        switch(sUriMatcher.match(uri)) {
            case EVENT:
            case EVENT_ID:
                table = EventContract.EventEntry.TABLE_NAME;
                notifyUri = sNotifyUriForEvent;
                break;
            case USER:
            case USER_ID:
                table = ProfileContract.UserEntry.TABLE_NAME;
                notifyUri = sNotifyUriForUser;
                break;
            default:
                return 0;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.replace(table, null, values);

        if (rowId != -1) {
            getContext().getContentResolver().notifyChange(notifyUri, null);
        }

        return rowId == -1 ? 0 : 1;
    }
}
