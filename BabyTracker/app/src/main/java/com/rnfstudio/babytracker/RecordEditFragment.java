package com.rnfstudio.babytracker;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.Calendar;

/**
 * Created by Roger on 2015/8/11.
 */
public class RecordEditFragment extends Fragment {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[RecordEditFragment]";
    private static final Boolean DEBUG = true;

    public static final String KEY_REQUEST_CODE = "request_code";
    public static final int REQUEST_CODE_SET_TYPE = 0;
    public static final int REQUEST_CODE_SET_START_DATE = 1;
    public static final int REQUEST_CODE_SET_START_TIME = 2;
    public static final int REQUEST_CODE_SET_END_DATE = 3;
    public static final int REQUEST_CODE_SET_END_TIME = 4;
    public static final int REQUEST_CODE_SET_AMOUNT = 5;

    public static final int RESULT_CODE_SUCCESS = 1;
    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private Event mEvent;

    private TextView typeEdit;
    private TextView startDateEdit;
    private TextView startTimeEdit;
    private TextView endDateEdit;
    private TextView endTimeEdit;
    private TextView durationEdit;
    private TextView amountEdit;
    private Button buttonCancel;
    private Button buttonOkay;

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
     * See <a href=http://stackoverflow.com/questions/16424538/setcontentview-in-fragment>setContentView in fragment</>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) Log.v(TAG, "[onCreateView] called");

        // parse starting intent extra
        Intent startIntent = getActivity().getIntent();
        mEvent = Event.createFromBundle(startIntent.getExtras());
        if (mEvent.getId() == 0) {
            if (DEBUG) Log.w(TAG, "[onCreateView] Empty extras, fail to initialize activity");
            getActivity().finish();
        }

        // inflate views and initialize them
        View rootView = inflater.inflate(R.layout.record_editor, container, false);
        initViews(rootView);
        refreshViews();

        return rootView;
    }

    private void initViews(View root) {
        typeEdit = (TextView) root.findViewById(R.id.typeEdit);
        startDateEdit = (TextView) root.findViewById(R.id.startDateEdit);
        startTimeEdit = (TextView) root.findViewById(R.id.startTimeEdit);
        endDateEdit = (TextView) root.findViewById(R.id.endDateEdit);
        endTimeEdit = (TextView) root.findViewById(R.id.endTimeEdit);
        durationEdit = (TextView) root.findViewById(R.id.durationEdit);
        amountEdit = (TextView) root.findViewById(R.id.amountEdit);

        typeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(R.array.record_edit_activity_type, 0, null)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                // Do something useful with the the position of the selected radio button
                                String type = getResources().getStringArray(R.array.record_edit_activity_type)[selectedPosition];
                                Log.d("xxxxx", "type: " + type + ", pos: " + selectedPosition);
                            }
                        })
                        .show();
            }
        });

        startDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerDialogFragment();
                Bundle args = new Bundle();
                args.putInt(DatePickerDialogFragment.KEY_YEAR, mEvent.getStartTimeCopy().get(Calendar.YEAR));
                args.putInt(DatePickerDialogFragment.KEY_MONTH, mEvent.getStartTimeCopy().get(Calendar.MONTH));
                args.putInt(DatePickerDialogFragment.KEY_DAY, mEvent.getStartTimeCopy().get(Calendar.DAY_OF_MONTH));
                newFragment.setArguments(args);
                newFragment.setTargetFragment(RecordEditFragment.this, REQUEST_CODE_SET_START_DATE);
                newFragment.show(RecordEditFragment.this.getFragmentManager(), DatePickerDialogFragment.TAG);
            }
        });

        startTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerDialogFragment();
                newFragment.setTargetFragment(RecordEditFragment.this, REQUEST_CODE_SET_START_TIME);
                newFragment.show(RecordEditFragment.this.getFragmentManager(), TimePickerDialogFragment.TAG);
            }
        });

        endDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerDialogFragment();
                Bundle args = new Bundle();
                args.putInt(DatePickerDialogFragment.KEY_YEAR, mEvent.getEndTimeCopy().get(Calendar.YEAR));
                args.putInt(DatePickerDialogFragment.KEY_MONTH, mEvent.getEndTimeCopy().get(Calendar.MONTH));
                args.putInt(DatePickerDialogFragment.KEY_DAY, mEvent.getEndTimeCopy().get(Calendar.DAY_OF_MONTH));
                newFragment.setArguments(args);
                newFragment.setTargetFragment(RecordEditFragment.this, REQUEST_CODE_SET_END_DATE);
                newFragment.show(RecordEditFragment.this.getFragmentManager(), DatePickerDialogFragment.TAG);
            }
        });

        endTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerDialogFragment();
                newFragment.setTargetFragment(RecordEditFragment.this, REQUEST_CODE_SET_END_TIME);
                newFragment.show(RecordEditFragment.this.getFragmentManager(), TimePickerDialogFragment.TAG);
            }
        });

        // ok/cancel buttons
        buttonCancel = (Button) root.findViewById(R.id.button_cancel);
        buttonOkay = (Button) root.findViewById(R.id.button_ok);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: maybe we can write db in worker thread
                mEvent.writeDB(getActivity(), false);

                // FIXME: this doesn't seem to work
//                // notify RecordLoader that data has changed
//                Loader loader = RecordEditFragment.this.getLoaderManager().getLoader(RecordLoader.LOADER_ID);
//                if (loader != null) loader.onContentChanged();

                getActivity().finish();
            }
        });
    }

    private void refreshViews() {
        Log.v(TAG, "[refreshViews] called");

        typeEdit.setText(mEvent.getDisplayType(getActivity()));
        startDateEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getStartTimeCopy(), "yyyy-MM-dd"));
        startTimeEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getStartTimeCopy(), "HH:mm:SS"));
        endDateEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getEndTimeCopy(), "yyyy-MM-dd"));
        endTimeEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getEndTimeCopy(), "HH:mm:SS"));
        durationEdit.setText(mEvent.getDisplayDuration(getActivity()));
        amountEdit.setText(mEvent.getDisplayAmount(getActivity()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(TAG, "[onActivityResult] requestCode: " + requestCode + ", resultCode: " + resultCode + "data: " + data.toUri(0));

        int year = data.getIntExtra(DatePickerDialogFragment.KEY_YEAR, 0);
        int month = data.getIntExtra(DatePickerDialogFragment.KEY_MONTH, 0);
        int day = data.getIntExtra(DatePickerDialogFragment.KEY_DAY, 0);

        Log.v(TAG, String.format("Set default date: %04d/%02d/%02d", year, month, day));

        if (requestCode == REQUEST_CODE_SET_START_DATE) {
            mEvent.setStartDate(year, month, day);

        } else if (requestCode == REQUEST_CODE_SET_END_DATE) {
            mEvent.setEndDate(year, month, day);
        }

        refreshViews();
    }
}
