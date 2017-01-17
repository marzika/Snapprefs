package com.marz.snapprefs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;

import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Settings.MiscSettings;

import java.io.File;
import java.io.IOException;
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
    public static final int SAVE_F2S = 4;
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;
    public static final int TIMER_MINIMUM_DISABLED = 0;
    public static final int SPIN_EXCESS = 200;

    private static ConcurrentHashMap<String, Object> preferenceMap = new ConcurrentHashMap<>();
    private static XSharedPreferences xSPrefs;

    private static XSharedPreferences createXSPrefsIfNotExisting() {
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + HookMethods.class.getPackage().getName() + "/shared_prefs/" + HookMethods.class.getPackage().getName()
                + "_preferences" + ".xml");

        if (prefsFile.exists()) {
            prefsFile.setReadable(true, false);
            Logger.log("XPrefs file exists: " + prefsFile.getPath(), LogType.PREFS);
        }

        Logger.log("Loading preferences", LogType.PREFS);


        Logger.log("Null preferences... Creating new", LogType.PREFS);
        Logger.log("Package name: " + HookMethods.PACKAGE_NAME, LogType.PREFS);

        xSPrefs = new XSharedPreferences(HookMethods.PACKAGE_NAME, HookMethods.PACKAGE_NAME + "_preferences");


        Logger.log("Making readable", LogType.PREFS);
        xSPrefs.makeWorldReadable();

        return xSPrefs;
    }

    public static void loadMapFromXposed() {
        assignDefaultSavePath();

        createXSPrefsIfNotExisting();

        xSPrefs.reload();

        try {
            int spinCount = 0;
            Field field = XSharedPreferences.class.getDeclaredField("mLoaded");
            field.setAccessible(true);
            boolean mLoaded;
            boolean triggerSpinExcess = false;
            int currentExcess = 0;

            Logger.log("Starting spin locker", LogType.PREFS);
            do {
                spinCount++;

                if ((spinCount % 500) == 0)
                    Logger.log("Current spin count: " + spinCount, LogType.PREFS);

                if (spinCount > 35000)
                    break;

                field.setAccessible(true);
                mLoaded = (boolean) field.get(xSPrefs);

                if (mLoaded && !triggerSpinExcess)
                    triggerSpinExcess = true;

                if (triggerSpinExcess)
                    currentExcess++;

            } while (currentExcess < SPIN_EXCESS);

            Logger.log("Completed " + spinCount + " spins", LogType.PREFS);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        loadMap(xSPrefs);
    }

    public static void initialiseListener(SharedPreferences sharedPreferences, final Activity activity) {
        Logger.log("Initialising Preference Listener", LogType.PREFS);

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key) {
                Logger.log("SharedPreference changed: " + key, LogType.PREFS.setForced());
                Prefs preference = Prefs.getPrefFromKey(key);

                if (preference == null) {
                    Logger.log("No value found in the internal preference list: " + key, LogType.PREFS);
                    return;
                }

                if (preference.defaultVal == null) {
                    Logger.log("No default value found in the internal preference list", LogType.PREFS);
                    return;
                }

                if (preference.defaultVal instanceof Boolean)
                    setPref(preference, sPrefs.getBoolean(key, (boolean) preference.defaultVal));
                else if (preference.defaultVal instanceof String)
                    setPref(preference, sPrefs.getString(key, (String) preference.defaultVal));
                else if (preference.defaultVal instanceof Integer)
                    setPref(preference, sPrefs.getInt(key, (int) preference.defaultVal));
                else {
                    Logger.log("Unknown preference type! " + preference.defaultVal, LogType.PREFS.setForced());
                    return;
                }

                try {
                    if(getBool(Prefs.KILL_SC_ON_PREF_CHANGE))
                        MainActivity.killSCService(activity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void loadMap(SharedPreferences sharedPreferences) {
        Logger.log("loading preference map: " + (sharedPreferences != null), LogType.PREFS);

        if (sharedPreferences == null)
            return;

        Map<String, ?> map = sharedPreferences.getAll();
        Logger.log("Map size: " + (map != null ? map.size() : "null"), LogType.PREFS);

        if (map == null) {
            Logger.log("Null preference map", LogType.PREFS.setForced());
            return;
        }

        //preferenceMap = new ConcurrentHashMap<>(map);
        preferenceMap = new ConcurrentHashMap<>();
        for (String key : map.keySet()) {
            if (key == null) {
                Logger.log("Null preference key", LogType.PREFS.setForced());
                continue;
            }
            Object obj = map.get(key);

            if (obj == null) {
                Logger.log("Loaded null object for: " + key, LogType.PREFS.setForced());
                return;
            }
            //Logger.log("Loaded preference: " + key + " val: " + obj);
            preferenceMap.put(key, obj);
        }
    }

    public static ConcurrentHashMap<String, Object> getMap() {
        return preferenceMap;
    }

    public static Object getPref(String key, Object defaultVal) {
        Object preferenceVal = preferenceMap.get(key);

        if (preferenceVal == null)
            return defaultVal;

        return preferenceVal;
    }

    public static Object getPref(Prefs preference) {
        Object preferenceVal = preferenceMap.get(preference.key);

        if (preferenceVal == null)
            return preference.defaultVal;

        return preferenceVal;
    }

    public static boolean getBool(Prefs preference) {
        return (boolean) getPref(preference);
    }

    public static String getString(Prefs preference) {
        return (String) getPref(preference);
    }

    public static int getInt(Prefs preference) {
        Object preferenceVal = getPref(preference);

        if (preferenceVal instanceof String)
            return Integer.parseInt((String) preferenceVal);

        return (int) preferenceVal;
    }

    public static void setPref(Prefs preference, Object value) {
        preferenceMap.put(preference.key, value != null ? value : preference.defaultVal);
        Logger.log(String.format("Setting preference [Pref:%s] to [Value:%s]", preference, value));
    }

    public static void putContent(Map<String, Object> values) {
        SharedPreferences.Editor editor = MainActivity.prefs.edit();

        for (String key : values.keySet()) {
            Object obj = values.get(key);

            if (obj instanceof Integer)
                editor.putInt(key, (Integer) obj);
            else if (obj instanceof String)
                editor.putString(key, (String) obj);
            else if (obj instanceof Boolean)
                editor.putBoolean(key, (boolean) obj);
        }

        if (editor.commit()) {
            for (String key : values.keySet()) {
                Object obj = values.get(key);
                preferenceMap.put(key, obj);
            }
        }

        updateProtection();
    }

    public static void putString(String key, String value) {
        SharedPreferences.Editor editor = MainActivity.prefs.edit();
        editor.putString(key, value);
        if (editor.commit())
            preferenceMap.put(key, value);

        updateProtection();
    }

    public static void putBool(String key, boolean value) {
        SharedPreferences.Editor editor = MainActivity.prefs.edit();
        editor.putBoolean(key, value);

        if (editor.commit())
            preferenceMap.put(key, value);

        updateProtection();
    }

    public static void putInt(String key, int value) {
        SharedPreferences.Editor editor = MainActivity.prefs.edit();
        editor.putInt(key, value);
        if (editor.commit())
            preferenceMap.put(key, value);

        updateProtection();
    }

    public static boolean shouldAddGhost() {
        return getBool(Prefs.SPEED) || getBool(Prefs.TEXT_TOOLS) || getBool(Prefs.WEATHER);
    }

    /**
     * This method should be used exclusively in the Snapprefs threads - Not on a snapchat thread
     *
     * @param deviceId The ID of the device
     * @return
     */
    public static int getLicenceUsingID(String deviceId) {
        String confirmationId = getString(Prefs.CONFIRMATION_ID);

        if (confirmationId.equals(Prefs.CONFIRMATION_ID.defaultVal))
            return 0;

        String storedDeviceId = getString(Prefs.DEVICE_ID);

        if (storedDeviceId == null || storedDeviceId.equals(Prefs.DEVICE_ID.defaultVal) ||
                !storedDeviceId.equals(deviceId))
            return 0;

        return (int) getPref(storedDeviceId, Prefs.LICENCE.defaultVal);
    }

    public static int getLicence() {
        String confirmationId = getString(Prefs.CONFIRMATION_ID);

        if (confirmationId.equals(Prefs.CONFIRMATION_ID.defaultVal))
            return 0;

        String storedDeviceId = getString(Prefs.DEVICE_ID);

        if (storedDeviceId == null || storedDeviceId.equals(Prefs.DEVICE_ID.defaultVal))
            return 0;

        return (int) getPref(storedDeviceId, Prefs.LICENCE.defaultVal);
    }

    private static String assignDefaultSavePath() {
        try {
            return (String) (Prefs.SAVE_PATH.defaultVal = getExternalPath() + "/Snapprefs");
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getSavePath() {
        String savePath = getString(Prefs.SAVE_PATH);

        if (savePath == null) {
            String newPath = assignDefaultSavePath();

            if (newPath == null) {
                Logger.log("[SEVERE ERROR] PROBLEM ASSIGNING SAVEPATH! Probably too close to runtime", LogType.PREFS.setForced());
                return null;
            }
            else
                return newPath;
        }

        return getString(Prefs.SAVE_PATH);
    }

    public static String getContentPath() {
        return getExternalPath() + "/Snapprefs";
    }

    public static String getFilterPath() {
        String path = getString(Prefs.CUSTOM_FILTER_LOCATION);

        if( path == null ) {
            String newPath = (String) (Prefs.CUSTOM_FILTER_LOCATION.defaultVal = getSavePath() + "/Filters");

            if (newPath == null) {
                Logger.log("[SEVERE ERROR] PROBLEM ASSIGNING SAVEPATH! Probably too close to runtime", LogType.PREFS.setForced());
                return null;
            }
            else
                return newPath;
        }

        return path;
    }

    public static String getExternalPath() {
        try {
            Class<?> environmentcls = Class.forName("android.os.Environment");
            Method setUserRequiredM = environmentcls.getMethod("setUserRequired", boolean.class);
            setUserRequiredM.invoke(null, false);


            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logger.log("Get external path exception", e, LogType.PREFS);
        }

        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void updateProtection() {
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + MiscSettings.class.getPackage().getName() + "/shared_prefs/" + MiscSettings.class.getPackage().getName()
                + "_preferences" + ".xml");

        if (prefsFile.exists())
            prefsFile.setReadable(true, false);
    }

    public enum Prefs {
        // FORMAT: PREF_NAME("pref_key", defaultvalue)
        // Default value cannot be null as it's used to determine the preference type
        // You could add a third TYPE field which you can use to check against in the InitialiseListener method
        CUSTOM_FILTER("pref_key_custom_filter_checkbox", false),
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
        DEBUGGING("pref_key_debug", BuildConfig.BUILD_TYPE.toLowerCase() == "debug"),
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
        STEALTH_VIEWING("pref_key_viewed", false),
        STEALTH_CHAT_SAVING("pref_key_stealth_chat_saving", false),
        STEALTH_NOTIFICATIONS("pref_key_stealth_notification", false),
        HIDE_TYPING_AND_PRESENCE("pref_key_typing", false),
        UNLIM_GROUPS("pref_key_groups_unlim", false),
        SELECT_ALL("pref_key_selectall", false),
        HIDE_BF("pref_key_hidebf", false),
        TIMER_COUNTER("pref_key_timercounter", false),
        FLASH_KEY("pref_key_flashkey", true),
        CHAT_AUTO_SAVE("pref_key_save_chat_text", false),
        CHAT_MEDIA_SAVE("pref_key_save_chat_image", false),
        INTEGRATION("pref_key_integration", true),
        BUTTON_POSITION("pref_key_save_button_position", false),
        BUTTON_OPACITY("pref_key_save_button_opacity", 100),
        BUTTON_RESIZE("pref_key_save_button_opacity_resize", false),
        LENSES_LOAD("pref_key_load_lenses", true),
        LENSES_COLLECT("pref_key_collect_lenses", true),
        LENSES_AUTO_ENABLE("pref_key_auto_enable_lenses", false),
        LENSES_FORCED("pref_key_forced_lenses", true),
        LENSES_SORT_BY_SEL("pref_key_sort_by_sel", true),
        LENSES_HIDE_CURRENTLY_PROVIDED_SC_LENSES("pref_key_hide_curr_provided_sc_lenses", false),
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
        AUTO_ADVANCE("pref_key_auto_advance", false),
        CHAT_LOGGING("pref_key_chat_logging", true),
        GROUPS("pref_key_groups", true),
        KILL_SC_ON_PREF_CHANGE("pref_key_kill_sc", true),

        VFILTER_AMARO("AMARO", false),
        VFILTER_F1997("F1997", false),
        VFILTER_BRANNAN("BRANNAN", false),
        VFILTER_EARLYBIRD("EARLYBIRD", true),
        VFILTER_HEFE("HEFE", false),
        VFILTER_HUDSON("HUDSON", false),
        VFILTER_INKWELL("INKWELL", false),
        VFILTER_LOMO("LOMO", true),
        VFILTER_LORD_KELVIN("LORD_KELVIN", false),
        VFILTER_NASHVILLE("NASHVILLE", false),
        VFILTER_RISE("RISE", true),
        VFILTER_SIERRA("SIERRA", false),
        VFILTER_SUTRO("SUTRO", false),
        VFILTER_TOASTER("TOASTER", true),
        VFILTER_VALENCIA("VALENCIA", false),
        VFILTER_WALDEN("WALDEN", false),
        VFILTER_XPROLL("XPROLL", false),

        // String based values requiring save paths must have a function to do so
        //
        SAVE_PATH("pref_key_save_location", null),
        CUSTOM_FILTER_LOCATION("", null),
        CONFIRMATION_ID("confirmation_id", ""),
        DEVICE_ID("device_id", ""),
        PREF_KEY_SAVE_LOCATION("pref_key_save_location", ""),
        PREF_KEY_HIDE_LOCATION("pref_key_hide_location", ""),

        SAVEMODE_SNAP("pref_key_save", SAVE_BUTTON),
        SAVEMODE_STORY("pref_key_save_story", SAVE_BUTTON),
        TOAST_LENGTH("pref_key_toasts_duration", TOAST_LENGTH_LONG),
        TIMER_MINIMUM("pref_key_timer_minimum", TIMER_MINIMUM_DISABLED),
        MAX_RECORDING_TIME("pref_key_max_recording_time", "10"),
        FORCE_NAVBAR("pref_key_forcenavbar", 0),
        CUSTOM_FILTER_TYPE("pref_key_filter_type", 0),
        LICENCE(DEVICE_ID.key, 0),
        ROTATION_MODE("pref_rotation", Common.ROTATION_CW),
        ADJUST_METHOD("pref_adjustment", Common.ADJUST_CROP),
        LENS_SELECTOR_SPAN("pref_lens_span", 4);

        public String key;
        public Object defaultVal;

        Prefs(String key, Object defaultVal) {
            this.key = key;
            this.defaultVal = defaultVal;
        }

        public static Prefs getPrefFromKey(String key) {
            for (Prefs pref : Prefs.values()) {
                if (pref.key.equals(key))
                    return pref;
            }

            return null;
        }
    }
}