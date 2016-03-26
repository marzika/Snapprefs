package com.marz.snapprefs.Util;

import android.graphics.Typeface;

import java.io.File;
import java.util.Hashtable;

/**
 * Created by Marcell on 2016.03.26..
 */
//http://stackoverflow.com/a/16902532
public class TypefaceUtil {
    private static Hashtable<File, Typeface> fontCache = new Hashtable<File, Typeface>();

    public static Typeface get(File name) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromFile(name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}
