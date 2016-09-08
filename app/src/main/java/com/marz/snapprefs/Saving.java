package com.marz.snapprefs;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.marz.snapprefs.SnapData.FlagState;
import com.marz.snapprefs.Util.NotificationUtils;
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

public class Saving
{
    //public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    public static Resources mSCResources;
    public static Bitmap sentImage;
    public static Uri videoUri;
    public static XC_LoadPackage.LoadPackageParam lpparam2;
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
    //TODO implement user selected save mode
    private static boolean asyncSaveMode = true;

    static void initSaving( final XC_LoadPackage.LoadPackageParam lpparam,
                            final XModuleResources modRes, final Context snapContext ) {
        mResources = modRes;
        lpparam2 = lpparam;

        if ( mSCResources == null ) mSCResources = snapContext.getResources();
        Preferences.refreshPreferences();

        try {
            //TODO Set up Sweep2Save - IMPORTANT -
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
                                                if ( Preferences.mModeSave ==
                                                        Preferences.DO_NOT_SAVE )
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
                        if ( Preferences.mModeSave == Preferences.DO_NOT_SAVE )
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

                    if ( Preferences.mModeSave == Preferences.DO_NOT_SAVE )
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
                    Preferences.refreshPreferences();
                    Logger.log( "----------------------- SNAPPREFS/Sent Snap ------------------------", false );

                    if ( !Preferences.mSaveSentSnaps ) {
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
                            //TODO Replace with updated system
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
                    if ( Preferences.mTimerUnlimited ) {
                        findAndHookMethod( "com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING );
                        param.setResult( (double) 9999.9F );
                    } else {
                        if ( (double) Preferences.mTimerMinimum !=
                                Preferences.TIMER_MINIMUM_DISABLED &&
                                currentResult < (double) Preferences.mTimerMinimum ) {
                            param.setResult( (double) Preferences.mTimerMinimum );
                        }
                    }
                }
            } );
            if ( Preferences.mHideTimer ) {
                findAndHookMethod( "com.snapchat.android.ui.SnapTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING );
            }
            if ( Preferences.mHideTimerStory ) {
                findAndHookMethod( "com.snapchat.android.ui.StoryTimerView", lpparam.classLoader, "onDraw", Canvas.class, XC_MethodReplacement.DO_NOTHING );
            }
            if ( Preferences.mLoopingVids ) {
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

    public static void performS2SSave() {
        if ( currentSnapData != null && relativeContext != null ) {
            if ( currentSnapData.getSnapType() == SnapType.STORY && Preferences.mModeStory !=
                    Preferences.SAVE_S2S )
                return;
            else if ( currentSnapData.getSnapType() == SnapType.SNAP && Preferences.mModeSave !=
                    Preferences.SAVE_S2S )
                return;


            performManualSnapDataSave();
        }
    }

    public static void performButtonSave() {
        if ( currentSnapData != null && relativeContext != null ) {
            if ( currentSnapData.getSnapType() == SnapType.STORY && Preferences.mModeStory !=
                    Preferences.SAVE_BUTTON )
                return;
            else if ( currentSnapData.getSnapType() == SnapType.SNAP && Preferences.mModeSave !=
                    Preferences.SAVE_BUTTON )
                return;

            performManualSnapDataSave();
        }
    }

    public static void performManualSnapDataSave() {
        if ( Preferences.mModeSave == Preferences.DO_NOT_SAVE )
            return;

        if ( currentSnapData != null && relativeContext != null ) {

            Logger.printMessage( "Found SnapData to save" );
            Logger.printMessage( "Key: " + currentSnapData.getmKey() );
            Logger.printMessage( "Sender: " + currentSnapData.getStrSender() );
            Logger.printMessage( "Timestamp: " + currentSnapData.getStrTimestamp() );
            Logger.printMessage( "SnapType: " + currentSnapData.getSnapType() );
            Logger.printMessage( "MediaType: " + currentSnapData.getMediaType() );

            try {
                //handleSave( relativeContext, currentSnapData );
                if ( currentSnapData.getFlags().contains( FlagState.COMPLETED ) &&
                        !currentSnapData.getFlags().contains( FlagState.SAVED ) ) {
                    if ( asyncSaveMode )
                        new AsyncSaveSnapData().execute( relativeContext, currentSnapData );
                    else
                        handleSave( relativeContext, currentSnapData );
                } else {
                    if ( Preferences.mToastEnabled ) {
                        NotificationUtils.showMessage(
                                "Snap already saved",
                                Color.rgb( 70, 200, 70 ),
                                SavingUtils.getToastLength(),
                                lpparam2.classLoader );
                    }
                }
            } catch ( Exception e ) {
                Logger.printFinalMessage( "Exception saving snap" );

                if ( Preferences.mToastEnabled ) {
                    NotificationUtils.showMessage(
                            "Code exception saving snap",
                            Color.rgb( 200, 70, 70 ),
                            SavingUtils.getToastLength(),
                            lpparam2.classLoader );
                }
            }
        } else {
            Logger.printFinalMessage( "No SnapData to save" );
            if ( Preferences.mToastEnabled ) {
                NotificationUtils.showMessage(
                        "No SnapData to save",
                        Color.rgb( 200, 70, 70 ),
                        SavingUtils.getToastLength(),
                        lpparam2.classLoader );
            }
        }
    }

    private static void handleSnapHeader( Context context, Object receivedSnap ) throws Exception {
        Logger.printTitle( "Handling SnapData HEADER" );
        Logger.printMessage( "Header object: " + receivedSnap.getClass().getCanonicalName() );


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

        if ( shouldSave( snapData ) ) {
            if ( asyncSaveMode )
                new AsyncSaveSnapData().execute( context, snapData );
            else
                handleSave( context, snapData );
        } else {
            currentSnapData = snapData;
            relativeContext = context;

            Logger.printFinalMessage( "Set to BUTTON saving - Awaiting press" );
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
        Object cacheType = getObjectField( mCache, "mCacheType" );
        Logger.log( "CacheType: " + cacheType );

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
        if ( shouldSave( snapData ) ) {
            if ( asyncSaveMode )
                new AsyncSaveSnapData().execute( context, snapData );
            else
                handleSave( context, snapData );
        } else
            Logger.printFinalMessage( "Set to BUTTON saving - Awaiting press" );
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
            Logger.printFinalMessage( "Tried to attach Null Bitmap" );
            return;
        }

        Logger.printMessage( "Pulled Bitmap" );

        // Assign the payload to the snapData
        snapData.setPayload( bmp );
        Logger.printMessage( "Successfully attached payload" );

        if ( shouldSave( snapData ) ) {
            if ( asyncSaveMode )
                new AsyncSaveSnapData().execute( context, snapData );
            else
                handleSave( context, snapData );
        } else
            Logger.printFinalMessage( "Auto saving disabled" );
    }

    private static boolean shouldSave( SnapData snapData ) {
        if ( !snapData.getFlags().contains( FlagState.COMPLETED ) )
            return false;

        if ( snapData.getSnapType() == SnapType.SNAP &&
                Preferences.mModeSave == Preferences.SAVE_BUTTON ||
                Preferences.mModeSave == Preferences.SAVE_S2S )
            return false;
        else if ( snapData.getSnapType() == SnapType.STORY &&
                Preferences.mModeStory == Preferences.SAVE_BUTTON ||
                Preferences.mModeStory == Preferences.SAVE_S2S )
            return false;

        return true;
    }

    /**
     * Check if the snapData has already been handled
     *
     * @param snapData
     * @param flagState - Assign a flagstate to include (E.G PAYLOAD/HEADER)
     * @return True if contains any of the flags
     */
    private static boolean scanForExisting( SnapData snapData, FlagState flagState ) {
        //TODO Remove the FAILED flag to retry saving snaps after failure occurs
        return ( snapData.getFlags().contains( flagState ) ||
                snapData.getFlags().contains( FlagState.COMPLETED ) ||
                snapData.getFlags().contains( FlagState.SAVED ) ) &&
                !snapData.getFlags().contains( FlagState.FAILED );
    }

    /**
     * Used to perform a save on a completed snapData object
     *
     * @param context
     * @param snapData
     * @throws Exception
     */
    public static void handleSave( Context context, SnapData snapData ) throws Exception {
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

                    if ( Preferences.mToastEnabled ) {
                        NotificationUtils.showMessage(
                                snapData.getMediaType().typeName + " saved",
                                Color.rgb( 70, 200, 70 ),
                                SavingUtils.getToastLength(),
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

                    if ( Preferences.mToastEnabled ) {
                        String message = "Failed saving";

                        if ( snapData.getMediaType() != null )
                            message += " " + snapData.getMediaType().typeName;

                        NotificationUtils.showMessage(
                                message,
                                Color.rgb( 200, 70, 70 ),
                                SavingUtils.getToastLength(),
                                lpparam2.classLoader );
                    }

                    return;
                }
                case ONGOING: {
                    return;
                }
                case EXISTING: {
                    if ( Preferences.mToastEnabled ) {
                        NotificationUtils.showMessage(
                                snapData.getMediaType().typeName + " already exists",
                                Color.rgb( 70, 200, 70 ),
                                SavingUtils.getToastLength(),
                                lpparam2.classLoader );
                    }

                    Logger.printMessage( "Wiping payload and adding SAVED flag" );

                    // Wipe the payload to save memory
                    // Also assigns the SAVED flag to the snap
                    snapData.wipePayload();

                    Logger.printFinalMessage(
                            snapData.getMediaType().typeName + " already exists" );
                }
            }
        }
    }


    /**
     * If printFlags is true, will print the snapData's flag list
     *
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
     *
     * @param context
     * @param snapData
     * @return
     * @throws Exception
     */
    private static SaveResponse saveReceivedSnap( Context context, SnapData snapData ) throws
                                                                                       Exception {
        if ( Preferences.mModeSave == Preferences.DO_NOT_SAVE ) {
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
    public static SaveResponse saveSnap( SnapType snapType, MediaType mediaType, Context context,
                                         Bitmap image, FileInputStream video, String filename,
                                         String sender ) throws Exception {
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
                SavingUtils.vibrate( context, false );
                return SaveResponse.EXISTING;
            }

            // the following code is somewhat redundant as it defeats the point of an async task
            // Perform an async save of the JPG
            return SavingUtils.saveJPG( imageFile, image, context ) ?
                    SaveResponse.SUCCESS :
                    SaveResponse.FAILED;
        } else if ( mediaType == MediaType.IMAGE_OVERLAY ) {
            File overlayFile =
                    new File( directory, filename + "_overlay" + MediaType.IMAGE.fileExtension );

            if ( Preferences.mOverlays ) {
                if ( overlayFile.exists() ) {
                    Logger.printMessage( "VideoOverlay already exists" );
                    SavingUtils.vibrate( context, false );
                    return SaveResponse.SUCCESS;
                }

                // the following code is somewhat redundant as it defeats the point of an async task
                // Perform an async save of the PNG
                return SavingUtils.savePNG( overlayFile, image, context ) ?
                        SaveResponse.SUCCESS :
                        SaveResponse.FAILED;
            }
        } else if ( mediaType == MediaType.VIDEO ) {
            File videoFile = new File( directory, filename + MediaType.VIDEO.fileExtension );

            if ( videoFile.exists() ) {
                Logger.printMessage( "Video already exists" );
                SavingUtils.vibrate( context, false );
                return SaveResponse.EXISTING;
            }

            // the following code is somewhat redundant as it defeats the point of an async task
            // Perform an async save of the PNG
            return SavingUtils.saveVideo( videoFile, video, context ) ?
                    SaveResponse.SUCCESS :
                    SaveResponse.FAILED;
        }

        return SaveResponse.FAILED;
    }

    public static File createFileDir( String category, String sender ) throws IOException {
        File directory = new File( Preferences.mSavePath );

        if ( Preferences.mSortByCategory || ( Preferences.mSortByUsername && sender == null ) ) {
            directory = new File( directory, category );
        }

        if ( Preferences.mSortByUsername && sender != null ) {
            directory = new File( directory, sender );
        }

        if ( !directory.exists() && !directory.mkdirs() ) {
            throw new IOException( "Failed to create directory " + directory );
        }

        return directory;
    }

    public enum SnapType
    {
        SNAP( "snap", "/ReceivedSnaps" ),
        STORY( "story", "/Stories" ),
        SENT( "sent", "/SentSnaps" ),
        CHAT( "chat", "/Chat" );

        public final String name;
        public final String subdir;

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

        public final String fileExtension;
        public final String typeName;

        MediaType( String fileExtension, String typeName ) {
            this.fileExtension = fileExtension;
            this.typeName = typeName;
        }
    }

    public static class AsyncSaveSnapData extends AsyncTask<Object, Void, Boolean>
    {
        @Override protected Boolean doInBackground( Object... params ) {
            Context context = (Context) params[ 0 ];
            SnapData snapData = (SnapData) params[ 1 ];

            Logger.printMessage( "Performing ASYNC save" );

            try {
                Saving.handleSave( context, snapData );
            } catch ( Exception e ) {
                Logger.log( "Exception performing AsyncSave ", e );
            }
            return null;
        }
    }
}