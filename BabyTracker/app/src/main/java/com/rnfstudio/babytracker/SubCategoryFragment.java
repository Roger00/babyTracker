package com.rnfstudio.babytracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Roger on 2016/1/22.
 */
public class SubCategoryFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_sub_category, container, false);
        Bundle args = getArguments();
        ((Button) rootView.findViewById(R.id.button2)).setText(
                Integer.toString(args.getInt(ARG_OBJECT)));
        return rootView;
    }
}