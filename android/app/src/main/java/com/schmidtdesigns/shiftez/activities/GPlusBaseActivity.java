package com.schmidtdesigns.shiftez.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.Utils;

/**
 * Created by braden on 15-06-09.
 */
public abstract class GPlusBaseActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    private static final String TAG = "GPlusBaseActivity";
    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 9001;
    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore from saved instance state
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mIsResolving);
    }

    public boolean login() {
        if (!Utils.isOnline(getApplicationContext())) {
            Log.e(TAG, "ERROR: No Network Connection.");
            Toast.makeText(this, "No Network Connection.", Toast.LENGTH_LONG).show();
            return false;
        } else if (!Utils.checkForPlayServices(this)) {
            Log.e(TAG, "ERROR: Play Services is not installed.");
            return false;
        } else {
            mShouldResolve = true;
            mGoogleApiClient.connect();
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + connectionHint);

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            if (ShiftEZ.getInstance().getAccount() == null) {
                getProfileInformation();
            } else {
                Log.i(TAG, "Account is not null.");
            }
        } else {
            Log.e(TAG, "getCurrentPerson is null");
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult result) {
        Log.e(TAG, "result.getStatus():" + result.getStatus());
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "***********In onConnectionFailed.");

        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            updateUI(false);
        }
    }

    private void showErrorDialog(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
                            updateUI(false);
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt, errorCode);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            mShouldResolve = false;
            updateUI(false);
        }
    }

    //TODO
    public void updateUI(boolean result) {
    }

    public abstract void getProfileInformation();

    public void revoke() {
        Log.i(TAG, "Revoke triggered");

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //get the preferences editor
        SharedPreferences.Editor editor = pref.edit();
        // Remove the account data saved in prefs
        editor.remove(Constants.ACCOUNT_PARAM);
        editor.apply();

        ShiftEZ.getInstance().setAccount(null);

        // Revoke all granted permissions and clear the default account.  The user will have
        // to pass the consent screen to sign in again.
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                mGoogleApiClient.disconnect();
            } else {
                Log.e(TAG, "GPlus API Client is not connected during revoke.");
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

        ShiftEZ.getInstance().setAccount(null);

        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
            } else {
                Log.e(TAG, "GPlus API Client is not connected during logout.");
            }
        } else {
            Log.e(TAG, "GPlus API Client is null during logout.");
        }
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
