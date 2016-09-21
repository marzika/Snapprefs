package com.marz.snapprefs.Util;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by stirante
 */

public class DebugHelper {

    /**
     * Enables Developer settings, which can be accessed on the bottom of normal settings by clicking around version code
     */
    public static final boolean DEVELOPER_SETTINGS = false;
    /**
     * Enable this, if the first one did not change anything
     */
    public static final boolean DEVELOPER_SETTINGS_2 = false;
    /**
     * Enables logging timber logs to logcat. Works only when Developer settings are enabled
     */
    public static final boolean TIMBER = false;

    private static XC_LoadPackage.LoadPackageParam lpparam;

    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        DebugHelper.lpparam = lpparam;
        if (DEVELOPER_SETTINGS) {
            XposedHelpers.findAndHookMethod("com.snapchat.android.framework.release.ReleaseManager", lpparam.classLoader, "a", Context.class, XC_MethodReplacement.returnConstant("DEBUG"));
        }
        if (TIMBER) {
            XposedHelpers.findAndHookMethod("com.snapchat.android.framework.logging.Timber$1", lpparam.classLoader, "run", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.setBooleanField(param.thisObject, "d", true);
                }
            });
        }
        if (DEVELOPER_SETTINGS_2) {
            XposedHelpers.findAndHookMethod("com.snapchat.android.framework.release.ReleaseManager", lpparam.classLoader, "f", XC_MethodReplacement.returnConstant(true));
        }
    }

    public static void log(String message) {
        callStaticMethod(findClass("com.snapchat.android.Timber", lpparam.classLoader), "c", "SNAPPREFS", message, new Object[]{});
    }

}
