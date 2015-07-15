package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.PostAccount;
import com.schmidtdesigns.shiftez.network.AddAccountRetrofitRequest;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Schedule Fragment used to get the schedules from the server, show them in a pager, and allow
 * the user to add a schedule by taking a picture and sending to {@link UploadActivity}.
 */
public class LoginActivity extends GPlusBaseActivity {

    // Logging tag
    private static final String TAG = "LoginActivity";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.sign_in_button)
    SignInButton mSignInButton;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

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

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
    }

    @OnClick(R.id.sign_in_button)
    public void loginWithGPlus() {
        updateUI(true);
        if (!login()) {
            updateUI(false);
        }
    }

    // TODO HANDLE FAILED LOGINS UPDATED UI
    public void updateUI(boolean result) {
        if (result) {
            mProgress.setVisibility(View.VISIBLE);
            mSignInButton.setEnabled(false);
        } else {
            mProgress.setVisibility(View.GONE);
            mSignInButton.setEnabled(true);
        }
    }

    public void getProfileInformation() {
        String email = Plus.AccountApi.getAccountName(getGoogleApiClient());
        Person person = Plus.PeopleApi.getCurrentPerson(getGoogleApiClient());

        // TODO PERSON CAN BE NULL??
        if (person != null) {
            HashMap<String, String> accountParams = new HashMap<>();
            accountParams.put("user_id", email);
            accountParams.put("user_name", person.getDisplayName());

            AddAccountRetrofitRequest accountRequest = new AddAccountRetrofitRequest(accountParams);
            getSpiceManager().execute(accountRequest,
                    Constants.ADD_ACCOUNT_PARAM,
                    5 * DurationInMillis.ONE_MINUTE,
                    new AddAccountRequestListener());

        } else {
            Log.e(TAG, "Person was null.");
            updateUI(false);
        }
    }

    private class AddAccountRequestListener implements RequestListener<PostAccount> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getLocalizedMessage());
        }

        @Override
        public void onRequestSuccess(PostAccount postAccount) {
            Log.i(TAG, "ACCOUNT: " + postAccount);
            saveAccount(postAccount.getAccount());
        }
    }

    public void saveAccount(Account account) {
        ShiftEZ.getInstance().setAccount(account);

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //get the preferences editor
        SharedPreferences.Editor editor = pref.edit();

        // save user info
        editor.putString(Constants.ACCOUNT_PARAM, Account.serializeToJson(ShiftEZ.getInstance().getAccount()));
        editor.apply();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
