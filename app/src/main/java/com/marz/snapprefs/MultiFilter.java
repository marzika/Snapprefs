package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class MultiFilter {
    public static ArrayList<String> added = new ArrayList<String>();
    private static Class<?> fc;
    private static Class<?> el;

    public static void initMultiFilter(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {
        File myFile = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/Filters/");
        myFile.mkdirs();
        final File[] files = myFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".png");
            }
        });
        for (File f : files) {
            XposedBridge.log("Adding " + f.getName());
        }
        NowPlaying.init();
        fc = findClass(Obfuscator.filters.OBJECT_CLASS, lpparam.classLoader);
        el = findClass(Obfuscator.filters.FILTER_CLASS, lpparam.classLoader);
        findAndHookMethod(Obfuscator.visualfilters.FILTERS_CLASS, lpparam.classLoader, "a", MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            if (((boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "nowPlaying"))) {
                                MotionEvent event = (MotionEvent) param.args[0];
                                View view = (View) callMethod(param.thisObject, "c");//prev. d
                                if (event.getRawY() > view.getHeight() / 2) {
                                    param.setResult(true);
                                    NowPlaying.changeLayout();
                                    view.invalidate();
                                }
                            }
                        } catch (NullPointerException e) {
                            //ignore, click doesn't happens on the NowPlaying filter
                        }
                    }
                }
        );
        findAndHookMethod(Obfuscator.filters.LOADER_CLASS, lpparam.classLoader, "a", Context.class, findClass(Obfuscator.filters.LOADER_FIRST, lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Adding new filters");
                if (NowPlaying.isPlaying()) {
                    XposedBridge.log("Adding now playing");
                    if (added.contains("NowPlaying")) {
                        return;
                    }
                    Object elObj = XposedHelpers.newInstance(el, param.args[1]);
                    //TODO Get battery_view id
                    View view = (View) XposedHelpers.callMethod(param.args[1], "a", new Class[]{int.class, ViewGroup.class, boolean.class}, Obfuscator.filters.BATTERY_VIEW, null, false); //battery_view 2130968587
                    XposedHelpers.setObjectField(elObj, "a", view);
                    //TODO get battery_icon id
                    ImageView image = (ImageView) XposedHelpers.callMethod(view, "findViewById", Obfuscator.filters.BATTERY_ICON);
                    image.setImageBitmap(NowPlaying.getBitmap());
                    image.setTranslationY(0);
                    Object e = XposedHelpers.newInstance(fc, elObj);
                    XposedHelpers.setAdditionalInstanceField(e, "nowPlaying", true);
                    ((List) param.getResult()).add(e);
                    added.add("NowPlaying");
                }
                for (File f : files) {
                    addFilter(f.toString(), loadBitmap(f), param);
                }
            }
        });
    }

    private static void addFilter(String id, Bitmap bitmap, XC_MethodHook.MethodHookParam param) {
        if (added.contains(id) || bitmap == null) {
            return;
        }
        Object elObj = XposedHelpers.newInstance(el, param.args[1]);
        //TODO Get battery_view id
        View view = (View) XposedHelpers.callMethod(param.args[1], "a", new Class[]{int.class, ViewGroup.class, boolean.class}, Obfuscator.filters.BATTERY_VIEW, null, false); //battery_view 2130968587
        XposedHelpers.setObjectField(elObj, "a", view);
        //TODO get battery_icon id
        ImageView image = (ImageView) XposedHelpers.callMethod(view, "findViewById", Obfuscator.filters.BATTERY_ICON); //"Battery" - battery_icon
        image.setImageBitmap(bitmap);
        image.setTranslationY(0);
        ((List) param.getResult()).add(XposedHelpers.newInstance(fc, elObj));
        added.add(id);
    }

    public static Bitmap loadBitmap(final File f) {
        if (added.contains(f.toString())) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        final Bitmap finalBmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getPath());
                new Canvas(finalBmp).drawBitmap(bmp, 0, 0, new Paint());
                bmp.recycle();
                return null;
            }
        }.execute();
        return finalBmp;
    }
}