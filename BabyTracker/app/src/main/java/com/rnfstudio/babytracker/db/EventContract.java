package com.rnfstudio.babytracker.db;

import android.provider.BaseColumns;

import com.rnfstudio.babytracker.SwipeButtonHandler;

/**
 * Created by Roger on 2015/7/22.
 */
public class EventContract {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    /* Inner class that defines the table contents */
    public static abstract class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "eventLogs";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME_EVENT_TYPE = "type";
        public static final String COLUMN_NAME_EVENT_SUBTYPE = "subtype";
        public static final String COLUMN_NAME_EVENT_START_TIME = "startTime";
        public static final String COLUMN_NAME_EVENT_END_TIME = "endTime";
        public static final String COLUMN_NAME_EVENT_DURATION = "duration";
        public static final String COLUMN_NAME_EVENT_AMOUNT = "amount";
        public static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";
//        public static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:MM:SS.SSS";

        public static final int NO_TYPE = -1;
        public static final String NO_TYPE_STRING = "no_type";

        public static final int EMPTY_AMOUNT = -1;

        public static final int EVENT_TYPE_SLEEP = 0;
        public static final int EVENT_TYPE_MEAL = 1;
        public static final int EVENT_TYPE_DIAPER = 2;

        public static final int EVENT_SUBTYPE_MEAL_BREAST_FEED = 0;
        public static final int EVENT_SUBTYPE_MEAL_BREAST_FEED_LEFT = 1;
        public static final int EVENT_SUBTYPE_MEAL_BREAST_FEED_RIGHT = 2;
        public static final int EVENT_SUBTYPE_MEAL_BOTTLE_FEED = 3;
        public static final int EVENT_SUBTYPE_MEAL_MILK_FEED = 4;

        public static final int EVENT_SUBTYPE_DIAPER_PEEPOO = 0;
        public static final int EVENT_SUBTYPE_DIAPER_PEEPEE = 1;
        public static final int EVENT_SUBTYPE_DIAPER_POOPOO = 2;

        public static int getMainType(String eventType) {
            if (eventType.equals(SwipeButtonHandler.MENU_ITEM_SLEEP)) {
                return EVENT_TYPE_SLEEP;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_BOTH)) {
                return EVENT_TYPE_MEAL;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_LEFT)) {
                return EVENT_TYPE_MEAL;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_RIGHT)) {
                return EVENT_TYPE_MEAL;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_BOTTLED)) {
                return EVENT_TYPE_MEAL;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_MILK)) {
                return EVENT_TYPE_MEAL;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_DIAPER_BOTH)) {
                return EVENT_TYPE_DIAPER;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_DIAPER_PEEPEE)) {
                return EVENT_TYPE_DIAPER;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_DIAPER_POOPOO)) {
                return EVENT_TYPE_DIAPER;
            } else {
                return NO_TYPE;
            }
        }

        public static int getSubType(String eventType) {
            if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_BOTH)) {
                return EVENT_SUBTYPE_MEAL_BREAST_FEED;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_LEFT)) {
                return EVENT_SUBTYPE_MEAL_BREAST_FEED_LEFT;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_RIGHT)) {
                return EVENT_SUBTYPE_MEAL_BREAST_FEED_RIGHT;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_BOTTLED)) {
                return EVENT_SUBTYPE_MEAL_BOTTLE_FEED;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_MEAL_MILK)) {
                return EVENT_SUBTYPE_MEAL_MILK_FEED;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_DIAPER_BOTH)) {
                return EVENT_SUBTYPE_DIAPER_PEEPOO;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_DIAPER_PEEPEE)) {
                return EVENT_SUBTYPE_DIAPER_PEEPEE;
            } else if (eventType.equals(SwipeButtonHandler.MENU_ITEM_DIAPER_POOPOO)) {
                return EVENT_SUBTYPE_DIAPER_POOPOO;
            } else {
                return NO_TYPE;
            }
        }

        public static String getTypeStr(int mainType, int subType) {
            if (mainType == EVENT_TYPE_SLEEP) {
                return SwipeButtonHandler.MENU_ITEM_SLEEP;
            } else if (mainType == EVENT_TYPE_MEAL) {
                return getMealTypeStr(subType);
            } else if (mainType == EVENT_TYPE_DIAPER) {
                return getDiaperTypeStr(subType);
            } else {
                return NO_TYPE_STRING;
            }
        }

        public static String getMealTypeStr(int subType) {
            if (subType == EVENT_SUBTYPE_MEAL_BREAST_FEED) {
                return SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_BOTH;
            } else if (subType == EVENT_SUBTYPE_MEAL_BREAST_FEED_LEFT) {
                return SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_LEFT;
            } else if (subType == EVENT_SUBTYPE_MEAL_BREAST_FEED_RIGHT) {
                return SwipeButtonHandler.MENU_ITEM_MEAL_TYPE_BREAST_RIGHT;
            } else if (subType == EVENT_SUBTYPE_MEAL_BOTTLE_FEED) {
                return SwipeButtonHandler.MENU_ITEM_MEAL_BOTTLED;
            } else if (subType == EVENT_SUBTYPE_MEAL_MILK_FEED) {
                return SwipeButtonHandler.MENU_ITEM_MEAL_MILK;
            } else {
                return NO_TYPE_STRING;
            }
        }

        public static String getDiaperTypeStr(int subType) {
            if (subType == EVENT_SUBTYPE_DIAPER_PEEPOO) {
                return SwipeButtonHandler.MENU_ITEM_DIAPER_BOTH;
            } else if (subType == EVENT_SUBTYPE_DIAPER_PEEPEE) {
                return SwipeButtonHandler.MENU_ITEM_DIAPER_PEEPEE;
            } else if (subType == EVENT_SUBTYPE_DIAPER_POOPOO) {
                return SwipeButtonHandler.MENU_ITEM_DIAPER_POOPOO;
            } else {
                return NO_TYPE_STRING;
            }
        }
    }

    public static class EventQuery {
        public static final int EVENT_ID = 0;
        public static final int EVENT_TYPE = 1;
        public static final int EVENT_SUBTYPE = 2;
        public static final int EVENT_START_TIME = 3;
        public static final int EVENT_END_TIME = 4;
        public static final int EVENT_DURATION = 5;
        public static final int EVENT_AMOUNT = 6;
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    // SQL statements
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                    EventEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    EventEntry.COLUMN_NAME_EVENT_TYPE + INT_TYPE + COMMA_SEP +
                    EventEntry.COLUMN_NAME_EVENT_SUBTYPE + INT_TYPE + COMMA_SEP +
                    EventEntry.COLUMN_NAME_EVENT_START_TIME + INT_TYPE + COMMA_SEP +
                    EventEntry.COLUMN_NAME_EVENT_END_TIME + INT_TYPE + COMMA_SEP +
                    EventEntry.COLUMN_NAME_EVENT_DURATION + INT_TYPE + COMMA_SEP +
                    EventEntry.COLUMN_NAME_EVENT_AMOUNT + INT_TYPE + COMMA_SEP +
                    "UNIQUE (" + EventEntry.COLUMN_NAME_EVENT_TYPE + COMMA_SEP +
                    EventEntry.COLUMN_NAME_EVENT_START_TIME + ")" + " )";
    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME;

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
    public EventContract() {}

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
}
