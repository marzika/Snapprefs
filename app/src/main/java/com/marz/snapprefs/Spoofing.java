package com.marz.snapprefs;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.marz.snapprefs.Util.FileUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Spoofing {

    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    static XSharedPreferences prefs;
    private static float mSpeedValue;

    static void initSpeed(final LoadPackageParam lpparam, Context context) {
        Logger.log("Setting speed");
        String speed = FileUtils.readFromFile(context);
        final float mSpeedValue = Float.parseFloat(speed);
        findAndHookMethod(Obfuscator.spoofing.SPEEDOMETERVIEW_CLASS, lpparam.classLoader, Obfuscator.spoofing.SPEEDOMETERVIEW_SETSPEED, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = mSpeedValue;
                Logger.log("Set speed to " + mSpeedValue, true);
            }
        });
    }

    static void initLocation(final LoadPackageParam lpparam, Context context, final String latitude, final String longitude) {
        findAndHookMethod("akt", lpparam.classLoader, "d", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                float mLatitude = Float.valueOf(latitude);
                float mLongitude = Float.valueOf(longitude);
                Location fakedLocation = new Location(LocationManager.GPS_PROVIDER);
                //fakedLocation.setAccuracy();
                fakedLocation.setAccuracy((float) 0.001);
                fakedLocation.setAltitude(0.001);
                fakedLocation.setLatitude(mLatitude);
                fakedLocation.setLongitude(mLongitude);
                float accuracy = fakedLocation.getAccuracy();
                double altitude = fakedLocation.getAltitude();
                double longitude = fakedLocation.getLongitude();
                double latitude = fakedLocation.getLatitude();
                String provider = fakedLocation.getProvider();
                Logger.log("Acc: " + accuracy + "\nAltitude: " + altitude + "\nLongitude: " + longitude + "\nLatitude: " + latitude + "\nProvider: " + provider);
                param.setResult(fakedLocation);
            }
        });
    }
}
