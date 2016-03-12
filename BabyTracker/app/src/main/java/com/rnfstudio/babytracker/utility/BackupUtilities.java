package com.rnfstudio.babytracker.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.db.EventDBHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Roger on 2016/2/12.
 */
public class BackupUtilities {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[BackupUtilities]";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    /**
     * Copy database to sdcard<p>
     *
     * See <a href="http://stackoverflow.com/questions/9997976/
     *     android-pulling-sqlite-database-android-device">Pull database in Android</a>
     */
    public static void copyDB2SDcard(final Context context) {
        try {
            File externalDir = Environment.getExternalStorageDirectory();
            File backupDir = new File(externalDir, "BBTracker");

            if (externalDir != null && externalDir.canWrite()) {

                // create parent folder
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }

                String currentDBPath = context
                        .getDatabasePath(EventDBHelper.DATABASE_NAME).getPath();
                String datetime = TimeUtils.flattenCalendarTimeSafely(Calendar.getInstance(),
                        "yyyyMMddHHmmss");
                String backupDBPath = "backup_" + datetime + ".db";

                File currentDB = new File(currentDBPath);
                final File backupDB = new File(backupDir, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    String message = context.getString(R.string.backup_successful, backupDB);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    new AlertDialog.Builder(context)
                            .setMessage(R.string.backup_successful_share_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupDB));
                                    sendIntent.setType("application/octet-stream");
                                    context.startActivity(Intent.createChooser(sendIntent,
                                            context.getString(R.string.share_file_to)));
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                } else {
                    Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(context, R.string.backup_fail_not_allowed,
                        Toast.LENGTH_SHORT).show();
            }

        } catch (IOException ioe) {
            Log.d(TAG, "[copyDB2SDcard] Exception: " + ioe.toString());
        }
    }

    public static void restoreDBfromSDcard(Context context) {
        Toast.makeText(context, R.string.restore_successful, Toast.LENGTH_SHORT).show();
    }
}
