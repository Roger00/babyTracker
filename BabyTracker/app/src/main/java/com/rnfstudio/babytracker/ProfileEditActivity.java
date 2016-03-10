package com.rnfstudio.babytracker;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.rnfstudio.babytracker.utility.Utilities;

/**
 * Created by Roger on 2016/3/6.
 */
public class ProfileEditActivity extends FragmentActivity {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    public static class ProfileEditFragment extends Fragment {

        private EditText mNameEdit;
        private EditText mBirthEdit;
        private RadioGroup mGenderRadioGroup;
        private Button mButtonCancel;
        private Button mButtonOkay;
        private ImageView mProfileImage;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {

            View root = inflater.inflate(R.layout.profile_editor, container, false);

            final Profile profile = MainApplication.getUserProfile();

            mNameEdit = (EditText) root.findViewById(R.id.nameEdit);
            mNameEdit.setText(profile.getName());

            mBirthEdit = (EditText) root.findViewById(R.id.birthEdit);
            mBirthEdit.setText(profile.getBirthStr());

            mGenderRadioGroup = (RadioGroup) root.findViewById(R.id.genderRadioGroup);
            int checkedGenderWidgetId = profile.getGender() == ProfileContract.GENDER_BOY ?
                    R.id.radioButtonBoy : R.id.radioButtonGirl;
            mGenderRadioGroup.check(checkedGenderWidgetId);

            mProfileImage = (ImageView) root.findViewById(R.id.profileImage);
            mProfileImage.setImageBitmap(profile.getProfilePicture());

            // ok/cancel buttons
            mButtonCancel = (Button) root.findViewById(R.id.button_cancel);
            mButtonOkay = (Button) root.findViewById(R.id.button_ok);

            mButtonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),
                            R.string.edit_canceled, Toast.LENGTH_SHORT).show();

                    getActivity().setResult(RESULT_CANCELED, null);
                    getActivity().finish();
                }
            });

            mButtonOkay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean changed = updateProfile(profile);

                    int msgId = R.string.edit_successful;
                    if (!changed) {
                        boolean isValidDate = isValidDate(mBirthEdit.getText().toString());
                        msgId = isValidDate ? R.string.edit_canceled_no_change :
                                R.string.error_invalid_date;
                    }

                    Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();

                    getActivity().setResult(changed ? RESULT_OK : RESULT_CANCELED, null);
                    getActivity().finish();
                }
            });

            return root;
        }

        private boolean updateProfile(Profile profile) {
            boolean changed = false;

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

            // update database if necessary
            if (changed) profile.asyncWriteDB(getActivity());

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editor);
    }
}
