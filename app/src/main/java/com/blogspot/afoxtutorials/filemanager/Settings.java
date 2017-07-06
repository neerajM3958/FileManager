package com.blogspot.afoxtutorials.filemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class Settings extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeID = new Themer(this).themeRes();
        setTheme(themeID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, RootActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, RootActivity.class);
                startActivity(i);
                finish();
                break;
        }
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue.toString().trim().length() < 3) {
                return false;
            }
            preference.setSummary(newValue.toString());
            return true;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference themePrefernce = findPreference("theme_list_prefrence");
            themePrefernce.setOnPreferenceChangeListener(this);
            SharedPreferences themeSharedPreferences = PreferenceManager.getDefaultSharedPreferences(themePrefernce.getContext());
            String summaryTheme = themeSharedPreferences.getString(themePrefernce.getKey(), "");
            onPreferenceChange(themePrefernce, summaryTheme);

            Preference hiddenPrefrence = findPreference(getString(R.string.key_hidden));
            hiddenPrefrence.setOnPreferenceChangeListener(this);

            Preference rootPrefrence = findPreference(getString(R.string.key_root));
            rootPrefrence.setOnPreferenceChangeListener(this);

            Preference storagePrefernce = findPreference(getString(R.string.key_storage));
            storagePrefernce.setOnPreferenceChangeListener(this);
            SharedPreferences storageSharedPreferences = PreferenceManager.getDefaultSharedPreferences(storagePrefernce.getContext());
            String summaryStorage = storageSharedPreferences.getString(storagePrefernce.getKey(), "");
            onPreferenceChange(storagePrefernce, summaryStorage);


        }
    }

}
