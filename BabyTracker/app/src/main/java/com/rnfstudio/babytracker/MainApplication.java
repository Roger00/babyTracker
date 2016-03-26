package com.rnfstudio.babytracker;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rnfstudio.babytracker.db.EventProvider;
import com.rnfstudio.babytracker.db.Profile;
import com.rnfstudio.babytracker.db.ProfileContract;

/**
 * Created by Roger on 2015/7/22.
 */
public class MainApplication extends Application {

    private static final String TAG = "[MainApplication]";
    public static final String PACKAGE_NAME = "com.rnfstudio.babytracker";

    private static final String SP_KEY_CURRENT_USER = "sp_key_current_user";

    private static final long USER_ID_NO_ID = -1;

    private static long sCurrentUser = 1;
    private static Profile sProfile;

    public static long getUserId(Context context) {
        if (sCurrentUser == USER_ID_NO_ID) {
            loadDefaultUserId(context);
        }

        return sCurrentUser;
    }

    public static void setUserId(Context context, long userId) {
        sCurrentUser = userId;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(SP_KEY_CURRENT_USER, userId).apply();
    }

    private static void loadDefaultUserId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sCurrentUser = sp.getLong(SP_KEY_CURRENT_USER, USER_ID_NO_ID);
    }

    public static Profile getUserProfile() {
        return sProfile;
    }

    public static void setUserProfile(Profile profile) {
        sProfile = profile;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void loadDefaultUserProfile(Context context) {
        Profile profile = null;

        // load default user profile
        try (Cursor cursor = context.getContentResolver().query(EventProvider.sNotifyUriForUser,
                new String[] {ProfileContract.UserEntry._ID,
                        ProfileContract.UserEntry.COLUMN_NAME_DISPLAY_NAME,
                        ProfileContract.UserEntry.COLUMN_NAME_GENDER,
                        ProfileContract.UserEntry.COLUMN_NAME_BIRTH_YEAR,
                        ProfileContract.UserEntry.COLUMN_NAME_BIRTH_MONTH,
                        ProfileContract.UserEntry.COLUMN_NAME_BIRTH_DAY,
                        ProfileContract.UserEntry.COLUMN_NAME_PROFILE_PICTURE},
                ProfileContract.UserEntry._ID + "=?",
                new String[]{String.valueOf(getUserId(context))},
                null)) {

            if (cursor != null && cursor.moveToNext()) {
                profile = Profile.createFromCursor(cursor);
            }

        } catch (Exception e) {
            Log.w(TAG, "Fail to load default user profile: " + e.toString());
        }

        if (profile != null) {
            setUserProfile(profile);
        }
    }
}