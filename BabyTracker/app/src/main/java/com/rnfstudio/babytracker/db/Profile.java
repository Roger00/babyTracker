package com.rnfstudio.babytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.rnfstudio.babytracker.utility.Utilities;

import java.util.Calendar;

/**
 * Created by Roger on 2016/3/10.
 */
public class Profile {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "Profile";

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
    private String mName;
    private int mGender;
    private int mBirthYear;
    private int mBirthMonth;
    private int mBirthDay;
    private Bitmap mProfilePic;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    private Profile(Long id, String name, int gender, int year, int month, int day, Bitmap pic) {
        mId = id;
        mName = name;
        mGender = gender;
        mBirthYear = year;
        mBirthMonth = month;
        mBirthDay = day;
        mProfilePic = pic;
    }

    public static Profile createFromCursor(Cursor c) {
        if (c != null && c.moveToNext()) {
            long id = c.getLong(ProfileContract.ProfileQuery.ID);
            String name = c.getString(ProfileContract.ProfileQuery.DISPLAY_NAME);
            int gender = c.getInt(ProfileContract.ProfileQuery.GENDER);
            int year = c.getInt(ProfileContract.ProfileQuery.BIRTH_YEAR);
            int month = c.getInt(ProfileContract.ProfileQuery.BIRTH_MONTH);
            int day = c.getInt(ProfileContract.ProfileQuery.BIRTH_DAY);
            byte[] data = c.getBlob(ProfileContract.ProfileQuery.PROFILE_PICTURE);

            Bitmap pic = null;
            if (data != null) {
                pic = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
            return new Profile(id, name, gender, year, month, day, pic);

        } else {
            Log.w(TAG, "Fail to create Profile from cursor");
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public Bitmap getProfilePicture() {
        return mProfilePic;
    }

    public void setProfilePicture(Bitmap bitmap) {
        mProfilePic = bitmap;
    }

    public boolean writeDB(Context context) {
        ContentValues cvs = new ContentValues();
        cvs.put(ProfileContract.UserEntry._ID, mId);
        cvs.put(ProfileContract.UserEntry.COLUMN_NAME_DISPLAY_NAME, mName);
        cvs.put(ProfileContract.UserEntry.COLUMN_NAME_GENDER, mGender);
        cvs.put(ProfileContract.UserEntry.COLUMN_NAME_BIRTH_YEAR, mBirthYear);
        cvs.put(ProfileContract.UserEntry.COLUMN_NAME_BIRTH_MONTH, mBirthMonth);
        cvs.put(ProfileContract.UserEntry.COLUMN_NAME_BIRTH_DAY, mBirthDay);
        cvs.put(ProfileContract.UserEntry.COLUMN_NAME_PROFILE_PICTURE,
                Utilities.encodeBitmap(mProfilePic));

        if (mId == -1) {
            cvs.remove(ProfileContract.UserEntry._ID);
            Uri insertUri = context.getContentResolver()
                    .insert(EventProvider.sNotifyUriForUser, cvs);
            return insertUri != null;

        } else {
            int rowsAffected = context.getContentResolver().update(
                    EventProvider.sNotifyUriForUser,
                    cvs,
                    ProfileContract.UserEntry._ID + "=?",
                    new String[] {String.valueOf(mId)});

            return rowsAffected == 1;
        }
    }

}
