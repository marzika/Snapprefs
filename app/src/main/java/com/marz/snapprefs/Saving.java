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
import android.widget.Toast;

import com.marz.snapprefs.SnapData.FlagState;
import com.marz.snapprefs.Util.NotificationUtils;
import com.marz.snapprefs.Util.NotificationUtils.ToastType;
import com.marz.snapprefs.Util.SavingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class Saving {
    //public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    public static Resources mSCResources;
    public static Bitmap sentImage;
    public static Uri videoUri;
    public static XC_LoadPackage.LoadPackageParam lpparam2;
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault());
    private static SimpleDateFormat dateFormatSent =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    private static XModuleResources mResources;
    private static HashSet<Object> spamGuardSet = new HashSet<>();
    private static ConcurrentHashMap<String, SnapData> hashSnapData = new ConcurrentHashMap<>();
    private static boolean printFlags = true;
    private static String currentSnapKey;
    private static Context relativeContext;
    //TODO implement user selected save mode
    private static boolean asyncSaveMode = true;

    static void initSaving(final XC_LoadPackage.LoadPackageParam lpparam,
                           final XModuleResources modRes, final Context snapContext) {
        mResources = modRes;
        lpparam2 = lpparam;

        if (mSCResources == null) mSCResources = snapContext.getResources();
        Preferences.refreshPreferences();

        try {
            ClassLoader cl = lpparam.classLoader;

            /**
             * Called whenever a video is decrypted by snapchat
             * Will pre-load the next snap in the list
             */
            findAndHookConstructor(Obfuscator.save.DECRYPTEDSNAPVIDEO_CLASS, cl, findClass(
                    Obfuscator.save.CACHE_CLASS, cl), String.class, Bitmap.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            try {
                                handleVideoPayload(snapContext, param);
                            } catch (Exception e) {
                                Logger.log(
                                        "Exception handling Video Payload\n" +
                                                e.getMessage());
                            }
                        }
                    });

            /**
             * Called whenever a bitmap is set to the view (I believe)
             */
            findAndHookMethod(Obfuscator.save.IMAGESNAPRENDERER_CLASS +
                    "$1", cl, Obfuscator.save.IMAGESNAPRENDERER_NEW_BITMAP, Bitmap.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        handleImagePayload(snapContext, param);
                    } catch (Exception e) {
                        Logger.log("Exception handling Image Payload\n" + e.getMessage());
                    }
                }
            });

            /**
             * Called every time a snap is viewed - Quite reliable
             */
            findAndHookMethod(Obfuscator.save.RECEIVEDSNAP_CLASS, cl, Obfuscator.save
                    .RECEIVEDSNAP_BEING_SEEN, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    boolean isBeingViewed = (boolean) param.args[0];

                    if (isBeingViewed) {
                        Object obj = param.thisObject;

                        try {
                            handleSnapHeader(snapContext, obj);
                        } catch (Exception e) {
                            Logger.log("Exception handling HEADER\n" + e.getMessage());
                        }
                    }
                }
            });

            findAndHookMethod(Obfuscator.save.SNAPPREVIEWFRAGMENT_CLASS, lpparam.classLoader, "l", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if( Preferences.mSaveSentSnaps )
                            handleSentSnap(param.thisObject, snapContext);
                    } catch (Exception e) {
                        Logger.log("Error getting sent media", e);
                    }
                }
            });

            /**
             * We hook this method to set the CanonicalDisplayTime to our desired one if it is under
             * our limit and hide the counter if we need it.
             */

            findAndHookMethod(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader, Obfuscator.save.RECEIVEDSNAP_DISPLAYTIME, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    //Logger.afterHook("RECEIVEDSNAP - DisplayTime");
                    Double currentResult = (Double) param.getResult();
                    if (Preferences.mTimerUnlimited) {
                        findAndHookMethod("com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING);
                        param.setResult((double) 9999.9F);
                    } else {
                        if ((double) Preferences.mTimerMinimum !=
                                Preferences.TIMER_MINIMUM_DISABLED &&
                                currentResult < (double) Preferences.mTimerMinimum) {
                            param.setResult((double) Preferences.mTimerMinimum);
                        }
                    }
                }
            });
            if (Preferences.mHideTimer) {
                findAndHookMethod("com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING);
            }
            if (Preferences.mHideTimerStory) {
                findAndHookMethod("com.snapchat.android.ui.StoryTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING);
            }
            if (Preferences.mLoopingVids) {
                findAndHookMethod("com.snapchat.opera.shared.view.TextureVideoView", lpparam.classLoader, "start", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        callMethod(param.thisObject, "setLooping", true);
                    }
                });
                findAndHookMethod("com.snapchat.android.controller.countdown.SnapCountdownController ", lpparam.classLoader, "a", long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] =
                                3600000L;//It's how long you see video looping in milliseconds
                    }
                });
            }
            /**
             * We hook SnapView.a to determine wether we have stopped viewing the Snap.
             */
            findAndHookMethod(Obfuscator.save.SNAPVIEW_CLASS, lpparam.classLoader, Obfuscator.save.SNAPVIEW_HIDE, findClass(Obfuscator.save.ENDREASON_CLASS, lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Logger.afterHook("SNAPVIEW - Hide");
                    spamGuardSet.clear();
                }
            });
            findAndHookMethod("com.snapchat.android.stories.ui.StorySnapView", lpparam.classLoader, "a", findClass(Obfuscator.save.STORYVIEW_SHOW_FIRST, lpparam.classLoader), findClass("com.snapchat.android.ui.snapview.SnapViewSessionStopReason", lpparam.classLoader), int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Logger.afterHook("StorySnapView - Hide1");
                }
            });
        } catch (Exception e) {
            Logger.log("Error occured: Snapprefs doesn't currently support this version, wait for an update", e);

            findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Toast.makeText((Context) param.thisObject, "This version of snapchat is currently not supported by Snapprefs.", Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    public static void handleSentSnap(Object snapPreviewFragment, Context snapContext) {
        try {
            Logger.printTitle("Handling SENT snap");
            Activity activity = (Activity) callMethod(snapPreviewFragment, "getActivity");
            Object snapEditorView = getObjectField(snapPreviewFragment, "b");
            Object mediaBryo = getObjectField(snapEditorView, "p");

            if (mediaBryo == null) {
                Logger.printFinalMessage("MediaBryo not assigned - Halting process");
                return;
            }

            String mKey = (String) getObjectField(mediaBryo, "mClientId");
            Logger.printMessage("mKey: " + mKey);

            SnapData snapData = hashSnapData.get(mKey);

            if (snapData != null && !snapData.hasFlag(FlagState.FAILED)) {
                Logger.printFinalMessage("Snap already handled");
                return;
            } else if (snapData == null) {
                Logger.printMessage("SnapData not found - Creating new");
                snapData = new SnapData(mKey);
                snapData.setSnapType(SnapType.SENT);
                hashSnapData.put(mKey, snapData);
            }

            SaveResponse response = null;
            String filename = dateFormatSent.format(new Date());
            String bryoName = mediaBryo.getClass().getCanonicalName();

            Logger.printMessage("Saving with filename: " + filename);
            Logger.printMessage("MediaBryo Type: " + bryoName);

            if (bryoName.equals("VZ")) {
                Logger.printMessage("Media Type: VIDEO");
                Uri uri = (Uri) getObjectField(mediaBryo, "mVideoUri");

                if (uri == null)
                    response = SaveResponse.FAILED;
                else {
                    String regex = "preview/tracked_video_(.*?).mp4.nomedia";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(uri.toString());

                    if (matcher.find()) {
                        try {
                            Logger.printMessage("Original filename: " + matcher.group(0));
                        } catch (IndexOutOfBoundsException ignore) {
                            Logger.printMessage("Original filename: " + uri.getPath());
                        }
                    } else
                        Logger.printMessage("Original filename: " + uri.getPath());

                    Logger.printMessage("Uri valid - Trying to save");
                    FileInputStream videoStream = new FileInputStream(uri.getPath());
                    response = saveSnap(SnapType.SENT, MediaType.VIDEO,
                            snapContext, null, videoStream, filename, null);
                }
            } else if (bryoName.equals("VC")) {
                Logger.printMessage("Media Type: IMAGE");
                Bitmap bmp = (Bitmap) callMethod(snapEditorView, "a", activity, true);
                if (bmp != null) {
                    Logger.printMessage("Sent image found - Trying to save");
                    response = saveSnap(SnapType.SENT, MediaType.IMAGE,
                            snapContext, bmp, null, filename, null);
                }
            }

            if (response == null) {
                Logger.printMessage("Response not assigned - Assumed failed");
                response = SaveResponse.FAILED;
            }

            snapData.getFlags().clear();
            if (response == SaveResponse.SUCCESS) {
                Logger.printFinalMessage("Saved sent snap");
                createStatefulToast("Saved send snap", ToastType.GOOD);
                snapData.addFlag(FlagState.SAVED);
                return;
            } else if (response == SaveResponse.FAILED) {
                Logger.printFinalMessage("Error saving snap");
                createStatefulToast("Error saving snap", ToastType.BAD);
                snapData.addFlag(FlagState.FAILED);
                return;
            } else {
                Logger.printFinalMessage("Unhandled save response");
                createStatefulToast("Unhandled save response", ToastType.WARNING);
                return;
            }
        } catch (Exception e) {
            Logger.log("Error getting sent media", e);
        }
    }

    public static void performS2SSave() {
        SnapData currentSnapData = null;
        Logger.printTitle("Launching S2S");
        if (currentSnapKey != null) {
            currentSnapData = hashSnapData.get(currentSnapKey);

            if (currentSnapData != null && currentSnapData.getSnapType() != null && relativeContext != null) {
                if (currentSnapData.getSnapType() == SnapType.STORY &&
                        Preferences.mModeStory != Preferences.SAVE_S2S) {
                    Logger.printFinalMessage("Tried to perform story S2S from different mode");
                    return;
                } else if (currentSnapData.getSnapType() == SnapType.SNAP &&
                        Preferences.mModeSave != Preferences.SAVE_S2S) {
                    Logger.printFinalMessage("Tried to perform snap S2S from different mode");
                    return;
                }
            }
        }

        Logger.printMessage("SnapData set: " + (currentSnapData != null));
        performManualSnapDataSave(currentSnapData, relativeContext);
    }

    public static void performButtonSave() {
        SnapData currentSnapData = null;
        Logger.printTitle("Launching BUTTON Save");

        if (currentSnapKey != null) {
            currentSnapData = hashSnapData.get(currentSnapKey);

            if (currentSnapData != null && currentSnapData.getSnapType() != null && relativeContext != null) {
                if (currentSnapData.getSnapType() == SnapType.STORY &&
                        Preferences.mModeStory != Preferences.SAVE_BUTTON) {
                    Logger.printFinalMessage("Tried to perform story button save from different mode");
                    return;
                } else if (currentSnapData.getSnapType() == SnapType.SNAP
                        && Preferences.mModeSave != Preferences.SAVE_BUTTON) {
                    Logger.printFinalMessage("Tried to perform snap button save from different mode");
                    return;
                }
            }
        }

        Logger.printMessage("SnapData set: " + (currentSnapData != null));
        performManualSnapDataSave(currentSnapData, relativeContext);
    }

    public static void performManualSnapDataSave(SnapData snapData, Context context) {
        if (snapData != null && context != null) {
            Logger.printMessage("Found SnapData to save");
            Logger.printMessage("Key: " + snapData.getmKey());
            Logger.printMessage("Sender: " + snapData.getStrSender());
            Logger.printMessage("Timestamp: " + snapData.getStrTimestamp());
            Logger.printMessage("SnapType: " + snapData.getSnapType());
            Logger.printMessage("MediaType: " + snapData.getMediaType());
            printFlags(snapData);

            try {
                if (snapData.hasFlag(FlagState.COMPLETED) &&
                        !snapData.hasFlag(FlagState.SAVED)) {
                    snapData.addFlag(FlagState.PROCESSING);
                    if (asyncSaveMode) {
                        new AsyncSaveSnapData().execute(context, snapData);
                    } else
                        handleSave(context, snapData);
                } else {
                    if (snapData.hasFlag(FlagState.SAVED)) {
                        createStatefulToast("Snap recently saved", ToastType.GOOD);
                        Logger.printFinalMessage("Snap recently saved");
                    }
                }
            } catch (Exception e) {
                Logger.printFinalMessage("Exception saving snap");
                createStatefulToast("Code exception saving snap", ToastType.BAD);
            }
        } else {
            Logger.printFinalMessage("No SnapData to save");
            createStatefulToast("No SnapData to save", ToastType.WARNING);
        }
    }

    private static void handleSnapHeader(Context context, Object receivedSnap) throws Exception {
        Logger.printTitle("Handling SnapData HEADER");
        Logger.printMessage("Header object: " + receivedSnap.getClass().getCanonicalName());

        String mId = (String) getObjectField(receivedSnap, "mId");
        SnapType snapType =
                receivedSnap.getClass().getCanonicalName().equals("VK") ? SnapType.STORY :
                        SnapType.SNAP;

        Logger.printMessage("SnapType: " + snapType.name);

        String mKey = mId;
        String strSender;

        if (snapType == SnapType.SNAP) {
            mKey += (String) getObjectField(receivedSnap, "mCacheKeyInstanceSuffix");
            strSender = (String) getObjectField(receivedSnap, "mSender");
        } else
            strSender = (String) getObjectField(receivedSnap, "mUsername");

        Logger.printMessage("Key: " + mKey);
        Logger.printMessage("Sender: " + strSender);

        SnapData snapData = hashSnapData.get(mKey);

        printFlags(snapData);

        if (snapData != null && scanForExisting(snapData, FlagState.HEADER)) {
            Logger.printFinalMessage("Existing SnapData with HEADER found");
            return;
        } else if (snapData == null) {
            // If the snapdata doesn't exist, create a new one with the provided mKey
            Logger.printMessage("No SnapData found for Header... Creating new");
            snapData = new SnapData(mKey);
            hashSnapData.put(mKey, snapData);
            Logger.printMessage("Hash Size: " + hashSnapData.size());
        }

        long lngTimestamp = (Long) callMethod(receivedSnap, Obfuscator.save.SNAP_GETTIMESTAMP);
        Date timestamp = new Date(lngTimestamp);
        String strTimestamp = dateFormat.format(timestamp);

        Logger.printMessage("Timestamp: " + strTimestamp);

        snapData.setHeader(mId, mKey, strSender, strTimestamp, snapType);
        Logger.printMessage("Header attached");

        if (shouldAutoSave(snapData)) {
            snapData.addFlag(FlagState.PROCESSING);
            if (asyncSaveMode && snapData.hasFlag(FlagState.COMPLETED))
                new AsyncSaveSnapData().execute(context, snapData);
            else
                handleSave(context, snapData);
        } else {
            Logger.printFinalMessage("Not saving this round");
            printFlags(snapData);
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
    private static void handleVideoPayload(Context context, XC_MethodHook.MethodHookParam param)
            throws Exception {

        Logger.printTitle("Handling VIDEO Payload");

        // Grab the MediaCache - Class: ahJ
        Object mCache = param.args[0];

        if (mCache == null) {
            Logger.printFinalMessage("Null Cache passed");
            return;
        }

        // Grab the MediaKey - Variable: ahJ.mKey
        String mKey = (String) param.args[1];

        if (mKey == null) {
            Logger.printFinalMessage("Null Key passed");
            return;
        }

        Logger.printMessage("Key: " + mKey);

        // Grab the Key to Item Map (Contains file paths)
        @SuppressWarnings("unchecked")
        Map<String, Object> mKeyToItemMap =
                (Map<String, Object>) getObjectField(mCache, "mKeyToItemMap");

        if (mKeyToItemMap == null) {
            Logger.printFinalMessage("Mkey-Item Map not found");
            return;
        }

        // Attempt to get the item associated with the key
        Object item = mKeyToItemMap.get(mKey);

        if (item == null) {
            Logger.printMessage("Item not found with key:");
            Logger.printFinalMessage(mKey);
            return;
        }

        // Get the path of the video file
        String mAbsoluteFilePath = (String) getObjectField(item, "mAbsoluteFilePath");

        if (mAbsoluteFilePath == null) {
            Logger.printFinalMessage("No path object found");
            return;
        }

        // Some pattern matching to trim down the filepath for logging
        String regex = "cache/uv/sesrh_dlw(.*?).mp4.nomedia";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mAbsoluteFilePath);

        if (matcher.find()) {
            try {
                Logger.printMessage("Path: " + matcher.group(0));
            } catch (IndexOutOfBoundsException ignore) {
                Logger.printMessage("Path: " + mAbsoluteFilePath);
            }
        } else
            Logger.printMessage("Path: " + mAbsoluteFilePath);

        // Split the mKey as story videos are post-fixed with an extra code
        if (mKey.contains("#"))
            mKey = mKey.split("#")[0];

        // Get the snapdata associated with the mKey above
        SnapData snapData = hashSnapData.get(mKey);

        // Print the snapdata's current flags
        printFlags(snapData);

        // Check if the snapdata exists and whether it has already been handled
        if (snapData != null && scanForExisting(snapData, FlagState.PAYLOAD)) {
            Logger.printFinalMessage("Tried to modify existing data");
            return;
        } else if (snapData == null) {
            // If the snapdata doesn't exist, create a new one with the provided mKey
            Logger.printMessage("No SnapData found for Payload... Creating new");
            snapData = new SnapData(mKey);
            hashSnapData.put(mKey, snapData);
            Logger.printMessage("Hash Size: " + hashSnapData.size());
        }

        // Get the stream using the filepath provided
        FileInputStream video = new FileInputStream(mAbsoluteFilePath);

        // Assign the payload to the snapdata
        snapData.setPayload(video);
        Logger.printMessage("Successfully attached payload");

        // If set to button saving, do not save
        if (shouldAutoSave(snapData)) {
            snapData.addFlag(FlagState.PROCESSING);
            if (asyncSaveMode && snapData.hasFlag(FlagState.COMPLETED))
                new AsyncSaveSnapData().execute(context, snapData);
            else
                handleSave(context, snapData);
        } else
            Logger.printFinalMessage("Not saving this round");
    }

    /**
     * Performs saving of the image bitmap into the HashMap
     *
     * @param context
     * @param param
     * @throws Exception
     */
    public static void handleImagePayload(Context context, XC_MethodHook.MethodHookParam param)
            throws Exception {
        Logger.printTitle("Handling IMAGE Payload");
        Logger.printMessage("Getting Bitmap");

        // Class: ahZ - holds the mKey for the payload
        Object obj = getObjectField(param.thisObject, "b");
        // Get the mKey out of ahZ
        String mKey = (String) getObjectField(obj, "mKey");
        Logger.printMessage("Key: " + mKey);

        // Find the snapData associated with the mKey
        SnapData snapData = hashSnapData.get(mKey);

        // Display the snapData's current flags
        printFlags(snapData);

        // Check if the snapData has been processed
        if (snapData != null && scanForExisting(snapData, FlagState.PAYLOAD)) {
            Logger.printFinalMessage("Tried to modify existing data");
            return;
        } else if (snapData == null) {
            Logger.printMessage("No SnapData found for Payload... Creating new");
            snapData = new SnapData(mKey);
            hashSnapData.put(mKey, snapData);
            Logger.printMessage("Hash Size: " + hashSnapData.size());
        }

        // Get the bitmap payload
        Bitmap originalBmp = (Bitmap) param.args[0];

        if (originalBmp == null) {
            Logger.printFinalMessage("Tried to attach Null Bitmap");
            return;
        }

        if( originalBmp.isRecycled() )
        {
            Logger.printFinalMessage("Bitmap is already recycled");
            snapData.addFlag(FlagState.FAILED);
            createStatefulToast("Error saving image", ToastType.BAD);
            return;
        }

        Bitmap bmp = originalBmp.copy(Bitmap.Config.ARGB_8888, false);

        Logger.printMessage("Pulled Bitmap");

        // Assign the payload to the snapData
        snapData.setPayload(bmp);
        Logger.printMessage("Successfully attached payload");

        if (shouldAutoSave(snapData)) {
            snapData.addFlag(FlagState.PROCESSING);
            if (asyncSaveMode && snapData.hasFlag(FlagState.COMPLETED))
                new AsyncSaveSnapData().execute(context, snapData);
            else
                handleSave(context, snapData);
        } else
            Logger.printFinalMessage("Not saving this round");
    }

    private static boolean shouldAutoSave(SnapData snapData) {
        Logger.printMessage("Performing saving checks");
        if (!snapData.hasFlag(FlagState.COMPLETED)) {
            Logger.printMessage("COMPLETED flag not assigned");
            return false;
        }

        Logger.printMessage("COMPLETED flag is assigned");

        if (snapData.getSnapType() == null) {
            Logger.printMessage("Header not assigned");
            snapData.removeFlag(FlagState.COMPLETED);
            snapData.removeFlag(FlagState.HEADER);
            return false;
        }

        Logger.printMessage("Passed header check");

        if (snapData.getPayload() == null) {
            Logger.printMessage("Payload not assigned");
            snapData.removeFlag(FlagState.PAYLOAD);
            snapData.removeFlag(FlagState.COMPLETED);
            return false;
        }

        Logger.printMessage("Passed payload checks");

        if (snapData.getSnapType() == SnapType.SNAP &&
                (Preferences.mModeSave == Preferences.DO_NOT_SAVE ||
                        Preferences.mModeSave == Preferences.SAVE_BUTTON ||
                        Preferences.mModeSave == Preferences.SAVE_S2S)) {
            Logger.printMessage("Snap save mode check failed");
            return false;
        } else if (snapData.getSnapType() == SnapType.STORY &&
                (Preferences.mModeStory == Preferences.DO_NOT_SAVE ||
                        Preferences.mModeStory == Preferences.SAVE_BUTTON ||
                        Preferences.mModeStory == Preferences.SAVE_S2S)) {
            Logger.printMessage("Story save mode check failed");
            return false;
        }

        Logger.printMessage("Save checks passed, moving on");
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
        if (snapData.hasFlag(FlagState.SAVED))
            return true;
        else if (snapData.hasFlag(flagState))
            return true;
        else if (snapData.hasFlag(FlagState.PROCESSING))
            return true;
        else
            return false;
        /*return !snapData.hasFlag(FlagState.SAVED) ||
                (snapData.hasFlag(flagState) || snapData.hasFlag(FlagState.SAVED)) &&
                (!snapData.hasFlag(FlagState.FAILED) || !snapData.hasFlag(FlagState.COMPLETED));*/
    }

    /**
     * Used to perform a save on a completed snapData object
     *
     * @param context
     * @param snapData
     * @throws Exception
     */
    public static void handleSave(Context context, SnapData snapData) throws Exception {
        // Ensure snapData is ready for saving
        if (snapData.hasFlag(FlagState.COMPLETED)) {
            Logger.printMessage("Saving Snap");

            // Attempt to save the snap
            SaveResponse saveResponse = saveReceivedSnap(context, snapData);

            while (snapData.hasFlag(FlagState.PROCESSING))
                snapData.removeFlag(FlagState.PROCESSING);

            // Handle the response from the save attempt
            switch (saveResponse) {
                case SUCCESS: {
                    Logger.printMessage("Wiping payload and adding SAVED flag");

                    // Wipe the payload to save memory
                    // Also assigns the SAVED flag to the snap
                    snapData.wipePayload();
                    snapData.setSaved();

                    createStatefulToast(snapData.getMediaType().typeName + " saved", ToastType.GOOD);

                    Logger.printFinalMessage("Snap Saving Completed");
                    return;
                }
                case FAILED: {
                    Logger.printFinalMessage("Failed to save snap");

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
                    Logger.printFinalMessage("Handle save status ONGOING");
                    return;
                }
                case EXISTING: {
                    createStatefulToast(
                            snapData.getMediaType().typeName + " already exists", ToastType.WARNING);

                    Logger.printMessage("Wiping payload and adding SAVED flag");

                    // Wipe the payload to save memory
                    // Also assigns the SAVED flag to the snap
                    snapData.wipePayload();
                    snapData.setSaved();

                    Logger.printFinalMessage(
                            snapData.getMediaType().typeName + " already exists");
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

        Logger.printMessage("Flags:");

        if (snapData == null || snapData.getFlags().size() <= 0) {
            Logger.printMessage("-  NONE  -");
            return;
        }

        // Loop through the list of states and print them
        for (FlagState flagState : snapData.getFlags())
            Logger.printMessage("-  " + flagState.toString() + "  -");
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
            Logger.printMessage("Null SnapData");
            return SaveResponse.FAILED;
        } else if (!snapData.hasFlag(FlagState.COMPLETED)) {
            // If the snapData doesn't contains COMPLETED; Print out why and return
            String strMessage = snapData.hasFlag(FlagState.PAYLOAD) ? "PAYLOAD" :
                    "HEADER";
            Logger.printMessage("Tried to save snap without assigned " + strMessage);
            return SaveResponse.ONGOING;
        } else if (snapData.hasFlag(FlagState.SAVED)) {
            Logger.printMessage("Tried to save a snap that has already been processed");
            return SaveResponse.EXISTING;
        }

        // Get the snapData's payload
        Object payload = snapData.getPayload();

        // Check if it's null (Probably redundant)
        if (payload == null) {
            Logger.printMessage("Attempted to save Null Payload");
            return SaveResponse.FAILED;
        }

        String filename = snapData.getStrSender() + "_" + snapData.getStrTimestamp();

        switch (snapData.getMediaType()) {
            case VIDEO: {
                Logger.printMessage("Video " + snapData.getSnapType().name + " opened");

                return saveSnap(snapData.getSnapType(), MediaType.VIDEO, context, null,
                        (FileInputStream) payload, filename, snapData.getStrSender());
            }
            case IMAGE: {
                Logger.printMessage("Image " + snapData.getStrSender() + " opened");

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
                Logger.printMessage("Unknown MediaType");
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
    public static SaveResponse saveSnap(SnapType snapType, MediaType mediaType, Context context,
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
                Logger.printMessage("Image already exists: " + filename);
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

            if (Preferences.mOverlays) {
                if (overlayFile.exists()) {
                    Logger.printMessage("VideoOverlay already exists");
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
                Logger.printMessage("Video already exists");
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

    public static void createStatefulToast(String message, ToastType type) {
        NotificationUtils.showStatefulMessage(message, type, lpparam2.classLoader);
    }

    public static File createFileDir(String category, String sender) throws IOException {
        File directory = new File(Preferences.mSavePath);

        if (Preferences.mSortByCategory || (Preferences.mSortByUsername && sender == null)) {
            directory = new File(directory, category);
        }

        if (Preferences.mSortByUsername && sender != null) {
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

    public enum SaveResponse {
        SUCCESS, FAILED, ONGOING, EXISTING
    }

    public enum MediaType {
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

    public static class AsyncSaveSnapData extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            Context context = (Context) params[0];
            SnapData snapData = (SnapData) params[1];

            Logger.printMessage("Performing ASYNC save");

            try {
                Saving.handleSave(context, snapData);
                return true;
            } catch (Exception e) {
                Logger.log("Exception performing AsyncSave ", e);
            }
            return false;
        }
    }
}