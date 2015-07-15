package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

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
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.fragments.SchedulePagerFragment;
import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.Store;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends GPlusBaseActivity {

    private final String TAG = this.getClass().getSimpleName();
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    private Drawer mDrawer;

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
    }

    private void setupDrawer() {
        Account account = ShiftEZ.getInstance().getAccount();
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                        //.withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(account.getName())
                                .withEmail(account.getEmail())
                        //.withIcon(getResources().getDrawable(R.drawable.profile))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
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
                        }
                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .build();

        updateDrawerStores();

        //TODO Set selection based on default store
        mDrawer.setSelection(1);
    }

    private void updateDrawerStores() {
        ArrayList<IDrawerItem> items = new ArrayList<>();
        items.add(new SectionDrawerItem().withName(R.string.drawer_header_stores));
        ArrayList<Store> stores = ShiftEZ.getInstance().getAccount().getStores();
        if (!stores.isEmpty()) {
            for (Store s : stores) {
                items.add(new PrimaryDrawerItem().withName(s.getStoreName()).withTag(s));
            }
        } else {
            items.add(new PrimaryDrawerItem().withName("No Stores"));
        }
        items.add(new DividerDrawerItem());
        items.add(new SecondaryDrawerItem().withName(R.string.drawer_item_settings));

        mDrawer.setItems(items);

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
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_revoke:
                revoke();
                return true;
            default:
                Log.e(TAG, "Invalid menu action id received: " + id);
        }

        return super.onOptionsItemSelected(item);
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

    public void updateStores(ArrayList<Store> stores) {
        ShiftEZ.getInstance().getAccount().setStores(stores);
        updateDrawerStores();
    }

}
