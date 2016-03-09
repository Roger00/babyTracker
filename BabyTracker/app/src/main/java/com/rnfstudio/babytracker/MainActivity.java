package com.rnfstudio.babytracker;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
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
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.db.EventProvider;
import com.rnfstudio.babytracker.utility.MenuDialogFragment;
import com.rnfstudio.babytracker.utility.ProfilePictureDialogFragment;
import com.rnfstudio.babytracker.utility.RoundedImageView;
import com.rnfstudio.babytracker.utility.SlidingTabLayout;
import com.rnfstudio.babytracker.utility.TimeUtils;
import com.rnfstudio.babytracker.utility.Utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

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

    RoundedImageView mProfileImage;

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
        TextView daysFromBirth = (TextView) findViewById(R.id.daysFromBirth);
        daysFromBirth.setText(getDaysFromBirthString());

        mProfileImage = (RoundedImageView) findViewById(R.id.profileImage);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new ProfilePictureDialogFragment();
                newFragment.show(MainActivity.this.getSupportFragmentManager(),
                        ProfilePictureDialogFragment.TAG);
            }
        });

        new AsyncTask<Void, Void, Bitmap>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected Bitmap doInBackground(Void... params) {
                long userId = MainApplication.getUserId(getApplicationContext());

                try(Cursor c = getContentResolver().query(EventProvider.sNotifyUriForUser,
                        new String[]{EventContract.UserEntry.COLUMN_NAME_PROFILE_PICTURE},
                        EventContract.UserEntry._ID + "=?",
                        new String[]{String.valueOf(userId)},
                        null)) {

                    if (c != null && c.moveToNext()) {
                        byte[] rawBitmap = c.getBlob(0);

                        if (rawBitmap != null) {
                            return BitmapFactory.decodeByteArray(rawBitmap, 0, rawBitmap.length);
                        }
                    }
                } catch (IllegalStateException ise) {
                    Log.w(TAG, "Exception when load profile image: " + ise.toString());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
                    Log.w(TAG, "fail to load or decode profile picture");
                } else {
                    animSwitchImageRes(getApplicationContext(), mProfileImage, bitmap);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String imageFilePath = ProfilePictureDialogFragment.sCurrentPhotoPath;
            Log.d(TAG, "data: " + imageFilePath);

            Bitmap image = BitmapFactory.decodeFile(imageFilePath);
            if (image != null) {
                mProfileImage.setImageBitmap(getCenterBitmap(image));
            } else {
                Log.w(TAG, "[onActivityResult] decode fail");
                Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
            }

        } else {
            if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
                Log.d(TAG, "data: " + data.toUri(0));

                new AsyncTask<Uri, Void, Bitmap>() {

                    @Override
                    protected Bitmap doInBackground(Uri... params) {
                        Bitmap bitmap = null;

                        try (InputStream is = getContentResolver().openInputStream(params[0])) {
                            bitmap = getCenterBitmap(BitmapFactory.decodeStream(is));

                            ContentValues cvs = new ContentValues();
                            long userId = MainApplication.getUserId(getApplicationContext());
                            cvs.put(EventContract.UserEntry._ID, userId);
                            cvs.put(EventContract.UserEntry.COLUMN_NAME_PROFILE_PICTURE, Utilities.encodeBitmap(bitmap));
                            getContentResolver().update(EventProvider.sNotifyUriForUser, cvs,
                                    EventContract.UserEntry._ID + "=?",
                                    new String[]{String.valueOf(userId)});

                        } catch (IOException ioe) {
                            Log.w(TAG, "[onActivityResult] exception: " + ioe.toString());
                        }

                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (bitmap == null) {
                            Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
                        } else {
                            animSwitchImageRes(getApplicationContext(), mProfileImage, bitmap);
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.getData());
            }
        }
    }

    /**
     * See <a href="http://stackoverflow.com/questions/6908604/android-crop-center-of-bitmap">
     *     Android Crop Center of Bitmap</a>
     */
    private Bitmap getCenterBitmap(Bitmap srcBitmap) {
        Bitmap dstBitmap;

        if (srcBitmap.getWidth() >= srcBitmap.getHeight()){
            dstBitmap = Bitmap.createBitmap(
                    srcBitmap,
                    srcBitmap.getWidth()/2 - srcBitmap.getHeight()/2,
                    0,
                    srcBitmap.getHeight(),
                    srcBitmap.getHeight()
            );

        } else {
            dstBitmap = Bitmap.createBitmap(
                    srcBitmap,
                    0,
                    srcBitmap.getHeight()/2 - srcBitmap.getWidth()/2,
                    srcBitmap.getWidth(),
                    srcBitmap.getWidth()
            );
        }

        return dstBitmap;
    }

    /**
     * See <a href="http://stackoverflow.com/questions/7161500/creating-animation-on-imageview-while-changing-image-resource">
     *     Creating animation on ImageView while changing image resource</a>
     */
    public static void animSwitchImageRes(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }
}