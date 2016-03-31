package com.rnfstudio.babytracker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rnfstudio.babytracker.db.Profile;
import com.rnfstudio.babytracker.db.ProfileContract;
import com.rnfstudio.babytracker.utility.ProfileImageTask;
import com.rnfstudio.babytracker.utility.ProfilePictureDialogFragment;
import com.rnfstudio.babytracker.utility.Utilities;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.Calendar;

/**
 * Created by Roger on 2016/3/13.
 */
public class WelcomeActivity extends FragmentActivity
        implements ProfileImageTask.ProfileImageCallback {

    private static final String TAG = "[WelcomeActivity]";
    private static final String SP_KEY_FIRST_USAGE = "first_usage";

    private static final int[] SETUP_PAGES = {
            R.layout.fragment_setup_welcome,
            R.layout.fragment_setup_name,
            R.layout.fragment_setup_gender,
            R.layout.fragment_setup_birth,
            R.layout.fragment_setup_picture,
            R.layout.fragment_setup_complete};

    private int mPageIndex = 0;
    private int mBackKeyPressCount = 0;
    private static Profile sProfile;
    private Button mPrevButton;
    private Button mNextButton;
    Uri cropInputUri;
    Uri cropOutputUri;

    public static class WelcomeFragment extends Fragment {
        private static final String ARG_PAGE_INDEX = "page_index";
        private int mPageIndex;
        private View mViewRoot;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            mPageIndex = getArguments().getInt(ARG_PAGE_INDEX);

            View root = mViewRoot = inflater.inflate(SETUP_PAGES[mPageIndex], container, false);

            // load default/edited settings
            switch (SETUP_PAGES[mPageIndex]) {
                case R.layout.fragment_setup_welcome:
                    TextView welcomeText = (TextView) root.findViewById(R.id.welcomeText);
                    String appName = getResources().getString(R.string.app_name);
                    welcomeText.setText(getResources()
                            .getString(R.string.welcome_setup_title, appName));
                    break;

                case R.layout.fragment_setup_name:
                    EditText nameEdit = (EditText) root.findViewById(R.id.nameEdit);
                    nameEdit.setText(sProfile.getName());
                    break;

                case R.layout.fragment_setup_gender:
                    RadioGroup genderGroup = (RadioGroup) root.findViewById(R.id.genderRadioGroup);
                    switch (sProfile.getGender()) {
                        case ProfileContract.GENDER_UNSET:
                            genderGroup.clearCheck();
                            break;
                        case ProfileContract.GENDER_BOY:
                            genderGroup.check(R.id.radioButtonBoy);
                            break;
                        case ProfileContract.GENDER_GIRL:
                            genderGroup.check(R.id.radioButtonGirl);
                            break;
                    }
                    break;

                case R.layout.fragment_setup_birth:
                    DatePicker datePicker = (DatePicker) root.findViewById(R.id.datePicker);
                    datePicker.updateDate(sProfile.getBirthYear(), sProfile.getBirthMonth() - 1,
                            sProfile.getBirthDay());
                    break;

                case R.layout.fragment_setup_picture:
                    ImageButton cameraButton = (ImageButton) root.findViewById(R.id.buttonCamera);
                    cameraButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProfilePictureDialogFragment.startCameraSafely(getActivity());
                        }
                    });

                    ImageButton galleryButton = (ImageButton) root.findViewById(R.id.buttonGallery);
                    galleryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProfilePictureDialogFragment.pickPictureFromGallery(getActivity());
                        }
                    });

                    if (sProfile.getProfilePicture() != null) {
                        ImageView profileImageView = (ImageView) root.findViewById(R.id.profileImage);
                        profileImageView.setImageBitmap(sProfile.getProfilePicture());
                    }

                    break;
            }

            return root;
        }

        @Override
        public void onPause() {
            super.onPause();

            // save settings to Profile object
            switch (SETUP_PAGES[mPageIndex]) {
                case R.layout.fragment_setup_name:
                    EditText nameEdit = (EditText) mViewRoot.findViewById(R.id.nameEdit);
                    sProfile.setName(nameEdit.getText().toString());
                    Log.v(TAG, "[onPause] setup name: " + nameEdit.getText().toString());
                    break;

                case R.layout.fragment_setup_gender:
                    RadioGroup genderGroup = (RadioGroup) mViewRoot
                            .findViewById(R.id.genderRadioGroup);

                    switch (genderGroup.getCheckedRadioButtonId()) {
                        case R.id.radioButtonBoy:
                            sProfile.setGender(ProfileContract.GENDER_BOY);
                            Log.v(TAG, "[onPause] setup gender: boy");
                            break;
                        case R.id.radioButtonGirl:
                            sProfile.setGender(ProfileContract.GENDER_GIRL);
                            Log.v(TAG, "[onPause] setup gender: girl");
                            break;
                        default:
                            Log.v(TAG, "[onPause] setup gender: none");
                            break;
                    }
                    break;

                case R.layout.fragment_setup_birth:
                    DatePicker datePicker = (DatePicker) mViewRoot.findViewById(R.id.datePicker);
                    sProfile.setBirth(datePicker.getYear(),
                            datePicker.getMonth() + 1,
                            datePicker.getDayOfMonth());
                    Log.v(TAG, "[onPause] setup birth: " + String.format("%d/%d/%d",
                            datePicker.getYear(),
                            datePicker.getMonth() + 1,
                            datePicker.getDayOfMonth()));
                    break;
            }
        }

        public void setProfileImage(Bitmap bitmap) {
            ImageView profileImageView = (ImageView) mViewRoot.findViewById(R.id.profileImage);
            if (profileImageView != null) {
                Utilities.animSwitchImageRes(getActivity(), profileImageView, bitmap);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        // load splash screen for normal startup
        if (!isFirstUsage()) {
            loadSplashScreen();
            return;
        }

        // load setup screen for the 1st usage
        setContentView(R.layout.activity_welcome);

        // create Profile instance
        Calendar c = Calendar.getInstance();
        sProfile = new Profile((long) -1, "",
                ProfileContract.GENDER_UNSET,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                null);

        switchToPage(mPageIndex);

        mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int targetPage = mPageIndex + 1;
                if (targetPage >= 0 && targetPage < SETUP_PAGES.length) {
                    switchToPage(targetPage);

                } else if (targetPage == SETUP_PAGES.length) {
                    completeSetup();
                }
            }
        });

        mPrevButton = (Button) findViewById(R.id.button_prev);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int targetPage = mPageIndex - 1;
                if (targetPage >= 0 && targetPage < SETUP_PAGES.length) {
                    switchToPage(targetPage);
                }
            }
        });

        updatePrevNextButton();

        // initialize Uri for cropped image
        cropOutputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
    }

    /**
     * See <a href="http://stackoverflow.com/questions/5486789/how-do-i-make-a-splash-screen">
     *      How do I make a splash screen?</a>
     */
    private void loadSplashScreen() {
        setContentView(R.layout.activity_splash);

        // do init tasks here
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                MainApplication.loadDefaultUserProfile(getApplicationContext());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.v(TAG, "[loadSplashScreen] sleep interrupted");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.d(TAG, "[loadSplashScreen] done loading, now lets go to main page");
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // reset back key press count
        mBackKeyPressCount = 0;
    }

    private void updatePrevNextButton() {
        // show / hide previous button
        if (mPrevButton != null) {
            mPrevButton.setVisibility(mPageIndex == 0 ? View.GONE: View.VISIBLE);
        }

        // update next button text
        if (mNextButton != null) {
            mNextButton.setText(mPageIndex == 0 ? getResources().getString(R.string.go_setup) :
                    getResources().getString(R.string.next));
        }
    }

    public void completeSetup() {
        // save profile
        if (sProfile.writeDB(this)) {
            // update user id
            MainApplication.setUserId(this, sProfile.getId());

            // use new profile
            MainApplication.setUserProfile(sProfile);

            // mark as completed
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(SP_KEY_FIRST_USAGE, false).apply();

            // to main page
            startActivity(new Intent(this, MainActivity.class));
            finish();

        } else {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * See <a href="http://developer.android.com/intl/zh-tw/training/basics/fragments/creating.html">
     *     Creating a Fragment</a>
     */
    private void switchToPage(int pageIndex) {
        WelcomeFragment newFragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putInt(WelcomeFragment.ARG_PAGE_INDEX, pageIndex);
        newFragment.setArguments(args);
        android.support.v4.app.FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment, String.valueOf(pageIndex));
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        // update page index
        mPageIndex = pageIndex;

        // reset back key press count
        mBackKeyPressCount = 0;

        updatePrevNextButton();
    }

    private boolean isFirstUsage() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SP_KEY_FIRST_USAGE, true);
    }

    @Override
    public void onBackPressed() {
        onBackPressedImpl();
    }

    private void onBackPressedImpl() {
        int targetPage = mPageIndex - 1;
        if (targetPage >= 0 && targetPage < SETUP_PAGES.length) {
            switchToPage(targetPage);

        } else if (targetPage < 0) {
            if (mBackKeyPressCount == 0) {
                Toast.makeText(this, R.string.back_key_exit_toast, Toast.LENGTH_SHORT).show();
                mBackKeyPressCount += 1;
            } else {
                finish();
            }
        }
    }

    /**
     * Receives image capture results and starts cropping profile image
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            cropInputUri = Uri.fromFile(new File(ProfilePictureDialogFragment.sCurrentPhotoPath));
            Crop.of(cropInputUri, cropOutputUri).asSquare().start(this);

        } else if (requestCode == MainActivity.REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
            cropInputUri = data.getData();
            Crop.of(cropInputUri, cropOutputUri).asSquare().start(this);

        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            new ProfileImageTask(this, sProfile, cropOutputUri, this, false).execute();
        }
    }

    /**
     * Receives cropped image and update profile
     */
    @Override
    public void OnProfileImageUpdated(Profile profile, Bitmap bitmap) {
        // update data
        sProfile.setProfilePicture(bitmap);

        // update view
        WelcomeFragment frag = (WelcomeFragment) getSupportFragmentManager()
                .findFragmentByTag(String.valueOf(mPageIndex));
        frag.setProfileImage(bitmap);
    }
}
