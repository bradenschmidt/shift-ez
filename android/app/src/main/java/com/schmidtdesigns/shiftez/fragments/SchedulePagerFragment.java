package com.schmidtdesigns.shiftez.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.activities.UploadActivity;
import com.schmidtdesigns.shiftez.adapters.ScheduleAdapter;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;
import com.schmidtdesigns.shiftez.network.ScheduleRetrofitRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Schedule Fragment used to get the schedules from the server, show them in a pager, and allow
 * the user to add a schedule by taking a picture and sending to {@link UploadActivity}.
 */
public class SchedulePagerFragment extends BaseFragment {

    // Logging tag
    private static final String TAG = "SchedulePagerFragment";
    // The pager and adapter used to show the returned schedules
    @InjectView(R.id.schedule_pager)
    public ViewPager mPager;
    // Fab to add schedule
    @InjectView(R.id.fab)
    public View mFab;
    // Progress bar for loading schedules
    @InjectView(R.id.progress)
    public ProgressBar mProgress;
    // Failure image to show on schedule retrieval failure
    @InjectView(R.id.failureImage)
    public ImageView mFailureImageView;

    // The image file we create and upload
    private File mImageFile;


    public SchedulePagerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_schedules, container, false);

        ButterKnife.inject(this, rootView);

        /*
        * Request object for schedules
	    */
        int year = 2015;
        Boolean reverse = false;
        ScheduleRetrofitRequest scheduleRequest = new ScheduleRetrofitRequest(year, reverse);
        getSpiceManager().execute(scheduleRequest, "schedules", DurationInMillis.ONE_MINUTE,
                new ListScheduleRequestListener());

        return rootView;
    }

    /**
     * Create an image file for the camera to use during saving
     *
     * @return File - file to use
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA)
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "shiftez");

        // Make the shiftez dir if needed
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory.");
                return null;
            }
        }

        // Create the File to use, ensured to be unique
        mImageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return mImageFile;
    }

    /**
     * Get a file then launch the camera to capture the image with the filename.
     * Does error checking for cameras and camera activity.
     */
    @OnClick(R.id.fab)
    public void dispatchTakePictureIntent() {
        // Check Camera
        if (getActivity().getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, "Couldn't create file:");
                    Log.e(TAG, ex.toString());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, Constants.REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(getActivity(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * Check if result was a photo which was OK. If so then launch the media scanner, show image
         * on the screen, then upload the image to the server.
         */
        if (requestCode == Constants.REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            galleryAddPic();

            Log.i(TAG, "Captured image: " + mImageFile.getAbsolutePath());

            Intent intent = new Intent(getActivity(), UploadActivity.class);

            // Send file location
            intent.putExtra(Constants.IMAGE_PARAM, mImageFile.getAbsoluteFile().toString());

            // Setup Transition Animation
            String transitionName = getString(R.string.activity_upload_base_name);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                            mFab,   // The view which starts the transition
                            transitionName    // The transitionName of the view we’re transitioning
                            // to
                    );
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());


        } else {
            Log.e(TAG, "Got bad req code: " + requestCode + " and bad resultCode: " + resultCode);
        }
    }

    /**
     * Launch the media scanner to pick up the new image file.
     */
    //TODO TEST??
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(mImageFile);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


    /**
     * Called on completion of the api request to get the Schedules.
     * Deals with api call failure -
     * On Success - use the schedules.
     */
    public final class ListScheduleRequestListener implements RequestListener<ScheduleResponse> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO SHOW CAUSE
            Log.e(TAG, spiceException.getCause().toString());

            Toast.makeText(getActivity(), "Failed to Retrieve Schedules", Toast.LENGTH_SHORT)
                    .show();
            mProgress.setVisibility(View.GONE);
            mFailureImageView.setVisibility(View.VISIBLE);
        }

        /**
         * Display the schedules in the pager, hiding the progress bar and showing the fab
         *
         * @param result Schedules in the Datastore
         */
        @Override
        public void onRequestSuccess(final ScheduleResponse result) {
            //Log.d(TAG, result.toString());

            mProgress.setVisibility(View.GONE);
            mPager.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);

            ScheduleAdapter mPagerAdapter = new ScheduleAdapter(getActivity(),
                    result.getSchedules());
            mPager.setAdapter(mPagerAdapter);

            mPager.setCurrentItem(mPagerAdapter.getCurrentWeekPosition(), true);
        }
    }

}
