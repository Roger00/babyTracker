package com.rnfstudio.babytracker;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rnfstudio.babytracker.db.Profile;

/**
 * Created by Roger on 2016/3/26.
 */
public class DrawerItemClickHandler implements ListView.OnItemClickListener {

    interface SwitchUserCallback {
        void OnSwitchUser(Profile profile);
        void OnCreateNewUser();
    }

    SwitchUserCallback mCallback;

    public DrawerItemClickHandler(SwitchUserCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Profile profile = (Profile) view.getTag();

        // add new profile
        if (profile == null) {
            mCallback.OnCreateNewUser();

        } else {
            mCallback.OnSwitchUser(profile);
        }
    }
}
