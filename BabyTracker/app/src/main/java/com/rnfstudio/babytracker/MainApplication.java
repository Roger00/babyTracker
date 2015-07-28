package com.rnfstudio.babytracker;

import android.app.Application;
import android.content.Context;

import com.rnfstudio.babytracker.db.EventDB;

/**
 * Created by Roger on 2015/7/22.
 */
public class MainApplication extends Application {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private EventDB mEventDB = null;

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
    public void onCreate() {
        mEventDB = getEventDatabase(this);
    }

    public static synchronized EventDB getEventDatabase(Context context) {
        MainApplication app = (MainApplication)context.getApplicationContext();
        if (app.mEventDB == null) {
            app.mEventDB = new EventDB(context.getApplicationContext());
        }
        return app.mEventDB;
    }
}
