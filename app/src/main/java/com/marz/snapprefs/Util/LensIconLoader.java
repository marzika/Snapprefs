package com.marz.snapprefs.Util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageButton;

import com.marz.snapprefs.Fragments.LensesFragment;

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
            LensesFragment.LensButtonPair pair = (LensesFragment.LensButtonPair) params[0];
            Activity context = (Activity) params[1];

            final String url = pair.url;
            final ImageButton button = pair.button;

            final Bitmap bmp = getBitmapFromURL(url, 1);
            pair.bmp = bmp;

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("snapchat", "Loading image: " + url);
                    button.setImageBitmap(bmp);
                    button.invalidate();
                }
            });


            return null;
        }
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

            bmImg = BitmapFactory.decodeStream(is, null, options);
            return bmImg;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("Error", e.toString());
            return null;
        }
    }
}
