package com.rnfstudio.babytracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.utility.DatePickerDialogFragment;
import com.rnfstudio.babytracker.utility.MilkPickerDialogFragment;
import com.rnfstudio.babytracker.utility.TimePickerDialogFragment;
import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

    private static Map<String, Integer> sCmdIdMap = null;
    private static Map<Integer, String> sRevCmdIdMap = null;

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
    private TextView durationLabel;
    private TextView durationEdit;
    private TextView amountLabel;
    private TextView amountEdit;

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

        // initialize command id mapping
        if (sCmdIdMap == null) {
            sCmdIdMap = new HashMap<>();
            sRevCmdIdMap = new HashMap<>();

            String[] cmdIds = getResources().getStringArray(R.array.record_edit_activity_type_cmdId);
            for (int i = 0; i < cmdIds.length; i++) {
                sCmdIdMap.put(cmdIds[i], i);
                sRevCmdIdMap.put(i, cmdIds[i]);
            }
        }

        return rootView;
    }

    private void initViews(View root) {
        typeEdit = (TextView) root.findViewById(R.id.typeEdit);
        startDateEdit = (TextView) root.findViewById(R.id.startDateEdit);
        startTimeEdit = (TextView) root.findViewById(R.id.startTimeEdit);
        endDateEdit = (TextView) root.findViewById(R.id.endDateEdit);
        endTimeEdit = (TextView) root.findViewById(R.id.endTimeEdit);
        durationLabel = (TextView) root.findViewById(R.id.durationLabel);
        durationEdit = (TextView) root.findViewById(R.id.durationEdit);
        amountLabel = (TextView) root.findViewById(R.id.amountLabel);
        amountEdit = (TextView) root.findViewById(R.id.amountEdit);

        typeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = sCmdIdMap.get(mEvent.getTypeStr());
                new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(R.array.record_edit_activity_type, selected, null)
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
                                mEvent.setEventType(sRevCmdIdMap.get(selectedPosition));
                                refreshViews();
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

                Bundle args = new Bundle();
                args.putInt(TimePickerDialogFragment.KEY_HOUR_OF_DAY, mEvent.getStartTimeCopy().get(Calendar.HOUR_OF_DAY));
                args.putInt(TimePickerDialogFragment.KEY_MINUTE, mEvent.getStartTimeCopy().get(Calendar.MINUTE));
                newFragment.setArguments(args);

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

                Bundle args = new Bundle();
                args.putInt(TimePickerDialogFragment.KEY_HOUR_OF_DAY, mEvent.getEndTimeCopy().get(Calendar.HOUR_OF_DAY));
                args.putInt(TimePickerDialogFragment.KEY_MINUTE, mEvent.getEndTimeCopy().get(Calendar.MINUTE));
                newFragment.setArguments(args);

                newFragment.setTargetFragment(RecordEditFragment.this, REQUEST_CODE_SET_END_TIME);
                newFragment.show(RecordEditFragment.this.getFragmentManager(), TimePickerDialogFragment.TAG);
            }
        });

        amountEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new MilkPickerDialogFragment();

                Bundle args = new Bundle();
                args.putInt(MilkPickerDialogFragment.EXTRA_DEFAULT_AMOUNT, mEvent.getAmount());
                newFragment.setArguments(args);

                newFragment.setTargetFragment(RecordEditFragment.this, REQUEST_CODE_SET_AMOUNT);
                newFragment.show(RecordEditFragment.this.getFragmentManager(), MilkPickerDialogFragment.TAG);
            }
        });

        // ok/cancel buttons
        Button buttonCancel = (Button) root.findViewById(R.id.button_cancel);
        Button buttonOkay = (Button) root.findViewById(R.id.button_ok);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra(RecordListFragment.KEY_RESULT_CODE, RecordListFragment.RESULT_CODE_CANCEL);
                getActivity().setResult(RecordListFragment.REQUEST_CODE_EDIT, result);
                getActivity().finish();
            }
        });

        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEvent.calculateDuration() <= 0) {
                    Toast.makeText(getActivity(), R.string.error_negative_duration, Toast.LENGTH_SHORT).show();

                    Intent result = new Intent();
                    result.putExtra(RecordListFragment.KEY_RESULT_CODE, RecordListFragment.RESULT_CODE_CANCEL);
                    getActivity().setResult(RecordListFragment.REQUEST_CODE_EDIT, result);
                    getActivity().finish();

                } else {
                    Toast.makeText(getActivity(), R.string.edit_successful, Toast.LENGTH_SHORT).show();

                    // TODO: maybe we can write db in worker thread
                    mEvent.writeDB(getActivity(), false);

                    Intent result = new Intent();
                    result.putExtra(RecordListFragment.KEY_RESULT_CODE, RecordListFragment.RESULT_CODE_CONFIRM);
                    getActivity().setResult(RecordListFragment.REQUEST_CODE_EDIT, result);
                    getActivity().finish();
                }
            }
        });
    }

    private void refreshViews() {
        typeEdit.setText(mEvent.getDisplayType(getActivity()));
        startDateEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getStartTimeCopy(), "yyyy-MM-dd"));
        startTimeEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getStartTimeCopy(), "HH:mm:SS"));
        endDateEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getEndTimeCopy(), "yyyy-MM-dd"));
        endTimeEdit.setText(TimeUtils.flattenCalendarTimeSafely(mEvent.getEndTimeCopy(), "HH:mm:SS"));
        durationEdit.setText(mEvent.getDisplayDuration(getActivity()));
        amountEdit.setText(mEvent.getDisplayAmount(getActivity()));

        // show/hide information
        boolean showAmount = mEvent.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_MEAL_BOTTLED) ||
                mEvent.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_MEAL_MILK);
        amountLabel.setVisibility(showAmount ? View.VISIBLE : View.INVISIBLE);
        amountEdit.setVisibility(showAmount ? View.VISIBLE : View.INVISIBLE);

        boolean showDuration = !mEvent.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_BOTH) &&
                !mEvent.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_PEEPEE) &&
                !mEvent.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_POOPOO);
        durationLabel.setVisibility(showDuration ? View.VISIBLE : View.INVISIBLE);
        durationEdit.setVisibility(showDuration ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int year = data.getIntExtra(DatePickerDialogFragment.KEY_YEAR, 0);
        int month = data.getIntExtra(DatePickerDialogFragment.KEY_MONTH, 0);
        int day = data.getIntExtra(DatePickerDialogFragment.KEY_DAY, 0);
        int hourOfDay = data.getIntExtra(TimePickerDialogFragment.KEY_HOUR_OF_DAY, 0);
        int minute = data.getIntExtra(TimePickerDialogFragment.KEY_MINUTE, 0);
        int amountInML = data.getIntExtra(MilkPickerDialogFragment.EXTRA_MILLI_LITER, 0);

        if (requestCode == REQUEST_CODE_SET_START_DATE) {
            mEvent.setStartDate(year, month, day);

        } else if (requestCode == REQUEST_CODE_SET_END_DATE) {
            mEvent.setEndDate(year, month, day);

        } else if (requestCode == REQUEST_CODE_SET_START_TIME) {
            mEvent.setStartTime(hourOfDay, minute);

        } else if (requestCode == REQUEST_CODE_SET_END_TIME) {
            mEvent.setEndTime(hourOfDay, minute);

        } else if (requestCode == REQUEST_CODE_SET_AMOUNT) {
            mEvent.setAmount(amountInML);
        }

        refreshViews();
    }
}
