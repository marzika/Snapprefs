package com.marz.snapprefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.marz.snapprefs.Tabs.BuyTabFragment;
import com.marz.snapprefs.Tabs.DataTabFragment;
import com.marz.snapprefs.Tabs.DeluxeTabFragment;
import com.marz.snapprefs.Tabs.FiltersTabFragment;
import com.marz.snapprefs.Tabs.GeneralTabFragment;
import com.marz.snapprefs.Tabs.MainTabFragment;
import com.marz.snapprefs.Tabs.SavingTabFragment;
import com.marz.snapprefs.Tabs.SharingTabFragment;
import com.marz.snapprefs.Tabs.SpoofingTabFragment;
import com.marz.snapprefs.Tabs.TextTabFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import de.cketti.library.changelog.ChangeLog;

public class MainActivity extends AppCompatActivity {
    public static final String PREF_KEY_SAVE_LOCATION = "pref_key_save_location";
    public static final String PREF_KEY_HIDE_LOCATION = "pref_key_hide_location";
    private static final int REQUEST_CHOOSE_DIR = 1;
    private static final int REQUEST_HIDE_DIR = 2;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    HashMap<Integer, Fragment> cache = new HashMap<>();
    private SharedPreferences sharedPreferences;
    private ArrayList<MenuItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        final String confirmationID = readStringPreference("confirmation_id");
        final Context context = this;
        ChangeLog cl = new ChangeLog(context);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if (readLicense(deviceId, confirmationID) == 1 || readLicense(deviceId, confirmationID) == 2) {
            mAdView.destroy();
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("4874476DA9EEB44071D24FAB8B3BA420")
                    .build();
            mAdView.loadAd(adRequest);
            mAdView.setVisibility(View.VISIBLE);
        }

        /**
         *Setup the DrawerLayout and NavigationView
         */

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.fuckyou);
        if (readLicense(deviceId, confirmationID) == 1 || readLicense(deviceId, confirmationID) == 2) {
            mNavigationView.getMenu().getItem(1).getSubMenu().getItem(1).setEnabled(true);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mDrawerLayout.getLayoutParams();
            lp.setMargins(0, 0, 0, 0);
            mDrawerLayout.setLayoutParams(lp);
        }
        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the MainTabFragment as the first Fragment
         */

        mFragmentManager = getSupportFragmentManager();
//        mFragmentTransaction = mFragmentManager.beginTransaction();
//        mFragmentTransaction.replace(R.id.containerView,new MainTabFragment()).commit();
        mFragmentManager.beginTransaction().replace(R.id.containerView, getForId(R.id.nav_item_main)).commit();
        mNavigationView.getMenu().getItem(0).setCheckable(true);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        items.add(mNavigationView.getMenu().getItem(0));
        /**
         * Setup click events on the Navigation View Items.
         */

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                menuItem.setCheckable(true);
                menuItem.setChecked(true);
                Iterator<MenuItem> it = items.iterator();
                while (it.hasNext())
                {
                    MenuItem item = it.next();
                    if(!item.equals(menuItem)){
                        item.setChecked(false);
                    }
                }
                items.add(menuItem);
                mFragmentManager.beginTransaction().replace(R.id.containerView,getForId(menuItem.getItemId())).commit();
                return false;
            }

        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }

    public Fragment getForId(int id) {
        if (cache.get(id) == null) {
            switch (id) {
                case R.id.nav_item_buy:
                    cache.put(id, new BuyTabFragment());
                    break;
                case R.id.nav_item_main:
                    cache.put(id, new MainTabFragment());
                    break;
                case R.id.nav_item_deluxe:
                    cache.put(id, new DeluxeTabFragment());
                    break;
                case R.id.nav_item_general:
                    cache.put(id, new GeneralTabFragment());
                    break;
                case R.id.nav_item_saving:
                    cache.put(id, new SavingTabFragment());
                    break;
                case R.id.nav_item_text:
                    cache.put(id, new TextTabFragment());
                    break;
                case R.id.nav_item_spoofing:
                    cache.put(id, new SpoofingTabFragment());
                    break;
                case R.id.nav_item_sharing:
                    cache.put(id, new SharingTabFragment());
                    break;
                case R.id.nav_item_data:
                    cache.put(id, new DataTabFragment());
                    break;
                case R.id.nav_item_filters:
                    cache.put(id, new FiltersTabFragment());
                    break;
            }
        }
        return cache.get(id);
    }
    // Receives the result of the DirectoryChooserActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_DIR && resultCode == Activity.RESULT_OK) {
                String newLocation = data.getData().toString().substring(7);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_KEY_SAVE_LOCATION, newLocation);
                editor.apply();

                //Preference pref = PreferenceFragmentCompat.findPreference(PREF_KEY_SAVE_LOCATION);
                //pref.setSummary(newLocation);
            }
        if (requestCode == REQUEST_HIDE_DIR && resultCode == Activity.RESULT_OK) {
                String newHiddenLocation =  data.getData().toString().substring(7);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_KEY_HIDE_LOCATION, "Last hidden: " + newHiddenLocation);
                editor.apply();

                writeNoMediaFile(newHiddenLocation);
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

    public int readLicense(String deviceID, String confirmationID) {
        int status;
        if (confirmationID != null) {
            SharedPreferences prefs = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE);
            String dvcid = prefs.getString("device_id", null);
            if (dvcid != null && dvcid.equals(deviceID)) {
                status = prefs.getInt(deviceID, 0);
            } else {
                status = 0;
            }
        } else {
            status = 0;
        }
        return status;
    }

    public String readStringPreference(String key) {
        SharedPreferences prefs = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE);
        String returned = prefs.getString(key, null);
        return returned;
    }
}