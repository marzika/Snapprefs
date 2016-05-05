package com.marz.snapprefs.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.marz.snapprefs.Fragments.DownloadedFiltersFragment;
import com.marz.snapprefs.R;

import java.io.File;

/**
 * Created by daltonding on 9/30/15.
 */
public class DownloadedFilterPreview extends Activity {

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
        button = (Button) findViewById(R.id.filter_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final File toDelete = new File(imgPath);
                AlertDialog.Builder builder = new AlertDialog.Builder(DownloadedFilterPreview.this);
                builder.setMessage("Are you sure, that you want to delete " + toDelete.getName() + " ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FileUtils.deleteSDFile(toDelete);
                        Toast.makeText(getApplication(), "Succesfully deleted " + toDelete.getName(), Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        DownloadedFiltersFragment.buttonReload.performClick();
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

    }


}
