package com.marz.snapprefs;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
    public static int mModeSave = SAVE_AUTO;
    public static int mModeStory = SAVE_BUTTON;
    public static int mToastLength = TOAST_LENGTH_LONG;
    public static int mTimerMinimum = TIMER_MINIMUM_DISABLED;
    public static int mForceNavbar = 0;
    public static boolean mCustomFilterBoolean = false;
    public static boolean mPaintTools = true;
    public static boolean mMultiFilterBoolean = true;
    public static int mCustomFilterType;
    public static boolean mTimerUnlimited = true;
    public static boolean mHideTimerStory = false;
    public static boolean mLoopingVids = true;
    public static boolean mHideTimer = false;
    public static boolean mToastEnabled = true;
    public static boolean mVibrationEnabled = true;
    public static String mSavePath = getExternalPath() + "/Snapprefs";
    public static String mCustomFilterLocation = "";
    public static String mConfirmationID = "";
    public static String mDeviceID = "";
    public static boolean mSaveSentSnaps = false;
    public static boolean mSortByCategory = true;
    public static boolean mSortByUsername = true;
    public static boolean mDebugging = true;
    public static boolean mOverlays = false;
    public static boolean mSpeed = false;
    public static boolean mWeather = false;
    public static boolean mStoryPreload = false;
    public static boolean mDiscoverSnap = false;
    public static boolean mDiscoverUI = false;
    public static boolean mCustomSticker = false;
    public static boolean mHideLive = false;
    public static boolean mHidePeople = false;
    public static boolean mReplay = false;
    public static boolean mStealth = false;
    public static boolean mTyping = false;
    public static boolean mUnlimGroups = false;
    public static int mLicense = 0;
    public static boolean mLocation;
    public static boolean selectAll;
    public static boolean hideBf;
    public static boolean shouldAddGhost;
    public static boolean mTimerCounter;
    public static boolean mChatAutoSave;
    public static boolean mChatMediaSave;
    public static boolean mIntegration;
    public static boolean latest = false;
    public static boolean mButtonPosition = false;
    public static boolean mLoadLenses = true;
    public static boolean mCollectLenses = true;
    public static boolean acceptedToU = true;
    static XSharedPreferences prefs;
    static XSharedPreferences license;
    static boolean selectStory;
    static boolean selectVenue;
    static boolean mTextTools;
    static boolean debug;
    private static boolean fullCaption;
    private static boolean hideRecent;
    private static boolean shouldAddVFilters;
    private static PreferenceParser preferenceParser;

    static void refreshPreferences() {
        /*if (preferenceParser == null) {
            preferenceParser = new PreferenceParser(Environment.getDataDirectory() + "/data/" +
                    HookMethods.PACKAGE_NAME + "/shared_prefs/" + HookMethods.PACKAGE_NAME + "_preferences.xml");
        }*/

        prefs = new XSharedPreferences(HookMethods.PACKAGE_NAME, HookMethods.PACKAGE_NAME + "_preferences");

        prefs.reload();

        try {
            int spinCount = 0;
            Field field = XSharedPreferences.class.getDeclaredField("mLoaded");
            field.setAccessible(true);
            boolean mLoaded;

            do {
                Log.d("snapchat", "spinning" + (++spinCount));

                if (spinCount > 35000)
                    break;

                field.setAccessible(true);
                mLoaded = (boolean) field.get(prefs);
            } while (!mLoaded);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            refreshSelectedPreferences(prefs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HookedLayouts.refreshButtonPreferences();
    }

    public static void refreshSelectedPreferences(Object sharedPreferences) {
        SharedPreferences prefs = (SharedPreferences) sharedPreferences;

        acceptedToU = true;
        selectAll = prefs.getBoolean("pref_key_selectall", false);
        selectStory = prefs.getBoolean("pref_key_selectstory", false);
        selectVenue = prefs.getBoolean("pref_key_selectvenue", false);
        hideBf = prefs.getBoolean("pref_key_hidebf", false);
        hideRecent = prefs.getBoolean("pref_key_hiderecent", false);
        mTextTools = prefs.getBoolean("pref_key_text", false);
        mPaintTools = prefs.getBoolean("pref_key_paint_checkbox", mPaintTools);
        mTimerCounter = prefs.getBoolean("pref_key_timercounter", true);
        mChatAutoSave = prefs.getBoolean("pref_key_save_chat_text", true);
        mChatMediaSave = prefs.getBoolean("pref_key_save_chat_image", true);
        mIntegration = prefs.getBoolean("pref_key_integration", true);
        mCustomFilterBoolean = prefs.getBoolean("pref_key_custom_filter_checkbox", mCustomFilterBoolean);
        mMultiFilterBoolean = prefs.getBoolean("pref_key_multi_filter_checkbox", mMultiFilterBoolean);
        mCustomFilterLocation = getExternalPath() + "/Snapprefs/Filters";
        mCustomFilterType = prefs.getInt("pref_key_filter_type", 0);
        mSpeed = prefs.getBoolean("pref_key_speed", false);
        mWeather = prefs.getBoolean("pref_key_weather", false);
        mLocation = prefs.getBoolean("pref_key_location", false);
        mStoryPreload = prefs.getBoolean("pref_key_storypreload", false);
        mDiscoverSnap = prefs.getBoolean("pref_key_discover", false);
        mDiscoverUI = prefs.getBoolean("pref_key_discover_ui", false);
        mCustomSticker = prefs.getBoolean("pref_key_sticker", false);
        mHideLive = prefs.getBoolean("pref_key_hidelive", false);
        mHidePeople = prefs.getBoolean("pref_key_hidepeople", false);
        mReplay = prefs.getBoolean("pref_key_replay", false);
        mStealth = prefs.getBoolean("pref_key_viewed", false);
        mTyping = prefs.getBoolean("pref_key_typing", false);
        mUnlimGroups = prefs.getBoolean("pref_key_groups_unlim", false);
        mForceNavbar = prefs.getInt("pref_key_forcenavbar", 0);
        mConfirmationID = prefs.getString("confirmation_id", "");
        debug = prefs.getBoolean("pref_key_debug", false);
        mDeviceID = prefs.getString("device_id", null);
        mLicense = prefs.getInt(mDeviceID, 0);

        //SAVING

        mModeSave = prefs.getInt("pref_key_save", mModeSave);
        mModeStory = prefs.getInt("pref_key_save_story", mModeStory);
        mTimerMinimum = prefs.getInt("pref_key_timer_minimum", mTimerMinimum);
        mToastEnabled = prefs.getBoolean("pref_key_toasts_checkbox", mToastEnabled);
        mVibrationEnabled = prefs.getBoolean("pref_key_vibration_checkbox", mVibrationEnabled);
        mToastLength = prefs.getInt("pref_key_toasts_duration", mToastLength);
        mSavePath = prefs.getString("pref_key_save_location", mSavePath);
        mSaveSentSnaps = prefs.getBoolean("pref_key_save_sent_snaps", mSaveSentSnaps);
        mSortByCategory = prefs.getBoolean("pref_key_sort_files_mode", mSortByCategory);
        mSortByUsername = prefs.getBoolean("pref_key_sort_files_username", mSortByUsername);
        mDebugging = prefs.getBoolean("pref_key_debug_mode", mDebugging);
        mOverlays = prefs.getBoolean("pref_key_overlay", mOverlays);
        mTimerUnlimited = prefs.getBoolean("pref_key_timer_unlimited", mTimerUnlimited);
        mHideTimerStory = prefs.getBoolean("pref_key_timer_story_hide", mHideTimerStory);
        mLoopingVids = prefs.getBoolean("pref_key_looping_video", mLoopingVids);
        mHideTimer = prefs.getBoolean("pref_key_timer_hide", mHideTimer);
        mButtonPosition = prefs.getBoolean("pref_key_save_button_position", mButtonPosition);
        mLoadLenses = prefs.getBoolean("pref_key_load_lenses", mLoadLenses);
        mCollectLenses = prefs.getBoolean("pref_key_collect_lenses", mCollectLenses);


        //SHARING

        Common.ROTATION_MODE = Integer.parseInt(prefs.getString("pref_rotation", Integer.toString(Common.ROTATION_MODE)));
        Common.ADJUST_METHOD = Integer.parseInt(prefs.getString("pref_adjustment", Integer.toString(Common.ADJUST_METHOD)));
        Common.CAPTION_UNLIMITED_VANILLA = prefs.getBoolean("pref_caption_unlimited_vanilla", Common.CAPTION_UNLIMITED_VANILLA);
        Common.CAPTION_UNLIMITED_FAT = prefs.getBoolean("pref_caption_unlimited_fat", Common.CAPTION_UNLIMITED_FAT);
        Common.DEBUGGING = prefs.getBoolean("pref_debug", Common.DEBUGGING);
        Common.CHECK_SIZE = !prefs.getBoolean("pref_size_disabled", !Common.CHECK_SIZE);
        Common.TIMBER = prefs.getBoolean("pref_timber", Common.TIMBER);

        shouldAddGhost = mSpeed || mTextTools || mLocation || mWeather;

        Log.d("snapchat", "CONTAINS ACCEPTED: " + prefs.contains("acceptedToU"));
        Log.d("snapchat", "Loading lenses: " + mLoadLenses);
        Log.d("snapchat", "Collecting lenses: " + mCollectLenses);

        printSettings();
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(settingKey, state);
        editor.apply();

        boolean newstate = sharedPreferences.getBoolean(settingKey, mLoadLenses);
        Log.d("snapchat", "Updated preference boolean: " + settingKey + " to " + newstate);
    }

    public static void printSettings() {

        Logger.log("\nTo see the advanced output enable debugging mode in the Support tab", true);

        Logger.setDebuggingEnabled(true);
        Logger.log("\n~~~~~~~~~~~~ SNAPPREFS SETTINGS");
        Logger.log("SelectAll: " + selectAll);
        Logger.log("SelectStory: " + selectStory);
        Logger.log("SelectVenue: " + selectVenue);
        Logger.log("HideBF: " + hideBf);
        Logger.log("HideRecent: " + hideRecent);
        Logger.log("ShouldAddGhost: " + shouldAddGhost);
        Logger.log("mTextTools: " + mTextTools);
        Logger.log("mTimerCounter: " + mTimerCounter);
        Logger.log("mChatAutoSave: " + mChatAutoSave);
        Logger.log("mChatMediaSave: " + mChatMediaSave);
        Logger.log("mIntegration: " + mIntegration);
        Logger.log("mPaintTools: " + mPaintTools);
        Logger.log("CustomFilters: " + mCustomFilterBoolean);
        Logger.log("MultiFilters: " + mMultiFilterBoolean);
        Logger.log("CustomFiltersLocation: " + mCustomFilterLocation);
        Logger.log("CustomFilterType: " + mCustomFilterType);
        Logger.log("mSpeed: " + mSpeed);
        Logger.log("mWeather: " + mWeather);
        Logger.log("mLocation: " + mLocation);
        Logger.log("mStoryPreload: " + mStoryPreload);
        Logger.log("mDiscoverSnap: " + mDiscoverSnap);
        Logger.log("mDiscoverUI: " + mDiscoverUI);
        Logger.log("mCustomSticker: " + mCustomSticker);
        Logger.log("mHideLive: " + mHideLive);
        Logger.log("mHidePeople: " + mHidePeople);
        Logger.log("mReplay: " + mReplay);
        Logger.log("mStealth: " + mStealth);
        Logger.log("mTyping: " + mTyping);
        Logger.log("mUnlimGroups: " + mUnlimGroups);
        Logger.log("mForceNavbar: " + mForceNavbar);
        Logger.log("*****Debugging: " + debug + " *****");
        Logger.log("mLicense: " + mLicense);
        Logger.log("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        Logger.log("----------------------- SAVING SETTINGS -----------------------");
        Logger.log("Preferences have changed:");
        String[] saveModes = {"SAVE_BUTTON", "SAVE_S2S", "DO_NOT_SAVE", "SAVE_AUTO"};
        Logger.log("~ mModeSave: " + saveModes[mModeSave]);
        Logger.log("~ mModeStory: " + saveModes[mModeSave]);
        Logger.log("~ mOverlays: " + mOverlays);
        Logger.log("~ mTimerMinimum: " + mTimerMinimum);
        Logger.log("~ mToastEnabled: " + mToastEnabled);
        Logger.log("~ mVibrationEnabled: " + mVibrationEnabled);
        Logger.log("~ mToastLength: " + mToastLength);
        Logger.log("~ mSavePath: " + mSavePath);
        Logger.log("~ mSaveSentSnaps: " + mSaveSentSnaps);
        Logger.log("~ mSortByCategory: " + mSortByCategory);
        Logger.log("~ mSortByUsername: " + mSortByUsername);
        Logger.log("~ mTimerUnlimited: " + mTimerUnlimited);
        Logger.log("~ mHideTimerStory: " + mHideTimerStory);
        Logger.log("~ mLoopingVids: " + mLoopingVids);
        Logger.log("~ mHideTimer: " + mHideTimer);
        Logger.log("~ acceptedToU: " + acceptedToU);
    }

    public static class PreferenceParser {
        private HashMap<String, Object> preferenceMap = new HashMap<>();
        private String path;
        private DocumentBuilderFactory builderFactory;

        public PreferenceParser(String path) {
            this.path = path;
            this.builderFactory = DocumentBuilderFactory.newInstance();
            preferenceMap = new HashMap<>();
        }

        public void loadPreferences() throws FileNotFoundException {
            preferenceMap = new HashMap<>();
            File prefsFile = new File(path);
            Log.d("snapchat", "Loading preferences");

            if (!prefsFile.exists()) {
                Log.d("snapchat", "File dosn't exist: " + path);
                return;
            }

            try {

                //Using factory get an instance of document builder
                DocumentBuilder db = builderFactory.newDocumentBuilder();

                //parse using builder to get DOM representation of the XML file
                Document preferenceDocument = db.parse(prefsFile);

                Element mainElement = preferenceDocument.getDocumentElement();
                Log.d("snapchat", "Building boolean preferences");
                buildBooleanList(mainElement);
                Log.d("snapchat", "Building integer preferences");
                buildIntList(mainElement);
                Log.d("snapchat", "Building string preferences");
                buildStringList(mainElement);
            } catch (ParserConfigurationException pce) {
                pce.printStackTrace();
            } catch (SAXException se) {
                se.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public void buildBooleanList(Element mainElement) {
            NodeList nodeList = mainElement.getElementsByTagName("boolean");

            if (nodeList != null) {
                Log.d("snapchat", "Nodelist null");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Log.d("snapchat", "Looping nodelst");
                    Element prefElement = (Element) nodeList.item(i);
                    String prefKey = prefElement.getAttribute("name");
                    String strValue = prefElement.getAttribute("value");
                    boolean prefState = strValue.equalsIgnoreCase("true");
                    preferenceMap.put(prefKey, prefState);
                    Log.d("snapchat", "Found boolean: " + prefKey + " : " + prefState);
                }
            }
        }

        public void buildStringList(Element mainElement) {
            NodeList nodeList = mainElement.getElementsByTagName("string");

            if (nodeList != null) {
                Log.d("snapchat", "Nodelist null");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Log.d("snapchat", "Looping nodelst");
                    Element prefElement = (Element) nodeList.item(i);
                    String prefKey = prefElement.getAttribute("name");
                    String strState = prefElement.getFirstChild().getNodeValue();

                    preferenceMap.put(prefKey, strState);
                    Log.d("snapchat", "Found string: " + prefKey + " : " + strState);
                }
            }
        }

        public void buildIntList(Element mainElement) {
            NodeList nodeList = mainElement.getElementsByTagName("int");

            if (nodeList != null) {
                Log.d("snapchat", "Nodelist null");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Log.d("snapchat", "Looping nodelst");
                    Element prefElement = (Element) nodeList.item(i);
                    String prefKey = prefElement.getAttribute("name");
                    String strState = prefElement.getAttribute("value");

                    try {
                        int parsedStr = Integer.parseInt(strState);
                        preferenceMap.put(prefKey, parsedStr);
                        Log.d("snapchat", "Found int: " + prefKey + " : " + parsedStr);
                    } catch( Exception e)
                    {
                        Log.d("snapchat", "Could not parse value: " + prefKey);
                    }
                }
            }
        }

        public Object getPreference(String key, Object defaultVal) {
            Object prefValue = preferenceMap.get(key);

            if (prefValue == null)
                return defaultVal;

            return prefValue;
        }

        public void closeParser() {
            preferenceMap.clear();
        }

        public HashMap<String, Object> getPreferenceMap() {
            return preferenceMap;
        }
    }
}
