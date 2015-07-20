package com.schmidtdesigns.shiftez.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.Utils;
import com.schmidtdesigns.shiftez.activities.AddStoreActivity;
import com.schmidtdesigns.shiftez.activities.MainActivity;
import com.schmidtdesigns.shiftez.activities.UploadActivity;
import com.schmidtdesigns.shiftez.adapters.ScheduleAdapter;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.schmidtdesigns.shiftez.models.ShareStore;
import com.schmidtdesigns.shiftez.models.Store;
import com.schmidtdesigns.shiftez.network.JoinStoreRetrofitRequest;
import com.schmidtdesigns.shiftez.network.RemoveStoreRetrofitRequest;
import com.schmidtdesigns.shiftez.network.ShareStoreRetrofitRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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
    @InjectView(R.id.emptyText)
    TextView mEmptyText;

    // The image file we create and upload
    private File mImageFile;
    private String mStoreName;
    private String mDepName;
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
            mStoreName = getArguments().getString(STORE_PARAM);
            mDepName = getArguments().getString(DEP_PARAM);
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
            startActivity(new Intent(getActivity(), AddStoreActivity.class));
        } else {
            Store store = ShiftEZ.getInstance().getAccount().getStoreByStoreDep(mStoreName, mDepName);

            if (store != null) {
                ArrayList<Schedule> schedules = store.getSchedules();

                if (schedules.isEmpty()) {
                    mEmptyText.setVisibility(View.VISIBLE);
                } else {
                    mPager.setVisibility(View.VISIBLE);

                    ScheduleAdapter mPagerAdapter = new ScheduleAdapter(getActivity(), schedules);
                    mPager.setAdapter(mPagerAdapter);

                    mPager.setCurrentItem(mPagerAdapter.getCurrentWeekPosition(), true);
                }
            } else {
                Log.e(TAG, "Store not found in list of stores for account.");
            }
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
                ((MainActivity) getActivity()).refreshStores();
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
            case R.id.action_remove_store:
                removeStore();
                return true;
            default:
                break;
        }

        return false;
    }

    private void removeStore() {
        HashMap<String, String> storeParams = new HashMap<>();
        storeParams.put("store_name", mStoreName);
        storeParams.put("dep_name", mDepName);
        storeParams.put("store_user_id", mStoreUserId);

        RemoveStoreRetrofitRequest removeStoreRequest =
                new RemoveStoreRetrofitRequest(ShiftEZ.getInstance().getAccount().getEmail(),
                        storeParams);
        getSpiceManager().execute(removeStoreRequest,
                Constants.REMOVE_KEY_PARAM, DurationInMillis.ONE_SECOND,
                new RemoveStoreRequestListener());
    }

    private void shareStore() {
        HashMap<String, String> storeParams = new HashMap<>();
        storeParams.put("store_name", mStoreName);
        storeParams.put("dep_name", mDepName);
        storeParams.put("store_user_id", mStoreUserId);

        ShareStoreRetrofitRequest shareStoreRequest =
                new ShareStoreRetrofitRequest(ShiftEZ.getInstance().getAccount().getEmail(),
                        storeParams);
        getSpiceManager().execute(shareStoreRequest,
                Constants.SHARE_KEY_PARAM, DurationInMillis.ONE_SECOND,
                new ShareStoreRequestListener());
    }

    private void joinStore() {
        final EditText input = new EditText(new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));

        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle))
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

    @OnClick(R.id.fab)
    public void addSchedule() {
        Intent intent = new Intent(getActivity(), UploadActivity.class);

        // Setup Transition Animation
        String transitionName = getString(R.string.activity_upload_base_name);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        mFab,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning
                        // to
                );
        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }

    private class RemoveStoreRequestListener implements RequestListener<PostResult> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());
            Utils.spiceErrorCheck(spiceException, getActivity());
        }

        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.d(TAG, postResult.toString());
            Toast.makeText(getActivity(), "Store Removed", Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).refreshStores();
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
            Utils.spiceErrorCheck(spiceException, getActivity());
        }

        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.d(TAG, postResult.toString());
            Toast.makeText(getActivity(), "Store Joined", Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).refreshStores();
        }
    }
}
