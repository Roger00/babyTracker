package com.rnfstudio.babytracker;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.PopupWindow;

import com.rnfstudio.babytracker.utility.ButtonState;
import com.rnfstudio.babytracker.utility.PressState;

import java.util.List;
import java.util.Map;

/**
 * Created by Roger on 2015/7/5.
 */
public class MainMenuItem {
    private static final String TAG = "MainMenuItem";
    private String mId;
    private String mName;
    private String mState = ButtonState.STOP;
    private long mStartTime = 0;
    private PressState mPressedState = new PressState();
    private PopupWindow mPopupWindow;
    
    public static final String MENU_ITEM_SLEEP = "SLEEP";
    public static final String MENU_ITEM_MEAL = "MEAL";
    public static final String MENU_ITEM_DIAPER = "DIAPER";
    public static final String MENU_ITEM_MILESTONES = "MILESTONES";
    public static final String MENU_ITEM_MEDICATION = "MEDICATION";
    public static final String MENU_ITEM_GROW = "GROW";
    public static final String MENU_ITEM_ALARM = "ALARM";
    public static final String MENU_ITEM_STATS = "STATS";
    public static final String MENU_ITEM_SETTINGS = "SETTINGS";

    public static final String[] MENU_ITEMS =
            {MENU_ITEM_SLEEP,
            MENU_ITEM_MEAL,
            MENU_ITEM_DIAPER,
            MENU_ITEM_MILESTONES,
            MENU_ITEM_MEDICATION,
            MENU_ITEM_GROW,
            MENU_ITEM_ALARM,
            MENU_ITEM_STATS,
            MENU_ITEM_SETTINGS
            };

    private static Map<String, List<String>> HashMap;

    public MainMenuItem(Context context, String id) {
        mId = id;

        Resources res = context.getResources();
        int resId = res.getIdentifier(id, "string", MainActivity.PACKAGE_NAME);
        try {
            mName = res.getString(resId);
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, "Exception during getString: " + e.toString());
            mName = "";
        }

        initSubMenuItems();
    }

    private void initSubMenuItems() {
//        HashMap<String, ArrayList<String>> map;
//        map = new HashMap<String, ArrayList<String>>();
    }

    public String getName() { return mName; }

    public String getId() { return mId; }

    public String getState() { return mState; }

    public void setState(final String state) {mState = state; }

    public long getStartTime()  {
        return mStartTime;
    }

    public void setStartTime(long time) {
        mStartTime = time;
    }

    public PressState getPressedState() { return mPressedState; }

    public PopupWindow getPopupWindow() { return mPopupWindow; }

    public void setPopupWindow(PopupWindow popupWindow) {
        mPopupWindow = popupWindow;
    }

    public String getSubItemId(int direction) {
        return "";
    }

    public String getSubItemName(int direction) {
        return "";
    }

    public static String getDisplayCmd(Context context, String cmdId) {
        Resources res = context.getResources();
        int resId = res.getIdentifier(cmdId, "string", "com.rnfstudio.babytracker");

        String displayCmd = "";
        if (resId != 0) {
            try {
                displayCmd = res.getString(resId);
            } catch (Resources.NotFoundException e) {
            }
        }

        return displayCmd;
    }
}
