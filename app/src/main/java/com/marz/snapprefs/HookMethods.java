package com.marz.snapprefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.DebugHelper;
import com.marz.snapprefs.Util.NotificationUtils;
import com.marz.snapprefs.Util.XposedUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;

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
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;


public class HookMethods
        implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    public static Activity SnapContext;
    public static String MODULE_PATH = null;
    public static ClassLoader classLoader;
    public static XModuleResources mResources;
    public static Bitmap saveImg;
    static EditText editText;
    static Typeface defTypeface;
    static boolean haveDefTypeface;
    static XModuleResources modRes;
    static Context context;
    static int counter = 0;
    private static int snapchatVersion;
    private static InitPackageResourcesParam resParam;
    Class CaptionEditText;
    boolean latest = false;

    public static int px(float f) {
        return Math.round((f * SnapContext.getResources().getDisplayMetrics().density));
    }

    public static String getSCUsername(ClassLoader cl) {
        Class scPreferenceHandler = findClass(Obfuscator.misc.PREFERENCES_CLASS, cl);
        try {
            return (String) callMethod(scPreferenceHandler.newInstance(), Obfuscator.misc.GETUSERNAME_METHOD);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void hookAllMethods(String className, ClassLoader cl, boolean hookSubClasses, boolean hookSuperClasses) {
        Log.d("snapprefs", "Starting allhook");
        final Class targetClass = findClass(className, cl);
        Method[] allMethods = targetClass.getDeclaredMethods();

        Log.d("snapprefs", "Methods to hook: " + allMethods.length);
        for (final Method baseMethod : allMethods) {
            final Class<?>[] paramList = baseMethod.getParameterTypes();
            final String fullMethodString = targetClass.getSimpleName() + "." + baseMethod.getName() + "(" + Arrays.toString(paramList) + ") -> " + baseMethod.getReturnType();

            if (Modifier.isAbstract(baseMethod.getModifiers())) {
                Log.d("snapprefs", "Abstract method: " + fullMethodString);
                continue;
            }

            Object[] finalParam = new Object[paramList.length + 1];

            System.arraycopy(paramList, 0, finalParam, 0, paramList.length);

            finalParam[paramList.length] = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.d("snapprefs", "HookTrigger: " + fullMethodString);
                }
            };

            findAndHookMethod(targetClass, baseMethod.getName(), finalParam);
            Log.d("snapprefs", "Hooked method: " + fullMethodString);
        }

        if (hookSubClasses) {
            Class[] subClasses = targetClass.getClasses();

            Log.d("snapprefs", "Hooking Subclasses: " + subClasses.length);

            for (Class subClass : subClasses)
                hookAllMethods(subClass.getName(), cl, hookSubClasses, hookSuperClasses);
        }

        if (hookSuperClasses) {
            Class superClass = targetClass.getSuperclass();
            if (superClass == null || superClass.getSimpleName().equals("Object"))
                return;

            Log.d("snapprefs", "FOUND SUPERCLASS: " + superClass.getSimpleName());
            hookAllMethods(superClass.getName(), cl, false, true);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mResources = XModuleResources.createInstance(startupParam.modulePath, null);
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        try {
            if (!resparam.packageName.equals(Common.PACKAGE_SNAP))
                return;

            Object activityThread =
                    callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
            Context localContext = (Context) callMethod(activityThread, "getSystemContext");

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

            Logger.log("Initialising preferences from xposed");

            try {
                if (Preferences.getMap() == null || Preferences.getMap().isEmpty()) {
                    Logger.log("Loading map from xposed");
                    Preferences.loadMapFromXposed();
                }
            } catch (Exception e) {
                Log.e("snapchat", "EXCEPTION LOADING HOOKED PREFS");
                e.printStackTrace();
            }

            //mSavePath = Preferences.getExternalPath().getAbsolutePath() + "/Snapprefs";
            //mCustomFilterLocation = Preferences.getExternalPath().getAbsolutePath() + "/Snapprefs/Filters";
            //Preferences.loadMapFromXposed();
            resParam = resparam;

            // TODO Set up removal of button when mode is changed
            // Currently requires snapchat to restart to remove the button
            saveImg = BitmapFactory.decodeResource(mResources, R.drawable.save_button);

            try {
                HookedLayouts.addSaveButtonsAndGestures(resparam, mResources, localContext);
            } catch (Resources.NotFoundException ignore) {
            }

            try {
                NotificationUtils.handleInitPackageResources(modRes);
            } catch( Resources.NotFoundException ignore) {}

            if (Preferences.shouldAddGhost()) {
                try {
                    HookedLayouts.addIcons(resparam, mResources);
                } catch (Resources.NotFoundException ignore) {
                }
            }
            if (Preferences.getBool(Prefs.INTEGRATION)) {
                try {
                    HookedLayouts.addShareIcon(resparam);
                } catch (Resources.NotFoundException ignore) {
                }
            }
            if (Preferences.getBool(Prefs.HIDE_PEOPLE)) {
                try {
                    Stories.addSnapprefsBtn(resparam, mResources);
                } catch (Resources.NotFoundException ignore) {
                }
            }

            //Chat.initChatSave(resparam, mResources);
            try {
                HookedLayouts.fullScreenFilter(resparam);
            } catch (Resources.NotFoundException ignore) {
            }
        } catch (Exception e) {
            Logger.log("Exception thrown in handleInitPackageResources", e);
        }
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        try {
            if (!lpparam.packageName.equals(Common.PACKAGE_SNAP) && !lpparam.packageName.equals(Common.PACKAGE_SP))
                return;

            if(lpparam.packageName.equals(Common.PACKAGE_SP)) {
                findAndHookMethod("com.marz.snapprefs.Util.CommonUtils", lpparam.classLoader, "isModuleEnabled", XC_MethodReplacement.returnConstant((BuildConfig.BUILD_TYPE == "debug" ? Common.MODULE_ENABLED_CHECK_INT : BuildConfig.VERSION_CODE)));
                return;
            }

            try {
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

            findAndHookMethod("android.media.MediaRecorder", lpparam.classLoader, "setMaxDuration", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Logger.printFinalMessage("setMaxDuration - " + param.args[0], LogType.SAVING);
                    param.args[0] = 12000000;//2 mins
                }
            });


            findAndHookMethod("android.app.Application", lpparam.classLoader, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Friendmojis.init(lpparam);
                    DebugHelper.init(lpparam);

                    Logger.loadSelectedLogTypes();
                    Logger.log("Loading map from xposed");
                    Preferences.loadMapFromXposed();

                    Logger.log("Application hook: " + param.thisObject.getClass().getCanonicalName());

                    findAndHookMethod(Obfuscator.timer.RECORDING_MESSAGE_HOOK_CLASS, lpparam.classLoader, Obfuscator.timer.RECORDING_MESSAGE_HOOK_METHOD, Message.class, new XC_MethodHook() {
                        boolean internallyCalled = false;
                        int maxRecordTime = Integer.parseInt(Preferences.getString(Prefs.MAX_RECORDING_TIME).trim()) * 1000;
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // If maxRecordTime is same as SC timecap, let SC perform as normal
                            if (maxRecordTime > 10000) {
                                super.beforeHookedMethod(param);
                                Message message = (Message) param.args[0];
                                Logger.log("HandleMessageId: " + message.what);

                                if (message.what == 15 && !internallyCalled) {
                                    if (maxRecordTime > 10000) {
                                        internallyCalled = true;

                                        Handler handler = message.getTarget();
                                        Message newMessage = Message.obtain(handler, 15);

                                        handler.sendMessageDelayed(newMessage, maxRecordTime - 10000);
                                        Logger.log(String.format("Triggering video end in %s more ms", maxRecordTime - 10000));
                                    }

                                    param.setResult(null);
                                } else if (internallyCalled)
                                    internallyCalled = false;
                            }
                        }
                    });

                    XC_MethodHook initHook = new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            //Preferences.loadMapFromXposed();
                            SnapContext = (Activity) param.thisObject;
                            Logger.log("Loading map from xposed");
                            Preferences.loadMapFromXposed();
                            if (!Preferences.getBool(Prefs.ACCEPTED_TOU)) {//new ContextThemeWrapper(context.createPackageContext("com.marz.snapprefs", Context.CONTEXT_IGNORE_SECURITY), R.style.AppCompatDialog)
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
                            boolean isNull = SnapContext == null;
                            Logger.log("SNAPCONTEXT, NULL? - " + isNull, true);
                            // Fallback method to force the MediaRecorder implementation in Snapchat
                            // XposedHelpers.findAndHookMethod("com.snapchat.android.camera.videocamera.recordingpreferences.VideoRecorderFactory", lpparam.classLoader, "b", XC_MethodReplacement.returnConstant(false));
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
                            if (Preferences.getBool(Prefs.HIDE_LIVE) || Preferences.getBool(Prefs.HIDE_PEOPLE) ||
                                    Preferences.getBool(Prefs.DISCOVER_UI)) {
                                Stories.initStories(lpparam);
                            }
                            if (Preferences.getBool(Prefs.GROUPS)) {
                                Groups.initGroups(lpparam);
                            }
                            if (Preferences.shouldAddGhost()) {
                                HookedLayouts.initVisiblity(lpparam);
                            }
                            if (Preferences.getBool(Prefs.MULTI_FILTER)) {
                                MultiFilter.initMultiFilter(lpparam, mResources, SnapContext);
                            }
                            if (Preferences.getBool(Prefs.DISCOVER_SNAP)) {
                                DataSaving.blockDsnap(lpparam);
                            }
                            if (Preferences.getBool(Prefs.STORY_PRELOAD)) {
                                DataSaving.blockStoryPreLoad(lpparam);
                            }
                            if (Preferences.getBool(Prefs.DISCOVER_UI)) {
                                DataSaving.blockFromUi(lpparam);
                            }
                            if (Preferences.getBool(Prefs.SPEED)) {
                                Spoofing.initSpeed(lpparam, SnapContext);
                            }
                            if (Preferences.getBool(Prefs.LOCATION)) {
                                Spoofing.initLocation(lpparam, SnapContext);
                            }
                            if (Preferences.getBool(Prefs.WEATHER)) {
                                Spoofing.initWeather(lpparam, SnapContext);
                            }
                            if (Preferences.getBool(Prefs.PAINT_TOOLS)) {
                                PaintTools.initPaint(lpparam, mResources);
                            }
                            if (Preferences.getBool(Prefs.TIMER_COUNTER)) {
                                Misc.initTimer(lpparam, mResources);
                            }

                            ClassLoader cl = lpparam.classLoader;

                            if (Preferences.getBool(Prefs.CHAT_AUTO_SAVE)) {
                                Chat.initTextSave(lpparam, SnapContext);
                            }
                            if (Preferences.getBool(Prefs.CHAT_LOGGING))
                                Chat.initChatLogging(lpparam, SnapContext);

                            if (Preferences.getBool(Prefs.CHAT_MEDIA_SAVE)) {
                                Chat.initImageSave(lpparam, mResources);
                            }
                            if (Preferences.getBool(Prefs.INTEGRATION)) {
                                HookedLayouts.initIntegration(lpparam, mResources);
                            }
                            Misc.forceNavBar(lpparam, Preferences.getInt(Prefs.FORCE_NAVBAR));
                            getEditText(lpparam);
                            // COMPLETED 9.39.5
                            findAndHookMethod(Obfuscator.save.SCREENSHOTDETECTOR_CLASS, lpparam.classLoader, Obfuscator.save.SCREENSHOTDETECTOR_RUN, LinkedHashMap.class, XC_MethodReplacement.DO_NOTHING);
                            findAndHookMethod(Obfuscator.save.SNAPSTATEMESSAGE_CLASS, lpparam.classLoader, Obfuscator.save.SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT, Long.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    param.args[0] = 0L;
                                    Logger.log("StateBuilder.setScreenshotCount set to 0L", true);
                                }
                            });
                            if (Preferences.getBool(Prefs.CUSTOM_STICKER)) {
                                Stickers.initStickers(lpparam, modRes, SnapContext);
                            }

                            if (Preferences.getLicence() > 0)
                                Premium.initPremium(lpparam);

                            /*hookAllConstructors(ahO, new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                            super.afterHookedMethod(param);

                                            Logger.log("EventType: " + getObjectField(param.thisObject, "mEventName"));
                                        }
                                    });

                            findAndHookMethod("ahO", cl, "a", String.class, Object.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    super.beforeHookedMethod(param);
                                    Logger.log(String.format("Object event [Key:%s][Object:%s]", param.args[0], param.args[1]));
                                }
                            });

                            findAndHookMethod("ahO", cl, "a", String.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    super.beforeHookedMethod(param);
                                    Logger.log(String.format("String event [Key:%s]", param.args[0]));
                                }
                            });*/
                        }
                    };

                    findAndHookMethod("com.snapchat.android.LandingPageActivity",
                            lpparam.classLoader, "onCreate", Bundle.class, initHook);
                    findAndHookMethod("com.snapchat.android.LandingPageActivity",
                            lpparam.classLoader, "onResume", initHook);
                    findAndHookMethod(Obfuscator.save.LANDINGPAGEACTIVITY_CLASS, lpparam.classLoader, "onSnapCapturedEvent", findClass(Obfuscator.visualfilters.SNAPCHAPTUREDEVENT_CLASS, lpparam.classLoader), new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            VisualFilters.added.clear();
                            VisualFilters.added2.clear();
                            MultiFilter.added.clear();
                            PaintTools.once = false;
                            XposedBridge.log("CLEARING ADDED");
                        }
                    });

        /*findAndHookMethod("com.snapchat.android.Timber", lpparam.classLoader, "c", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("TIMBER: " + param.args[0] + " : " + param.args[1], true);
            }
        });*/

                    //Showing lenses or not
                    // Old code - Used when share button was placed above the TAKE PICTURE button
                    /*
                    findAndHookMethod(Obfuscator.icons.ICON_HANDLER_CLASS, lpparam.classLoader, Obfuscator.icons.SHOW_LENS, boolean.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (HookedLayouts.upload != null) {
                                if ((boolean) param.args[0]) {
                                    HookedLayouts.upload.setVisibility(View.INVISIBLE);
                                } else {
                                    HookedLayouts.upload.setVisibility(View.VISIBLE);
                                }
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
                    });*/
                    // COMPLETED 9.39.5
                    for (String s : Obfuscator.ROOTDETECTOR_METHODS) {
                        findAndHookMethod(Obfuscator.ROOTDETECTOR_CLASS, lpparam.classLoader, s, XC_MethodReplacement.returnConstant(false));
                        Logger.log("ROOTCHECK: " + s, true);
                    }
                    // External class - Belongs to android
                    //Gabe is a douche
                    // COMPLETED 9.39.5
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

                    if (Preferences.getBool(Prefs.CAPTION_UNLIMITED_VANILLA)) {
                        // New unlimited captions function
                        // COMPLETED 9.39.5
                        XposedHelpers.findAndHookMethod(Obfuscator.misc.CAPTIONVIEW, lpparam.classLoader, Obfuscator.misc.CAPTIONVIEW_TEXT_LIMITER, int.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                param.args[0] = 999999999;
                            }
                        });
                        String snapCaptionView =
                                "com.snapchat.android.app.shared.ui.caption.SnapCaptionView";
                        hookAllConstructors(findClass(snapCaptionView, lpparam.classLoader), new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                if (Preferences.getBool(Prefs.CAPTION_UNLIMITED_VANILLA)) {
                                    XposedUtils.log("Unlimited vanilla captions - 1");
                                    EditText vanillaCaptionEditText = (EditText) param.thisObject;
                                    // Set single lines mode to false
                                    vanillaCaptionEditText.setSingleLine(false);
                                    vanillaCaptionEditText.setFilters(new InputFilter[0]);
                                    // Remove actionDone IME option, by only setting flagNoExtractUi
                                    vanillaCaptionEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
                                    // Remove listener hiding keyboard when enter is pressed by setting the listener to null
                                    vanillaCaptionEditText.setOnEditorActionListener(null);
                                    // Remove listener for cutting of text when the first line is full by setting the text change listeners list to null
                                    setObjectField(vanillaCaptionEditText, "mListeners", null);
                                }
                            }
                        });
                        XposedHelpers.findAndHookMethod("com.snapchat.android.app.shared.ui.caption.SnapCaptionView", lpparam.classLoader, "onCreateInputConnection", EditorInfo.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (Preferences.getBool(Prefs.CAPTION_UNLIMITED_VANILLA)) {
                                    XposedUtils.log("Unlimited vanilla captions - 2");
                                    EditorInfo editorInfo = (EditorInfo) param.args[0];
                                    editorInfo.imeOptions = EditorInfo.IME_ACTION_NONE;
                                }
                            }
                        });
                        XposedHelpers.findAndHookMethod("TX$3", lpparam.classLoader, "onEditorAction", TextView.class, int.class, KeyEvent.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                Logger.printFinalMessage("onEditorAction: int= " + param.args[1], LogType.SAVING);
                            }
                        });

                        //findAndHookMethod("com.snapchat.android.ui.caption.CaptionEditText", lpparam.classLoader, "n", XC_MethodReplacement.DO_NOTHING);
                    }
                    // VanillaCaptionEditText was moved from an inner-class to a separate class in 8.1.0
                    // TODO Find below class - ENTIRE PACKAGE REFACTORED - DONE?
                    /*String vanillaCaptionEditTextClassName =
                            "com.snapchat.android.ui.caption.VanillaCaptionEditText";
                    hookAllConstructors(findClass(vanillaCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (Preferences.getBool(Prefs.CAPTION_UNLIMITED_VANILLA)) {
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

                    //This is all Gabe's fault
                    // FatCaptionEditText was moved from an inner-class to a separate class in 8.1.0
                    // TODO Find below class - ENTIRE PACKAGE REFACTORED
                    String fatCaptionEditTextClassName =
                            "com.snapchat.android.ui.caption.FatCaptionEditText";
                    hookAllConstructors(findClass(fatCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (Preferences.getBool(Prefs.CAPTION_UNLIMITED_FAT)) {
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
                    });*/
                    //SNAPSHARE
                    Sharing.initSharing(lpparam, mResources);
                    //SNAPPREFS
                    if (Preferences.getBool(Prefs.HIDE_BF)) {
                        // COMPLETED 9.39.5
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
                    if (Preferences.getBool(Prefs.CUSTOM_FILTER)) {
                        addFilter(lpparam);
                    }
                    if (Preferences.getBool(Prefs.SELECT_ALL)) {
                        HookSendList.initSelectAll(lpparam);
                    }
                    //Completed 9.39.5
                    findAndHookMethod("com.snapchat.android.camera.CameraFragment", lpparam.classLoader, "onKeyDownEvent", XposedHelpers.findClass(Obfuscator.flash.KEYEVENT_CLASS, lpparam.classLoader), new XC_MethodHook() {
                        public boolean frontFlash = false;
                        public long lastChange = System.currentTimeMillis();

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            //this.mIsVisible && this.n.e() != 0 && this.n.e() != 2 && !this.n.c()
                            boolean isVisible = XposedHelpers.getBooleanField(param.thisObject, Obfuscator.flash.ISVISIBLE_FIELD);
                            Object swipeLayout = getObjectField(param.thisObject, Obfuscator.flash.SWIPELAYOUT_FIELD);
                            int resId = (int) getObjectField(swipeLayout, Obfuscator.flash.GETRESID_OBJECT);
                            boolean c = (boolean) XposedHelpers.callMethod(swipeLayout, Obfuscator.flash.ISSCROLLED_METHOD);
                            if (isVisible && resId != 0 && resId != 2 && !c) {
                                int keycode = XposedHelpers.getIntField(param.args[0], Obfuscator.flash.KEYCODE_FIELD);
                                int flashkey;
                                if(Preferences.getBool(Prefs.FLASH_KEY) == true){
                                    flashkey = KeyEvent.KEYCODE_VOLUME_UP;
                                } else {
                                    flashkey = KeyEvent.KEYCODE_VOLUME_DOWN;
                                }
                                if (keycode == flashkey) {
                                    if (System.currentTimeMillis() - lastChange > 500) {
                                        lastChange = System.currentTimeMillis();
                                        frontFlash = !frontFlash;
                                        XposedHelpers.callMethod(getObjectField(param.thisObject, Obfuscator.flash.OVERLAY_FIELD), Obfuscator.flash.FLASH_METHOD, new Class[]{boolean.class}, frontFlash);
                                    }
                                    param.setResult(null);
                                }
                            }
                        }
                    });

                    if (Preferences.getBool(Prefs.AUTO_ADVANCE))
                        XposedHelpers.findAndHookMethod(Obfuscator.stories.AUTOADVANCE_CLASS, lpparam.classLoader, Obfuscator.stories.AUTOADVANCE_METHOD, XC_MethodReplacement.returnConstant(false));

                }
            });
        } catch (Exception e) {
            Logger.log("Exception thrown in handleLoadPackage", e);
        }
    }

    private void addFilter(LoadPackageParam lpparam) {
        //Replaces the batteryfilter with our custom one
        //Pedro broke this part - He didn't really.
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
                                if (Preferences.getFilterPath() == null) {
                                    iv.setImageDrawable(modRes.getDrawable(R.drawable.custom_filter_1));
                                    Logger.log("Replaced batteryfilter from R.drawable", true);
                                } else {
                                    if (Preferences.getInt(Prefs.CUSTOM_FILTER_TYPE) == 0) {
                                        iv.setImageDrawable(Drawable.createFromPath(
                                                Preferences.getFilterPath() +
                                                        "/fullscreen_filter.png"));
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                    } else if (Preferences.getInt(Prefs.CUSTOM_FILTER_TYPE) == 1) {
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                        iv.setImageDrawable(Drawable.createFromPath(
                                                Preferences.getFilterPath() +
                                                        "/banner_filter.png"));
                                    }
                                    Logger.log(
                                            "Replaced batteryfilter from " +
                                                    Preferences.getFilterPath() +
                                                    " Type: " +
                                                    Preferences.getInt(Prefs.CUSTOM_FILTER_TYPE), true);
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
                findClass("com.snapchat.android.app.shared.feature.preview.model.filter.BatteryLevel", lpparam.classLoader); //prev. com.snapchat.android.app.shared.model.filter.BatteryLevel

        findAndHookMethod(Obfuscator.spoofing.BATTERY_FILTER, lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Object battery = getStaticObjectField(batteryInfoProviderEnum, Obfuscator.spoofing.BATTERY_FULL_ENUM);
                param.setResult(battery);
            }
        });
    }

    public void getEditText(LoadPackageParam lpparam) {
        //TODO Find below hook - ENTIRE PACKAGE REFACTOR
        this.CaptionEditText =
                XposedHelpers.findClass("com.snapchat.android.app.shared.ui.caption.SnapCaptionView", lpparam.classLoader);
        XposedBridge.hookAllConstructors(this.CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws PackageManager.NameNotFoundException {
                editText = (EditText) param.thisObject;
                if (!haveDefTypeface) {
                    defTypeface = editText.getTypeface();
                    haveDefTypeface = true;
                }
            }
        });
    }
}
