package com.rnfstudio.babytracker.utility;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.rnfstudio.babytracker.MainActivity;
import com.rnfstudio.babytracker.R;

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
    private static final int MAX_AMOUNT = 250;
    private static final int MIN_AMOUNT = 0;
    private static final int DEFAULT_AMOUNT = 100;

    private static SoundPool sSoundPool;
    private static int[] sSoundIds;
    private static final int MAX_AUDIO_STREAMS = 10;
    private static final int ID_SOUND_EFFECT_INCREASE = 0;
    private static final int ID_SOUND_EFFECT_DECREASE = 1;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    static {
        sSoundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()).setMaxStreams(MAX_AUDIO_STREAMS).build();

        sSoundIds = new int[MAX_AUDIO_STREAMS];
    }

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
    // @FIXME: do use default constructor ONLY
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
        // load sound effects
        sSoundIds[ID_SOUND_EFFECT_INCREASE] = sSoundPool.load(getActivity(), R.raw.sound_effect_inc, 1);
        sSoundIds[ID_SOUND_EFFECT_DECREASE] = sSoundPool.load(getActivity(), R.raw.sound_effect_dec, 1);

        // inflate layout
        Context context = getActivity();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_milk_picker, null);

        // set callbacks
        Button minusBtn = (Button) layout.findViewById(R.id.minus);
        Button plusBtn = (Button) layout.findViewById(R.id.plus);
        final EditText amountEdit = (EditText) layout.findViewById(R.id.amount);
        amountEdit.setText(String.valueOf(DEFAULT_AMOUNT));
        final ImageView milkBody = (ImageView) layout.findViewById(R.id.milkBody);
        milkBody.setImageDrawable(getResources().getDrawable(R.drawable.milk_body, null));
        milkBody.setPivotY(getResources().getDimension(R.dimen.milk_body_height));
        milkBody.setScaleY(((float) DEFAULT_AMOUNT) / MAX_AMOUNT);
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAmountSafely(amountEdit, milkBody, (-1) * INCREMENT_DECREMENT_AMOUNT);
            }
        });
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAmountSafely(amountEdit, milkBody, INCREMENT_DECREMENT_AMOUNT);
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

    private void changeAmountSafely(EditText amountEdit, ImageView milkBody, int increment) {
        int oldAmount = getAmountFromEdit(amountEdit);
        int newAmount = Math.max(MIN_AMOUNT, Math.min(oldAmount + increment, MAX_AMOUNT));
        amountEdit.setText(String.format("%d", newAmount));

        milkBody.setPivotY(getResources().getDimension(R.dimen.milk_body_height));

        AnimatorSet as = new AnimatorSet();
        as.playTogether(ObjectAnimator.ofFloat(milkBody, "scaleY", ((float) oldAmount / MAX_AMOUNT), ((float) newAmount / MAX_AMOUNT)));
        as.playTogether(ObjectAnimator.ofFloat(milkBody, "alpha", (float) 0.5, (float) 1.0));
        as.setDuration(700);
        as.start();

        int soundId = increment > 0 ? ID_SOUND_EFFECT_INCREASE : ID_SOUND_EFFECT_DECREASE;
        sSoundPool.play(sSoundIds[soundId], 1, 1, 1, 0, (float)2.0);
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
