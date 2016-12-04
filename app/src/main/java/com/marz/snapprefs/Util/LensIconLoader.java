package com.marz.snapprefs.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.marz.snapprefs.Fragments.LensesFragment;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
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
    @Nullable
    private static Bitmap retrieveAppropriateBitmap(String url, Context context) {
        File iconDirectory = new File(Preferences.getSavePath(), "/LensIcon/");

        if (!iconDirectory.exists() && !iconDirectory.mkdirs()) {
            return getBitmapFromURL(url, 1, context);
        }

        String hashedFileName = hashBuilder(url);
        File iconFile = new File(iconDirectory, hashedFileName + ".png");
        Bitmap bmp;

        if (iconFile.exists()) {
            bmp = loadBitmapFromFile(iconFile);

            if (bmp != null)
                return bmp;
        }

        bmp = getBitmapFromURL(url, 1, context);

        if (bmp == null)
            return null;

        SavingUtils.savePNGAsync(iconFile, bmp, context, false);

        MainActivity.writeNoMediaFile(Preferences.getSavePath() + "/LensIcon");

        return bmp;
    }

    private static Bitmap loadBitmapFromFile(File iconFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(iconFile.getPath(), options);
    }

    @Nullable
    private static Bitmap getBitmapFromURL(String src, int sampleSize, Context context) {
        if (!MainActivity.isNetworkAvailable(context))
            return null;

        Bitmap bmImg;
        URL myFileUrl;

        try {
            myFileUrl = new URL(src);

            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;

            return BitmapFactory.decodeStream(is, null, options);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("Error", e.toString());
            return null;
        }
    }

    private static String hashBuilder(String inputVal) {
        return Integer.toString(inputVal.hashCode() % 999999999);
    }

    public static class AsyncLensIconDownloader extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                LensesFragment.LensItemData pair = (LensesFragment.LensItemData) params[0];
                Activity context = (Activity) params[1];
                final ImageView iconView = (ImageView) params[2];

                final String url = pair.url;
                final Bitmap bmp = retrieveAppropriateBitmap(url, context);

                if (bmp == null) {
                    Logger.log("Could not retrieve Lens Icon", LogType.LENS);
                    return null;
                }

                float density = context.getResources().getDisplayMetrics().density;
                final int imgSize = (int) (65f * density);
                pair.lensIcon = Bitmap.createScaledBitmap(bmp, imgSize, imgSize, false);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iconView.setImageBitmap(bmp);
                    }
                });
            } catch (Throwable e) {
                Logger.log("Error inside async", e, LogType.LENS);
            }

            return null;
        }
    }
}
