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

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marz.snapprefs.Util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import de.robv.android.xposed.XposedBridge;

/**
 * The latest part of this Logging System was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class Logger {

    private static final String LOG_TAG = "Snapprefs: ";
    private static int printWidth = 70;
    private static boolean defaultForced = false;
    private static boolean defaultPrefix = true;

    private static boolean loggingEnabled = true;
    private static boolean hasLoaded = false;
    private static HashSet<String> logTypes = new HashSet<>();

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

        } catch (Throwable t) {
            Log.d("SNAPPREFS", "Tried to log before fully loaded: [" + message + "]");
            return;
        }

        if (prefix) {
            message = LOG_TAG + message;
        }

        try {
            XposedBridge.log(message);
        } catch (Throwable e) {
            Log.d("snapprefs", message);
        }
    }

    /**
     * Prints a title in a line width of at least {@link #printWidth} with areas before and after filled with '#'s
     *
     * @param message The message to print in the title
     */
    public static void printTitle(String message, LogType logType) {
        logType.removeTag();
        log("", logType);
        printFilledRow(logType);
        printMessage(message, logType);
        printFilledRow(logType);
    }

    /**
     * Prints a message with left and right aligned '#'s, to be used with {@link #printTitle(String, LogType)} and {@link #printFilledRow(LogType)}
     *
     * @param message The message to print between the '#'s
     */
    public static void printMessage(String message, LogType logType) {
        log("#" + StringUtils.center(message, printWidth) + "#", logType.removeTag());
    }

    /**
     * Prints a message using @printMessage and then prints a filled row with @printFilledRow
     *
     * @param message The final message that is going to be printed
     */
    static void printFinalMessage(String message, LogType logType) {
        logType.removeTag();
        printMessage(message, logType);
        printFilledRow(logType);
    }

    /**
     * Print a '#' Filled row of width {@link #printWidth}
     */
    static void printFilledRow(LogType logType) {
        log(StringUtils.repeat("#", printWidth + 2), logType.removeTag());
    }

    /**
     * Write debug information to the Xposed Log if enabled.
     *
     * @param message The message you want to log
     * @param prefix  Whether it should be prefixed by the log-tag
     */
    public static void log(String message, boolean prefix) {
        log(message, prefix, defaultForced);
    }

    /**
     * Write a throwable to the Xposed Log, even when debugging is disabled.
     *
     * @param throwable The throwable to log
     */
    public static void log(Throwable throwable) {
        try {
            XposedBridge.log(throwable);
        } catch (Throwable t) {
            Log.e("SNAPPREFS", "Throwable: " + throwable.getMessage());
            throwable.printStackTrace();
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

    /**
     * Logs the current stack trace(ie. the chain of calls to get where you are now)
     */
    static void logStackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        for (StackTraceElement traceElement : stackTraceElements)
            Logger.log("Stack trace: [Class: " + traceElement.getClassName() + "] [Method: " + traceElement.getMethodName() + "]", defaultPrefix, defaultForced);
    }

    public static void disableLogging() {
        loggingEnabled = false;
    }

    public static void enableLogging() {
        loggingEnabled = true;
    }

    public static void log(String message, Throwable throwable, LogType logType) {
        log(message, logType.setForced());
        log(throwable);
    }

    public static void log(String message) {
        log(message, LogType.DEBUG);
    }

    public static void log(String message, @Nullable LogType logType) {
        if (hasLoaded &&
                (!loggingEnabled || !Preferences.getBool(Preferences.Prefs.DEBUGGING) &&
                        logType != null && !logType.isForced()))
            return;

        if (logType == null) {
            assignPrefixAndPrint(message);
            return;
        }

        if (logType.isForced() || logTypes.contains(logType.name())) {
            String outputMsg = (logType.showTag ? logType.tag + " " : "" ) + message;

            assignPrefixAndPrint(outputMsg);

            if (logType.tempForce)
                logType.tempForce = false;
        }
    }

    private static void assignPrefixAndPrint(String message) {
        if (defaultPrefix)
            message = LOG_TAG + message;

        try {
            XposedBridge.log(message);
        } catch (Throwable t) {
            Log.d("Snapprefs", message);
        }
    }

    @SuppressWarnings("unchecked")
    static void loadSelectedLogTypes() {
        File logTypeFile = new File(Preferences.getContentPath(), "LogTypes.json");
        log("Performing LogType load", LogType.FORCED);

        if (!logTypeFile.exists()) {
            loadDefaultLogTypes();
            return;
        }

        Gson gson = new Gson();
        FileReader reader = null;

        try {
            reader = new FileReader(logTypeFile);
            logTypes = gson.fromJson(reader, HashSet.class);
            hasLoaded = true;
            log(String.format("Loaded %s log types", logTypes.toString()), LogType.FORCED);
        } catch (FileNotFoundException e) {
            log("LogType list file not found", LogType.FORCED);
            loadDefaultLogTypes();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void saveSelectedLogTypes() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        File logTypeFile = new File(Preferences.getContentPath(), "/LogTypes.json");

        try {
            logTypeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(logTypeFile);
            gson.toJson(logTypes, writer);
            log(String.format("Saved %s log types", logTypes.toString()), LogType.FORCED);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void loadDefaultLogTypes() {
        log("Loading default LogTypes", LogType.FORCED);

        for (LogType type : LogType.values())
            logTypes.add(type.name());

        hasLoaded = true;
        saveSelectedLogTypes();
    }

    public static void setLogTypeState(String type, boolean state) {
        if (!state) {
            if (logTypes.remove(type)) {
                saveSelectedLogTypes();
                Logger.log("Successfully disabled LogType " + type, LogType.DEBUG);
            }
            else
                Logger.log("Couldn't disable LogType " + type, LogType.DEBUG);
        } else {
            if (logTypeContainsName(type) && logTypes.add(type)) {
                saveSelectedLogTypes();
                Logger.log("Successfully enabled LogType " + type, LogType.DEBUG);
            }
            else
                Logger.log("Couldn't enable LogType " + type, LogType.DEBUG);
        }
    }

    private static boolean logTypeContainsName(String name) {
        for (LogType type : LogType.values()) {
            if (type.name().equals(name))
                return true;
        }

        return false;
    }

    public static HashSet<String> getActiveLogTypes() {
        return logTypes;
    }

    public enum LogType {
        DEBUG("Debug"),
        CHAT("Chat"),
        LENS("Lens"),
        GROUPS("Groups"),
        DATABASE("Database"),
        SAVING("Saving"),
        PREFS("Prefs"),
        FILTER("Filter"),
        PREMIUM("Premium"),
        FORCED("Forced", true);

        public String tag;
        private boolean isForced = false;
        private boolean tempForce = false;
        private boolean showTag = true;

        LogType(String tag) {
            this.tag = String.format("[%s]", tag);
        }

        LogType(String tag, boolean isForced) {
            this(tag);
            this.isForced = isForced;
        }

        public LogType setForced() {
            this.tempForce = true;
            return this;
        }

        public LogType removeTag() {
            this.showTag = false;
            return this;
        }

        public boolean isForced() {
            return this.tempForce || this.isForced;
        }
    }
}
