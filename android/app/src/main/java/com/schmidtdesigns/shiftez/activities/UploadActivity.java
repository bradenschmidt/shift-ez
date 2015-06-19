package com.schmidtdesigns.shiftez.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.network.ImageUploadRetrofitRequest;
import com.schmidtdesigns.shiftez.network.ImageUploadUrlRetrofitRequest;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.mime.TypedFile;

public class UploadActivity extends BaseActivity {

    private static final String TAG = "UploadActivity";
    @InjectView(R.id.week_spinner)
    public Spinner mWeekSpinner;
    @InjectView(R.id.year_spinner)
    public Spinner mYearSpinner;
    @InjectView(R.id.week_offset_spinner)
    public Spinner mWeekOffsetSpinner;
    // The image file we want to upload
    private File mImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        setupToolbar(toolbar);

        String imageFile = getIntent().getStringExtra(Constants.IMAGE_PARAM);
        // Get the schedule from the intent
        if (imageFile == null) {
            Log.e(TAG, "Image File from intent is null");
            finish();
        } else {
            mImageFile = new File(imageFile);
        }

        setupSpinners();

        ImageView image =
                (ImageView) findViewById(R.id.schedule_image_preview);
        setupScheduleImage(image);

        Button uploadButton = (Button) findViewById(R.id.schedule_upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

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

    private void setupSpinners() {
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
        mWeekSpinner.setAdapter(weekAdapter);


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
        mWeekOffsetSpinner.setAdapter(weekOffsetAdapter);


        // Create an ArrayAdapter using a list containing the possible years
        //  (last year, current, and next
        DateTime time = new DateTime();
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
        mYearSpinner.setAdapter(yearAdapter);
    }

    private void setupScheduleImage(final ImageView image) {
        Picasso.with(getApplicationContext())
                .load(mImageFile)
                .into(image);
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
    public void uploadImage() {
        // Get an image upload url
        ImageUploadUrlRetrofitRequest imageUploadUrlRequest = new ImageUploadUrlRetrofitRequest();
        getSpiceManager().execute(imageUploadUrlRequest, "imageuploadurl", DurationInMillis.ONE_SECOND, new ImageUploadUrlListener());

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
         * @param imageUploadUrl
         */
        @Override
        public void onRequestSuccess(ImageUploadUrl imageUploadUrl) {
            Log.i(TAG, "Got an upload url: " + imageUploadUrl.getUploadUrl());
            Log.i(TAG, "Path is: " + imageUploadUrl.getPath());


            TypedFile image = new TypedFile("image/*", mImageFile);
            HashMap<String, String> imageParams = new HashMap<>();
            imageParams.put("store", "8th St Coop Home Centre");
            imageParams.put("dep", "Lumber");
            imageParams.put("user_id", "bps");
            imageParams.put("user_name", "Braden");
            imageParams.put("week", String.valueOf(mWeekSpinner.getSelectedItem()));
            imageParams.put("week_offset", String.valueOf(mWeekOffsetSpinner.getSelectedItem()));
            imageParams.put("year", String.valueOf(mYearSpinner.getSelectedItem()));

            Log.d(TAG, "Uploading image with params: " + imageParams.toString());

            // Upload image and info
            ImageUploadRetrofitRequest imageUploadRequest = new ImageUploadRetrofitRequest(imageUploadUrl, image, imageParams);
            getSpiceManager().execute(imageUploadRequest, "imageupload", DurationInMillis.ONE_SECOND, new ImageUploadListener());

        }
    }

    /**
     * Callback for image upload by robospice POST. On success show a popup message.
     */
    private class ImageUploadListener implements RequestListener<com.schmidtdesigns.shiftez.models.PostResult> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO
            Log.e(TAG, spiceException.getCause().toString());
            Toast.makeText(getApplicationContext(), "Image upload successful:" + spiceException.getCause().toString(), Toast.LENGTH_LONG).show();

        }

        /**
         * Use the returned message to show result.
         *
         * @param postResult
         */
        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.i(TAG, "Image upload successful: " + postResult.toString());
            Toast.makeText(getApplicationContext(), "Image upload successful:" + postResult.toString(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
