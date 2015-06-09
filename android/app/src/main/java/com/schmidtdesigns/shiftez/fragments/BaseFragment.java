package com.schmidtdesigns.shiftez.fragments;

/**
 */

import android.support.v4.app.Fragment;

import com.octo.android.robospice.SpiceManager;
import com.schmidtdesigns.shiftez.network.RetrofitSpiceService;

/**
 * Handles SpiceManager on fragment lifecycle changes.
 * <p/>
 * <p/>
 * Created by braden on 15-06-09.
 */
public class BaseFragment extends Fragment {

    /**
     * The Tag of this class used for logging
     */
    private String TAG = "BaseFragment";

    /**
     * Create a spiceManager for all fragments
     */
    private SpiceManager spiceManager = new SpiceManager(RetrofitSpiceService.class);

    @Override
    public void onStart() {
        spiceManager.start(getActivity());
        super.onStart();
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
