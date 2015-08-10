package com.rnfstudio.babytracker;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.utility.TimeUtils;
import com.rnfstudio.babytracker.utility.Utilities;

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

            int type = cursor.getInt(EventContract.EventQuery.EVENT_TYPE);
            int subType = cursor.getInt(EventContract.EventQuery.EVENT_SUBTYPE);
            String typeStr = Utilities.getDisplayCmd(getActivity(), EventContract.EventEntry.getTypeStr(type, subType));
            Calendar startTime = TimeUtils.unflattenEventTime((cursor.getString(EventContract.EventQuery.EVENT_START_TIME)));
            Calendar endTime = TimeUtils.unflattenEventTime((cursor.getString(EventContract.EventQuery.EVENT_END_TIME)));
            int durationInSec = (int) (cursor.getLong(EventContract.EventQuery.EVENT_DURATION) / 1000);
            int amountInMilliLiter = cursor.getInt(EventContract.EventQuery.EVENT_AMOUNT);

            typeText.setText(typeStr);
            startTimeText.setText(TimeUtils.flattenCalendarTimeSafely(startTime, "yyyy-MM-dd HH:mm"));

            int secs = TimeUtils.getRemainSeconds(durationInSec);
            int mins = TimeUtils.getRemainMinutes(durationInSec);
            int hours = TimeUtils.getRemainHours(durationInSec);
            int days = TimeUtils.getRemainDays(durationInSec);

            Resources res = context.getResources();
            String duration = "";
            if (days > 0) {
                duration =  res.getQuantityString(R.plurals.duration_info_days, days, days);
            } else if (hours > 0) {
                duration = res.getQuantityString(R.plurals.duration_info_hours, hours, hours, mins);
            } else if (mins > 0) {
                duration = res.getQuantityString(R.plurals.duration_info_minutes, mins, mins);
            } else if (secs > 0) {
                duration = res.getQuantityString(R.plurals.duration_info_seconds, secs, secs);
            } else {
                duration = res.getString(R.string.duration_info_pretty_short);
            }
            durationText.setText(duration);
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
