package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.provider.BaseColumns;

import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.LensDatabaseHelper;

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
        lensPrepareState = findClass("com.snapchat.android.util.eventbus.LensPrepareStateChangedEvent", lpparam.classLoader);
        PrepareStatus = findClass("com.snapchat.android.util.eventbus.LensPrepareStateChangedEvent$PrepareStatus", lpparam.classLoader);
        LensClass = findClass("com.snapchat.android.model.lenses.Lens", lpparam.classLoader);
        atzClass = findClass("atz", lpparam.classLoader);
        Class TypeClass = findClass("com.snapchat.android.model.lenses.Lens$Type", lpparam.classLoader);
        enumScheduledType = getStaticObjectField(TypeClass, "SCHEDULED");

        if (MainActivity.lensDBHelper == null)
            MainActivity.lensDBHelper = new LensDatabaseHelper(snapContext);

        findAndHookMethod("com.snapchat.android.database.SharedPreferenceKey", lpparam.classLoader, "getBoolean", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                if (getObjectField(methodHookParam.thisObject, "b").equals("is_device_whitelisted_for_lenses_on_backend")) {
                    methodHookParam.setResult(true);
                }
            }
        });

        findAndHookMethod("AN", lpparam.classLoader, "onJsonResult", Object.class, findClass("Ae", lpparam.classLoader), new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                onJsonResultRebuilt(param, param.args[0], param.args[1]);
                return null;
            }
        });
    }

    public static void onJsonResultRebuilt(XC_MethodHook.MethodHookParam param, Object atR, Object Ae) {
        Logger.log("CallingJsonResult");

        if (callBoolMethod(Ae, "c") && atR != null && callBoolMethod(atR, "b") &&
                callBoolMethod(atR, "d") && callBoolMethod(atR, "f")) {

            Logger.log("Entered statement");
            List a = (List) callMethod(atR, "a");
            Logger.log("Active lens size: " + a.size());
            ArrayList<Object> activeLenses = new ArrayList<>();
            ArrayList<String> lensBlacklist = new ArrayList<>();

            for (Object atzObj : a) {
                Object lens = newInstance(LensClass, atzClass.cast(atzObj), enumScheduledType);

                if( Preferences.getBool(Prefs.LENSES_COLLECT) ) {
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

            a = (List) callMethod(atR, "c");
            Logger.log("Precached lens size: " + a.size());

            ArrayList<Object> precachedLenses = new ArrayList<>();
            for (Object atzObj : a) {
                Logger.log("Looped precached lens");
                Object lens = newInstance(LensClass, atzClass.cast(atzObj), enumScheduledType);
                activeLenses.add(lens);
            }

            if( Preferences.getBool(Prefs.LENSES_LOAD)) {
                activeLenses = buildModifiedList(activeLenses, lensBlacklist);
                precachedLenses = buildModifiedList(precachedLenses, lensBlacklist);
            }

            Logger.log("Finished list building");
            Logger.log("Building method params");
            Object callback = getObjectField(param.thisObject, "mCallback");
            Object atRg = callMethod(atR, "g");
            Object atRe = callMethod(atR, "e");
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
        ArrayList<LensData> lensList = MainActivity.lensDBHelper.getAllExcept(lensBlacklist);
        Logger.log("New lenses to load: " + lensList.size());

        for (LensData lensData : lensList) {
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
        //lensData.mLensIcon = getBitmapFromURL(lensData.mIconLink);

        return lensData;
    }

    public static class LensEntry implements BaseColumns {
        public static final String TABLE_NAME = "LensTable";
        public static final String COLUMN_NAME_MCODE = "mCode";
        public static final String COLUMN_NAME_GPLAYID = "mGplayIapId";
        public static final String COLUMN_NAME_MHINTID = "mHintId";
        public static final String COLUMN_NAME_MICONLINK = "mIconLink";
        public static final String COLUMN_NAME_MID = "mId";
        public static final String COLUMN_NAME_MLENSLINK = "mLensLink";
        public static final String COLUMN_NAME_MSIGNATURE = "mSignature";
        public static final String COLUMN_NAME_ACTIVE = "mActiveState";
    }
}