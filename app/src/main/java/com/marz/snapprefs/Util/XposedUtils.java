package com.marz.snapprefs.Util;

/**
 * XposedUtils.java created on 2014-01-14.
 * <p/>
 * Copyright (C) 2014 P1nGu1n
 * <p/>
 * This file is part of Snapshare.
 * <p/>
 * Snapshare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Snapshare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * a gazillion times. If not, see <http://www.gnu.org/licenses/>.
 */

import com.marz.snapprefs.Common;
import com.marz.snapprefs.HookMethods;
import com.marz.snapprefs.Obfuscator;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.newInstance;

/**
 * A set of commonly used utilities using the Xposed Framework.
 */
public class XposedUtils {
    private static XSharedPreferences preferences;

    /**
     * Restrict instantiation of this class, it only contains static methods.
     */
    private XposedUtils() {
    }

    /**
     * Refreshes preferences

     public static void refreshPreferences() {
     //preferences.reload();
     Common.ROTATION_MODE = Integer.parseInt(preferences.getString("pref_rotation", Integer.toString(Common.ROTATION_MODE)));
     Common.ADJUST_METHOD = Integer.parseInt(preferences.getString("pref_adjustment", Integer.toString(Common.ADJUST_METHOD)));
     Common.CAPTION_UNLIMITED_VANILLA = preferences.getBoolean("pref_caption_unlimited_vanilla", Common.CAPTION_UNLIMITED_VANILLA);
     Common.CAPTION_UNLIMITED_FAT = preferences.getBoolean("pref_caption_unlimited_fat", Common.CAPTION_UNLIMITED_FAT);
     Common.DEBUGGING = preferences.getBoolean("pref_debug", Common.DEBUGGING);
     Common.CHECK_SIZE = !preferences.getBoolean("pref_size_disabled", !Common.CHECK_SIZE);
     Common.TIMBER = preferences.getBoolean("pref_timber", Common.TIMBER);
     } **/


    /**
     * Write debug information to the Xposed Log if enabled in the settings
     *
     * @param message The message you want to log
     * @param prefix  Whether it should be prefixed by the log-tag
     */
    public static void log(String message, boolean prefix) {
        if (Common.DEBUGGING) {
            if (prefix) {
                message = Common.LOG_TAG + message;
            }
            XposedBridge.log(message);
        }
    }

    /**
     * Write debug information to the Xposed Log if enabled in the settings
     * This method always prefixes the message by the log-tag
     *
     * @param message The message you want to log
     */
    public static void log(String message) {
        log(message, true);
    }

    /**
     * Write a throwable to the Xposed Log, even when debugging is disabled.
     *
     * @param throwable The throwable to log
     */
    public static void log(Throwable throwable) {
        XposedBridge.log(throwable);
    }

    /**
     * Write a throwable with a message to the Xposed Log, even when debugging is disabled.
     *
     * @param message   The message to log
     * @param throwable The throwable to log after the message
     */
    public static void log(String message, Throwable throwable) {
        log(message);
        log(throwable);
    }
    /**
     * Sends a class to Bus.post()
     *
     * @param updateEvent   class to post -> newInstance(findClass("CLASSNAME", CLASSLOADER));
     */
    public static void sendToBus(Object updateEvent) {
        Object bus = callStaticMethod(findClass(Obfuscator.bus.GETBUS_CLASS, HookMethods.classLoader), Obfuscator.bus.GETBUS_METHOD);
        callMethod(bus, Obfuscator.bus.BUS_POST, updateEvent);
    }
}
