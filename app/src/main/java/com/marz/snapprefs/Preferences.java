package com.marz.snapprefs;

import android.content.SharedPreferences;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Created by Andre on 07/09/2016.
 */
public class Preferences {
    public static final int SAVE_S2S = 1;
    public static final int DO_NOT_SAVE = 2;
    public static final int SAVE_BUTTON = 0;
    public static final int SAVE_AUTO = 3;
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;
    public static final int TIMER_MINIMUM_DISABLED = 0;
    public static final int SPIN_EXCESS = 200;

    private static ConcurrentHashMap<String, Object> preferenceMap = new ConcurrentHashMap<>();
    private static XSharedPreferences xSPrefs;
    private static FileObserver observer;

    public static XSharedPreferences createXSPrefsIfNotExisting()
    {
        Log.d("snapchat", "Loading preferences");

        if( xSPrefs == null) {
            Log.d("snapchat", "Null preferences... Creating new");
            Log.d("snapchat", "Package name: " + HookMethods.PACKAGE_NAME);

            File prefsFile = new File(
                    Environment.getDataDirectory(), "data/"
                    + HookMethods.class.getPackage().getName() + "/shared_prefs/" + HookMethods.class.getPackage().getName()
                    + "_preferences" + ".xml");

            if( prefsFile.exists()) {
                prefsFile.setReadable(true, false);
                Log.d("snapchat", "XPrefs file exists: " + prefsFile.getPath());
            }

            xSPrefs = new XSharedPreferences(HookMethods.PACKAGE_NAME, HookMethods.PACKAGE_NAME + "_preferences");
        }

        Log.d("snapchat", "Making readable");
        xSPrefs.makeWorldReadable();

        return xSPrefs;
    }

    public static void loadMapFromXposed()
    {
        /*observer = new FileObserver(prefsFile.getAbsolutePath()) {//this needs to be field, because as variable it will be garbage collected
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.CLOSE_WRITE) {
                    Log.d("snapchat", "HOOKED Observer: CLOSE_WRITE");
                } else if( event == FileObserver.CLOSE_NOWRITE)
                    Log.d("snapchat", "HOOKED Observer: CLOSE_NOWRITE");
                else if( event == FileObserver.ACCESS)
                    Log.d("snapchat", "HOOKED Observer: ACCESS");
                else if( event == FileObserver.OPEN)
                    Log.d("snapchat", "HOOKED Observer: OPEN");
                else if( event == FileObserver.CREATE)
                    Log.d("snapchat", "HOOKED Observer: CREATE");

            }
        };
        observer.startWatching();*/

        createXSPrefsIfNotExisting();

        xSPrefs.reload();

        try {
            int spinCount = 0;
            Field field = XSharedPreferences.class.getDeclaredField("mLoaded");
            field.setAccessible(true);
            boolean mLoaded;
            boolean triggerSpinExcess = false;
            int currentExcess = 0;

            Log.v("snapchat", "Starting spin");
            do {
                spinCount++;

                if( (spinCount % 50) == 0)
                    Log.v("snapchat", "Current spin count: " + spinCount);

                if (spinCount > 35000)
                    break;

                field.setAccessible(true);
                mLoaded = (boolean) field.get(xSPrefs);

                if(mLoaded && !triggerSpinExcess)
                    triggerSpinExcess = true;

                if( triggerSpinExcess)
                    currentExcess++;

            } while (currentExcess < SPIN_EXCESS);

            Log.v("snapchat", "Completed " + spinCount + " spins");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        loadMap(xSPrefs);
    }

    public static void initialiseListener( SharedPreferences sharedPreferences)
    {
        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key) {
                Log.d("snapchat", "SharedPreference changed: " + key);
                Object preferenceVal = preferenceMap.get(key);

                if( preferenceVal == null ) {
                    Log.d("snapchat", "No value found in preferences: " + key);
                    return;
                }

                Prefs[] prefsList = Prefs.values();

                for( Prefs preference : prefsList)
                {
                    if(!key.equals(preference.key))
                        continue;

                    if( preferenceVal instanceof Boolean)
                        setPref(preference, sPrefs.getBoolean(key, (boolean) preference.defaultVal));
                    else if( preferenceVal instanceof String)
                        setPref(preference, sPrefs.getString(key, (String) preference.defaultVal));
                    else if(preferenceVal instanceof Integer)
                       setPref(preference, sPrefs.getInt(key, (int) preference.defaultVal));

                    break;
                }
            }
        });
    }

    public static void loadMap(SharedPreferences sharedPreferences)
    {
        Log.d("snapchat", "loading preference map: " + (sharedPreferences != null));

        if( sharedPreferences == null )
            return;

        Map<String, ?> map = sharedPreferences.getAll();
        Log.d("snapchat", "Map size: " + (map != null ? map.size() : "null"));

        if( map == null) {
            Log.d("snapchat", "Null map :(");
            return;
        }

        //preferenceMap = new ConcurrentHashMap<>(map);
        preferenceMap = new ConcurrentHashMap<>();
        for( String key : map.keySet())
        {
            if( key == null)
            {
                Log.d("snapchat", "Null key");
                continue;
            }
            Object obj = map.get(key);
            Log.d("snapchat", "Loaded preference: " + key + " val: " + obj);
            preferenceMap.put(key, obj);
        }
    }

    public static ConcurrentHashMap<String, Object> getMap()
    {
        return preferenceMap;
    }

    public static Object getPref(String key, Object defaultVal){
        Object preferenceVal = preferenceMap.get( key );

        //Log.d("snapchat", "Preference key: " + key + " Value: " + preferenceVal);

        if( preferenceVal == null)
            return defaultVal;

        return preferenceVal;
    }

    public static Object getPref(Prefs preference){
        Object preferenceVal = preferenceMap.get( preference.key );

        Log.d("snapchat", "Preference key: " + preference.key + " Value: " + preferenceVal);

        if( preferenceVal == null)
            return preference.defaultVal;

        return preferenceVal;
    }

    public static boolean getBool(Prefs preference)
    {
        return (boolean) getPref(preference);
    }

    public static String getString(Prefs preference)
    {
        return (String) getPref(preference);
    }

    public static int getInt(Prefs preference)
    {
        Object preferenceVal = getPref(preference);

        if( preferenceVal instanceof String)
            return Integer.parseInt((String) preferenceVal);

        return (int) preferenceVal;
    }

    public static void setPref(String key, Object value) throws NullPointerException
    {
        preferenceMap.put(key, value);
    }

    public static void setPref(Prefs preference, Object value)
    {
        preferenceMap.put(preference.key, value != null ? value : preference.defaultVal);
    }

    public static boolean shouldAddGhost(){
        return getBool(Prefs.SPEED ) || getBool(Prefs.TEXT_TOOLS) || getBool(Prefs.WEATHER);
    }

    /**
     * This method should be used exclusively in the Snapprefs threads - Not on a snapchat thread
     * @param deviceId
     * @return
     */
    public static int getLicenceUsingID(String deviceId)
    {
        String confirmationId = getString(Prefs.CONFIRMATION_ID);

        if( confirmationId.equals(Prefs.CONFIRMATION_ID.defaultVal))
            return 0;

        String storedDeviceId = getString(Prefs.DEVICE_ID);

        if( storedDeviceId == null || storedDeviceId.equals(Prefs.DEVICE_ID.defaultVal) ||
                !storedDeviceId.equals(deviceId))
            return 0;

        return (int) getPref( storedDeviceId, Prefs.LICENCE.defaultVal);
    }

    public static int getLicence()
    {
        String confirmationId = getString(Prefs.CONFIRMATION_ID);

        if( confirmationId.equals(Prefs.CONFIRMATION_ID.defaultVal))
            return 0;

        String storedDeviceId = getString(Prefs.DEVICE_ID);

        if( storedDeviceId == null || storedDeviceId.equals(Prefs.DEVICE_ID.defaultVal))
            return 0;

        return (int) getPref( storedDeviceId, Prefs.LICENCE.defaultVal);
    }


    public static String getExternalPath() {
        try {
            Class<?> environmentcls = Class.forName("android.os.Environment");
            Method setUserRequiredM = environmentcls.getMethod("setUserRequired", boolean.class);
            setUserRequiredM.invoke(null, false);


            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logger.log("Get external path exception", e);
        }

        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void updateBoolean(String settingKey, boolean state) {
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + "com.marz.snapprefs" + "/shared_prefs/" + "com.marz.snapprefs"
                + "_preferences" + ".xml");

        if( MainActivity.prefs == null )
        {
            Log.d("snapchat", "Updating preference");
            MainActivity.prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        }

        SharedPreferences.Editor editor = MainActivity.prefs.edit();
        editor.putBoolean(settingKey, state);
        editor.commit();
        prefsFile.setReadable(true, false);
    }

    public enum Prefs
    {
        CUSTOM_FILTER("pref_key_force_navbar", false),
        PAINT_TOOLS("pref_key_paint_checkbox", true),
        MULTI_FILTER("pref_key_multi_filter_checkbox", true),
        TIMER_UNLIMITED("pref_key_timer_unlimited", true),
        HIDE_TIMER_STORY("pref_key_timer_story_hide", false),
        LOOPING_VIDS("pref_key_looping_video", true),
        HIDE_TIMER_SNAP("pref_key_timer_hide", false),
        TOAST_ENABLED("pref_key_toasts_checkbox", true),
        VIBRATIONS_ENABLED("pref_key_vibration_checkbox", true),
        SAVE_SENT_SNAPS("pref_key_save_sent_snaps", true),
        SORT_BY_CATEGORY("pref_key_sort_files_mode", false),
        SORT_BY_USERNAME("pref_key_sort_files_username", true),
        DEBUGGING("pref_key_debug", true),
        OVERLAYS("pref_key_overlay", false),
        SPEED("pref_key_speed", false),
        WEATHER("pref_key_weather", false),
        LOCATION("pref_key_location", false),
        STORY_PRELOAD("pref_key_storypreload", false),
        DISCOVER_SNAP("pref_key_discover", false),
        DISCOVER_UI("pref_key_discover_ui", false),
        CUSTOM_STICKER("pref_key_sticker", false),
        HIDE_LIVE("pref_key_hidelive", false),
        HIDE_PEOPLE("pref_key_hidepeople", false),
        REPLAY("pref_key_replay", false),
        STEALTH("pref_key_viewed", false),
        TYPING("pref_key_typing", false),
        UNLIM_GROUPS("pref_key_groups_unlim", false),
        SELECT_ALL("pref_key_selectall", false),
        HIDE_BF("pref_key_hidebf", false),
        TIMER_COUNTER("pref_key_timercounter", false),
        CHAT_AUTO_SAVE("pref_key_save_chat_text", false),
        CHAT_MEDIA_SAVE("pref_key_save_chat_image", false),
        INTEGRATION("pref_key_integration", false),
        BUTTON_POSITION("pref_key_save_button_position", false),
        LENSES_LOAD("pref_key_load_lenses", true),
        LENSES_COLLECT("pref_key_collect_lenses", true),
        ACCEPTED_TOU("acceptedToU", false),
        SELECT_STORY("pref_key_selectstory", false),
        SELECT_VENUE("pref_key_selectvenue", false),
        TEXT_TOOLS("pref_key_text", false),
        HIDE_RECENT("pref_key_hiderecent", false),
        ADD_VISUAL_FILTERS("", false),
        CAPTION_UNLIMITED_VANILLA("pref_caption_unlimited_vanilla", false),
        CAPTION_UNLIMITED_FAT("pref_caption_unlimited_fat", false),
        CHECK_SIZE("pref_size_disabled", true),
        TIMBER("pref_timber", false),

        SAVE_PATH("pref_key_save_location", getExternalPath() + "/Snapprefs"),
        CUSTOM_FILTER_LOCATION("", SAVE_PATH.defaultVal + "/FILTERS"),
        CONFIRMATION_ID("confirmation_id", ""),
        DEVICE_ID("device_id", ""),

        SAVEMODE_SNAP("pref_key_save", SAVE_AUTO),
        SAVEMODE_STORY("pref_key_save_story", SAVE_AUTO),
        TOAST_LENGTH("pref_key_toasts_duration", TOAST_LENGTH_LONG),
        TIMER_MINIMUM("pref_key_timer_minimum", TIMER_MINIMUM_DISABLED),
        FORCE_NAVBAR("pref_key_force_navbar", 0),
        CUSTOM_FILTER_TYPE("pref_key_filter_type", 0),
        LICENCE(DEVICE_ID.key, 0),
        ROTATION_MODE("pref_rotation", Common.ROTATION_CW),
        ADJUST_METHOD("pref_adjustment", Common.ADJUST_CROP);

        public String key;
        public Object defaultVal;

        Prefs(String key, Object defaultVal)
        {
            this.key = key;
            this.defaultVal = defaultVal;
        }
    }
}
