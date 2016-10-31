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

import com.marz.snapprefs.Databases.ChatsDatabaseHelper;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Obfuscator.chat;
import com.marz.snapprefs.Util.ChatData;
import com.marz.snapprefs.Util.NotificationUtils;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class Chat {
    public static HashSet<String> loadedMessages = new HashSet<>();
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("'['dd'th' MMMM yyyy']' - hh:mm", Locale.getDefault());
    private static ChatsDatabaseHelper chatDBHelper;

    private static String yourUsername;

    public static ChatsDatabaseHelper getChatDBHelper(Context context) {
        if (chatDBHelper == null)
            chatDBHelper = new ChatsDatabaseHelper(context);

        return chatDBHelper;
    }

    static void initTextSave(final XC_LoadPackage.LoadPackageParam lpparam, final Context snapContext) {
        ClassLoader cl = lpparam.classLoader;

        final Class chatClass = findClass(Obfuscator.chat.CHAT_CLASS, cl);
        findAndHookMethod(Obfuscator.chat.MESSAGEVIEWHOLDER_CLASS, lpparam.classLoader, "r", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                try {
                    Object chatLinker = getObjectField(param.thisObject, Obfuscator.chat.MESSAGEVIEWHOLDER_VAR1);

                    if (chatLinker == null)
                        return;

                    Object chat = getObjectField(chatLinker, Obfuscator.chat.MESSAGEVIEWHOLDER_VAR2);

                    if (chat == null)
                        return;


                    if (chatClass.isInstance(chat)) {
                        Boolean isSaved = (Boolean) callMethod(chat, Obfuscator.chat.MESSAGEVIEWHOLDER_ISSAVED);
                        Boolean isFailed = (Boolean) callMethod(chat, Obfuscator.chat.MESSAGEVIEWHOLDER_ISFAILED);

                        if (isSaved == null || isFailed == null) {
                            Logger.log("Null Chat Data [isSaved:%s] [isFailed:%s]", LogType.CHAT);
                            return;
                        }

                        if (!isSaved && !isFailed) {
                            Logger.log("Performed chat save", LogType.CHAT);
                            callMethod(param.thisObject, Obfuscator.chat.MESSAGEVIEWHOLDER_SAVE);
                        }
                    }
                } catch (Throwable t) {
                    Logger.log("Error saving chat message", t, LogType.CHAT);
                }
            }
        });
    }

    static void initChatLogging(final XC_LoadPackage.LoadPackageParam lpparam, final Context snapContext) {
        ClassLoader cl = lpparam.classLoader;

        yourUsername = HookMethods.getSCUsername(lpparam.classLoader);
        getChatDBHelper(snapContext);

        findAndHookMethod(chat.ABSTRACT_CONVERSATION_CLASS, cl, Obfuscator.chat.SENT_CHAT_METHOD, findClass(chat.CHAT_CLASS, cl), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                Object chatMessage = param.args[0];
                handleSentChatMessage(chatMessage);
            }
        });

        final Class chatDetailsClass = findClass(Obfuscator.chat.CHAT_MESSAGE_DETAILS_CLASS, cl);
        findAndHookMethod(Obfuscator.chat.SECURE_CHAT_SERVICE_CLASS, cl,
                chat.SCS_MESSAGE_METHOD, findClass(Obfuscator.chat.CHAT_MESSAGE_BASE_CLASS, cl), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        Object chatMessage = param.args[0];
                        String type = (String) getObjectField(chatMessage, "type");

                        if (chatDetailsClass.isInstance(chatMessage) && type.equals("chat_message"))
                            handleChatMessage(chatMessage);
                    }
                });
    }

    /*private static void fillListWithChatMessages(List<Object> messageList, String conversation_id, String blacklist) throws IllegalAccessException, InstantiationException {
        List<Object> newChatList = chatDBHelper.getAllChatsFromExcept(conversation_id, blacklist);

        if (newChatList == null)
            return;

        for (Object chatObj : newChatList) {
            ChatData chatData = (ChatData) chatObj;
            Logger.log("Injecting message: " + chatData.getMessageType());

            Object messageBodyObject = messageBodyClass.newInstance();

            if (chatData.getMessageType() == ChatData.MessageType.TEXT) {
                callMethod(messageBodyObject, "d", chatData.getText());
                callMethod(messageBodyObject, "a", "text");
            } else if (chatData.getMessageType() == ChatData.MessageType.MEDIA) {
                setObjectField(messageBodyObject, "type", "media");

                Object mediaObject = mediaClass.newInstance();
                setObjectField(mediaObject, "iv", chatData.getSnapIV());
                setObjectField(mediaObject, "key", chatData.getSnapKey());
                setObjectField(mediaObject, "mediaId", chatData.getMediaID());
                setObjectField(mediaObject, "mediaUrl", chatData.getMediaURL());
                setObjectField(mediaObject, "timerSec", chatData.getSnapDuration());
                setObjectField(mediaObject, "width", chatData.getSnapWidth());
                setObjectField(mediaObject, "height", chatData.getSnapHeight());

                setObjectField(messageBodyObject, "media", mediaObject);
            }

            HashMap<String, Object> savedMap = new HashMap<>();
            Object savedObject = savedClass.newInstance();
            callMethod(savedObject, "a", Boolean.TRUE);
            callMethod(savedObject, "a", 1);
            savedMap.put(yourUsername, savedObject);

            Object headerObject = headerClass.newInstance();
            callMethod(headerObject, "c", chatData.getConversationId());
            callMethod(headerObject, "a", chatData.getRecipients());
            callMethod(headerObject, "a", chatData.getSender());
            callMethod(headerObject, "a", Boolean.FALSE);

            Object messageObject = messageClass.newInstance();
            callMethod(messageObject, "a", messageBodyObject);
            callMethod(messageObject, "a", chatData.getMessageId());
            callMethod(messageObject, "a", savedMap);
            callMethod(messageObject, "a", headerObject);
            callMethod(messageObject, "d", chatData.getTimestamp());
            callMethod(messageObject, "c", chatData.getSeq_num());
            callMethod(messageObject, "i", "chat_message");
            callMethod(messageObject, "k", chatData.getUniqueId());

            Object snapObject = snapClass.newInstance();
            callMethod(snapObject, "b", messageObject);
            callMethod(snapObject, "a", chatData.getIter_token());

            messageList.add(snapObject);
            Logger.log("Injected message");
        }
    }*/

    private static void handleChatMessage(Object chatObj) {
        ChatData chatData = new ChatData();

        try {
            chatData.setMessageId((String) getObjectField(chatObj, "chatMessageId"));
            chatData.setTimestamp((Long) getObjectField(chatObj, "timestamp"));

            Object header = getObjectField(chatObj, "header");
            chatData.setConversationId((String) getObjectField(header, "convId"));
            chatData.setSender((String) getObjectField(header, "from"));

            Object body = getObjectField(chatObj, "body");
            chatData.setText((String) getObjectField(body, "text"));

            chatData.setFriendName(getFriendNameFromId(chatData.getConversationId()));

            chatDBHelper.insertChat(chatData);
        } catch (Exception e) {
            Logger.log("Error creating new chat message", e, LogType.CHAT);
        }
    }

    private static void handleSentChatMessage(Object chatObj) {
        ChatData chatData = new ChatData();

        try {
            chatData.setMessageId((String) callMethod(chatObj, "getId"));
            chatData.setText((String) callMethod(chatObj, "r"));
            chatData.setSender((String) getObjectField(chatObj, "am"));
            chatData.setConversationId((String) callMethod(chatObj, "M_"));
            chatData.setTimestamp((Long) callMethod(chatObj, "i"));
            chatData.setFriendName(getFriendNameFromId(chatData.getConversationId()));

            chatDBHelper.insertChat(chatData);
        } catch (Exception e) {
            Logger.log("Error creating new chat message", e, LogType.CHAT);
        }
    }

    private static String getFriendNameFromId(String conversationId) {
        String[] splitNames = conversationId.split("~");

        for (String name : splitNames) {
            if (!name.equals(yourUsername))
                return name.trim();
        }

        return null;
    }

    static void initImageSave(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        /**
         * We hook this method to get the ChatImage from the imageView of ImageResourceView,
         * then we get the properties and save the actual Image.
         */
        final Object[] chatMediaArr = new Object[1];
        findAndHookMethod("com.snapchat.android.ui.ImageResourceView", lpparam.classLoader, "setChatMedia", findClass(Obfuscator.chat.CHAT_MEDIA_CLASS, lpparam.classLoader), findClass("com.snapchat.android.ui.SnapchatResource.a", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                chatMediaArr[0] = param.args[0];
            }
        });
        findAndHookMethod(Obfuscator.chat.CHATLAYOUT_CLASS, lpparam.classLoader, Obfuscator.chat.CHATLAYOUT_INSTANTIATEITEM, ViewGroup.class, int.class, new XC_MethodHook() {
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
                        Logger.log("Button press on chat image detected");

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
        final Class<?> CenterCropTextureVideoView = findClass("com.snapchat.android.app.feature.messaging.chat.view2.ChatVideoFullScreenView", lpparam.classLoader);
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
                            } else if (response == Saving.SaveResponse.EXISTING) {
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
