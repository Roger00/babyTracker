package com.rnfstudio.babytracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Roger on 2015/8/6.
 */
public class MilkPickerDialogFragment extends DialogFragment {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "MilkPicker";
    private static final int INCREMENT_DECREMENT_AMOUNT = 10;
    private static final int MAX_AMOUNT = 500;
    private static final int MIN_AMOUNT = 0;
    private static final int DEFAULT_AMOUNT = 100;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private String mFuncId = null;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public MilkPickerDialogFragment(String id) {
        mFuncId = id;
    }

    public static MilkPickerDialogFragment newInstance(String id) {
        MilkPickerDialogFragment fragment = new MilkPickerDialogFragment(id);
        return fragment;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // inflate layout
        Context context = getActivity();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_milk_picker, null);

        // set callbacks
        Button minusBtn = (Button) layout.findViewById(R.id.minus);
        Button plusBtn = (Button) layout.findViewById(R.id.plus);
        final EditText amountEdit = (EditText) layout.findViewById(R.id.amount);
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAmountSafely(amountEdit, (-1) * INCREMENT_DECREMENT_AMOUNT);
            }
        });
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAmountSafely(amountEdit, INCREMENT_DECREMENT_AMOUNT);
            }
        });

        // build dialog
        return new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "ok" + getAmountFromEdit(amountEdit));
                        MainActivity caller = (MainActivity) getActivity();
                        caller.onMilkPickerResult(mFuncId, getAmountFromEdit(amountEdit));
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "cancel" + getAmountFromEdit(amountEdit));
                        dismiss();
                    }
                })
                .create();
    }

    private void changeAmountSafely(EditText amountEdit, int increment) {
        int amount = getAmountFromEdit(amountEdit);
        amount = Math.max(MIN_AMOUNT, Math.min(amount + increment, MAX_AMOUNT));
        amountEdit.setText(String.format("%d", amount));
    }

    private int getAmountFromEdit(EditText edit) {
        String amountStr = edit.getText().toString();
        int ret = DEFAULT_AMOUNT;
        try {
            ret = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
        }
        return ret;
    }
}
