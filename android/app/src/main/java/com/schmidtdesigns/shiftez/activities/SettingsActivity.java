package com.schmidtdesigns.shiftez.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.schmidtdesigns.shiftez.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @InjectView(R.id.settings_toolbar)
    Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        setupToolbar(mToolbar);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(R.id.frame_container,
                new PrefsFragment()).commit();
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

    public static class PrefsFragment extends PreferenceFragment {
        public final String TAG = this.getClass().getSimpleName();

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            Preference aboutPref = findPreference("aboutPref");
            aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference.getKey().equals("aboutPref")) {
                        promptAbout();
                    }
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            // getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Set up a listener whenever a key changes
            // getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        private void promptAbout() {
            new AlertDialog.Builder(getActivity())
                    .setTitle("About")
                    .setMessage("Shift EZ is being actively developed by Braden Schmidt."
                            + "Any questions, comments or issues can be directed to bradenschmidt+shiftez@gmail.com."
                            + "Visit the web app at http://shift-ez.appspot.com to access schedules online.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(R.drawable.ic_action_info)
                    .show();

        }
    }


}
