package com.marz.snapprefs;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.marz.snapprefs.Util.FileUtils;

import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Spoofing {
    static float speed;

    static void initSpeed(final LoadPackageParam lpparam, Context context) {
        findAndHookMethod(Obfuscator.spoofing.SPEEDOMETERVIEW_CLASS, lpparam.classLoader, Obfuscator.spoofing.SPEEDOMETERVIEW_SETSPEED, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (speed != 0) {
                    param.args[0] = speed;
                }
            }
        });
    }

    static void initLocation(final LoadPackageParam lpparam, final Context context) {
        findAndHookMethod(Obfuscator.spoofing.LOCATION_CLASS, lpparam.classLoader, Obfuscator.spoofing.LOCATION_GETLOCATION, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String rawlatitude = FileUtils.readFromSDFile("latitude");
                String rawlongitude = FileUtils.readFromSDFile("longitude");
                float mLatitude = Float.valueOf(rawlatitude);
                float mLongitude = Float.valueOf(rawlongitude);
                Location fakedLocation = new Location(LocationManager.GPS_PROVIDER);
                Random acc = new Random();
                Random alt = new Random();
                fakedLocation.setAccuracy(acc.nextFloat() * 50);
                fakedLocation.setAltitude(alt.nextDouble() * 500);
                fakedLocation.setLatitude(mLatitude);
                fakedLocation.setLongitude(mLongitude);
                float accuracy = fakedLocation.getAccuracy();
                double altitude = fakedLocation.getAltitude();
                double longitude = fakedLocation.getLongitude();
                double latitude = fakedLocation.getLatitude();
                String provider = fakedLocation.getProvider();
                //Logger.log("Acc: " + accuracy + "\nAltitude: " + altitude + "\nLongitude: " + longitude + "\nLatitude: " + latitude + "\nProvider: " + provider);
                param.setResult(fakedLocation);
            }
        });
    }
}
