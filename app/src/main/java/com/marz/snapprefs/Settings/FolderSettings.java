package com.marz.snapprefs.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

import static com.marz.snapprefs.Preferences.Prefs.PREF_KEY_HIDE_LOCATION;

/**
 * Created by MARZ on 2016. 02. 11..
 */
public class FolderSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener  {
    private int preferenceId;
    private static final int REQUEST_CHOOSE_DIR = 1;
    private static final int REQUEST_HIDE_DIR = 2;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(preferenceId);

        // Set onClickListener for choosing the Save Location
        android.support.v7.preference.Preference locationChooser = findPreference(Prefs.PREF_KEY_SAVE_LOCATION.key);
        locationChooser.setSummary(Preferences.getString(Prefs.PREF_KEY_SAVE_LOCATION));
        locationChooser.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                /*// Open a new activity asking the user to select a folder
                final Intent chooserIntent = new Intent(getActivity(), DirectoryChooserActivity.class);
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "Snapprefs");
                getActivity().startActivityForResult(chooserIntent, REQUEST_CHOOSE_DIR);*/
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, "/mnt/sdcard/");
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                getActivity().startActivityForResult(i, REQUEST_CHOOSE_DIR);
                return true;
            }
        });

        Preference hidingChooser = findPreference(PREF_KEY_HIDE_LOCATION.key);
        hidingChooser.setSummary(Preferences.getString(PREF_KEY_HIDE_LOCATION));
        hidingChooser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Open a new activity asking the user to select a folder
                /*final Intent chooserIntent = new Intent(getActivity(), DirectoryChooserActivity.class);
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "Snapprefs");
                getActivity().startActivityForResult(chooserIntent, REQUEST_HIDE_DIR);*/
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, "/mnt/sdcard/");
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                getActivity().startActivityForResult(i, REQUEST_HIDE_DIR);
                return true;
            }
        });
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
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        // Set preferences file permissions to be world readable
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + FolderSettings.class.getPackage().getName() + "/shared_prefs/" + FolderSettings.class.getPackage().getName()
                + "_preferences" + ".xml");

        if( prefsFile.exists())
            prefsFile.setReadable(true, false);
    }

    public FolderSettings setPreferenceId(int preferenceId) {
        this.preferenceId = preferenceId;
        return this;
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        try {
            preference.setSummary(sharedPreferences.getString(key, ""));
        } catch (ClassCastException | NullPointerException ignore) {
            //boolean cannot be cast to String
        }
    }
}
