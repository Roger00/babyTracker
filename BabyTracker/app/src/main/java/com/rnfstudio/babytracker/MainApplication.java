package com.rnfstudio.babytracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Roger on 2015/7/22.
 */
public class MainApplication extends Application {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String PACKAGE_NAME = "com.rnfstudio.babytracker";

    private static final String SP_KEY_CURRENT_USER = "sp_key_current_user";

    private static final long USER_ID_NO_ID = -1;

    private static long sCurrentUser;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    static {
        sCurrentUser = 1;
    }

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
    public static long getUserId(Context context) {
        if (sCurrentUser == USER_ID_NO_ID) {
            initCurrentUser(context);
        }

        return sCurrentUser;
    }

    private static void initCurrentUser(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sCurrentUser = sp.getLong(SP_KEY_CURRENT_USER, USER_ID_NO_ID);
    }

    public static void setUserId(Context context, long userId) {
        sCurrentUser = userId;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(SP_KEY_CURRENT_USER, userId).apply();
    }
}
