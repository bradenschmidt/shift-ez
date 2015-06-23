package com.schmidtdesigns.shiftez.activities;

import android.support.v7.app.AppCompatActivity;

import com.octo.android.robospice.SpiceManager;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.network.RetrofitSpiceService;

/**
 * Created by braden on 15-06-09.
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected SpiceManager spiceManager = new SpiceManager(RetrofitSpiceService.class);

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }

    public boolean isLoggedIn() {
        return ShiftEZ.getInstance().getAccount() != null;
    }

}
