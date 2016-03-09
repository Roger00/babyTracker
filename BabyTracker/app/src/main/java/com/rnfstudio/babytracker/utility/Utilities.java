package com.rnfstudio.babytracker.utility;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Roger on 2015/8/5.
 */
public class Utilities {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------
    public static String getDisplayCmd(Context context, String cmdId) {
        Resources res = context.getResources();
        int resId = res.getIdentifier(cmdId, "string", "com.rnfstudio.babytracker");

        String displayCmd = "";
        if (resId != 0) {
            try {
                displayCmd = res.getString(resId);
            } catch (Resources.NotFoundException e) {
            }
        }

        return displayCmd;
    }

    /**
     * TODO: do NOT compress bitmap, this is waste of CPU
     *
     * See <a href="http://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array">
     *     converting Java bitmap to byte array</a>
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static byte[] encodeBitmap(Bitmap bitmap) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();

        } catch (IOException e) {
            return null;
        }
    }
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
}
