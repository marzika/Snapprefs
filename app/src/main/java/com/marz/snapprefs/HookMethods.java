package com.marz.snapprefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.marz.snapprefs.Util.XposedUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;


public class HookMethods
        implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    public static Activity SnapContext;
    public static String MODULE_PATH = null;
    public static ClassLoader classLoader;
    static EditText editText;
    static Typeface defTypeface;
    static boolean haveDefTypeface;
    static XModuleResources modRes;
    static Context context;
    static int counter = 0;
    private static XModuleResources mResources;
    private static int snapchatVersion;
    private static InitPackageResourcesParam resParam;
    Class CaptionEditText;
    boolean latest = false;

    public static int px(float f) {
        return Math.round((f * SnapContext.getResources().getDisplayMetrics().density));
    }

    public static void printStackTraces() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            Logger.log("Class name :: " + element.getClassName() + "  || method name :: " +
                    element.getMethodName());
        }
    }

    public boolean postData() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://snapprefs.com/checkversion.php");


        try {
            // Add your data
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("version", "1.5.0"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            final ByteArrayBuffer baf = new ByteArrayBuffer(20);

            int current;

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            String text = new String(baf.toByteArray());
            String status;
            String error_msg;
            try {

                JSONObject obj = new JSONObject(text);
                status = obj.getString("status");
                error_msg = obj.getString("error_msg");
                if (status.equals("0") && !error_msg.isEmpty()) {
                    latest = true;
                }
                if (status.equals("1") && error_msg.isEmpty()) {
                    //Toast.makeText(SnapContext, "New version available, update NOW from the Xposed repo.", Toast.LENGTH_SHORT).show();
                    latest = false;
                }
            } catch (Throwable t) {
                Log.e("Snapprefs", "Could not parse malformed JSON: \"" + text + "\"");
                latest = false;
            }
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            //saveIntPreference("license_status", 0);
            latest = false;
        }

        return latest;
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mResources = XModuleResources.createInstance(startupParam.modulePath, null);
        //refreshPreferences();
        File prefsFile = new File(
                Environment.getDataDirectory(), "data/"
                + HookMethods.PACKAGE_NAME + "/shared_prefs/" + HookMethods.PACKAGE_NAME
                + "_preferences" + ".xml");
        prefsFile.setReadable(true, false);
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        try {
            if (!resparam.packageName.equals(Common.PACKAGE_SNAP)) {
                Logger.log("Skipping resource hooks: Same target package");
                return;
            }

            int name = R.id.name;
            int checkBox = R.id.checkBox;
            int friend_item = R.layout.friend_item;
            int group_item = R.layout.group_item;

            modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);

            FriendListDialog.name = XResources.getFakeResId(modRes, name);
            resparam.res.setReplacement(FriendListDialog.name, modRes.fwd(name));

            FriendListDialog.checkBox = XResources.getFakeResId(modRes, checkBox);
            resparam.res.setReplacement(FriendListDialog.checkBox, modRes.fwd(checkBox));

            FriendListDialog.friend_item = XResources.getFakeResId(modRes, checkBox);
            resparam.res.setReplacement(FriendListDialog.friend_item, modRes.fwd(friend_item));

            GroupDialog.group_item = XResources.getFakeResId(modRes, group_item);
            resparam.res.setReplacement(GroupDialog.group_item, modRes.fwd(group_item));

            //mSavePath = Preferences.getExternalPath().getAbsolutePath() + "/Snapprefs";
            //mCustomFilterLocation = Preferences.getExternalPath().getAbsolutePath() + "/Snapprefs/Filters";
            Preferences.refreshPreferences();
            resParam = resparam;

            Object activityThread =
                    callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
            Context localContext = (Context) callMethod(activityThread, "getSystemContext");

            // TODO Set up removal of button when mode is changed
            // Currently requires snapchat to restart to remove the button
            HookedLayouts.addSaveButtonsAndGestures(resparam, mResources, localContext);

            if (Preferences.shouldAddGhost) {
                HookedLayouts.addIcons(resparam, mResources);
            }
            if (Preferences.mIntegration) {
                HookedLayouts.addShareIcon(resparam);
            }
            if (Preferences.mHidePeople) {
                Stories.addSnapprefsBtn(resparam, mResources);
            }

            //Chat.initChatSave(resparam, mResources);
            HookedLayouts.fullScreenFilter(resparam);
        } catch (Exception e) {
            Logger.log("Exception thrown in handleInitPackageResources", e);
        }
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        try {
            if (!lpparam.packageName.equals(Common.PACKAGE_SNAP))
                return;

            try {
                Preferences.mSavePath =
                        Preferences.getExternalPath() + "/Snapprefs";
                Preferences.mCustomFilterLocation =
                        Preferences.getExternalPath() +
                                "/Snapprefs/Filters";
                XposedUtils.log("----------------- SNAPPREFS HOOKED -----------------", false);
                Object activityThread =
                        callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
                context = (Context) callMethod(activityThread, "getSystemContext");
                classLoader = lpparam.classLoader;

                PackageInfo piSnapChat =
                        context.getPackageManager().getPackageInfo(lpparam.packageName, 0);
                XposedUtils.log(
                        "SnapChat Version: " + piSnapChat.versionName + " (" +
                                piSnapChat.versionCode +
                                ")", false);
                XposedUtils.log("SnapPrefs Version: " + BuildConfig.VERSION_NAME + " (" +
                        BuildConfig.VERSION_CODE + ")", false);
                if (!Obfuscator.isSupported(piSnapChat.versionCode)) {
                    Logger.log("This Snapchat version is unsupported", true, true);
                    Toast.makeText(context, "This Snapchat version is unsupported", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                XposedUtils.log("Exception while trying to get version info", e);
                return;
            }
            findAndHookMethod("android.app.Application", lpparam.classLoader, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Preferences.refreshPreferences();
                    Logger.log("makeWorldReadable: " + Preferences.prefs.makeWorldReadable(), true);
                    Preferences.printSettings();
                    if (Preferences.mLicense == 1 || Preferences.mLicense == 2) {

                        if (Preferences.mReplay) {
                            //Premium.initReplay(lpparam, modRes, SnapContext);
                        }
                        if (Preferences.mTyping) {
                            Premium.initTyping(lpparam, modRes, SnapContext);
                        }
                        if (Preferences.mStealth && Preferences.mLicense == 2) {
                            Premium.initViewed(lpparam, modRes, SnapContext);
                        }
                    }
                    Preferences.prefs.reload();
                    Preferences.refreshPreferences();

                    XC_MethodHook initHook = new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Preferences.prefs.reload();
                            Preferences.refreshPreferences();
                            Preferences.prefs.makeWorldReadable();
                            SnapContext = (Activity) param.thisObject;
                            if (!Preferences.acceptedToU) {//new ContextThemeWrapper(context.createPackageContext("com.marz.snapprefs", Context.CONTEXT_IGNORE_SECURITY), R.style.AppCompatDialog)
                                AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext)
                                        .setTitle("ToU and Privacy Policy")
                                        .setMessage("You haven't accepted our Terms of Use and Privacy. Please read it carefully and accept it, otherwise you will not be able to use our product. Open the Snapprefs app to do that.")
                                        .setIcon(android.R.drawable.ic_dialog_alert);
                                builder.setCancelable(false);
                                final AlertDialog dialog = builder.create();
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.show();
                                return;
                            }
                            boolean isNull;
                            isNull = SnapContext == null;
                            Logger.log("SNAPCONTEXT, NULL? - " + isNull, true);
                            Preferences.prefs.reload();
                            Preferences.refreshPreferences();
                            //SNAPPREFS
                            Saving.initSaving(lpparam, mResources, SnapContext);
                            //NewSaving.initSaving(lpparam);
                            Lens.initLens(lpparam, mResources, SnapContext);
                            File vfilters = new File(
                                    Preferences.getExternalPath() +
                                            "/Snapprefs/VisualFilters/xpro_map.png");
                            if (vfilters.exists()) {
                                VisualFilters.initVisualFilters(lpparam);
                            } else {
                                Toast.makeText(context, "VisualFilter files are missing, download them!", Toast.LENGTH_SHORT).show();
                            }
                            if (Preferences.mHideLive || Preferences.mHidePeople ||
                                    Preferences.mDiscoverUI) {
                                Stories.initStories(lpparam);
                            }
                            Groups.initGroups(lpparam);
                            if (Preferences.shouldAddGhost) {
                                HookedLayouts.initVisiblity(lpparam);
                            }
                            if (Preferences.mMultiFilterBoolean) {
                                MultiFilter.initMultiFilter(lpparam, mResources, SnapContext);
                            }
                            if (Preferences.mDiscoverSnap) {
                                DataSaving.blockDsnap(lpparam);
                            }
                            if (Preferences.mStoryPreload) {
                                DataSaving.blockStoryPreLoad(lpparam);
                            }
                            if (Preferences.mDiscoverUI) {
                                DataSaving.blockFromUi(lpparam);
                            }
                            if (Preferences.mSpeed) {
                                Spoofing.initSpeed(lpparam, SnapContext);
                            }
                            if (Preferences.mLocation) {
                                Spoofing.initLocation(lpparam, SnapContext);
                            }
                            if (Preferences.mWeather) {
                                Spoofing.initWeather(lpparam, SnapContext);
                            }
                            if (Preferences.mPaintTools) {
                                PaintTools.initPaint(lpparam, mResources);
                            }
                            if (Preferences.mTimerCounter) {
                                Misc.initTimer(lpparam, mResources);
                            }
                            if (Preferences.mChatAutoSave) {
                                Chat.initTextSave(lpparam, mResources);
                            }
                            if (Preferences.mChatMediaSave) {
                                Chat.initImageSave(lpparam, mResources);
                            }
                            if (Preferences.mIntegration) {
                                HookedLayouts.initIntegration(lpparam, mResources);
                            }
                            Misc.forceNavBar(lpparam, Preferences.mForceNavbar);
                            getEditText(lpparam);
                            findAndHookMethod(Obfuscator.save.SCREENSHOTDETECTOR_CLASS, lpparam.classLoader, Obfuscator.save.SCREENSHOTDETECTOR_RUN, LinkedHashMap.class, XC_MethodReplacement.DO_NOTHING);
                            findAndHookMethod(Obfuscator.save.SNAPSTATEMESSAGE_CLASS, lpparam.classLoader, Obfuscator.save.SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT, Long.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    param.args[0] = 0L;
                                    Logger.log("StateBuilder.setScreenshotCount set to 0L", true);
                                }
                            });
                            if (Preferences.mCustomSticker) {
                                Stickers.initStickers(lpparam, modRes, SnapContext);
                            }
                        }
                    };

                    findAndHookMethod("com.snapchat.android.LandingPageActivity",
                            lpparam.classLoader, "onCreate", Bundle.class, initHook);
                    findAndHookMethod("com.snapchat.android.LandingPageActivity",
                            lpparam.classLoader, "onResume", initHook);

        /*findAndHookMethod("com.snapchat.android.Timber", lpparam.classLoader, "c", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("TIMBER: " + param.args[0] + " : " + param.args[1], true);
            }
        });*/
                    //Showing lenses or not
                    findAndHookMethod(Obfuscator.icons.ICON_HANDLER_CLASS, lpparam.classLoader, Obfuscator.icons.SHOW_LENS, boolean.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if ((boolean) param.args[0]) {
                                HookedLayouts.upload.setVisibility(View.INVISIBLE);
                            } else {
                                HookedLayouts.upload.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    //Recording of video ended
                    findAndHookMethod(Obfuscator.icons.ICON_HANDLER_CLASS, lpparam.classLoader, Obfuscator.icons.RECORDING_VIDEO, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (HookedLayouts.upload != null)
                                HookedLayouts.upload.setVisibility(View.VISIBLE);
                        }
                    });
                    for (String s : Obfuscator.ROOTDETECTOR_METHODS) {
                        findAndHookMethod(Obfuscator.ROOTDETECTOR_CLASS, lpparam.classLoader, s, XC_MethodReplacement.returnConstant(false));
                        Logger.log("ROOTCHECK: " + s, true);
                    }
                    findAndHookMethod("android.media.MediaRecorder", lpparam.classLoader, "setMaxDuration", int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.args[0] = 12000000;
                        }
                    });
                    findAndHookMethod("android.media.MediaRecorder", lpparam.classLoader, "setMaxFileSize", long.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {//1730151
                            param.args[0] = 5190453;//5190453
                        }
                    });

                    final Class<?> receivedSnapClass =
                            findClass(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader);
                    try {
                        XposedHelpers.setStaticIntField(receivedSnapClass, "SECOND_MAX_VIDEO_DURATION", 99999);
                        //Better quality images
                        final Class<?> snapMediaUtils =
                                findClass("com.snapchat.android.util.SnapMediaUtils", lpparam.classLoader);
                        XposedHelpers.setStaticIntField(snapMediaUtils, "IGNORED_COMPRESSION_VALUE", 100);
                        XposedHelpers.setStaticIntField(snapMediaUtils, "RAW_THUMBNAIL_ENCODING_QUALITY", 100);
                        final Class<?> profileImageUtils =
                                findClass("com.snapchat.android.util.profileimages.ProfileImageUtils", lpparam.classLoader);
                        XposedHelpers.setStaticIntField(profileImageUtils, "COMPRESSION_QUALITY", 100);
                        final Class<?> snapImageBryo =
                                findClass(Obfuscator.save.SNAPIMAGEBRYO_CLASS, lpparam.classLoader);
                        XposedHelpers.setStaticIntField(snapImageBryo, "JPEG_ENCODING_QUALITY", 100);
                        Logger.log("Setting static fields", true);
                    } catch (Throwable t) {
                        Logger.log("Setting static fields failed :(", true);
                        Logger.log(t.toString());
                    } /*For viewing longer videos?*/

                    if (Common.CAPTION_UNLIMITED_VANILLA) {
                        findAndHookMethod("com.snapchat.android.ui.caption.CaptionEditText", lpparam.classLoader, "n", XC_MethodReplacement.DO_NOTHING);
                    }
                    // VanillaCaptionEditText was moved from an inner-class to a separate class in 8.1.0
                    String vanillaCaptionEditTextClassName =
                            "com.snapchat.android.ui.caption.VanillaCaptionEditText";
                    hookAllConstructors(findClass(vanillaCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (Common.CAPTION_UNLIMITED_VANILLA) {
                                XposedUtils.log("Unlimited vanilla captions");
                                EditText vanillaCaptionEditText = (EditText) param.thisObject;
                                // Set single lines mode to false
                                vanillaCaptionEditText.setSingleLine(false);
                                vanillaCaptionEditText.setFilters(new InputFilter[0]);
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
                    String fatCaptionEditTextClassName =
                            "com.snapchat.android.ui.caption.FatCaptionEditText";
                    hookAllConstructors(findClass(fatCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
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
                    Sharing.initSharing(lpparam, mResources);
                    //SNAPPREFS
                    if (Preferences.hideBf) {
                        findAndHookMethod("com.snapchat.android.model.Friend", lpparam.classLoader, Obfuscator.FRIENDS_BF, new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) {
                                //logging("Snap Prefs: Removing Best-friends");
                                return false;
                            }
                        });
                    }
        /*if (hideRecent == true){
        findAndHookMethod(Common.Class_Friend, lpparam.classLoader, Common.Method_Recent, new XC_MethodReplacement(){
		@Override
		protected Object replaceHookedMethod(MethodHookParam param)
				throws Throwable {
			logging("Snap Prefs: Removing Recents");
			return false;
        }
		});
		}*/
                    if (Preferences.mCustomFilterBoolean) {
                        addFilter(lpparam);
                    }
                    if (Preferences.selectAll) {
                        HookSendList.initSelectAll(lpparam);
                    }


                }
            });
        } catch (Exception e) {
            Logger.log("Exception thrown in handleLoadPackage", e);
        }
    }


    private void addFilter(LoadPackageParam lpparam) {
        //Replaces the batteryfilter with our custom one
        findAndHookMethod(ImageView.class, "setImageResource", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
                    ImageView iv = (ImageView) param.thisObject;
                    int resId = (Integer) param.args[0];
                    if (iv != null)
                        if (iv.getContext().getPackageName().equals("com.snapchat.android"))
                            if (resId ==
                                    iv.getContext().getResources().getIdentifier("camera_batteryfilter_full", "drawable", "com.snapchat.android"))
                                if (Preferences.mCustomFilterLocation == null) {
                                    iv.setImageDrawable(modRes.getDrawable(R.drawable.custom_filter_1));
                                    Logger.log("Replaced batteryfilter from R.drawable", true);
                                } else {
                                    if (Preferences.mCustomFilterType == 0) {
                                        iv.setImageDrawable(Drawable.createFromPath(
                                                Preferences.mCustomFilterLocation +
                                                        "/fullscreen_filter.png"));
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                    } else if (Preferences.mCustomFilterType == 1) {
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                        iv.setImageDrawable(Drawable.createFromPath(
                                                Preferences.mCustomFilterLocation +
                                                        "/banner_filter.png"));
                                    }
                                    Logger.log(
                                            "Replaced batteryfilter from " +
                                                    Preferences.mCustomFilterLocation +
                                                    " Type: " +
                                                    Preferences.mCustomFilterType, true);
                                }
                    //else if (resId == iv.getContext().getResources().getIdentifier("camera_batteryfilter_empty", "drawable", "com.snapchat.android"))
                    //    iv.setImageDrawable(modRes.getDrawable(R.drawable.custom_filter_1)); quick switch to a 2nd filter?
                } catch (Throwable t) {
                    XposedBridge.log(t);
                }
            }
        });
        //Used to emulate the battery status as being FULL -> above 90%
        final Class<?> batteryInfoProviderEnum =
                findClass("com.snapchat.android.app.shared.model.filter.BatteryLevel", lpparam.classLoader);
        findAndHookMethod(Obfuscator.spoofing.BATTERY_FILTER, lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Object battery = getStaticObjectField(batteryInfoProviderEnum, Obfuscator.spoofing.BATTERY_FULL_ENUM);
                param.setResult(battery);
            }
        });
    }

    public void getEditText(LoadPackageParam lpparam) {
        this.CaptionEditText =
                XposedHelpers.findClass("com.snapchat.android.ui.caption.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(this.CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws PackageManager.NameNotFoundException {
                Preferences.refreshPreferences();
                editText = (EditText) param.thisObject;
                if (!haveDefTypeface) {
                    defTypeface = editText.getTypeface();
                    haveDefTypeface = true;
                }
            }
        });
    }
}
