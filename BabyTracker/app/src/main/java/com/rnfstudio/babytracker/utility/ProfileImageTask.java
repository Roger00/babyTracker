package com.rnfstudio.babytracker.utility;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.db.Profile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Get resized & centered profile image from different sources
 *
 * Created by Roger on 2016/3/10.
 */
public class ProfileImageTask extends AsyncTask<Void, Void, Bitmap> {

    public interface ProfileImageCallback {
        void OnProfileImageUpdated(Profile profile, Bitmap bitmap);
    }

    private static final String TAG = "[ProfileImageTask]";

    Context mContext;
    Profile mProfile;
    String mPathname;
    Uri mUri;
    ProfileImageCallback mCallback;
    boolean mUpdateProfile;

    public ProfileImageTask(Context context,
                            Profile profile,
                            String pathname,
                            Uri uri,
                            ProfileImageCallback callback, boolean updateProfile) {
        mContext = context;
        mProfile = profile;
        mPathname = pathname;
        mUri = uri;
        mCallback = callback;
        mUpdateProfile = updateProfile;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            // resize and center bitmap from source
            Bitmap src = getBitmapFromSource();
            Bitmap resized = Utilities.getResizedCenterBitmap(mContext, src);

            // update profile image and write to database
            if (resized != null && mUpdateProfile) {
                mProfile.setProfilePicture(resized);
                mProfile.writeDB(mContext);
            }
            return resized;

        } catch (Exception e) {
            Log.w(TAG, "Exception: " + e.toString());
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private Bitmap getBitmapFromSource() {
        if (!TextUtils.isEmpty(mPathname)) {
            return BitmapFactory.decodeFile(mPathname);

        } else if (mUri != null) {
            try (InputStream is = mContext.getContentResolver().openInputStream(mUri)) {
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                Log.w(TAG, "Fail to get bitmap from uri: " + mUri);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(mContext,R.string.error_unknown, Toast.LENGTH_SHORT).show();
        } else {
            mCallback.OnProfileImageUpdated(mProfile, bitmap);
        }
    }
}
