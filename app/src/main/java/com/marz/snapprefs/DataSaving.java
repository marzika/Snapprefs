package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by Marcell on 2015.07.23..
 */
public class DataSaving {
    static void initMethod(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        Logger.log("We are in DataSaving.initMethod", true);
        try {
            Class<?> DownloadRequest = findClass(Obfuscator.datasaving.DOWNLOADREQUEST_CLASS, lpparam.classLoader);
            Logger.log("We are in DataSaving, we have class 1", true);
            Class<?> DynamicByteBuffer = findClass(Obfuscator.datasaving.DYNAMICBYTEBUFFER_CLASS, lpparam.classLoader);
            Logger.log("We are in DataSaving, we have class 2", true);
            Class<?> NetworkResult = findClass(Obfuscator.datasaving.NETWORKRESULT_CLASS, lpparam.classLoader);
            Logger.log("We are in DataSaving, we have class 3", true);
            findAndHookMethod(Obfuscator.datasaving.DSNAPDOWNLOADER_CLASS, lpparam.classLoader, Obfuscator.datasaving.DSNAPDOWNLOADER_DOWNLOADSNAP, DownloadRequest, DynamicByteBuffer, NetworkResult, XC_MethodReplacement.DO_NOTHING);
            Logger.log("We are in DataSaving, denied Discovery", true);
        } catch (Throwable t) {
            Logger.log("Error while DataSaving", true);
            Logger.log(t.toString());
        }
    }
}
