package com.schmidtdesigns.shiftez.activities;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
        boolean isAccountNotNull = ShiftEZ.getInstance().getAccount() != null;

        Log.i(TAG, "Is User Logged in: " + isAccountNotNull);

        if (isAccountNotNull) {
            Log.i(TAG, "Account Info: " + ShiftEZ.getInstance().getAccount().toString());
        }

        return isAccountNotNull;
    }

}
