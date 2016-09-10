package com.marz.snapprefs.Util;

import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;

import com.marz.snapprefs.HookMethods;
import com.marz.snapprefs.Obfuscator;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.R;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

/**
 * Created by MARZ on 2016. 02. 18..
 */
public class NotificationUtils {
    public static final int LENGHT_LONG = 3500; // 3.5 seconds
    public static final int LENGHT_SHORT = 2000; // 2 seconds
    public static int DEFAULT_ICON;

    public static void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (resparam.packageName.equalsIgnoreCase("com.snapchat.android")) {
            int drawable = R.drawable.snapprefs_btn_check_on_pressed_holo_light;
            XModuleResources modRes = XModuleResources.createInstance(HookMethods.MODULE_PATH, resparam.res);
            DEFAULT_ICON = XResources.getFakeResId(modRes, drawable);
            resparam.res.setReplacement(DEFAULT_ICON, modRes.fwd(drawable));
        }
    }

    public static void showMessage(String string, int color, int duration, ClassLoader classLoader) {
        showMessage(string, null, color, -1, duration, -1, classLoader);
    }

    public static void showMessage(String string, String title, int color, int textColor, int duration, int icon, ClassLoader classLoader) {
        Object aVar = null;
        Object a = XposedHelpers.callStaticMethod(XposedHelpers.findClass(Obfuscator.notification.NOTIFICATION_CLASS_1, classLoader), "a");
        //Object CHAT_V2 = XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.snapchat.android.util.debug.FeatureFlagManager$FeatureFlag", classLoader), "CHAT_V2");
        //if ((boolean) (XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.snapchat.android.util.debug.FeatureFlagManager", classLoader), "b", new Class[]{XposedHelpers.findClass("com.snapchat.android.util.debug.FeatureFlagManager$FeatureFlag", classLoader)}, CHAT_V2))) {
        if (XposedHelpers.getObjectField(a, "c") != null) {
            aVar = XposedHelpers.callMethod(XposedHelpers.getObjectField(a, "b"), "get", XposedHelpers.getObjectField(a, "c"));
        }
        if (aVar == null) {
            //aVar = XposedHelpers.callMethod(a, "b");
            aVar = XposedHelpers.newInstance(XposedHelpers.findClass("vz$a", classLoader), XposedHelpers.getObjectField(a, "c"));
        }
        //} else {
        //    aVar = XposedHelpers.callMethod(a, "b");
        //}
        Object xu = XposedHelpers.newInstance(XposedHelpers.findClass(Obfuscator.notification.NOTIFICATION_CLASS_2, classLoader), new Class[]{String.class, String.class, int.class}, string, XposedHelpers.getObjectField(aVar, "a"), color);
        XposedHelpers.setObjectField(xu, "alternateNotificationPanel", null);
        XposedHelpers.setBooleanField(xu, "hideTitleBar", true);
        XposedHelpers.setBooleanField(xu, "dismissCurrentNotification", true);
        if (duration != -1)
            XposedHelpers.setLongField(xu, "duration", duration);
        if (icon != -1)
            XposedHelpers.setIntField(xu, "iconRes", icon);
        if (textColor != -1)
            XposedHelpers.setIntField(xu, "textColor", textColor);
        if (title != null)
            XposedHelpers.setObjectField(xu, "primaryText", title);
        XposedHelpers.callMethod(XposedHelpers.getObjectField(a, "a"), "a", xu);
    }

    public static void showStatefulMessage(String message, ToastType type, ClassLoader cl) {
        if (!Preferences.mToastEnabled)
            return;

        NotificationUtils.showMessage(
                message,
                type.color,
                SavingUtils.getToastLength(),
                cl);
    }

    public enum ToastType {
        GOOD(Color.rgb(70, 200, 70)),
        WARNING(Color.rgb(255, 140, 0)),
        BAD(Color.rgb(200, 70, 70));

        private int color;

        private ToastType(int color) {
            this.color = color;
        }
    }
}
