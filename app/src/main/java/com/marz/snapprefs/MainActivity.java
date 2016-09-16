package com.marz.snapprefs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import de.cketti.library.changelog.ChangeLog;

public class MainActivity extends AppCompatActivity {
    public static final String PREF_KEY_SAVE_LOCATION = "pref_key_save_location";
    public static final String PREF_KEY_HIDE_LOCATION = "pref_key_hide_location";
    protected static final int MSG_REGISTER_WITH_GCM = 101;
    protected static final int MSG_REGISTER_WEB_SERVER = 102;
    protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
    protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
    private static final int REQUEST_CHOOSE_DIR = 1;
    private static final int REQUEST_HIDE_DIR = 2;
    // Resgistration Id from GCM
    private static final String PREF_GCM_REG_ID = "PREF_GCM_REG_ID";
    // Your project number and web server url. Please change below.
    private static final String GCM_SENDER_ID = "410204387699";
    private static final String WEB_SERVER_URL = "http://snapprefs.com/gcm/register_user.php";
    private static final int ACTION_PLAY_SERVICES_DIALOG = 100;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    HashMap<Integer, Fragment> cache = new HashMap<>();
    GoogleCloudMessaging gcm;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_WITH_GCM:
                    new GCMRegistrationTask().execute();
                    break;
                case MSG_REGISTER_WEB_SERVER:
                    new WebServerRegistrationTask().execute();
                    break;
                case MSG_REGISTER_WEB_SERVER_SUCCESS:
                    /*Toast.makeText(getApplicationContext(),
                            "registered with web server", Toast.LENGTH_LONG).show();*/
                    break;
                case MSG_REGISTER_WEB_SERVER_FAILURE:
                    Toast.makeText(getApplicationContext(),
                            "registration with web server failed",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }

    };
    private SharedPreferences sharedPreferences;
    private ArrayList<MenuItem> items = new ArrayList<>();
    private SharedPreferences prefs;
    private String gcmRegId;
    private boolean acceptedToU = false;

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
        final SharedPreferences prefs = context.getSharedPreferences("com.marz.snapprefs_preferences", Context.MODE_WORLD_READABLE);
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + getPackageName() + "/shared_prefs/" + getPackageName()
                + "_preferences" + ".xml");
        prefsFile.setReadable(true, false);
        acceptedToU = prefs.getBoolean("acceptedToU", false);
        if(!acceptedToU){
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("ToU and Privacy Policy")
                    .setView(R.layout.tos)
                    .setMessage("You haven't accepted our Terms of Use and Privacy. Please read it carefully and accept it, otherwise you will not be able to use our product.")
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("acceptedToU", true);
                            editor.apply();
                            Toast.makeText(MainActivity.this, "You accepted the Terms of Use and Privacy Policy", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert);
            builder.setCancelable(false);
            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            Button privacypolicy = (Button) dialog.findViewById(R.id.privacypolicy);
            Button tou = (Button) dialog.findViewById(R.id.tou);
            CheckBox accepted = (CheckBox) dialog.findViewById(R.id.readandaccepted);

            privacypolicy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://snapprefs.com/wp/privacy-policy/"));
                        startActivity(myIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "No application can handle this request." + " Please install a webbrowser", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
            tou.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://snapprefs.com/wp/terms-of-use/"));
                        startActivity(myIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "No application can handle this request." + " Please install a webbrowser", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            accepted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(b);
                }
            });
        }
        if (isGooglePlayInstalled()) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

            // Read saved registration id from shared preferences.
            gcmRegId = getSharedPreferences().getString(PREF_GCM_REG_ID, "");

            if (TextUtils.isEmpty(gcmRegId)) {
                handler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
            } else {
                //Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_SHORT).show();
            }
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        TextView pugs = (TextView) findViewById(R.id.pugs);
        if (readLicense(deviceId, confirmationID) == 1 || readLicense(deviceId, confirmationID) == 2) {
            mAdView.destroy();
            pugs.setVisibility(View.GONE);
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("4874476DA9EEB44071D24FAB8B3BA420")
                    .build();
            mAdView.loadAd(adRequest);
            pugs.setVisibility(View.VISIBLE);
            pugs.setText("\uD83D\uDC36" + " " + pugs.getText() + " " + "\uD83D\uDC36");
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

    private SharedPreferences getSharedPreferences() {
        if (prefs == null) {
            prefs = getApplicationContext().getSharedPreferences(
                    "com.marz.snapprefs_preferences", Context.MODE_PRIVATE);
        }
        return prefs;
    }

    public void saveInSharedPref(String result) {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREF_GCM_REG_ID, result);
        editor.commit();
    }

    private boolean isGooglePlayInstalled() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        ACTION_PLAY_SERVICES_DIALOG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Google Play Service is not installed",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    private class GCMRegistrationTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (gcm == null && isGooglePlayInstalled()) {
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            }
            try {
                gcmRegId = gcm.register(GCM_SENDER_ID);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return gcmRegId;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                /*Toast.makeText(getApplicationContext(), "registered with GCM: " + result,
                        Toast.LENGTH_LONG).show();*/
                saveInSharedPref(result);
                handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER);
            }
        }

    }

    private class WebServerRegistrationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            URL url = null;
            try {
                url = new URL(WEB_SERVER_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
            }
            Map<String, String> dataMap = new HashMap<String, String>();
            dataMap.put("regId", gcmRegId);

            StringBuilder postBody = new StringBuilder();
            Iterator iterator = dataMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry param = (Map.Entry) iterator.next();
                postBody.append(param.getKey()).append('=')
                        .append(param.getValue());
                if (iterator.hasNext()) {
                    postBody.append('&');
                }
            }
            String body = postBody.toString();
            byte[] bytes = body.getBytes();

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");

                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();

                int status = conn.getResponseCode();
                if (status == 200) {
                    // Request success
                    handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_SUCCESS);
                } else {
                    throw new IOException("Request failed with error code "
                            + status);
                }
            } catch (ProtocolException pe) {
                pe.printStackTrace();
                handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
            } catch (IOException io) {
                io.printStackTrace();
                handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return null;
        }
    }
}