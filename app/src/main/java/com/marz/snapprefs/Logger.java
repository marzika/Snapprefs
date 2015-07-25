/*
 * Copyright (C) 2014  Sturmen, stammler, Ramis and P1nGu1n
 *
 * This file is part of Keepchat.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.marz.snapprefs;

import de.robv.android.xposed.XposedBridge;

public class Logger {

    public static final String LOG_TAG = "SnapPrefs: ";
    private static boolean debugging;

    /**
     * Restrict instantiation of this class, it only contains static methods.
     */
    private Logger() {
    }

    /**
     * Enable or disable writing debug information to the Xposed Log.
     *
     * @param debug Whether to enable or disable it
     */
    public static void setDebuggingEnabled(boolean debug) {
        debugging = debug;
    }

    /**
     * Write debug information to the Xposed Log if enabled or forced by the parameter.
     *
     * @param message The message you want to log
     * @param prefix  Whether it should be prefixed by the log-tag
     * @param forced  Whether to force log and thus overrides the debug setting
     */
    public static void log(String message, boolean prefix, boolean forced) {
        if (debugging || forced) {
            if (prefix) {
                message = LOG_TAG + message;
            }
            XposedBridge.log(message);
        }
    }

    /**
     * Write debug information to the Xposed Log if enabled.
     *
     * @param message The message you want to log
     * @param prefix  Whether it should be prefixed by the log-tag
     */
    public static void log(String message, boolean prefix) {
        log(message, prefix, false);
    }

    /**
     * Write debug information to the Xposed Log if enabled.
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
        log(message, true, true);
        log(throwable);
    }
}
