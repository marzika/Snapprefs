package com.marz.snapprefs.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Vibrator;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Andre on 06/09/2016.
 */
public abstract class SavingUtils
{
    public static void saveJPGAsync( final File fileToSave, final Bitmap bmp, final Context context ) {
        new Thread(new Runnable() {
            public void run() {
                saveJPG(fileToSave, bmp, context);
            }
        }).start();
    }
    public static boolean saveJPG( File fileToSave, Bitmap bmp, Context context ) {
        boolean state = false;

        if ( bmp == null ) {
            Logger.printMessage( "saveJPG - Passed Null Image" );
            vibrate( context, false );
            return false;
        }

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream( fileToSave );

            bmp.compress( Bitmap.CompressFormat.JPEG, 100, outputStream );
            outputStream.flush();
            outputStream.close();
            vibrate( context, true );
            runMediaScanner( context, fileToSave.getAbsolutePath() );

            state = true;
        } catch ( Exception e ) {
            Logger.printMessage( "Exception while saving an image: " + e.getMessage() );
            vibrate( context, false );
        }
        finally {
            try {
                if(outputStream != null)
                    outputStream.close();
            } catch( Exception ignored){}
        }

        return state;
    }

    public static void savePNGAsync( final File fileToSave, final Bitmap bmp, final Context context ) {
        new Thread(new Runnable() {
            public void run() {
                savePNG(fileToSave, bmp, context);
            }
        }).start();
    }
    public static boolean savePNG( File fileToSave, Bitmap bmp, Context context ) {
        boolean state = false;
        if ( bmp == null ) {
            Logger.printMessage( "savePNG - Passed Null Image" );
            vibrate( context, false );
            return false;
        }
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream( fileToSave );
            bmp.compress( Bitmap.CompressFormat.PNG, 100, outputStream );
            outputStream.flush();
            vibrate( context, true );
            runMediaScanner( context, fileToSave.getAbsolutePath() );

            state = true;
        } catch ( Exception e ) {
            Logger.printMessage( "Exception while saving an image: " + e.getMessage() );
            vibrate( context, false );
        }
        finally {
            try {
                if(outputStream != null)
                    outputStream.close();
            } catch( Exception ignored){}
        }

        return state;
    }

    public static void saveVideoAsync( final File fileToSave, final FileInputStream fileStream, final Context context ) {
        new Thread(new Runnable() {
            public void run() {
                saveVideo(fileToSave, fileStream, context);
            }
        }).start();
    }
    public static boolean saveVideo( File fileToSave, FileInputStream fileStream, Context context
    ) {
        boolean state = false;
        if ( fileStream == null ) {
            Logger.printMessage( "saveVideo - Passed Null Video" );
            vibrate( context, false );
            return false;
        }
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;

        try {
            // Use bufferedinputstreams for faster saving - Probably unecessary
            inputStream =
                    new BufferedInputStream( fileStream );
            outputStream =
                    new BufferedOutputStream( new FileOutputStream( fileToSave ) );

            // General disk cluster size for higher efficiency
            byte[] buffer = new byte[ 8192 ];
            int read;
            while ( ( read = inputStream.read( buffer ) ) > 0 )
                outputStream.write( buffer, 0, read );

            outputStream.flush();

            vibrate( context, true );
            runMediaScanner( context, fileToSave.getAbsolutePath() );

            state = true;
        } catch ( Exception e ) {
            Logger.printMessage( "Exception while saving a video: " + e.getMessage() );
            vibrate( context, false );
        }
        finally {
            try {
                if( inputStream != null )
                    inputStream.close();
                if(outputStream != null)
                    outputStream.close();
            } catch( Exception ignored){}
        }

        return state;
    }

    public static void vibrate( Context context, boolean success ) {
        if( !Preferences.mVibrationEnabled )
            return;

        if ( success ) {
            Vibrator v = (Vibrator) context.getSystemService( Context.VIBRATOR_SERVICE );
            v.vibrate( genVibratorPattern( 0.7f, 400 ), -1 );
        } else {
            Vibrator v = (Vibrator) context.getSystemService( Context.VIBRATOR_SERVICE );
            v.vibrate( genVibratorPattern( 1.0f, 700 ), -1 );
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

    public static int getToastLength() {
        if ( Preferences.mToastLength == Preferences.TOAST_LENGTH_SHORT ) {
            return NotificationUtils.LENGHT_SHORT;
        } else {
            return NotificationUtils.LENGHT_LONG;
        }
    }
}
