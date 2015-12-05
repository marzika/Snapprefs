package com.marz.snapprefs.FilterStoreUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.marz.snapprefs.R;

import java.io.InputStream;

/**
 * Created by Marcell on 2015.12.05..
 */
public class VisualPreview extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visualpreview);
        new DownloadImageTask((TouchImageView) findViewById(R.id.img)).execute("http://snapprefs.com/filter.jpg");
    }
    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        TouchImageView bmImage;

        public DownloadImageTask(TouchImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
