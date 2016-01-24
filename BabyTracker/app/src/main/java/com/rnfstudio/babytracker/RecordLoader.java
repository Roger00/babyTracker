package com.rnfstudio.babytracker;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.db.EventDB;
import com.rnfstudio.babytracker.utility.CircleWidget;

/**
 * A special loader which loads from db instead from ContentProvider
 *
 * Created by Roger on 2015/8/10.
 *
 * Refer to: http://stackoverflow.com/questions/7182485/usage-cursorloader-without-contentprovider
 */
public class RecordLoader extends AsyncTaskLoader<Cursor> {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final int LOADER_ID_DEFAULT = 0;
    public static final int LOADER_ID_CIRCLE = 1;

    public static final int QUERY_TYPE_ALL = 0;
    public static final int QUERY_TYPE_CIRCLE = 1;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private Context mContext;
    private int mMainType = EventContract.EventEntry.NO_TYPE;
    private int mQueryType = QUERY_TYPE_ALL;
    private Cursor mCursor;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Constructs RecordLoader instance
     *
     * @param queryType: type of query you want to perform. E.g., all events or today events
     * @param mainType: main type of events you want to query. E.g., sleep, meal or diaper
     */
    public RecordLoader(Context context, int queryType, int mainType) {
        super(context);
        mContext = context;
        mQueryType = queryType;
        mMainType = mainType;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        EventDB db = MainApplication.getEventDatabase(mContext);

        if (mQueryType == QUERY_TYPE_ALL) {
            mCursor = db.queryAllEvents(mMainType);

        } else if (mQueryType == QUERY_TYPE_CIRCLE) {
            mCursor = db.queryEventsForMainTypeAndPeriod(mMainType,
                    CircleWidget.getQueryAheadTIme(),
                    CircleWidget.getQueryEndTime(),
                    CircleWidget.getQueryStartTime(),
                    CircleWidget.getQueryEndTime());
        }

        return mCursor;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}
