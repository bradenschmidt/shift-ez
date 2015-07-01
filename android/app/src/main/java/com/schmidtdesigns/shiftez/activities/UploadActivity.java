package com.schmidtdesigns.shiftez.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.adapters.StoreAdapter;
import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.Store;
import com.schmidtdesigns.shiftez.network.ImageUploadRetrofitRequest;
import com.schmidtdesigns.shiftez.network.ImageUploadUrlRetrofitRequest;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.mime.TypedFile;

public class UploadActivity extends BaseActivity {
    // Log tag
    private static final String TAG = "UploadActivity";

    // The image file we want to upload
    private File mImageFile;

    // View Holder containing all of this activities views.
    private ViewHolder mHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ButterKnife.inject(this);

        mHolder = new ViewHolder(this);

        setupToolbar();

        String imageFile = getIntent().getStringExtra(Constants.IMAGE_PARAM);
        // Get the schedule from the intent
        if (imageFile == null) {
            Log.e(TAG, "Image File from intent is null");
            finish();
        } else {
            mImageFile = new File(imageFile);
        }

        setupSpinners();

        setupScheduleImage();
    }

    /**
     * Setup the toolbar for this activity
     */
    private void setupToolbar() {
        if (mHolder.mToolbar != null) {
            setSupportActionBar(mHolder.mToolbar);
        } else {
            Log.e(TAG, "Toolbar is null");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Setup the week, year, and week offset spinners with the possible values.
     */
    private void setupSpinners() {
        Account account = ShiftEZ.getInstance().getAccount();

        // Setup the stores to list
        StoreAdapter storeAdapter = new StoreAdapter(getApplicationContext(),
                R.layout.spinner_item, R.layout.spinner_dropdown_item, account.getStores());
        // Apply the adapter to the spinner
        mHolder.mStoreSpinner.setAdapter(storeAdapter);
        // TODO Set default position to current store
        //mHolder.mDepSpinner.setSelection();
        mHolder.mStoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mHolder.mDepSpinner.setEnabled(true);
                mHolder.mUploadButton.setEnabled(true);

                Store store = (Store) mHolder.mStoreSpinner.getSelectedItem();

                // Setup the Departments to use
                ArrayAdapter<String> depAdapter = new ArrayAdapter<>(getApplicationContext(),
                        R.layout.spinner_item, store.getDeps());
                // Specify the layout to use when the list of choices appears
                depAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                // Apply the adapter to the spinner
                mHolder.mDepSpinner.setAdapter(depAdapter);
                // TODO Set default position to current dep
                //mHolder.mDepSpinner.setSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
            }

        });

        // Create an ArrayAdapter using a list containing the possible weeks
        ArrayList<Integer> weeks = new ArrayList<>();
        for (int i = 1; i <= 53; i++) {
            weeks.add(i);
        }

        ArrayAdapter<Integer> weekAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_item, weeks);
        // Specify the layout to use when the list of choices appears
        weekAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the spinner
        mHolder.mWeekSpinner.setAdapter(weekAdapter);
        // Set default position to current week
        DateTime time = new DateTime();
        mHolder.mWeekSpinner.setSelection(weeks.indexOf(time.getWeekOfWeekyear()));


        // Create an ArrayAdapter using a list containing the possible week offsets
        ArrayList<Integer> weekOffsets = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            weekOffsets.add(i);
        }

        ArrayAdapter<Integer> weekOffsetAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_item, weekOffsets);
        // Specify the layout to use when the list of choices appears
        weekOffsetAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the spinner
        mHolder.mWeekOffsetSpinner.setAdapter(weekOffsetAdapter);


        // Create an ArrayAdapter using a list containing the possible years
        //  (last year, current, and next
        int currYear = time.getYear();
        time = time.minusYears(1);
        int lastYear = time.getYear();
        time = time.plusYears(2);
        int nextYear = time.getYear();

        ArrayList<Integer> years = new ArrayList<>();
        years.add(lastYear);
        years.add(currYear);
        years.add(nextYear);

        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_item, years);
        // Specify the layout to use when the list of choices appears
        yearAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the spinner
        mHolder.mYearSpinner.setAdapter(yearAdapter);
        // Set default to current year
        mHolder.mYearSpinner.setSelection(1, true);
    }

    /**
     * Setup the schedule image preview to contain the image file location passed by intent
     */
    private void setupScheduleImage() {
        Picasso.with(getApplicationContext())
                .load(mImageFile)
                .into(mHolder.mScheduleImagePreview);
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

    /**
     * Launch request to Upload the image, requests an upload link from the server, then uploads the
     * image found at mImageFile to the server with the given schedule info. Uses two linked
     * listeners.
     */
    @OnClick(R.id.schedule_upload_button)
    public void uploadImage() {
        // Get an image upload url
        ImageUploadUrlRetrofitRequest imageUploadUrlRequest = new ImageUploadUrlRetrofitRequest();
        getSpiceManager().execute(imageUploadUrlRequest, "imageuploadurl", DurationInMillis.ONE_SECOND, new ImageUploadUrlListener());

    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'activity_upload.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        // Possible weeks
        @InjectView(R.id.week_spinner)
        public Spinner mWeekSpinner;
        // Possible years
        @InjectView(R.id.year_spinner)
        public Spinner mYearSpinner;
        // Possible offsets
        @InjectView(R.id.week_offset_spinner)
        public Spinner mWeekOffsetSpinner;
        // toolbar used by this activity
        @InjectView(R.id.upload_toolbar)
        public Toolbar mToolbar;
        @InjectView(R.id.schedule_upload_button)
        public Button mUploadButton;
        @InjectView(R.id.store_spinner)
        Spinner mStoreSpinner;
        @InjectView(R.id.dep_spinner)
        Spinner mDepSpinner;
        // Image preview shown
        @InjectView(R.id.schedule_image_preview)
        ImageView mScheduleImagePreview;

        ViewHolder(UploadActivity uploadActivity) {
            ButterKnife.inject(this, uploadActivity);
            // Start as disabled
            mDepSpinner.setEnabled(false);
        }
    }

    /**
     * Callback for the getting the image upload url. On success create the params of the schedule
     * image and launch robospice post to server.
     */
    private class ImageUploadUrlListener implements RequestListener<ImageUploadUrl> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO
            Log.e(TAG, spiceException.getCause().toString());
            Toast.makeText(getApplicationContext(), "Image upload successful:" + spiceException.getCause().toString(), Toast.LENGTH_LONG).show();

        }

        /**
         * Use the imageUploadUrl as a POST location for the image.
         *
         * @param imageUploadUrl ImageUploadUrl to send the image to
         */
        @Override
        public void onRequestSuccess(ImageUploadUrl imageUploadUrl) {
            Log.i(TAG, "Got an upload url: " + imageUploadUrl.getUploadUrl());
            Log.i(TAG, "Image Path is: " + imageUploadUrl.getPath());

            // Collect params from Account and from spinners.
            TypedFile image = new TypedFile("image/*", mImageFile);
            HashMap<String, String> imageParams = new HashMap<>();
            imageParams.put("store", ((Store) mHolder.mStoreSpinner.getSelectedItem()).getStoreName());
            imageParams.put("dep", String.valueOf(mHolder.mDepSpinner.getSelectedItem()));
            imageParams.put("user_id", ShiftEZ.getInstance().getAccount().getEmail());
            imageParams.put("user_name", ShiftEZ.getInstance().getAccount().getName());
            imageParams.put("week", String.valueOf(mHolder.mWeekSpinner.getSelectedItem()));
            imageParams.put("week_offset", String.valueOf(mHolder.mWeekOffsetSpinner.getSelectedItem()));
            imageParams.put("year", String.valueOf(mHolder.mYearSpinner.getSelectedItem()));

            Log.d(TAG, "Uploading image with params: " + imageParams.toString());

            // Upload image and info
            ImageUploadRetrofitRequest imageUploadRequest = new ImageUploadRetrofitRequest(imageUploadUrl, image, imageParams);
            getSpiceManager().execute(imageUploadRequest, "imageupload", DurationInMillis.ONE_SECOND, new ImageUploadListener());

        }
    }

    /**
     * Callback for image upload by robospice POST. On success show a popup message.
     */
    private class ImageUploadListener implements RequestListener<PostResult> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO
            Log.e(TAG, spiceException.getCause().toString());
            Toast.makeText(getApplicationContext(), "Image upload Failed:" + spiceException.getCause().toString(), Toast.LENGTH_LONG).show();

        }

        /**
         * Use the returned message to show result.
         *
         * @param postResult PostResponse containing information from the response
         */
        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.i(TAG, "Image upload successful: " + postResult.toString());
            Toast.makeText(getApplicationContext(), postResult.niceToString(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
