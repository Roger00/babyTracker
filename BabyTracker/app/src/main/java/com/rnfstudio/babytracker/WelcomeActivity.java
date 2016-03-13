package com.rnfstudio.babytracker;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Roger on 2016/3/13.
 */
public class WelcomeActivity extends FragmentActivity {
    private static final String TAG = "[WelcomeActivity]";
    private static final String SP_KEY_FIRST_USAGE = "first_usage";

    private static final int[] SETUP_PAGES = {
            R.layout.fragment_setup_welcome,
            R.layout.fragment_setup_name,
            R.layout.fragment_setup_gender,
            R.layout.fragment_setup_birth,
            R.layout.fragment_setup_picture};

    private int mPageIndex = 0;

    public static class WelcomeFragment extends Fragment {
        private static final String ARG_PAGE_INDEX = "page_index";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            int pageIndex = getArguments().getInt(ARG_PAGE_INDEX);
            Log.d(TAG, "[onCreateView] pageIndex: " + pageIndex);

            View root = inflater.inflate(SETUP_PAGES[pageIndex], container, false);
            return root;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isFirstUsage()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_welcome);

        switchToPage(mPageIndex);

        final Button nextBtn = (Button) findViewById(R.id.button_next);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int targetPage = mPageIndex + 1;
                if (targetPage >= 0 && targetPage < SETUP_PAGES.length) {
                    switchToPage(targetPage);

                } else if (targetPage == SETUP_PAGES.length) {
                    completeSetup();
                }
            }
        });

        final Button prevBtn = (Button) findViewById(R.id.button_prev);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedImpl();
            }
        });
    }

    public void completeSetup() {
        // mark as completed
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(SP_KEY_FIRST_USAGE, false).apply();

        // to main page
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * See <a href="http://developer.android.com/intl/zh-tw/training/basics/fragments/creating.html">
     *     Creating a Fragment</a>
     */
    private void switchToPage(int pageIndex) {
        Log.d(TAG, "[switchToPage] pageIndex: " + pageIndex);

        WelcomeFragment newFragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putInt(WelcomeFragment.ARG_PAGE_INDEX, pageIndex);
        newFragment.setArguments(args);
        android.support.v4.app.FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        // update page index
        mPageIndex = pageIndex;
    }

    private boolean isFirstUsage() {
        return true;
//        return PreferenceManager.getDefaultSharedPreferences(this)
//                .getBoolean(SP_KEY_FIRST_USAGE, true);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        onBackPressedImpl();
    }

    private void onBackPressedImpl() {
        int targetPage = mPageIndex - 1;
        if (targetPage >= 0 && targetPage < SETUP_PAGES.length) {
            switchToPage(targetPage);
        }
    }
}
