package com.rnfstudio.babytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
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
    public Profile(Long id, String name, int gender, int year, int month, int day, Bitmap pic) {
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
    public long getId() {
        return mId;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public int getBirthYear() {
        return mBirthYear;
    }

    public int getBirthMonth() {
        return mBirthMonth;
    }

    public int getBirthDay() {
        return mBirthDay;
    }

    public String getBirthStr() {
        return String.format("%d/%d/%d", mBirthYear, mBirthMonth, mBirthDay);
    }

    public void setBirthYear(int year) {
        mBirthYear = year;
    }

    public void setBirthMonth(int month) {
        mBirthMonth = month;
    }

    public void setBirthDay(int day) {
        mBirthDay = day;
    }

    public void setBirth(int year, int month, int day) {
        setBirthYear(year);
        setBirthMonth(month);
        setBirthDay(day);
    }

    public int getGender() { return mGender; }

    public void setGender(int gender) { mGender = gender; }

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

            // update id if insert successfully
            if (insertUri != null && !TextUtils.isEmpty(insertUri.getLastPathSegment())) {
                mId = Long.parseLong(insertUri.getLastPathSegment());
            }

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

    public void asyncWriteDB(final Context context) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                writeDB(context);
                return null;
            }
        }.execute();
    }
}
