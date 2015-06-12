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
import com.schmidtdesigns.shiftez.models.ScheduleResponse;
import com.schmidtdesigns.shiftez.network.ScheduleRetrofitRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ScheduleFragment extends BaseFragment {

    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String TAG = "ScheduleFragment";
    private ViewPager mPager;
    private ScheduleAdapter mPagerAdapter;

    private String mCurrentPhotoPath;


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


        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            galleryAddPic();

            Log.i(TAG, "Captured image: " + mCurrentPhotoPath);

            mPager.setVisibility(View.INVISIBLE);

            Bitmap bitmap = BitmapFactory.decodeFile(new File (mCurrentPhotoPath).getAbsolutePath());

            ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView2);
            imageView.setImageBitmap(bitmap);
            //imageView.setAdjustViewBounds(true);

            imageView.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "Got bad req code: " + requestCode + " and bad resultCode: " + resultCode);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "shiftez");

        if (! storageDir.exists()) {
            if (! storageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory.");
                return null;
            }
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

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

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
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
            Log.d(TAG, result.toString());

            /**
            Schedule s = result.getSchedules().get(0);

            ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView);
            Picasso.with(getActivity()).load(s.getImage()).into(imageView);

            TextView textView = (TextView) getActivity().findViewById(R.id.textView);
            textView.setText("Year: " + s.getYear() + " Week: " + s.getWeek());
            **/

            mPagerAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), result.getSchedules());
            mPager.setAdapter(mPagerAdapter);

        }
    }

}
