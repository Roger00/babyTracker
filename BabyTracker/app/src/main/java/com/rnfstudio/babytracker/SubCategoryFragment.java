package com.rnfstudio.babytracker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.utility.CircleView;
import com.rnfstudio.babytracker.utility.CircleWidget;
import com.rnfstudio.babytracker.utility.MenuDialogFragment;

import java.util.ArrayList;

/**
 * Created by Roger on 2016/1/22.
 */
public class SubCategoryFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
            RecordAdapter.RecordItemCallbacks,
            OnEventChangedListener {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[SubCategoryFragment]";

    private static final boolean DEBUG = false;

    public static final int REQUEST_CODE_EDIT = 0;
    public static final int REQUEST_CODE_MENU = 1;
    public static final String KEY_RESULT_CODE = "result";
    public static final int RESULT_CODE_CONFIRM = 0;
    public static final int RESULT_CODE_CANCEL = 1;

    public static final String ARG_TAB_ID = "tab_id";

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
    }

    /**
     * Override onCreateView Use customized layout
     *
     * Refer to:
     * http://developer.android.com/intl/zh-tw/reference/android/app/ListFragment.html
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.fragment_sub_category, container, false);

        Bundle args = getArguments();
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

        mCircleWidget = new CircleWidget(getActivity());
        mCircleWidget.setCircle((CircleView) rootView.findViewById(R.id.circle));
        mCircleWidget.setInfoPanel((TextView) rootView.findViewById(R.id.circleTitle));

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set adapter for CircleView
        setListAdapter(mRecordAdapter);

        // initialize cursor loader
        getLoaderManager().initLoader(RecordLoader.LOADER_ID_DEFAULT, null, this);
        getLoaderManager().initLoader(RecordLoader.LOADER_ID_CIRCLE, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (DEBUG) Log.v(TAG, "[onCreateLoader] called, id:" + id + ", mainType:" + getMainType());

        if (id == RecordLoader.LOADER_ID_DEFAULT) {
            return new RecordLoader(getActivity(), RecordLoader.QUERY_TYPE_ALL, getMainType());

        } else if (id == RecordLoader.LOADER_ID_CIRCLE){
            return new RecordLoader(getActivity(), RecordLoader.QUERY_TYPE_CIRCLE, getMainType());
        }

        if (DEBUG) Log.w(TAG, "[onCreateLoader] incorrect ID");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (DEBUG) Log.v(TAG, "[onLoadFinished] called, id:" + loader.getId() + ", mainType:" + getMainType());

        if (loader.getId() == RecordLoader.LOADER_ID_DEFAULT) {
            mRecordAdapter.swapCursor(data);

        } else if (loader.getId() == RecordLoader.LOADER_ID_CIRCLE) {
            mCircleWidget.setEvents(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (DEBUG) Log.v(TAG, "[onLoaderReset] called, id:" + loader.getId() + ", mainType:" + getMainType());

        if (loader.getId() == RecordLoader.LOADER_ID_DEFAULT) {
            mRecordAdapter.swapCursor(null);

        } else if (loader.getId() == RecordLoader.LOADER_ID_CIRCLE) {
            mCircleWidget.setEvents(new ArrayList<Event>());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(TAG, "[onActivityResult] requestCode: " + requestCode + ", resultCode: " + resultCode + "data: " + data);

        if (requestCode == REQUEST_CODE_EDIT) {
            if (data != null && data.getIntExtra(KEY_RESULT_CODE, RESULT_CODE_CANCEL) == RESULT_CODE_CONFIRM) {
                Log.v(TAG, "[onActivityResult] restart loader for edit confirm");
                getLoaderManager().restartLoader(RecordLoader.LOADER_ID_DEFAULT, null, this);
                getLoaderManager().restartLoader(RecordLoader.LOADER_ID_CIRCLE, null, this);
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
                    getLoaderManager().restartLoader(RecordLoader.LOADER_ID_DEFAULT, null, this);
                    getLoaderManager().restartLoader(RecordLoader.LOADER_ID_CIRCLE, null, this);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "[onAttach] called, type: " + getMainType());
    }

    protected void startRecordEditor(Event event) {
        Log.v(TAG, "startRecordEditor");

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
    public void onRecordClick(Event e) {
        startRecordEditor(e);
    }

    @Override
    public void onRecordLongClick(Event e) {
        DialogFragment newFragment = new MenuDialogFragment();
        newFragment.setArguments(e.toBundle());
        newFragment.setTargetFragment(SubCategoryFragment.this, REQUEST_CODE_MENU);
        newFragment.show(getFragmentManager(), MenuDialogFragment.TAG);
    }

    @Override
    public void onEventChanged(int mainType) {
        Log.v(TAG, "[onEventChanged] restart loaders for event change, mainType: " + mainType);

        if (isAdded()) {
            getLoaderManager().restartLoader(RecordLoader.LOADER_ID_DEFAULT, null, this);
            getLoaderManager().restartLoader(RecordLoader.LOADER_ID_CIRCLE, null, this);
        } else {
            Log.v(TAG, "[onEventChanged] fragment not attached to activity, skip update");
        }
    }
}