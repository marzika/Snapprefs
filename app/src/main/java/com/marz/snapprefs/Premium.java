package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Premium {
    static void initPremium(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        Class<?> ate = findClass("ate", lpparam.classLoader);
        findAndHookMethod("aty", lpparam.classLoader, "d", XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("ate", lpparam.classLoader, "y", XC_MethodReplacement.returnConstant(false));
        findAndHookMethod("ate", lpparam.classLoader, "u", XC_MethodReplacement.returnConstant(false));
        //findAndHookMethod("aty", lpparam.classLoader, "b",XC_MethodReplacement.DO_NOTHING);
        findAndHookMethod("aty", lpparam.classLoader, "a", ate, XC_MethodReplacement.DO_NOTHING);
        //findAndHookMethod("ate", lpparam.classLoader, "o", XC_MethodReplacement.DO_NOTHING);
        //findAndHookMethod("ate", lpparam.classLoader, "A", XC_MethodReplacement.DO_NOTHING);
        //findAndHookConstructor("uu", lpparam.classLoader, XC_MethodReplacement.DO_NOTHING);
        //findAndHookMethod("yy", lpparam.classLoader, "execute", XC_MethodReplacement.DO_NOTHING);
    }
}
