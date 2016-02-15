package com.marz.snapprefs;

import android.content.res.XModuleResources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Misc {
    static void initTimer(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        final Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setTextSize(100);
        p.setStrokeWidth(10);
        p.setAntiAlias(true);
        findAndHookMethod(Obfuscator.timer.TAKESNAPBUTTON_CLASS, lpparam.classLoader, Obfuscator.timer.TAKESNAPBUTTON_ONDRAW, Canvas.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getBooleanField(param.thisObject, Obfuscator.timer.TAKESNAPBUTTON_BLEAN1) && XposedHelpers.getBooleanField(param.thisObject, Obfuscator.timer.TAKESNAPBUTTON_BLEAN2)) {
                    Canvas c = (Canvas) param.args[0];
                    long time = SystemClock.elapsedRealtime() - XposedHelpers.getLongField(param.thisObject, Obfuscator.timer.TAKESNAPBUTTON_TIME);
                    String t = String.valueOf(time / 1000);
                    Rect rekt = new Rect();
                    p.getTextBounds(t, 0, t.length(), rekt);
                    c.drawText(t, XposedHelpers.getFloatField(param.thisObject, Obfuscator.timer.TAKESNAPBUTTON_X) - (rekt.width() / 2) - 5, XposedHelpers.getFloatField(param.thisObject, Obfuscator.timer.TAKESNAPBUTTON_Y) + (rekt.height() / 2) - 5, p);
                }
            }
        });
    }
}
