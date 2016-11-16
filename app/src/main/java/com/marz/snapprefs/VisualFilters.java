package com.marz.snapprefs;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.marz.snapprefs.Logger.LogType;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.sample.filter.IF1977Filter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFAmaroFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFBrannanFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFEarlybirdFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFHefeFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFHudsonFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFImageFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFInkwellFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFLomoFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFLordKelvinFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFNashvilleFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFRiseFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFSierraFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFSutroFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFToasterFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFValenciaFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFWaldenFilter;
import jp.co.cyberagent.android.gpuimage.sample.filter.IFXprollFilter;

import static com.marz.snapprefs.Preferences.Prefs.VFILTER_AMARO;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_BRANNAN;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_EARLYBIRD;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_F1997;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_HEFE;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_HUDSON;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_INKWELL;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_LOMO;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_LORD_KELVIN;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_NASHVILLE;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_RISE;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_SIERRA;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_SUTRO;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_TOASTER;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_VALENCIA;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_WALDEN;
import static com.marz.snapprefs.Preferences.Prefs.VFILTER_XPROLL;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;



public class VisualFilters {
    private static Context context;
    private static final String FILTER_TYPE = "filterType";
    private static final String FILTER_TITLE = "filterTitle";
    private static final String NULLIFY_FLAG = "nullify";
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    public static ArrayList<String> added = new ArrayList<>();
    public static ArrayList<String> added2 = new ArrayList<>();

    enum FilterType {
        AMARO(IFAmaroFilter.class),
        F1997(IF1977Filter.class),
        BRANNAN(IFBrannanFilter.class),
        EARLYBIRD(IFEarlybirdFilter.class),
        HEFE(IFHefeFilter.class),
        HUDSON(IFHudsonFilter.class),
        INKWELL(IFInkwellFilter.class),
        LOMO(IFLomoFilter.class),
        LORD_KELVIN(IFLordKelvinFilter.class),
        NASHVILLE(IFNashvilleFilter.class),
        RISE(IFRiseFilter.class),
        SIERRA(IFSierraFilter.class),
        SUTRO(IFSutroFilter.class),
        TOASTER(IFToasterFilter.class),
        VALENCIA(IFValenciaFilter.class),
        WALDEN(IFWaldenFilter.class),
        XPROLL(IFXprollFilter.class);

        private Class<? extends IFImageFilter> clz;
        private boolean enabled = true;

        public String getNiceName() {
            return clz.getSimpleName().replace("IF", "").replace("Filter", "").replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
        }

        public void setEnabled(boolean b) {
            enabled = b;
        }

        public boolean isEnabled() {
            return enabled;
        }

        FilterType(Class<? extends IFImageFilter> clz) {
            this.clz = clz;
        }

        @Override
        public String toString() {
            return name();
        }

        public IFImageFilter getFilter() {
            return (IFImageFilter) XposedHelpers.newInstance(clz);
        }
    }

    public static void initVisualFilters(final XC_LoadPackage.LoadPackageParam lpparam){
        setPreferences();
        XposedHelpers.findAndHookMethod(Obfuscator.visualfilters.FILTERMETRICSPROVIDER_CLASS, lpparam.classLoader, Obfuscator.visualfilters.VISUAL_FILTER_TYPE_CHECK_METHOD, XposedHelpers.findClass(Obfuscator.visualfilters.VISUAL_FILTER_TYPE_CHECK_METHOD_PARAMETER_CLASS, lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable()) {
                    param.setThrowable(null);
                    param.setResult(getStaticObjectField(findClass(Obfuscator.visualfilters.SETFILTER_B_CLASS, lpparam.classLoader), "INSTASNAP"));
                }
            }
        });
        //Had to change equals and hashCode method, because getAdditionalInstanceField depends on that and equals and hashCode method are changed in snapchat to use methods we're changing. It just creates StackOverflowException
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "hashCode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(System.identityHashCode(param.thisObject));
            }
        });
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "equals", Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(param.thisObject == param.args[0]);
            }
        });
        //change getName method of filters
        findAndHookMethod(Obfuscator.visualfilters.VISUALFILTERBASE, lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE) != null)
                    param.setResult(XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE).toString());
            }
        });
        /*findAndHookMethod(Obfuscator.visualfilters.FILTERSLOADER_2_CLASS, lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE) != null)
                    param.setResult(XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE).toString());
            }
        });*/
        //changed constructor to not throw NullPointerException
        Class <?> adb$b = findClass(Obfuscator.visualfilters.SETFILTER_B_CLASS, lpparam.classLoader);
        /*findAndHookConstructor(Obfuscator.visualfilters.FILTERSLOADER_2_CLASS, lpparam.classLoader, Obfuscator.visualfilters.SETFILTER_B_CLASS, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) param.setResult(null);
            }
        });*/
        //catch adding greyscale filter and add after it our filters
        Class <?> greyscaleParam = findClass(Obfuscator.visualfilters.GREYSCALE_CLASS, lpparam.classLoader);
        findAndHookMethod(Obfuscator.visualfilters.ADDFILTER_CLASS, lpparam.classLoader, "a", greyscaleParam, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Class<?> fk = lpparam.classLoader.loadClass(Obfuscator.visualfilters.ADDFILTER_PARAM);
                if (fk.isInstance(param.args[0])) {
                    Object afh = XposedHelpers.getObjectField(param.args[0], "a");
                    if (XposedHelpers.getAdditionalInstanceField(afh, FILTER_TYPE) != null) return;
                    Class<?> afn = lpparam.classLoader.loadClass(Obfuscator.visualfilters.FILTERSLOADER_2_CLASS);
                    Class<?> afi = lpparam.classLoader.loadClass(Obfuscator.visualfilters.ADDER_CLASS);
                    Object grey = XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass("com.snapchat.android.app.shared.feature.preview.model.filter.VisualFilterType"), "valueOf", "GREYSCALE");
                    if (afn.isInstance(afh)) {
                        Object visualFilterType = XposedHelpers.getObjectField(afh, "b");
                        if (visualFilterType == grey) {
                            for (FilterType fType : FilterType.values()) {
                                if (!fType.isEnabled()) continue;
                                if(added.contains(fType.toString())){
                                    continue;
                                }
                                Object filter = XposedHelpers.newInstance(lpparam.classLoader.loadClass(Obfuscator.visualfilters.ADDER_3_PARAM), new Class[]{lpparam.classLoader.loadClass("com.snapchat.android.app.shared.feature.preview.model.filter.VisualFilterType")}, grey);
                                XposedHelpers.setAdditionalInstanceField(filter, FILTER_TYPE, fType);
                                Object wrapper = XposedHelpers.newInstance(fk, new Class[]{lpparam.classLoader.loadClass(Obfuscator.visualfilters.FILTERS_CLASS)}, filter);
                                XposedHelpers.callMethod(param.thisObject, "a", wrapper);
                                added.add(fType.toString());
                            }
                        }
                    }
                    if (afi.isInstance(afh)) {
                        Object visualFilterType = XposedHelpers.getObjectField(afh, "b");
                        if (visualFilterType == grey) {
                            for (FilterType fType : FilterType.values()) {
                                if (!fType.isEnabled()) continue;
                                if(added2.contains(fType.toString())){
                                    continue;
                                }
                                Object filter = XposedHelpers.newInstance(lpparam.classLoader.loadClass(Obfuscator.visualfilters.ADDER_CLASS), new Class[]{Context.class, lpparam.classLoader.loadClass("com.snapchat.photoeffect.LibPhotoEffect"), lpparam.classLoader.loadClass(Obfuscator.visualfilters.VISUALFILTER_TYPE)}, context, null, grey);
                                XposedHelpers.setAdditionalInstanceField(filter, FILTER_TYPE, fType);
                                Object wrapper = XposedHelpers.newInstance(fk, new Class[]{lpparam.classLoader.loadClass(Obfuscator.visualfilters.FILTERS_CLASS)}, filter);
                                XposedHelpers.callMethod(param.thisObject, "a", wrapper);
                                added2.add(fType.toString());
                            }
                        }
                    }
                }
            }
        });
        //just picking context
        Class <?> mediabryoSnapType = findClass(Obfuscator.visualfilters.BRYO_SNAPTYPE, lpparam.classLoader);
        findAndHookMethod(Obfuscator.visualfilters.ADDER_PARAM, lpparam.classLoader, "a", boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, mediabryoSnapType, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (context == null) {
                    context = (Context) XposedHelpers.getObjectField(param.thisObject, "b");
                }
            }
        });
        //if it's our filter add our shaders
        findAndHookMethod(Obfuscator.visualfilters.ADDER_CLASS, lpparam.classLoader, "a", Bitmap.class, Bitmap.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE) == null)
                    return;
                Bitmap bitmap1 = (Bitmap) param.args[0];
                Bitmap bitmap2 = (Bitmap) param.args[1];

                try {
                    applyFilter(bitmap1, bitmap2, (FilterType) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE));
                } catch( Throwable t) {
                    Logger.log("Error applying filter", t, LogType.FILTER);
                }
                param.setResult(true);
            }
        });
        //Add filter title
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, Obfuscator.visualfilters.FILTER_GETVIEW, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE) == null || XposedHelpers.getAdditionalInstanceField(param.thisObject, NULLIFY_FLAG) != null)
                    return;
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE) != null) {
                    param.setResult(XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE));
                    return;
                }
                FilterType type = (FilterType) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE);
                if (context == null) {
                    return;
                }
                TextView tv = new OutlinedTextView(context);
                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                tv.setText(type.getNiceName());
                tv.setTextSize(40);
                tv.setGravity(Gravity.CENTER);
//                tv.setBackgroundColor(0x77000000);
                tv.setTextColor(0xffffffff);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, FILTER_TITLE, tv);
                param.setResult(tv);
            }
        });
        //method which sets visibility
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "a", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE) != null) {
                    XposedHelpers.callMethod(XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE), "setVisibility", param.args[0]);
                }
            }
        });
        //title nullifier
        findAndHookMethod(Obfuscator.visualfilters.FILTERSLOADER_CLASS, lpparam.classLoader, "d", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE) != null) {
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, NULLIFY_FLAG, true);
                    View v = ((View) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE));
                    v.setVisibility(View.GONE);
                }
            }
        });
        //title fade out
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "g", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE) != null) {
                    View v = ((View) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE));
                    v.animate().alpha(0.0F).setDuration(700L).setInterpolator(new LinearInterpolator()).start();
                }
            }
        });
        //title animation reset
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "h", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE) != null) {
                    View v = ((View) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE));
                    v.animate().cancel();
                    v.setAlpha(1.0F);
                }
            }
        });
    }

    private static void setPreferences() {
        FilterType.AMARO.setEnabled(Preferences.getBool(VFILTER_AMARO));
        FilterType.F1997.setEnabled(Preferences.getBool(VFILTER_F1997));
        FilterType.BRANNAN.setEnabled(Preferences.getBool(VFILTER_BRANNAN));
        FilterType.EARLYBIRD.setEnabled(Preferences.getBool(VFILTER_EARLYBIRD));
        FilterType.HEFE.setEnabled(Preferences.getBool(VFILTER_HEFE));
        FilterType.HUDSON.setEnabled(Preferences.getBool(VFILTER_HUDSON));
        FilterType.INKWELL.setEnabled(Preferences.getBool(VFILTER_INKWELL));
        FilterType.LOMO.setEnabled(Preferences.getBool(VFILTER_LOMO));
        FilterType.LORD_KELVIN.setEnabled(Preferences.getBool(VFILTER_LORD_KELVIN));
        FilterType.NASHVILLE.setEnabled(Preferences.getBool(VFILTER_NASHVILLE));
        FilterType.RISE.setEnabled(Preferences.getBool(VFILTER_RISE));
        FilterType.SIERRA.setEnabled(Preferences.getBool(VFILTER_SIERRA));
        FilterType.SUTRO.setEnabled(Preferences.getBool(VFILTER_SUTRO));
        FilterType.TOASTER.setEnabled(Preferences.getBool(VFILTER_TOASTER));
        FilterType.VALENCIA.setEnabled(Preferences.getBool(VFILTER_VALENCIA));
        FilterType.WALDEN.setEnabled(Preferences.getBool(VFILTER_WALDEN));
        FilterType.XPROLL.setEnabled(Preferences.getBool(VFILTER_XPROLL));
    }

    private static void applyFilter(Bitmap source, Bitmap result, FilterType type) {
        GPUImage gpuImage = new GPUImage(context);
        gpuImage.setImage(source);
        gpuImage.setFilter(type.getFilter());
        Bitmap filtered = gpuImage.getBitmapWithFilterApplied();

        int[] pixels = new int[filtered.getHeight() * filtered.getWidth()];
        filtered.getPixels(pixels, 0, filtered.getWidth(), 0, 0, filtered.getWidth(), filtered.getHeight());
        result.setPixels(pixels, 0, filtered.getWidth(), 0, 0, filtered.getWidth(), filtered.getHeight());
    }
}
