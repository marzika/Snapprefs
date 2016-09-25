package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;

import com.marz.snapprefs.Databases.LensDatabaseHelper;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.LensData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

public class Lens {
    public static Class lensPrepareState;
    public static Class PrepareStatus;
    public static Class LensClass;
    public static Object enumScheduledType;
    public static Class atzClass;
    public static HashMap<String, LensData> lensDataMap = new HashMap<>();

    static void initLens(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        lensPrepareState = findClass(Obfuscator.lens.LENSPREPARESTATECHANGE, lpparam.classLoader);
        PrepareStatus = findClass(Obfuscator.lens.STATECHANGEPREPARESTATUSENUM, lpparam.classLoader);
        LensClass = findClass(Obfuscator.lens.LENSCLASS, lpparam.classLoader);
        atzClass = findClass(Obfuscator.lens.LENSCLASS_SECOND_CONSTRUCTOR_ARG, lpparam.classLoader);
        Class TypeClass = findClass(Obfuscator.lens.LENSCLASS + "$Type", lpparam.classLoader);
        enumScheduledType = getStaticObjectField(TypeClass, "SCHEDULED");

        if (MainActivity.lensDBHelper == null)
            MainActivity.lensDBHelper = new LensDatabaseHelper(snapContext);

        // TODO Allow for this to be toggled
        findAndHookMethod("com.snapchat.android.database.SharedPreferenceKey", lpparam.classLoader, "getBoolean", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                if (getObjectField(methodHookParam.thisObject, "b").equals("is_device_whitelisted_for_lenses_on_backend")) {
                    methodHookParam.setResult(true);
                }
            }
        });

        findAndHookMethod(Obfuscator.lens.LENSCALLBACK_CLASS, lpparam.classLoader, "onJsonResult", Object.class, findClass("Ae", lpparam.classLoader), new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                onJsonResultRebuilt(param, param.args[0], param.args[1]);
                return null;
            }
        });
    }

    public static void onJsonResultRebuilt(XC_MethodHook.MethodHookParam param, Object arg1, Object arg2) {
        Logger.log("CallingJsonResult");

        if (callBoolMethod(arg2, "c") && arg1 != null && callBoolMethod(arg1, "b") &&
                callBoolMethod(arg1, "d") && callBoolMethod(arg1, "f")) {

            Logger.log("Entered statement");
            List a = (List) callMethod(arg1, "a");
            Logger.log("Active lens size: " + a.size());
            ArrayList<Object> activeLenses = new ArrayList<>();
            ArrayList<String> lensBlacklist = new ArrayList<>();

            for (Object atzObj : a) {
                Object lens = newInstance(LensClass, atzClass.cast(atzObj), enumScheduledType);

                if (Preferences.getBool(Prefs.LENSES_COLLECT)) {
                    String url = (String) getObjectField(lens, "mIconLink");
                    Logger.log("Icon url: " + url);
                    String mCode = (String) getObjectField(lens, "mCode");

                    Logger.log("Handling lens: " + mCode);

                    if (MainActivity.lensDBHelper.containsLens(mCode)) {
                        Logger.log("Already contains lens: " + mCode);
                        lensBlacklist.add(mCode);
                    } else {
                        Logger.log("Saving new lens: " + mCode);
                        performLensSave(lens);
                    }
                }

                activeLenses.add(lens);
            }

            Logger.log("Finished active lens loop");

            a = (List) callMethod(arg1, "c");
            Logger.log("Precached lens size: " + a.size());

            ArrayList<Object> precachedLenses = new ArrayList<>();
            for (Object atzObj : a) {
                Logger.log("Looped precached lens");
                Object lens = newInstance(LensClass, atzClass.cast(atzObj), enumScheduledType);
                precachedLenses.add(lens);
            }

            if (Preferences.getBool(Prefs.LENSES_LOAD)) {
                activeLenses = buildModifiedList(activeLenses, lensBlacklist);
                precachedLenses = buildModifiedList(precachedLenses, lensBlacklist);
            }

            Logger.log("Finished list building");
            Logger.log("Building method params");
            Object callback = getObjectField(param.thisObject, "mCallback");
            Object atRg = callMethod(arg1, "g");
            Object atRe = callMethod(arg1, "e");
            Object longValue = callMethod(atRe, "longValue");
            Logger.log("Ready to call");
            try {
                callMethod(callback, "a", activeLenses, precachedLenses, atRg, longValue);
                Logger.log("Called method... Returning");
            } catch (Exception e) {
                Logger.log("Error calling method", e);
            }
            return;
        }

        Logger.log("Skipping statement");
        Object callback = getObjectField(param.thisObject, "mCallback");
        callMethod(callback, "a");
        Logger.log("Called end method");
    }

    public static boolean callBoolMethod(Object object, String methodName) {
        return (boolean) callMethod(object, methodName);
    }

    public static void logCaller() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTraceElements) {
            String className = element.getClassName();
            String methodName = element.getMethodName();

            Logger.log("Caller: " + className + " -> " + methodName);
        }
    }

    public static ArrayList<Object> buildModifiedList(ArrayList<Object> list, ArrayList<String> lensBlacklist) {
        Logger.log("Original list size: " + list.size());
        ArrayList<Object> lensList = MainActivity.lensDBHelper.getAllExcept(lensBlacklist);

        if (lensList == null) {
            Logger.log("No lenses to load!");
            return list;
        }

        Logger.log("New lenses to load: " + lensList.size());

        for (Object lensObj : lensList) {
            LensData lensData = (LensData) lensObj;
            if (!lensData.mActive)
                continue;

            Object lens = buildModifiedLens(lensData);
            list.add(lens);
        }
        Logger.log("Total lens count: " + list.size());

        return list;
    }

    public static Object buildModifiedLens(LensData lensData) {
        Object lens = newInstance(LensClass, lensData.mId, lensData.mCode, enumScheduledType, lensData.mIconLink, null);
        setObjectField(lens, "mHintId", lensData.mHintId);
        setObjectField(lens, "mGplayIapId", lensData.mGplayIapId);
        setObjectField(lens, "mIsBackSection", false);
        setObjectField(lens, "mIsFeatured", true);
        setObjectField(lens, "mIsLoading", true);
        setObjectField(lens, "mLensLink", lensData.mLensLink);
        setObjectField(lens, "mPriority", 0);
        setObjectField(lens, "mSignature", lensData.mSignature);

        return lens;
    }

    public static void performLensSave(Object lens) {
        LensData lensData = buildSaveableLensData(lens);
        MainActivity.lensDBHelper.insertLens(lensData);
    }

    public static LensData buildSaveableLensData(Object lens) {
        LensData lensData = new LensData();
        lensData.mId = (String) getObjectField(lens, "mId");
        lensData.mCode = (String) getObjectField(lens, "mCode");
        lensData.mGplayIapId = (String) getObjectField(lens, "mGplayIapId");
        lensData.mHintId = (String) getObjectField(lens, "mHintId");
        //lensData.mHintTranslations = (Map<String, String>) getObjectField(lens, "mHintTranslations");
        lensData.mIconLink = (String) getObjectField(lens, "mIconLink");
        //lensData.mIsBackSection = (boolean) getObjectField(lens, "mIsBackSection");
        //lensData.mIsFeatured = (boolean) getObjectField(lens, "mIsFeatured");
        //lensData.mIsLoading = (boolean) getObjectField(lens, "mIsLoading");
        //lensData.mIsSponsored = (boolean) getObjectField(lens, "mIsSponsored");
        lensData.mLensLink = (String) getObjectField(lens, "mLensLink");
        //lensData.mPriority = (int) getObjectField(lens, "mPriority");
        lensData.mSignature = (String) getObjectField(lens, "mSignature");
        lensData.mActive = Preferences.getBool(Prefs.LENSES_AUTO_ENABLE);
        //lensData.mLensIcon = getBitmapFromURL(lensData.mIconLink);

        return lensData;
    }
}