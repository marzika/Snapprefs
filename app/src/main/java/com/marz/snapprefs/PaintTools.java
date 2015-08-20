package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class PaintTools {
    public static void initPaint(XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context context) {
        final Class<?> colorPickerView = findClass("com.snapchat.android.ui.ColorPickerView", lpparam.classLoader);
        XposedHelpers.findAndHookConstructor("com.snapchat.android.ui.ColorPickerView", lpparam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View colorpickerview = (View) getObjectField(param.thisObject, "h");
                if (colorpickerview == null) {
                    Logger.log("colorPickerView-launched -- colorpickerview = null", true);
                } else {
                    Logger.log("colorPickerView-launched -- colorpickerview = NOT null", true);
                }
                ImageButton colorpicker = new ImageButton(context);
                colorpicker.setBackgroundColor(0);
                colorpicker.setImageDrawable(modRes.getDrawable(R.drawable.colorpicker));
                colorpicker.setScaleX((float) 0.4);
                colorpicker.setScaleY((float) 0.4);
                colorpicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "ColorPicker, yeah!", Toast.LENGTH_SHORT).show();
                    }
                });
                RelativeLayout.LayoutParams paramsPicker = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsPicker.topMargin = HookMethods.px(-40.0f);
                paramsPicker.rightMargin = HookMethods.px(5.0f);
                paramsPicker.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                ImageButton widthpicker = new ImageButton(context);
                widthpicker.setBackgroundColor(0);
                widthpicker.setImageDrawable(modRes.getDrawable(R.drawable.width));
                widthpicker.setScaleX((float) 0.4);
                widthpicker.setScaleY((float) 0.4);
                widthpicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "WidthPicker, yeah!", Toast.LENGTH_SHORT).show();
                    }
                });
                RelativeLayout.LayoutParams paramsWidth = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsWidth.topMargin = HookMethods.px(-40.0f);
                paramsWidth.rightMargin = HookMethods.px(55.0f);
                paramsWidth.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(colorpicker, paramsPicker);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(widthpicker, paramsWidth);
            }
        });
    }

    public static void setPaint(XC_LoadPackage.LoadPackageParam lpparam, Context context, final Integer color, final Integer alpha, final Float width) {
        Class<?> legacyCanvasView = findClass("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader);
        XposedHelpers.findAndHookConstructor("com.snapchat.android.ui.LegacyCanvasView$a", lpparam.classLoader, legacyCanvasView, int.class, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("CanvasView - ORIGINAL, setColor: " + param.args[1] + " setStrokeWidth: " + param.args[2], true);
                param.args[2] = width;
                Logger.log("CanvasView - NEW setColor: " + color + " setStrokeWidth: " + width, true);
            }
        });
        findAndHookMethod("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader, "setColor", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = color;
            }
        });
    }
}
