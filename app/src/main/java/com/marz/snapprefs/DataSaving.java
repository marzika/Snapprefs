package com.marz.snapprefs;

import android.database.Cursor;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Looper;
import android.widget.Toast;

import com.marz.snapprefs.Util.NotificationUtils;

import de.robv.android.xposed.XC_MethodHook;
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
            Class<?> DownloadRequest = findClass(Obfuscator.datasaving.DOWNLOADREQUEST_CLASS, lpparam.classLoader);
            Class<?> DynamicByteBuffer = findClass(Obfuscator.datasaving.DYNAMICBYTEBUFFER_CLASS, lpparam.classLoader);
            Class<?> NetworkResult = findClass(Obfuscator.datasaving.NETWORKRESULT_CLASS, lpparam.classLoader);
            findAndHookMethod(Obfuscator.datasaving.DSNAPDOWNLOADER_CLASS, lpparam.classLoader, Obfuscator.datasaving.DSNAPDOWNLOADER_DOWNLOADSNAP, DownloadRequest, DynamicByteBuffer, NetworkResult, XC_MethodReplacement.DO_NOTHING);
            findAndHookMethod(Obfuscator.datasaving.DSNAPDOWNLOAD_CLASS, lpparam.classLoader, "a", String.class, XposedHelpers.findClass(Obfuscator.datasaving.DSNAPDOWNLOAD_PARAM, lpparam.classLoader), XC_MethodReplacement.DO_NOTHING);
        } catch (Throwable t) {
            Logger.log("Error while blocking DSnap Downloading", true);
            Logger.log(t.toString());
        }
    }

    static void blockFromUi(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            //Class<?> ChannelPage = findClass("com.snapchat.android.discover.model.ChannelPage", lpparam.classLoader);
            //findAndHookMethod("afh", lpparam.classLoader, "a", ChannelPage, boolean.class, XC_MethodReplacement.returnConstant(false));
            findAndHookMethod(Obfuscator.datasaving.DSNAPINTRODOWNLOAD_CLASS, lpparam.classLoader, "a", String.class, XposedHelpers.findClass(Obfuscator.datasaving.DSNAPDOWNLOAD_PARAM, lpparam.classLoader), XC_MethodReplacement.DO_NOTHING);
            //findAndHookMethod(Obfuscator.datasaving.CHANNELDOWNLOADER_CLASS, lpparam.classLoader, Obfuscator.datasaving.CHANNELDOWNLOADER_START, Cursor.class, XC_MethodReplacement.returnConstant(null));
        } catch (Throwable t) {
            Logger.log("Error while blocking Channel Intro Download", true);
            Logger.log(t.toString());
        }
    }
    static void blockStoryPreLoad(final XC_LoadPackage.LoadPackageParam lpparam){
        try {
            findAndHookMethod(Obfuscator.datasaving.LIVESTORYPRELOAD_CLASS, lpparam.classLoader, Obfuscator.datasaving.LIVESTORYPRELOAD_METHOD, XC_MethodReplacement.DO_NOTHING);
            findAndHookMethod(Obfuscator.datasaving.STORYPRELOAD_CLASS, lpparam.classLoader, Obfuscator.datasaving.STORYPRELOAD_METHOD, XC_MethodReplacement.DO_NOTHING);
        } catch (Throwable t){
            Logger.log("Error while blocking Story preload", true);
            Logger.log(t.toString());
        }
    }
}
