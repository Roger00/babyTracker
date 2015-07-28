package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rnfstudio.babytracker.R;

/**
 * Created by Roger on 2015/7/14.
 */
public class SwipeButton extends TextView {
    private static final String TAG = "SwipeButton";
    private static final boolean DEBUG = false;

    private static final String EMPTY_COMMAND = "";

    private static PopupWindow sPopup;

    private PressState mPressedState;
    private String mMainFuncId;
    private String mLeftFuncId;
    private String mRightFuncId;
    private String mUpFuncId;
    private String mDownFuncId;
    private Handler mHandler;

    public interface Handler {
        public void OnClick(Context context, String cmd);
        public void addSwipeButton(SwipeButton btn);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SwipeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwipeButton(Context context) {
        super(context);
        init(context, null);
    }

    private void init(final Context context, AttributeSet attrs) {
        mPressedState = new PressState();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // decide gesture
                int x = (int) event.getX();
                int y = (int) event.getY();
                int downX = SwipeButton.this.getPressedState().getDownX();
                int downY = SwipeButton.this.getPressedState().getDownY();
                int gestureDirection = DirectionUtils.getDirection(downX, downY, x, y);

                // decide which function to use
                float distance = getDistance(downX, downY, x, y);
                float centerFuncThreshold = context.getResources().getDimension(R.dimen.swipe_button_center_function_threshold);
                boolean enabledSubFunc = distance >= centerFuncThreshold;
                String command = getCommand(context, enabledSubFunc, gestureDirection);

                Resources res = context.getResources();
                int resId = res.getIdentifier(command, "string", "com.rnfstudio.babytracker");
                String displayCmd = "";
                if (resId != 0) {
                    try {
                        displayCmd = res.getString(resId);
                    } catch (Resources.NotFoundException e) {
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (DEBUG)
                        Log.v(TAG, "[onTouch] ACTION_DOWN at: " + String.format("(%d,%d)", x, y));

                    // set pressed
                    getPressedState().setPressedLocation(x, y);
                    setPressed(true);

                    // show tooltip
                    showTooltip(context, v, displayCmd);

                    // send haptic feedback
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(20);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (DEBUG)
                        Log.v(TAG, "[onTouch] ACTION_UP/ACTION_CANCEL at: " + String.format("(%d,%d)", x, y));

                    // clear pressed state
                    SwipeButton.this.getPressedState().clearPressedState();
                    setPressed(false);

                    // dismiss tooltip
                    if (SwipeButton.this.getPopupWindow(context) != null) {
                        SwipeButton.this.getPopupWindow(context).dismiss();
                    }

                    // trigger onClick
                    mHandler.OnClick(context, command);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (SwipeButton.this.getPressedState().isPressed()) {
                        if (DEBUG)
                            Log.v(TAG, "[onTouch] ACTION_MOVE at: " + String.format("(%d,%d), direction: %d", x, y, gestureDirection));

                        // update tooltip
                        updateTooltip(context, displayCmd);
                    }

                    return true;
                }

                // MotionEvent not handled, return false
                return false;
            }
        });

        if (attrs == null) {
            return;
        }

        // initialize sub-functions
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SwipeButton,
                0, 0);

        try {
            mMainFuncId = a.getString(R.styleable.SwipeButton_main);
            mLeftFuncId = a.getString(R.styleable.SwipeButton_left);
            mRightFuncId = a.getString(R.styleable.SwipeButton_right);
            mUpFuncId = a.getString(R.styleable.SwipeButton_up);
            mDownFuncId = a.getString(R.styleable.SwipeButton_down);
        } finally {
            a.recycle();
        }

        Resources res = context.getResources();
        int displayStrId = res.getIdentifier(mMainFuncId, "string", "com.rnfstudio.babytracker");
        setText(res.getString(displayStrId));
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.menu_item_text_size));
        setGravity(Gravity.CENTER);
        setTextColor(getResources().getColor(android.R.color.white));

        // set background
        Drawable bgDrawable = context.getResources().getDrawable(R.drawable.swipe_button_background);
        setBackground(bgDrawable);
    }

    private void showTooltip(Context context, View v, String command) {
        PopupWindow popup = getPopupWindow(context);
        popup.setHeight(v.getHeight());
        popup.setWidth(v.getWidth());
        int[] originXY = new int[2];
        v.getLocationInWindow(originXY);

        updateTooltip(context, command);
        popup.showAtLocation(v, Gravity.NO_GRAVITY, originXY[0], originXY[1] - v.getHeight());
    }

    private void updateTooltip(Context context, String text) {
        if (!TextUtils.isEmpty(text)) {
            TextView tv = (TextView) SwipeButton.this.getPopupWindow(context).getContentView().findViewById(R.id.popupText);
            tv.setText(text);
        }
    }

    private float getDistance(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    private PressState getPressedState() {
        return mPressedState;
    }

    private PopupWindow getPopupWindow(Context context) {
        if (sPopup == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            sPopup = new PopupWindow(inflater.inflate(R.layout.popup_tooltip, null), 0, 0, false);
        }
        return sPopup;
    }

    private String getCommand(Context context, boolean enabledSubFunc, int gestureDirection) {
        if (!enabledSubFunc) {
            return TextUtils.isEmpty(mMainFuncId) ? EMPTY_COMMAND : mMainFuncId;
        }

        String command = mMainFuncId;
        switch (gestureDirection) {
            case DirectionUtils.DIRECTION_LEFT:
                command = mLeftFuncId;
                break;
            case DirectionUtils.DIRECTION_RIGHT:
                command = mRightFuncId;
                break;
            case DirectionUtils.DIRECTION_UP:
                command = mUpFuncId;
                break;
            case DirectionUtils.DIRECTION_DOWN:
                command = mDownFuncId;
                break;
            default:
                break;
        }
        return TextUtils.isEmpty(command) ? EMPTY_COMMAND : command;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
        mHandler.addSwipeButton(this);
    }
}
