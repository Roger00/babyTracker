package com.rnfstudio.babytracker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roger on 2015/7/5.
 */
public class MainMenuAdapter extends BaseAdapter {
    private final Context mContext;
    private List<MainMenuItem> mItems = new ArrayList<MainMenuItem>();

    public MainMenuAdapter(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        List<MainMenuItem> items = new ArrayList<MainMenuItem>();

        String[] menuItemIds = MainMenuItem.MENU_ITEMS;
        for (String id : menuItemIds) {
            mItems.add(new MainMenuItem(mContext, id));
        }
    }

    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.swipe_button, parent, false);
        } else {
            v = convertView;
        }
        return v;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.d("xxxxx", "getView called");
//
//        View v;
//        if (convertView == null) {
//            // if it's not recycled, initialize some attributes
//            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            v = inflater.inflate(R.layout.menu_item, parent, false);
//        } else {
//            v = convertView;
//        }
//
//        final MainMenuItem item = (MainMenuItem) getItem(position);
//        if (item != null) {
//            TextView title = (TextView) v.findViewById(R.id.title);
//            title.setText(item.getName());
//
//            if (item.getId().equals(MainMenuItem.MENU_ITEM_SLEEP)) {
//                v.setOnClickListener(new View.OnClickListener() {
//                     @Override
//                     public void onClick(View v) {
//                         TextView title = (TextView) v.findViewById(R.id.title);
//                         String timeStr = (item.getState() == ButtonState.RUNNING) ? item.getName() : "0:00:00";
//                         if (title != null) title.setText(timeStr);
//
//                         String nextState = item.getState() == ButtonState.RUNNING ? ButtonState.STOP : ButtonState.RUNNING;
//                         item.setState(nextState);
//                         item.setStartTime(System.currentTimeMillis());
//                     }
//                });
//
//            } else if (item.getId().equals(MainMenuItem.MENU_ITEM_DIAPER)) {
//                v.setOnClickListener(new View.OnClickListener() {
//                     @Override
//                     public void onClick(View v) {
//                         AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                         AlertDialog dialog = builder.setTitle("HI").setMessage("Hello").create();
//                         View mealSelectView = inflater.inflate(R.layout.meal_selector, null);
//                         dialog.setView(mealSelectView);
//                         dialog.show();
//                     }
//                });
//
//            } else if (item.getId().equals(MainMenuItem.MENU_ITEM_MEAL)) {
//                v.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        int x = (int) event.getX();
//                        int y = (int) event.getY();
//                        int downX = item.getPressedState().getDownX();
//                        int downY = item.getPressedState().getDownY();
//                        int gestureDirection = DirectionUtils.getDirection(downX, downY, x, y);
//                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                            Log.d("xxxxx", "down at" + x + ", " + y);
//                            item.getPressedState().setPressedLocation(x, y);
//
//                            PopupWindow popup = getTooltipPopup(item, v.getWidth(), v.getHeight());
//                            item.setPopupWindow(popup);
//                            int[] viewCoord = new int[2];
//                            v.getLocationInWindow(viewCoord);
//                            popup.showAtLocation(v, Gravity.NO_GRAVITY, viewCoord[0], viewCoord[1] - v.getHeight());
//
//                            return true;
//
//                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                            Log.d("xxxxx", "up at" + x + ", " + y);
//                            item.getPressedState().clearPressedState();
//
//                            if (item.getPopupWindow() != null) item.getPopupWindow().dismiss();
//
//                            return true;
//
//                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                            if (item.getPressedState().isPressed()) {
//                                Log.d("xxxxx", "move at" + x + ", " + y + ", direction: " + gestureDirection);
//
//                                TextView tv = (TextView) item.getPopupWindow().getContentView().findViewById(R.id.popupText);
////                                tv.setText(item.getSubItem(gestureDirection));
//                            }
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//            }
//        }
//
//        // associate MainMenuItem to the view
//        v.setTag(item);
//
//        return v;
//    }
//
//    private boolean hitTest(View v, int x, int y) {
//        Rect rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
//        Log.d("xxx", "rect:" + rect.toShortString());
//        return rect.contains(x, y);
//    }
//
//    public List<MainMenuItem> getItems() { return mItems; }
//
//    private PopupWindow getTooltipPopup(MainMenuItem item, int w, int h) {
//        PopupWindow popup = item.getPopupWindow();
//        if (popup == null) {
//            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            popup = new PopupWindow(inflater.inflate(R.layout.popup_tooltip, null), w, h, false);
//        }
//        popup.setWidth(w);
//        popup.setHeight(h);
//
//        return popup;
//    }

}
