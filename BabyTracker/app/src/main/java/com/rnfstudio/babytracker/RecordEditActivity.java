package com.rnfstudio.babytracker;

import android.app.Activity;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.db.EventDB;
import com.rnfstudio.babytracker.utility.TimeUtils;

/**
 * Created by Roger on 2015/8/11.
 */
public class RecordEditActivity extends Activity {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "[RecordEditActivity]";
    private static final Boolean DEBUG = true;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private Event mEvent;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // parse starting intent extra
        Intent startIntent = getIntent();
        mEvent = Event.createFromBundle(startIntent.getExtras());
        if (mEvent.getId() == 0) {
            if (DEBUG) Log.w(TAG, "[onCreate] Empty extras, fail to initialize activiity");
            finish();
        }

        // inflate views and initialize them
        setContentView(R.layout.activity_record_editor);
        initViews();

        // ok/cancel buttons
        Button buttonCancel = (Button) findViewById(R.id.button_cancel);
        Button buttonOkay = (Button) findViewById(R.id.button_ok);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: maybe we can write db in worker thread
                mEvent.writeDB(RecordEditActivity.this);

                // FIXME: this doesn't seem to work
//                // notify RecordLoader that data has changed
//                Loader loader = RecordEditActivity.this.getLoaderManager().getLoader(RecordLoader.LOADER_ID);
//                if (loader != null) loader.onContentChanged();

                finish();
            }
        });
    }

    private void initViews() {
        TextView typeEdit = (TextView) findViewById(R.id.typeEdit);
        TextView startEdit = (TextView) findViewById(R.id.startEdit);
        TextView endEdit = (TextView) findViewById(R.id.endEdit);
        TextView durationEdit = (TextView) findViewById(R.id.durationEdit);
        TextView amountEdit = (TextView) findViewById(R.id.amountEdit);

        Intent i = getIntent();
        Event event = Event.createFromBundle(i.getExtras());
        if (event.getId() == 0) return;

        typeEdit.setText(event.getDisplayType(this));
        startEdit.setText(TimeUtils.flattenCalendarTimeSafely(event.getStartTimeCopy(), "yyyy-MM-dd HH:mm"));
        endEdit.setText(TimeUtils.flattenCalendarTimeSafely(event.getEndTimeCopy(), "yyyy-MM-dd HH:mm"));
        durationEdit.setText(event.getDisplayDuration(this));
        amountEdit.setText(event.getDisplayAmount(this));

        typeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
