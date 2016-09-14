package com.marz.snapprefs.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

/**
 * Created by MARZ on 2016. 02. 11..
 */
public class FolderSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener  {
    private SharedPreferences sharedPreferences;
    private int preferenceId;
    public static final String PREF_KEY_SAVE_LOCATION = "pref_key_save_location";
    public static final String PREF_KEY_HIDE_LOCATION = "pref_key_hide_location";
    private static final int REQUEST_CHOOSE_DIR = 1;
    private static final int REQUEST_HIDE_DIR = 2;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(preferenceId);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, PREF_KEY_SAVE_LOCATION);
        onSharedPreferenceChanged(sharedPreferences, PREF_KEY_HIDE_LOCATION);
        // Set onClickListener for choosing the Save Location
        android.support.v7.preference.Preference locationChooser = findPreference(PREF_KEY_SAVE_LOCATION);
        locationChooser.setSummary(sharedPreferences.getString(PREF_KEY_SAVE_LOCATION, ""));
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

        Preference hidingChooser = findPreference(PREF_KEY_HIDE_LOCATION);
        hidingChooser.setSummary(sharedPreferences.getString(PREF_KEY_HIDE_LOCATION, ""));
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
    }


    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        // Set preferences file permissions to be world readable
        File sharedPrefsDir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");
        File sharedPrefsFile = new File(sharedPrefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
        if (sharedPrefsFile.exists()) {
            sharedPrefsFile.setReadable(true, false);
        }
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
