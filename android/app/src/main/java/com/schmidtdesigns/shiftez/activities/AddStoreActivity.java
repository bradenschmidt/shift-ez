package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.Utils;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.network.NewStoreRetrofitRequest;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AddStoreActivity extends BaseActivity {
    // Log tag
    private final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.store_name)
    EditText mStoreName;
    @InjectView(R.id.dep_name)
    EditText mDepName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_store);

        ButterKnife.inject(this);

        setupToolbar();
    }

    /**
     * Setup the toolbar for this activity
     */
    private void setupToolbar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
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

    @OnClick(R.id.add_store_button)
    public void uploadNewStore() {
        String storeName = mStoreName.getText().toString();
        String depName = mDepName.getText().toString();

        if (storeName.equals("") || depName.equals("")) {
            Toast.makeText(this, "Store or Department Name cannot be empty.", Toast.LENGTH_LONG).show();
        } else {
            HashMap<String, String> storeParams = new HashMap<>();
            storeParams.put("store_name", storeName);
            storeParams.put("dep_name", depName);

            Log.d(TAG, "Uploading new store with params: " + storeParams.toString());

            // Upload store and info
            NewStoreRetrofitRequest storeUploadRequest = new NewStoreRetrofitRequest(
                    ShiftEZ.getInstance().getAccount().getEmail(), storeParams);
            getSpiceManager().execute(storeUploadRequest, Constants.UPLOAD_NEW_STORE,
                    DurationInMillis.ONE_SECOND, new NewStoreUploadListener());
        }
    }

    private class NewStoreUploadListener implements RequestListener<PostResult> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());

            Utils.spiceErrorCheck(spiceException, getApplicationContext());
            finish();
        }

        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.d(TAG, postResult.toString());
            // TODO HANDLE DIFFERENT POST RESULTS

            Toast.makeText(getApplicationContext(), "Store Added", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        }
    }

}
