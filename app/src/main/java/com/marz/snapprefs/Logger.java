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

import android.util.Log;

import com.marz.snapprefs.Util.StringUtils;

import de.robv.android.xposed.XposedBridge;

public class Logger {

    public static final String LOG_TAG = "SnapPrefs: ";
    private static int printWidth = 70;

    /**
     * Restrict instantiation of this class, it only contains static methods.
     */
    private Logger() {
    }


    /**
     * Write debug information to the Xposed Log if enabled or forced by the parameter.
     *
     * @param message The message you want to log
     * @param prefix  Whether it should be prefixed by the log-tag
     * @param forced  Whether to force log and thus overrides the debug setting
     */
    public static void log(String message, boolean prefix, boolean forced) {
        try {
            if (!Preferences.getBool(Preferences.Prefs.DEBUGGING) && !forced)
                return;
        } catch( Throwable t)
        {
            Log.d("SNAPPREFS", "Tried to log before fully loaded: ["  + message + "]");
            return;
        }

        if (prefix) {
            message = LOG_TAG + message;
        }

        try {
            XposedBridge.log(message);
        } catch( Throwable e)
        {
            Log.d("SNAPPREFS", message);
        }
    }

    public static void afterHook( String message )
    {
        log( "AfterHook: " + message );
    }

    public static void beforeHook( String message )
    {
        log( "BeforeHook: " + message );
    }

    public static void printTitle( String message )
    {
        log( "" );
        printFilledRow();
        printMessage( message );
        printFilledRow();
    }
    public static void printMessage( String message )
    {
        log( "#" + StringUtils.center( message, printWidth ) + "#" );
    }

    public static void printFinalMessage( String message )
    {
        printMessage( message );
        printFilledRow();
    }

    public static void printFilledRow()
    {
        log( StringUtils.repeat( "#", printWidth + 2 ) );
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
        try {
            XposedBridge.log(throwable);
        } catch( Throwable t )
        {
            Log.e("SNAPPREFS", "Throwable: " + t.getMessage());
            t.printStackTrace();
        }
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

    public static void logStackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        for(StackTraceElement traceElement : stackTraceElements)
            Logger.log("Stack trace: [Class: " + traceElement.getClassName() + "] [Method: " + traceElement.getMethodName() + "]");
    }
}
