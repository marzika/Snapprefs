package com.marz.snapprefs.Util;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.marz.snapprefs.HookMethods;
import com.marz.snapprefs.Obfuscator;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.R;

import java.util.Map;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by MARZ on 2016. 02. 18..
 */
public class NotificationUtils {
    public static final int LENGTH_LONG = 3500; // 3.5 seconds
    public static final int LENGTH_SHORT = 2000; // 2 seconds
    private static int DEFAULT_ICON;
    private static Drawable statusDrawable;
    private static ToastType lastToastType;

    public static void handleInitPackageResources(XModuleResources modRes) throws Throwable {
        statusDrawable = modRes.getDrawable(R.drawable.status_toast);
    }

    public static void handleInitLocalResource(Context context) {
        statusDrawable = context.getResources().getDrawable(R.drawable.status_toast);
    }

    public static void showMessage(String string, int color, int duration, ClassLoader classLoader) {
        showMessage(string, null, color, -1, duration, -1, classLoader);
    }

    private static void showMessage(String string, String title, int color, int textColor, int duration, int icon, ClassLoader classLoader) {
        Object aVar = null;
        Object a = XposedHelpers.callStaticMethod(XposedHelpers.findClass(Obfuscator.notification.NOTIFICATION_CLASS_1, classLoader), "a");
        //Object CHAT_V2 = XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.snapchat.android.util.debug.FeatureFlagManager$FeatureFlag", classLoader), "CHAT_V2");
        //if ((boolean) (XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.snapchat.android.util.debug.FeatureFlagManager", classLoader), "b", new Class[]{XposedHelpers.findClass("com.snapchat.android.util.debug.FeatureFlagManager$FeatureFlag", classLoader)}, CHAT_V2))) {

        String notifyString = (String) XposedHelpers.getObjectField(a, "c");

        if (notifyString != null) {
            Map<String, ?> notifyMap = (Map<String, ?>) XposedHelpers.getObjectField(a, "b");
            aVar = notifyMap.get(notifyString);
        }
        if (aVar == null)
            aVar = XposedHelpers.newInstance(XposedHelpers.findClass(Obfuscator.notification.NOTIFICATION_CLASS_1 + "$a", classLoader), notifyString);

        Object notificationMaker = XposedHelpers.newInstance(XposedHelpers.findClass(Obfuscator.notification.NOTIFICATION_CLASS_2, classLoader), new Class[]{String.class, String.class, int.class}, string, XposedHelpers.getObjectField(aVar, "a"), color);
        XposedHelpers.setObjectField(notificationMaker, "alternateNotificationPanel", null);
        XposedHelpers.setBooleanField(notificationMaker, "hideTitleBar", true);
        XposedHelpers.setBooleanField(notificationMaker, "dismissCurrentNotification", true);
        if (duration != -1)
            XposedHelpers.setLongField(notificationMaker, "duration", duration);
        if (icon != -1)
            XposedHelpers.setIntField(notificationMaker, "iconRes", icon);
        if (textColor != -1)
            XposedHelpers.setIntField(notificationMaker, "textColor", textColor);
        if (title != null)
            XposedHelpers.setObjectField(notificationMaker, "primaryText", title);
        XposedHelpers.callMethod(XposedHelpers.getObjectField(a, "a"), "a", notificationMaker);
    }

    public static void showStatefulMessage(String message, ToastType type, ClassLoader cl) {
        if (!Preferences.getBool(Prefs.TOAST_ENABLED))
            return;

        if (Preferences.getBool(Prefs.STEALTH_NOTIFICATIONS) && Preferences.getLicence() >= 2)
            showStealthToast(type);
        else {
            NotificationUtils.showMessage(
                    message,
                    type.color,
                    SavingUtils.getToastLength(),
                    cl);
        }
    }

    private static void showStealthToast(ToastType type) {
        if (Preferences.getLicence() < 2 || statusDrawable == null)
            return;

        final int offset = 20;
        final boolean longLength = Preferences.getInt(Prefs.TOAST_LENGTH) == Preferences.TOAST_LENGTH_LONG;

        if (lastToastType == null || type != lastToastType) {
            lastToastType = type;

            statusDrawable.setColorFilter(new
                    PorterDuffColorFilter(type.color, PorterDuff.Mode.MULTIPLY));
        }

        final ImageView view = new ImageView(HookMethods.SnapContext);
        view.setImageDrawable(statusDrawable);
        view.bringToFront();
        final int horizontalPosition = Preferences.getBool(Prefs.BUTTON_POSITION) ? Gravity.END : Gravity.START;

        HookMethods.SnapContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast statusToast = new Toast(HookMethods.SnapContext);
                statusToast.setView(view);
                statusToast.setGravity(Gravity.BOTTOM | horizontalPosition, offset, offset);
                statusToast.setDuration(longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                statusToast.show();
            }
        });
    }

    public enum ToastType {
        GOOD(Color.rgb(70, 200, 70)),
        WARNING(Color.rgb(255, 140, 0)),
        BAD(Color.rgb(200, 70, 70));

        private int color;

        ToastType(int color) {
            this.color = color;
        }
    }
}
