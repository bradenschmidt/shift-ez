package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.models.Account;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Schedule Fragment used to get the schedules from the server, show them in a pager, and allow
 * the user to add a schedule by taking a picture and sending to {@link UploadActivity}.
 */
public class LoginActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Logging tag
    private static final String TAG = "LoginActivity";
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.login_name)
    TextView mLoginName;
    @InjectView(R.id.progress)
    ProgressBar mProgress;


    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    public LoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }



    }

    @OnClick(R.id.sign_in_button)
    public void loginWithGPlus() {
        login();
        mProgress.setVisibility(View.VISIBLE);
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
    public void onConnectionSuspended(int i) {
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
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

    public void login() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        Log.i(TAG, "***********In onConnected.");
        getProfileInformation();
        mProgress.setVisibility(View.GONE);
    }

    private void getProfileInformation() {
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        mLoginName.setText(person.getDisplayName());

        ShiftEZ.getInstance().setAccount(new Account(person.getName().toString(), email));

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //get the preferences editor
        SharedPreferences.Editor editor = pref.edit();

        // save user info
        editor.putString(Constants.ACCOUNT_PARAM, Account.serializeToJson(ShiftEZ.getInstance().getAccount()));
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

}
