package com.marz.snapprefs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.widget.ImageView;

import com.marz.snapprefs.Util.NotificationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by MARZ on 2016. 04. 21..
 */
public class NewSaving {
    // Length of toasts
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;

    public static boolean mToastEnabled = true;
    public static boolean mVibrationEnabled = true;
    public static int mToastLength = TOAST_LENGTH_LONG;
    public static String mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
    private static String lastId = null;
    private static Object toSave = null;
    private static SnapType type;
    private static MediaType mediaType;
    private static Bitmap overlay = null;
    private static long timestamp = 0;
    private static String sender = null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault());

    public static void initSaving(final XC_LoadPackage.LoadPackageParam lpparam) {
        final Class<?> ImageSnapRendererClass = XposedHelpers.findClass("Og", lpparam.classLoader);
        final Class<?> VideoSnapRendererClass = XposedHelpers.findClass("Oj", lpparam.classLoader);
        final Class<?> FilterVideoViewRendererPlayerClass = XposedHelpers.findClass("On", lpparam.classLoader);
        final Class<?> VideoViewRendererPlayerClass = XposedHelpers.findClass("Op", lpparam.classLoader);
        final Class<?> StorySnapClass = XposedHelpers.findClass("LR", lpparam.classLoader);
        XposedHelpers.findAndHookMethod("Sl", lpparam.classLoader, "i", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object snap = XposedHelpers.getObjectField(param.thisObject, "c");
                if (lastId != null && lastId.equals(XposedHelpers.callMethod(snap, "d"))) {
                    Logger.log("Called second time? we should return", true);
                    return;
                }
                overlay = null;
                lastId = (String) XposedHelpers.callMethod(snap, "d");
                if (StorySnapClass.isInstance(snap)) {
                    sender = (String) XposedHelpers.getObjectField(snap, "mUsername");
                    type = SnapType.STORY;
                } else {
                    sender = (String) XposedHelpers.getObjectField(snap, "mSender");
                    type = SnapType.SNAP;
                }
                timestamp = XposedHelpers.getLongField(snap, "mTimestamp");//true, leaving it here for the filename dateFormat.format(timestamp);
                Object renderer = XposedHelpers.getObjectField(param.thisObject, "g");
                if (ImageSnapRendererClass.isInstance(renderer)) {
                    mediaType = MediaType.IMAGE;
                    ImageView h = (ImageView) XposedHelpers.getObjectField(renderer, "h");
                    if (h != null && h.getDrawable() != null && h.getDrawable() instanceof BitmapDrawable) {
                        XposedBridge.log("Here add saving");
                        Bitmap bitmap = ((BitmapDrawable) h.getDrawable()).getBitmap();
                        toSave = bitmap;
                        save();
                    }
                } else if (VideoSnapRendererClass.isInstance(renderer)) {
                    mediaType = MediaType.VIDEO;
                    Object Oo = XposedHelpers.getObjectField(renderer, "b");
                    if (Oo != null) {
                        if (FilterVideoViewRendererPlayerClass.isInstance(Oo)) {
                            Object VideoFilterView = XposedHelpers.getObjectField(Oo, "e");
                            if (VideoFilterView != null) {
                                Uri uri = (Uri) XposedHelpers.getObjectField(VideoFilterView, "b");
                                if (uri != null) {
                                    XposedBridge.log("Here add saving - FVVRPC");
                                    FileInputStream fis = new FileInputStream(uri.getPath());
                                    toSave = fis;
                                    try {
                                        overlay = ((BitmapDrawable) ((ImageView) XposedHelpers.getObjectField(snap, "a")).getDrawable()).getBitmap();
                                    } catch (NullPointerException ignore) {
                                        Logger.log("It's without an overlay", true);
                                    }
                                    save();
                                }
                            }
                        } else if (VideoViewRendererPlayerClass.isInstance(Oo)) {
                            Object SnapVideoView = XposedHelpers.getObjectField(Oo, "c");
                            if (SnapVideoView != null) {
                                Class<?> clz = XposedHelpers.findClass("com.snapchat.android.ui.TextureVideoView", lpparam.classLoader);
                                Field b = clz.getDeclaredField("b");
                                b.setAccessible(true);
                                Uri uri = (Uri) b.get(SnapVideoView);
                                if (uri != null) {
                                    XposedBridge.log("Here add saving - VVRPC");
                                    FileInputStream fis = new FileInputStream(uri.getPath());
                                    toSave = fis;
                                    save();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private static void save() {
        String filename = sender + "-" + dateFormat.format(timestamp) + mediaType.fileExtension;
        File fileToSave = new File(mSavePath + type.subdir + filename);
        Logger.log("Saving a " + type.name + " " + mediaType.fileExtension + " from " + sender + ", sent at " + dateFormat.format(timestamp), true);
        switch (mediaType) {
            case IMAGE: {
                new saveImageJPGTask().execute(fileToSave, toSave, HookMethods.SnapContext);
                break;
            }
            case VIDEO: {
                new saveVideoTask().execute(toSave, fileToSave, HookMethods.SnapContext);
                if (overlay != null) {
                    String overlayName = sender + "-" + dateFormat.format(timestamp) + "_overlay" + MediaType.IMAGE_OVERLAY.fileExtension;
                    File overlayToSave = new File(mSavePath + type.subdir + overlayName);
                    new saveImagePNGTask().execute(overlayToSave, overlay, HookMethods.SnapContext);
                }
                break;
            }
            default: {
                Logger.log("invalid mediaType", true);
                break;
            }
        }
    }

    private static void runMediaScanner(Context context, String... mediaPath) {
        try {
            Logger.log("MediaScanner started");
            MediaScannerConnection.scanFile(context, mediaPath, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Logger.log("MediaScanner scanned file: " + uri.toString());
                        }
                    });
        } catch (Exception e) {
            Logger.log("Error occurred while trying to run MediaScanner", e);
        }
    }

    private static int getToastLength() {
        if (mToastLength == TOAST_LENGTH_SHORT) {
            return NotificationUtils.LENGHT_SHORT;
        } else {
            return NotificationUtils.LENGHT_LONG;
        }
    }

    private static void vibrate(Context context, boolean success) {
        if (mVibrationEnabled) {
            if (success) {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(genVibratorPattern(0.7f, 400), -1);
            } else {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(genVibratorPattern(1.0f, 700), -1);
            }
        }
    }

    //http://stackoverflow.com/questions/20808479/algorithm-for-generating-vibration-patterns-ranging-in-intensity-in-android/20821575#20821575
    // intensity 0-1
    // duration mS
    public static long[] genVibratorPattern(float intensity, long duration) {
        float dutyCycle = Math.abs((intensity * 2.0f) - 1.0f);
        long hWidth = (long) (dutyCycle * (duration - 1)) + 1;
        long lWidth = dutyCycle == 1.0f ? 0 : 1;

        int pulseCount = (int) (2.0f * ((float) duration / (float) (hWidth + lWidth)));
        long[] pattern = new long[pulseCount];

        for (int i = 0; i < pulseCount; i++) {
            pattern[i] = intensity < 0.5f ? (i % 2 == 0 ? hWidth : lWidth) : (i % 2 == 0 ? lWidth : hWidth);
        }

        return pattern;
    }

    public enum MediaType {
        IMAGE(".jpg"),
        IMAGE_OVERLAY(".png"),
        VIDEO(".mp4");

        private final String fileExtension;

        MediaType(String fileExtension) {
            this.fileExtension = fileExtension;
        }
    }

    public enum SnapType {
        SNAP("snap", "/ReceivedSnaps/"),
        STORY("story", "/Stories/"),
        SENT("sent", "/SentSnaps/"),
        CHAT("chat", "/Chat/");

        private final String name;
        private final String subdir;

        SnapType(String name, String subdir) {
            this.name = name;
            this.subdir = subdir;
        }
    }

    public static class saveImageJPGTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            Boolean success;
            File fileToSave = (File) params[0];
            Bitmap bmp = (Bitmap) params[1];
            Context context = (Context) params[2];
            try {
                FileOutputStream out = new FileOutputStream(fileToSave);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                vibrate(context, true);
                Logger.log("Image has been saved");
                Logger.log("Path: " + fileToSave.toString());
                runMediaScanner(context, fileToSave.getAbsolutePath());

                success = true;
            } catch (Exception e) {
                Logger.log("Exception while saving an image", e);
                vibrate(context, false);
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            String message;
            int color;
            if (result) {
                message = "Image saved";
                color = Color.GREEN;
            } else {
                message = "Error while saving";
                color = Color.RED;
            }
            NotificationUtils.showMessage(message, color, getToastLength(), HookMethods.classLoader);
        }
    }

    public static class saveImagePNGTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            Boolean success;
            File fileToSave = (File) params[0];
            Bitmap bmp = (Bitmap) params[1];
            Context context = (Context) params[2];
            try {
                FileOutputStream out = new FileOutputStream(fileToSave);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                vibrate(context, true);
                Logger.log("Image has been saved");
                Logger.log("Path: " + fileToSave.toString());
                runMediaScanner(context, fileToSave.getAbsolutePath());

                success = true;
            } catch (Exception e) {
                Logger.log("Exception while saving an image", e);
                vibrate(context, false);
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            String message;
            int color;
            if (result) {
                message = "Image saved";
                color = Color.GREEN;
            } else {
                message = "Error while saving";
                color = Color.RED;
            }
            NotificationUtils.showMessage(message, color, getToastLength(), HookMethods.classLoader);
        }
    }

    public static class saveVideoTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            Boolean success;
            FileInputStream video = (FileInputStream) params[0];
            File fileToSave = (File) params[1];
            Context context = (Context) params[2];
            try {
                FileInputStream in = video;
                //Logger.log(in.toString(), true);
                FileOutputStream out = new FileOutputStream(fileToSave);
                //Logger.log(out.toString(), true);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.flush();
                out.close();
                vibrate(context, true);
                Logger.log("Video has been saved");
                Logger.log("Path: " + fileToSave.toString());
                runMediaScanner(context, fileToSave.getAbsolutePath());
                success = true;
            } catch (Exception e) {
                Logger.log("Exception while saving a video", e);
                vibrate(context, false);
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            String message;
            int color;
            if (result) {
                message = "Video saved";
                color = Color.GREEN;
            } else {
                message = "Error while saving";
                color = Color.RED;
            }
            NotificationUtils.showMessage(message, color, getToastLength(), HookMethods.classLoader);
        }
    }
}
