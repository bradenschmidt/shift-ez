package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.fragments.SchedulePagerFragment;
import com.schmidtdesigns.shiftez.models.Account;

public class MainActivity extends BaseActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if(!isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            String accountString = sharedPrefs.getString(Constants.ACCOUNT_PARAM, null);
            ShiftEZ shiftEZ = ShiftEZ.getInstance();
            if (shiftEZ != null) {
                if (accountString != null) {
                    // Recover using cache pref data
                    shiftEZ.setAccount(Account.deserializeFromJson(accountString));
                } else {
                    // No pref data saved
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }

            } else {
                Log.i(TAG, "shiftEZ is null");
            }
        }

        displayView(new SchedulePagerFragment(), false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_revoke:
                revoke();
                return true;
            default:
                Log.e(TAG, "Invalid menu action id received: " + id);
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayView(Fragment fragment, boolean hasUp) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(hasUp);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

}
