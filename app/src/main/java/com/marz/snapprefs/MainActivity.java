package com.marz.snapprefs;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class MainActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00a650"));
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.abs);
        getActionBar().setBackgroundDrawable(colorDrawable);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Settings()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }
}