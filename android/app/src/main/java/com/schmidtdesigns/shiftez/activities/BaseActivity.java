package com.schmidtdesigns.shiftez.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.octo.android.robospice.SpiceManager;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.Store;
import com.schmidtdesigns.shiftez.network.RetrofitSpiceService;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by braden on 15-06-09.
 */
public class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BaseActivity";
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    protected SpiceManager spiceManager = new SpiceManager(RetrofitSpiceService.class);
    private GoogleApiClient mGoogleApiClient;
    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);

        /*
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        */
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();

        /*
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
        */
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    public boolean isLoggedIn() {
        boolean isAccountNotNull = ShiftEZ.getInstance().getAccount() != null;

        Log.i(TAG, "Is User Logged in: " + isAccountNotNull);

        if (isAccountNotNull) {
            Log.i(TAG, "Account Info: " + ShiftEZ.getInstance().getAccount().toString());
        }

        return isAccountNotNull;
    }

    private boolean checkForPlayServices() {
        int state = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (state == ConnectionResult.SUCCESS) {
            Toast.makeText(this, "SUCCESS", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(state, this, -1);
            dialog.show();
            return false;
        }
    }

    public boolean login() {
        if (!checkForPlayServices()) {
            Log.e(TAG, "ERROR: Play Services is not installed.");
            return false;
        } else {
            mGoogleApiClient.connect();
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        Log.i(TAG, "***********In onConnected.");
        getProfileInformation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "***********In onConnectionFailed.");

        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public void getProfileInformation() {
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        // TODO: GET FROM SERVER
        ArrayList<Store> stores = new ArrayList<>();
        ArrayList<String> deps = new ArrayList<>(Arrays.asList("Lumber", "Hardware"));
        Store store1 = new Store("8th St Coop Home Centre", deps);
        Store store2 = new Store("Rona", deps);
        stores.add(store1);
        stores.add(store2);
        // END TODO

        ShiftEZ.getInstance().setAccount(new Account(person.getName().toString(), email, stores));

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //get the preferences editor
        SharedPreferences.Editor editor = pref.edit();

        // save user info
        editor.putString(Constants.ACCOUNT_PARAM, Account.serializeToJson(ShiftEZ.getInstance().getAccount()));
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
    }

    public void revoke() {
        Log.i(TAG, "Revoke triggered");

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //get the preferences editor
        SharedPreferences.Editor editor = pref.edit();
        // Remove the account data saved in prefs
        editor.remove(Constants.ACCOUNT_PARAM);
        editor.apply();

        // Revoke all granted permissions and clear the default account.  The user will have
        // to pass the consent screen to sign in again.
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                mGoogleApiClient.disconnect();
            }
        } else {
            Log.e(TAG, "GPlus API Client is null during revoke.");
        }
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void logout() {
        Log.i(TAG, "Logout triggered");

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //get the preferences editor
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(Constants.ACCOUNT_PARAM);
        editor.apply();

        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
            }
        } else {
            Log.e(TAG, "GPlus API Client is null during logout.");
        }
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
