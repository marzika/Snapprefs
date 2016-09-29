package com.marz.snapprefs.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marz.snapprefs.Fragments.LensesFragment;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.MainActivity;
import com.marz.snapprefs.Preferences;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensIconLoader {
    public static class AsyncLensIconDownloader extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            LensesFragment.LensContainerData pair = (LensesFragment.LensContainerData) params[0];
            Activity context = (Activity) params[1];

            final String url = pair.url;
            final LinearLayout inflatedLayout = pair.inflatedLayout;
            final ImageView button = pair.iconImageView;
            final TextView textView = pair.textView;
            final Bitmap bmp = retrieveAppropriateBitmap(url, context);

            if( bmp == null )
                return null;

            float density = context.getResources().getDisplayMetrics().density;
            final int imgSize = (int) (65f * density);
            pair.bmp = Bitmap.createScaledBitmap(bmp, imgSize, imgSize, false);

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Logger.log("Loading image: " + url);
                    button.setImageBitmap(bmp);
                }
            });


            return null;
        }
    }

    public static Bitmap retrieveAppropriateBitmap( String url, Context context )
    {
        File iconDirectory = new File(Preferences.getSavePath(), "/LensIcons.nomedia");

        if( !iconDirectory.exists() && !iconDirectory.mkdirs()) {
            return getBitmapFromURL(url, 1);
        }

        String hashedFileName = hashBuilder(url);
        File iconFile = new File( iconDirectory, hashedFileName + ".png");

        Logger.log("IconFile: " + iconFile.getPath());
        if(iconFile.exists())
        {
            Bitmap bmp = loadBitmapFromFile(iconFile);

            if(bmp != null)
                return bmp;
        }

        Bitmap bmp = getBitmapFromURL(url, 1);
        SavingUtils.savePNGAsync(iconFile, bmp, context, false);


        File nomediaFile = new File( Preferences.getSavePath(), "/LensIcon/.nomedia");

        if( !nomediaFile.exists() )
            MainActivity.writeNoMediaFile(Preferences.getSavePath() + "/LensIcon/");

        return bmp;
    }

    public static Bitmap loadBitmapFromFile(File iconFile)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(iconFile.getPath(), options);
    }

    public static Bitmap getBitmapFromURL(String src, int sampleSize) {
        Bitmap bmImg;
        URL myFileUrl = null;

        try {
            myFileUrl = new URL(src);

            HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();

            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = sampleSize;

            return BitmapFactory.decodeStream(is, null, options);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("Error", e.toString());
            return null;
        }
    }

    public static String hashBuilder(String inputVal)
    {
        return Integer.toString(inputVal.hashCode() % 999999999);
    }
}
