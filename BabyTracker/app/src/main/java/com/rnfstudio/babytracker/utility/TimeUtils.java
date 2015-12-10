package com.rnfstudio.babytracker.utility;

import android.util.Log;

import com.rnfstudio.babytracker.db.EventContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Roger on 2015/7/22.
 */
public class TimeUtils {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "TimeUtils";
    private static final boolean DEBUG = true;

    public static final String TIME_FORMAT_HH_MM_SS = "HH:mm:ss";

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
    public static Calendar unFlattenCalendarTimeSafely(String s, String pattern) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat(pattern);
        try {
            c.setTime(f.parse(s));
        } catch (ParseException e) {
            if (DEBUG) Log.d(TAG, "[unFlattenCalendarTime] exception: " + e.toString());
            return null;
        }
        return c;
    }

    public static String flattenCalendarTimeSafely(Calendar c, String pattern) {
        SimpleDateFormat f = new SimpleDateFormat(pattern);
        return f.format(c.getTime());
    }

    public static long secondsUntilNow(long t) {
        return (long) (Math.abs(Calendar.getInstance().getTimeInMillis() - t) / 1000.0);
    }

    public static int getRemainSeconds(long seconds) {
        return (int) seconds % 60;
    }

    public static int getRemainMinutes(long seconds) {
        return (int) ((seconds / 60) % 60);
    }

    public static int getRemainHours(long seconds) {
        return (int) (((seconds / 60) / 60) % 24);
    }

    public static int getRemainDays(long seconds) {
        return (int) (((seconds / 60) / 60) / 24);
    }

    public static int getRemainDaysInMonth(int days) {
        return days % 30;
    }

    public static int getRemainMonthsInYear(int days) {
        return ((days / 30) % 60);
    }

    public static int getRemainYears(int days) {
        return ((days / 30) / 12);
    }

    public static int daysBetween(Calendar c1, Calendar c2) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        if (c1.before(c2)) {
            start.setTime(c1.getTime());
            end.setTime(c2.getTime());
        } else {
            start.setTime(c2.getTime());
            end.setTime(c1.getTime());
        }

        int days = 0;
        while (start.before(end)) {
            start.add(Calendar.DAY_OF_MONTH, 1);
            days++;
        }
        return days;
    }


    public static Calendar unflattenEventTime(String timeStr) {
        return unFlattenCalendarTimeSafely(timeStr, EventContract.EventEntry.SIMPLE_DATE_TIME_FORMAT);
    }

    public static String flattenEventTime(Calendar c) {
        return flattenCalendarTimeSafely(c, EventContract.EventEntry.SIMPLE_DATE_TIME_FORMAT);
    }

    public static long getTodayMidnightInMillis() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.MILLISECOND, 0);
        return time.getTimeInMillis();
    }

    public static long getTomorrowMidnightInMillis() {
        return getTodayMidnightInMillis() + 86400000;
    }
}
