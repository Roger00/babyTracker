package com.rnfstudio.babytracker;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.utility.TimeUtils;

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
                    Intent edit = new Intent(getActivity(), RecordEditActivity.class);
                    edit.putExtras(event.toBundle());
                    getActivity().startActivity(edit);
                }
            });
        }
    }
    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[RecordListFragment]";
    private static final boolean DEBUG = true;

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
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == RecordLoader.LOADER_ID) {
            mAdapter.swapCursor(null);
        }
    }
}
