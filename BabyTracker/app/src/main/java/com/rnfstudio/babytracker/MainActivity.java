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
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    public static final String TAG = "[MainActivity]";

    ViewPager mViewPager;
    SlidingTabLayout mSlidingTabLayout;

    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate called");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mDemoCollectionPagerAdapter.setContext(this);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        // Initialize the SlidingTabLayout. Note that the order is important. First init ViewPager and Adapter and only then init SlidingTabLayout
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.custom_tab, 0);
        mSlidingTabLayout.setViewPager(mViewPager);

//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                // This space for rent
////                Log.d(TAG, String.format("[onPageScrolled] position: %d, positionOffset: %.1f, positionOffsetPixels: %d",
////                        position, positionOffset, positionOffsetPixels));
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                // This space for rent
//                Log.d(TAG, "[onPageSelected] position: " + position);
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                // This space for rent
//                Log.d(TAG, "[onPageScrollStateChanged] state: " + state);
//            }
//        });
    }
}

class DemoCollectionPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private int[] imageResId = {
            R.drawable.star_pressed_resized,
            R.drawable.star_pressed_resized,
            R.drawable.star_pressed_resized,
            R.drawable.star_pressed_resized
    };

    public DemoCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = i == 0 ? new MainFragment() : new SubCategoryFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(SubCategoryFragment.ARG_OBJECT, i + 1);
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

