package com.marz.snapprefs;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.widget.Toast;

import com.marz.snapprefs.SnapData.FlagState;
import com.marz.snapprefs.Util.NotificationUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class Saving
{

    //public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    // Modes for saving Snapchats
    public static final int SAVE_AUTO = 3;
    public static final int DO_NOT_SAVE = 2;
    public static final int SAVE_BUTTON = 0;
    // Length of toasts
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;
    // Minimum timer duration disabled
    public static final int TIMER_MINIMUM_DISABLED = 0;
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    // Preferences and their default values
    public static int mModeSave = SAVE_AUTO;
    public static int mTimerMinimum = TIMER_MINIMUM_DISABLED;
    public static boolean mTimerUnlimited = true;
    public static boolean mHideTimerStory = false;
    public static boolean mLoopingVids = true;
    public static boolean mHideTimer = false;
    public static boolean mToastEnabled = true;
    public static boolean mVibrationEnabled = true;
    public static int mToastLength = TOAST_LENGTH_LONG;
    public static String mSavePath =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
    public static boolean mSaveSentSnaps = false;
    public static boolean mSortByCategory = true;
    public static boolean mSortByUsername = true;
    public static boolean mDebugging = true;
    public static boolean mOverlays = true;
    public static Resources mSCResources;
    public static Bitmap sentImage;
    public static Uri videoUri;
    public static XC_LoadPackage.LoadPackageParam lpparam2;
    static XSharedPreferences prefs;
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault() );
    private static SimpleDateFormat dateFormatSent =
            new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss", Locale.getDefault() );
    private static XModuleResources mResources;
    private static HashSet<Object> spamGuardSet = new HashSet<>();
    private static ConcurrentHashMap<String, SnapData> hashSnapData = new ConcurrentHashMap<>();
    private static boolean printFlags = true;
    private static SnapData currentSnapData;
    private static Context relativeContext;

    static void initSaving( final XC_LoadPackage.LoadPackageParam lpparam,
                            final XModuleResources modRes, final Context snapContext ) {
        mResources = modRes;
        lpparam2 = lpparam;

        if ( mSCResources == null ) mSCResources = snapContext.getResources();
        refreshPreferences();


        try {
            ClassLoader cl = lpparam.classLoader;

            /**
             * Called whenever a video is decrypted by snapchat
             * Will pre-load the next snap in the list
             */
            findAndHookConstructor( Obfuscator.save.DECRYPTEDSNAPVIDEO_CLASS, cl, findClass(
                    Obfuscator.save.CACHE_CLASS, cl ), String.class, Bitmap.class,
                                    new XC_MethodHook()
                                    {
                                        @Override
                                        protected void afterHookedMethod(
                                                MethodHookParam param )
                                                throws Throwable {
                                            super.afterHookedMethod( param );

                                            try {
                                                if ( mModeSave == DO_NOT_SAVE )
                                                    return;

                                                handleVideoPayload( snapContext, param );
                                            } catch ( Exception e ) {
                                                Logger.log(
                                                        "Exception handling Video Payload\n" +
                                                                e.getMessage() );
                                            }
                                        }
                                    } );

            /**
             * Called whenever a bitmap is set to the view (I believe)
             */
            findAndHookMethod( Obfuscator.save.IMAGESNAPRENDERER_CLASS +
                                       "$1", cl, Obfuscator.save.IMAGESNAPRENDERER_NEW_BITMAP, Bitmap.class, new XC_MethodHook()
            {
                @Override
                protected void beforeHookedMethod( MethodHookParam param ) throws Throwable {
                    try {
                        if ( mModeSave == DO_NOT_SAVE )
                            return;

                        handleImagePayload( snapContext, param );
                    } catch ( Exception e ) {
                        Logger.log( "Exception handling Image Payload\n" + e.getMessage() );
                    }
                }
            } );

            /**
             * Called every time a snap is viewed - Quite reliable
             */
            findAndHookMethod( Obfuscator.save.RECEIVEDSNAP_CLASS, cl, Obfuscator.save
                    .RECEIVEDSNAP_BEING_SEEN, boolean.class, new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable {
                    super.afterHookedMethod( param );

                    if ( mModeSave == DO_NOT_SAVE )
                        return;

                    boolean isBeingViewed = (boolean) param.args[ 0 ];

                    if ( isBeingViewed ) {
                        Object obj = param.thisObject;

                        try {
                            handleSnapHeader( snapContext, obj );
                        } catch ( Exception e ) {
                            Logger.log( "Exception handling HEADER\n" + e.getMessage() );
                        }
                    }
                }
            } );

            // TODO Implement a better system for saving sent snaps/videos
            // Currently not too bad but could use fine tuning
            final Class<?> snapImagebryo =
                    findClass( Obfuscator.save.SNAPIMAGEBRYO_CLASS, lpparam.classLoader );
            findAndHookMethod( Obfuscator.save.SENT_CLASS, lpparam.classLoader, Obfuscator.save.SENT_METHOD, Bitmap.class, new XC_MethodHook()
            {
                @Override
                protected void beforeHookedMethod( MethodHookParam param ) throws Throwable {
                    Logger.beforeHook( "SENT_CLASS" );
                    sentImage = (Bitmap) param.args[ 0 ];
                }
            } );

            Class<?> mediabryoA =
                    findClass( "com.snapchat.android.model.Mediabryo$a", lpparam.classLoader );
            findAndHookConstructor( "com.snapchat.android.model.Mediabryo", lpparam.classLoader, mediabryoA, new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable {
                    Logger.afterHook( "Mediabryo" );
                    videoUri = (Uri) getObjectField( param.thisObject, "mVideoUri" );

                    //sentVideo = new FileInputStream(videoUri.toString());
                    if ( videoUri != null ) {
                        Logger.log( "We have the URI " + videoUri.toString(), true );
                    }
                }
            } );
            /**
             * Method which gets called to prepare an image for sending (before selecting contacts).
             * We check whether it's an image or a video and save it.
             */
            findAndHookMethod( Obfuscator.save.SNAPPREVIEWFRAGMENT_CLASS, lpparam.classLoader, "l", new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable {
                    refreshPreferences();
                    Logger.log( "----------------------- SNAPPREFS/Sent Snap ------------------------", false );

                    if ( !mSaveSentSnaps ) {
                        Logger.log( "Not saving sent snap" );
                        return;
                    }
                    Logger.log( "Saving sent snap" );
                    try {
                        final Context context =
                                (Context) callMethod( param.thisObject, "getActivity" );
                        Logger.log( "We have the Context", true );
                        Object mediabryo =
                                getObjectField( param.thisObject, "a" ); //ash is AnnotatedMediabryo, in SnapPreviewFragment
                        Logger.log( "We have the MediaBryo", true );
                        final String fileName = dateFormatSent.format( new Date() );
                        Logger.log( "We have the filename " + fileName, true );

                        // Check if instance of SnapImageBryo and thus an image or a video
                        if ( snapImagebryo.isInstance( mediabryo ) ) {
                            Logger.log( "The sent snap is an Image", true );
                            //Bitmap sentimg = (Bitmap) callMethod(mediabryo, "e", mediabryo);
                            //TODO Test spamguard
                            if ( spamGuardSet.contains( sentImage ) )
                                return;
                            else
                                spamGuardSet.add( sentImage );

                            SaveResponse status =
                                    saveSnap( SnapType.SENT, MediaType.IMAGE, snapContext, sentImage, null, fileName, null );

                            if ( status == SaveResponse.SUCCESS )
                                spamGuardSet.remove( sentImage );
                        } else {
                            Logger.log( "The sent snap is a Video", true );

                            FileInputStream sentVid = new FileInputStream( videoUri.getPath() );

                            //TODO test spamguard
                            if ( spamGuardSet.contains( videoUri ) )
                                return;
                            else
                                spamGuardSet.add( sentVid );

                            Logger.log( "Saving sent VIDEO SNAP", true );
                            SaveResponse status =
                                    saveSnap( SnapType.SENT, MediaType.VIDEO, context, null, sentVid, fileName, null );
                            if ( status == SaveResponse.SUCCESS )
                                spamGuardSet.remove( videoUri );

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
                    } catch ( Throwable t ) {
                        Logger.log( "Saving sent snaps failed", true );
                        Logger.log( t.toString(), true );
                    }
                }
            } );

            /**
             * We hook this method to set the CanonicalDisplayTime to our desired one if it is under
             * our limit and hide the counter if we need it.
             */

            findAndHookMethod( Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader, Obfuscator.save.RECEIVEDSNAP_DISPLAYTIME, new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable {
                    //Logger.afterHook("RECEIVEDSNAP - DisplayTime");
                    Double currentResult = (Double) param.getResult();
                    if ( mTimerUnlimited ) {
                        findAndHookMethod( "com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING );
                        param.setResult( (double) 9999.9F );
                    } else {
                        if ( (double) mTimerMinimum != TIMER_MINIMUM_DISABLED &&
                                currentResult < (double) mTimerMinimum ) {
                            param.setResult( (double) mTimerMinimum );
                        }
                    }
                }
            } );
            if ( mHideTimer ) {
                findAndHookMethod( "com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING );
            }
            if ( mHideTimerStory ) {
                findAndHookMethod( "com.snapchat.android.ui.StoryTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING );
            }
            if ( mLoopingVids ) {
                findAndHookMethod( "com.snapchat.opera.shared.view.TextureVideoView", lpparam.classLoader, "start", new XC_MethodHook()
                {
                    @Override
                    protected void beforeHookedMethod( MethodHookParam param ) throws Throwable {
                        callMethod( param.thisObject, "setLooping", true );
                    }
                } );
                findAndHookMethod( "com.snapchat.android.controller.countdown.SnapCountdownController ", lpparam.classLoader, "a", long.class, new XC_MethodHook()
                {
                    @Override
                    protected void beforeHookedMethod( MethodHookParam param ) throws Throwable {
                        param.args[ 0 ] =
                                100000L;//It's how long you see video looping in milliseconds
                    }
                } );
            }
            /**
             * We hook SnapView.a to determine wether we have stopped viewing the Snap.
             */
            findAndHookMethod( Obfuscator.save.SNAPVIEW_CLASS, lpparam.classLoader, Obfuscator.save.SNAPVIEW_HIDE, findClass( Obfuscator.save.ENDREASON_CLASS, lpparam.classLoader ), new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable {
                    Logger.afterHook( "SNAPVIEW - Hide" );
                    spamGuardSet.clear();
                }
            } );
            findAndHookMethod( "com.snapchat.android.stories.ui.StorySnapView", lpparam.classLoader, "a", findClass( Obfuscator.save.STORYVIEW_SHOW_FIRST, lpparam.classLoader ), findClass( "com.snapchat.android.ui.snapview.SnapViewSessionStopReason", lpparam.classLoader ), int.class, new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable {
                    Logger.afterHook( "StorySnapView - Hide1" );
                }
            } );
        } catch ( Exception e ) {
            Logger.log( "Error occured: Snapprefs doesn't currently support this version, wait for an update", e );

            findAndHookMethod( "com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook()
            {
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable {
                    Toast.makeText( (Context) param.thisObject, "This version of snapchat is currently not supported by Snapprefs.", Toast.LENGTH_LONG )
                            .show();
                }
            } );
        }
    }

    public static void saveSnapButtonPress() {
        if ( currentSnapData != null && relativeContext != null ) {
            Logger.printMessage( "Found SnapData to save" );
            Logger.printMessage( "Key: " + currentSnapData.getmKey() );
            Logger.printMessage( "Sender: " + currentSnapData.getStrSender() );
            Logger.printMessage( "Timestamp: " + currentSnapData.getStrTimestamp() );
            Logger.printMessage( "SnapType: " + currentSnapData.getSnapType() );
            Logger.printMessage( "MediaType: " + currentSnapData.getMediaType() );

            try {
                handleSave( relativeContext, currentSnapData );
            } catch( Exception e )
            {
                Logger.printFinalMessage( "Exception saving snap" );

                if ( HookMethods.mToastEnabled ) {
                    NotificationUtils.showMessage(
                            "Code exception saving snap",
                            Color.rgb( 200, 70, 70),
                            getToastLength(),
                            lpparam2.classLoader );
                }
            }
        } else {
            Logger.printFinalMessage( "No SnapData to save" );
            if ( HookMethods.mToastEnabled ) {
                NotificationUtils.showMessage(
                        "No SnapData to save",
                        Color.rgb( 200, 70, 70),
                        getToastLength(),
                        lpparam2.classLoader );
            }
        }
    }

    private static void handleSnapHeader( Context context, Object receivedSnap ) throws Exception {
        Logger.printTitle( "Handling SnapData HEADER" );

        String mId = (String) getObjectField( receivedSnap, "mId" );
        SnapType snapType =
                receivedSnap.getClass().getCanonicalName().equals( "VK" ) ? SnapType.STORY :
                        SnapType.SNAP;


        Logger.printMessage( "SnapType: " + snapType.name );

        String mKey = mId;
        String strSender;

        if ( snapType == SnapType.SNAP ) {
            mKey += (String) getObjectField( receivedSnap, "mCacheKeyInstanceSuffix" );
            strSender = (String) getObjectField( receivedSnap, "mSender" );
        } else
            strSender = (String) getObjectField( receivedSnap, "mUsername" );

        Logger.printMessage( "Key: " + mKey );
        Logger.printMessage( "Sender: " + strSender );

        SnapData snapData = hashSnapData.get( mKey );

        if ( snapData != null && scanForExisting( snapData, FlagState.HEADER ) ) {
            Logger.printFinalMessage( "Existing SnapData with HEADER found" );
            return;
        } else if ( snapData == null ) {
            snapData = new SnapData( mKey );
            hashSnapData.put( mKey, snapData );
        }

        printFlags( snapData );

        long lngTimestamp = (Long) callMethod( receivedSnap, Obfuscator.save.SNAP_GETTIMESTAMP );
        Date timestamp = new Date( lngTimestamp );
        String strTimestamp = dateFormat.format( timestamp );

        Logger.printMessage( "Timestamp: " + strTimestamp );

        snapData.setHeader( mId, mKey, strSender, strTimestamp, snapType );

        if ( mModeSave != SAVE_BUTTON )
            handleSave( context, snapData );
        else {
            currentSnapData = snapData;
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
    private static void handleVideoPayload( Context context, XC_MethodHook.MethodHookParam param )
            throws Exception {

        Logger.printTitle( "Handling VIDEO Payload" );

        // Grab the MediaCache - Class: ahJ
        Object mCache = param.args[ 0 ];

        if ( mCache == null ) {
            Logger.printFinalMessage( "Null Cache passed" );
            return;
        }

        // Grab the MediaKey - Variable: ahJ.mKey
        String mKey = (String) param.args[ 1 ];

        if ( mKey == null ) {
            Logger.printFinalMessage( "Null Key passed" );
            return;
        }

        Logger.printMessage( "Key: " + mKey );

        // Grab the Key to Item Map (Contains file paths)
        @SuppressWarnings("unchecked")
        Map<String, Object> mKeyToItemMap =
                (Map<String, Object>) getObjectField( mCache, "mKeyToItemMap" );

        if ( mKeyToItemMap == null ) {
            Logger.printFinalMessage( "Mkey-Item Map not found" );
            return;
        }

        // Attempt to get the item associated with the key
        Object item = mKeyToItemMap.get( mKey );

        if ( item == null ) {
            Logger.printMessage( "Item not found with key:" );
            Logger.printFinalMessage( mKey );
            return;
        }

        // Get the path of the video file
        String mAbsoluteFilePath = (String) getObjectField( item, "mAbsoluteFilePath" );

        if ( mAbsoluteFilePath == null ) {
            Logger.printFinalMessage( "No path object found" );
            return;
        }

        // Some pattern matching to trim down the filepath for logging
        String regex = "cache/uv/sesrh_dlw(.*?).mp4.nomedia";
        Pattern pattern = Pattern.compile( regex );
        Matcher matcher = pattern.matcher( mAbsoluteFilePath );

        if ( matcher.find() ) {
            try {
                Logger.printMessage( "Path: " + matcher.group( 0 ) );
            } catch ( IndexOutOfBoundsException ignore ) {
                Logger.printMessage( "Path: " + mAbsoluteFilePath );
            }
        } else
            Logger.printMessage( "Path: " + mAbsoluteFilePath );

        // Split the mKey as story videos are post-fixed with an extra code
        if ( mKey.contains( "#" ) )
            mKey = mKey.split( "#" )[ 0 ];

        // Get the snapdata associated with the mKey above
        SnapData snapData = hashSnapData.get( mKey );

        // Check if the snapdata exists and whether it has already been handled
        if ( snapData != null && scanForExisting( snapData, FlagState.PAYLOAD ) ) {
            Logger.printFinalMessage( "Tried to modify existing data" );
            return;
        } else if ( snapData == null ) {
            // If the snapdata doesn't exist, create a new one with the provided mKey
            Logger.printMessage( "No SnapData found for Payload... Creating new" );
            snapData = new SnapData( mKey );
            hashSnapData.put( mKey, snapData );
        }

        // Print the snapdata's current flags
        printFlags( snapData );

        // Get the stream using the filepath provided
        FileInputStream video = new FileInputStream( mAbsoluteFilePath );

        // Assign the payload to the snapdata
        snapData.setPayload( video );
        Logger.printMessage( "Successfully attached payload" );

        // If set to button saving, do not save
        if ( mModeSave != SAVE_BUTTON )
            handleSave( context, snapData );
    }

    /**
     * Performs saving of the image bitmap into the HashMap
     *
     * @param context
     * @param param
     * @throws Exception
     */
    public static void handleImagePayload( Context context, XC_MethodHook.MethodHookParam param )
            throws Exception {
        Logger.printTitle( "Handling IMAGE Payload" );
        Logger.printMessage( "Getting Bitmap" );

        // Class: ahZ - holds the mKey for the payload
        Object obj = getObjectField( param.thisObject, "b" );
        // Get the mKey out of ahZ
        String mKey = (String) getObjectField( obj, "mKey" );
        Logger.printMessage( "Key: " + mKey );

        // Find the snapData associated with the mKey
        SnapData snapData = hashSnapData.get( mKey );

        // Check if the snapData has been processed
        if ( snapData != null && scanForExisting( snapData, FlagState.PAYLOAD ) ) {
            Logger.printFinalMessage( "Tried to modify existing data" );
            return;
        } else if ( snapData == null ) {
            Logger.printMessage( "No SnapData found for Payload... Creating new" );
            snapData = new SnapData( mKey );
            hashSnapData.put( mKey, snapData );
        }

        // Display the snapData's current flags
        printFlags( snapData );

        // Get the bitmap payload
        Bitmap bmp = (Bitmap) param.args[ 0 ];

        if ( bmp == null ) {
            Logger.printFinalMessage( "Tried to attatch Null Bitmap" );
            return;
        }

        Logger.printMessage( "Pulled Bitmap" );

        // Assign the payload to the snapData
        snapData.setPayload( bmp );
        Logger.printMessage( "Successfully attached payload" );

        if ( mModeSave != SAVE_BUTTON )
            handleSave( context, snapData );
    }

    /**
     * Used to perform a save on a completed snapData object
     * @param context
     * @param snapData
     * @throws Exception
     */
    private static void handleSave( Context context, SnapData snapData ) throws Exception {
        // Ensure snapData is ready for saving
        if ( snapData.getFlags().contains( FlagState.COMPLETED ) ) {
            Logger.printMessage( "Saving Snap" );

            // Attempt to save the snap
            SaveResponse saveResponse = saveReceivedSnap( context, snapData );

            // Handle the response from the save attempt
            switch ( saveResponse ) {
                case SUCCESS: {
                    Logger.printMessage( "Wiping payload and adding SAVED flag" );

                    // Wipe the payload to save memory
                    // Also assigns the SAVED flag to the snap
                    snapData.wipePayload();

                    /*if ( mModeSave == SAVE_BUTTON ) {
                        currentSnapData = null;
                        relativeContext = null;
                    }*/

                    if ( HookMethods.mToastEnabled ) {
                        NotificationUtils.showMessage(
                                snapData.getMediaType().typeName + " saved",
                                Color.rgb( 70, 200, 70),
                                getToastLength(),
                                lpparam2.classLoader );
                    }

                    Logger.printFinalMessage( "Snap Saving Completed" );
                    return;
                }
                case FAILED: {
                    Logger.printFinalMessage( "Failed to save snap" );

                    // Assign a FAILED flag to the snap
                    // If the snap fails to save, a force close will likely be necessary
                    // TODO Perform more FAILED handling
                    snapData.getFlags().add( FlagState.FAILED );

                    if ( HookMethods.mToastEnabled ) {
                        NotificationUtils.showMessage(
                                "Failed saving " + snapData.getMediaType().typeName,
                                Color.rgb( 200, 70, 70),
                                getToastLength(),
                                lpparam2.classLoader );
                    }

                    return;
                }
                case ONGOING: {
                    return;
                }
                case EXISTING: {
                    if ( HookMethods.mToastEnabled ) {
                        NotificationUtils.showMessage(
                                snapData.getMediaType().typeName + " already exists",
                                Color.rgb( 70, 200, 70),
                                getToastLength(),
                                lpparam2.classLoader );
                    }

                    Logger.printFinalMessage( snapData.getMediaType().typeName + " already exists" );
                    return;
                }
            }
        }
    }

    /**
     * Check if the snapData has already been handled
     * @param snapData
     * @param flagState - Assign a flagstate to include (E.G PAYLOAD/HEADER)
     * @return True if contains any of the flags
     */
    private static boolean scanForExisting( SnapData snapData, FlagState flagState ) {
        //TODO Remove the FAILED flag to retry saving snaps after failure occurs
        return snapData.getFlags().contains( flagState ) ||
                snapData.getFlags().contains( FlagState.COMPLETED ) ||
                snapData.getFlags().contains( FlagState.SAVED ) ||
                snapData.getFlags().contains( FlagState.FAILED );
    }


    /**
     * If printFlags is true, will print the snapData's flag list
     * @param snapData
     */
    private static void printFlags( SnapData snapData ) {
        if ( !printFlags )
            return;

        Logger.printMessage( "Flags:" );

        if ( snapData.getFlags().size() <= 0 ) {
            Logger.printMessage( "-  NONE  -" );
            return;
        }

        // Loop through the list of states and print them
        for ( FlagState flagState : snapData.getFlags() )
            Logger.printMessage( "-  " + flagState.toString() + "  -" );
    }

    /**
     * Perform a save on the snapData
     * @param context
     * @param snapData
     * @return
     * @throws Exception
     */
    private static SaveResponse saveReceivedSnap( Context context, SnapData snapData ) throws
                                                                                       Exception {
        if ( mModeSave == DO_NOT_SAVE ) {
            Logger.printMessage( "Mode: don't save" );
            return SaveResponse.FAILED;
        }

        // Check if trying to save null snapData
        if ( snapData == null ) {
            Logger.printMessage( "Null SnapData" );
            return SaveResponse.FAILED;
        } else if ( !snapData.getFlags().contains( FlagState.COMPLETED ) ) {
            // If the snapData doesn't contains COMPLETED; Print out why and return
            String strMessage = snapData.getFlags().contains( FlagState.PAYLOAD ) ? "PAYLOAD" :
                    "HEADER";
            Logger.printMessage( "Tried to save snap without assigned " + strMessage );
            return SaveResponse.ONGOING;
        } else if ( snapData.getFlags().contains( FlagState.SAVED ) ) {
            Logger.printMessage( "Tried to save a snap that has already been processed" );
            return SaveResponse.EXISTING;
        }

        // Get the snapData's payload
        Object payload = snapData.getPayload();

        // Check if it's null (Probably redundant)
        if ( payload == null ) {
            Logger.printMessage( "Attempted to save Null Payload" );
            return SaveResponse.FAILED;
        }

        String filename = snapData.getStrSender() + "_" + snapData.getStrTimestamp();

        switch ( snapData.getMediaType() ) {
            case VIDEO: {
                Logger.printMessage( "Video " + snapData.getSnapType().name + " opened" );

                return saveSnap( snapData.getSnapType(), MediaType.VIDEO, context, null,
                                 (FileInputStream) payload, filename, snapData.getStrSender() );
            }
            case IMAGE: {
                Logger.printMessage( "Image " + snapData.getStrSender() + " opened" );

                return saveSnap( snapData.getSnapType(), MediaType.IMAGE, context,
                                 (Bitmap) payload, null, filename, snapData.getStrSender() );
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
                Logger.printMessage( "Unknown MediaType" );
                return SaveResponse.FAILED;
            }
        }
    }

    /**
     * Perform a direct save of a snap
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
    public static SaveResponse saveSnap( SnapType snapType, MediaType mediaType, Context context,
                                         Bitmap image, FileInputStream video, String filename,
                                         String sender ) throws Exception{
        File directory;

        try {
            directory = createFileDir( snapType.subdir, sender );
        } catch ( IOException e ) {
            Logger.log( e );
            return SaveResponse.FAILED;
        }

        if ( mediaType == MediaType.IMAGE ) {
            File imageFile = new File( directory, filename + MediaType.IMAGE.fileExtension );
            if ( imageFile.exists() ) {
                Logger.printMessage( "Image already exists: " + filename );
                vibrate( context, false );
                return SaveResponse.EXISTING;
            }

            // the following code is somewhat redundant as it defeats the point of an async task
            // Perform an async save of the JPG
            AsyncTask<Object, Void, Boolean> task =
                    new saveImageJPGTask().execute( imageFile, image, context );

            try {
                // Wait for the JPG to save and report the state
                return task.get() ? SaveResponse.SUCCESS : SaveResponse.FAILED;
            } catch ( InterruptedException e ) {
                Logger.printMessage( "Interrupted Exception" );
                return SaveResponse.FAILED;
            } catch ( ExecutionException e ) {
                Logger.printMessage( "Execution Exception: " + e.getMessage() );
                return SaveResponse.FAILED;
            }
        } else if ( mediaType == MediaType.IMAGE_OVERLAY ) {
            File overlayFile =
                    new File( directory, filename + "_overlay" + MediaType.IMAGE.fileExtension );

            if ( mOverlays ) {
                if ( overlayFile.exists() ) {
                    Logger.printMessage( "VideoOverlay already exists" );
                    vibrate( context, false );
                    return SaveResponse.SUCCESS;
                }

                // the following code is somewhat redundant as it defeats the point of an async task
                // Perform an async save of the PNG
                AsyncTask<Object, Void, Boolean> task =
                        new saveImagePNGTask().execute( overlayFile, image, context );

                try {
                    // Wait for the JPG to save and report the state
                    return task.get() ? SaveResponse.SUCCESS : SaveResponse.FAILED;
                } catch ( InterruptedException e ) {
                    Logger.log( "Interrupted Exception" );
                    return SaveResponse.FAILED;
                } catch ( ExecutionException e ) {
                    Logger.log( "Execution Exception: " + e.getMessage() );
                    return SaveResponse.FAILED;
                }
            }
        } else if ( mediaType == MediaType.VIDEO ) {
            File videoFile = new File( directory, filename + MediaType.VIDEO.fileExtension );

            if ( videoFile.exists() ) {
                Logger.printMessage( "Video already exists" );
                vibrate( context, false );
                return SaveResponse.EXISTING;
            }

            // the following code is somewhat redundant as it defeats the point of an async task
            // Perform an async save of the PNG
            AsyncTask<Object, Void, Boolean> task =
                    new saveVideoTask().execute( video, videoFile, context );

            try {
                // Wait for the MP4 to save and report the state
                return task.get() ? SaveResponse.SUCCESS : SaveResponse.FAILED;
            } catch ( InterruptedException e ) {
                Logger.printMessage( "Interrupted Exception" );
                return SaveResponse.FAILED;
            } catch ( ExecutionException e ) {
                Logger.printMessage( "Execution Exception: " + e.getMessage() );
                return SaveResponse.FAILED;
            }
        }

        return SaveResponse.FAILED;
    }

    private static File createFileDir( String category, String sender ) throws IOException {
        File directory = new File( mSavePath );

        if ( mSortByCategory || ( mSortByUsername && sender == null ) ) {
            directory = new File( directory, category );
        }

        if ( mSortByUsername && sender != null ) {
            directory = new File( directory, sender );
        }

        if ( !directory.exists() && !directory.mkdirs() ) {
            throw new IOException( "Failed to create directory " + directory );
        }

        return directory;
    }

    /*
     * Tells the media scanner to scan the newly added image or video so that it
     * shows up in the gallery without a reboot. And shows a Toast message where
     * the media was saved.
     * @param context Current context
     * @param filePath File to be scanned by the media scanner
     */
    private static void runMediaScanner( Context context, String... mediaPath ) {
        try {
            Logger.printMessage( "MediaScanner started" );
            MediaScannerConnection.scanFile( context, mediaPath, null,
                                             new MediaScannerConnection.OnScanCompletedListener()
                                             {
                                                 public void onScanCompleted( String path,
                                                                              Uri uri ) {
                                                     Logger.log( "MediaScanner scanned file: " +
                                                                         uri.toString() );
                                                 }
                                             } );
        } catch ( Exception e ) {
            Logger.printMessage( "Error occurred while trying to run MediaScanner" );
        }
    }

    private static int getToastLength() {
        if ( mToastLength == TOAST_LENGTH_SHORT ) {
            return NotificationUtils.LENGHT_SHORT;
        } else {
            return NotificationUtils.LENGHT_LONG;
        }
    }

    private static void vibrate( Context context, boolean success ) {
        if ( mVibrationEnabled ) {
            if ( success ) {
                Vibrator v = (Vibrator) context.getSystemService( Context.VIBRATOR_SERVICE );
                v.vibrate( genVibratorPattern( 0.7f, 400 ), -1 );
            } else {
                Vibrator v = (Vibrator) context.getSystemService( Context.VIBRATOR_SERVICE );
                v.vibrate( genVibratorPattern( 1.0f, 700 ), -1 );
            }
        }
    }

    //http://stackoverflow.com/questions/20808479/algorithm-for-generating-vibration-patterns-ranging-in-intensity-in-android/20821575#20821575
    // intensity 0-1
    // duration mS
    public static long[] genVibratorPattern( float intensity, long duration ) {
        float dutyCycle = Math.abs( ( intensity * 2.0f ) - 1.0f );
        long hWidth = (long) ( dutyCycle * ( duration - 1 ) ) + 1;
        long lWidth = dutyCycle == 1.0f ? 0 : 1;

        int pulseCount = (int) ( 2.0f * ( (float) duration / (float) ( hWidth + lWidth ) ) );
        long[] pattern = new long[ pulseCount ];

        for ( int i = 0; i < pulseCount; i++ ) {
            pattern[ i ] = intensity < 0.5f ? ( i % 2 == 0 ? hWidth : lWidth ) :
                    ( i % 2 == 0 ? lWidth : hWidth );
        }

        return pattern;
    }

    static void refreshPreferences() {

        prefs = new XSharedPreferences( new File(
                Environment.getDataDirectory(), "data/"
                + PACKAGE_NAME + "/shared_prefs/" + PACKAGE_NAME
                + "_preferences" + ".xml" ) );
        prefs.reload();

        mModeSave = prefs.getInt( "pref_key_save", mModeSave );
        mTimerMinimum = prefs.getInt( "pref_key_timer_minimum", mTimerMinimum );
        mToastEnabled = prefs.getBoolean( "pref_key_toasts_checkbox", mToastEnabled );
        mToastLength = prefs.getInt( "pref_key_toasts_duration", mToastLength );
        mSavePath = prefs.getString( "pref_key_save_location", mSavePath );
        mVibrationEnabled = prefs.getBoolean( "pref_key_vibration_checkbox", mVibrationEnabled );
        mSaveSentSnaps = prefs.getBoolean( "pref_key_save_sent_snaps", mSaveSentSnaps );
        mSortByCategory = prefs.getBoolean( "pref_key_sort_files_mode", mSortByCategory );
        mSortByUsername = prefs.getBoolean( "pref_key_sort_files_username", mSortByUsername );
        mOverlays = prefs.getBoolean( "pref_key_overlay", mOverlays );
        mDebugging = prefs.getBoolean( "pref_key_debug_mode", mDebugging );
        mTimerUnlimited = prefs.getBoolean( "pref_key_timer_unlimited", mTimerUnlimited );
        mHideTimerStory = prefs.getBoolean( "pref_key_timer_story_hide", mHideTimerStory );
        mLoopingVids = prefs.getBoolean( "pref_key_looping_video", mLoopingVids );
        mHideTimer = prefs.getBoolean( "pref_key_timer_hide", mHideTimer );
    }

    public enum SnapType
    {
        SNAP( "snap", "/ReceivedSnaps" ),
        STORY( "story", "/Stories" ),
        SENT( "sent", "/SentSnaps" ),
        CHAT( "chat", "/Chat" );

        private final String name;
        private final String subdir;

        SnapType( String name, String subdir ) {
            this.name = name;
            this.subdir = subdir;
        }
    }

    public enum SaveResponse
    {
        SUCCESS, FAILED, ONGOING, EXISTING
    }

    public enum MediaType
    {
        IMAGE( ".jpg", "Image" ),
        IMAGE_OVERLAY( ".png", "Overlay" ),
        VIDEO( ".mp4", "Video" );

        private final String fileExtension;
        private final String typeName;

        MediaType( String fileExtension, String typeName ) {
            this.fileExtension = fileExtension;
            this.typeName = typeName;
        }
    }

    public static class saveImageJPGTask extends AsyncTask<Object, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground( Object... params ) {
            if ( params[ 1 ] == null ) {
                Logger.printMessage( "Background JPG - Passed Null Image" );
                return false;
            }

            Boolean success;
            File fileToSave = (File) params[ 0 ];

            Bitmap bmp = (Bitmap) params[ 1 ];
            Context context = (Context) params[ 2 ];

            try {
                FileOutputStream out = new FileOutputStream( fileToSave );

                bmp.compress( Bitmap.CompressFormat.JPEG, 100, out );
                out.flush();
                out.close();
                vibrate( context, true );
                runMediaScanner( context, fileToSave.getAbsolutePath() );

                success = true;
            } catch ( Exception e ) {
                Logger.printMessage( "Exception while saving an image: " + e.getMessage() );
                vibrate( context, false );
                success = false;
            }

            return success;
        }
    }

    public static class saveImagePNGTask extends AsyncTask<Object, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground( Object... params ) {
            if ( params[ 1 ] == null ) {
                Logger.printMessage( "Background PNG - Passed Null Image" );
                return false;
            }

            Boolean success;
            File fileToSave = (File) params[ 0 ];
            Bitmap bmp = (Bitmap) params[ 1 ];
            Context context = (Context) params[ 2 ];

            try {
                FileOutputStream out = new FileOutputStream( fileToSave );
                bmp.compress( Bitmap.CompressFormat.PNG, 100, out );
                out.flush();
                out.close();
                vibrate( context, true );
                runMediaScanner( context, fileToSave.getAbsolutePath() );

                success = true;
            } catch ( Exception e ) {
                Logger.printMessage( "Exception while saving an image" );
                vibrate( context, false );
                success = false;
            }
            return success;
        }
    }

    public static class saveVideoTask extends AsyncTask<Object, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground( Object... params ) {
            if ( params[ 0 ] == null ) {
                Logger.printMessage( "Background VIDEO - Passed Null Video" );
                return false;
            }

            Boolean success;

            File fileToSave = (File) params[ 1 ];
            Context context = (Context) params[ 2 ];

            try {
                // Use bufferedinputstreams for faster saving - Probably unecessary
                BufferedInputStream inputStream =
                        new BufferedInputStream( (FileInputStream) params[ 0 ] );
                BufferedOutputStream outputStream =
                        new BufferedOutputStream( new FileOutputStream( fileToSave ) );

                // General disk cluster size for higher efficiency
                byte[] buffer = new byte[ 8192 ];
                int read;
                while ( ( read = inputStream.read( buffer ) ) > 0 )
                    outputStream.write( buffer, 0, read );

                outputStream.flush();
                inputStream.close();
                outputStream.close();

                vibrate( context, true );
                runMediaScanner( context, fileToSave.getAbsolutePath() );
                success = true;
            } catch ( Exception e ) {
                Logger.printMessage( "Exception while saving a video" );
                vibrate( context, false );
                success = false;
            }

            return success;
        }
    }
}