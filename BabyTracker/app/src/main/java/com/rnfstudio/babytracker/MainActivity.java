package com.rnfstudio.babytracker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.EventProvider;
import com.rnfstudio.babytracker.db.Profile;
import com.rnfstudio.babytracker.db.ProfileContract;
import com.rnfstudio.babytracker.utility.ProfileImageTask;
import com.rnfstudio.babytracker.utility.ProfilePictureDialogFragment;
import com.rnfstudio.babytracker.utility.RoundedImageView;
import com.rnfstudio.babytracker.utility.SlidingTabLayout;
import com.rnfstudio.babytracker.utility.TimeUtils;
import com.rnfstudio.babytracker.utility.Utilities;

import java.util.Calendar;

/**
 * Modified from example:
 * http://developer.android.com/intl/zh-tw/training/implementing-navigation/lateral.html
 */
public class MainActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ProfileImageTask.ProfileImageCallback {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    public static class SubCategoryPagerAdapter extends FragmentPagerAdapter {
        private Context mContext;

        private int[] imageResId = {
                R.drawable.ic_home_black_24dp,
                R.drawable.ic_sleep,
                R.drawable.ic_local_dining_black_24dp,
                R.drawable.ic_diaper
        };

        private SparseArray<Fragment> mFragmentMap = new SparseArray<>();

        public SubCategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            // lazy load fragments
            Fragment fragment = mFragmentMap.get(i);

            if (fragment == null) {
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
            return TABS_COUNT;
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
        private SubCategoryPagerAdapter mPagerAdapter;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {}

        @Override
        public void onPageScrollStateChanged(int state) {}

        public void setAdapter(SubCategoryPagerAdapter adapter) {
            mPagerAdapter = adapter;
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[MainActivity]";

    public static final int TABS_COUNT = 4;
    public static final int TAB_ID_MAIN = 0;
    public static final int TAB_ID_SLEEP = 1;
    public static final int TAB_ID_MEAL = 2;
    public static final int TAB_ID_DIAPER = 3;

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_SELECT = 2;
    public static final int REQUEST_PROFILE_EDIT = 3;

    public static final int LOADER_ID_PROFILE = 2;
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

    TextView mDisplayName;
    TextView mDaysFromBirth;
    RoundedImageView mProfileImage;
    Profile mProfile;

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
        mSlidingTabLayout.setCustomTabView(R.layout.custom_tab, R.id.customText);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources()
                .getColor(android.R.color.holo_orange_dark));

        // re-direct page change events to our listener
        mSlidingTabLayout.setOnPageChangeListener(mSubCategoryPageChangeListener);

        // initialize days from birth string
        mDaysFromBirth = (TextView) findViewById(R.id.daysFromBirth);
//        mDaysFromBirth.setText(getDaysFromBirthString());

        View.OnClickListener profileEditListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, ProfileEditActivity.class),
                        REQUEST_PROFILE_EDIT);
            }
        };

        // click on display name or days from birth text should trigger profile edit
        mDisplayName = (TextView) findViewById(R.id.displayName);
        mDisplayName.setOnClickListener(profileEditListener);
        mDaysFromBirth.setOnClickListener(profileEditListener);

        mProfileImage = (RoundedImageView) findViewById(R.id.profileImage);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new ProfilePictureDialogFragment();
                newFragment.show(MainActivity.this.getSupportFragmentManager(),
                        ProfilePictureDialogFragment.TAG);
            }
        });

        setProfile(MainApplication.getUserProfile());
//        // initialize cursor loader
//        getSupportLoaderManager().initLoader(LOADER_ID_PROFILE, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_PROFILE) {
            String selection = ProfileContract.UserEntry._ID + "=?";
            String[] selectionArgs = new String[] {String.valueOf(MainApplication.getUserId(this))};

            return new CursorLoader(this,
                    EventProvider.sNotifyUriForUser,
                    new String[] {ProfileContract.UserEntry._ID,
                            ProfileContract.UserEntry.COLUMN_NAME_DISPLAY_NAME,
                            ProfileContract.UserEntry.COLUMN_NAME_GENDER,
                            ProfileContract.UserEntry.COLUMN_NAME_BIRTH_YEAR,
                            ProfileContract.UserEntry.COLUMN_NAME_BIRTH_MONTH,
                            ProfileContract.UserEntry.COLUMN_NAME_BIRTH_DAY,
                            ProfileContract.UserEntry.COLUMN_NAME_PROFILE_PICTURE},
                    selection, selectionArgs,
                    ProfileContract.UserEntry._ID + " DESC");
        }

        Log.w(TAG, "[onCreateLoader] incorrect ID");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        setProfile(Profile.createFromCursor(data));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    public void setProfile(Profile p) {
        setProfile(p, true);
    }

    public void setProfile(Profile p, boolean animated) {
        // update profile
        mProfile = p;

        MainApplication.setUserProfile(p);

        // update views
        mDisplayName.setText(mProfile.getName());
        mDaysFromBirth.setText(getDaysFromBirthString());

        if (animated) {
            Utilities.animSwitchImageRes(this, mProfileImage, mProfile.getProfilePicture());
        } else {
            mProfileImage.setImageBitmap(mProfile.getProfilePicture());
        }

    }

    private String getDaysFromBirthString() {
        Calendar birth = Calendar.getInstance();
        birth.set(Calendar.YEAR, mProfile.getBirthYear());
        birth.set(Calendar.MONTH, mProfile.getBirthMonth() - 1);
        birth.set(Calendar.DAY_OF_MONTH, mProfile.getBirthDay());

        int daysBetween = TimeUtils.daysBetween(birth, Calendar.getInstance());
        int days = TimeUtils.getRemainDaysInMonth(daysBetween);
        int months = TimeUtils.getRemainMonthsInYear(daysBetween);
        int years = TimeUtils.getRemainYears(daysBetween);

        Log.d(TAG, String.format("%d, %d, %d, %d", daysBetween, years, months, days));

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String imageFilePath = ProfilePictureDialogFragment.sCurrentPhotoPath;
            new ProfileImageTask(this, mProfile, imageFilePath, null, this, true).execute();

        } else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
            new ProfileImageTask(this, mProfile, null, data.getData(), this, true).execute();

        } else if (requestCode == REQUEST_PROFILE_EDIT && resultCode == RESULT_OK) {
            setProfile(MainApplication.getUserProfile());
        }
    }

    @Override
    public void OnProfileImageUpdated(Profile profile, Bitmap bitmap) {
        profile.setProfilePicture(bitmap);
        setProfile(profile);
    }
}