package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;
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
        findAndHookMethod(Obfuscator.chat.CHATLAYOUT_CLASS, lpparam.classLoader, "a", ViewGroup.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
               List chatMediaList = (List) getObjectField(param.thisObject, "a");
               chatMediaArr[0] = chatMediaList.get((int) param.args[1]);
            }
        });
        final Class<?> PhotoViewClass = findClass("uk.co.senab.photoview.PhotoView", lpparam.classLoader);
        hookAllConstructors(PhotoViewClass, new XC_MethodHook() {
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
                        Object chatMedia = chatMediaArr[0];
                        Logger.log("We have the chatMedia", true);
                        Long timestamp = (Long) callMethod(chatMedia, Obfuscator.save.CHAT_GETTIMESTAMP); // model.chat.Chat
                        Logger.log("We have the timestamp " + timestamp.toString(), true);
                        String sender = (String) callMethod(chatMedia, Obfuscator.save.STATEFULCHATFEEDITEM_GETSENDER); //in StatefulChatFeedItem
                        Logger.log("We have the sender " + sender, true);
                        String filename = sender + "_" + dateFormat.format(timestamp);
                        Logger.log("We have the file name " + filename, true);

                        try {
                            Saving.saveSnap( Saving.SnapType.CHAT, Saving.MediaType.IMAGE, imageView.getContext(), chatImage, null, filename, sender );
                        } catch( Exception e )
                        {

                        }
                        return true;
                    }
                });
            }
        });
        final Class<?> TextureVideoView = findClass("com.snapchat.opera.shared.view.TextureVideoView", lpparam.classLoader);
        final Class<?> CenterCropTextureVideoView = findClass("com.snapchat.android.ui.chat.ChatVideoFullScreenView", lpparam.classLoader);
        hookAllConstructors(CenterCropTextureVideoView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                final FrameLayout frameLayout = (FrameLayout) param.thisObject;
                RelativeLayout saveLayoutSnap = new RelativeLayout(HookMethods.SnapContext);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
                saveLayoutSnap.setPadding(0, 0, HookMethods.px(5), HookMethods.px(30));
                ImageButton saveBtnSnap = new ImageButton((Context) param.args[0]);
                saveBtnSnap.setBackgroundColor(0);
                Drawable saveImg = HookMethods.SnapContext.getResources().getDrawable(+(int) Long.parseLong(Obfuscator.save.STORIES_MYOVERLAYSAVE_ICON.substring(2), 16)); //stories_mystoryoverlaysave_icon
                saveBtnSnap.setImageDrawable(saveImg);
                saveBtnSnap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.log("----------------------- SNAPPREFS ------------------------", false);
                        Logger.log("Button press on chat video detected");
                        Object textureVideoView = getObjectField(param.thisObject, "b");
                        Uri mUri = null;
                        FileInputStream video = null;
                        try {
                            Field mUriField = TextureVideoView.getDeclaredField("b");
                            mUriField.setAccessible(true);
                            mUri = (Uri) mUriField.get(textureVideoView);
                            Logger.log("We have the chat video url: " + mUri.toString(), true);
                            video = new FileInputStream(Uri.parse(mUri.toString()).getPath());
                        } catch (Exception e) {
                            Logger.log("Error while saving the Chat image:",true);
                            e.printStackTrace();
                        }
                        Object chatMedia = chatMediaArr[0];
                        Logger.log("We have the chatMedia", true);
                        Long timestamp = (Long) callMethod(chatMedia, Obfuscator.save.CHAT_GETTIMESTAMP); // model.chat.Chat
                        Logger.log("We have the timestamp " + timestamp.toString(), true);
                        String sender = (String) callMethod(chatMedia, Obfuscator.save.STATEFULCHATFEEDITEM_GETSENDER); //in StatefulChatFeedItem
                        Logger.log("We have the sender " + sender, true);
                        String filename = sender + "_" + dateFormat.format(timestamp);
                        Logger.log("We have the file name " + filename, true);

                        try {
                            Saving.saveSnap( Saving.SnapType.CHAT, Saving.MediaType.VIDEO, (Context) param.args[ 0 ], null, video, filename, sender );
                        } catch( Exception e )
                        {

                        }
                    }
                });
                saveLayoutSnap.addView(saveBtnSnap);
                frameLayout.addView(saveLayoutSnap, layoutParams);
                saveBtnSnap.setVisibility(View.VISIBLE);
                saveLayoutSnap.setVisibility(View.VISIBLE);
                saveLayoutSnap.bringToFront();
            }
        });

    }
}
