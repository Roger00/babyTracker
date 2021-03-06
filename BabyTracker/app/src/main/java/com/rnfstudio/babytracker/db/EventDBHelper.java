package com.rnfstudio.babytracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Roger on 2015/7/22.
 */
public class EventDBHelper extends SQLiteOpenHelper {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[EventDBHelper]";
    private static final boolean DEBUG = true;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "user_category.db";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public EventDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (DEBUG) Log.v(TAG, "[Ctor] called");
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (DEBUG) Log.v(TAG, "[onCreate] called");
        db.execSQL(EventContract.SQL_CREATE_EVENT_TABLE);
        db.execSQL(ProfileContract.SQL_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DEBUG) Log.v(TAG, "[onUpgrade] called");
    }

    // do nothing since we haven't changed schema until now
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DEBUG) Log.v(TAG, "[onDowngrade] called");
    }
}
