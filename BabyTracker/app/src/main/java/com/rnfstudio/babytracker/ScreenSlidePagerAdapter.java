package com.rnfstudio.babytracker;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roger on 2015/12/14.
 *
 * Adapted from codes:
 * <a href="http://developer.android.com/intl/zh-tw/training/animation/screen-slide.html#views">
 *     Android ViewPager Tutorial</a>
 * <a href="http://stackoverflow.com/questions/13664155/dynamically-add-and-remove-view-to-viewpager">
 *     Dynamically add and remove view to viewPager</>
 */
public class ScreenSlidePagerAdapter extends PagerAdapter {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "ScreenSlidePagerAdapter";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private Context mContext;
    private List<View> mViews = new ArrayList<>();

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public ScreenSlidePagerAdapter(Context context) {
        mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);

        for (int i = 0; i < 3; i++) {
            View circleView = inflater.inflate(R.layout.circle_panel, null);
            TextView titleView = (TextView) circleView.findViewById(R.id.title);
            titleView.setText(String.valueOf(i));
            mViews.add(circleView);
        }
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, "instantiateItem called, position: " + position);
        View v = mViews.get(position);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView (mViews.get(position));
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
