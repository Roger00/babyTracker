package com.rnfstudio.babytracker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Window;

import com.rnfstudio.babytracker.utility.SlidingTabLayout;

/**
 * Modified from example:
 * http://developer.android.com/intl/zh-tw/training/implementing-navigation/lateral.html
 */
public class MainActivity extends FragmentActivity {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    public static class SubCategoryPagerAdapter extends FragmentPagerAdapter {
        private Context mContext;

        private int[] imageResId = {
                R.drawable.star_pressed_resized,
                R.drawable.star_pressed_resized,
                R.drawable.star_pressed_resized,
                R.drawable.star_pressed_resized
        };

        public SubCategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = i == TAB_ID_MAIN ? new MainFragment() : new SubCategoryFragment();

            Bundle args = new Bundle();
            args.putInt(SubCategoryFragment.ARG_TAB_ID, i);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Drawable image = mContext.getResources().getDrawable(imageResId[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }

        public void setContext(Context context) {
            mContext = context;
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[MainActivity]";

    public static final int TAB_ID_MAIN = 0;
    public static final int TAB_ID_SLEEP = 1;
    public static final int TAB_ID_MEAL = 2;
    public static final int TAB_ID_DIAPER = 3;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    ViewPager mViewPager;
    SubCategoryPagerAdapter mSubCategoryPagerAdapter;
    SlidingTabLayout mSlidingTabLayout;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate called");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mSubCategoryPagerAdapter =
                new SubCategoryPagerAdapter(getSupportFragmentManager());
        mSubCategoryPagerAdapter.setContext(this);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSubCategoryPagerAdapter);

        // Initialize the SlidingTabLayout. Note that the order is important.
        // First init ViewPager and Adapter and only then init SlidingTabLayout
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.custom_tab, 0);
        mSlidingTabLayout.setViewPager(mViewPager);
    }
}



