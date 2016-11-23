package com.marz.snapprefs;

/**
 * ReceiveMediaActivity.java created on 2013-06-26.
 * <p/>
 * Copyright (C) 2013 Sebastian Stammler <stammler@cantab.net>
 * <p/>
 * This file is part of Snapshare.
 * <p/>
 * Snapshare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Snapshare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * a gazillion times. If not, see <http://www.gnu.org/licenses/>.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.marz.snapprefs.Util.CommonUtils;
import com.marz.snapprefs.Util.FileUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

/**
 * This Activity has an intent-filter to receive images and videos.
 * <p/>
 * It basically functions as a wrapper around Snapchat. Upon creation, we double check, that actually
 * an image or video was passed to it and then call Snapchat's main launcher Activity,
 * com.snapchat.android.LandingPageActivity with the same intent.
 * <p/>
 * Now the remaining work is done in some hooked methods of Snapchat. Upon creation of the
 * LandingPageActivity, the injected code checks if the intent is a share intent and then does the
 * work necessary to let the image or video be shown.
 */
public class ReceiveMediaActivity extends Activity implements DialogInterface.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean finish = true;
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        try {
            if (type != null && Intent.ACTION_SEND.equals(action) && (type.startsWith("image/") || type.startsWith("video/"))) {
                Uri mediaUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

                if (mediaUri != null) {
                    File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/temp");
                    directory.mkdirs();
                    for (File f : directory.listFiles()) {
                        if (f.getName().toLowerCase().startsWith("share")) f.delete();
                    }
                    File out = File.createTempFile("share", ".no_media", directory);
                    Log.d(Common.LOG_TAG, "Received Media share of type " + type + "\nand URI " + mediaUri.toString() + "\nCalling hooked Snapchat with same Intent.");
                    if (CommonUtils.getModuleStatus() == Common.MODULE_STATUS_ACTIVATED) {
                        if (type.startsWith("image/")) {
                            finish = false;
                            if(Preferences.getInt(Preferences.Prefs.ADJUST_METHOD) == Common.ADJUST_SCALE){
                                FileUtils.saveStream(getContentResolver().openInputStream(mediaUri), out);
                                intent.setComponent(ComponentName.unflattenFromString("com.snapchat.android/.LandingPageActivity"));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(out));
                                startActivity(intent);
                            } else {
                                UCrop.Options options = new UCrop.Options();
                                options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);
                                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                                options.setCompressionQuality(100);
                                UCrop.of(mediaUri, Uri.fromFile(out))
                                        .withAspectRatio(9, 16)
                                        .withMaxResultSize(1080, 1920)
                                        .withOptions(options)
                                        .start(this);
                            }
                        } else {
                            FileUtils.saveStream(getContentResolver().openInputStream(mediaUri), out);
                            intent.setComponent(ComponentName.unflattenFromString("com.snapchat.android/.LandingPageActivity"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(out));
                            startActivity(intent);
                        }
                    } else {
                        createXposedDialog().show();
                        finish = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Finish this activity
        if (finish) {
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Intent intent = getIntent();
            intent.putExtra(Intent.EXTRA_STREAM, resultUri);
            intent.setComponent(ComponentName.unflattenFromString("com.snapchat.android/.LandingPageActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            cropError.printStackTrace();
        }
    }

    /**
     * Pretty much self-explanatory, creates a dialog saying the module is not activated.
     *
     * @return The created dialog
     */
    private AlertDialog createXposedDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Dialog));
        dialogBuilder.setTitle(getString(R.string.app_name));
        if(CommonUtils.getModuleStatus() == Common.MODULE_STATUS_NOT_ACTIVATED) {
            dialogBuilder.setMessage(getString(R.string.module_not_enabled));
        } else {
            dialogBuilder.setMessage(getString(R.string.device_needs_restart_for_update));
        }
        dialogBuilder.setPositiveButton(getString(R.string.open_xposed_installer), this);
        dialogBuilder.setNegativeButton(getString(R.string.close), this);
        return dialogBuilder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            CommonUtils.openXposedInstaller(ReceiveMediaActivity.this);
        }
        finish();
    }
}
