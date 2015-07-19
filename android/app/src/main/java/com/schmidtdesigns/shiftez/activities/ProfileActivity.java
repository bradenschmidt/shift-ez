package com.schmidtdesigns.shiftez.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ProfileActivity extends GPlusBaseActivity {

    private final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.profile_toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.profileImage)
    ImageView mProfileImage;
    @InjectView(R.id.profile_name)
    TextView mProfileName;
    @InjectView(R.id.profileEmail)
    TextView mProfileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.inject(this);

        setupToolbar(mToolbar);

        Picasso.with(mProfileImage.getContext()).load(ShiftEZ.getInstance().getAccount().getUserImageUrl()).into(mProfileImage);
        mProfileEmail.setText(ShiftEZ.getInstance().getAccount().getEmail());
        mProfileName.setText(ShiftEZ.getInstance().getAccount().getName());
    }

    @Override
    public void getProfileInformation() {
        // TODO
    }

    private void setupToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        } else {
            Log.e(TAG, "Toolbar is null");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_profile_revoke)
    public void revoke() {
        super.revoke();
    }

    @OnClick(R.id.button_profile_sign_out)
    public void logout() {
        super.logout();
    }

}
