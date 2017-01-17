package com.marz.snapprefs;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.SnapData.FlagState;
import com.marz.snapprefs.Util.CommonUtils;
import com.marz.snapprefs.Util.FlingSaveGesture;
import com.marz.snapprefs.Util.GestureEvent;
import com.marz.snapprefs.Util.NotificationUtils;
import com.marz.snapprefs.Util.NotificationUtils.ToastType;
import com.marz.snapprefs.Util.SavingUtils;
import com.marz.snapprefs.Util.StringUtils;
import com.marz.snapprefs.Util.SweepSaveGesture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.marz.snapprefs.Util.StringUtils.obfus;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setDoubleField;

public class Saving {
    //public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    //TODO implement user selected save mode
    private static final boolean threadedSaveMode = true;
    private static final boolean asyncSaveMode = false;
    private static final boolean printFlags = true;
    private static Resources mSCResources;
    private static XC_LoadPackage.LoadPackageParam lpparam2;
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault());
    private static SimpleDateFormat dateFormatSent =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    private static LinkedHashMap<String, SnapData> hashSnapData = new LinkedHashMap<String, SnapData>() {
        @Override protected boolean removeEldestEntry(Entry eldest) {
            return size() > 40;
        }
    };
    private static String currentSnapKey;
    private static Context relativeContext;
    private static Object enum_NO_AUTO_ADVANCE;
    private static GestureEvent gestureEvent;
    private static boolean gestureCalledInternally = false;

    static void initSaving(final XC_LoadPackage.LoadPackageParam lpparam,
                           final XModuleResources modRes, final Context snapContext) {
        lpparam2 = lpparam;

        if (mSCResources == null) mSCResources = snapContext.getResources();

        try {
            final ClassLoader cl = lpparam.classLoader;

            final Class storyClass = findClass(Obfuscator.save.STORYSNAP_CLASS, cl);
            Class AdvanceType = findClass(Obfuscator.misc.ADVANCE_TYPE_CLASS, cl);
            enum_NO_AUTO_ADVANCE = getStaticObjectField(AdvanceType, Obfuscator.misc.NO_AUTO_ADVANCE_OBJECT);

            /**
             * Called whenever a video is decrypted by snapchat
             * Will pre-load the next snap in the list
             */
            // UPDATED METHOD & CONTENT 9.39.5
            findAndHookConstructor(Obfuscator.save.DECRYPTEDSNAPVIDEO_CLASS, cl, findClass(
                    Obfuscator.save.CACHE_CLASS, cl), String.class, Bitmap.class, String.class, long.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            try {
                                handleVideoPayload(snapContext, param);
                            } catch (Exception e) {
                                Logger.log(
                                        "Exception handling Video Payload", e, LogType.SAVING);
                            }
                        }
                    });

            /**
             * Called whenever a bitmap is set to the view (I believe)
             **/
            // UPDATED METHOD & CONTENT 9.39.5
            findAndHookMethod(Obfuscator.save.IMAGESNAPRENDERER_CLASS2, cl, Obfuscator.save.IMAGESNAPRENDERER_NEW_BITMAP, Bitmap.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        handleImagePayload(snapContext, param);
                    } catch (Exception e) {
                        Logger.log("Exception handling Image Payload", e, LogType.SAVING);
                    }
                }
            });

            if (Preferences.getInt(Prefs.SAVEMODE_STORY) == Preferences.SAVE_S2S ||
                    Preferences.getInt(Prefs.SAVEMODE_STORY) == Preferences.SAVE_F2S) {
                findAndHookMethod(Obfuscator.save.DIRECTIONAL_LAYOUT_CLASS, cl, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        if (gestureCalledInternally)
                            return;

                        if (gestureEvent == null) {
                            if (Preferences.getInt(Prefs.SAVEMODE_STORY) == Preferences.SAVE_S2S)
                                gestureEvent = new SweepSaveGesture();
                            else if (Preferences.getInt(Prefs.SAVEMODE_STORY) == Preferences.SAVE_F2S)
                                gestureEvent = new FlingSaveGesture();
                            else {
                                Logger.log("No gesture method provided", LogType.SAVING);
                                return;
                            }
                        }

                        String mKey = (String) getAdditionalInstanceField(param.thisObject, "mKey");

                        if (mKey != null) {
                            if (gestureEvent.onTouch((FrameLayout) param.thisObject, (MotionEvent) param.args[0], SnapType.STORY) ==
                                    GestureEvent.ReturnType.TAP) {
                                gestureCalledInternally = true;
                                Logger.log("Performed TAP?", LogType.SAVING);
                                param.setResult(callMethod(param.thisObject, "dispatchTouchEvent", param.args[0]));
                            } else
                                param.setResult(true);
                        }

                        gestureCalledInternally = false;
                    }
                });
            }

            findAndHookMethod(Obfuscator.save.STORY_VIEWER_MEDIA_CACHE, cl, Obfuscator.save.VIEWING_STORY_METHOD,
                    String.class, findClass(Obfuscator.save.STORY_DETAILS_PACKET, cl), ImageView.class, findClass(Obfuscator.save.VIEWING_STORY_VAR4, cl), new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Logger.log("### START StoryViewerMediaCache ###", LogType.SAVING);
                            Object godPacket = param.args[1];
                            Object storyList = getObjectField(param.thisObject, Obfuscator.save.SVMC_STORYLIST_OBJECT);
                            String POSTER_USERNAME = (String) callMethod(godPacket, Obfuscator.save.SDP_GET_STRING, "POSTER_USERNAME");
                            String CLIENT_ID = (String) callMethod(godPacket, Obfuscator.save.SDP_GET_STRING, "CLIENT_ID");
                            Object storySnap = callMethod(storyList, Obfuscator.save.SDP_GET_OBJECT, POSTER_USERNAME, CLIENT_ID);

                            if (storySnap == null) {
                                Logger.log("Null storysnap?", LogType.SAVING);
                                return;
                            }

                            String storyUsername = (String) callMethod(godPacket, Obfuscator.save.SDP_GET_STRING, "POSTER_USERNAME");

                            if (storyUsername.equals(HookMethods.getSCUsername(lpparam.classLoader))) {
                                Logger.log("Story is yours", LogType.SAVING);
                                return;
                            }

                            View view = (View) param.args[2];

                            if (Preferences.getInt(Prefs.SAVEMODE_STORY) != Preferences.SAVE_AUTO) {
                                FrameLayout snapContainer = scanForStoryContainer(view);
                                String mKey = (String) getObjectField(storySnap, "mId");

                                if (snapContainer != null) {
                                    if (Preferences.getInt(Prefs.SAVEMODE_STORY) == Preferences.SAVE_BUTTON)
                                        HookedLayouts.assignStoryButton(snapContainer, snapContext, mKey);
                                    else if (Preferences.getInt(Prefs.SAVEMODE_STORY) == Preferences.SAVE_S2S ||
                                            Preferences.getInt(Prefs.SAVEMODE_STORY) == Preferences.SAVE_F2S) {
                                        FrameLayout snapContainerParent = (FrameLayout) snapContainer.getParent();

                                        if (snapContainerParent != null)
                                            setAdditionalInstanceField(snapContainerParent, "mKey", mKey);
                                    }
                                }
                            }

                            setAdditionalInstanceField(view, "StorySnap", storySnap);
                            Logger.log("StoryViewerMediaCache.a : KEY " + getObjectField(storySnap, "mId"), LogType.SAVING);
                            Logger.log("Str: " + param.args[0], LogType.SAVING);
                            Logger.log("### END StoryViewerMediaCache ###", LogType.SAVING);
                        }
                    });

            findAndHookConstructor(Obfuscator.save.STORY_IMAGE_HOLDER, cl, ImageView.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    Object storySnap = getAdditionalInstanceField(param.args[0], "StorySnap");

                    if (storySnap != null) {
                        Logger.log("### START gC <INIT> ###", LogType.SAVING);

                        String storyUsername = (String) getObjectField(storySnap, "mUsername");

                        if (storyUsername.equals(HookMethods.getSCUsername(lpparam.classLoader))) {
                            Logger.log("Story is yours", LogType.SAVING);
                            return;
                        }

                        setAdditionalInstanceField(param.thisObject, "StorySnap", storySnap);
                        Logger.log("Key: " + getObjectField(storySnap, "mId"), LogType.SAVING);
                        Logger.log("### END gC <INIT> ###", LogType.SAVING);
                    }

                }
            });

            findAndHookMethod(Obfuscator.save.STORY_LOADER, cl, Obfuscator.save.SL_ISVIEWING_METHOD, storyClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Logger.log("asT.i", LogType.SAVING);

                    Object storySnap = param.args[0];

                    if (storySnap == null) {
                        Logger.log("Null StorySnap", LogType.SAVING);
                        return;
                    }

                    String storyUsername = (String) getObjectField(storySnap, "mUsername");

                    if (storyUsername.equals(HookMethods.getSCUsername(lpparam.classLoader))) {
                        Logger.log("Story is yours", LogType.SAVING);
                        return;
                    }

                    handleSnapHeader(snapContext, storySnap);
                }
            });

            findAndHookMethod(Obfuscator.save.STORY_IMAGE_HOLDER, cl, "onResourceReady", Object.class, findClass(Obfuscator.save.SL_VAR2, cl), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object storySnap = getAdditionalInstanceField(param.thisObject, "StorySnap");

                    if (storySnap == null) {
                        return;
                    }

                    Logger.log("### START gC onResourceReady ###", LogType.SAVING);

                    Object image = param.args[0];

                    if (!(image instanceof Bitmap)) {
                        Logger.log("### RETURNED gC onResourceReady - Not bitmap ###", LogType.SAVING);
                        return;
                    }

                    String storyUsername = (String) getObjectField(storySnap, "mUsername");

                    if (storyUsername.equals(HookMethods.getSCUsername(lpparam.classLoader))) {
                        Logger.log("Story is yours", LogType.SAVING);
                        return;
                    }


                    String mKey = (String) getObjectField(storySnap, "mId");
                    handleImagePayload(snapContext, mKey, (Bitmap) image);
                }
            });

            /**
             * Called every time a snap is viewed - Quite reliable
             */
            // UPDATED METHOD & CONTENT 9.39.5
            findAndHookMethod(Obfuscator.save.RECEIVEDSNAP_CLASS, cl, Obfuscator.save
                    .RECEIVEDSNAP_BEING_SEEN, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    boolean isBeingViewed = (boolean) param.args[0];

                    Logger.log("Viewing snap: " + isBeingViewed, LogType.SAVING);
                    if (isBeingViewed) {
                        Object obj = param.thisObject;

                        try {
                            handleSnapHeader(snapContext, obj);
                        } catch (Exception e) {
                            Logger.log("Exception handling HEADER", e, LogType.SAVING);
                        }
                    }
                }
            });
            // UPDATED METHOD & CONTENT 9.39.5
            findAndHookMethod(Obfuscator.save.SNAPPREVIEWFRAGMENT_CLASS, lpparam.classLoader, Obfuscator.save.SNAPPREVIEWFRAGMENT_METHOD1, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if (Preferences.getBool(Prefs.SAVE_SENT_SNAPS))
                            handleSentSnap(param.thisObject, snapContext);
                    } catch (Exception e) {
                        Logger.log("Error getting sent media", e, LogType.SAVING);
                    }
                }
            });

            /**
             * We hook this method to set the CanonicalDisplayTime to our desired one if it is under
             * our limit and hide the counter if we need it.
             */

            // UPDATED METHOD & CONTENT 9.39.5
            if (Preferences.getBool(Prefs.TIMER_UNLIMITED) || Preferences.getInt(Prefs.TIMER_MINIMUM) !=
                    Preferences.TIMER_MINIMUM_DISABLED) {

                findAndHookMethod(Obfuscator.save.STORY_DETAILS_PACKET, cl, Obfuscator.save.SDP_GET_ENUM_METHOD, String.class, Object.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        String key = (String) param.args[0];
                        //Logger.log("aGgkey: " + key);
                        if (Preferences.getBool(Prefs.AUTO_ADVANCE) && key.equals("auto_advance_mode"))
                            param.args[1] = enum_NO_AUTO_ADVANCE;
                        else if (Preferences.getInt(Prefs.TIMER_MINIMUM) !=
                                Preferences.TIMER_MINIMUM_DISABLED && key.equals("total_duration_sec")) {
                            param.args[1] = 9999;
                        }
                    }
                });

                if (Preferences.getBool(Prefs.TIMER_UNLIMITED) || Preferences.getInt(Prefs.TIMER_MINIMUM) !=
                        Preferences.TIMER_MINIMUM_DISABLED) {
                    XposedBridge.hookAllConstructors(findClass(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Double currentResult = XposedHelpers.getDoubleField(param.thisObject, Obfuscator.save.MCANONICALDISPLAYNAME);
                            if (Preferences.getBool(Prefs.TIMER_UNLIMITED)) {
                                findAndHookMethod(Obfuscator.save.CLASS_SNAP_TIMER_VIEW, lpparam.classLoader, Obfuscator.save.METHOD_SNAPTIMERVIEW_ONDRAW, Canvas.class, XC_MethodReplacement.DO_NOTHING);
                                setDoubleField(param.thisObject, Obfuscator.save.MCANONICALDISPLAYNAME, (double) 9999.9F);
                            } else {
                                if (Preferences.getInt(Prefs.TIMER_MINIMUM) !=
                                        Preferences.TIMER_MINIMUM_DISABLED &&
                                        currentResult < (double) Preferences.getInt(Prefs.TIMER_MINIMUM)) {
                                    setDoubleField(param.thisObject, Obfuscator.save.MCANONICALDISPLAYNAME, (double) Preferences.getInt(Prefs.TIMER_MINIMUM));
                                }
                            }
                        }
                    });

                    XposedBridge.hookAllConstructors(findClass(Obfuscator.save.STORYSNAP_CLASS, lpparam.classLoader), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Double currentResult = XposedHelpers.getDoubleField(param.thisObject, Obfuscator.save.MCANONICALDISPLAYNAME);
                            if (Preferences.getBool(Prefs.TIMER_UNLIMITED)) {
                                setDoubleField(param.thisObject, Obfuscator.save.MCANONICALDISPLAYNAME, (double) 9999.9F);
                            } else {
                                if (Preferences.getInt(Prefs.TIMER_MINIMUM) !=
                                        Preferences.TIMER_MINIMUM_DISABLED &&
                                        currentResult < (double) Preferences.getInt(Prefs.TIMER_MINIMUM)) {
                                    setDoubleField(param.thisObject, Obfuscator.save.MCANONICALDISPLAYNAME, (double) Preferences.getInt(Prefs.TIMER_MINIMUM));
                                }
                            }
                        }
                    });
                }
            }

            if (Preferences.getBool(Prefs.HIDE_TIMER_SNAP)) {
                // UPDATED METHOD & CONTENT
                findAndHookMethod(Obfuscator.save.CLASS_SNAP_TIMER_VIEW, lpparam.classLoader, Obfuscator.save.METHOD_SNAPTIMERVIEW_ONDRAW, Canvas.class, XC_MethodReplacement.DO_NOTHING);
            }
            if (Preferences.getBool(Prefs.HIDE_TIMER_STORY)) {
                // UPDATED METHOD & CONTENT
                findAndHookMethod(Obfuscator.save.CLASS_NEW_STORY_TIMER_VIEW, lpparam.classLoader, Obfuscator.save.METHOD_STORYTIMERVIEW_ONDRAW, Canvas.class, XC_MethodReplacement.DO_NOTHING);
                findAndHookMethod(Obfuscator.save.CLASS_STORY_TIMER_VIEW, lpparam.classLoader, Obfuscator.save.METHOD_STORYTIMERVIEW_ONDRAW, Canvas.class, XC_MethodReplacement.DO_NOTHING);
            }
            if (Preferences.getBool(Prefs.LOOPING_VIDS)) {
                // UPDATED METHOD & CONTENT
                findAndHookMethod(Obfuscator.save.CLASS_TEXTURE_VIDEO_VIEW, lpparam.classLoader, Obfuscator.save.METHOD_TVV_START, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        callMethod(param.thisObject, Obfuscator.save.METHOD_TVV_SETLOOPING, true);
                    }
                });
                // UPDATED METHOD & CONTENT
                findAndHookMethod(Obfuscator.save.CLASS_SNAP_COUNTDOWN_CONTROLLER, lpparam.classLoader, Obfuscator.save.METHOD_SCC_VAR1, long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] =
                                3600000L;//It's how long you see video looping in milliseconds
                    }
                });
            }

            //List<Bitmap> a = this.i.a(this.F.g(), ProfileImageSize.MEDIUM);
            // UPDATED METHOD & CONTENT 9.39.5
            findAndHookMethod(Obfuscator.save.CLASS_FRIEND_MINI_PROFILE_POPUP_FRAGMENT, lpparam.classLoader, Obfuscator.save.FRIEND_MINI_PROFILE_POPUP_GET_CACHED_PROFILE_PICTURES, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    View mini_profile_snapcode = (View) getObjectField(param.thisObject, Obfuscator.save.MINI_PROFILE_SNAPCODE);
                    mini_profile_snapcode.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            // ProfileImageUtils$ProfileImageSize inner class
                            Class<?> profileImageSizeClass = findClass(Obfuscator.save.PROFILE_IMAGE_UTILS_PROFILE_IMAGE_SIZE_INNER_CLASS, lpparam.classLoader);
                            // F
                            Object friendObject = getObjectField(param.thisObject, Obfuscator.save.FRIEND_MINI_PROFILE_POPUP_FRIEND_FIELD);
                            // ^.g
                            String username = (String) callMethod(friendObject, Obfuscator.save.GET_FRIEND_USERNAME);
                            // ProfileImageSize.MEDIUM
                            Object MEDIUM = getStaticObjectField(findClass(Obfuscator.save.PROFILE_IMAGE_UTILS_PROFILE_IMAGE_SIZE_INNER_CLASS, lpparam.classLoader), "MEDIUM");
                            // this.i
                            Object i = getObjectField(param.thisObject, Obfuscator.save.FRIEND_MINI_PROFILE_POPUP_FRIENDS_PROFILE_IMAGES_CACHE);
                            //^.a(F.g(), ProfileImageSize.MEDIUM)
                            List<Bitmap> profileImages = (List<Bitmap>) callMethod(i, Obfuscator.save.PROFILE_IMAGES_CACHE_GET_PROFILE_IMAGES, new Class[]{String.class, profileImageSizeClass}, username, MEDIUM);
                            String filePath = SavingUtils.generateFilePath("ProfileImages", username);
                            if (Preferences.getBool(Prefs.DEBUGGING)) {
                                Logger.printTitle("Profile Image Saving Debug Information", LogType.SAVING);
                                Logger.printMessage("Profile Image Size Inner Class: " + profileImageSizeClass, LogType.SAVING);
                                Logger.printMessage("friendObject: " + friendObject, LogType.SAVING);
                                Logger.printMessage("Medium: " + MEDIUM, LogType.SAVING);
                                Logger.printMessage("'i' Object: " + i, LogType.SAVING);
                                Logger.printMessage("profileImages List Object: " + profileImages, LogType.SAVING);
                                Logger.printFilledRow(LogType.SAVING);

                                Logger.printTitle("Profile Image Saving Save Path Debug Information", LogType.SAVING);
                                Logger.printMessage("Sort by Category Pref: " + Preferences.getBool(Prefs.SORT_BY_CATEGORY), LogType.SAVING);
                                Logger.printMessage("Sort by Username Pref: " + Preferences.getBool(Prefs.SORT_BY_USERNAME), LogType.SAVING);
                                Logger.printMessage("File Path: " + filePath, LogType.SAVING);
                                Logger.printFilledRow(LogType.SAVING);
                            }
                            final File profileImagesFolder = new File(filePath);
                            if (!profileImagesFolder.mkdirs() && !profileImagesFolder.exists()) {
                                Logger.log("Error creating ProfileImages and/or Username folder");
                                return false;
                            }

                            if (profileImages == null) {
                                SavingUtils.vibrate(snapContext, false);
                                NotificationUtils.showStatefulMessage("Error Saving Profile Images For " + username + "\nIf The Profile Image Is Not Blank Please Enable Debug Mode And Rep", ToastType.BAD, lpparam.classLoader);
                                return false;
                            }
                            int succCounter = 0;
                            int sizeOfProfileImages = profileImages.size();
                            for (int iterator = 0; iterator < sizeOfProfileImages; iterator++) {
                                Bitmap bmp = profileImages.get(iterator);
                                File f = null;
                                try {
                                    f = new File(profileImagesFolder, username + "-" + iterator + "-" + CommonUtils.sha256(bmp) + ".jpg");
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                                if (f == null) {
                                    NotificationUtils.showStatefulMessage("File f is null!", ToastType.BAD, lpparam.classLoader);
                                    Logger.logStackTrace();
                                    return false;
                                }
                                if (f.exists()) {
                                    NotificationUtils.showStatefulMessage("Profile Images already Exist.", ToastType.BAD, lpparam.classLoader);
                                    return true;
                                }

                                if (SavingUtils.saveJPG(f, profileImages.get(iterator), snapContext, false)) {
                                    succCounter++;
                                }
                            }
                            Boolean succ = (succCounter == sizeOfProfileImages);
                            NotificationUtils.showStatefulMessage("Saved " + succCounter + "/" + sizeOfProfileImages + " profile images.", succ ? ToastType.GOOD : ToastType.BAD, lpparam.classLoader);
                            SavingUtils.vibrate(snapContext, succ);
                            return true;
                        }
                    });
                }
            });

            //HookMethods.hookAllMethods("Ce", cl, true);
        } catch (Exception e) {
            Logger.log("Error occurred: Snapprefs doesn't currently support this version, wait for an update", e);

            findAndHookMethod(Obfuscator.save.LANDINGPAGEACTIVITY_CLASS, lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Toast.makeText((Context) param.thisObject, "This version of Snapchat is currently not supported by Snapprefs.", Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    private static FrameLayout scanForStoryContainer(View view) {
        if (view == null) {
            Logger.log("Called scan with Null view");
            return null;
        }

        Object parent = view.getParent();

        if (parent != null) {
            if (parent instanceof View) {
                int id = ((View) parent).getId();

                if (id == Obfuscator.save.OPERA_PAGE_VIEW_ID) {
                    Logger.log("Found Opera container");
                    return (FrameLayout) parent;
                } else
                    return scanForStoryContainer((View) parent);
            }
        }

        Logger.log("Null scan attempt");
        return null;
    }

    // UPDATED 9.39.5
    private static void handleSentSnap(Object snapPreviewFragment, Context snapContext) {
        try {
            Logger.printTitle("Handling SENT snap", LogType.SAVING);
            Activity activity = (Activity) callMethod(snapPreviewFragment, "getActivity");
            Object snapEditorView = getObjectField(snapPreviewFragment, Obfuscator.save.OBJECT_SNAP_EDITOR_VIEW);

            if (snapEditorView == null) {
                Logger.printFinalMessage("SnapEditorView not assigned - Halting process", LogType.SAVING);
                return;
            }

            Object mediaBryo = getObjectField(snapEditorView, Obfuscator.save.OBJECT_MEDIABRYO);

            if (mediaBryo == null) {
                Logger.printFinalMessage("MediaBryo not assigned - Halting process", LogType.SAVING);
                return;
            }

            String mKey = (String) getObjectField(mediaBryo, Obfuscator.save.OBJECT_MCLIENTID);
            Logger.printMessage("mKey: " + mKey, LogType.SAVING);

            SnapData snapData = hashSnapData.get(mKey);

            if (snapData != null && !snapData.hasFlag(FlagState.FAILED)) {
                Logger.printFinalMessage("Snap already handled", LogType.SAVING);
                return;
            } else if (snapData == null) {
                Logger.printMessage("SnapData not found - Creating new", LogType.SAVING);
                snapData = new SnapData(mKey);
                snapData.setSnapType(SnapType.SENT);
                hashSnapData.put(mKey, snapData);
            }

            SaveResponse response = null;
            String filename = dateFormatSent.format(new Date());
            String bryoName = mediaBryo.getClass().getCanonicalName();

            Logger.printMessage("Saving with filename: " + filename, LogType.SAVING);
            Logger.printMessage("MediaBryo Type: " + bryoName, LogType.SAVING);

            if (bryoName.equals(Obfuscator.save.CLASS_MEDIABRYO_VIDEO)) {
                Logger.printMessage("Media Type: VIDEO", LogType.SAVING);
                Uri uri = (Uri) getObjectField(mediaBryo, Obfuscator.save.OBJECT_MVIDEOURI);

                if (uri == null)
                    response = SaveResponse.FAILED;
                else {
                    String regex = "preview/tracked_video_(.*?).mp4.nomedia";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(uri.toString());

                    if (matcher.find()) {
                        try {
                            Logger.printMessage("Original filename: " + matcher.group(0), LogType.SAVING);
                        } catch (IndexOutOfBoundsException ignore) {
                            Logger.printMessage("Original filename: " + uri.getPath(), LogType.SAVING);
                        }
                    } else
                        Logger.printMessage("Original filename: " + uri.getPath(), LogType.SAVING);

                    Logger.printMessage("Uri valid - Trying to save", LogType.SAVING);
                    FileInputStream videoStream = new FileInputStream(uri.getPath());
                    response = saveSnap(SnapType.SENT, MediaType.VIDEO,
                            snapContext, null, videoStream, filename, null);
                }
            } else if (bryoName.equals(Obfuscator.save.SNAPIMAGEBRYO_CLASS)) {
                Logger.printMessage("Media Type: IMAGE", LogType.SAVING);
                Bitmap bmp = (Bitmap) callMethod(snapEditorView, Obfuscator.save.METHOD_GET_SENT_BITMAP, activity, true);
                if (bmp != null) {
                    Logger.printMessage("Sent image found - Trying to save", LogType.SAVING);
                    response = saveSnap(SnapType.SENT, MediaType.IMAGE,
                            snapContext, bmp, null, filename, null);
                } else {
                    Logger.printMessage("Couldn't find sent image!", LogType.SAVING);
                    response = SaveResponse.FAILED;
                }
            }

            if (response == null) {
                Logger.printMessage("Response not assigned - Assumed failed", LogType.SAVING);
                response = SaveResponse.FAILED;
            }

            snapData.getFlags().clear();
            if (response == SaveResponse.SUCCESS) {
                Logger.printFinalMessage("Saved sent snap", LogType.SAVING);
                createStatefulToast("Saved send snap", ToastType.GOOD);
                snapData.addFlag(FlagState.SAVED);
            } else if (response == SaveResponse.FAILED) {
                Logger.printFinalMessage("Error saving snap", LogType.SAVING);
                createStatefulToast("Error saving snap", ToastType.BAD);
                snapData.addFlag(FlagState.FAILED);
            } else {
                Logger.printFinalMessage("Unhandled save response", LogType.SAVING);
                createStatefulToast("Unhandled save response", ToastType.WARNING);
            }
        } catch (Exception e) {
            Logger.log("Error getting sent media", e);
        }
    }

    public static void performS2SSave() {
        SnapData currentSnapData = null;
        Logger.printTitle("Launching S2S", LogType.SAVING);
        if (currentSnapKey != null) {
            currentSnapData = hashSnapData.get(currentSnapKey);

            if (currentSnapData != null && currentSnapData.getSnapType() != null && relativeContext != null) {
                if (currentSnapData.getSnapType() == SnapType.STORY &&
                        Preferences.getInt(Prefs.SAVEMODE_STORY) != Preferences.SAVE_S2S &&
                        Preferences.getInt(Prefs.SAVEMODE_STORY) != Preferences.SAVE_F2S) {
                    Logger.printFinalMessage("Tried to perform story S2S from different mode", LogType.SAVING);
                    return;
                } else if (currentSnapData.getSnapType() == SnapType.SNAP &&
                        Preferences.getInt(Prefs.SAVEMODE_SNAP) != Preferences.SAVE_S2S &&
                        Preferences.getInt(Prefs.SAVEMODE_SNAP) != Preferences.SAVE_F2S) {
                    Logger.printFinalMessage("Tried to perform snap S2S from different mode", LogType.SAVING);
                    return;
                }
            }
        }

        Logger.printMessage("SnapData set: " + (currentSnapData != null), LogType.SAVING);
        performManualSnapDataSave(currentSnapData, relativeContext);
    }

    static void performButtonSave() {
        if (currentSnapKey != null) {
            performButtonSave(currentSnapKey);
        }
    }

    public static void performButtonSave(String mKey) {
        SnapData currentSnapData = null;
        Logger.printTitle("Launching BUTTON Save", LogType.SAVING);

        if (mKey != null) {
            Logger.printMessage("Checking key: " + mKey, LogType.SAVING);
            currentSnapData = hashSnapData.get(mKey);

            if (currentSnapData != null && currentSnapData.getSnapType() != null && relativeContext != null) {
                if (currentSnapData.getSnapType() == SnapType.STORY &&
                        Preferences.getInt(Prefs.SAVEMODE_STORY) != Preferences.SAVE_BUTTON) {
                    Logger.printFinalMessage("Tried to perform story button save from different mode", LogType.SAVING);
                    return;
                } else if (currentSnapData.getSnapType() == SnapType.SNAP
                        && Preferences.getInt(Prefs.SAVEMODE_SNAP) != Preferences.SAVE_BUTTON) {
                    Logger.printFinalMessage("Tried to perform snap button save from different mode", LogType.SAVING);
                    return;
                }
            }
        }

        Logger.printMessage("SnapData set: " + (currentSnapData != null), LogType.SAVING);
        performManualSnapDataSave(currentSnapData, relativeContext);
    }

    private static void performManualSnapDataSave(SnapData snapData, Context context) {
        if (snapData != null && context != null) {
            Logger.printMessage("Found SnapData to save", LogType.SAVING);
            Logger.printMessage("Key: " + snapData.getmKey(), LogType.SAVING);
            Logger.printMessage("Sender: " + obfus(snapData.getStrSender()), LogType.SAVING);
            Logger.printMessage("Timestamp: " + snapData.getStrTimestamp(), LogType.SAVING);
            Logger.printMessage("SnapType: " + snapData.getSnapType(), LogType.SAVING);
            Logger.printMessage("MediaType: " + snapData.getMediaType(), LogType.SAVING);
            printFlags(snapData);

            try {
                if (snapData.hasFlag(FlagState.COMPLETED) &&
                        !snapData.hasFlag(FlagState.SAVED)) {
                    selectSaveType(snapData, context);
                } else {
                    if (snapData.hasFlag(FlagState.SAVED)) {
                        createStatefulToast("Snap recently saved", ToastType.WARNING);
                        Logger.printFinalMessage("Snap recently saved", LogType.SAVING);
                    }
                }
            } catch (Exception e) {
                Logger.printFinalMessage("Exception saving snap", LogType.SAVING);
                createStatefulToast("Code exception saving snap", ToastType.BAD);
            }
        } else {
            Logger.printFinalMessage("No SnapData to save", LogType.SAVING);
            createStatefulToast("No SnapData to save", ToastType.WARNING);
        }
    }

    //UPDATED to 9.39.5
    private static void handleSnapHeader(Context context, Object receivedSnap) throws Exception {

        Logger.printTitle("Handling SnapData HEADER", LogType.SAVING);
        Logger.printMessage("Header object: " + receivedSnap.getClass().getCanonicalName(), LogType.SAVING);

        String mId = (String) getObjectField(receivedSnap, Obfuscator.save.OBJECT_MID);

        SnapType snapType;

        String className = receivedSnap.getClass().getCanonicalName();

        switch (className) {
            case Obfuscator.save.STORYSNAP_CLASS:
                snapType = SnapType.STORY;
                break;
            case Obfuscator.save.RECEIVEDSNAP_CLASS:
                snapType = SnapType.SNAP;
                break;
            default:
                Logger.printFinalMessage("Obfuscator out of date for SnapType in SAVING CLASS", LogType.SAVING);
                return;
        }

        Logger.printMessage("SnapType: " + snapType.name, LogType.SAVING);

        String mKey = mId;
        String strSender;

        if (snapType == SnapType.SNAP) {
            mKey += (String) getObjectField(receivedSnap, Obfuscator.save.OBJECT_CACHEKEYSUFFIX);
            strSender = (String) getObjectField(receivedSnap, "mSender");
        } else
            strSender = (String) getObjectField(receivedSnap, "mUsername");

        Logger.printMessage("Key: " + mKey, LogType.SAVING);
        Logger.printMessage("Sender: " + obfus(strSender), LogType.SAVING);

        SnapData snapData = hashSnapData.get(mKey);

        printFlags(snapData);

        if (snapData != null && scanForExisting(snapData, FlagState.HEADER)) {
            Logger.printFinalMessage("Existing SnapData with HEADER found", LogType.SAVING);
            return;
        } else if (snapData == null) {
            // If the snapdata doesn't exist, create a new one with the provided mKey
            Logger.printMessage("No SnapData found for Header... Creating new", LogType.SAVING);
            snapData = new SnapData(mKey);
            hashSnapData.put(mKey, snapData);
            Logger.printMessage("Hash Size: " + hashSnapData.size(), LogType.SAVING);
        }

        if (!snapData.hasFlag(FlagState.COMPLETED)) {
            long lngTimestamp = (Long) callMethod(receivedSnap, Obfuscator.save.SNAP_GETTIMESTAMP);
            Date timestamp = new Date(lngTimestamp);
            String strTimestamp = dateFormat.format(timestamp);

            Logger.printMessage("Timestamp: " + strTimestamp, LogType.SAVING);

            snapData.setHeader(mId, mKey, strSender, strTimestamp, snapType);
            Logger.printMessage("Header attached", LogType.SAVING);
        } else
            Logger.printMessage("Snap already completed", LogType.SAVING);

        if (shouldAutoSave(snapData)) {
            selectSaveType(snapData, context);
        } else {
            printFlags(snapData);
            Logger.printFinalMessage("Not saving this round", LogType.SAVING);
            currentSnapKey = snapData.getmKey();
            relativeContext = context;
        }
    }

    /**
     * Performs saving of the video stream into the HashMap
     *
     * @param context
     * @param param
     * @throws Exception
     */
    // UPDATED METHOD 9.39.5
    private static void handleVideoPayload(Context context, XC_MethodHook.MethodHookParam param)
            throws Exception {

        Logger.printTitle("Handling VIDEO Payload", LogType.SAVING);

        // Grab the MediaCache - Class: ahm
        Object mCache = param.args[0];

        if (mCache == null) {
            Logger.printFinalMessage("Null Cache passed", LogType.SAVING);
            return;
        }

        // Grab the MediaKey - Variable: ahm.mKey
        String mKey = (String) param.args[1];

        if (mKey == null) {
            Logger.printFinalMessage("Null Key passed", LogType.SAVING);
            return;
        }

        String parsedKey = StringUtils.stripKey(mKey);
        Logger.printMessage("Key: " + parsedKey, LogType.SAVING);

        // Grab the Key to Item Map (Contains file paths)
        @SuppressWarnings("unchecked")
        Map<String, Object> mKeyToItemMap =
                (Map<String, Object>) getObjectField(mCache, Obfuscator.save.CACHE_KEYTOITEMMAP);

        if (mKeyToItemMap == null) {
            Logger.printFinalMessage("Mkey-Item Map not found", LogType.SAVING);
            return;
        }

        // Attempt to get the item associated with the key
        Object item = mKeyToItemMap.get(mKey);

        if (item == null) {
            Logger.printMessage("Item not found with key:", LogType.SAVING);
            Logger.printFinalMessage(mKey, LogType.SAVING);
            return;
        }

        // Get the path of the video file
        String mAbsoluteFilePath = (String) getObjectField(item, Obfuscator.save.CACHE_ITEM_PATH);

        if (mAbsoluteFilePath == null) {
            Logger.printFinalMessage("No path object found", LogType.SAVING);
            return;
        }

        // Some pattern matching to trim down the filepath for logging
        String regex = "cache/uv/sesrh_dlw(.*?).mp4.nomedia";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mAbsoluteFilePath);

        if (matcher.find()) {
            try {
                Logger.printMessage("Path: " + matcher.group(0), LogType.SAVING);
            } catch (IndexOutOfBoundsException ignore) {
                Logger.printMessage("Path: " + mAbsoluteFilePath, LogType.SAVING);
            }
        } else
            Logger.printMessage("Path: " + mAbsoluteFilePath, LogType.SAVING);

        // Get the snapdata associated with the mKey above
        SnapData snapData = hashSnapData.get(parsedKey);

        // Print the snapdata's current flags
        printFlags(snapData);

        // Check if the snapdata exists and whether it has already been handled
        if (snapData != null && scanForExisting(snapData, FlagState.PAYLOAD)) {
            Logger.printFinalMessage("Tried to modify existing data", LogType.SAVING);
            return;
        } else if (snapData == null) {
            // If the snapdata doesn't exist, create a new one with the provided mKey
            Logger.printMessage("No SnapData found for Payload... Creating new", LogType.SAVING);
            snapData = new SnapData(parsedKey);
            hashSnapData.put(parsedKey, snapData);
            Logger.printMessage("Hash Size: " + hashSnapData.size(), LogType.SAVING);
        }

        if (!snapData.hasFlag(FlagState.COMPLETED)) {

            // Get the stream using the filepath provided
            FileInputStream video = new FileInputStream(mAbsoluteFilePath);

            // Assign the payload to the snapdata
            snapData.setPayload(video);
            Logger.printMessage("Successfully attached payload", LogType.SAVING);
        } else
            Logger.printMessage("Snap already completed", LogType.SAVING);

        // If set to button saving, do not save
        if (shouldAutoSave(snapData)) {
            selectSaveType(snapData, context);
        } else
            Logger.printFinalMessage("Not saving this round", LogType.SAVING);
    }

    /**
     * Performs saving of the image bitmap into the HashMap
     *
     * @param context
     * @param param
     * @throws Exception
     */
    // UPDATED TO LATEST 9.39.5
    private static void handleImagePayload(Context context, XC_MethodHook.MethodHookParam param)
            throws Exception {
        // Class: ahZ - holds the mKey for the payload
        Object keyholder = getObjectField(param.thisObject, Obfuscator.save.OBJECT_KEYHOLDERCLASSOBJECT);
        // Get the mKey out of ahZ
        String mKey = (String) getObjectField(keyholder, Obfuscator.save.OBJECT_KEYHOLDER_KEY);

        Bitmap bmp = (Bitmap) param.args[0];

        handleImagePayload(context, mKey, bmp);
    }

    private static void handleImagePayload(Context context, String mKey, Bitmap originalBmp)
            throws Exception {
        Logger.printTitle("Handling IMAGE Payload", LogType.SAVING);

        if (mKey == null) {
            Logger.printFinalMessage("Image Payload Null Key", LogType.SAVING);
            return;
        } else if (originalBmp == null) {
            Logger.printFinalMessage("Tried to attach Null Bitmap", LogType.SAVING);
            return;
        }

        Logger.printMessage("Key: " + mKey, LogType.SAVING);
        // Find the snapData associated with the mKey
        SnapData snapData = hashSnapData.get(mKey);

        // Display the snapData's current flags
        printFlags(snapData);

        // Check if the snapData has been processed
        if (snapData != null && scanForExisting(snapData, FlagState.PAYLOAD)) {
            Logger.printFinalMessage("Tried to modify existing data", LogType.SAVING);
            return;
        } else if (snapData == null) {
            Logger.printMessage("No SnapData found for Payload... Creating new", LogType.SAVING);
            snapData = new SnapData(mKey);
            hashSnapData.put(mKey, snapData);
            Logger.printMessage("Hash Size: " + hashSnapData.size(), LogType.SAVING);
        }

        if (!snapData.hasFlag(FlagState.COMPLETED)) {
            if (originalBmp.isRecycled()) {
                Logger.printFinalMessage("Bitmap is already recycled", LogType.SAVING);
                snapData.addFlag(FlagState.FAILED);
                createStatefulToast("Error saving image", ToastType.BAD);
                return;
            }

            Bitmap bmp = originalBmp.copy(Bitmap.Config.ARGB_8888, false);

            Logger.printMessage("Pulled Bitmap", LogType.SAVING);

            // Assign the payload to the snapData
            snapData.setPayload(bmp);

            Logger.printMessage("Successfully attached payload", LogType.SAVING);
        } else
            Logger.printMessage("Snap already completed", LogType.SAVING);

        if (shouldAutoSave(snapData)) {
            selectSaveType(snapData, context);
        } else
            Logger.printFinalMessage("Not saving this round", LogType.SAVING);
    }

    private static boolean shouldAutoSave(SnapData snapData) {
        Logger.printMessage("Performing saving checks", LogType.SAVING);
        if (!snapData.hasFlag(FlagState.COMPLETED)) {
            Logger.printMessage("COMPLETED flag not assigned", LogType.SAVING);
            return false;
        }

        Logger.printMessage("COMPLETED flag is assigned", LogType.SAVING);

        if (snapData.getSnapType() == null) {
            Logger.printMessage("Header not assigned", LogType.SAVING);
            snapData.removeFlag(FlagState.COMPLETED);
            snapData.removeFlag(FlagState.HEADER);
            return false;
        }

        Logger.printMessage("Passed header check", LogType.SAVING);

        if (snapData.getPayload() == null) {
            Logger.printMessage("Payload not assigned", LogType.SAVING);
            snapData.removeFlag(FlagState.PAYLOAD);
            snapData.removeFlag(FlagState.COMPLETED);
            return false;
        }

        Logger.printMessage("Passed payload checks", LogType.SAVING);

        if (snapData.getSnapType() == SnapType.SNAP &&
                Preferences.getInt(Prefs.SAVEMODE_SNAP) != Preferences.SAVE_AUTO) {
            Logger.printMessage("Snap save mode check failed", LogType.SAVING);
            return false;
        } else if (snapData.getSnapType() == SnapType.STORY &&
                Preferences.getInt(Prefs.SAVEMODE_STORY) != Preferences.SAVE_AUTO) {
            Logger.printMessage("Story save mode check failed", LogType.SAVING);
            return false;
        }

        Logger.printMessage("Save checks passed, moving on", LogType.SAVING);
        return true;
    }

    /**
     * Check if the snapData has already been handled
     *
     * @param snapData
     * @param flagState - Assign a flagstate to include (E.G PAYLOAD/HEADER)
     * @return True if contains any of the flags
     */
    private static boolean scanForExisting(SnapData snapData, FlagState flagState) {
        return snapData.hasFlag(FlagState.SAVED) ||
                snapData.hasFlag(flagState) ||
                snapData.hasFlag(FlagState.PROCESSING);
    }

    private static void selectSaveType(SnapData snapData, Context context) throws Exception {
        if (!snapData.hasFlag(FlagState.PROCESSING))
            snapData.addFlag(FlagState.PROCESSING);

        if (threadedSaveMode)
            new SaveThread(snapData, context).start();
        else if (asyncSaveMode)
            AsyncTaskCompat.executeParallel(new AsyncSaveSnapData(), context, snapData);
        else
            handleSave(context, snapData);
    }

    /**
     * Used to perform a save on a completed snapData object
     *
     * @param context
     * @param snapData
     * @throws Exception
     */
    private static void handleSave(Context context, SnapData snapData) throws Exception {
        // Ensure snapData is ready for saving
        if (snapData.hasFlag(FlagState.COMPLETED)) {
            Logger.printMessage("Saving Snap", LogType.SAVING);

            // Attempt to save the snap
            SaveResponse saveResponse = saveReceivedSnap(context, snapData);

            while (snapData.hasFlag(FlagState.PROCESSING))
                snapData.removeFlag(FlagState.PROCESSING);

            // Handle the response from the save attempt
            switch (saveResponse) {
                case SUCCESS: {
                    Logger.printMessage("Wiping payload and adding SAVED flag", LogType.SAVING);

                    // Wipe the payload to save memory
                    // Also assigns the SAVED flag to the snap
                    snapData.wipePayload();
                    snapData.setSaved();

                    createStatefulToast(snapData.getMediaType().typeName + " saved", ToastType.GOOD);

                    Logger.printFinalMessage("Snap Saving Completed", LogType.SAVING);
                    return;
                }
                case FAILED: {
                    Logger.printFinalMessage("Failed to save snap", LogType.SAVING);

                    // Assign a FAILED flag to the snap
                    // If the snap fails to save, a force close will likely be necessary
                    snapData.getFlags().clear();
                    snapData.addFlag(FlagState.FAILED);

                    String message = "Failed saving";

                    if (snapData.getMediaType() != null)
                        message += " " + snapData.getMediaType().typeName;

                    createStatefulToast(message, ToastType.BAD);

                    return;
                }
                case ONGOING: {
                    Logger.printFinalMessage("Handle save status ONGOING", LogType.SAVING);
                    return;
                }
                case EXISTING: {
                    createStatefulToast(
                            snapData.getMediaType().typeName + " already exists", ToastType.WARNING);

                    Logger.printMessage("Wiping payload and adding SAVED flag", LogType.SAVING);

                    // Wipe the payload to save memory
                    // Also assigns the SAVED flag to the snap
                    snapData.wipePayload();
                    snapData.setSaved();

                    Logger.printFinalMessage(
                            snapData.getMediaType().typeName + " already exists", LogType.SAVING);
                }
            }
        }
    }


    /**
     * If printFlags is true, will print the snapData's flag list
     *
     * @param snapData
     */
    private static void printFlags(SnapData snapData) {
        if (!printFlags)
            return;

        Logger.printMessage("Flags:", LogType.SAVING);

        if (snapData == null || snapData.getFlags().size() <= 0) {
            Logger.printMessage("-  NONE  -", LogType.SAVING);
            return;
        }

        // Loop through the list of states and print them
        for (FlagState flagState : snapData.getFlags())
            Logger.printMessage("-  " + flagState.toString() + "  -", LogType.SAVING);
    }

    /**
     * Perform a save on the snapData
     *
     * @param context
     * @param snapData
     * @return
     * @throws Exception
     */
    private static SaveResponse saveReceivedSnap(Context context, SnapData snapData) throws
            Exception {

        // Check if trying to save null snapData
        if (snapData == null) {
            Logger.printMessage("Null SnapData", LogType.SAVING);
            return SaveResponse.FAILED;
        } else if (!snapData.hasFlag(FlagState.COMPLETED)) {
            // If the snapData doesn't contains COMPLETED; Print out why and return
            String strMessage = snapData.hasFlag(FlagState.PAYLOAD) ? "PAYLOAD" :
                    "HEADER";
            Logger.printMessage("Tried to save snap without assigned " + strMessage, LogType.SAVING);
            return SaveResponse.ONGOING;
        } else if (snapData.hasFlag(FlagState.SAVED)) {
            Logger.printMessage("Tried to save a snap that has already been processed", LogType.SAVING);
            return SaveResponse.EXISTING;
        }

        // Get the snapData's payload
        Object payload = snapData.getPayload();

        // Check if it's null (Probably redundant)
        if (payload == null) {
            Logger.printMessage("Attempted to save Null Payload", LogType.SAVING);
            return SaveResponse.FAILED;
        }

        String filename = snapData.getStrSender() + "_" + snapData.getStrTimestamp();

        switch (snapData.getMediaType()) {
            case VIDEO: {
                Logger.printMessage("Video " + snapData.getSnapType().name + " opened", LogType.SAVING);

                return saveSnap(snapData.getSnapType(), MediaType.VIDEO, context, null,
                        (FileInputStream) payload, filename, snapData.getStrSender());
            }
            case IMAGE: {
                Logger.printMessage("Image " + snapData.getSnapType().name + " opened", LogType.SAVING);

                return saveSnap(snapData.getSnapType(), MediaType.IMAGE, context,
                        (Bitmap) payload, null, filename, snapData.getStrSender());
            }
            // TODO Include IMAGE_OVERLAY saving - Probably a quick job as it's already linked
            /*case IMAGE_OVERLAY: {
                int saveMode = mModeSave;
                if ( saveMode == DO_NOT_SAVE ) {
                    return false;
                } else if ( saveMode == SAVE_AUTO ) {
                    return saveSnap( snapData.getSnapType(), MediaType.IMAGE_OVERLAY, context, snapData.getImage(), null, filename, snapData.getSender() );
                }
                break;
            }*/
            default: {
                Logger.printMessage("Unknown MediaType", LogType.SAVING);
                return SaveResponse.FAILED;
            }
        }
    }

    /**
     * Perform a direct save of a snap
     *
     * @param snapType
     * @param mediaType
     * @param context
     * @param image
     * @param video
     * @param filename
     * @param sender
     * @return
     * @throws Exception
     */
    static SaveResponse saveSnap(SnapType snapType, MediaType mediaType, Context context,
                                 Bitmap image, FileInputStream video, String filename,
                                 String sender) throws Exception {
        File directory;

        try {
            directory = createFileDir(snapType.subdir, sender);
        } catch (IOException e) {
            Logger.log(e);
            return SaveResponse.FAILED;
        }

        if (mediaType == MediaType.IMAGE) {
            File imageFile = new File(directory, filename + MediaType.IMAGE.fileExtension);
            if (imageFile.exists()) {
                Logger.printMessage("Image already exists: " + obfus(filename), LogType.SAVING);
                SavingUtils.vibrate(context, false);
                return SaveResponse.EXISTING;
            }

            // the following code is somewhat redundant as it defeats the point of an async task
            // Perform an async save of the JPG
            return SavingUtils.saveJPG(imageFile, image, context) ?
                    SaveResponse.SUCCESS :
                    SaveResponse.FAILED;
        } else if (mediaType == MediaType.IMAGE_OVERLAY) {
            File overlayFile =
                    new File(directory, filename + "_overlay" + MediaType.IMAGE.fileExtension);

            if (Preferences.getBool(Prefs.OVERLAYS)) {
                if (overlayFile.exists()) {
                    Logger.printMessage("VideoOverlay already exists", LogType.SAVING);
                    SavingUtils.vibrate(context, false);
                    return SaveResponse.SUCCESS;
                }

                // the following code is somewhat redundant as it defeats the point of an async task
                // Perform an async save of the PNG
                return SavingUtils.savePNG(overlayFile, image, context) ?
                        SaveResponse.SUCCESS :
                        SaveResponse.FAILED;
            }
        } else if (mediaType == MediaType.VIDEO) {
            File videoFile = new File(directory, filename + MediaType.VIDEO.fileExtension);

            if (videoFile.exists()) {
                Logger.printMessage("Video already exists", LogType.SAVING);
                SavingUtils.vibrate(context, false);
                return SaveResponse.EXISTING;
            }

            // the following code is somewhat redundant as it defeats the point of an async task
            // Perform an async save of the PNG
            return SavingUtils.saveVideo(videoFile, video, context) ?
                    SaveResponse.SUCCESS :
                    SaveResponse.FAILED;
        }

        return SaveResponse.FAILED;
    }

    static void createStatefulToast(String message, ToastType type) {
        NotificationUtils.showStatefulMessage(message, type, lpparam2.classLoader);
    }

    private static File createFileDir(String category, String sender) throws IOException {
        String savePath = Preferences.getSavePath();
        if (savePath == null)
            savePath = Preferences.getContentPath();

        File directory = new File(savePath);

        if (Preferences.getBool(Prefs.SORT_BY_CATEGORY) || (Preferences.getBool(Prefs.SORT_BY_USERNAME) && sender == null)) {
            directory = new File(directory, category);
        }

        if (Preferences.getBool(Prefs.SORT_BY_USERNAME) && sender != null) {
            directory = new File(directory, sender);
        }

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory " + directory);
        }

        return directory;
    }

    public enum SnapType {
        SNAP("snap", "/ReceivedSnaps"),
        STORY("story", "/Stories"),
        SENT("sent", "/SentSnaps"),
        CHAT("chat", "/Chat");

        public final String name;
        public final String subdir;

        SnapType(String name, String subdir) {
            this.name = name;
            this.subdir = subdir;
        }
    }

    enum SaveResponse {
        SUCCESS, FAILED, ONGOING, EXISTING
    }

    enum MediaType {
        IMAGE(".jpg", "Image"),
        IMAGE_OVERLAY(".png", "Overlay"),
        VIDEO(".mp4", "Video");

        public final String fileExtension;
        public final String typeName;

        MediaType(String fileExtension, String typeName) {
            this.fileExtension = fileExtension;
            this.typeName = typeName;
        }
    }

    private static class AsyncSaveSnapData extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            Context context = (Context) params[0];
            SnapData snapData = (SnapData) params[1];

            Logger.printMessage("Performing ASYNC save", LogType.SAVING);

            try {
                Saving.handleSave(context, snapData);
                return true;
            } catch (Exception e) {
                Logger.log("Exception performing AsyncSave ", e, LogType.SAVING);
            }
            return false;
        }
    }

    private static class SaveThread extends Thread {
        private SnapData snapData;
        private Context context;

        SaveThread(SnapData snapData, Context context) {
            this.snapData = snapData;
            this.context = context;
        }

        public void run() {
            Logger.printMessage("(" + android.os.Process.myTid() + ")" + " Performing THREADED save", LogType.SAVING);

            try {
                Saving.handleSave(context, snapData);
            } catch (Exception e) {
                Logger.log("Exception performing Threaded Save ", e, LogType.SAVING);
            }

            Logger.log("Thread " + android.os.Process.myTid() + " finished and destroyed");
        }
    }
}