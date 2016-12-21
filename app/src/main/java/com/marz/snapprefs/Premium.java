package com.marz.snapprefs;

import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences.Prefs;

import java.util.Map;
import java.util.concurrent.Executor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

public class Premium {
    static void initPremium(final XC_LoadPackage.LoadPackageParam lpparam) {
        final ClassLoader cl = lpparam.classLoader;

        final boolean blockPresence = Preferences.getBool(Prefs.HIDE_TYPING_AND_PRESENCE) && Preferences.getLicence() >= 1;
        final boolean stealthViewing = Preferences.getBool(Prefs.STEALTH_VIEWING) && Preferences.getLicence() >= 2;
        final boolean stealthSaving = Preferences.getBool(Prefs.STEALTH_CHAT_SAVING) && Preferences.getLicence() >= 2;

        if (blockPresence || stealthViewing || stealthSaving) {
            findAndHookMethod("GN", cl, "a", findClass("IM", cl), findClass("aKd", cl),
                    findClass("GJ", cl), new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object packet = param.args[1];
                            //Logger.log("GNPacket: " + packet.toString(), LogType.PREMIUM);
                            performPacketBlocking(lpparam, param, packet, blockPresence, stealthViewing, stealthSaving);
                        }
                    });

            findAndHookMethod("com.snapchat.android.util.chat.SecureChatService", cl, "a", findClass("aMj", cl), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object packet = param.args[0];
                    //Logger.log("aMjPacket: " + packet.toString(), LogType.PREMIUM);
                    performPacketBlocking(lpparam, param, packet, blockPresence, stealthViewing, stealthSaving);
                }
            });


            String strNetworkManager = "com.snapchat.android.app.shared.framework.network.manager.NetworkManager";
            final Class snapUpdateClass = findClass("Aa", cl);
            final Class chatTypingClass = findClass("JN", cl);

            findAndHookMethod(strNetworkManager, cl, "a", findClass("Qm", cl), findClass(strNetworkManager + "$a", cl),
                    Executor.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object packet = param.args[0];

                            if ((snapUpdateClass.isInstance(packet) && stealthViewing) ||
                                    (chatTypingClass.isInstance(packet) && blockPresence))
                                param.setResult(true);
                        }
                    });
        }
    }

    private static void performPacketBlocking(final XC_LoadPackage.LoadPackageParam lpparam, MethodHookParam param, Object packet,
                                              boolean blockPresence, boolean stealthViewing, boolean stealthSaving) {
        String type = (String) getObjectField(packet, "type");
        switch (type) {
            case "presence_v2":
                if (blockPresence) {
                    Logger.log("Performing presence block", LogType.PREMIUM);
                    initPresenceBlocking(lpparam.classLoader, packet);
                }
                //Logger.log("Presence: " + packet.toString(), LogType.PREMIUM);
                break;
            case "snap_state":
                boolean isReplayed = (boolean) getObjectField(packet, "replayed");

                if (stealthViewing && !isReplayed) {
                    Logger.log("Performing stealth view", LogType.PREMIUM);
                    Logger.log("Packet: " + packet.toString());
                    setObjectField(packet, "viewed", false);
                    setObjectField(packet, "timestamp", System.currentTimeMillis() - 200000);
                }
                //Logger.log("State: " + packet.toString(), LogType.PREMIUM);
                break;
            case "message_release":
                if (stealthViewing) {
                    Logger.log("Performing stealth release", LogType.PREMIUM);
                    Logger.log("Packet: " + packet.toString());
                    param.setResult(null);
                }
                //Logger.log("Release: " + packet.toString(), LogType.PREMIUM);
                break;
            case "message_state":
                if (stealthSaving) {
                    Logger.log("Performing stealth save", LogType.PREMIUM);
                    setObjectField(packet, "state", "unsaved");
                }
                //Logger.log("Message state: " + packet.toString(), LogType.PREMIUM);
                break;
            case "ping_response":
                param.setResult(null);
                break;
        }
    }


    private static void initPresenceBlocking(ClassLoader cl, Object presenceObj) {
        String yourUsername = HookMethods.getSCUsername(cl);
        Map<String, Boolean> presences = (Map<String, Boolean>) getObjectField(presenceObj, "presences");
        Boolean presenceState = presences.get(yourUsername);

        if (presenceState != null)
            presences.put(yourUsername, Boolean.FALSE);
    }
}
