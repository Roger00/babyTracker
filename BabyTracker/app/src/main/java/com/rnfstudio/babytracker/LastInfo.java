package com.rnfstudio.babytracker;

import android.content.Context;
import android.content.res.Resources;

import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.Calendar;

/**
 * Created by Roger on 2015/7/28.
 */
public class LastInfo {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    public long mLastSleep;
    public long mLastMeal;
    public long mLastDiaper;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public LastInfo(long lastSleep, long lastMeal, long lastDiaper) {
        mLastSleep = lastSleep;
        mLastMeal = lastMeal;
        mLastDiaper = lastDiaper;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public String getLastInfoMessage(Context context, long t) {
        if (t == 0) {
            return getDefaultMessage(context);
        }

        long diff = TimeUtils.secondsUntilNow(t);
        int secs = TimeUtils.getRemainSeconds(diff);
        int mins = TimeUtils.getRemainMinutes(diff);
        int hours = TimeUtils.getRemainHours(diff);
        int days = TimeUtils.getRemainDays(diff);

        Resources res = context.getResources();
        if (days > 0) {
            return res.getQuantityString(R.plurals.last_info_days_ago, days, days);
        } else if (hours > 0) {
            return res.getQuantityString(R.plurals.last_info_hours_ago, hours, hours, mins);
        } else if (mins > 0) {
            return res.getQuantityString(R.plurals.last_info_mins_ago, mins, mins);
        } else if (secs > 0) {
            return res.getQuantityString(R.plurals.last_info_secs_ago, secs, secs);
        } else {
            return getDefaultMessage(context);
        }

    }

    private String getDefaultMessage(Context context) {
        return context.getString(R.string.last_info_default_message);
    }

    public String getLastSleepMessage(Context context) {
        return getLastInfoMessage(context, mLastSleep);
    }

    public String getLastDiaperMessage(Context context) {
        return getLastInfoMessage(context, mLastDiaper);
    }

    public String getLastMealMessage(Context context) {
        return getLastInfoMessage(context, mLastMeal);
    }
}
