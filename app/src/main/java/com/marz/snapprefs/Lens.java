package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;

import com.marz.snapprefs.Databases.LensDatabaseHelper;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.LensData.LensType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.marz.snapprefs.Util.LensData.LensType.GEO;
import static com.marz.snapprefs.Util.LensData.LensType.SCHEDULED;
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
    public static Object enumSelfieLens;
    public static Class LensCategoryClass;
    public static Class lensListTypeClass;
    public static HashMap<String, LensData> lensDataMap = new HashMap<>();

    static void initLens(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        lensPrepareState = findClass(Obfuscator.lens.LENSPREPARESTATECHANGE, lpparam.classLoader);
        PrepareStatus = findClass(Obfuscator.lens.STATECHANGEPREPARESTATUSENUM, lpparam.classLoader);
        LensClass = findClass(Obfuscator.lens.LENSCLASS, lpparam.classLoader);
        lensListTypeClass = findClass(Obfuscator.lens.CLASS_LENSLIST_TYPE, lpparam.classLoader);
        Class TypeClass = findClass(Obfuscator.lens.LENSCLASS + "$Type", lpparam.classLoader);
        enumScheduledType = getStaticObjectField(TypeClass, "SCHEDULED");
        LensCategoryClass = findClass("com.looksery.sdk.domain.Category", lpparam.classLoader);
        enumSelfieLens = getStaticObjectField(LensCategoryClass, "SELFIE");

        if (MainActivity.lensDBHelper == null)
            MainActivity.lensDBHelper = new LensDatabaseHelper(snapContext);

        // TODO Allow for this to be toggled
        findAndHookMethod("com.snapchat.android.app.shared.persistence.sharedprefs.SharedPreferenceKey", lpparam.classLoader, "getBoolean", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                if (getObjectField(methodHookParam.thisObject, "b").equals("is_device_whitelisted_for_lenses_on_backend")) {
                    methodHookParam.setResult(true);
                }
            }
        });

        findAndHookMethod("aLW", lpparam.classLoader,
                "m", List.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        List<Object> oldGeoLensList = (List<Object>) param.args[0];
                        buildModifiedList(oldGeoLensList, GEO);
                    }
                });

        findAndHookMethod("aLP", lpparam.classLoader,
                "a", List.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        List<Object> oldScheduledLensList = (List<Object>) param.args[0];
                        buildModifiedList(oldScheduledLensList, SCHEDULED);
                    }
                });

        //Bypasses signiture checking
        findAndHookMethod(Obfuscator.lens.AUTHENTICATION_CLASS, lpparam.classLoader, Obfuscator.lens.SIGNITURE_CHECK_METHOD, LensClass, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });
    }

    public static void buildModifiedList(List<Object> list, LensType type) {
        Logger.log("Original list size: " + list.size());

        final HashMap<String, Object> queriedList = MainActivity.lensDBHelper.getAllOfType(type);

        if( queriedList == null ) {
            Logger.log("No lenses to load for type: " + type);
            return;
        }

        HashSet<String> containedList = new HashSet<>();

        for( Object lens : list ) {
            String mCode = (String) getObjectField(lens, "mCode");

            if(Preferences.getBool(Prefs.LENSES_COLLECT) && !queriedList.containsKey(mCode)) {
                performLensSave(lens, type);
            }

            if(!Preferences.getBool(Prefs.LENSES_HIDE_CURRENTLY_PROVIDED_SC_LENSES))
                containedList.add(mCode);
        }

        if(Preferences.getBool(Prefs.LENSES_HIDE_CURRENTLY_PROVIDED_SC_LENSES))
            list.clear();

        if(!Preferences.getBool(Prefs.LENSES_LOAD))
            return;

        Logger.log("Potential lenses to load: " + queriedList.size());

        int injectedLensCount = 0;
        for (Object lensObj : queriedList.values()) {
            LensData lensData = (LensData) lensObj;
            String mCode = lensData.mCode;

            if (!lensData.mActive || containedList.contains(mCode))
                continue;

            Object lens = buildModifiedLens(lensData);
            list.add(lens);
            injectedLensCount++;
        }

        Logger.log(String.format("Injected %s %s Lenses", injectedLensCount, String.valueOf(type)));
    }

    public static Object buildModifiedLens(LensData lensData) {
        ArrayList mCategory = new ArrayList() {{
            add(enumScheduledType);
        }};

        Object lens = newInstance(LensClass, lensData.mId, lensData.mCode, enumScheduledType, lensData.mIconLink, null, mCategory);
        setObjectField(lens, "mHintId", lensData.mHintId);
        //setObjectField(lens, "mGplayIapId", lensData.mGplayIapId);
        setObjectField(lens, "mIsBackSection", false);
        setObjectField(lens, "mIsFeatured", true);
        setObjectField(lens, "mIsLoading", true);
        setObjectField(lens, "mLensLink", lensData.mLensLink);
        setObjectField(lens, "mPriority", 0);
        setObjectField(lens, "mSignature", lensData.mSignature);

        return lens;
    }

    public static void performLensSave(Object lens, LensType type) {
        LensData lensData = buildSaveableLensData(lens, type);
        MainActivity.lensDBHelper.insertLens(lensData);
    }

    public static LensData buildSaveableLensData(Object lens, LensType type) {
        LensData lensData = new LensData();
        lensData.mId = (String) getObjectField(lens, "mId");
        lensData.mCode = (String) getObjectField(lens, "mCode");
        //lensData.mGplayIapId = (String) getObjectField(lens, "mGplayIapId");
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
        lensData.selTime = -1;

        lensData.mType = type;
        //lensData.mLensIcon = getBitmapFromURL(lensData.mIconLink);

        return lensData;
    }
}