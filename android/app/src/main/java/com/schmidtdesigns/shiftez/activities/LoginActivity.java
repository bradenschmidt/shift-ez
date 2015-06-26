package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.SignInButton;
import com.schmidtdesigns.shiftez.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Schedule Fragment used to get the schedules from the server, show them in a pager, and allow
 * the user to add a schedule by taking a picture and sending to {@link UploadActivity}.
 */
public class LoginActivity extends BaseActivity {

    // Logging tag
    private static final String TAG = "LoginActivity";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.sign_in_button)
    SignInButton mSignInButton;

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
        mProgress.setVisibility(View.VISIBLE);
        mSignInButton.setEnabled(false);
        if(!login()) {
            mProgress.setVisibility(View.GONE);
            mSignInButton.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
    }
}
