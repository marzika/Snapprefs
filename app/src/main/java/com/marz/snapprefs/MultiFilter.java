package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.BitmapFactory;
import android.os.Environment;
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

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalStaticField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MultiFilter {
    public static ArrayList<String> added = new ArrayList<String>();
    public static void initMultiFilter(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext){
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
        final Class<?> fc = findClass("NA", lpparam.classLoader);
        final Class<?> el = findClass("MH", lpparam.classLoader); // e-> first param passed
        findAndHookMethod("NN", lpparam.classLoader, "a", Context.class, findClass("KP", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Adding new filters");
                for (File f : files) {
                    XposedBridge.log("Trying to add: " + f.toString());
                    if(added.contains(f.toString())){
                        continue;
                    }
                    Object elObj = XposedHelpers.newInstance(el, param.args[1]);
                    View view = (View) XposedHelpers.callMethod(param.args[1], "a", new Class[]{int.class, ViewGroup.class, boolean.class}, 2130968587, null, false); //battery_view 2130968587
                    XposedHelpers.setObjectField(elObj, "a", view);
                    ImageView image = (ImageView) XposedHelpers.callMethod(view, "findViewById", 2131558544); //"Battery" - battery_icon
                    image.setImageBitmap(BitmapFactory.decodeFile(f.getPath()));
                    image.setTranslationY(0);
                    ((List) param.getResult()).add(XposedHelpers.newInstance(fc, elObj));
                    added.add(f.toString());
                }
            }
        });
        findAndHookMethod(Obfuscator.save.LANDINGPAGEACTIVITY_CLASS, lpparam.classLoader, "onSnapCapturedEvent", findClass("Ue", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                added.clear();
                XposedBridge.log("CLEARING ADDED");
            }
        });
    }
}