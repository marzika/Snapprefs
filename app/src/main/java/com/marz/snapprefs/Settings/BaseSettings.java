package com.marz.snapprefs.Settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.io.File;

/**
 * Created by MARZ on 2016. 02. 11..
 */
public class BaseSettings extends PreferenceFragmentCompat {
    private int preferenceId;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(preferenceId);

        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + BaseSettings.class.getPackage().getName() + "/shared_prefs/" + BaseSettings.class.getPackage().getName()
                + "_preferences" + ".xml");

        if( prefsFile.exists())
            prefsFile.setReadable(true, false);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + BaseSettings.class.getPackage().getName() + "/shared_prefs/" + BaseSettings.class.getPackage().getName()
                + "_preferences" + ".xml");

        if( prefsFile.exists())
            prefsFile.setReadable(true, false);
    }


    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Set preferences file permissions to be world readable
        getPreferenceManager().setSharedPreferencesMode(Activity.MODE_WORLD_READABLE);

        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + BaseSettings.class.getPackage().getName() + "/shared_prefs/" + BaseSettings.class.getPackage().getName()
                + "_preferences" + ".xml");

        if( prefsFile.exists())
            prefsFile.setReadable(true, false);
    }

    public BaseSettings setPreferenceId(int preferenceId) {
        this.preferenceId = preferenceId;
        return this;
    }

}
