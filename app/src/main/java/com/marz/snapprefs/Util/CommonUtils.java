package com.marz.snapprefs.Util;

/**
 * CommonUtils.java created on 2014-07-14.
 * <p/>
 * Copyright (C) 2014 P1nGu1n
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
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.marz.snapprefs.BuildConfig;
import com.marz.snapprefs.Common;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A set of commonly used utilities.
 */
public class CommonUtils {

    /**
     * Restrict instantiation of this class, it only contains static methods.
     */
    private CommonUtils() {
    }

    /**
     * Converts the content:// scheme to the file path
     *
     * @param contentResolver Provides access to the content model
     * @param contentUri      The URI to be converted using content:// scheme
     * @return The converted file path
     */
    public static String getPathFromContentUri(ContentResolver contentResolver, Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(contentUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(column_index);
            cursor.close();
            return filePath;
        } else {
            return null;
        }
    }

    /**
     * Converts the content:// scheme to the file:// scheme
     *
     * @param contentResolver Provides access to the content model
     * @param contentUri      The URI to be converted using content:// scheme
     * @return The converted URI using file:// scheme
     */
    public static Uri getFileUriFromContentUri(ContentResolver contentResolver, Uri contentUri) {
        String filePath = getPathFromContentUri(contentResolver, contentUri);
        if (filePath == null) {
            return null;
        }
        return Uri.fromFile(new File(filePath));
    }

    /**
     * Makes an human-readable string with 2 decimals of a number of bytes.
     * For example, (1024 * 1024 * 12.5) would return 12.50 MB.
     *
     * @param bytes The number of bytes
     * @return The human-readable bytes
     */
    public static String formatBytes(long bytes) {
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String[] prefixes = new String[]{"", "K", "M", "G", "T", "P", "E"};
        String prefix = prefixes[exp];
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), prefix);
    }

    /**
     * Creates a copy of a file
     *
     * @param source      The file you want to copy
     * @param destination The destination you want to copy the file to
     * @throws IOException An error occurred while copying the file
     */
    public static void copyFile(File source, File destination) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Checks whether Snapprefs is enabled. If it's enabled, Xposed should hook this method and return {@link com.marz.snapprefs.Common#MODULE_ENABLED_CHECK_INT}.
     *
     * @return Number that should be incremented for each build at {@link com.marz.snapprefs.Common#MODULE_ENABLED_CHECK_INT}
     */
    public static int isModuleEnabled() {
        return -1;
    }

    /**
     * Gets the status(Activate, Not Actived, Needs Restart) of Snapprefs
     * .
     * @return Returns one of the following values based on its findings: {@link Common#MODULE_STATUS_ACTIVATED}, {@link Common#MODULE_STATUS_NOT_ACTIVATED}, {@link Common#MODULE_STATUS_NOT_RESTARTED}
     */
    public static int getModuleStatus(){
        if(Preferences.getBool(Preferences.Prefs.DEBUGGING)) {
            Logger.log("isModuleEnabled return value: " + isModuleEnabled());
            Logger.log("MODULE_ENABLED_CHECK_INT value: " + Common.MODULE_ENABLED_CHECK_INT);
        }
        int enabledCheckInt = CommonUtils.isModuleEnabled();
        if(enabledCheckInt == (BuildConfig.BUILD_TYPE == "debug" ? Common.MODULE_ENABLED_CHECK_INT : BuildConfig.VERSION_CODE)) {
            return Common.MODULE_STATUS_ACTIVATED;
        }
        if(enabledCheckInt == -1) {
            return Common.MODULE_STATUS_NOT_ACTIVATED;
        }
        return Common.MODULE_STATUS_NOT_RESTARTED;
    }

    /**
     * Open Xposed Installer
     *
     * @param activity The activity to start the intent on
     */
    public static void openXposedInstaller(Activity activity) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
        if (intent == null) {
            Toast.makeText(activity, activity.getString(R.string.unable_open_xposed), Toast.LENGTH_LONG).show();
        } else {
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
    }

    public static String sha256(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] digest = md.digest();
        return String.format("%064x", new java.math.BigInteger(1, digest));
    }

    // Based off of http://stackoverflow.com/questions/15158651/generate-a-md5-sum-from-an-android-bitmap-object
    public static String sha256(Bitmap bmp) throws NoSuchAlgorithmException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bitmapBytes = baos.toByteArray();
        MessageDigest digest = MessageDigest.getInstance("SHA256");
        byte [] hashedBmp = digest.digest(bitmapBytes);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i  = 0; i < 10; i++) {
            stringBuilder.append(Integer.toString((hashedBmp[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

}
