package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.fragments.LoginFragment;
import com.schmidtdesigns.shiftez.fragments.ProfileSetupFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Schedule Fragment used to get the schedules from the server, show them in a pager, and allow
 * the user to add a schedule by taking a picture and sending to {@link UploadActivity}.
 */
public class LoginActivity extends GPlusBaseActivity {

    // Logging tag
    private static final String TAG = "LoginActivity";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

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

        displayView(LoginFragment.newInstance());
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
    }

    public void getProfileInformation() {
        String email = Plus.AccountApi.getAccountName(getGoogleApiClient());
        Person person = Plus.PeopleApi.getCurrentPerson(getGoogleApiClient());

        // TODO PERSON CAN BE NULL??
        if (person != null) {
            displayView(ProfileSetupFragment.newInstance(email, person.getDisplayName()));
        } else {
            Log.e(TAG, "Person was null.");
            //updateUI(true);
            loginFailedUI();
        }
    }

    private void loginFailedUI() {
        LoginFragment fragment = (LoginFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment_container);
        fragment.updateUI(false);
    }

    public void displayView(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

}
