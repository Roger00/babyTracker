package com.rnfstudio.babytracker;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
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
        return LayoutInflater.from(context).inflate(R.layout.profile_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Profile profile = Profile.createFromCursor(cursor);

        ImageView profileImage = (ImageView) view.findViewById(R.id.profileImage);
        TextView displayName = (TextView) view.findViewById(R.id.displayName);
        TextView daysFromBirth = (TextView) view.findViewById(R.id.daysFromBirth);

        if (profile != null) {
            displayName.setText(profile.getName());
            daysFromBirth.setText(MainActivity
                    .getDaysFromBirthString(context.getResources(), profile));
            if (profile.hasProfilePicture()) {
                profileImage.setImageBitmap(profile.getProfilePicture());
            }

            // associate profile w/ list item
            view.setTag(profile);
        }

    }
}
