package com.rnfstudio.babytracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import com.rnfstudio.babytracker.utility.BackupUtilities;

/**
 * Created by Roger on 2016/2/12.
 */
public class SettingsActivity extends Activity {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // load version name
            String versionName = getVersion(getActivity());
            if (!TextUtils.isEmpty(versionName)) {
                Preference version = findPreference(SettingsActivity.KEY_VERSION);
                version.setSummary(versionName);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             Preference preference) {
            String key = preference.getKey();

            if (KEY_PROFILE.equals(key)) {
                return true;

            } else if (KEY_BACKUP_TO_SDCARD.equals(key)) {
                BackupUtilities.copyDB2SDcard(getActivity());
                return true;

            } else if (KEY_RESTORE_FROM_SDCARD.equals(key)) {
                BackupUtilities.restoreDBfromSDcard(getActivity());
                return true;

            } else if (KEY_CHECK_UPDATES.equals(key)) {
                Intent storeIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + MainApplication.PACKAGE_NAME));
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(storeIntent);
                return true;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[SettingsActivity]";

    public static final String KEY_PROFILE = "key_profile";
    public static final String KEY_ENABLE_SOUND_EFFECT = "key_enable_sound_effect";
    public static final String KEY_ENABLE_VIBRATE = "key_enable_vibrate";
    public static final String KEY_BACKUP_TO_SDCARD = "key_backup_to_sdcard";
    public static final String KEY_RESTORE_FROM_SDCARD = "key_restore_from_sdcard";
    public static final String KEY_CHECK_UPDATES = "key_check_for_updates";
    public static final String KEY_VERSION = "key_version";

    public static final boolean DEFAULT_VALUE_ENABLE_SOUND_EFFECT = false;
    public static final boolean DEFAULT_VALUE_ENABLE_VIBRATE = false;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private PrefsFragment mPrefsFragment;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mPrefsFragment = (PrefsFragment) getFragmentManager().findFragmentById(R.id.prefsFragment);
    }

    /**
     * Check if enable sound effects
     */
    public static boolean isSoundEffectEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_ENABLE_SOUND_EFFECT, DEFAULT_VALUE_ENABLE_SOUND_EFFECT);
    }

    /**
     * Check if enable vibration
     */
    public static boolean isVibrateEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_ENABLE_VIBRATE, DEFAULT_VALUE_ENABLE_VIBRATE);
    }

    /**
     * See <a href="http://stackoverflow.com/questions/4616095/how-to-get-the-build-version-number
     *      -of-your-android-application">How to get the build version number</a>
     */
    public static String getVersion(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(MainApplication.PACKAGE_NAME, 0).versionName;

        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.w(TAG, "[getVersion] exception: " + nnfe.toString());
            return "";
        }
    }
}
