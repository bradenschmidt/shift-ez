package com.schmidtdesigns.shiftez.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.SignInButton;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.activities.LoginActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by braden on 15-06-29.
 */
public class LoginFragment extends BaseFragment {

    private static final String TAG = "LoginFragment";
    @InjectView(R.id.sign_in_button)
    SignInButton mSignInButton;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    LoginActivity mActivity;

    public LoginFragment() {
    }

    public static Fragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_login, container, false);
        ButterKnife.inject(this, rootView);


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (LoginActivity) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
    }


    @OnClick(R.id.sign_in_button)
    public void loginWithGPlus() {
        updateUI(true);
        if (!mActivity.login()) {
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
