package com.marz.snapprefs;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
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
import com.marz.snapprefs.Fragments.LensesFragment;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Tabs.BuyTabFragment;
import com.marz.snapprefs.Tabs.ChatLogsTabFragment;
import com.marz.snapprefs.Tabs.DataTabFragment;
import com.marz.snapprefs.Tabs.DeluxeTabFragment;
import com.marz.snapprefs.Tabs.FiltersTabFragment;
import com.marz.snapprefs.Tabs.GeneralTabFragment;
import com.marz.snapprefs.Tabs.LensesTabFragment;
import com.marz.snapprefs.Tabs.MainTabFragment;
import com.marz.snapprefs.Tabs.SavingTabFragment;
import com.marz.snapprefs.Tabs.SharingTabFragment;
import com.marz.snapprefs.Tabs.SpoofingTabFragment;
import com.marz.snapprefs.Tabs.TextTabFragment;
import com.marz.snapprefs.Util.CommonUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import de.cketti.library.changelog.ChangeLog;

import static com.marz.snapprefs.Preferences.Prefs.PREF_KEY_HIDE_LOCATION;
import static com.marz.snapprefs.Preferences.Prefs.PREF_KEY_SAVE_LOCATION;

public class MainActivity extends AppCompatActivity {
    protected static final int MSG_REGISTER_WITH_GCM = 101;
    protected static final int MSG_REGISTER_WEB_SERVER = 102;
    protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
    protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
    private static final int REQUEST_CHOOSE_DIR = 1;
    private static final int REQUEST_HIDE_DIR = 2;
    // Registration Id from GCM
    private static final String PREF_GCM_REG_ID = "PREF_GCM_REG_ID";
    // Your project number and web server url. Please change below.
    private static final String GCM_SENDER_ID = "410204387699";
    private static final String WEB_SERVER_URL = "http://snapprefs.com/gcm/register_user.php";
    private static final int ACTION_PLAY_SERVICES_DIALOG = 100;
    public static Context context;
    public static SharedPreferences prefs = null;
    private static FileObserver observer;
    private static UUID deviceUuid;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    public static FragmentManager mFragmentManager;
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
    private MenuItem mainMenuItem;
    private MenuItem lastItem;
    private String gcmRegId;

    public static String getDeviceId() {
        return deviceUuid != null ? deviceUuid.toString() : (String) Preferences.Prefs.DEVICE_ID.defaultVal;
    }

    public static SharedPreferences getPreferences() {
        return prefs;
    }

    @Override
    protected void onPause(){
        super.onPause();

        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + getPackageName() + "/shared_prefs/" + getPackageName()
                + "_preferences" + ".xml");

        if( prefsFile.exists())
            prefsFile.setReadable(true, false);

        LensesFragment.bitmapCache.clearCache();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Logger.loadSelectedLogTypes();
        ChangeLog cl = new ChangeLog(context);
        createDeviceId();




        /*Obfuscator.writeGsonFile();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Obfuscator.readJsonFile();*/

        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }
        Logger.log("MainActivity: Checking if module is enabled.");
        int moduleStatus = CommonUtils.getModuleStatus();
        if(moduleStatus != Common.MODULE_STATUS_ACTIVATED) {
            if (moduleStatus == Common.MODULE_STATUS_NOT_ACTIVATED) {
                Toast toast = Toast.makeText(getApplicationContext(), "Module Does Not Appear To Be Activated!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Please Restart Device For Hooks To Update!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        }


        Logger.log("MainActivity: createPrefsIfNotExisting");
        createPrefsIfNotExisting();

        if (Preferences.getMap() == null || Preferences.getMap().isEmpty()) {
            Logger.log("MainActivity: Map is null or empty: Loading new");
            Preferences.loadMap(prefs);
        }

        Logger.log("Load lenses: " + prefs.contains("pref_key_load_lenses"));

        Logger.log("SAVE LOCATION: " + Preferences.getSavePath());
        if (!Preferences.getBool(Preferences.Prefs.ACCEPTED_TOU)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("ToU and Privacy Policy")
                    .setView(R.layout.tos)
                    .setMessage("You haven't accepted our Terms of Use and Privacy. Please read it carefully and accept it, otherwise you will not be able to use our product.")
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Preferences.putBool("acceptedToU", true);
                            Toast.makeText(MainActivity.this, "You accepted the Terms of Use and Privacy Policy", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert);
            builder.setCancelable(false);
            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            Button privacyPolicy = (Button) dialog.findViewById(R.id.privacypolicy);
            Button tou = (Button) dialog.findViewById(R.id.tou);
            CheckBox accepted = (CheckBox) dialog.findViewById(R.id.readandaccepted);

            privacyPolicy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://snapprefs.com/wp/privacy-policy/"));
                        startActivity(myIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
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
            gcmRegId = createPrefsIfNotExisting().getString(PREF_GCM_REG_ID, "");

            if (TextUtils.isEmpty(gcmRegId)) {
                handler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
            } else {
                //Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_SHORT).show();
            }
        }

        setContentView(R.layout.activity_main);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        TextView pugs = (TextView) findViewById(R.id.pugs);
        if (Preferences.getLicenceUsingID(deviceUuid.toString()) == 1 || Preferences.getLicenceUsingID(deviceUuid.toString()) == 2) {
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
        if (Preferences.getLicenceUsingID(deviceUuid.toString()) == 1 || Preferences.getLicenceUsingID(deviceUuid.toString()) == 2) {
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
        mainMenuItem = mNavigationView.getMenu().getItem(0);
        mFragmentManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {
            int lastEntryCount = 0;
            @Override
            public void onBackStackChanged() {
                int entryCount = mFragmentManager.getBackStackEntryCount();
                Logger.log("StackSize: "+ entryCount);
                String entryName;

                lastEntryCount = entryCount;

                if(entryCount <= 0)
                    entryName = mainMenuItem.getTitle().toString();
                else
                    entryName = mFragmentManager.getBackStackEntryAt(entryCount - 1).getName();

                Logger.log("EntryName: " + entryName);
                selectNavItem(entryName);
            }
        });
//        mFragmentTransaction = mFragmentManager.beginTransaction();
//        mFragmentTransaction.replace(R.id.containerView,new MainTabFragment()).commit();
        mFragmentManager.beginTransaction().replace(R.id.containerView, getForId(R.id.nav_item_main)).commit();
        mainMenuItem.setCheckable(true);
        mainMenuItem.setChecked(true);
        lastItem = mainMenuItem;

        /**
         * Setup click events on the Navigation View Items.
         */

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem == lastItem)
                    return false;
                mDrawerLayout.closeDrawers();
                clearBackStack();
                menuItem.setCheckable(true);

                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

                if(menuItem != mainMenuItem)
                    fragmentTransaction.addToBackStack(menuItem.getTitle().toString());

                fragmentTransaction.replace(R.id.containerView, getForId(menuItem.getItemId()));
                fragmentTransaction.commit();
                selectNavItem(menuItem);
                return false;
            }
        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }

    private void selectNavItem(MenuItem item) {
        lastItem.setChecked(false);
        item.setChecked(true);
        lastItem = item;
    }

    private void selectNavItem(String entryName) {
        for(int i = 0; i < mNavigationView.getMenu().size(); i++) {
            MenuItem item = mNavigationView.getMenu().getItem(i);

            if( item.hasSubMenu() ) {
                selectNavItemFromSub(entryName, item.getSubMenu());
            } else {
                if (item.getTitle().equals(entryName)) {
                    selectNavItem(item);
                    return;
                }
            }
        }
    }

    private void selectNavItemFromSub(String entryName, SubMenu subMenu) {
        for(int i = 0; i < subMenu.size(); i++) {
            MenuItem item = subMenu.getItem(i);

            if( item.hasSubMenu() )
                selectNavItemFromSub(entryName, item.getSubMenu());
            else {
                if (item.getTitle().equals(entryName)) {
                    selectNavItem(item);
                    return;
                }
            }
        }
    }

    private boolean clearBackStack() {
        int count = mFragmentManager.getBackStackEntryCount() - 1;

        for(int i = count; i > -1; i--) {
            BackStackEntry stackEntry = mFragmentManager.getBackStackEntryAt(i);

            if( stackEntry == null )
                return false;

            Logger.log(String.format("Removed [%s][Index:%s] from back stack", stackEntry.getName(), i), LogType.DEBUG);
            mFragmentManager.popBackStack(stackEntry.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        return false;
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
                case R.id.nav_item_lenses:
                    cache.put(id, new LensesTabFragment());
                    break;
                case R.id.nav_item_chat:
                    cache.put(id, new ChatLogsTabFragment());
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

            Preferences.putString(PREF_KEY_SAVE_LOCATION.key, newLocation);
        }
        if (requestCode == REQUEST_HIDE_DIR && resultCode == Activity.RESULT_OK) {
            String newHiddenLocation = data.getData().toString().substring(7);

            Preferences.putString(PREF_KEY_HIDE_LOCATION.key, "Last hidden: " + newHiddenLocation);

            writeNoMediaFile(newHiddenLocation);
        }
    }

    /**
     * @param directoryPath The full path to the directory to place the .nomedia file
     * @return Returns true if the file was successfully written or appears to already exist
     */
    public static boolean writeNoMediaFile(String directoryPath) {
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            FileOutputStream noMediaOutStream = null;

            try {
                File noMedia = new File(directoryPath, ".nomedia");

                if (noMedia.exists()) {
                    return true;
                }

                noMediaOutStream = new FileOutputStream(noMedia);
                noMediaOutStream.write(0);
            } catch (Exception e) {
                return false;
            }
            finally {
                try {
                    if( noMediaOutStream != null ) {
                        noMediaOutStream.flush();
                        noMediaOutStream.close();
                    }
                } catch (IOException ignored) { }
            }
        } else {
            return false;
        }
        return true;
    }

    public void createDeviceId() {
        if (deviceUuid != null)
            return;

        final TelephonyManager tm = (TelephonyManager) this.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private SharedPreferences createPrefsIfNotExisting() {
        if(prefs != null)
            return prefs;

        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + getPackageName() + "/shared_prefs/" + getPackageName()
                + "_preferences" + ".xml");
        prefsFile.setReadable(true, false);
        Logger.log("Creating preference object : " + this.getPackageName());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return prefs;
    }

    public static boolean isNetworkAvailable(final Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public void onResume() {
        super.onResume();

        Preferences.initialiseListener(createPrefsIfNotExisting(), this);
    }

    public void saveInSharedPref(String result) {
        Preferences.putString(PREF_GCM_REG_ID, result);
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

    public static void killSCService(Activity activity) throws IOException {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for(RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            String packageName = serviceInfo.service.getPackageName();

            if(packageName.equals("com.snapchat.android")) {
                Logger.log("PackageName: " + packageName);
                Logger.log("Process: " + serviceInfo.process);
                Logger.log("Started: " + serviceInfo.started);

                Process suProcess = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                os.writeBytes("adb shell" + "\n");

                os.flush();

                os.writeBytes("am force-stop com.snapchat.android" + "\n");

                os.flush();

                Toast.makeText(activity, "Killed snapchat in the background", Toast.LENGTH_SHORT).show();
                break;
            }
        }
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