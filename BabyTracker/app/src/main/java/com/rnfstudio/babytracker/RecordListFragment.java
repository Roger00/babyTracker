package com.rnfstudio.babytracker;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.utility.DatePickerDialogFragment;
import com.rnfstudio.babytracker.utility.MenuDialogFragment;
import com.rnfstudio.babytracker.utility.TimeUtils;

import java.util.Calendar;

/**
 * Created by Roger on 2015/8/10.
 */
public class RecordListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    private class RecordAdapter extends CursorAdapter {
        private LayoutInflater mInflater; // Stores the layout inflater

        public RecordAdapter(Context context) {
            super(context, null, 0);

            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);
        }

        /**
         * Find layout and controls, the returned view will be passed to bindView()
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            // Inflates the list item layout.
            final View itemLayout =
                    mInflater.inflate(R.layout.record_list_item, viewGroup, false);
            return itemLayout;
        }

        /**
         * Set data to controls
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView typeText = (TextView) view.findViewById(R.id.type);
            TextView durationText = (TextView) view.findViewById(R.id.duration);
            TextView startTimeText = (TextView) view.findViewById(R.id.startTime);

            final Event event = Event.createFromCursor(cursor);
            typeText.setText(event.getDisplayType(getActivity()));
            startTimeText.setText(TimeUtils.flattenCalendarTimeSafely(event.getStartTimeCopy(), "yyyy-MM-dd HH:mm"));
            durationText.setText(event.getDisplayDuration(getActivity()));

            // add OnClick callback
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startRecordEditor(event);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    DialogFragment newFragment = new MenuDialogFragment();
                    newFragment.setArguments(event.toBundle());
                    newFragment.setTargetFragment(RecordListFragment.this, REQUEST_CODE_MENU);
                    newFragment.show(getFragmentManager(), MenuDialogFragment.TAG);

                    return true;
                }
            });

            // show/hide duration
            boolean showDuration = !event.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_BOTH) &&
                    !event.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_PEEPEE) &&
                    !event.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_POOPOO);
            durationText.setVisibility(showDuration ? View.VISIBLE : View.INVISIBLE);
        }
    }
    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[RecordListFragment]";
    private static final boolean DEBUG = true;

    public static final int REQUEST_CODE_EDIT = 0;
    public static final int REQUEST_CODE_MENU = 1;
    public static final String KEY_RESULT_CODE = "result";
    public static final int RESULT_CODE_CONFIRM = 0;
    public static final int RESULT_CODE_CANCEL = 1;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private RecordAdapter mAdapter;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create ContactsAdapter
        mAdapter = new RecordAdapter(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set adapter for ListView
        setListAdapter(mAdapter);

        // initialize cursor loader
        getLoaderManager().initLoader(RecordLoader.LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (DEBUG) Log.v(TAG, "[onCreateLoader] called, id:" + id);

        if (id == RecordLoader.LOADER_ID) {
            return new RecordLoader(getActivity());
        }

        if (DEBUG) Log.w(TAG, "[onCreateLoader] incorrect ID");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == RecordLoader.LOADER_ID) {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(TAG, "[onActivityResult] requestCode: " + requestCode + ", resultCode: " + resultCode + "data: " + data);

        if (requestCode == REQUEST_CODE_EDIT) {
            if (data != null && data.getIntExtra(KEY_RESULT_CODE, RESULT_CODE_CANCEL) == RESULT_CODE_CONFIRM) {
                Log.v(TAG, "[onActivityResult] restart loader for edit confirm");
                getLoaderManager().restartLoader(RecordLoader.LOADER_ID, null, this);
            }
        } else if (requestCode == REQUEST_CODE_MENU) {
            Bundle bundle = data.getExtras();
            Event event = Event.createFromBundle(bundle);
            int moreAction = bundle.getInt(MenuDialogFragment.EXTRA_MORE_ACTION, -1);
            switch (moreAction) {
                case 0:
                    startRecordEditor(event);
                    break;
                case 1:
                    Log.v(TAG, "[onActivityResult] restart loader for deletion");
                    getLoaderManager().restartLoader(RecordLoader.LOADER_ID, null, this);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == RecordLoader.LOADER_ID) {
            mAdapter.swapCursor(null);
        }
    }

    private void startRecordEditor(Event event) {
        Log.v(TAG, "startRecordEditor");

        Intent edit = new Intent(getActivity(), RecordEditActivity.class);
        edit.putExtras(event.toBundle());
        startActivityForResult(edit, REQUEST_CODE_EDIT);
    }
}
