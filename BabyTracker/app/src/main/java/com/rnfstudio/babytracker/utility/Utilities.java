package com.rnfstudio.babytracker.utility;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Base64;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.rnfstudio.babytracker.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();

        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] getBitmapByteArray(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        return buffer.array();
    }

    public static byte[] getBase64Bitmap(Bitmap bitmap) {
        byte[] bytes = getBitmapByteArray(bitmap);
        return Base64.encode(bytes, Base64.DEFAULT);
    }

    /**
     * See <a href="http://stackoverflow.com/questions/6908604/android-crop-center-of-bitmap">
     *     Android Crop Center of Bitmap</a>
     */
    private static Bitmap getCenterBitmap(Bitmap srcBitmap) {
        Bitmap centerBitmap;

        if (srcBitmap.getWidth() >= srcBitmap.getHeight()){
            centerBitmap = Bitmap.createBitmap(
                    srcBitmap,
                    srcBitmap.getWidth()/2 - srcBitmap.getHeight()/2,
                    0,
                    srcBitmap.getHeight(),
                    srcBitmap.getHeight()
            );

        } else {
            centerBitmap = Bitmap.createBitmap(
                    srcBitmap,
                    0,
                    srcBitmap.getHeight()/2 - srcBitmap.getWidth()/2,
                    srcBitmap.getWidth(),
                    srcBitmap.getWidth()
            );
        }

        return centerBitmap;
    }

    public static Bitmap getResizedCenterBitmap(Context context, Bitmap src) {
        int dstSize = context.getResources().getDimensionPixelSize(R.dimen.baby_icon_size);
        boolean isLandscape = src.getWidth() >= src.getHeight();
        float ratio = isLandscape ?
                (float) src.getWidth()/src.getHeight() : (float) src.getHeight()/src.getWidth();
        int dstWidth = isLandscape ? (int) (ratio * dstSize) : dstSize;
        int dstHeight = isLandscape ? dstSize : (int) (ratio * dstSize);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        return getCenterBitmap(resizedBitmap);
    }

    /**
     * See <a href="http://goo.gl/COsbOm">
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
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
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
