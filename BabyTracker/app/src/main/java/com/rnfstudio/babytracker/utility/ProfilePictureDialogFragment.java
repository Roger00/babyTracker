package com.rnfstudio.babytracker.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.rnfstudio.babytracker.MainActivity;
import com.rnfstudio.babytracker.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Roger on 2016/3/1.
 */
public class ProfilePictureDialogFragment extends DialogFragment {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String TAG = "[ProfilePicDialog]";

    public static String sCurrentPhotoPath;
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
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Resources res = getActivity().getResources();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_item,
                res.getStringArray(R.array.menu_picture_source));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick, which: " + which);

                if (which == 0) {
                    startCameraSafely(getActivity());
                } else if (which == 1) {
                    pickPictureFromGallery(getActivity());
                }
            }
        });

        return builder.create();
    }

    /**
     * See <a href="http://developer.android.com/intl/zh-tw/training/camera/photobasics.html">
     *     Taking photos simply</a>
     */
    public static void startCameraSafely(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(activity, R.string.error_create_file, Toast.LENGTH_SHORT).show();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                activity.startActivityForResult(takePictureIntent,
                        MainActivity.REQUEST_IMAGE_CAPTURE);
            }

        } else {
            Toast.makeText(activity, R.string.error_start_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        sCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * See <a href="http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app">
     *     How to pick an image from gallery (SD Card) for my app</a><p>
     *
     * See <a href="http://stackoverflow.com/questions/5309190/android-pick-images-from-gallery">
     *     android pick images from gallery</a>
     */
    public static void pickPictureFromGallery(Activity activity) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(photoPickerIntent, MainActivity.REQUEST_IMAGE_SELECT);
        } else {
            Toast.makeText(activity, R.string.error_unknown, Toast.LENGTH_SHORT).show();
        }
    }
}
