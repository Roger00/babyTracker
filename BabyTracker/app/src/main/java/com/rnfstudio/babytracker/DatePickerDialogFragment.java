package com.rnfstudio.babytracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Roger on 2015/11/26.
 */
public class DatePickerDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String TAG = "[DatePickerDialogFragment]";

    public static final String KEY_YEAR = "year";
    public static final String KEY_MONTH = "month";
    public static final String KEY_DAY = "day";

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
        int year;
        int month;
        int day;

        Bundle args = getArguments();

        // Use the current date as the default date in the picker
        if (args == null) {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

        } else {
            year = args.getInt(KEY_YEAR);
            month = args.getInt(KEY_MONTH);
            day = args.getInt(KEY_DAY);
        }

        Log.v(TAG, String.format("Set default date: %04d/%02d/%02d", year, month, day));

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        final int requestCode = getTargetRequestCode();
        Intent result = new Intent();
        result.putExtra(KEY_YEAR, year).putExtra(KEY_MONTH, month).putExtra(KEY_DAY, day);
        getTargetFragment().onActivityResult(requestCode, RecordEditFragment.RESULT_CODE_SUCCESS, result);
    }
}
