package com.marz.snapprefs;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.marz.snapprefs.Util.XposedUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setObjectField;


public class HookMethods implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {


    public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    // Modes for saving Snapchats
    public static final int SAVE_AUTO = 0;
    public static final int SAVE_S2S = 1;
    public static final int DO_NOT_SAVE = 2;
    // Length of toasts
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;
    // Minimum timer duration disabled
    public static final int TIMER_MINIMUM_DISABLED = 0;
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    // Preferences and their default values
    public static int mModeSnapImage = SAVE_AUTO;
    public static int mModeSnapVideo = SAVE_AUTO;
    public static int mModeStoryImage = SAVE_AUTO;
    public static int mModeStoryVideo = SAVE_AUTO;
    public static int mTimerMinimum = TIMER_MINIMUM_DISABLED;
    public static boolean mTimerUnlimited = true;
    public static boolean mHideTimer = false;
    public static boolean mToastEnabled = true;
    public static int mToastLength = TOAST_LENGTH_LONG;
    public static String mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
    public static boolean mSaveSentSnaps = false;
    public static boolean mSortByCategory = true;
    public static boolean mSortByUsername = true;
    public static boolean mDebugging = true;
    static XSharedPreferences prefs;
    static boolean selectStory;
    static boolean txtcolours;
    static boolean bgcolours;
    static boolean size;
    static boolean transparency;
    static boolean rainbow;
    static boolean bg_transparency;
    static boolean txtstyle;
    static boolean txtgravity;
    static boolean debug;
    static EditText editText;
    static XModuleResources modRes;
    static Context SnapContext;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    private static XModuleResources mResources;
    private static int snapchatVersion;
    private static String MODULE_PATH = null;
    private static boolean fullCaption;
    private static boolean selectAll;
    private static boolean hideBf;
    private static boolean hideRecent;
    private static boolean colours;
    Class CaptionEditText;
    //SNAPSHARE
    private Uri initializedUri;
    private XSharedPreferences sharedPreferences;
    private GestureModel gestureModel;
    private int screenHeight;

    public static int px(float f) {
        return Math.round((f * SnapContext.getResources().getDisplayMetrics().density));
    }

    static void refreshPreferences() {

        prefs = new XSharedPreferences(new File(
                Environment.getDataDirectory(), "data/"
                + PACKAGE_NAME + "/shared_prefs/" + PACKAGE_NAME
                + "_preferences" + ".xml"));
        prefs.reload();
        fullCaption = prefs.getBoolean("pref_key_fulltext", false);
        selectAll = prefs.getBoolean("pref_key_selectall", false);
        selectStory = prefs.getBoolean("pref_key_selectstory", false);
        hideBf = prefs.getBoolean("pref_key_hidebf", false);
        hideRecent = prefs.getBoolean("pref_key_hiderecent", false);
        txtcolours = prefs.getBoolean("pref_key_txtcolour", false);
        bgcolours = prefs.getBoolean("pref_key_bgcolour", false);
        size = prefs.getBoolean("pref_key_size", false);
        transparency = prefs.getBoolean("pref_key_size", false);
        rainbow = prefs.getBoolean("pref_key_rainbow", false);
        bg_transparency = prefs.getBoolean("pref_key_bg_transparency", false);
        txtstyle = prefs.getBoolean("pref_key_txtstyle", false);
        txtgravity = prefs.getBoolean("pref_key_txtgravity", false);
        debug = prefs.getBoolean("pref_key_debug", false);

        //SAVING

        mModeSnapImage = prefs.getInt("pref_key_snaps_images", mModeSnapImage);
        mModeSnapVideo = prefs.getInt("pref_key_snaps_videos", mModeSnapVideo);
        mModeStoryImage = prefs.getInt("pref_key_stories_images", mModeStoryImage);
        mModeStoryVideo = prefs.getInt("pref_key_stories_videos", mModeStoryVideo);
        mTimerMinimum = prefs.getInt("pref_key_timer_minimum", mTimerMinimum);
        mToastEnabled = prefs.getBoolean("pref_key_toasts_checkbox", mToastEnabled);
        mToastLength = prefs.getInt("pref_key_toasts_duration", mToastLength);
        mSavePath = prefs.getString("pref_key_save_location", mSavePath);
        mSaveSentSnaps = prefs.getBoolean("pref_key_save_sent_snaps", mSaveSentSnaps);
        mSortByCategory = prefs.getBoolean("pref_key_sort_files_mode", mSortByCategory);
        mSortByUsername = prefs.getBoolean("pref_key_sort_files_username", mSortByUsername);
        mDebugging = prefs.getBoolean("pref_key_debug_mode", mDebugging);
        mTimerUnlimited = prefs.getBoolean("pref_key_timer_unlimited", mTimerUnlimited);
        mHideTimer = prefs.getBoolean("pref_key_timer_hide", mHideTimer);


        //SHARING

        Common.ROTATION_MODE = Integer.parseInt(prefs.getString("pref_rotation", Integer.toString(Common.ROTATION_MODE)));
        Common.ADJUST_METHOD = Integer.parseInt(prefs.getString("pref_adjustment", Integer.toString(Common.ADJUST_METHOD)));
        Common.CAPTION_UNLIMITED_VANILLA = prefs.getBoolean("pref_caption_unlimited_vanilla", Common.CAPTION_UNLIMITED_VANILLA);
        Common.CAPTION_UNLIMITED_FAT = prefs.getBoolean("pref_caption_unlimited_fat", Common.CAPTION_UNLIMITED_FAT);
        Common.DEBUGGING = prefs.getBoolean("pref_debug", Common.DEBUGGING);
        Common.CHECK_SIZE = !prefs.getBoolean("pref_size_disabled", !Common.CHECK_SIZE);
        Common.TIMBER = prefs.getBoolean("pref_timber", Common.TIMBER);

        if (txtcolours == true || bgcolours == true || size == true || rainbow == true || bg_transparency == true || txtstyle == true || txtgravity == true) {
            colours = true;
        } else {
            colours = false;
        }
    }

    static void logging(String message) {
        if (debug == true)
            XposedBridge.log(message);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mResources = XModuleResources.createInstance(startupParam.modulePath, null);
        refreshPreferences();
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(Common.PACKAGE_SNAP))
            return;

        refreshPreferences();
        try {
            Logger.log("We are trying to get modres", true);
            modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
            Logger.log("Got modRes, trying to replace camera_batteryfilter_full", true);
            resparam.res.setReplacement(resparam.packageName, "drawable", "camera_batteryfilter_full", new XResources.DrawableLoader() {
                public Drawable newDrawable(XResources p1, int p2) throws Throwable {
                    /*Drawable e = Drawable.createFromPath("/sdcard/Snapprefs/Filters/custom_filter.png");
                    return e;*/
                    return new ColorDrawable(Color.WHITE);
                }

                ;
            });
            Logger.log("Replaced filter", true);
        } catch (Throwable t) {
            Logger.log("Error while replacing filter", true);
            Logger.log(t.toString(), true);
        }
        if (colours == true) {
            addGhost(resparam);
        }
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(Common.PACKAGE_SNAP))
            return;
        try {
            XposedUtils.log("----------------- SNAPPREFS HOOKED -----------------", false);
            Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
            Context context = (Context) callMethod(activityThread, "getSystemContext");

            PackageInfo piSnapChat = context.getPackageManager().getPackageInfo(lpparam.packageName, 0);
            XposedUtils.log("SnapChat Version: " + piSnapChat.versionName + " (" + piSnapChat.versionCode + ")", false);
            XposedUtils.log("SnapPrefs Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")", false);

            snapchatVersion = Obfuscator_share.getVersion(piSnapChat.versionCode);
        } catch (Exception e) {
            XposedUtils.log("Exception while trying to get version info", e);
            return;
        }
        prefs.reload();
        refreshPreferences();
        printSettings();
        getEditText(lpparam);/*
        final Class<?> receivedSnapClass = findClass("akc", lpparam.classLoader);
		try{
			XposedHelpers.setStaticIntField(receivedSnapClass, "SECOND_MAX_VIDEO_DURATION", 20);
			Logger.log("SECOND_MAX_VIDEO_DURATION set over 10", true);
		} catch (Throwable t){
			Logger.log("SECOND_MAX_VIDEO_DURATION set over 10 failed :(",true);
			Logger.log(t.toString());
		} For viewing longer videos?*/
        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                prefs.reload();
                refreshPreferences();
                if (SnapContext == null) SnapContext = (Activity) methodHookParam.thisObject;
                SnapContext = (Activity) methodHookParam.thisObject;
                //SNAPPREFS
                Saving.initSaving(lpparam, mResources, SnapContext);
                DataSaving.initMethod(lpparam, mResources, SnapContext);
            }
        });
        // VanillaCaptionEditText was moved from an inner-class to a separate class in 8.1.0
        String vanillaCaptionEditTextClassName = "com.snapchat.android.ui.caption.VanillaCaptionEditText";
        hookAllConstructors(findClass(vanillaCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Common.CAPTION_UNLIMITED_VANILLA) {
                    XposedUtils.log("Unlimited vanilla captions");
                    EditText vanillaCaptionEditText = (EditText) param.thisObject;
                    // Set single lines mode to false
                    vanillaCaptionEditText.setSingleLine(false);

                    // Remove actionDone IME option, by only setting flagNoExtractUi
                    vanillaCaptionEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    // Remove listener hiding keyboard when enter is pressed by setting the listener to null
                    vanillaCaptionEditText.setOnEditorActionListener(null);
                    // Remove listener for cutting of text when the first line is full by setting the text change listeners list to null
                    setObjectField(vanillaCaptionEditText, "mListeners", null);
                }
            }
        });

        // FatCaptionEditText was moved from an inner-class to a separate class in 8.1.0
        String fatCaptionEditTextClassName = "com.snapchat.android.ui.caption.FatCaptionEditText";
        hookAllConstructors(findClass(fatCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Common.CAPTION_UNLIMITED_FAT) {
                    XposedUtils.log("Unlimited fat captions");
                    EditText fatCaptionEditText = (EditText) param.thisObject;
                    // Remove InputFilter with character limit
                    fatCaptionEditText.setFilters(new InputFilter[0]);

                    // Remove actionDone IME option, by only setting flagNoExtractUi
                    fatCaptionEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    // Remove listener hiding keyboard when enter is pressed by setting the listener to null
                    fatCaptionEditText.setOnEditorActionListener(null);
                    // Remove listener for removing new lines by setting the text change listeners list to null
                    setObjectField(fatCaptionEditText, "mListeners", null);
                }
            }
        });


        //SNAPSHARE
        //Sharing.initSharing(lpparam, mResources);
        //KEEPCHAT
        //Saving.initSaving(lpparam, mResources);
        //SNAPPREFS

		/*findAndHookMethod(Common.Class_ScreenshotDetector, lpparam.classLoader, Common.Method_DetectionSession, List.class, long.class, XC_MethodReplacement.returnConstant(null));
        logging("Snap Prefs: Not reporting screenshots. DetectionSession");

		Constructor<?> ss;
		ss = findConstructorExact(Common.Class_Screenshot1, lpparam.classLoader);
		XposedBridge.hookMethod(ss, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				logging("Snap Prefs: Not reporting screenshots. SC1");
				return false;
			}
		});
		findAndHookMethod(Common.Class_Screenshot2, lpparam.classLoader, Common.Method_Screenshot2, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				logging("Snap Prefs: Not reporting screenshots. SC2");
				return false;
			}
		});
		findAndHookMethod(Common.Class_Screenshot2, lpparam.classLoader, Common.Method_Screenshot3, boolean.class , new XC_MethodReplacement(){
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				logging("Snap Prefs: Not reporting screenshots. SC3");
				return false;
			}
		});

		if (hideBf == true){
			findAndHookMethod(Common.Class_Friend, lpparam.classLoader, Common.Method_BestFriend, new XC_MethodReplacement(){
		@Override
		protected Object replaceHookedMethod(MethodHookParam param)
				throws Throwable {
			logging("Snap Prefs: Removing Best-friends");
			return false;
        }
		});
		}

		if (hideRecent == true){
		findAndHookMethod(Common.Class_Friend, lpparam.classLoader, Common.Method_Recent, new XC_MethodReplacement(){
		@Override
		protected Object replaceHookedMethod(MethodHookParam param)
				throws Throwable {
			logging("Snap Prefs: Removing Recents");
			return false;
        }
		});
		}*/
        if (selectAll == true) {
            HookSendList.initSelectAll(lpparam);
        }
    }

    private void printSettings() {

        Logger.log("\nTo see the advanced output enable debugging mode in the Support tab", true);

        logging("\n~~~~~~~~~~~~ SNAPPREFS SETTINGS");
        logging("FullCaption: " + fullCaption);
        logging("SelectAll: " + selectAll);
        logging("SelectStory: " + selectStory);
        logging("HideBF: " + hideBf);
        logging("HideRecent: " + hideRecent);
        logging("Colours: " + colours);
        logging("TxtColours: " + txtcolours);
        logging("BgColours: " + bgcolours);
        logging("Size: " + size);
        logging("Transparency: " + transparency);
        logging("Rainbow: " + rainbow);
        logging("Background Transparency: " + bg_transparency);
        logging("TextStyle: " + txtstyle);
        logging("TextGravity: " + txtgravity);
        logging("*****Debugging: " + debug + " *****");
        logging("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Logger.setDebuggingEnabled(mDebugging);

        logging("----------------------- SAVING SETTINGS -----------------------");
        logging("Preferences have changed:");
        String[] saveModes = {"SAVE_AUTO", "SAVE_S2S", "DO_NOT_SAVE"};
        logging("~ mModeSnapImage: " + saveModes[mModeSnapImage]);
        logging("~ mModeSnapVideo: " + saveModes[mModeSnapVideo]);
        logging("~ mModeStoryImage: " + saveModes[mModeStoryImage]);
        logging("~ mModeStoryVideo: " + saveModes[mModeStoryVideo]);
        logging("~ mTimerMinimum: " + mTimerMinimum);
        logging("~ mToastEnabled: " + mToastEnabled);
        logging("~ mToastLength: " + mToastLength);
        logging("~ mSavePath: " + mSavePath);
        logging("~ mSaveSentSnaps: " + mSaveSentSnaps);
        logging("~ mSortByCategory: " + mSortByCategory);
        logging("~ mSortByUsername: " + mSortByUsername);
        logging("~ mTimerUnlimited: " + mTimerUnlimited);
        logging("~ mHideTimer: " + mHideTimer);
    }

    public void getEditText(LoadPackageParam lpparam) {
        this.CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.caption.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(this.CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws PackageManager.NameNotFoundException {
                refreshPreferences();
                editText = (EditText) param.thisObject;
            }
        });
    }

    public void addGhost(InitPackageResourcesParam resparam) {
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "snap_preview", new XC_LayoutInflated() {
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                RelativeLayout relativeLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_decor_relative_layout", "id", Common.PACKAGE_SNAP));
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.topMargin = px(3.0f);
                layoutParams.leftMargin = px(55.0f);
                ImageButton ghost = new ImageButton(SnapContext);
                ghost.setBackgroundColor(0);
                ghost.setImageDrawable(mResources.getDrawable(R.drawable.triangle));
                ghost.setScaleX((float) 0.75);
                ghost.setScaleY((float) 0.75);
                ghost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.MainDialog(SnapContext, editText);
                        logging("SnapPrefs: Displaying MainDialog");
                    }
                });
                relativeLayout.addView(ghost, layoutParams);
            }
        });
    }
}