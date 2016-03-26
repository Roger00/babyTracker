package com.rnfstudio.babytracker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.Event;
import com.rnfstudio.babytracker.utility.TimeUtils;

/**
 * Created by Roger on 2016/1/24.
 */
public class RecordAdapter extends CursorAdapter {

    interface RecordItemCallbacks {
        void onRecordClick(Event e);
        void onRecordLongClick(Event e);
    }

    private RecordItemCallbacks mCallbacks;

    public RecordAdapter(Context context, RecordItemCallbacks callbacks) {
        super(context, null, 0);
        mCallbacks = callbacks;
    }

    /**
     * Find layout and controls, the returned view will be passed to bindView()
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context)
                .inflate(R.layout.record_list_item, viewGroup, false);
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
        typeText.setText(event.getDisplayType(context));
        startTimeText.setText(TimeUtils.flattenCalendarTimeSafely(event.getStartTimeCopy(),
                "yyyy-MM-dd HH:mm"));
        durationText.setText(event.getDisplayDuration(context));

        // add OnClick callback
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onRecordClick(event);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCallbacks.onRecordLongClick(event);
                return true;
            }
        });

        // show/hide duration
        boolean showDuration =
                !event.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_BOTH) &&
                !event.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_PEEPEE) &&
                !event.getTypeStr().equals(SwipeButtonHandler.MENU_ITEM_DIAPER_POOPOO);
        durationText.setVisibility(showDuration ? View.VISIBLE : View.GONE);
    }
}
