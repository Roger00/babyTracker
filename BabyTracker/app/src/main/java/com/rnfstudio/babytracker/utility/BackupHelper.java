package com.rnfstudio.babytracker.utility;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import com.rnfstudio.babytracker.MainApplication;
import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.db.EventProvider;
import com.rnfstudio.babytracker.db.ProfileContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/**
 * Created by Roger on 2016/2/12.
 */
public class BackupHelper {

    private static final int BACKUP_JSON_VERSION_NUMBER = 1;
    public static final String BACKUP_FOLDER_NAME = "BBTracker";
    public static final String BACKUP_FILE_PREFIX = "backup_";

    public static class BackupJsonFields {
        public static final String JSON_FIELD_VERSION = "version";
        public static final String JSON_FIELD_PROFILES = "profiles";
        public static final String JSON_FIELD_EVENTS = "events";
    }

    public static class EventJsonContract {
        public static final String JSON_FIELD_USER_ID = "userId";
        public static final String JSON_FIELD_EVENT_TYPE = "type";
        public static final String JSON_FIELD_EVENT_SUBTYPE = "subtype";
        public static final String JSON_FIELD_EVENT_START_TIME = "startTime";
        public static final String JSON_FIELD_EVENT_END_TIME = "endTime";
        public static final String JSON_FIELD_EVENT_DURATION = "duration";
        public static final String JSON_FIELD_EVENT_AMOUNT = "amount";
    }

    public static class ProfileJsonContract {
        public static final String JSON_FIELD_USER_ID = "userId";
        public static final String JSON_FIELD_DISPLAY_NAME = "displayName";
        public static final String JSON_FIELD_GENDER = "gender";
        public static final String JSON_FIELD_BIRTH_YEAR = "birthYear";
        public static final String JSON_FIELD_BIRTH_MONTH = "birthMoth";
        public static final String JSON_FIELD_BIRTH_DAY = "birthDay";
        public static final String JSON_FIELD_PROFILE_PICTURE = "profilePicture";
    }

    public static class BackupResultCodes {
        public static final int SUCCESSFUL = -1;
        public static final int UNKNOWN = 0;
        public static final int NO_PERMISSION = 1;
        public static final int IO_EXCEPTION = 2;
    }

    public static class RestoreResultCodes {
        public static final int SUCCESSFUL = -1;
        public static final int UNKNOWN = 0;
        public static final int NO_PERMISSION = 1;
        public static final int IO_EXCEPTION = 2;
        public static final int VERSION_MISMATCH = 3;
        public static final int PARSE_FAIL = 4;
    }

    /**
     * See <a href="http://goo.gl/ZzEzq3">
     *     Updating progress on progress dialog from AsyncTask can't build</a><p>
     *
     * For older implementations: Copy database to sdcard<p>
     *
     * See <a href="http://stackoverflow.com/questions/9997976/
     *     android-pulling-sqlite-database-android-device">Pull database in Android</a>
     */
    public static class BackupTask extends AsyncTask<Void, Integer, Void> {

        static final String TAG = "[BackupTask]";
        Context mContext;
        ProgressDialog mDialog;
        int mTaskDone;
        int mResult;
        File mBackupFile;

        public BackupTask(Context context) {
            mContext = context;
            mTaskDone = 0;
            mResult = BackupResultCodes.SUCCESSFUL;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage(mContext.getString(R.string.backup_process_message));
            mDialog.setIndeterminate(false);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setCancelable(true);
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            mDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mDialog.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            writeBackupJsonFile(generateBackupJsonStr());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mDialog.dismiss();

            String message;

            switch(mResult) {
                case BackupResultCodes.SUCCESSFUL:
                    // pop-up share dialog
                    shareBackupJsonFile();
                    message = mContext.getString(R.string.backup_successful, mBackupFile);
                    break;

                case BackupResultCodes.NO_PERMISSION:
                    message = mContext.getString(R.string.backup_fail_not_allowed, mBackupFile);
                    break;

                default:
                    message = mContext.getString(R.string.error_unknown);
                    break;
            }

            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            Toast.makeText(mContext, R.string.backup_canceled, Toast.LENGTH_SHORT).show();
        }

        private String generateBackupJsonStr() {
            // notify total number of tasks
            mDialog.setMax(getBackupTaskCount());

            try {
                JSONObject root = new JSONObject();
                root.put(BackupJsonFields.JSON_FIELD_VERSION, BACKUP_JSON_VERSION_NUMBER);
                root.put(BackupJsonFields.JSON_FIELD_PROFILES, generateProfilesJsonArray());
                root.put(BackupJsonFields.JSON_FIELD_EVENTS, generateEventsJsonArray());

                return root.toString();

            } catch (JSONException je) {
                Log.w(TAG, "[generateBackupJsonStr] exception: " + je.toString());
            }

            return "";
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private JSONArray generateEventsJsonArray() {
            JSONArray result = new JSONArray();

            // iterate all events
            try (Cursor cursor = mContext.getContentResolver().query(
                    EventProvider.sNotifyUriForEvent,
                    EventContract.getQueryProjection(),
                    null, null,
                    EventContract.EventEntry._ID + " ASC")) {

                while (!isCancelled() && cursor != null && cursor.moveToNext()) {
                    long id = cursor.getLong(EventContract.EventQuery.EVENT_ID);
                    long userId = cursor.getLong(EventContract.EventQuery.EVENT_USER_ID);
                    int type = cursor.getInt(EventContract.EventQuery.EVENT_TYPE);
                    int subType = cursor.getInt(EventContract.EventQuery.EVENT_SUBTYPE);
                    long startTime = cursor.getLong(EventContract.EventQuery.EVENT_START_TIME);
                    long endTime = cursor.getLong(EventContract.EventQuery.EVENT_END_TIME);
                    long duration = cursor.getLong(EventContract.EventQuery.EVENT_DURATION);
                    int amount = cursor.getInt(EventContract.EventQuery.EVENT_AMOUNT);

                    JSONObject event = new JSONObject();
                    event.put(EventJsonContract.JSON_FIELD_USER_ID, userId);
                    event.put(EventJsonContract.JSON_FIELD_EVENT_TYPE, type);
                    event.put(EventJsonContract.JSON_FIELD_EVENT_SUBTYPE, subType);
                    event.put(EventJsonContract.JSON_FIELD_EVENT_START_TIME, startTime);
                    event.put(EventJsonContract.JSON_FIELD_EVENT_END_TIME, endTime);
                    event.put(EventJsonContract.JSON_FIELD_EVENT_DURATION, duration);
                    event.put(EventJsonContract.JSON_FIELD_EVENT_AMOUNT, amount);

                    result.put(event);

                    // publish progress
                    publishProgress(++mTaskDone);
                }

            } catch (JSONException | SQLiteException se) {
                Log.w(TAG, "[generateEventsJsonArray] exception: " + se.toString());
            }

            return result;
        }

        /**
         * See <a href="http://goo.gl/G5ml8A">
         *     How many ways to convert bitmap to string and vice-versa?</a>
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        private JSONArray generateProfilesJsonArray() {
            JSONArray result = new JSONArray();

            // iterate all events
            try (Cursor cursor = mContext.getContentResolver().query(
                    EventProvider.sNotifyUriForUser,
                    ProfileContract.getQueryProjection(),
                    null, null,
                    ProfileContract.UserEntry._ID + " ASC")) {

                while (!isCancelled() && cursor != null && cursor.moveToNext()) {
                    long userId = cursor.getLong(ProfileContract.ProfileQuery.ID);
                    String name = cursor.getString(ProfileContract.ProfileQuery.DISPLAY_NAME);
                    int gender = cursor.getInt(ProfileContract.ProfileQuery.GENDER);
                    int year = cursor.getInt(ProfileContract.ProfileQuery.BIRTH_YEAR);
                    int month = cursor.getInt(ProfileContract.ProfileQuery.BIRTH_MONTH);
                    int day = cursor.getInt(ProfileContract.ProfileQuery.BIRTH_DAY);
                    byte[] data = cursor.getBlob(ProfileContract.ProfileQuery.PROFILE_PICTURE);

                    JSONObject profile = new JSONObject();
                    profile.put(ProfileJsonContract.JSON_FIELD_USER_ID, userId);
                    profile.put(ProfileJsonContract.JSON_FIELD_DISPLAY_NAME, name);
                    profile.put(ProfileJsonContract.JSON_FIELD_GENDER, gender);
                    profile.put(ProfileJsonContract.JSON_FIELD_BIRTH_YEAR, year);
                    profile.put(ProfileJsonContract.JSON_FIELD_BIRTH_MONTH, month);
                    profile.put(ProfileJsonContract.JSON_FIELD_BIRTH_DAY, day);

                    if (data != null && data.length > 0) {
                        String imageStr;
                        imageStr = Base64.encodeToString(data, Base64.DEFAULT);
                        if (!TextUtils.isEmpty(imageStr)) {
                            profile.put(ProfileJsonContract.JSON_FIELD_PROFILE_PICTURE, imageStr);
                        }
                    }

                    result.put(profile);

                    // publish progress
                    publishProgress(++mTaskDone);
                }

            } catch (JSONException | SQLiteException se) {
                Log.w(TAG, "[generateProfilesJsonArray] exception: " + se.toString());
            }

            return result;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private int getBackupTaskCount() {
            int cUsers = 0;
            int cEvents = 0;

            // get user count
            try (Cursor cursor = mContext.getContentResolver().query(
                    EventProvider.sNotifyUriForUser,
                    new String[]{ProfileContract.UserEntry._ID},
                    null, null, null)) {
                if (cursor != null) {
                    cUsers = cursor.getCount();
                }
            } catch (SQLiteException se) {
                Log.w(TAG, "[getBackupTaskCount] exception: " + se.toString());
            }

            // get event count
            try (Cursor cursor = mContext.getContentResolver().query(
                    EventProvider.sNotifyUriForEvent,
                    new String[]{EventContract.EventEntry._ID},
                    null, null, null)) {
                if (cursor != null) {
                    cEvents = cursor.getCount();
                }
            } catch (SQLiteException se) {
                Log.w(TAG, "[getBackupTaskCount] exception: " + se.toString());
            }

            return cUsers + cEvents;
        }

        /**
         * See <a href="http://goo.gl/ji59DS">How To Read/Write String From A File In Android</a>
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        private void writeBackupJsonFile(String data) {
            File backupDir = new File(Environment.getExternalStorageDirectory(),
                    BACKUP_FOLDER_NAME);
            String datetime = TimeUtils.flattenCalendarTimeSafely(Calendar.getInstance(),
                    "yyyyMMddHHmmss");
            mBackupFile = new File(backupDir, BACKUP_FILE_PREFIX + datetime);

            // check if not allowed to write sdcard
            if (!backupDir.canWrite()) {
                mResult = BackupResultCodes.NO_PERMISSION;

                return;
            }

            // create parent folder
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // write text to file
            try (FileOutputStream os = new FileOutputStream(mBackupFile);
                 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os)) {

                outputStreamWriter.write(data);

            } catch (IOException ioe) {
                Log.w(TAG, "[writeBackupJsonFile] exception: " + ioe.toString());
                mResult = BackupResultCodes.IO_EXCEPTION;
                return;
            }

            mResult = BackupResultCodes.SUCCESSFUL;
        }

        private void shareBackupJsonFile() {
            new AlertDialog.Builder(mContext)
                    .setMessage(R.string.backup_successful_share_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mBackupFile));
                            sendIntent.setType("application/octet-stream");
                            mContext.startActivity(Intent.createChooser(sendIntent,
                                    mContext.getString(R.string.share_file_to)));
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    public static class RestoreTask extends AsyncTask<Void, Integer, Void> {

        static final String TAG = "[RestoreTask]";
        Context mContext;
        ProgressDialog mDialog;
        int mTaskDone;
        int mResult;
        Uri mSourceUri;

        public RestoreTask(Context context, Uri inputUri) {
            mContext = context;
            mTaskDone = 0;
            mResult = RestoreResultCodes.SUCCESSFUL;
            mSourceUri = inputUri;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage(mContext.getString(R.string.restore_process_message));
            mDialog.setIndeterminate(false);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setCancelable(true);
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            // parse file and check its contents
            JSONObject root = parseBackupJsonStr(mSourceUri);

            if (root == null) {
                Log.d(TAG, "[RestoreTask] parse failed, do not restore.");
                mResult = RestoreResultCodes.PARSE_FAIL;
                return null;
            }

            int jsonVersion = root.optInt(BackupJsonFields.JSON_FIELD_VERSION, 0);
            if (jsonVersion != BACKUP_JSON_VERSION_NUMBER) {
                Log.d(TAG, "[RestoreTask] version number does not match, do not restore.");
                mResult = RestoreResultCodes.VERSION_MISMATCH;
                return null;
            }

            // notify total number of tasks
            mDialog.setMax(getRestoreTaskCount(root));

            // clear old database entries
            clearDatabaseEntries();

            // write through content provider
            importBackupData(root);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mDialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(mContext, R.string.restore_canceled, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mDialog.dismiss();

            String message;

            switch(mResult) {
                case RestoreResultCodes.SUCCESSFUL:
                    message = mContext.getString(R.string.restore_successful);
                    break;

                default:
                    message = mContext.getString(R.string.error_unknown);
                    break;
            }

            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }

        private JSONObject parseBackupJsonStr(Uri srcUri) {
            try {
                return new JSONObject(readStringFromUri(srcUri));

            } catch (JSONException je) {
                Log.w(TAG, "[parseBackupJsonStr] exception: " + je.toString());
                return null;
            }
        }

        /**
         * See <a href="http://goo.gl/z75T02">How To Read/Write String From A File In Android</a>
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        private String readStringFromUri(Uri uri) {
            if (uri != null) {
                try (InputStream is = mContext.getContentResolver().openInputStream(uri);
                     InputStreamReader inputStreamReader = new InputStreamReader(is);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    StringBuilder sb = new StringBuilder();
                    String receiveString;

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        sb.append(receiveString);
                    }

                    return sb.toString();

                } catch (IOException ioe) {
                    Log.w(TAG, "[readStringFromUri] exception: " + ioe.toString());
                }
            }
            return "";
        }

        private int getRestoreTaskCount(JSONObject root) {

            int cProfiles = 0;
            int cEvents = 0;

            try {
                JSONArray profiles = root.getJSONArray(BackupJsonFields.JSON_FIELD_PROFILES);
                JSONArray events = root.getJSONArray(BackupJsonFields.JSON_FIELD_EVENTS);
                cProfiles = profiles.length();
                cEvents = events.length();

            } catch (JSONException e) {
                Log.w(TAG, "[getRestoreTaskCount] exception: " + e.toString());
            }

            return cProfiles + cEvents;
        }

        private void clearDatabaseEntries() {
            mContext.getContentResolver().delete(EventProvider.sNotifyUriForUser, null, null);
            mContext.getContentResolver().delete(EventProvider.sNotifyUriForEvent, null, null);
        }

        private void importBackupData(JSONObject root) {
            try {
                JSONArray profiles = root.getJSONArray(BackupJsonFields.JSON_FIELD_PROFILES);
                JSONArray events = root.getJSONArray(BackupJsonFields.JSON_FIELD_EVENTS);

                // import profiles
                LongSparseArray<Long> userIdMap = importUserProfiles(profiles);

                // import events
                importEvents(events, userIdMap);

                // load the primary user profile
                loadMainUserProfile();

            } catch (JSONException e) {
                Log.w(TAG, "[importBackupData] exception: " + e.toString());
            }
        }

        private LongSparseArray<Long> importUserProfiles(JSONArray profiles) {
            LongSparseArray<Long> userIdMap = new LongSparseArray<>();

            for(int i = 0; !isCancelled() && i < profiles.length(); i++) {
                try {
                    JSONObject profile = profiles.getJSONObject(i);
                    long userId = profile.getLong(ProfileJsonContract.JSON_FIELD_USER_ID);
                    String name =
                            profile.optString(ProfileJsonContract.JSON_FIELD_DISPLAY_NAME, "");
                    int gender = profile.optInt(ProfileJsonContract.JSON_FIELD_GENDER,
                            ProfileContract.GENDER_UNKNOWN);
                    int year = profile.optInt(ProfileJsonContract.JSON_FIELD_BIRTH_YEAR, 1970);
                    int month = profile.optInt(ProfileJsonContract.JSON_FIELD_BIRTH_MONTH, 1);
                    int day = profile.optInt(ProfileJsonContract.JSON_FIELD_BIRTH_DAY, 1);
                    String imageStr = profile.optString(
                            ProfileJsonContract.JSON_FIELD_PROFILE_PICTURE, "");

                    // prepare content values for db insertion
                    ContentValues cvs = new ContentValues();
                    cvs.put(ProfileContract.UserEntry._ID, userId);
                    cvs.put(ProfileContract.UserEntry.COLUMN_NAME_DISPLAY_NAME, name);
                    cvs.put(ProfileContract.UserEntry.COLUMN_NAME_GENDER, gender);
                    cvs.put(ProfileContract.UserEntry.COLUMN_NAME_BIRTH_YEAR, year);
                    cvs.put(ProfileContract.UserEntry.COLUMN_NAME_BIRTH_MONTH, month);
                    cvs.put(ProfileContract.UserEntry.COLUMN_NAME_BIRTH_DAY, day);
                    if (!TextUtils.isEmpty(imageStr)) {
                        byte[] data = Base64.decode(imageStr, Base64.DEFAULT);
                        if (data != null) {
                            cvs.put(ProfileContract.UserEntry.COLUMN_NAME_PROFILE_PICTURE, data);
                        }
                    }

                    // insert user profile
                    Uri insertUri = mContext.getContentResolver()
                            .insert(EventProvider.sNotifyUriForUser, cvs);

                    // create user id mapping entry for later use
                    if (insertUri != null && !TextUtils.isEmpty(insertUri.getLastPathSegment())) {
                        long newUserId = Long.parseLong(insertUri.getLastPathSegment());
                        userIdMap.put(userId, newUserId);
                    }

                } catch (JSONException e) {
                    Log.w(TAG, "[importUserProfiles] exception: " + e.toString());
                }

                // publish progress
                publishProgress(++mTaskDone);
            }
            return userIdMap;
        }

        private void importEvents(JSONArray events, LongSparseArray<Long> userIdMap) {
            for(int i = 0; !isCancelled() && i < events.length(); i++) {
                try {
                    JSONObject event = events.getJSONObject(i);
                    long oldUserId = event.getLong(EventJsonContract.JSON_FIELD_USER_ID);
                    int type = event.getInt(EventJsonContract.JSON_FIELD_EVENT_TYPE);
                    int subType = event.getInt(EventJsonContract.JSON_FIELD_EVENT_SUBTYPE);
                    long startTime = event.getLong(EventJsonContract.JSON_FIELD_EVENT_START_TIME);
                    long endTime = event.getLong(EventJsonContract.JSON_FIELD_EVENT_END_TIME);
                    long duration = event.getLong(EventJsonContract.JSON_FIELD_EVENT_DURATION);
                    int amount = event.getInt(EventJsonContract.JSON_FIELD_EVENT_AMOUNT);

                    // prepare content values for db insertion
                    ContentValues cvs = new ContentValues();
                    cvs.put(EventContract.EventEntry.COLUMN_NAME_USER_ID,
                            userIdMap.get(oldUserId, oldUserId));
                    cvs.put(EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE, type);
                    cvs.put(EventContract.EventEntry.COLUMN_NAME_EVENT_SUBTYPE, subType);
                    cvs.put(EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME, startTime);
                    cvs.put(EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME, endTime);
                    cvs.put(EventContract.EventEntry.COLUMN_NAME_EVENT_DURATION, duration);
                    cvs.put(EventContract.EventEntry.COLUMN_NAME_EVENT_AMOUNT, amount);

                    // insert event
                    mContext.getContentResolver().insert(EventProvider.sNotifyUriForEvent, cvs);

                } catch (JSONException e) {
                    Log.w(TAG, "[importUserProfiles] exception: " + e.toString());
                }

                // publish progress
                publishProgress(++mTaskDone);
            }
        }

        private void loadMainUserProfile() {
            // use the first profile in database
            MainApplication.setUserId(mContext, 1);

            // load profile
            MainApplication.loadDefaultUserProfile(mContext);
        }
    }
}
