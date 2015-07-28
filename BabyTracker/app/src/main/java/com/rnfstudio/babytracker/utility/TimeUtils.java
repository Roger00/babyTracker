package com.rnfstudio.babytracker.utility;

import android.util.Log;

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

    public static long secondsBetween(Calendar c1, Calendar c2) {
        long timeInMillis1 = c1.getTimeInMillis();
        long timeInMillis2 = c2.getTimeInMillis();
        long diffInMillis = Math.abs(timeInMillis1 - timeInMillis2);
        return (long) (diffInMillis / 1000.0);
    }

    public static int getRemainSeconds(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return (int) seconds;
    }

    public static int getRemainMinutes(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;

        long hours = minutes / 60;
        minutes = minutes % 60;

        return (int) minutes;
    }

    public static int getRemainHours(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;

        long hours = minutes / 60;
        minutes = minutes % 60;

        long days = hours / 24;
        hours = hours % 24;

        return (int) hours;
    }

    public static int getRemainDays(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;

        long hours = minutes / 60;
        minutes = minutes % 60;

        long days = hours / 24;
        hours = hours % 24;

        return (int) days;
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

    public static int getRemainDaysInMonth(int days) {
        int months = days / 30;
        days = days % 30;
        return days;
    }

    public static int getRemainMonthsInYear(int days) {
        int months = days / 30;
        days = days % 30;

        int years = months / 60;
        months = months % 60;

        return months;
    }

    public static int getRemainYears(int days) {
        int months = days / 30;
        days = days % 30;

        int years = months / 60;
        months = months % 60;

        return years;
    }
}
