package com.marz.snapprefs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


import java.io.File;
import java.io.FileOutputStream;

public class SettingsOld extends PreferenceFragment {
    public static final String PREF_KEY_SAVE_LOCATION = "pref_key_save_location";
    public static final String PREF_KEY_HIDE_LOCATION = "pref_key_hide_location";
    public static final String PREF_KEY_FILTER_LOCATION = "pref_key_filter_location";
    public static final String PREF_KEY_LOCATION_PICKER = "pref_key_location_picker";
    private static final int REQUEST_CHOOSE_DIR = 0x0B00B135;
    private static final int REQUEST_HIDE_DIR = 0x2B00B135;
    private static final int REQUEST_FILTER_DIR = 0x3B00B135;
    private static XModuleResources mResources;
    private final Preference.OnPreferenceChangeListener launcherChangeListener = new Preference.OnPreferenceChangeListener() {

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int state = ((Boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

            Activity activity = getActivity();
            ComponentName alias = new ComponentName(activity, "com.marz.snapprefs.SettingsActivity-Alias");
            PackageManager p = activity.getPackageManager();
            p.setComponentEnabledSetting(alias, state, PackageManager.DONT_KILL_APP);
            return true;
        }
    };
    private SharedPreferences sharedPreferences;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preferences);
        final Context ctx = getActivity();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Add listener to the Launcher preference
        //Preference launcherPref = findPreference("pref_launcher");
        //launcherPref.setOnPreferenceChangeListener(launcherChangeListener);
    }

    public void onDestroy() {
        //mock.shutdown();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Set preferences file permissions to be world readable
        File sharedPrefsDir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");
        File sharedPrefsFile = new File(sharedPrefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
        if (sharedPrefsFile.exists()) {
            sharedPrefsFile.setReadable(true, false);
        }
    }

    /**
     * @param directoryPath The full path to the directory to place the .nomedia file
     * @return Returns true if the file was successfully written or appears to already exist
     */
    public boolean writeNoMediaFile(String directoryPath) {
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            try {
                File noMedia = new File(directoryPath, ".nomedia");
                if (noMedia.exists()) {
                    return true;
                }
                FileOutputStream noMediaOutStream = new FileOutputStream(noMedia);
                noMediaOutStream.write(0);
                noMediaOutStream.close();
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}