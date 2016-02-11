package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.db.EventDBHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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
    public static void copyDB2SDcard(Context context) {
        try {
            Log.d(TAG, "[copyDB2SDcard] called");
            File sd = context.getExternalFilesDir(null);

            if (sd.canWrite()) {
                String currentDBPath = context
                        .getDatabasePath(EventDBHelper.DATABASE_NAME).getPath();
                String backupDBPath = "backupname.db";

                Log.d(TAG, "[copyDB2SDcard] current path: " + currentDBPath);
                Log.d(TAG, "[copyDB2SDcard] backupDBPath: " + backupDBPath);

                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Toast.makeText(context, R.string.backup_successful, Toast.LENGTH_SHORT).show();

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
