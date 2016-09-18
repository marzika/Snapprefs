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

import com.marz.snapprefs.Util.NotificationUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
    static Class<?> chatClass = null;
    static HashMap<Integer, ChatData> hashChats = new HashMap<>();
    static String myUsername = null;
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("'['dd'th' MMMM yyyy']' - hh:mm", Locale.getDefault());

    static void initTextSave(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        chatClass = XposedHelpers.findClass(Obfuscator.chat.CHAT_CLASS, lpparam.classLoader);
        XposedHelpers.findAndHookMethod(Obfuscator.chat.MESSAGEVIEWHOLDER_CLASS, lpparam.classLoader, Obfuscator.chat.MESSAGEVIEWHOLDER_METHOD, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Object chat = XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, Obfuscator.chat.MESSAGEVIEWHOLDER_VAR1), Obfuscator.chat.MESSAGEVIEWHOLDER_VAR2);
                if (chat != null && chatClass.isInstance(chat)) {
                    if (!(boolean) XposedHelpers.callMethod(chat, Obfuscator.chat.MESSAGEVIEWHOLDER_ISSAVED) && !(boolean) XposedHelpers.callMethod(chat, Obfuscator.chat.MESSAGEVIEWHOLDER_ISFAILED)) {
                        try {
                            XposedHelpers.callMethod(param.thisObject, Obfuscator.chat.MESSAGEVIEWHOLDER_SAVE);
                        } catch (XposedHelpers.InvocationTargetError e) {
                            Logger.log("Unable to save chat text.", true);
                        }
                    }
                }
            }
        });

        ClassLoader cl = lpparam.classLoader;

        XposedHelpers
                .findAndHookConstructor(Obfuscator.chat.CONVERSATION_CLASS, lpparam.classLoader,
                        String.class, String.class, String.class, findClass("Vr", cl), findClass("akm", cl), findClass("akh", cl),
                        findClass("ahN", cl), findClass("com.snapchat.android.model.FriendManager", cl), findClass("com.squareup.otto.Bus", cl),
                        findClass("aiu", cl), findClass("GS", cl), findClass("So", cl), new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                if (myUsername == null)
                                    myUsername = (String) XposedHelpers.getObjectField(param.thisObject, "mMyUsername");
                            }
                        });


        XposedHelpers
                .findAndHookMethod(Obfuscator.chat.CONVERSATION_CLASS, lpparam.classLoader, "w", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //Logger.log("###grabbing all chats###");

                        ArrayList<Object> chatList = (ArrayList) param.getResult();

                        if (!Preferences.mChatAutoSave)
                            return;

                        for (Object obj : chatList)
                            handleChatFeedItem(obj);
                    }
                });
    }


    static void handleChatFeedItem(Object obj) {
        if (!obj.getClass().getName().equals("Wv"))
            return;

        String mId = (String) XposedHelpers.getObjectField(obj, "mId");
        int mIdHash = mId.hashCode();

        if (hashChats.containsKey(mIdHash)) {
            //Logger.log("Duplication blocked: " + ( mIdHash % 1000 ) );
            return;
        }

        boolean isSeen = (Boolean) XposedHelpers.callMethod(obj, "A");
        String strOtherUser;

        List<?> mRecipientList = (List) XposedHelpers.getObjectField(obj, "mRecipients");
        String mRecipient = (String) mRecipientList.get(0);
        String mSender = (String) XposedHelpers.getObjectField(obj, "mSender");

        boolean areYouTheSender = false;

        if (mRecipient.equals(myUsername))
            strOtherUser = mSender;
        else {
            strOtherUser = mRecipient;
            areYouTheSender = true;
        }

        boolean mIsSavedBySender =
                (boolean) XposedHelpers.getObjectField(obj, "mIsSavedBySender");
        boolean mIsSavedByRecipient =
                (boolean) XposedHelpers.getObjectField(obj, "mIsSavedByRecipient");

        //Logger.log("IsSeen: " + isSeen +  "Are you the sender? " + areYouTheSender + "from: " + mSender + " to " + mRecipient + " isSavedBySender: " + mIsSavedBySender + " isSaveByReci: " + mIsSavedByRecipient );

        if ((areYouTheSender && mIsSavedBySender) ||
                (!areYouTheSender && mIsSavedByRecipient))
            return;

        long mTimestamp = (long) XposedHelpers.getObjectField(obj, "mTimestamp");
        String mUserTest = (String) XposedHelpers.getObjectField(obj, "mUserText");

        ChatData chatData =
                new ChatData(mId, mRecipient, mSender, strOtherUser, mIsSavedBySender, mIsSavedByRecipient, mTimestamp, mUserTest);
        hashChats.put(mIdHash, chatData);

        Logger.log("Added chat from: " + mSender + " to " + mRecipient + " at " +
                dateFormat.format(mTimestamp));

        try {
            performChatSave(chatData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void performChatSave(ChatData chatData) throws IOException {
        String savePath = Preferences.mSavePath + "/ChatLogs/" + chatData.getmOtherUser() + ".txt";

        File outputFile = new File(savePath);

        if (outputFile.exists()) {
            try {
                if (scanForExistingHash(chatData, outputFile)) {
                    Logger.log("Failed scan checks");
                    return;
                }
            } catch (FileNotFoundException ignore) {
            }
        }

        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdir();
            outputFile.createNewFile();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(savePath, true));

        String strTimestamp = "[" + dateFormat.format(chatData.getmTimestamp()) + "]";

        writer.append(
                chatData.getHashMod(9999, true) + "# " + strTimestamp +
                        "\n>" + chatData.getmSender()
                        + "- " + chatData.getmUserText() + "\n");

        writer.close();
    }

    static boolean scanForExistingHash(ChatData chatData, File inputFile)
            throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String readLine;
        try {
            do {
                readLine = reader.readLine();

                if (readLine == null)
                    return false;

                String splitHash[] = readLine.split("#");

                if (splitHash.length > 0) {
                    String strHash = splitHash[0].trim();
                    String hashMod = Integer.toString(chatData.getHashMod(9999, true));

                    if (hashMod.equals(strHash))
                        return true;
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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
                            Saving.SaveResponse response = Saving.saveSnap(Saving.SnapType.CHAT, Saving.MediaType.IMAGE, imageView.getContext(), chatImage, null, filename, sender);
                            if (response == Saving.SaveResponse.SUCCESS) {
                                Logger.printFinalMessage("Saved Chat image");
                                Saving.createStatefulToast("Saved Chat image", NotificationUtils.ToastType.GOOD);
                            } else if (response == Saving.SaveResponse.EXISTING) {
                                Logger.printFinalMessage("Chat image exists");
                                Saving.createStatefulToast("Chat image exists", NotificationUtils.ToastType.WARNING);
                            } else if (response == Saving.SaveResponse.FAILED) {
                                Logger.printFinalMessage("Error saving Chat image");
                                Saving.createStatefulToast("Error saving Chat image", NotificationUtils.ToastType.BAD);
                            } else {
                                Logger.printFinalMessage("Unhandled save response");
                                Saving.createStatefulToast("Unhandled save response", NotificationUtils.ToastType.WARNING);
                            }
                        } catch (Exception e) {

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
                            Logger.log("Error while saving the Chat video:", true);
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
                            Saving.SaveResponse response = Saving.saveSnap(Saving.SnapType.CHAT, Saving.MediaType.VIDEO, (Context) param.args[0], null, video, filename, sender);
                            if (response == Saving.SaveResponse.SUCCESS) {
                                Logger.printFinalMessage("Saved Chat video");
                                Saving.createStatefulToast("Saved Chat video", NotificationUtils.ToastType.GOOD);
                            }else if (response == Saving.SaveResponse.EXISTING) {
                                    Logger.printFinalMessage("Chat video exists");
                                    Saving.createStatefulToast("Chat video exists", NotificationUtils.ToastType.WARNING);
                            } else if (response == Saving.SaveResponse.FAILED) {
                                Logger.printFinalMessage("Error saving Chat video");
                                Saving.createStatefulToast("Error saving Chat video", NotificationUtils.ToastType.BAD);
                            } else {
                                Logger.printFinalMessage("Unhandled save response");
                                Saving.createStatefulToast("Unhandled save response", NotificationUtils.ToastType.WARNING);
                            }
                        } catch (Exception e) {

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
