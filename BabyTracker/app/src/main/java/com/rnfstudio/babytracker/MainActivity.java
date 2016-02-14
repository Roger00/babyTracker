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
import android.util.SparseArray;
import android.view.Window;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.utility.SlidingTabLayout;
import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.Calendar;

/**
 * Modified from example:
 * http://developer.android.com/intl/zh-tw/training/implementing-navigation/lateral.html
 */
public class MainActivity extends FragmentActivity implements OnEventChangedListener {
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

        private SparseArray<Fragment> mFragmentMap = new SparseArray<>();

        public SubCategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(TAG, "[getItem] called, index: " + i);

            // lazy load fragments
            Fragment fragment = mFragmentMap.get(i);
            if (fragment == null) {
                Log.d(TAG, "[getItem] create new fragment instance");
                fragment = i == TAB_ID_MAIN ? new MainFragment() : new RecordListFragment();
                mFragmentMap.put(i, fragment);

                Bundle args = new Bundle();
                args.putInt(RecordListFragment.ARG_TAB_ID, i);
                fragment.setArguments(args);
            }

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

    public static class SubCategoryPageChangeListener implements ViewPager.OnPageChangeListener {
        public static final int DIRTY_FLAG_SLEEP = 1;
        public static final int DIRTY_FLAG_MEAL = 1 << 1;
        public static final int DIRTY_FLAG_DIAPER = 1 << 2;

        private static int sDirtyFlags = 0;

        private SubCategoryPagerAdapter mPagerAdapter;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            Log.v(TAG, "[onPageSelected] position: " + position);

            // MainActivity works as a Mediator
            // notify the affected fragment
            int mainType;
            switch (position) {
                case TAB_ID_SLEEP:
                    mainType = EventContract.EventEntry.EVENT_TYPE_SLEEP;
                    break;
                case TAB_ID_MEAL:
                    mainType = EventContract.EventEntry.EVENT_TYPE_MEAL;
                    break;
                case TAB_ID_DIAPER:
                    mainType = EventContract.EventEntry.EVENT_TYPE_DIAPER;
                    break;
                default:
                    return;
            }

            RecordListFragment frag = (RecordListFragment)
                    mPagerAdapter.getItem(position);

            // let's always notify event change when page changed
            // TODO: use ContentProvider instead
            if (frag != null) {
                frag.onEventChanged(mainType);
            } else {
                Log.d(TAG, "[onPageSelected] fail to find fragment for mainType: " + mainType);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}

        public void setAdapter(SubCategoryPagerAdapter adapter) {
            mPagerAdapter = adapter;
        }

        /**
         * For bit operations, refer to:
         * http://stackoverflow.com/questions/47981/how-do-you-set-clear-and-toggle-a-single-bit-in-c-c
         * @param mainType
         */
        public void setDirtyFlag(int mainType) {
            Log.v(TAG, "[setDirtyFlag] receive event change for mainType: " + mainType);

            Log.d(TAG, "[setDirtyFlag] sDirtyFlags(before): " + sDirtyFlags);
            switch (mainType) {
                case EventContract.EventEntry.EVENT_TYPE_SLEEP:
                    sDirtyFlags |= DIRTY_FLAG_SLEEP;
                    break;
                case EventContract.EventEntry.EVENT_TYPE_MEAL:
                    sDirtyFlags |= DIRTY_FLAG_MEAL;
                    break;
                case EventContract.EventEntry.EVENT_TYPE_DIAPER:
                    sDirtyFlags |= DIRTY_FLAG_DIAPER;
                    break;
            }
            Log.d(TAG, "[setDirtyFlag] sDirtyFlags(after): " + sDirtyFlags);
        }

        public boolean checkDirtyFlag(int mainType) {
            Log.v(TAG, "[checkDirtyFlag] receive event change for mainType: " + mainType);

            Log.d(TAG, "[checkDirtyFlag] sDirtyFlags(before): " + sDirtyFlags);

            boolean checked = false;
            switch (mainType) {
                case EventContract.EventEntry.EVENT_TYPE_SLEEP:
                    checked = (sDirtyFlags & DIRTY_FLAG_SLEEP) != 0;
                    break;
                case EventContract.EventEntry.EVENT_TYPE_MEAL:
                    checked = (sDirtyFlags & DIRTY_FLAG_MEAL) != 0;
                    break;
                case EventContract.EventEntry.EVENT_TYPE_DIAPER:
                    checked = (sDirtyFlags & DIRTY_FLAG_DIAPER) != 0;
                    break;
            }
            Log.d(TAG, "[checkDirtyFlag] checked " + checked);
            return checked;
        }

        public void clearDirtyFlag(int mainType) {
            Log.v(TAG, "[clearDirtyFlag] receive event change for mainType: " + mainType);

            Log.d(TAG, "[clearDirtyFlag] sDirtyFlags(before): " + sDirtyFlags);
            switch (mainType) {
                case EventContract.EventEntry.EVENT_TYPE_SLEEP:
                    sDirtyFlags &= ~DIRTY_FLAG_SLEEP;
                    break;
                case EventContract.EventEntry.EVENT_TYPE_MEAL:
                    sDirtyFlags &= ~DIRTY_FLAG_MEAL;
                    break;
                case EventContract.EventEntry.EVENT_TYPE_DIAPER:
                    sDirtyFlags &= ~DIRTY_FLAG_DIAPER;
                    break;
            }
            Log.d(TAG, "[clearDirtyFlag] sDirtyFlags(after): " + sDirtyFlags);
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
    SubCategoryPageChangeListener mSubCategoryPageChangeListener;

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

        // create listener for page change events
        mSubCategoryPageChangeListener = new SubCategoryPageChangeListener();
        mSubCategoryPageChangeListener.setAdapter(mSubCategoryPagerAdapter);

        // Initialize the SlidingTabLayout. Note that the order is important.
        // First init ViewPager and Adapter and only then init SlidingTabLayout
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.custom_tab, 0);
        mSlidingTabLayout.setViewPager(mViewPager);

        // re-direct page change events to our listener
        mSlidingTabLayout.setOnPageChangeListener(mSubCategoryPageChangeListener);

        // initialize days from birth string
        TextView daysFromBirth = (TextView) findViewById(R.id.daysFromBirth);
        daysFromBirth.setText(getDaysFromBirthString());
    }

    @Override
    public void onEventChanged(int mainType) {
        Log.v(TAG, "[onEventChanged] receive event change for mainType: " + mainType);

        // cache dirty flags
        mSubCategoryPageChangeListener.setDirtyFlag(mainType);
    }

    private String getDaysFromBirthString() {
        Calendar birth = Calendar.getInstance();
        birth.set(Calendar.YEAR, 2015);
        birth.set(Calendar.MONTH, Calendar.MARCH );
        birth.set(Calendar.DAY_OF_MONTH, 18);

        int daysBetween = TimeUtils.daysBetween(birth, Calendar.getInstance());
        int days = TimeUtils.getRemainDaysInMonth(daysBetween);
        int months = TimeUtils.getRemainMonthsInYear(daysBetween);
        int years = TimeUtils.getRemainYears(daysBetween);

        if (years > 0) {
            return getResources().getQuantityString(R.plurals.info_years_since_birth,
                    years, years, months, days);
        } else if (months > 0) {
            return getResources().getQuantityString(R.plurals.info_months_since_birth,
                    months, months, days);
        } else if (days > 0) {
            return getResources().getQuantityString(R.plurals.info_days_since_birth, days, days);
        } else {
            return getResources().getString(R.string.last_info_default_message);
        }
    }
}