package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Lens {
    public static boolean mDebugging = false;
    static XSharedPreferences prefs;
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();

    static void initLens(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        refreshPreferences();

        findAndHookMethod("com.snapchat.android.database.SharedPreferenceKey", lpparam.classLoader, "getBoolean", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                if (XposedHelpers.getObjectField(methodHookParam.thisObject, "b").equals("is_device_whitelisted_for_lenses_on_backend")) {
                    methodHookParam.setResult(true);
                }
            }
        });
        if(mDebugging){
            findAndHookMethod("qo", lpparam.classLoader, "e", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ArrayList result = (ArrayList) param.getResult();
                    for (Object o : result) {
                        Logger.log("LOADED LENS: " + o.toString() + "\n ########");//This just prints loaded lenses
                    }
                }
            });
        }
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
