package com.marz.snapprefs;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.marz.snapprefs.Util.NotificationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getLongField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;

public class Saving {

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
    //New Array Saving Method (ddmanfire)
    public static LinkedHashMap<Integer, Snap> snapsMap = new LinkedHashMap<>();
    public static int currentViewingSnap = -1;
    public static int currentSavingSnap = -1;
    // Preferences and their default values
    public static int mModeSnapImage = SAVE_AUTO;
    public static int mModeSnapVideo = SAVE_AUTO;
    public static int mModeStoryImage = SAVE_AUTO;
    public static int mModeStoryVideo = SAVE_AUTO;
    public static int mTimerMinimum = TIMER_MINIMUM_DISABLED;
    public static boolean mTimerUnlimited = true;
    public static boolean mHideTimerStory = false;
    public static boolean mLoopingVids = true;
    public static boolean mHideTimer = false;
    public static boolean mToastEnabled = true;
    public static boolean mVibrationEnabled = true;
    public static int mToastLength = TOAST_LENGTH_LONG;
    public static String mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
    public static boolean mSaveSentSnaps = false;
    public static boolean mSortByCategory = true;
    public static boolean mSortByUsername = true;
    public static boolean mDebugging = true;
    public static boolean mOverlays = true;
    public static boolean viewingSnap;
    public static Object receivedSnap;
    public static Object storyReceivedSnap;
    public static Object oldreceivedSnap;
    public static boolean usedOldReceivedSnap = false;
    public static Resources mSCResources;
    public static FileInputStream mVideo;
    public static Bitmap mImage;
    public static Bitmap sentImage;
    public static Bitmap lastSavedBitmap;
    public static Uri videoUri;
    public static Uri lastSavedUri;
    public static ClassLoader snapCL;
    public static Bitmap image;
    public static FileInputStream video;
    static XSharedPreferences prefs;
    static SnapType lastSnapType;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault());
    private static SimpleDateFormat dateFormatSent = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    private static XModuleResources mResources;
    private static GestureModel gestureModel;
    private static Class<?> storySnap;
    private static int screenHeight;
    private static long storyTimestamp;
    public static XC_LoadPackage.LoadPackageParam lpparam2;


    static void newSaveMethod(FileInputStream mVideo, Bitmap mImage, boolean isOverlay) {
        if (mVideo == null && mImage == null) {
            Logger.log("Skipping null");
        } else {
            currentSavingSnap++;
            if (mImage == null)
                snapsMap.put(currentSavingSnap, new Snap(mVideo));
            else if (mVideo == null)
                snapsMap.put(currentSavingSnap, new Snap(mImage, isOverlay));
            Logger.log("Added To HashMap!");
        }
    }

    static void newSaveMethod2(Context snapContext) {
        Snap toSave = snapsMap.get(currentViewingSnap);
        if (toSave.getMediaType() == MediaType.IMAGE) {
            mImage = toSave.getImage();
            saveReceivedSnap(snapContext, receivedSnap, MediaType.GESTUREDIMAGE);
        } else if (toSave.getMediaType() == MediaType.VIDEO) {
            mVideo = toSave.getVideo();
            saveReceivedSnap(snapContext, receivedSnap, MediaType.GESTUREDVIDEO);
        } else if (toSave.getMediaType() == MediaType.IMAGE_OVERLAY) {
            mImage = toSave.getImage();
            saveReceivedSnap(snapContext, receivedSnap, MediaType.GESTUREDOVERLAY);
        }
        Logger.log("Saving Done!");
    }
    static void initSaving(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        mResources = modRes;
        lpparam2 = lpparam;
        if (mSCResources == null) mSCResources = snapContext.getResources();
        refreshPreferences();

        try {
            storySnap = findClass(Obfuscator.save.STORYSNAP_CLASS, lpparam.classLoader);
            findAndHookMethod(Obfuscator.save.IMAGESNAPRENDERER_CLASS, lpparam.classLoader, Obfuscator.save.IMAGESNAPRENDERER_START, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ImageView imageview = (ImageView) getObjectField(param.thisObject, Obfuscator.save.IMAGESNAPRENDERER_VAR_IMAGEVIEW);
                    mImage = ((BitmapDrawable) imageview.getDrawable()).getBitmap();
                    newSaveMethod(null, mImage, false);
                    saveReceivedSnap(snapContext, receivedSnap, MediaType.IMAGE);
                }
            });
            /**
             * We hook this method to get the BitmapDrawable currently displayed.
             */

            /**
             * When the SnapView.a method gets called to show the actual snap, therefore it can be
             * used to determine if we are viewing the actual Snap or not.
             */

            findAndHookMethod(Obfuscator.save.SNAPVIEW_CLASS, lpparam.classLoader, Obfuscator.save.SNAPVIEW_SHOW, findClass(Obfuscator.save.SNAPVIEW_SHOW_FIRST, lpparam.classLoader), findClass(Obfuscator.save.SNAPVIEW_SHOW_SECOND, lpparam.classLoader), findClass(Obfuscator.save.SNAPVIEW_SHOW_THIRD, lpparam.classLoader), findClass(Obfuscator.save.SNAPVIEW_SHOW_FOURTH, lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    viewingSnap = true;
                    currentViewingSnap++;
                    //Logger.log("Starting to view a snap, plus viewingSnap: " + viewingSnap, true);
                }
            });
            /*findAndHookConstructor("com.snapchat.android.ui.snapview.SnapView", lpparam.classLoader, Context.class, lpparam.classLoader.loadClass("com.snapchat.android.ui.snapview.MultiLeveledSnapView"), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Button button = new Button((Context) param.args[0]);
                    button.setText("Save");
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(v.getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ViewGroup viewGroup = (ViewGroup) param.thisObject;
                    viewGroup.addView(button);
                }
            });*/

            XC_MethodHook gestureMethodHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (gestureModel == null || gestureModel.isSaved()) return;
                    //Logger.log("GestureHook: Not saved nor null", true);
                    MotionEvent motionEvent = (MotionEvent) param.args[0];
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                        //Logger.log("GestureHook: action_move is done", true);
                        //Logger.log("viewingSnap is: " + viewingSnap, true);
                        if (!viewingSnap) return;
                        // Result true means the event is handled
                        param.setResult(true);

                        if (!gestureModel.isInitialized()) {
                            gestureModel.initialize(motionEvent.getRawX(), motionEvent.getRawY());
                        } else if (!gestureModel.isSaved()) {
                            float deltaX = (motionEvent.getRawX() - gestureModel.getStartX());
                            float deltaY = (motionEvent.getRawY() - gestureModel.getStartY());
                            // Pythagorean theorem to calculate the distance between to points
                            float currentDistance = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

                            // Distance is bigger than previous, re-set reference point
                            if (currentDistance > gestureModel.getDistance()) {
                                gestureModel.setDistance(currentDistance);
                            } else { // On its way back
                                // Meaning it's at least 70% back to the start point and the gesture was longer then 20% of the screen
                                if ((currentDistance < (gestureModel.getDistance() * 0.3)) && (gestureModel.getDistance() > (gestureModel.getDisplayHeight() * 0.2))) {
                                    gestureModel.setSaved();
                                    //TODO add new saving method (also added image overlay saving to S2S)
                                    //newSaveMethod2(snapContext);
                                    Snap toSave = snapsMap.get(snapsMap.size() - 1);
                                    if (toSave.getMediaType() == MediaType.IMAGE) {
                                        mImage = toSave.getImage();
                                        saveReceivedSnap(snapContext, receivedSnap, MediaType.GESTUREDIMAGE);
                                    } else if (toSave.getMediaType() == MediaType.VIDEO) {
                                        mVideo = toSave.getVideo();
                                        saveReceivedSnap(snapContext, receivedSnap, MediaType.GESTUREDVIDEO);
                                    } else if (toSave.getMediaType() == MediaType.IMAGE_OVERLAY) {
                                        mImage = toSave.getImage();
                                        saveReceivedSnap(snapContext, receivedSnap, MediaType.GESTUREDOVERLAY);
                                    }
                                    Logger.log("Saving Done!");
                                    //saveReceivedSnap(snapContext, gestureModel.getReceivedSnap(), gestureModel.mediaType);
                                }
                            }
                        }
                    }
                }
            };

            final Class<?> snapImagebryo = findClass(Obfuscator.save.SNAPIMAGEBRYO_CLASS, lpparam.classLoader);
            final Class<?> mediabryoClass = findClass("com.snapchat.android.model.Mediabryo", lpparam.classLoader);
            findAndHookMethod(Obfuscator.save.SENT_CLASS, lpparam.classLoader, Obfuscator.save.SENT_METHOD, Bitmap.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    sentImage = (Bitmap) param.args[0];
                }
            });

            Class<?> mediabryoA = findClass("com.snapchat.android.model.Mediabryo$a", lpparam.classLoader);
            findAndHookConstructor("com.snapchat.android.model.Mediabryo", lpparam.classLoader, mediabryoA, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    videoUri = (Uri) getObjectField(param.thisObject, "mVideoUri");
                    //String vidString = videoUri.toString();
                    //sentVideo = new FileInputStream(videoUri.toString());
                    if (videoUri != null) {
                        Logger.log("We have the URI " + videoUri.toString(), true);
                    }
                    //sentVideo = new FileInputStream(videoUri.toString());
                }
            });
            /**
             * Method which gets called to prepare an image for sending (before selecting contacts).
             * We check whether it's an image or a video and save it.
             */
            findAndHookMethod(Obfuscator.save.SNAPPREVIEWFRAGMENT_CLASS, lpparam.classLoader, "o", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    refreshPreferences();
                    Logger.log("----------------------- SNAPPREFS/Sent Snap ------------------------", false);

                    if (!mSaveSentSnaps) {
                        Logger.log("Not saving sent snap");
                        return;
                    }
                    Logger.log("Saving sent snap");
                    try {
                        final Context context = (Context) callMethod(param.thisObject, "getActivity");
                        Logger.log("We have the Context", true);
                        Object mediabryo = getObjectField(param.thisObject, "a"); //ash is AnnotatedMediabryo, in SnapPreviewFragment
                        Logger.log("We have the MediaBryo", true);
                        final String fileName = dateFormatSent.format(new Date());
                        Logger.log("We have the filename " + fileName, true);

                        // Check if instance of SnapImageBryo and thus an image or a video
                        if (snapImagebryo.isInstance(mediabryo)) {
                            Logger.log("The sent snap is an Image", true);
                            //Bitmap sentimg = (Bitmap) callMethod(mediabryo, "e", mediabryo);
                            if (lastSavedBitmap == sentImage && lastSavedBitmap != null) {
                                return;
                            }
                            saveSnap(SnapType.SENT, MediaType.IMAGE, snapContext, sentImage, null, fileName, null);
                            lastSavedBitmap = sentImage;
                        } else {
                            Logger.log("The sent snap is a Video", true);
                            if (lastSavedUri == videoUri && lastSavedUri != null) {
                                return;
                            }
                            FileInputStream sentVid = new FileInputStream(videoUri.getPath());
                            Logger.log("Saving sent VIDEO SNAP", true);
                            saveSnap(SnapType.SENT, MediaType.VIDEO, context, null, sentVid, fileName, null);
                            lastSavedUri = videoUri;
                            /*findAndHookMethod("com.snapchat.android.model.Mediabryo", lpparam.classLoader, "c", mediabryoClass, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    Uri videoUri = (Uri) param.getResult();
                                    Logger.log("We have the URI " + videoUri.toString(), true);
                                    video = new FileInputStream(videoUri.toString());
                                    Logger.log("Saving sent VIDEO SNAP", true);
                                    saveSnap(SnapType.SENT, MediaType.VIDEO, context, null, video, fileName, null);
                                }
                            });*/
                        }
                    } catch (Throwable t) {
                        Logger.log("Saving sent snaps failed", true);
                        Logger.log(t.toString(), true);
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
                    Double currentResult = (Double) param.getResult();
                    if (mTimerUnlimited == true) {
                        findAndHookMethod("com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING);
                        param.setResult((double) 9999.9F);
                    } else {
                        if ((double) mTimerMinimum != TIMER_MINIMUM_DISABLED && currentResult < (double) mTimerMinimum) {
                            param.setResult((double) mTimerMinimum);
                        }
                    }
                }
            });
            if (mHideTimer == true) {
                findAndHookMethod("com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING);
            }
            if (mHideTimerStory == true) {
                findAndHookMethod("com.snapchat.android.ui.StoryTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING);
            }
            if (mLoopingVids == true) {
                findAndHookMethod("com.snapchat.android.ui.TextureVideoView", lpparam.classLoader, "start", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        callMethod(param.thisObject, "setLooping", true);
                    }
                });
                findAndHookMethod("com.snapchat.android.controller.countdown.SnapCountdownController ", lpparam.classLoader, "a", long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = 100000L;//It's how long you see video looping in milliseconds
                    }
                });
            }
            /**
             * We hook this method to handle our gestures made in the SC app itself.
             */
            findAndHookMethod(Obfuscator.save.LANDINGPAGEACTIVITY_CLASS, lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, gestureMethodHook);
            //findAndHookMethod(Obfuscator.save.SNAPVIEW_CLASS, lpparam.classLoader, "a", boolean.class, MotionEvent.class, gestureMethodHook);
            /**
             * We hook SnapView.c once again to get the receivedSnap argument, then store it along with the classLoader.
             */
            findAndHookMethod(Obfuscator.save.SNAPVIEW_CLASS, lpparam.classLoader, Obfuscator.save.SNAPVIEW_SHOW, findClass(Obfuscator.save.SNAPVIEW_SHOW_FIRST, lpparam.classLoader), findClass(Obfuscator.save.SNAPVIEW_SHOW_SECOND, lpparam.classLoader), findClass(Obfuscator.save.SNAPVIEW_SHOW_THIRD, lpparam.classLoader), findClass(Obfuscator.save.SNAPVIEW_SHOW_FOURTH, lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    //Logger.log("Starting to view a snap");
                    receivedSnap = param.args[0];
                    oldreceivedSnap = receivedSnap;
                    //Call for savereceivedsnap
                    snapCL = lpparam.classLoader;
                }
            });
            findAndHookMethod("com.snapchat.android.stories.ui.StorySnapView", lpparam.classLoader, "a", findClass(Obfuscator.save.SNAPVIEW_SHOW_FIRST, lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    //receivedSnap = param.args[0];
                    viewingSnap = true;
                    currentViewingSnap++;
                    Logger.log("Starting to view a story", true);
                }
            });
            /**
             * We hook SnapView.a to determine wether we have stopped viewing the Snap.
             */
            findAndHookMethod(Obfuscator.save.SNAPVIEW_CLASS, lpparam.classLoader, Obfuscator.save.SNAPVIEW_HIDE, findClass(Obfuscator.save.ENDREASON_CLASS, lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    //Logger.log("Stopped viewing the Snap", true);
                    viewingSnap = false;

                    //TODO clear maps & values
                    snapsMap.clear();
                    currentSavingSnap = -1;
                    currentViewingSnap = -1;
                }
            });
            findAndHookMethod("com.snapchat.android.stories.ui.StorySnapView", lpparam.classLoader, "a", findClass(Obfuscator.save.STORYVIEW_SHOW_FIRST, lpparam.classLoader), findClass("com.snapchat.android.ui.snapview.SnapViewSessionStopReason", lpparam.classLoader), int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Logger.log("Stopped viewing the Story", true);
                    viewingSnap = false;

                    //TODO clear maps & values
                    snapsMap.clear();
                    currentSavingSnap = -1;
                    currentViewingSnap = -1;
                }
            });
            /**
             * Sets the Snap as Screenshotted, so we constantly return false to it.
             */
            findAndHookMethod("com.snapchat.android.stories.ui.StorySnapView", lpparam.classLoader, "a", findClass(Obfuscator.save.STORYVIEW_SHOW_FIRST, lpparam.classLoader), findClass(Obfuscator.save.STORYVIEW_SHOW_SECOND, lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] != null) {
                        //Logger.log("MY INST NULL, IS IT BEFORE THE RENDERING?");
                        receivedSnap = param.args[0];
                        storyReceivedSnap = param.args[0];
                    }
                }
            });
            /*findAndHookMethod("GN", lpparam.classLoader, "aK", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    storyTimestamp = getLongField(param.thisObject, "mExpirationTimestamp");
                }
            });*/
            final Class<?> TextureVideoView = findClass("com.snapchat.android.ui.TextureVideoView", lpparam.classLoader);
            findAndHookMethod(Obfuscator.save.VIDEOSNAPRENDERER_CLASS, lpparam.classLoader, Obfuscator.save.VIDEOSNAPRENDERER_SHOW, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) getObjectField(param.thisObject, Obfuscator.save.VIDEOSNAPRENDERER_VAR_VIEW);
                    boolean found = false;
                    ArrayList<View> allViewsWithinMyTopView = getAllChildren(view);
                    for (View child : allViewsWithinMyTopView) {
                        //Logger.log("CHILD: "+ child.getId(), true);
                        //if (child instanceof TextureVideoView) {
                        Logger.log("FOUND VIDEOVIEW AS A CHILD - " + child.getId(), true);
                        Uri mUri = null;
                        try {
                            Field mUriField = TextureVideoView.getDeclaredField("b");
                            mUriField.setAccessible(true);
                            mUri = (Uri) mUriField.get(view);
                            mVideo = new FileInputStream(Uri.parse(mUri.toString()).getPath());
                            newSaveMethod(mVideo, null, false);
                            saveReceivedSnap(snapContext, receivedSnap, MediaType.VIDEO);
                            found = true;
                        } catch (Exception e) {
                            Logger.log("FUCKYOUFUCKINGNSPACHAT: " + e.toString());
                        }
                        //}
                    }
                    if (!found) {
                        Logger.log("NOT FOUND VIDEOVIEW AS A CHILD", true);
                    }
                }
            });


        } catch (Exception e) {
            Logger.log("Error occured: Snapprefs doesn't currently support this version, wait for an update", e);

            findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Toast.makeText((Context) param.thisObject, "This version of snapchat is currently not supported by Snapprefs.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private static void saveReceivedSnap(Context context, Object receivedSnap, MediaType mediaType) {
        String sender = null;
        SnapType snapType;
        Date timestamp = null;
        if (receivedSnap == null) {
            Logger.log("SRS - 1", true);
            //receivedSnap = oldreceivedSnap;
            //usedOldReceivedSnap = true;
            return;
        } else {
            //usedOldReceivedSnap = false;
        }
        try {
            sender = (String) getObjectField(receivedSnap, "mSender");
        } catch (NullPointerException ignore) {
        }
        if (sender == null) { //This means its a story
            //Class<?> storySnap = findClass(Obfuscator.save.STORYSNAP_CLASS, snapCL);
            try {
                sender = (String) getObjectField(storySnap.cast(receivedSnap), "mUsername");
            } catch (Exception e) {
                Logger.log(e.toString(), true);
            }
            snapType = SnapType.STORY;
            lastSnapType = SnapType.STORY;
            //timestamp = new Date((Long) getObjectField(storySnap.cast(receivedSnap), "mExpirationTimestamp"));
            timestamp = new Date();
        } else {
            snapType = SnapType.SNAP;
            lastSnapType = SnapType.SNAP;
            timestamp = new Date((Long) callMethod(receivedSnap, Obfuscator.save.SNAP_GETTIMESTAMP)); //Gettimestamp-Snap
        }
        String filename = sender + "_" + dateFormat.format(timestamp);
        //Logger.log("usedOldReceivedSnap = " + usedOldReceivedSnap, true);
        if (usedOldReceivedSnap) {
            filename = filename + "_1";
        }
        try {
            image = mImage;
            video = mVideo;
        } catch (NullPointerException ignore) {
        }
        switch (mediaType) {
            case VIDEO: {
                Logger.log("Video " + snapType.name + " opened");
                int saveMode = (snapType == SnapType.SNAP ? mModeSnapVideo : mModeStoryVideo);
                //if (saveMode == SAVE_S2S) {
                //    saveMode = SAVE_AUTO;
                //}
                if (saveMode == DO_NOT_SAVE) {
                    Logger.log("Mode: don't save");
                } else if (saveMode == SAVE_S2S) {
                    Logger.log("Mode: sweep to save");
                    gestureModel = new GestureModel(receivedSnap, screenHeight, MediaType.GESTUREDVIDEO);
                } else {
                    Logger.log("Mode: auto save");
                    gestureModel = null;
                    saveSnap(snapType, MediaType.VIDEO, context, image, video, filename, sender);
                }
                break;
            }
            case IMAGE: {
                Logger.log("Image " + snapType.name + " opened");
                int saveMode = (snapType == SnapType.SNAP ? mModeSnapImage : mModeStoryImage);
                //if (saveMode == SAVE_S2S) {
                //    saveMode = SAVE_AUTO;
                //}
                if (saveMode == DO_NOT_SAVE) {
                    Logger.log("Mode: don't save");
                } else if (saveMode == SAVE_S2S) {
                    Logger.log("Mode: sweep to save");
                    gestureModel = new GestureModel(receivedSnap, screenHeight, MediaType.GESTUREDIMAGE);
                } else {
                    Logger.log("Mode: auto save");
                    gestureModel = null;
                    saveSnap(snapType, MediaType.IMAGE, context, image, video, filename, sender);
                }
                break;
            }
            case IMAGE_OVERLAY: {
                int saveMode = (snapType == SnapType.SNAP ? mModeSnapVideo : mModeStoryVideo);
                //if (saveMode == SAVE_S2S) {
                //    saveMode = SAVE_AUTO;
                //}
                if (saveMode == DO_NOT_SAVE) {
                } else if (saveMode == SAVE_S2S) {
                    gestureModel = new GestureModel(receivedSnap, screenHeight, MediaType.GESTUREDOVERLAY);
                } else {
                    gestureModel = null;
                    saveSnap(snapType, MediaType.IMAGE_OVERLAY, context, image, video, filename, sender);
                }
                break;
            }
            case GESTUREDIMAGE: {
                Logger.log("GESTUREDIMAGE is coming", true);
                saveSnap(snapType, MediaType.IMAGE, context, image, video, filename, sender);
                break;
            }
            case GESTUREDVIDEO: {
                Logger.log("GESTUREDVIDEO is coming", true);
                saveSnap(snapType, MediaType.VIDEO, context, image, video, filename, sender);
                break;
            }
            case GESTUREDOVERLAY: {
                Logger.log("GESTUREDOVERLAY is coming", true);
                saveSnap(snapType, MediaType.IMAGE_OVERLAY, context, image, video, filename, sender);
                break;
            }
            default: {
                Logger.log("Unknown MediaType");
            }
        }
    }

    public static void saveSnap(SnapType snapType, MediaType mediaType, Context context, Bitmap image, FileInputStream video, String filename, String sender) {
        File directory;
        try {
            directory = createFileDir(snapType.subdir, sender);
        } catch (IOException e) {
            Logger.log(e);
            return;
        }

        File imageFile = new File(directory, filename + MediaType.IMAGE.fileExtension);
        File overlayFile = new File(directory, filename + "_overlay" + MediaType.IMAGE.fileExtension);
        File videoFile = new File(directory, filename + MediaType.VIDEO.fileExtension);

        if (mediaType == MediaType.IMAGE) {
            if (imageFile.exists()) {
                Logger.log("Image already exists");
                //showToast(context, mResources.getString(R.string.image_exists));
                NotificationUtils.showMessage(mResources.getString(R.string.image_exists), Color.rgb(255,0,0), NotificationUtils.LENGHT_LONG, lpparam2.classLoader);
                vibrate(context, false);
                return;
            }
            new saveImageJPGTask().execute(imageFile, image, context);
        } else if (mediaType == MediaType.IMAGE_OVERLAY) {
            if (mOverlays == true) {
                if (overlayFile.exists()) {
                    Logger.log("VideoOverlay already exists");
                    NotificationUtils.showMessage(mResources.getString(R.string.image_exists), Color.rgb(255, 0, 0), NotificationUtils.LENGHT_LONG, lpparam2.classLoader);
                    vibrate(context, false);
                    return;
                }
                new saveImagePNGTask().execute(overlayFile, image, context);
            }
        } else if (mediaType == MediaType.VIDEO) {
            if (videoFile.exists()) {
                Logger.log("Video already exists");
                vibrate(context, false);
                NotificationUtils.showMessage(mResources.getString(R.string.video_exists), Color.rgb(255, 0, 0), NotificationUtils.LENGHT_LONG, lpparam2.classLoader);
                return;
            }
            new saveVideoTask().execute(video, videoFile, context);
        }
        image = null;
        video = null;
    }

    private static File createFileDir(String category, String sender) throws IOException {
        File directory = new File(mSavePath);

        if (mSortByCategory || (mSortByUsername && sender == null)) {
            directory = new File(directory, category);
        }

        if (mSortByUsername && sender != null) {
            directory = new File(directory, sender);
        }

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory " + directory);
        }

        return directory;
    }

    public static class saveImageJPGTask extends AsyncTask<Object, Void, Boolean>{
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
            if(result){
                message = "Image saved";
                color = mResources.getColor(R.color.primary);
            } else {
                message = "Error while saving";
                color = Color.rgb(255,0,0);
            }
            NotificationUtils.showMessage(message, color, getToastLenght(), lpparam2.classLoader);
        }
    }
    public static class saveImagePNGTask extends AsyncTask<Object, Void, Boolean>{
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
            if(result){
                message = "Image saved";
                color = mResources.getColor(R.color.primary);
            } else {
                message = "Error while saving";
                color = Color.rgb(255,0,0);
            }
            NotificationUtils.showMessage(message, color, getToastLenght(), lpparam2.classLoader);
        }
    }

    public static class saveVideoTask extends AsyncTask<Object, Void, Boolean>{
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
            if(result){
                message = "Video saved";
                color = mResources.getColor(R.color.primary);
            } else {
                message = "Error while saving";
                color = Color.rgb(255,0,0);
            }
            NotificationUtils.showMessage(message, color, getToastLenght(), lpparam2.classLoader);
        }
    }
    /*
     * Tells the media scanner to scan the newly added image or video so that it
     * shows up in the gallery without a reboot. And shows a Toast message where
     * the media was saved.
     * @param context Current context
     * @param filePath File to be scanned by the media scanner
     */
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

    private static int getToastLenght() {
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
    public static long[] genVibratorPattern( float intensity, long duration )
    {
        float dutyCycle = Math.abs( ( intensity * 2.0f ) - 1.0f );
        long hWidth = (long) ( dutyCycle * ( duration - 1 ) ) + 1;
        long lWidth = dutyCycle == 1.0f ? 0 : 1;

        int pulseCount = (int) ( 2.0f * ( (float) duration / (float) ( hWidth + lWidth ) ) );
        long[] pattern = new long[ pulseCount ];

        for( int i = 0; i < pulseCount; i++ )
        {
            pattern[i] = intensity < 0.5f ? ( i % 2 == 0 ? hWidth : lWidth ) : ( i % 2 == 0 ? lWidth : hWidth );
        }

        return pattern;
    }

    static void refreshPreferences() {

        prefs = new XSharedPreferences(new File(
                Environment.getDataDirectory(), "data/"
                + PACKAGE_NAME + "/shared_prefs/" + PACKAGE_NAME
                + "_preferences" + ".xml"));
        prefs.reload();

        mModeSnapImage = prefs.getInt("pref_key_snaps_images", mModeSnapImage);
        mModeSnapVideo = prefs.getInt("pref_key_snaps_videos", mModeSnapVideo);
        mModeStoryImage = prefs.getInt("pref_key_stories_images", mModeStoryImage);
        mModeStoryVideo = prefs.getInt("pref_key_stories_videos", mModeStoryVideo);
        if(mModeStoryVideo==SAVE_AUTO)
            mModeStoryVideo=SAVE_S2S;
        mTimerMinimum = prefs.getInt("pref_key_timer_minimum", mTimerMinimum);
        mToastEnabled = prefs.getBoolean("pref_key_toasts_checkbox", mToastEnabled);
        mToastLength = prefs.getInt("pref_key_toasts_duration", mToastLength);
        mSavePath = prefs.getString("pref_key_save_location", mSavePath);
        mVibrationEnabled = prefs.getBoolean("pref_key_vibration_checkbox", mVibrationEnabled);
        mSaveSentSnaps = prefs.getBoolean("pref_key_save_sent_snaps", mSaveSentSnaps);
        mSortByCategory = prefs.getBoolean("pref_key_sort_files_mode", mSortByCategory);
        mSortByUsername = prefs.getBoolean("pref_key_sort_files_username", mSortByUsername);
        mOverlays = prefs.getBoolean("pref_key_overlay", mOverlays);
        mDebugging = prefs.getBoolean("pref_key_debug_mode", mDebugging);
        mTimerUnlimited = prefs.getBoolean("pref_key_timer_unlimited", mTimerUnlimited);
        mHideTimerStory = prefs.getBoolean("pref_key_timer_story_hide", mHideTimerStory);
        mLoopingVids = prefs.getBoolean("pref_key_looping_video", mLoopingVids);
        mHideTimer = prefs.getBoolean("pref_key_timer_hide", mHideTimer);

    }

    public static long hashBitmap(Bitmap bmp){
        long hash = 31; //or a higher prime at your choice
        for(int x = 0; x < bmp.getWidth(); x++){
            for (int y = 0; y < bmp.getHeight(); y++){
                hash *= (bmp.getPixel(x,y) + 31);
            }
        }
        return hash;
    }

    static private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    public enum SnapType {
        SNAP("snap", "/ReceivedSnaps"),
        STORY("story", "/Stories"),
        SENT("sent", "/SentSnaps"),
        CHAT("chat", "/Chat");

        private final String name;
        private final String subdir;

        SnapType(String name, String subdir) {
            this.name = name;
            this.subdir = subdir;
        }
    }
    public enum MediaType {
        IMAGE(".jpg"),
        IMAGE_OVERLAY(".png"),
        VIDEO(".mp4"),
        GESTUREDIMAGE(".jpg"),
        GESTUREDVIDEO(".mp4"),
        GESTUREDOVERLAY(".png");

        private final String fileExtension;

        MediaType(String fileExtension) {
            this.fileExtension = fileExtension;
        }
    }
}