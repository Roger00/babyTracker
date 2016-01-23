package com.rnfstudio.babytracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.db.EventContract;
import com.rnfstudio.babytracker.utility.MenuDialogFragment;

/**
 * Created by Roger on 2015/8/10.
 */
public class RecordListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, RecordAdapter.RecordItemCallbacks {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

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

        // create RecordAdapter
        mAdapter = new RecordAdapter(getActivity(), this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set adapter for ListView
        setListAdapter(mAdapter);

        // initialize cursor loader
        getLoaderManager().initLoader(RecordLoader.LOADER_ID_DEFAULT, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (DEBUG) Log.v(TAG, "[onCreateLoader] called, id:" + id);

        if (id == RecordLoader.LOADER_ID_DEFAULT) {
            return new RecordLoader(getActivity(), RecordLoader.QUERY_TYPE_ALL, getMainType());
        }

        if (DEBUG) Log.w(TAG, "[onCreateLoader] incorrect ID");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == RecordLoader.LOADER_ID_DEFAULT) {
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
                getLoaderManager().restartLoader(RecordLoader.LOADER_ID_DEFAULT, null, this);
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
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == RecordLoader.LOADER_ID_DEFAULT) {
            mAdapter.swapCursor(null);
        }
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
        newFragment.setTargetFragment(RecordListFragment.this, REQUEST_CODE_MENU);
        newFragment.show(getFragmentManager(), MenuDialogFragment.TAG);
    }
}
