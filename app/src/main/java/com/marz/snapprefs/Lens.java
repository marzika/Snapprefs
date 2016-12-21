package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;

import com.marz.snapprefs.Databases.LensDatabaseHelper;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.LensData.LensType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.marz.snapprefs.Util.LensData.LensType.GEO;
import static com.marz.snapprefs.Util.LensData.LensType.SCHEDULED;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

public class Lens {
    private static final List<String> stringFilter = Arrays.asList(
            "code_scheduled_lens_-_",
            "len_",
            "code_special_lens_-_"
    );
    private static Class lensPrepareState;
    private static Class PrepareStatus;
    private static Class LensClass;
    private static Object enumScheduledType;
    private static Object enumGeoType;
    private static Object enumSelfieLens;
    private static Class LensCategoryClass;
    private static Class lensListTypeClass;
    private static LensDatabaseHelper lensDatabaseHelper;

    public static LensDatabaseHelper getLensDatabase(Context context) {
        if (lensDatabaseHelper == null)
            lensDatabaseHelper = new LensDatabaseHelper(context);

        return lensDatabaseHelper;
    }

    public static LensDatabaseHelper getLensDatabase() {
        return lensDatabaseHelper;
    }

    public static String stripLensName(String mCode) {
        String nameBuilder = mCode;
        for (String filter : stringFilter)
            nameBuilder = nameBuilder.replace(filter, "");

        nameBuilder = nameBuilder.replaceAll("_", " ");

        return nameBuilder;
    }

    static void initLens(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        lensPrepareState = findClass(Obfuscator.lens.LENSPREPARESTATECHANGE, lpparam.classLoader);
        PrepareStatus = findClass(Obfuscator.lens.STATECHANGEPREPARESTATUSENUM, lpparam.classLoader);
        LensClass = findClass(Obfuscator.lens.LENSCLASS, lpparam.classLoader);
        lensListTypeClass = findClass(Obfuscator.lens.CLASS_LENSLIST_TYPE, lpparam.classLoader);
        Class TypeClass = findClass(Obfuscator.lens.LENSCLASS + "$Type", lpparam.classLoader);
        enumScheduledType = getStaticObjectField(TypeClass, "SCHEDULED");
        enumGeoType = getStaticObjectField(TypeClass, "GEO");
        LensCategoryClass = findClass("com.looksery.sdk.domain.Category", lpparam.classLoader);
        enumSelfieLens = getStaticObjectField(LensCategoryClass, "SELFIE");

        getLensDatabase(snapContext);
        // TODO Allow for this to be toggled
        findAndHookMethod("com.snapchat.android.app.shared.persistence.sharedprefs.SharedPreferenceKey", lpparam.classLoader, "getBoolean", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                if (getObjectField(methodHookParam.thisObject, "b").equals("is_device_whitelisted_for_lenses_on_backend")) {
                    methodHookParam.setResult(true);
                }
            }
        });

        findAndHookMethod("com.snapchat.android.location.LocationRequestController", lpparam.classLoader,
                "a", List.class, new XC_MethodHook() {
                    @Override
                    @SuppressWarnings("unchecked")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        List<Object> oldGeoLensList;

                        if (param.args[0] != null)
                            oldGeoLensList = (List<Object>) param.args[0];
                        else
                            oldGeoLensList = new ArrayList<>();

                        addLensesToDB(oldGeoLensList, GEO);
                    }
                });

        findAndHookMethod("aau$1", lpparam.classLoader,
                "a", List.class, List.class, String.class, long.class, new XC_MethodHook() {
                    @Override
                    @SuppressWarnings("unchecked")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        List<Object> oldScheduledLensList;

                        if (param.args[0] != null)
                            oldScheduledLensList = (List<Object>) param.args[0];
                        else
                            oldScheduledLensList = new ArrayList<>();

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

    private static void addLensesToDB(List<Object> list, LensType type) {
        for (Object lens : list) {
            String mCode = (String) getObjectField(lens, "mCode");

            if (Preferences.getBool(Prefs.LENSES_COLLECT) && !getLensDatabase().containsLens(mCode))
                performLensSave(lens, type);
        }
    }

    private static void buildModifiedList(List<Object> list, LensType type) {
        Logger.log("Original lens list size: " + list.size(), LogType.LENS);

        final LinkedHashMap<String, Object> queriedList = (LinkedHashMap<String, Object>) getLensDatabase().getAllActive();
        final boolean canInjectLenses = queriedList != null;

        if (!canInjectLenses)
            Logger.log("No lenses to load for type: " + type, LogType.LENS);

        HashSet<String> containedList = new HashSet<>();

        for (Object lens : list) {
            String mCode = (String) getObjectField(lens, "mCode");

            if (Preferences.getBool(Prefs.LENSES_COLLECT) &&
                    (!canInjectLenses || !queriedList.containsKey(mCode)) &&
                    !getLensDatabase().containsLens(mCode)) {
                performLensSave(lens, type);
            }

            if (!Preferences.getBool(Prefs.LENSES_HIDE_CURRENTLY_PROVIDED_SC_LENSES) && canInjectLenses)
                containedList.add(mCode);
        }

        if (Preferences.getBool(Prefs.LENSES_HIDE_CURRENTLY_PROVIDED_SC_LENSES))
            list.clear();

        if (!Preferences.getBool(Prefs.LENSES_LOAD) || !canInjectLenses)
            return;

        Logger.log("Potential lenses to load: " + queriedList.size(), LogType.LENS);

        int injectedLensCount = 0;
        for (Object lensObj : queriedList.values()) {
            LensData lensData = (LensData) lensObj;
            String mCode = lensData.mCode;

            if (!lensData.mActive || containedList.contains(mCode))
                continue;

            Object lens = buildModifiedLens(lensData, type);
            list.add(lens);
            injectedLensCount++;
        }

        Logger.log(String.format("Injected %s %s Lenses", injectedLensCount, String.valueOf(type)), LogType.LENS);
    }

    private static Object buildModifiedLens(LensData lensData, LensType type) {
        Object lensType = enumScheduledType;
        Object lens = newInstance(LensClass, lensData.mId, lensData.mCode, lensType, lensData.mIconLink, null, null);
        setObjectField(lens, "mHintId", lensData.mHintId);
        //setObjectField(lens, "mGplayIapId", lensData.mGplayIapId);
        setObjectField(lens, "mIsBackSection", false);
        setObjectField(lens, "mIsFeatured", true);
        setObjectField(lens, "mIsLoading", true);
        setObjectField(lens, "mLensLink", lensData.mLensLink);
        setObjectField(lens, "mPriority", 0);
        setObjectField(lens, "mSignature", lensData.mSignature);

        Object category = callMethod(lens, "a");
        setObjectField(lens, "mCategories", category);

        return lens;
    }

    private static void performLensSave(Object lens, LensType type) {
        LensData lensData = buildSaveableLensData(lens, type);
        Logger.log("Inserting lens of type: " + type, LogType.LENS);

        try {
            getLensDatabase().insertLens(lensData);
        } catch (Exception e) {
            if (lensData == null || lensData.mCode == null)
                Logger.log("Error inserting lens", e, LogType.LENS);
            else
                Logger.log("Error inserting lens: " + lensData.mCode, e, LogType.LENS);
        }
    }

    private static LensData buildSaveableLensData(Object lens, LensType type) {
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