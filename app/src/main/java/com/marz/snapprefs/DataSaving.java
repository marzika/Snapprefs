package com.marz.snapprefs;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by Marcell on 2015.07.23..
 */
public class DataSaving {
    static void blockDsnap(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> DownloadRequestHolder = findClass(Obfuscator.datasaving.DOWNLOADREQUEST_HOLDER_CLASS, lpparam.classLoader);
            findAndHookMethod(Obfuscator.datasaving.DSNAPDOWNLOADER_CLASS, lpparam.classLoader, Obfuscator.datasaving.DSNAPDOWNLOADER_DOWNLOADSNAP, String.class, DownloadRequestHolder, XC_MethodReplacement.DO_NOTHING);
        } catch (Throwable t) {
            Logger.log("Error while blocking DSnap Downloading", true);
            Logger.log(t.toString());
        }
    }

    static void blockFromUi(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> optional = findClass("com.google.common.base.Optional", lpparam.classLoader);
            findAndHookMethod(Obfuscator.datasaving.DSNAPDOWNLOADER_CLASS, lpparam.classLoader, "a", String.class, String.class, optional, XC_MethodReplacement.DO_NOTHING);
        } catch (Throwable t) {
            Logger.log("Error while blocking Channel Intro Download", true);
            Logger.log(t.toString());
        }
    }
    static void blockStoryPreLoad(final XC_LoadPackage.LoadPackageParam lpparam){
        try {
            findAndHookMethod(Obfuscator.datasaving.LIVESTORYPRELOAD_CLASS, lpparam.classLoader, Obfuscator.datasaving.LIVESTORYPRELOAD_METHOD, XC_MethodReplacement.DO_NOTHING);
        } catch (Throwable t){
            Logger.log("Error while blocking Story preload", true);
            Logger.log(t.toString());
        }
    }
}
