package com.rnfstudio.babytracker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.rnfstudio.babytracker.db.Profile;
import com.rnfstudio.babytracker.db.ProfileContract;
import com.rnfstudio.babytracker.utility.ProfileImageTask;
import com.rnfstudio.babytracker.utility.ProfilePictureDialogFragment;
import com.rnfstudio.babytracker.utility.Utilities;

import java.util.Calendar;

/**
 * Created by Roger on 2016/3/6.
 */
public class ProfileEditActivity extends FragmentActivity {

    private static final String TAG = "[ProfileEditActivity]";
    public static final String EXTRA_REQUEST_CODE = "request_code";

    private static Profile sProfile;
    private static boolean sIsCreateProfile;

    public static class ProfileEditFragment extends Fragment
            implements ProfileImageTask.ProfileImageCallback {

        private EditText mNameEdit;
        private EditText mBirthEdit;
        private RadioGroup mGenderRadioGroup;
        private Button mButtonCancel;
        private Button mButtonOkay;
        private ImageView mProfileImage;
        private boolean mProfileImageChanged;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {

            View root = inflater.inflate(R.layout.profile_editor, container, false);
            return initViews(root, getLocalUserProfile());
        }

        private Profile getLocalUserProfile() {
            if (ProfileEditActivity.sIsCreateProfile) {

                if (ProfileEditActivity.sProfile == null) {

                    Calendar c = Calendar.getInstance();
                    ProfileEditActivity.sProfile = new Profile((long) -1,
                            getString(R.string.default_user_name),
                            ProfileContract.GENDER_BOY,
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH) + 1,
                            c.get(Calendar.DAY_OF_MONTH),
                            null);
                }

                return ProfileEditActivity.sProfile;
            }

            return MainApplication.getUserProfile();
        }

        private View initViews(View root, final Profile profile) {
            mNameEdit = (EditText) root.findViewById(R.id.nameEdit);
            mNameEdit.setText(profile.getName());

            mBirthEdit = (EditText) root.findViewById(R.id.birthEdit);
            mBirthEdit.setText(profile.getBirthStr());

            mGenderRadioGroup = (RadioGroup) root.findViewById(R.id.genderRadioGroup);
            int checkedGenderWidgetId = profile.getGender() == ProfileContract.GENDER_BOY ?
                    R.id.radioButtonBoy : R.id.radioButtonGirl;
            mGenderRadioGroup.check(checkedGenderWidgetId);

            mProfileImage = (ImageView) root.findViewById(R.id.profileImage);
            if (profile.getProfilePicture() != null) {
                mProfileImage.setImageBitmap(profile.getProfilePicture());
            }
            mProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new ProfilePictureDialogFragment();
                    newFragment.show(getActivity().getSupportFragmentManager(),
                            ProfilePictureDialogFragment.TAG);
                }
            });

            // ok/cancel buttons
            mButtonCancel = (Button) root.findViewById(R.id.button_cancel);
            mButtonOkay = (Button) root.findViewById(R.id.button_ok);

            mButtonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),
                            R.string.edit_canceled, Toast.LENGTH_SHORT).show();

                    getActivity().setResult(RESULT_CANCELED);
                    getActivity().finish();
                }
            });

            mButtonOkay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean changed = updateProfile(profile);

                    // update database if necessary
                    if (changed) {

                        // lock UI
                        mButtonCancel.setEnabled(false);
                        mButtonOkay.setEnabled(false);

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(),
                                                R.string.edit_storing, Toast.LENGTH_SHORT).show();
                                    }
                                });

                                profile.writeDB(getActivity());
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                Toast.makeText(getActivity(),
                                        R.string.edit_successful, Toast.LENGTH_SHORT).show();

                                getActivity().setResult(RESULT_OK);
                                getActivity().finish();
                            }

                        }.execute();

                    } else {
                        boolean isValidDate = isValidDate(mBirthEdit.getText().toString());
                        int msgId = isValidDate ? R.string.edit_canceled_no_change :
                                R.string.error_invalid_date;
                        Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();

                        getActivity().setResult(RESULT_CANCELED);
                        getActivity().finish();
                    }
                }
            });

            return root;
        }

        private boolean updateProfile(Profile profile) {
            boolean changed = sIsCreateProfile;

            if (!profile.getName().equals(mNameEdit.getText().toString())) {
                profile.setName(mNameEdit.getText().toString());
                changed = true;
            }

            String[] birthTokens = mBirthEdit.getText().toString().split("/");
            if (!profile.getBirthStr().equals(mBirthEdit.getText().toString()) &&
                    isValidDate(mBirthEdit.getText().toString())) {
                profile.setBirth(Integer.parseInt(birthTokens[0]),
                        Integer.parseInt(birthTokens[1]),
                        Integer.parseInt(birthTokens[2]));
                changed = true;
            }

            int selectRadioId = mGenderRadioGroup.getCheckedRadioButtonId();
            int selectGender = selectRadioId == R.id.radioButtonBoy ?
                    ProfileContract.GENDER_BOY : ProfileContract.GENDER_GIRL;
            if (profile.getGender() != selectGender) {
                profile.setGender(selectGender);
                changed = true;
            }

            if (mProfileImageChanged) {
                Bitmap bitmap = ((BitmapDrawable) mProfileImage.getDrawable()).getBitmap();
                profile.setProfilePicture(bitmap);
                changed = true;
            }

            return changed;
        }

        private boolean isValidDate(String date) {
            if (!Utilities.isValidDateFormat(date, "yyyy/MM/dd")) {
                return false;
            }

            String[] tokens = date.split("/");
            int year = Integer.parseInt(tokens[0]);
            int month = Integer.parseInt(tokens[1]);
            int day = Integer.parseInt(tokens[2]);

            return 1970 <= year && year <= 2088 &&
                    1 <= month && month <= 12 &&
                    1 <= day && day <= 31;
        }

        @Override
        public void OnProfileImageUpdated(Profile profile, Bitmap bitmap) {
            mProfileImageChanged = true;
            Utilities.animSwitchImageRes(getActivity(), mProfileImage, bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // decide the main purpose of this activity
        int requestCode = getIntent().getIntExtra(EXTRA_REQUEST_CODE,
                MainActivity.REQUEST_PROFILE_EDIT);
        sIsCreateProfile = requestCode == MainActivity.REQUEST_PROFILE_CREATE;

        if (sIsCreateProfile) {
            setTitle(getString(R.string.profile_editor_title_create));
        }

        setContentView(R.layout.activity_profile_editor);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ProfileEditFragment frag = (ProfileEditFragment) getSupportFragmentManager()
                .findFragmentById(R.id.record_edit_fragment);

        if (requestCode == MainActivity.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String imageFilePath = ProfilePictureDialogFragment.sCurrentPhotoPath;
            new ProfileImageTask(this, sProfile, imageFilePath, null, frag, false).execute();

        } else if (requestCode == MainActivity.REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
            new ProfileImageTask(this, sProfile, null, data.getData(), frag, false).execute();
        }
    }

    public static Profile getCreatedUserProfile() {
        return sProfile;
    }

    public static void setCreatedUserProfile(Profile p) {
        sProfile = p;
    }
}
