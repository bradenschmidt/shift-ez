package com.schmidtdesigns.shiftez.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.adapters.ScheduleAdapter;
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;
import com.schmidtdesigns.shiftez.network.ImageUploadRetrofitRequest;
import com.schmidtdesigns.shiftez.network.ImageUploadUrlRetrofitRequest;
import com.schmidtdesigns.shiftez.network.ScheduleRetrofitRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import retrofit.mime.TypedFile;

/**
 * Schedule Fragment used to get the schedules from the server, show them in a pager, and take and
 * upload a picture.
 */
public class ScheduleFragment extends BaseFragment {

    // The activity code used when launching camera intent
    static final int REQUEST_TAKE_PHOTO = 1;
    // Logging tag
    private static final String TAG = "ScheduleFragment";
    // The pager and adapter used to show the returned schedules
    private ViewPager mPager;
    private ScheduleAdapter mPagerAdapter;

    // The image file we create and upload
    private File mImageFile;


    public ScheduleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_schedule, container, false);

        /*
        * Request object for schedules
	    */
        int year = 2015;
        Boolean reverse = false;
        ScheduleRetrofitRequest scheduleRequest = new ScheduleRetrofitRequest(year, reverse);
        getSpiceManager().execute(scheduleRequest, "schedules", DurationInMillis.ONE_MINUTE, new ListScheduleRequestListener());

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) rootView.findViewById(R.id.schedulePager);

        View v = rootView.findViewById(R.id.fab);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * Check if result was a photo which was OK. If so then launch the media scanner, show image
         * on the screen, then upload the image to the server.
         */
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            galleryAddPic();

            Log.i(TAG, "Captured image: " + mImageFile.getAbsolutePath());

            mPager.setVisibility(View.INVISIBLE);

            Bitmap bitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());

            ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView2);
            imageView.setImageBitmap(bitmap);
            //imageView.setAdjustViewBounds(true);

            imageView.setVisibility(View.VISIBLE);

            uploadImage();
        } else {
            Log.e(TAG, "Got bad req code: " + requestCode + " and bad resultCode: " + resultCode);
        }
    }

    /**
     * Create an image file for the camera to use during saving
     *
     * @return File - file to use
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "shiftez");

        // Make the shiftez dir if needed
        if (! storageDir.exists()) {
            if (! storageDir.mkdirs()) {
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
    private void dispatchTakePictureIntent() {
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(getActivity(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Launch the media scanner to pick up the new image file.
     */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(mImageFile);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
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
     * Called on completion of the api request to get the Schedules.
     * Deals with api call failure -
     * On Success - use the schedules.
     */
    public final class ListScheduleRequestListener implements RequestListener<ScheduleResponse> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO
            Toast.makeText(getActivity(), "Schedule Request Failure", Toast.LENGTH_SHORT).show();
            Log.d(TAG, spiceException.getCause().toString());

            //TextView mainText = (TextView) findViewById(R.id.mainText);
            //mainText.setText("Failed");
        }

        /**
         * Use the schedules
         *
         * @param result - Schedules in the Datastore
         */
        @Override
        public void onRequestSuccess(final ScheduleResponse result) {
            Toast.makeText(getActivity(), "Schedule Request Success", Toast.LENGTH_SHORT).show();
            //Log.d(TAG, result.toString());

            mPagerAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), result.getSchedules());
            mPager.setAdapter(mPagerAdapter);

        }
    }

    /**
     * Callback for the getting the image upload url. On success create the params of the schedule
     * image and launch robospice post to server.
     *
     */
    private class ImageUploadUrlListener implements RequestListener<ImageUploadUrl> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO
            Log.e(TAG, spiceException.getCause().toString());
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
            imageParams.put("week", "14");
            imageParams.put("year", "2014");


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
        }

        /**
         * Use the returned message to show result.
         * @param postResult
         */
        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.i(TAG, "Image upload successful: " + postResult.toString());
            Toast.makeText(getActivity(), "Image upload successful:" + postResult.toString(), Toast.LENGTH_LONG).show();

        }
    }
}
