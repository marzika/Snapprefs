package com.marz.snapprefs.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.marz.snapprefs.Fragments.DownloadedFiltersFragment;
import com.marz.snapprefs.Fragments.FilterFragment;
import com.marz.snapprefs.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by Marcell on 2016.05.05..
 */
public class FilterPreview extends Activity {

    Button button;
    ImageView image;
    String imgPath;
    String imgId;
    Activity fp;
    ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_filter);
        progress = ProgressDialog.show(FilterPreview.this, "Loading", "Please wait", true);

        image = (ImageView) findViewById(R.id.filterpreview);

        if (getIntent().hasExtra("imagePath") && getIntent().hasExtra("imageId")) {
            imgPath = getIntent().getStringExtra("imagePath");
            imgId = getIntent().getStringExtra("imageId");
            imgPath = imgPath + ".png";
            DrawableManager.fetchDrawableOnThread(imgPath, image);

            fp = this;

            addListenerOnButton();
        }
        progress.dismiss();

    }

    public void addListenerOnButton() {
        button = (Button) findViewById(R.id.filter_button);
        button.setText("Save Filter");
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new SaveFilter(imgPath, imgId).execute();
            }
        });

    }
    class SaveFilter extends AsyncTask<Void, Void, Boolean> {
        String path;
        String id;

        public SaveFilter(String path, String id) {
            this.path = path;
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage("Downloading filter");
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(path);
                HttpResponse response = httpClient.execute(request);
                InputStream input = response.getEntity().getContent();
                File f = new File(FilterFragment.filtersDir, id + ".png");
                FileOutputStream output = new FileOutputStream(f);
                try {
                    byte[] buffer = new byte[4096];
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } finally {
                    output.close();
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progress.dismiss();
            if (result)
                Toast.makeText(FilterPreview.this, "Saved succesfully", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(FilterPreview.this, "Failed to download filter!", Toast.LENGTH_LONG).show();
            DownloadedFiltersFragment.buttonReload.performClick();
        }
    }


}
