package com.marz.snapprefs.FilterStoreUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.R;

import java.io.File;

/**
 * Created by daltonding on 9/30/15.
 */
public class FilterPreview extends Activity {

    Button button;
    ImageView image;
    String imgPath;
    Activity fp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_filter);

        image = (ImageView) findViewById(R.id.filterpreview);

        if (getIntent().hasExtra("imagePath")) {
            imgPath = getIntent().getStringExtra("imagePath");

            try {
                Bitmap bm = decodeSampledBitmapFromUri(imgPath, 600,
                        1067);

                image.setImageBitmap(bm);
            } catch (Exception e) {
                e.printStackTrace();
            }

            fp = this;

            addListenerOnButton();
        }

    }

    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth,
                                             int reqHeight) {

        Bitmap bm = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);


        return bm;
    }

    public int calculateInSampleSize(

            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height
                        / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }

        return inSampleSize;
    }

    public void addListenerOnButton() {
        button = (Button) findViewById(R.id.change_filter);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //FileUtils.writeToSDFolder(imgPath, "filterSettings");       //TODO somehow dynamically changing the file name in the filter loading method doesnt work.

                String filtPath = Environment.getExternalStorageDirectory().toString() + "/Snapprefs/Filters";

                File temp = new File(filtPath + "/temp_fullscreen_filter.png");

                File from = new File(imgPath);

                File temp2 = new File(imgPath);

                File to = new File(filtPath + "/fullscreen_filter.png");

                to.renameTo(temp);
                from.renameTo(to);
                temp.renameTo(temp2);
                //String path=filtPath + "/fullscreen_filter.png";//it contain your path of image..im using a temp string..
                //String filename=path.substring(path.lastIndexOf("/")+1);

                // to.renameTo(temp);

                // to.renameTo(new File(filtPath + "/" + to.getName().substring(0, to.getName().length()-4) + ".png"));
                Logger.log("renamed");

                //from.renameTo(new File(filtPath + "/fullscreen_filter.png"));

                Toast.makeText(getApplicationContext(), "Set! Restart Snapchat",
                        Toast.LENGTH_LONG).show();

                Tab1Fragment.toReload = true;
            }

        });

    }


}
