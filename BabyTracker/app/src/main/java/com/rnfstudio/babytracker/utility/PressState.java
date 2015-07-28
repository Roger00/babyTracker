package com.rnfstudio.babytracker.utility;

/**
 * Created by Roger on 2015/7/10.
 */
public class PressState {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PRESSED = 1;

    private int mDownX = 0;
    private int mDownY = 0;
    private int mState = STATE_NORMAL;

    public int getDownX() {
        return mDownX;
    }

    public int getDownY() {
        return mDownY;
    }

    public boolean isPressed() {
        return mState == STATE_PRESSED;
    }

    public void setPressedLocation(int x, int y) {
        mDownX = x;
        mDownY = y;
        mState = STATE_PRESSED;
    }

    public void clearPressedState() {
        mState = STATE_NORMAL;
    }
}

