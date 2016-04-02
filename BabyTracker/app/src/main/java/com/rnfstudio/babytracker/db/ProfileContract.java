package com.rnfstudio.babytracker.db;

import android.content.Context;
import android.provider.BaseColumns;

import com.rnfstudio.babytracker.MainApplication;

/**
 * Created by Roger on 2016/3/10.
 */
public class ProfileContract {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    /* Inner class that defines the user profile */
    public static abstract class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "userProfile";
        public static final String COLUMN_NAME_DISPLAY_NAME = "displayName";
        public static final String COLUMN_NAME_GENDER = "gender";
        public static final String COLUMN_NAME_BIRTH_YEAR = "birthYear";
        public static final String COLUMN_NAME_BIRTH_MONTH = "birthMoth";
        public static final String COLUMN_NAME_BIRTH_DAY = "birthDay";
        public static final String COLUMN_NAME_PROFILE_PICTURE = "profilePicture";
    }

    public static class ProfileQuery {
        public static final int ID = 0;
        public static final int DISPLAY_NAME = 1;
        public static final int GENDER = 2;
        public static final int BIRTH_YEAR = 3;
        public static final int BIRTH_MONTH = 4;
        public static final int BIRTH_DAY = 5;
        public static final int PROFILE_PICTURE = 6;
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    // SQL statements
    private static final String INT_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    UserEntry.COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE + COMMA_SEP +
                    UserEntry.COLUMN_NAME_GENDER + INT_TYPE + COMMA_SEP +
                    UserEntry.COLUMN_NAME_BIRTH_YEAR + INT_TYPE + COMMA_SEP +
                    UserEntry.COLUMN_NAME_BIRTH_MONTH + INT_TYPE + COMMA_SEP +
                    UserEntry.COLUMN_NAME_BIRTH_DAY + INT_TYPE + COMMA_SEP +
                    UserEntry.COLUMN_NAME_PROFILE_PICTURE + BLOB_TYPE + ")";

    public static final int GENDER_UNSET = -1;
    public static final int GENDER_BOY = 0;
    public static final int GENDER_GIRL = 1;
    public static final int GENDER_UNKNOWN = 2;

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

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
}
