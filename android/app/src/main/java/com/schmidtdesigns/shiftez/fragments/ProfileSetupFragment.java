package com.schmidtdesigns.shiftez.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.Utils;
import com.schmidtdesigns.shiftez.activities.MainActivity;
import com.schmidtdesigns.shiftez.adapters.StoreAdapter;
import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.Store;
import com.schmidtdesigns.shiftez.models.StoreResponse;
import com.schmidtdesigns.shiftez.network.NewStoreRetrofitRequest;
import com.schmidtdesigns.shiftez.network.StoreRetrofitRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by braden on 15-06-29.
 */
public class ProfileSetupFragment extends BaseFragment {

    private static final String TAG = "ProfileSetupFragment";
    private static final String NAME_PARAM = "name";
    private static final String EMAIL_PARAM = "email";
    @InjectView(R.id.store_spinner)
    Spinner mStoreSpinner;
    @InjectView(R.id.dep_spinner)
    Spinner mDepSpinner;
    @InjectView(R.id.profile_setup_button)
    Button mProfileSetupButton;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    private String mName;
    private String mEmail;
    private ArrayList<Store> mStores;

    public ProfileSetupFragment() {
    }

    public static Fragment newInstance(String email, String displayName) {
        ProfileSetupFragment fragment = new ProfileSetupFragment();
        Bundle args = new Bundle();
        args.putString(EMAIL_PARAM, email);
        args.putString(NAME_PARAM, displayName);
        Log.d(TAG, "EMAIL AND NAME: " + email + displayName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(NAME_PARAM);
            mEmail = getArguments().getString(EMAIL_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_profile_setup, container, false);
        ButterKnife.inject(this, rootView);

        getStores();

        return rootView;
    }

    private void getStores() {
        // Get stores and info
        StoreRetrofitRequest storeRequest = new StoreRetrofitRequest(mEmail);
        getSpiceManager().execute(storeRequest, Constants.STORES, DurationInMillis.ONE_SECOND,
                new StoresListener());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Setup the week, year, and week offset spinners with the possible values.
     * @param stores
     */
    private void setupSpinners(ArrayList<Store> stores) {
        // Setup the stores to list
        // TODO GET STORES FROM SERVER

        stores.add(stores.size(), new Store(getResources().getText(R.string.new_store).toString(),
                new ArrayList<>(Collections.singletonList(
                        getResources().getText(R.string.new_dep).toString()))));

        StoreAdapter storeAdapter = new StoreAdapter(getActivity(),
                R.layout.spinner_item, R.layout.spinner_dropdown_item, stores);
        // Apply the adapter to the spinner
        mStoreSpinner.setAdapter(storeAdapter);
        // TODO Set default position to current store
        //mHolder.mDepSpinner.setSelection();
        mStoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Store store = (Store) mStoreSpinner.getSelectedItem();

                if (store.getStoreName().equals(getResources().getText(R.string.new_store).toString())) {
                    showAddStoreDialog();
                }

                mDepSpinner.setEnabled(true);
                mProfileSetupButton.setEnabled(true);


                List<String> deps = store.getDeps();
                if (!deps.get(deps.size() - 1).equals(getResources().getText(R.string.new_dep).toString())) {
                    deps.add(getResources().getText(R.string.new_dep).toString());

                }

                // Setup the Departments to use
                ArrayAdapter<String> depAdapter = new ArrayAdapter<>(getActivity(),
                        R.layout.spinner_item, deps);
                // Specify the layout to use when the list of choices appears
                depAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                // Apply the adapter to the spinner
                mDepSpinner.setAdapter(depAdapter);
                // TODO Set default position to current dep
                //mDepSpinner.setSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
            }

        });
        mDepSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mDepSpinner.getSelectedItem().equals(getResources().getText(R.string.new_dep).toString())) {
                    Store store = (Store) mStoreSpinner.getSelectedItem();

                    showAddDepDialog(store.getStoreName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick(R.id.profile_setup_button)
    public void setupProfile() {
        /*
        // TODO: GET FROM SERVER
        ArrayList<Store> stores = new ArrayList<>();
        ArrayList<String> deps = new ArrayList<>(Arrays.asList("Lumber", "Hardware"));
        ArrayList<String> deps2 = new ArrayList<>(Arrays.asList("Lumber", "Cashier"));
        Store store1 = new Store("8th St Coop Home Centre", deps);
        Store store2 = new Store("Rona", deps2);
        stores.add(store1);
        stores.add(store2);
        // END TODO
        */

        Store store = (Store) mStoreSpinner.getSelectedItem();
        List<String> deps = store.getDeps();

        String last = (String) Utils.getLastItem(deps);

        if (last != null) {
            if (last.equals(getResources().getText(R.string.new_dep).toString())) {
                deps.remove(deps.size() - 1);
            }
        }

        ArrayList<Store> stores = new ArrayList<>(Collections.singletonList(store));

        Account account = new Account(mName, mEmail, stores);
        Log.i(TAG, "Saving New Profile Info: " + account);

        ShiftEZ.getInstance().setAccount(account);

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //get the preferences editor
        SharedPreferences.Editor editor = pref.edit();

        // save user info
        editor.putString(Constants.ACCOUNT_PARAM, Account.serializeToJson(ShiftEZ.getInstance().getAccount()));
        editor.apply();

        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    public void updateUI(boolean result) {
        if (result) {
            mProgress.setVisibility(View.VISIBLE);
            mProfileSetupButton.setEnabled(false);
        } else {
            mProgress.setVisibility(View.GONE);
            mProfileSetupButton.setEnabled(true);
        }
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
        storeParams.put("store", storeName);
        storeParams.put("dep", depName);
        storeParams.put("user_id", mEmail);

        Log.d(TAG, "Uploading new store with params: " + storeParams.toString());

        // Upload store and info
        NewStoreRetrofitRequest storeUploadRequest = new NewStoreRetrofitRequest(storeParams);
        getSpiceManager().execute(storeUploadRequest, Constants.UPLOAD_NEW_STORE, DurationInMillis.ONE_SECOND, new NewStoreUploadListener());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private class NewStoreUploadListener implements RequestListener<PostResult> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());

            //TODO
            if (spiceException instanceof NoNetworkException) {
                Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.d(TAG, postResult.toString());
            // TODO HANDLE DIFFERENT POST RESULTS

            updateUI(false);
            getStores();
        }
    }

    private class StoresListener implements RequestListener<StoreResponse> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());

            if (spiceException instanceof NoNetworkException) {
                Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onRequestSuccess(StoreResponse storeResponse) {
            Log.d(TAG, storeResponse.toString());
            // TODO HANDLE DIFFERENT store responses
            mStores = storeResponse.getStores();
            setupSpinners(mStores);

        }
    }
}
