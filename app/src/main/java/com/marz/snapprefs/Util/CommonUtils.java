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
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.marz.snapprefs.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
     * Checks whether Snapshare is enabled. If it's enabled, Xposed should hook this method and return true.
     *
     * @return If Snapshare is enabled or not
     */
    public static boolean isModuleEnabled() {
        return true;
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
}
