package com.rnfstudio.babytracker;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.db.EventProvider;
import com.rnfstudio.babytracker.utility.CircleView;
import com.rnfstudio.babytracker.utility.CircleWidget;
import com.rnfstudio.babytracker.utility.MenuDialogFragment;

import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * Created by Roger on 2016/1/22.
 */
public class RecordListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
            RecordAdapter.RecordItemCallbacks {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[RecordListFragment]";

    private static final boolean DEBUG = false;

    public static final int REQUEST_CODE_EDIT = 0;
    public static final int REQUEST_CODE_MENU = 1;

    public static final String KEY_RESULT_CODE = "result";
    public static final int RESULT_CODE_CONFIRM = 0;
    public static final int RESULT_CODE_CANCEL = 1;

    public static final String ARG_TAB_ID = "tab_id";

    public static final int LOADER_ID_DEFAULT = 0;
    public static final int LOADER_ID_CIRCLE = 1;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private RecordAdapter mRecordAdapter;
    private CircleWidget mCircleWidget;
    private ContentObserver mContentObserver;

    private int mMainType = EventContract.EventEntry.NO_TYPE;

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

        // create an adapter for ListView
        mRecordAdapter = new RecordAdapter(getActivity(), this);

        // create content observer for event changes
        mContentObserver = new ContentObserver(new Handler(getActivity().getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                restartLoaders();
            }
        };

        // register observer
        getActivity().getContentResolver().registerContentObserver(
                EventProvider.sNotifyUriForEvent,
                true,
                mContentObserver);
    }

    /**
     * Override onCreateView Use customized layout
     *
     * Refer to:
     * http://developer.android.com/intl/zh-tw/reference/android/app/ListFragment.html
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.fragment_sub_category, container, false);

        Bundle args = getArguments() == null ? new Bundle() : getArguments();

        switch (args.getInt(ARG_TAB_ID)) {
            case MainActivity.TAB_ID_MEAL:
                setMainType(EventContract.EventEntry.EVENT_TYPE_MEAL);
                break;
            case MainActivity.TAB_ID_DIAPER:
                setMainType(EventContract.EventEntry.EVENT_TYPE_DIAPER);
                break;
            case MainActivity.TAB_ID_SLEEP:
                setMainType(EventContract.EventEntry.EVENT_TYPE_SLEEP);
                break;
            default:
                setMainType(EventContract.EventEntry.NO_TYPE);
                break;
        }


        if (isEnableCircleWidget()) {
            mCircleWidget = new CircleWidget(getActivity());
            mCircleWidget.setCircle((CircleView) rootView.findViewById(R.id.circle));
            mCircleWidget.setInfoPanel((TextView) rootView.findViewById(R.id.circleTitle));
            mCircleWidget.setMainType(getMainType());
        } else {
            final FrameLayout circleWidget = (FrameLayout) rootView.findViewById(R.id.circleWidget);
            circleWidget.setVisibility(GONE);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set adapter for CircleView
        setListAdapter(mRecordAdapter);

        // initialize cursor loader
        getLoaderManager().initLoader(LOADER_ID_DEFAULT, null, this);
        if (isEnableCircleWidget()) getLoaderManager().initLoader(LOADER_ID_CIRCLE, null, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // unregister observer
        getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String typeStr = Integer.toString(getMainType());
        String userIdStr = Long.toString(MainApplication.getUserId(getActivity()));

        boolean hasMainType = getMainType() != EventContract.EventEntry.NO_TYPE;

        // only select data from the latest half-day
        String selection = EventContract.EventEntry.COLUMN_NAME_USER_ID + "=?";
        selection += hasMainType ?
                " AND " + EventContract.EventEntry.COLUMN_NAME_EVENT_TYPE + "=? AND " +
                EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME +
                " BETWEEN ? AND ? AND " +
                EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME +
                " BETWEEN ? AND ?" : "";

        if (id == LOADER_ID_DEFAULT) {

            return new CursorLoader(getActivity(),
                    EventProvider.sNotifyUriForEvent,
                    EventContract.getQueryProjection(),
                    selection,
                    getCircleViewQuerySelectionArgs(userIdStr, typeStr, hasMainType),
                    EventContract.EventEntry.COLUMN_NAME_EVENT_END_TIME + " DESC");

        } else if (id == LOADER_ID_CIRCLE) {

            return new CursorLoader(getActivity(),
                    EventProvider.sNotifyUriForEvent,
                    EventContract.getQueryProjection(),
                    selection,
                    getCircleViewQuerySelectionArgs(userIdStr, typeStr, hasMainType),
                    EventContract.EventEntry.COLUMN_NAME_EVENT_START_TIME + " ASC");
        }

        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private String[] getCircleViewQuerySelectionArgs(String userId,
                                                     String type,
                                                     boolean hasMainType) {

        String aheadTime = Long.toString(CircleWidget.getQueryAheadTIme());
        String endTime = Long.toString(CircleWidget.getQueryEndTime());
        String startTime = Long.toString(CircleWidget.getQueryStartTime());

        return hasMainType ? new String[] {userId, type, aheadTime, endTime, startTime, endTime} :
                new String[] {userId};
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_ID_DEFAULT) {
            mRecordAdapter.swapCursor(data);

        } else if (loader.getId() == LOADER_ID_CIRCLE) {
            mCircleWidget.setEvents(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ID_DEFAULT) {
            mRecordAdapter.swapCursor(null);

        } else if (loader.getId() == LOADER_ID_CIRCLE) {
            mCircleWidget.setEvents(new ArrayList<Event>());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MENU) {
            Bundle bundle = data.getExtras();
            Event event = Event.createFromBundle(bundle);
            int moreAction = bundle.getInt(MenuDialogFragment.EXTRA_MORE_ACTION, -1);
            switch (moreAction) {
                case 0:
                    startRecordEditor(event);
                    break;
                default:
                    break;
            }
        }
    }

    protected void startRecordEditor(Event event) {
        Intent edit = new Intent(getActivity(), RecordEditActivity.class);
        edit.putExtras(event.toBundle());
        startActivityForResult(edit, REQUEST_CODE_EDIT);
    }

    public void setMainType(int type) {
        mMainType = type;
    }

    public int getMainType() {
        return mMainType;
    }

    @Override
    public void onRecordClick(Event event) {
        startRecordEditor(event);
    }

    @Override
    public void onRecordLongClick(Event e) {
        DialogFragment newFragment = new MenuDialogFragment();
        newFragment.setArguments(e.toBundle());
        newFragment.setTargetFragment(RecordListFragment.this, REQUEST_CODE_MENU);
        newFragment.show(getFragmentManager(), MenuDialogFragment.TAG);
    }

    private void restartLoaders() {
        getLoaderManager().restartLoader(LOADER_ID_DEFAULT, null, this);

        if (isEnableCircleWidget()) {
            getLoaderManager().restartLoader(LOADER_ID_CIRCLE, null, this);
        }
    }

    private boolean isEnableCircleWidget() {
        return getMainType() != EventContract.EventEntry.NO_TYPE;
    }
}