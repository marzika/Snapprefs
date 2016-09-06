package com.marz.snapprefs;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.PorterDuff;
        import android.graphics.PorterDuffXfermode;
        import android.os.Environment;
        import android.support.v4.view.GravityCompat;
        import android.view.Gravity;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.animation.LinearInterpolator;
        import android.widget.TextView;

        import java.io.File;
        import java.util.ArrayList;

        import de.robv.android.xposed.XC_MethodHook;
        import de.robv.android.xposed.XSharedPreferences;
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

        import static de.robv.android.xposed.XposedHelpers.callMethod;
        import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
        import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
        import static de.robv.android.xposed.XposedHelpers.findClass;
        import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
        import static de.robv.android.xposed.XposedHelpers.getObjectField;


public class VisualFilters {
    private static Context context;
    private static final String FILTER_TYPE = "filterType";
    private static final String FILTER_TITLE = "filterTitle";
    private static final String NULLIFY_FLAG = "nullify";
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    private static boolean mAmaro = false;
    private static boolean mF1997 = false;
    private static boolean mBrannan = false;
    private static boolean mEarlybird = true;
    private static boolean mHefe = false;
    private static boolean mHudson = false;
    private static boolean mInkwell = false;
    private static boolean mLomo = true;
    private static boolean mLordKelvin = false;
    private static boolean mNashville = false;
    private static boolean mRise = true;
    private static boolean mSierra = false;
    private static boolean mSutro = false;
    private static boolean mToaster = true;
    private static boolean mValencia = false;
    private static boolean mWalden = false;
    private static boolean mXproll = false;
    static XSharedPreferences prefs;
    public static ArrayList<String> added = new ArrayList<String>();
    public static ArrayList<String> added2 = new ArrayList<String>();

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
        refreshPreferences();
        setPreferences();
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
        findAndHookMethod("afo", lpparam.classLoader, "a", new XC_MethodHook() {
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
                    Object grey = XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass("com.snapchat.android.app.shared.model.filter.VisualFilterType"), "valueOf", "GREYSCALE");
                    if (afn.isInstance(afh)) {
                        Object visualFilterType = XposedHelpers.getObjectField(afh, "b");
                        if (visualFilterType == grey) {
                            for (FilterType fType : FilterType.values()) {
                                if (!fType.isEnabled()) continue;
                                if(added.contains(fType.toString())){
                                    continue;
                                }
                                Object filter = XposedHelpers.newInstance(lpparam.classLoader.loadClass(Obfuscator.visualfilters.ADDER_3_PARAM), new Class[]{lpparam.classLoader.loadClass("com.snapchat.android.app.shared.model.filter.VisualFilterType")}, grey);
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
                                Object filter = XposedHelpers.newInstance(lpparam.classLoader.loadClass(Obfuscator.visualfilters.ADDER_CLASS), new Class[]{Context.class, lpparam.classLoader.loadClass("com.snapchat.photoeffect.LibPhotoEffect"), lpparam.classLoader.loadClass("com.snapchat.android.app.shared.model.filter.VisualFilterType")}, context, null, grey);
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
        Class <?> mediabryoSnapType = findClass("com.snapchat.android.model.Mediabryo$SnapType", lpparam.classLoader);
        findAndHookMethod(Obfuscator.visualfilters.ADDER_PARAM, lpparam.classLoader, "a", boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, mediabryoSnapType, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (context == null) {
                    context = (Context) XposedHelpers.getObjectField(param.thisObject, "g");
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
                applyFilter(bitmap1, bitmap2, (FilterType) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TYPE));
                param.setResult(true);
            }
        });
        //Add filter title
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "d", new XC_MethodHook() {
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
        findAndHookMethod(Obfuscator.visualfilters.FILTERSLOADER_CLASS, lpparam.classLoader, "e", new XC_MethodHook() {
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
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "h", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE) != null) {
                    View v = ((View) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE));
                    v.animate().alpha(0.0F).setDuration(700L).setInterpolator(new LinearInterpolator()).start();
                }
            }
        });
        //title animation reset
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "i", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE) != null) {
                    View v = ((View) XposedHelpers.getAdditionalInstanceField(param.thisObject, FILTER_TITLE));
                    v.animate().cancel();
                    v.setAlpha(1.0F);
                }
            }
        });
        findAndHookMethod(Obfuscator.save.LANDINGPAGEACTIVITY_CLASS, lpparam.classLoader, "onSnapCapturedEvent", findClass(Obfuscator.visualfilters.SNAPCHAPTUREDEVENT_CLASS, lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                added.clear();
                added2.clear();
                XposedBridge.log("CLEARING ADDED - VS");
            }
        });
    }

    private static void setPreferences() {
        FilterType.AMARO.setEnabled(mAmaro);
        FilterType.F1997.setEnabled(mF1997);
        FilterType.BRANNAN.setEnabled(mBrannan);
        FilterType.EARLYBIRD.setEnabled(mEarlybird);
        FilterType.HEFE.setEnabled(mHefe);
        FilterType.HUDSON.setEnabled(mHudson);
        FilterType.INKWELL.setEnabled(mInkwell);
        FilterType.LOMO.setEnabled(mLomo);
        FilterType.LORD_KELVIN.setEnabled(mLordKelvin);
        FilterType.NASHVILLE.setEnabled(mNashville);
        FilterType.RISE.setEnabled(mRise);
        FilterType.SIERRA.setEnabled(mSierra);
        FilterType.SUTRO.setEnabled(mSutro);
        FilterType.TOASTER.setEnabled(mToaster);
        FilterType.VALENCIA.setEnabled(mValencia);
        FilterType.WALDEN.setEnabled(mWalden);
        FilterType.XPROLL.setEnabled(mXproll);
    }

    private static void applyFilter(Bitmap source, Bitmap result, FilterType type) {
        GPUImage gpuImage = new GPUImage(context);
        gpuImage.setImage(source);
        gpuImage.setFilter(type.getFilter());
        try
        {
            Bitmap filtered = gpuImage.getBitmapWithFilterApplied();

            int[] pixels = new int[filtered.getHeight() * filtered.getWidth()];
            filtered.getPixels(pixels, 0, filtered.getWidth(), 0, 0, filtered.getWidth(), filtered.getHeight());
            result.setPixels(pixels, 0, filtered.getWidth(), 0, 0, filtered.getWidth(), filtered.getHeight());
        } catch( NullPointerException e )
        {
            Logger.log("Error loading filter: " + type.toString() );
            return;
        }
//        Canvas canvas = new Canvas(result);
//
//        Paint paint = new Paint();
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(50);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
//
//        canvas.drawBitmap(result, 0, 0, paint);
//        canvas.drawText(type.name(), 150, 150, paint);
    }
    static void refreshPreferences() {
        prefs = new XSharedPreferences(new File(
                Environment.getDataDirectory(), "data/"
                + PACKAGE_NAME + "/shared_prefs/" + PACKAGE_NAME
                + "_preferences" + ".xml"));
        prefs.reload();
        mAmaro = prefs.getBoolean("AMARO", mAmaro);
        mF1997 = prefs.getBoolean("F1997", mF1997);
        mBrannan  = prefs.getBoolean("BRANNAN", mBrannan );
        mEarlybird   = prefs.getBoolean("EARLYBIRD", mEarlybird  );
        mHefe  = prefs.getBoolean("HEFE", mHefe);
        mHudson  = prefs.getBoolean("HUDSON", mHudson);
        mInkwell  = prefs.getBoolean("INKWELL", mInkwell);
        mLomo  = prefs.getBoolean("LOMO", mLomo);
        mLordKelvin  = prefs.getBoolean("LORD_KELVIN", mLordKelvin);
        mNashville  = prefs.getBoolean("NASHVILLE", mNashville);
        mRise  = prefs.getBoolean("RISE", mRise);
        mSierra  = prefs.getBoolean("SIERRA", mSierra);
        mSutro  = prefs.getBoolean("SUTRO", mSutro);
        mToaster  = prefs.getBoolean("TOASTER", mToaster);
        mValencia  = prefs.getBoolean("VALENCIA", mValencia);
        mWalden  = prefs.getBoolean("WALDEN", mWalden);
        mXproll  = prefs.getBoolean("XPROLL", mXproll);
    }
}
