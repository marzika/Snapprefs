package com.marz.snapprefs;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.URLUtil;

import com.marz.snapprefs.Util.CommonUtils;
import com.marz.snapprefs.Util.ImageUtils;
import com.marz.snapprefs.Util.VideoUtils;
import com.marz.snapprefs.Util.XposedUtils;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Sharing {

    private static XModuleResources mResources;
    private static Uri initializedUri;
    private static int snapchatVersion;

    static void initSharing(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        mResources = modRes;
        try {
            XposedUtils.log("----------------- SNAPSHARE HOOKED -----------------", false);
            Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
            Context context = (Context) callMethod(activityThread, "getSystemContext");

            PackageInfo piSnapChat = context.getPackageManager().getPackageInfo(lpparam.packageName, 0);
            XposedUtils.log("SnapChat Version: " + piSnapChat.versionName + " (" + piSnapChat.versionCode + ")", false);
            XposedUtils.log("SnapPrefs Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")", false);
        } catch (Exception e) {
            XposedUtils.log("Exception while trying to get version info", e);
            return;
        }

        final Class snapCapturedEventClass = findClass(Obfuscator.sharing.SNAPCAPTUREDEVENT_CLASS, lpparam.classLoader);
        final Class SnapCaptureContext = findClass(Obfuscator.sharing.SNAPCAPTURECONTEXT_CLASS, lpparam.classLoader);
        final Media mediaImg = new Media(); // a place to store the image
        final Media mediaVid = new Media(); // a place to store the video

        // This is where the media is loaded and transformed. Hooks after the onCreate() call of the main Activity.
        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HookMethods.refreshPreferences();
                XposedUtils.log("----------------- SNAPSHARE STARTED -----------------", false);
                final Activity activity = (Activity) param.thisObject;
                // Get intent, action and MIME type
                Intent intent = activity.getIntent();
                String type = intent.getType();
                String action = intent.getAction();
                XposedUtils.log("Intent type: " + type + ", intent action:" + action);

                // Check if this is a normal launch of Snapchat or actually called by Snapshare and if loaded from recents
                if (type != null && Intent.ACTION_SEND.equals(action) && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                    Uri mediaUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    // Check for bogus call
                    if (mediaUri == null) {
                        return;
                    }
                    /* We check if the current media got already initialized and should exit instead
                     * of doing the media initialization again. This check is necessary
                     * because onCreate() is also called if the phone is just rotated. */
                    if (initializedUri == mediaUri) {
                        XposedUtils.log("Media already initialized, exit onCreate() hook");
                        return;
                    }

                    ContentResolver contentResolver = activity.getContentResolver();

                    if (type.startsWith("image/")) {
                        XposedUtils.log("Image URI: " + mediaUri.toString());
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, mediaUri);
                            XposedUtils.log("Image shared, size: " + bitmap.getWidth() + " x " + bitmap.getHeight() + " (w x h)");

                            String filePath = mediaUri.getPath();
                            if (mediaUri.getScheme().equals("content")) {
                                filePath = CommonUtils.getPathFromContentUri(contentResolver, mediaUri);
                                XposedUtils.log("Converted content URI to file path " + filePath);
                            }
                            // Rotate image using EXIF-data
                            bitmap = ImageUtils.rotateUsingExif(bitmap, filePath);
                            // Landscape images have to be rotated 90 degrees clockwise for Snapchat to be displayed correctly
                            if (Common.ROTATION_MODE != Common.ROTATION_NONE) {
                                if (bitmap.getWidth() > bitmap.getHeight()) {
                                    XposedUtils.log("Landscape image detected, rotating image " + Common.ROTATION_MODE + " degrees");
                                    bitmap = ImageUtils.rotateBitmap(bitmap, Common.ROTATION_MODE);
                                } else {
                                    XposedUtils.log("Image is in portrait, rotation not needed");
                                }
                            }

                            // Snapchat will break if the image is too large and it will scale the image up if the Display rectangle is larger than the image.
                            ImageUtils imageUtils = new ImageUtils(activity);
                            switch (Common.ADJUST_METHOD) {
                                case Common.ADJUST_CROP:
                                    XposedUtils.log("Adjustment Method: Crop");
                                    bitmap = imageUtils.adjustmentMethodCrop(bitmap);
                                    break;
                                case Common.ADJUST_SCALE:
                                    XposedUtils.log("Adjustment Method: Scale");
                                    bitmap = imageUtils.adjustmentMethodScale(bitmap);
                                    break;
                                case Common.ADJUST_NONE:
                                    XposedUtils.log("Adjustment Method: None");
                                    bitmap = imageUtils.adjustmentMethodNone(bitmap);
                                    break;
                            }

                            // Make Snapchat show the image
                            mediaImg.setContent(bitmap);
                        } catch (Exception e) {
                            XposedUtils.log(e);
                            return;
                        }
                    } else if (type.startsWith("video/")) {
                        Uri videoUri;
                        // Snapchat expects the video URI to be in the file:// scheme, not content:// scheme
                        if (URLUtil.isFileUrl(mediaUri.toString())) {
                            videoUri = mediaUri;
                            XposedUtils.log("Already had File URI: " + mediaUri.toString());
                        } else { // No file URI, so we have to convert it
                            videoUri = CommonUtils.getFileUriFromContentUri(contentResolver, mediaUri);
                            if (videoUri != null) {
                                XposedUtils.log("Converted content URI to file URI " + videoUri.toString());
                            } else {
                                XposedUtils.log("Couldn't resolve URI to file:// scheme: " + mediaUri.toString());
                                return;
                            }
                        }

                        File videoFile = new File(videoUri.getPath());
                        File tempFile = File.createTempFile("snapshare_video", null);

                        try {
                            if (Common.ROTATION_MODE == Common.ROTATION_NONE) {
                                XposedUtils.log("Rotation disabled, creating a temporary copy");
                                CommonUtils.copyFile(videoFile, tempFile);
                            } else {
                                VideoUtils.rotateVideo(videoFile, tempFile);
                            }

                            videoUri = Uri.fromFile(tempFile);
                            XposedUtils.log("Temporary file path: " + videoUri);
                        } catch (Exception e) {
                            XposedUtils.log(e);
                            return;
                        }

                        long fileSize = tempFile.length();
                        // Get size of video and compare to the maximum size
                        if (Common.CHECK_SIZE && fileSize > Common.MAX_VIDEO_SIZE) {
                            String readableFileSize = CommonUtils.formatBytes(fileSize);
                            String readableMaxSize = CommonUtils.formatBytes(Common.MAX_VIDEO_SIZE);
                            XposedUtils.log("Video might be too big (" + readableFileSize + ")");
                            // Inform the user with a dialog
                            //createSizeDialog(activity, readableFileSize, readableMaxSize).show();
                        }
                        mediaVid.setContent(videoUri);
                    }

                    /**
                     * Mark image as initialized
                     * @see initializedUri
                     */
                    createDialog(activity).show();
                    initializedUri = mediaUri;
                } else {
                    XposedUtils.log("Regular call of Snapchat.");
                    initializedUri = null;
                }
            }

        });

        /**
         * We want to send our media once the camera is ready, that's why we hook the refreshFlashButton/onCameraStateEvent method.
         * The media is injected by calling the eventbus to send a snapcapture event with our own media.
         */

        XC_MethodHook cameraLoadedHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HookMethods.refreshPreferences(); // Refresh preferences for captions
                if (initializedUri == null) {
                    return; // We don't have an image to send, so don't try to send one
                }

                XposedUtils.log("Doing it's magic!");
                Object snapCaptureEvent;
                /*findAndHookMethod("com.snapchat.android.camera.CameraFragment", lpparam.classLoader, "a", Bitmap.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = mediaImg.getContent();
                        Logger.log("Set media in camerafragment", true);
                    }
                });*/

                findAndHookMethod("com.snapchat.android.camera.CameraFragment", lpparam.classLoader, "a", Bitmap.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = mediaImg.getContent();
                        Logger.log("Set IMAGE media in camerafragment", true);
                    }
                });
                findAndHookMethod("com.snapchat.android.camera.CameraFragment", lpparam.classLoader, "a", Uri.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = mediaVid.getContent();
                        Logger.log("Set Video media in camerafragment", true);
                    }
                });

                // Since 4.1.10 a new Class called Snapbryo stores all the data for snaps
                // SnapCapturedEvent(Snapbryo(Builder(Media)))
                //    Object builder = newInstance(findClass("com.snapchat.android.model.Snapbryo.Builder", lpparam.classLoader));
                //   builder = callMethod(builder, Obfuscator_share.BUILDER_CONSTRUCTOR.getValue(snapchatVersion), media.getContent());
                //  Object snapbryo = callMethod(builder, Obfuscator_share.CREATE_SNAPBRYO.getValue(snapchatVersion));
                //Object snapbryo = media.getContent();
                //snapCaptureEvent = newInstance(snapCapturedEventClass, snapbryo, SnapCaptureContext.getEnumConstants()[0]); //SNAPCAPTURECONTEXT

                // Call the eventbus to post our SnapCapturedEvent, this will take us to the SnapPreviewFragment
                //Object busProvider = callStaticMethod(findClass(Obfuscator.sharing.BUSPROVIDER_CLASS, lpparam.classLoader), Obfuscator.sharing.BUSPROVIDER_RETURNBUS);//upd. below
                //callMethod(busProvider, "a", snapCaptureEvent);
                // Clean up after ourselves, otherwise snapchat will crash
                initializedUri = null;
            }
        };

        // In 5.0.36.0 (beta) refreshFlashButton was removed, we use onCameraStateEvent instead
        Class<?> cameraStateEventClass = findClass(Obfuscator.sharing.CAMERASTATEEVENT_CLASS, lpparam.classLoader);
        findAndHookMethod("com.snapchat.android.camera.CameraFragment", lpparam.classLoader, "onCameraStateEvent", cameraStateEventClass, cameraLoadedHook);
        XposedUtils.log("Hooked onCameraStateEvent");
    }

    //SNAPSHARE

    /**
     * Creates a dialog saying the image is too large. Two options are given: continue or quit.
     *
     * @param activity         The activity to be used to create the dialog
     * @return The dialog to show
     */
    private static AlertDialog createDialog(final Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(mResources.getString(R.string.app_name));
        dialogBuilder.setMessage(mResources.getString(R.string.dialog_main));
        dialogBuilder.setPositiveButton(mResources.getString(R.string.continue_anyway), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        dialogBuilder.setNegativeButton(mResources.getString(R.string.go_back), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.finish();
            }
        });
        return dialogBuilder.create();
    }
}
