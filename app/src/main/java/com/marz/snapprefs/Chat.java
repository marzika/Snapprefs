package com.marz.snapprefs;

import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class Chat {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault());
    static Class<?> chatClass = null;
    static void initTextSave(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        chatClass = XposedHelpers.findClass(Obfuscator.chat.CHAT_CLASS, lpparam.classLoader);
        XposedHelpers.findAndHookMethod(Obfuscator.chat.MESSAGEVIEWHOLDER_CLASS, lpparam.classLoader, Obfuscator.chat.MESSAGEVIEWHOLDER_METHOD, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Object chat = XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, Obfuscator.chat.MESSAGEVIEWHOLDER_VAR1), Obfuscator.chat.MESSAGEVIEWHOLDER_VAR2);
                if (chat != null && chatClass.isInstance(chat)) {
                    if (!(boolean)XposedHelpers.callMethod(chat, Obfuscator.chat.MESSAGEVIEWHOLDER_ISSAVED)) {
                        XposedHelpers.callMethod(param.thisObject, Obfuscator.chat.MESSAGEVIEWHOLDER_SAVE);
                    }
                }
            }
        });
    }

    static void initImageSave(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        /**
         * We hook this method to get the ChatImage from the imageView of ImageResourceView,
         * then we get the properties and save the actual Image.
         */
        final Object[] chatMediaArr = new Object[1];
        findAndHookMethod("com.snapchat.android.ui.ImageResourceView", lpparam.classLoader, "setChatMedia", findClass("com.snapchat.android.model.chat.ChatMedia", lpparam.classLoader), findClass("com.snapchat.android.ui.SnapchatResource.a", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                chatMediaArr[0] = param.args[0];
            }
        });
        final Class<?> imageResourceViewClass = findClass(Obfuscator.save.IMAGERESOURCEVIEW_CLASS, lpparam.classLoader);
        hookAllConstructors(imageResourceViewClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                final ImageView imageView = (ImageView) param.thisObject;
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Logger.log("----------------------- SNAPPREFS ------------------------", false);
                        Logger.log("Long press on chat image detected");

                        Bitmap chatImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        Logger.log("We have the chat image", true);
                        Object imageResource = getObjectField(param.thisObject, Obfuscator.save.IMAGERESOURCEVIEW_VAR_IMAGERESOURCE);
                        Logger.log("We have the imageResource", true);
                        //Object chatMedia = getObjectField(imageResource, Obfuscator.save.IMAGERESOURCE_VAR_CHATMEDIA); // in ImageResource
                        Object chatMedia = chatMediaArr[0];
                        Logger.log("We have the chatMedia", true);
                        Long timestamp = (Long) callMethod(chatMedia, Obfuscator.save.CHAT_GETTIMESTAMP); // model.chat.Chat
                        //Long timestamp = 0L;
                        Logger.log("We have the timestamp " + timestamp.toString(), true);
                        String sender = (String) callMethod(chatMedia, Obfuscator.save.STATEFULCHATFEEDITEM_GETSENDER); //in StatefulChatFeedItem
                        Logger.log("We have the sender " + sender, true);
                        String filename = sender + "_" + dateFormat.format(timestamp);
                        Logger.log("We have the file name " + filename, true);

                        Saving.saveSnap(Saving.SnapType.CHAT, Saving.MediaType.IMAGE, imageView.getContext(), chatImage, null, filename, sender);
                        return true;
                    }
                });
            }
        });

    }
}
