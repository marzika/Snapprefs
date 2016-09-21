package com.marz.snapprefs.Settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.io.File;

/**
 * Created by MARZ on 2016. 02. 11..
 */
public class MiscSettings extends PreferenceFragmentCompat {
    private int preferenceId;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(preferenceId);


        Preference pref = findPreference("pref_key_launcher");
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PackageManager packageManager = getActivity().getPackageManager();
                int state = (Boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                ComponentName aliasName = new ComponentName(getActivity(), "com.marz.snapprefs.MainActivity-Alias");
                packageManager.setComponentEnabledSetting(aliasName, state, PackageManager.DONT_KILL_APP);
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }


    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Set preferences file permissions to be world readable
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + MiscSettings.class.getPackage().getName() + "/shared_prefs/" + MiscSettings.class.getPackage().getName()
                + "_preferences" + ".xml");

        if (prefsFile.exists())
            prefsFile.setReadable(true, false);
    }

    public MiscSettings setPreferenceId(int preferenceId) {
        this.preferenceId = preferenceId;
        return this;
    }

}
