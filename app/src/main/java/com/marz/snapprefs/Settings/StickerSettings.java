package com.marz.snapprefs.Settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.MainActivity;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.R;

import java.io.File;
import java.util.HashSet;

/**
 * Created by MARZ on 2016. 02. 11..
 */
public class StickerSettings extends PreferenceFragmentCompat {
    private int preferenceId;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(preferenceId);


        Preference wipeFolder = findPreference("pref_sticker_wipe");
        wipeFolder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(MainActivity.context)
                        .setTitle("Wipe SVG Folder")
                        .setMessage("Are you sure you want to wipe the SVG Folder (requires root)?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String folderPath = Environment.getDataDirectory() + "/data/com.snapchat.android/files/twemoji_2_svg/";
                                    Process proc = Runtime.getRuntime()
                                            .exec(new String[]{"su", "-c", "rm -rf " + folderPath});
                                    proc.waitFor();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
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

    public StickerSettings setPreferenceId(int preferenceId) {
        this.preferenceId = preferenceId;
        return this;
    }
}
