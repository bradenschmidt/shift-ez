package com.schmidtdesigns.shiftez.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.activities.UploadActivity;
import com.schmidtdesigns.shiftez.adapters.ScheduleAdapter;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.schmidtdesigns.shiftez.models.ShareStore;
import com.schmidtdesigns.shiftez.models.Store;
import com.schmidtdesigns.shiftez.network.JoinStoreRetrofitRequest;
import com.schmidtdesigns.shiftez.network.NewStoreRetrofitRequest;
import com.schmidtdesigns.shiftez.network.ScheduleRetrofitRequest;
import com.schmidtdesigns.shiftez.network.ShareStoreRetrofitRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Future;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Schedule Fragment used to get the schedules from the server, show them in a pager, and allow
 * the user to add a schedule by taking a picture and sending to {@link UploadActivity}.
 */
public class SchedulePagerFragment extends BaseFragment {

    // Logging tag
    private static final String TAG = BaseFragment.class.getSimpleName();
    private static final String STORE_PARAM = "store";
    private static final String DEP_PARAM = "dep";
    private static final String USER_ID_PARAM = "store_user_id";
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
    @InjectView(R.id.emptyText)
    TextView mEmptyText;

    // The image file we create and upload
    private File mImageFile;
    private String mStore;
    private String mDep;
    private String mStoreUserId;


    public SchedulePagerFragment() {
    }

    public static Fragment newInstance(Store store) {
        SchedulePagerFragment fragment = new SchedulePagerFragment();
        if (store != null) {
            Bundle args = new Bundle();
            args.putString(STORE_PARAM, store.getStoreName());
            args.putString(DEP_PARAM, store.getDepName());
            args.putString(USER_ID_PARAM, store.getUserId());
            Log.d(TAG, "STORE: " + store);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStore = getArguments().getString(STORE_PARAM);
            mDep = getArguments().getString(DEP_PARAM);
            mStoreUserId = getArguments().getString(USER_ID_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_schedules, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this, rootView);

        if(ShiftEZ.getInstance().getAccount().getStores().isEmpty()) {
            showAddStoreDialog();
        } else {
            getSchedules();
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.schedule_pager_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // TODO Not working
                refreshSchedules();
                return true;
            case R.id.action_settings:
                // TODO REMOVE
                return false;
            case R.id.action_share_store:
                shareStore();
                return true;
            case R.id.action_join_store:
                joinStore();
                return true;
            default:
                break;
        }

        return false;
    }

    public void refreshSchedules() {
        Future<?> s = getSpiceManager().removeDataFromCache(Schedule.class, Constants.SCHEDULE_KEY_PARAM);
        if (s.isDone()) {
            getSchedules();
        }
    }

    private void shareStore() {
        HashMap<String, String> storeParams = new HashMap<>();
        storeParams.put("store_name", mStore);
        storeParams.put("dep_name", mDep);
        storeParams.put("store_user_id", mStoreUserId);

        ShareStoreRetrofitRequest shareStoreRequest =
                new ShareStoreRetrofitRequest(ShiftEZ.getInstance().getAccount().getEmail(),
                        storeParams);
        getSpiceManager().execute(shareStoreRequest,
                Constants.SHARE_KEY_PARAM, DurationInMillis.ONE_SECOND,
                new ShareStoreRequestListener());
    }


    private void joinStore() {
        final EditText input = new EditText(getActivity());

        new AlertDialog.Builder(getActivity())
                .setTitle("Join Store")
                .setMessage("Enter Shared Store Key:")
                .setView(input)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable key = input.getText();
                        JoinStoreRetrofitRequest joinStoreRequest =
                                new JoinStoreRetrofitRequest(ShiftEZ.getInstance().getAccount().getEmail(),
                                        key.toString());
                        getSpiceManager().execute(joinStoreRequest,
                                Constants.JOIN_KEY_PARAM, DurationInMillis.ONE_SECOND,
                                new JoinStoreRequestListener());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // DO NOTHING
            }
        }).show();
    }

    /**
     * Get the schedules from the server
     */
    private void getSchedules() {
        ScheduleRetrofitRequest scheduleRequest =
                new ScheduleRetrofitRequest(ShiftEZ.getInstance().getAccount().getEmail(), false);
        getSpiceManager().execute(scheduleRequest,
                Constants.SCHEDULE_KEY_PARAM, 5 * DurationInMillis.ONE_SECOND,
                new ListScheduleRequestListener());

        // TODO ENSURE THIS IS INVALIDATED ON UPLOADS OR NEW CONTENT
        // USE CACHED IF NO NETWORK
        // https://groups.google.com/forum/#!topic/robospice/C1bZGKQeLLc
        //getFromCacheAndLoadFromNetworkIfExpired
    }


    public ArrayList<Schedule> getScheduleByStoreDep(ArrayList<Schedule> schedules,
                                                     String storeName, String depName) {
        ArrayList<Schedule> stores = new ArrayList<>();
        for (Schedule s : schedules) {
            if (s.getStoreName().equals(storeName) && s.getDepName().equals(depName)) {
                stores.add(s);
            }
        }
        return stores;
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
        //TODO IMAGE PICKER?

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

    private void showAddStoreDialog() {
        final EditText input = new EditText(getActivity());

        new AlertDialog.Builder(getActivity())
                .setTitle("Add Store")
                .setMessage("Enter New Store Name:")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable storeName = input.getText();
                        showAddDepDialog(storeName.toString());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // DO NOTHING
            }
        }).show();
    }

    private void showAddDepDialog(final String storeName) {
        final EditText input = new EditText(getActivity());

        new AlertDialog.Builder(getActivity())
                .setTitle("Add Department")
                .setMessage("Enter New Department Name:")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable depName = input.getText();
                        uploadNewStore(storeName, depName.toString());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // DO NOTHING
            }
        }).show();
    }

    private void uploadNewStore(String storeName, String depName) {
        HashMap<String, String> storeParams = new HashMap<>();
        storeParams.put("store_name", storeName);
        storeParams.put("dep_name", depName);
        storeParams.put("user_id", ShiftEZ.getInstance().getAccount().getEmail());

        Log.d(TAG, "Uploading new store with params: " + storeParams.toString());

        // Upload store and info
        NewStoreRetrofitRequest storeUploadRequest = new NewStoreRetrofitRequest(storeParams);
        getSpiceManager().execute(storeUploadRequest, Constants.UPLOAD_NEW_STORE,
                DurationInMillis.ONE_SECOND, new NewStoreUploadListener());
    }

    /**
     * Called on completion of the api request to get the Schedules.
     * Deals with api call failure -
     * On Success - use the schedules.
     */
    public final class ListScheduleRequestListener implements RequestListener<Schedule.Response> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO SHOW CAUSE
            // TODO Null exception
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
        public void onRequestSuccess(final Schedule.Response result) {
            Log.d(TAG, result.toString());

            mProgress.setVisibility(View.GONE);
            mFab.setVisibility(View.VISIBLE);

            ArrayList<Schedule> schedules = getScheduleByStoreDep(result.getSchedules(), mStore, mDep);

            if (schedules.isEmpty()) {
                mEmptyText.setVisibility(View.VISIBLE);
            } else {
                mPager.setVisibility(View.VISIBLE);

                ScheduleAdapter mPagerAdapter = new ScheduleAdapter(getActivity(), schedules);
                mPager.setAdapter(mPagerAdapter);

                mPager.setCurrentItem(mPagerAdapter.getCurrentWeekPosition(), true);
            }
        }
    }

    private class NewStoreUploadListener implements RequestListener<PostResult> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());

            //TODO MOVE TO UTILS
            if (spiceException instanceof NoNetworkException) {
                Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.d(TAG, postResult.toString());
            // TODO HANDLE DIFFERENT POST RESULTS

            getSchedules();
        }
    }

    private class ShareStoreRequestListener implements RequestListener<ShareStore> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO
            Log.e(TAG, spiceException.getMessage());
        }

        @Override
        public void onRequestSuccess(ShareStore shareStore) {
            Log.i(TAG, shareStore.toString());
            Toast.makeText(getActivity(), shareStore.getDesc(), Toast.LENGTH_SHORT).show();

            String key = shareStore.getKey();

            if (key != null) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Shared Store Key");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, key);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
            }
        }
    }

    private class JoinStoreRequestListener implements RequestListener<PostResult> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());
        }

        @Override
        public void onRequestSuccess(PostResult postResult) {

        }
    }
}
