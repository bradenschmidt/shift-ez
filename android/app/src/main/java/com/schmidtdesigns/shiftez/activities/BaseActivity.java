package com.schmidtdesigns.shiftez.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.network.RetrofitSpiceService;

/**
 * Created by braden on 15-06-09.
 */
public class BaseActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

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
            return true;
        } else {
            // Attempt to recover account data from prefs
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            String accountString = sharedPrefs.getString(Constants.ACCOUNT_PARAM, null);
            ShiftEZ shiftEZ = ShiftEZ.getInstance();
            if (shiftEZ != null && accountString != null) {
                // Recover using cache pref data
                shiftEZ.setAccount(Account.deserializeFromJson(accountString));
                return true;
            }
        }

        return false;
    }
}
