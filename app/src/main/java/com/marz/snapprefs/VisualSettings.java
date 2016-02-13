package com.marz.snapprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.io.File;

public class VisualSettings extends PreferenceFragment {

    private SharedPreferences sharedPreferences;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.visual_preferences);
        final Context ctx = getActivity();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
}