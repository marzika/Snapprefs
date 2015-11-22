package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Lens {
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    static XSharedPreferences prefs;
    public static boolean mDebugging = true;

    static void initLens(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        refreshPreferences();
        XposedHelpers.findAndHookMethod("com.snapchat.android.database.SharedPreferenceKey", lpparam.classLoader, "getBoolean", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                if (XposedHelpers.getObjectField(methodHookParam.thisObject, "b").equals("is_device_whitelisted_for_lenses_on_backend")) {
                    methodHookParam.setResult(true);
                }
            }
        });

        /*XposedHelpers.findAndHookMethod(Obfuscator.lens.LENSESPROVIDER_CLASS, lpparam.classLoader, Obfuscator.lens.LENSESPROVIDER_GETLENSES, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ArrayList result = (ArrayList) param.getResult();
                for (Object o : result) {
                    if (mDebugging) {
                        Logger.log("LOADED LENS: " + o.toString());
                    }
                }
            }
        });*/
    }
    static void refreshPreferences() {

        prefs = new XSharedPreferences(new File(
                Environment.getDataDirectory(), "data/"
                + PACKAGE_NAME + "/shared_prefs/" + PACKAGE_NAME
                + "_preferences" + ".xml"));
        prefs.reload();
        mDebugging = prefs.getBoolean("pref_key_debug_mode", mDebugging);
    }
}