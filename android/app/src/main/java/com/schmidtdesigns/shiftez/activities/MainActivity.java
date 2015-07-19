package com.schmidtdesigns.shiftez.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.fragments.SchedulePagerFragment;
import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.Store;
import com.schmidtdesigns.shiftez.network.AccountStoresRetrofitRequest;
import com.schmidtdesigns.shiftez.network.NewStoreRetrofitRequest;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends GPlusBaseActivity {

    private final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.failureImage)
    ImageView mFailureImage;
    @InjectView(R.id.fragment_container)
    FrameLayout mFragmentContainer;

    private Drawer mDrawer;
    private String mDepName;
    private String mStoreName;
    private int mDrawerPos = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        if (!isLoggedIn()) {
            Log.i(TAG, "USER IS NOT LOGGED IN");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        setupDrawer();

        // Get the refresh schedule boolean from the intent
        Boolean refreshStores = getIntent().getBooleanExtra(Constants.REFRESH_STORES, false);
        if (refreshStores) {
            refreshStores();
        } else {
            getStores();
        }
    }

    //TODO TEST
    public void refreshStores() {
        getSpiceManager().removeDataFromCache(Store.Response.class, Constants.STORE_KEY);
        getStores();
    }

    private void setupDrawer() {
        Account account = ShiftEZ.getInstance().getAccount();
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_wallpaper)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(account.getName())
                                .withEmail(account.getEmail())
                                .withIcon((account.getUserImageUrl() != null) ? account.getUserImageUrl() : "" )
                )
                .withSelectionListEnabled(false)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        return true;
                    }
                })
                .build();

        //Now create your drawer and pass the AccountHeader.Result
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof PrimaryDrawerItem) {
                            Store store = (Store) drawerItem.getTag();
                            displayView(SchedulePagerFragment.newInstance(store));
                        } else if (drawerItem instanceof SecondaryDrawerItem) {
                            if (getString(((SecondaryDrawerItem) drawerItem).getNameRes()).equals(getString(R.string.drawer_item_add_store))) {
                                showAddStoreDialog();
                            }
                        }
                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .build();

        updateDrawerStores();
    }

    /**
     * Get the stores with schedules from the server
     */
    public void getStores() {
        AccountStoresRetrofitRequest storeRequest =
                new AccountStoresRetrofitRequest(ShiftEZ.getInstance().getAccount().getEmail());

        getSpiceManager().getFromCacheAndLoadFromNetworkIfExpired(storeRequest,
                Constants.STORE_KEY,
                5 * DurationInMillis.ONE_MINUTE,
                new StoresRequestListener());
    }

    public void updateAccountStores(ArrayList<Store> stores) {
        ShiftEZ.getInstance().getAccount().setStores(stores);
        updateDrawerStores();
    }

    private void updateDrawerStores() {
        ArrayList<IDrawerItem> items = new ArrayList<>();
        items.add(new SectionDrawerItem().withName(R.string.drawer_header_stores));
        ArrayList<Store> stores = ShiftEZ.getInstance().getAccount().getStores();
        if (!stores.isEmpty()) {
            int i = 0;
            for (Store s : stores) {
                items.add(new PrimaryDrawerItem().withName(s.getStoreName()).withTag(s).withIdentifier(i).withIcon(R.drawable.ic_action_business));
                i++;
                if (s.getStoreName().equals(mStoreName) && s.getDepName().equals(mDepName)) {
                    mDrawerPos = i;
                }
            }
        } else {
            items.add(new PrimaryDrawerItem().withName("No Stores"));
        }
        items.add(new DividerDrawerItem());
        items.add(new SecondaryDrawerItem().withName(R.string.drawer_item_add_store).withIcon(R.drawable.ic_action_add_box));
        items.add(new DividerDrawerItem());
        items.add(new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(R.drawable.ic_action_settings));

        mDrawer.setItems(items);
        mDrawer.setSelection(mDrawerPos);
    }

    @Override
    public void updateUI(boolean result) {
        //TODO
        Log.i(TAG, "Update UI Needed.");
    }

    @Override
    public void getProfileInformation() {
        //TODO
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

        switch (id) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                Log.e(TAG, "Invalid menu action id received: " + id);
        }

        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void displayView(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_container, fragment);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            transaction.addToBackStack(null);
        }
        // Commit the transaction
        transaction.commit();
    }

    public void showAddStoreDialog() {
        final EditText input = new EditText(new ContextThemeWrapper(this, R.style.AppCompatAlertDialogStyle));

        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppCompatAlertDialogStyle))
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
        final EditText input = new EditText(new ContextThemeWrapper(this, R.style.AppCompatAlertDialogStyle));

        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog_AppCompat))
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

        mStoreName = storeName;
        mDepName = depName;

        Log.d(TAG, "Uploading new store with params: " + storeParams.toString());

        // Upload store and info
        NewStoreRetrofitRequest storeUploadRequest = new NewStoreRetrofitRequest(
                ShiftEZ.getInstance().getAccount().getEmail(), storeParams);
        getSpiceManager().execute(storeUploadRequest, Constants.UPLOAD_NEW_STORE,
                DurationInMillis.ONE_SECOND, new NewStoreUploadListener());
    }

    private class StoresRequestListener implements RequestListener<Store.Response> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());
            mProgress.setVisibility(View.GONE);
            mFailureImage.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRequestSuccess(Store.Response response) {
            Log.d(TAG, response.toString());
            updateAccountStores(response.getStores());

            mProgress.setVisibility(View.GONE);
            mFragmentContainer.setVisibility(View.VISIBLE);

            // TODO Default Store
            mDrawer.setSelection(mDrawerPos);
        }
    }

    private class NewStoreUploadListener implements RequestListener<PostResult> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());

            //TODO MOVE TO UTILS
            if (spiceException instanceof NoNetworkException) {
                Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onRequestSuccess(PostResult postResult) {
            Log.d(TAG, postResult.toString());
            // TODO HANDLE DIFFERENT POST RESULTS

            Toast.makeText(getApplicationContext(), "Store Added", Toast.LENGTH_SHORT).show();

            getStores();

            //TODO set drawer selection to new store.

        }
    }
}
