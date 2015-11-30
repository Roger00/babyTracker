package com.rnfstudio.babytracker.utility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.rnfstudio.babytracker.R;
import com.rnfstudio.babytracker.RecordEditActivity;
import com.rnfstudio.babytracker.RecordEditFragment;
import com.rnfstudio.babytracker.RecordListFragment;
import com.rnfstudio.babytracker.db.Event;

/**
 * Created by Roger on 2015/11/30.
 *
 * See <a href="http://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog">Create alert dialog</>
 * See <a href="http://www.tutorialsbuzz.com/2014/06/android-dialogfragment-listview.html">Create alert dialog 2</>
 */
public class MenuDialogFragment extends DialogFragment {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    public static final String TAG = "[MenuDialogFragment]";

    public static final String EXTRA_MORE_ACTION = "more action";
    public static final String EXTRA_EVENT = "event";

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
        final Event event = Event.createFromBundle(getArguments());

        Resources res = getActivity().getResources();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_item,
                res.getStringArray(R.array.menu_record_item));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick, which: " + which);

                if (which == 0) {
                    sendResult(which, event);

                } else if (which == 1) {
                    boolean success = event.removeFromDB(getActivity());
                    String toastStr = success ? getString(R.string.delete_successful) :
                            getString(R.string.error_unknown);
                    Toast.makeText(getActivity(), toastStr, Toast.LENGTH_SHORT).show();

                    sendResult(which, event);
                }
            }
        });

        return builder.create();
    }

    private void sendResult(int moreAction, Event event) {
        final int requestCode = getTargetRequestCode();

        Bundle bundle = event.toBundle();
        bundle.putInt(EXTRA_MORE_ACTION, moreAction);

        Intent intent = new Intent();
        intent.putExtras(bundle);

        getTargetFragment().onActivityResult(requestCode, RecordEditFragment.RESULT_CODE_SUCCESS, intent);
    }
}
