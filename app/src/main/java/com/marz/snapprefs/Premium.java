package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;

import java.util.List;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Premium {
    static void initReplay(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        findAndHookMethod(Obfuscator.save.USER_CLASS, lpparam.classLoader, "a", XC_MethodReplacement.returnConstant(true));

        //Below prev. y
        //Big refactor: Assuming outcome
        findAndHookMethod(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader, "b", XC_MethodReplacement.returnConstant(false));

        //Below prev. u
        findAndHookMethod(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader, "j", XC_MethodReplacement.returnConstant(false));
        //findAndHookMethod("aty", lpparam.classLoader, "b",XC_MethodReplacement.DO_NOTHING);

        //findAndHookMethod("ate", lpparam.classLoader, "o", XC_MethodReplacement.DO_NOTHING);
        //findAndHookMethod("ate", lpparam.classLoader, "A", XC_MethodReplacement.DO_NOTHING);
        //findAndHookConstructor("uu", lpparam.classLoader, XC_MethodReplacement.DO_NOTHING);
        //findAndHookMethod("yy", lpparam.classLoader, "execute", XC_MethodReplacement.DO_NOTHING);
    }

    static void initTyping(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        //TODO Check function and types
        findAndHookMethod(Obfuscator.chat.ISTYPING_CLASS, lpparam.classLoader, Obfuscator.chat.ISTYPING_METHOD, String.class, List.class, XC_MethodReplacement.DO_NOTHING);
        //prev. c
        findAndHookMethod(Obfuscator.chat.CONVERSATION_CLASS, lpparam.classLoader, "t", XC_MethodReplacement.DO_NOTHING);
    }

    static void initViewed(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        Class<?> recievedSnap = findClass(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader);
        findAndHookMethod(Obfuscator.premium.SNAP_UPDATE_CLASS, lpparam.classLoader, "a", recievedSnap, XC_MethodReplacement.DO_NOTHING);
    }
}
