package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.marz.snapprefs.Databases.ChatsDatabaseHelper;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Obfuscator.chat;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.ChatData;
import com.marz.snapprefs.Util.NotificationUtils;
import com.marz.snapprefs.Util.NotificationUtils.ToastType;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.marz.snapprefs.Util.StringUtils.obfus;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class Chat {
    public static HashSet<String> loadedMessages = new HashSet<>();
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("'['dd'th' MMMM yyyy']' - hh:mm", Locale.getDefault());
    private static SimpleDateFormat savingDateFormat =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault());
    private static ChatsDatabaseHelper chatDBHelper;
    private static HashMap<String, Object> chatMediaMap = new HashMap<>();

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
            //Logger.log("Error creating new chat message", e, LogType.CHAT);
            Logger.log("Error creating new chat message", LogType.CHAT);
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

        if (splitNames.length <= 0)
            return null;

        for (String name : splitNames) {
            if (!name.equals(yourUsername))
                return name.trim();
        }

        return null;
    }

    static void initImageSave(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        findAndHookMethod("JZ", lpparam.classLoader, "a", List.class, findClass("com.snapchat.android.app.feature.messaging.chat.model2.ChatMedia", lpparam.classLoader),
                List.class, View.class, findClass("Ka$b", lpparam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        if (!Preferences.getBool(Prefs.CHAT_MEDIA_SAVE))
                            return;

                        Logger.log("Getting Chat Video Media Key", LogType.CHAT);

                        Object chatMedia = param.args[1];
                        String mKey = (String) getObjectField(chatMedia, "H");

                        if (mKey == null) {
                            Logger.log("No mKey found!", LogType.CHAT);
                            return;
                        }

                        Logger.log("Found mKey: " + mKey, LogType.CHAT);

                        if (!chatMediaMap.containsKey(mKey))
                            chatMediaMap.put(mKey, chatMedia);
                        else
                            Logger.log("Map already contains key", LogType.CHAT);
                    }
                });

        findAndHookMethod("aFj", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                if (!Preferences.getBool(Prefs.CHAT_MEDIA_SAVE))
                    return;

                Logger.log("Finding Video URL from key", LogType.CHAT);

                Object godPacket = getObjectField(param.thisObject, "e");
                Map<String, Object> map = (Map<String, Object>) getObjectField(godPacket, "c");

                String mKey = (String) map.get("image_key");
                String mMediaUrl = (String) map.get("video_uri");

                String[] arrSplitUrl = mMediaUrl.split("media_cache/");

                if( arrSplitUrl.length <= 0 ) {
                    Logger.log("Malformed Video URL", LogType.CHAT);
                    return;
                }

                String splitUrl = arrSplitUrl[1];

                Logger.log(String.format("Checking for [MKey:%s] with [URL:%s]", mKey, splitUrl), LogType.CHAT);

                Object chatMedia = chatMediaMap.get(mKey);

                if (chatMedia == null) {
                    Logger.log("No mKey found in map", LogType.CHAT);
                    return;
                }

                chatMediaMap.remove(mKey);
                chatMediaMap.put(splitUrl, chatMedia);
                Logger.log("Assigned ChatMedia with key: " + splitUrl, LogType.CHAT);
            }
        });

        findAndHookMethod("com.snapchat.opera.view.basics.RotateLayout", lpparam.classLoader, "onTouchEvent",
                MotionEvent.class, new XC_MethodHook() {
                    private GestureDetector gestureDetector;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        if (!Preferences.getBool(Prefs.CHAT_MEDIA_SAVE))
                            return;

                        MotionEvent event = (MotionEvent) param.args[0];
                        ViewGroup viewGroup = (ViewGroup) param.thisObject;

                        if (gestureDetector == null) {
                            gestureDetector = new GestureDetector(new MediaGestureListener((ViewGroup) param.thisObject) {
                                @Override
                                public void onLongPress(MotionEvent e) {
                                    Logger.log("Video long press detected!", LogType.CHAT);

                                    try {
                                        int childCount = this.mediaLayout.getChildCount();

                                        if (childCount > 0) {
                                            FrameLayout videoLayout = (FrameLayout) this.mediaLayout.getChildAt(0);

                                            for (int i = 0; i < videoLayout.getChildCount(); i++) {
                                                View view = videoLayout.getChildAt(i);

                                                if (view.getId() != +2131690096)
                                                    return;

                                                Logger.log("Found TexturedVideoView", LogType.CHAT);

                                                Uri videoUri = (Uri) getObjectField(view, "b");

                                                if (videoUri == null) {
                                                    Logger.log("Null Video URI", LogType.CHAT);
                                                    return;
                                                }

                                                String strVideoUrl = videoUri.getPath();
                                                String[] arrSplitUrl = strVideoUrl.split("media_cache/");

                                                if( arrSplitUrl.length <= 0 ) {
                                                    Logger.log("Split url is malformed", LogType.CHAT);
                                                    return;
                                                }

                                                String splitUrl = arrSplitUrl[1];
                                                Logger.log("CachedFilename: " + splitUrl);
                                                Object chatMedia = chatMediaMap.get(splitUrl);

                                                if (chatMedia == null) {
                                                    Logger.log("No ChatMedia found for URL: " + splitUrl, LogType.CHAT);
                                                    return;
                                                }

                                                Logger.log("Found ChatMedia for saving", LogType.CHAT);
                                                Long timestamp = (Long) callMethod(chatMedia, "i"); // model.chat.Chat
                                                Logger.log("We have the timestamp " + timestamp, LogType.CHAT);
                                                String sender = (String) getObjectField(chatMedia, "am");
                                                Logger.log("We have the sender " + sender, LogType.CHAT);
                                                String formattedTimestamp = savingDateFormat.format(timestamp);
                                                String mId = (String) getObjectField(chatMedia, "j");
                                                String filename = String.format("%s_%s%s", sender, formattedTimestamp, mId.hashCode() % 999999);
                                                Logger.log("We have the file name " + obfus(sender) + "_" + formattedTimestamp, LogType.CHAT);

                                                FileInputStream video = new FileInputStream(videoUri.getPath());
                                                Saving.SaveResponse response = Saving.saveSnap(Saving.SnapType.CHAT, Saving.MediaType.VIDEO, view.getContext(), null, video, filename, sender);
                                                if (response == Saving.SaveResponse.SUCCESS) {
                                                    Logger.log("Saved Chat Video", LogType.CHAT);
                                                    Saving.createStatefulToast("Saved Chat Video", NotificationUtils.ToastType.GOOD);
                                                } else if (response == Saving.SaveResponse.EXISTING) {
                                                    Logger.log("Chat Video exists", LogType.CHAT);
                                                    Saving.createStatefulToast("Chat Video exists", NotificationUtils.ToastType.WARNING);
                                                } else if (response == Saving.SaveResponse.FAILED) {
                                                    Logger.log("Error saving Chat Video", LogType.CHAT);
                                                    Saving.createStatefulToast("Error saving Chat Video", NotificationUtils.ToastType.BAD);
                                                } else {
                                                    Logger.log("Unhandled save response", LogType.CHAT);
                                                    Saving.createStatefulToast("Unhandled save response", NotificationUtils.ToastType.WARNING);
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        Logger.log("Problems saving video!", ex, LogType.CHAT.setForced());
                                        Saving.createStatefulToast("Problem saving video!", ToastType.BAD);
                                    }
                                }
                            });
                        }

                        if (gestureDetector.onTouchEvent(event))
                            return;

                        if (event.getAction() != MotionEvent.ACTION_UP)
                            param.setResult(true);
                    }
                });

        findAndHookConstructor("aEU", lpparam.classLoader, findClass("aEE", lpparam.classLoader),
                findClass("amw", lpparam.classLoader), Context.class, findClass("uk.co.senab.photoview.PhotoView", lpparam.classLoader),
                findClass("aEm", lpparam.classLoader), findClass("aEM", lpparam.classLoader),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        if (!Preferences.getBool(Prefs.CHAT_MEDIA_SAVE))
                            return;

                        final ImageView imageView = (ImageView) getObjectField(param.thisObject, "n");

                        imageView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                try {
                                    Logger.log("Image long press detected", LogType.CHAT);

                                    Bitmap chatImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                                    if (chatImage == null) {
                                        Logger.log("Null chat image", LogType.CHAT);
                                        return true;
                                    }

                                    final Object godPacket = getObjectField(param.thisObject, "e");
                                    final Map<String, Object> map = (Map<String, Object>) getObjectField(godPacket, "c");

                                    if (map == null) {
                                        Logger.log("Null packet map", LogType.CHAT);
                                        return true;
                                    }

                                    final String mKey = (String) map.get("image_key");

                                    if (mKey == null) {
                                        Logger.log("Null image mKey", LogType.CHAT);
                                        return true;
                                    }

                                    Logger.log("Finding ChatMedia with key: " + mKey, LogType.CHAT);

                                    final Object chatMedia = chatMediaMap.get(mKey);

                                    if (chatMedia == null) {
                                        Logger.log("Couldn't find ChatMedia", LogType.CHAT);
                                        return true;
                                    }

                                    Long timestamp = (Long) callMethod(chatMedia, "i"); // model.chat.Chat
                                    Logger.log("We have the timestamp " + timestamp, LogType.CHAT);
                                    String sender = (String) getObjectField(chatMedia, "am");
                                    Logger.log("We have the sender " + obfus(sender), LogType.CHAT);
                                    String formattedTimestamp = savingDateFormat.format(timestamp);
                                    String mId = (String) getObjectField(chatMedia, "j");
                                    String filename = String.format("%s_%s%s", sender, formattedTimestamp, mId.hashCode() % 999999);
                                    Logger.log("We have the file name " + obfus(sender) + "_" + formattedTimestamp, LogType.CHAT);

                                    Saving.SaveResponse response = Saving.saveSnap(Saving.SnapType.CHAT, Saving.MediaType.IMAGE, imageView.getContext(), chatImage, null, filename, sender);
                                    if (response == Saving.SaveResponse.SUCCESS) {
                                        Logger.log("Saved Chat image", LogType.CHAT);
                                        Saving.createStatefulToast("Saved Chat image", NotificationUtils.ToastType.GOOD);
                                    } else if (response == Saving.SaveResponse.EXISTING) {
                                        Logger.log("Chat image exists", LogType.CHAT);
                                        Saving.createStatefulToast("Chat image exists", NotificationUtils.ToastType.WARNING);
                                    } else if (response == Saving.SaveResponse.FAILED) {
                                        Logger.log("Error saving Chat image", LogType.CHAT);
                                        Saving.createStatefulToast("Error saving Chat image", NotificationUtils.ToastType.BAD);
                                    } else {
                                        Logger.log("Unhandled save response", LogType.CHAT);
                                        Saving.createStatefulToast("Unhandled save response", NotificationUtils.ToastType.WARNING);
                                    }

                                    return false;
                                } catch (Exception e) {
                                    Logger.log("Exception saving chat image!", e, LogType.CHAT.setForced());
                                    Saving.createStatefulToast("Exception saving Chat image", NotificationUtils.ToastType.BAD);

                                    return true;
                                }
                            }
                        });
                    }
                });
    }

    private static class MediaGestureListener extends GestureDetector.SimpleOnGestureListener {
        ViewGroup mediaLayout;

        MediaGestureListener(ViewGroup mediaLayout) {
            this.mediaLayout = mediaLayout;
        }
    }
}
