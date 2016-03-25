package com.rnfstudio.babytracker;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rnfstudio.babytracker.db.Profile;

/**
 * Created by Roger on 2016/3/25.
 */
public class ProfileAdapter extends CursorAdapter {

    public ProfileAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View itemLayout =
                LayoutInflater.from(context).inflate(R.layout.profile_list_item, parent, false);
        return itemLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Profile profile = Profile.createFromCursor(cursor);
        if (profile == null) {
            Log.d("xxxxx", "null profile");
            return;
        }

        ImageView profileImage = (ImageView) view.findViewById(R.id.profileImage);
        TextView displayName = (TextView) view.findViewById(R.id.displayName);
        TextView daysFromBirth = (TextView) view.findViewById(R.id.daysFromBirth);

        profileImage.setImageBitmap(profile.getProfilePicture());
        displayName.setText(profile.getName());
        daysFromBirth.setText(MainActivity.getDaysFromBirthString(context.getResources(), profile));
    }
}
