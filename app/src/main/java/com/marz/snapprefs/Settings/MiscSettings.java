package com.marz.snapprefs.Settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
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
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.R;

import java.io.File;
import java.util.HashSet;

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

        final LayoutInflater inflater = getLayoutInflater(savedInstanceState);

        Preference debugOptions = findPreference("pref_key_debug_options");
        debugOptions.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayDebugMenu(inflater);
                return false;
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

    private void displayDebugMenu(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.debug_layout, null, false);
        Switch debugSwitch = (Switch) view.findViewById(R.id.switch_debug_master);

        debugSwitch.setChecked(Preferences.getBool(Preferences.Prefs.DEBUGGING));

        debugSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.putBool(Preferences.Prefs.DEBUGGING.key, isChecked);
            }
        });
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.scroll_logtype_container);
        applyLogTypeSwitches(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setView(view);
        builder.setPositiveButton("Done", null);
        builder.show();
    }

    private void applyLogTypeSwitches(LinearLayout layout) {
        LogType[] logTypes = Logger.LogType.values();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        HashSet<String> activeTypes = Logger.getActiveLogTypes();

        for (LogType logType : logTypes) {
            Switch logSwitch = new Switch(layout.getContext());
            int pad = (int) (10f * scale);
            logSwitch.setPadding(pad, pad / 2, pad, pad / 2);
            logSwitch.setText(logType.name());
            logSwitch.setChecked(activeTypes.contains(logType.name()));
            logSwitch.setTextSize(7f * scale);
            logSwitch.setTextColor(Color.GRAY);
            logSwitch.setTag(logType.name());

            logSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
                    String tag = (String) compoundButton.getTag();
                    Logger.setLogTypeState(tag, state);
                }
            });

            layout.addView(logSwitch);
        }
    }
}
