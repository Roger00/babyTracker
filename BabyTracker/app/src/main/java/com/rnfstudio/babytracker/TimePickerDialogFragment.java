package com.rnfstudio.babytracker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Roger on 2015/11/25.
 */
public class TimePickerDialogFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String TAG = "[TimePicker]";

    public static final String KEY_HOUR_OF_DAY = "hour.of.day";
    public static final String KEY_MINUTE = "minute";

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
        int hour;
        int minute;

        Bundle args = getArguments();

        // Use the current date as the default date in the picker
        if (args == null) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);

        } else {
            hour = args.getInt(KEY_HOUR_OF_DAY);
            minute = args.getInt(KEY_MINUTE);
        }

        Log.v(TAG, String.format("Set default time: %02d/%02d", hour, minute));

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final int requestCode = getTargetRequestCode();
        Intent result = new Intent();
        result.putExtra(KEY_HOUR_OF_DAY, hourOfDay).putExtra(KEY_MINUTE, minute);
        getTargetFragment().onActivityResult(requestCode, RecordEditFragment.RESULT_CODE_SUCCESS, result);
    }
}
