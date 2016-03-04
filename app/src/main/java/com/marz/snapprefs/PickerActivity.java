package com.marz.snapprefs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.marz.snapprefs.Util.CommonUtils;

import java.io.File;

/**
 * Created by MARZ on 2016. 03. 04..
 */
public class PickerActivity extends Activity {
    public static int SELECT_GALLERY = 1;
    boolean finish = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_RUN.equals(action)) {
                Intent galleryPickerIntent = new Intent(Intent.ACTION_PICK);
                galleryPickerIntent.setType("video/*, image/*");
                startActivityForResult(galleryPickerIntent, SELECT_GALLERY);

            } else {
                finish = false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            if(requestCode == SELECT_GALLERY){
                String data = intent.getData().toString();
                ContentResolver contentResolver = this.getContentResolver();
                String mediaUri = CommonUtils.getPathFromContentUri(contentResolver, Uri.parse(data));
                File toSend = new File(mediaUri);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(toSend));
                intent.setType(getMime(mediaUri));
                intent.setAction(Intent.ACTION_SEND);
                intent.setComponent(ComponentName.unflattenFromString("com.snapchat.android/.LandingPageActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } else {
            finish = false;
        }
        if (finish) {
            finish();
        }
    }

    public String getMime(String fileName){
        String mime = "";
        String extension = "";
        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i+1);
        }
        if(extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg")){
            mime="image/"+extension;
        } else if (extension.equalsIgnoreCase("mp4")){
            mime="video/"+extension;
        } else {
            Toast.makeText(this, "UNSUPPORTED FILE EXTENSION", Toast.LENGTH_LONG).show();
        }
        return mime;
    }
}
