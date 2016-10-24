package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Util.ChatData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andre on 20/10/2016.
 */

public class ChatsDatabaseHelper extends CoreDatabaseHandler {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = Preferences.getContentPath() + "/ChatMessages.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String[] SQL_CREATE_ENTRIES = {
            "CREATE TABLE " + ChatEntry.TABLE_NAME + " (" +
                    ChatEntry.COLUMN_NAME_UNIQUE_ID + TEXT_TYPE + " PRIMARY KEY," +
                    ChatEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_MEDIA_TYPE + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_MESSAGE_ID + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_CONVERSATION_ID + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_MESSAGE_TEXT + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SENDER + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_RECIPIENT_GSON + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_TIMESTAMP + INT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_ITER_TOKEN + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SEQ_NUM + INT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SNAP_WIDTH + INT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SNAP_HEIGHT + INT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SNAP_IV + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SNAP_KEY + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SNAP_DURATION + INT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_MEDIA_URL + INT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_MEDIA_ID + TEXT_TYPE + " )",
    };

    private Gson gson;
    private String[] idHolder = new String[1];
    private String[] keyProjection = {ChatEntry.COLUMN_NAME_UNIQUE_ID};

    public ChatsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, SQL_CREATE_ENTRIES, DATABASE_VERSION);
        gson = new Gson();
    }

    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long getRowCount() {
        return super.getRowCount(ChatEntry.TABLE_NAME);
    }

    public boolean insertChat(ChatData chatData) {
        if (this.containsChat(chatData.getUniqueId())) {
            Logger.log("Already contains chat: " + chatData.getText());
            return false;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatEntry.COLUMN_NAME_CONVERSATION_ID, chatData.getConversationId());
        contentValues.put(ChatEntry.COLUMN_NAME_TYPE, String.valueOf(chatData.getMessageType()));
        contentValues.put(ChatEntry.COLUMN_NAME_UNIQUE_ID, chatData.getUniqueId());
        contentValues.put(ChatEntry.COLUMN_NAME_MESSAGE_ID, chatData.getMessageId());
        contentValues.put(ChatEntry.COLUMN_NAME_MESSAGE_TEXT, chatData.getText());
        contentValues.put(ChatEntry.COLUMN_NAME_SENDER, chatData.getSender());
        contentValues.put(ChatEntry.COLUMN_NAME_RECIPIENT_GSON, gson.toJson(chatData.getRecipients(), List.class));
        contentValues.put(ChatEntry.COLUMN_NAME_TIMESTAMP, chatData.getTimestamp());
        contentValues.put(ChatEntry.COLUMN_NAME_ITER_TOKEN, chatData.getIter_token());
        contentValues.put(ChatEntry.COLUMN_NAME_SEQ_NUM, chatData.getSeq_num());

        contentValues.put(ChatEntry.COLUMN_NAME_SNAP_KEY, chatData.getSnapKey());
        contentValues.put(ChatEntry.COLUMN_NAME_SNAP_IV, chatData.getSnapIV());
        contentValues.put(ChatEntry.COLUMN_NAME_SNAP_WIDTH, chatData.getSnapWidth());
        contentValues.put(ChatEntry.COLUMN_NAME_SNAP_HEIGHT, chatData.getSnapHeight());
        contentValues.put(ChatEntry.COLUMN_NAME_SNAP_DURATION, chatData.getSnapDuration());
        contentValues.put(ChatEntry.COLUMN_NAME_MEDIA_URL, chatData.getMediaURL());
        contentValues.put(ChatEntry.COLUMN_NAME_MEDIA_ID, chatData.getMediaID());
        contentValues.put(ChatEntry.COLUMN_NAME_MEDIA_TYPE, String.valueOf(chatData.getMediaType()));

        long rowsInserted = super.insertValues(ChatEntry.TABLE_NAME, contentValues);

        return rowsInserted > 0;
    }

    public boolean insertSnap(ChatData chatData) {
        if (this.containsChat(chatData.getUniqueId())) {
            Logger.log("Already contains chat: " + chatData.getText());
            return false;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatEntry.COLUMN_NAME_CONVERSATION_ID, chatData.getConversationId());
        contentValues.put(ChatEntry.COLUMN_NAME_TYPE, chatData.getMessageType().value);
        contentValues.put(ChatEntry.COLUMN_NAME_UNIQUE_ID, chatData.getUniqueId());
        contentValues.put(ChatEntry.COLUMN_NAME_MESSAGE_ID, chatData.getMessageId());
        contentValues.put(ChatEntry.COLUMN_NAME_SENDER, chatData.getSender());
        contentValues.put(ChatEntry.COLUMN_NAME_RECIPIENT_GSON, gson.toJson(chatData.getRecipients(), List.class));
        contentValues.put(ChatEntry.COLUMN_NAME_TIMESTAMP, chatData.getTimestamp());
        contentValues.put(ChatEntry.COLUMN_NAME_ITER_TOKEN, chatData.getIter_token());
        contentValues.put(ChatEntry.COLUMN_NAME_SEQ_NUM, chatData.getSeq_num());

        long rowsInserted = super.insertValues(ChatEntry.TABLE_NAME, contentValues);

        return rowsInserted > 0;
    }

    public boolean containsChat(String chatUniqueId) {
        if (chatUniqueId == null) {
            Logger.log("Supplied null ID for DB Contains Check");
            return false;
        }

        idHolder[0] = chatUniqueId;
        return super.containsObject(ChatEntry.TABLE_NAME, ChatEntry.COLUMN_NAME_UNIQUE_ID, idHolder, null, keyProjection);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getAllChats() {
        CallbackHandler callback = getCallback("getAllChatsFromCursor", Cursor.class);
        return (ArrayList<Object>) getAllBuiltObjects(ChatEntry.TABLE_NAME, callback);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getAllChatsFrom(String conversationId) {
        CallbackHandler callback = getCallback("getAllChatsFromCursor", Cursor.class);
        return (ArrayList<Object>) getAllBuiltObjects(ChatEntry.TABLE_NAME, ChatEntry.COLUMN_NAME_CONVERSATION_ID + " = '" + conversationId + "'", null, callback);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getAllChatsFromExcept(String conversationId, String formattedBlacklist) {
        CallbackHandler callback = getCallback("getAllChatsFromCursor", Cursor.class);

        return (ArrayList<Object>) getAllBuiltObjects(
                ChatEntry.TABLE_NAME,
                ChatEntry.COLUMN_NAME_CONVERSATION_ID + " = '" + conversationId + "'" +
                        " AND " + ChatEntry.COLUMN_NAME_UNIQUE_ID + " NOT IN " + formattedBlacklist,
                null,
                callback);
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     *
     * @param cursor
     * @return chatDataList
     */
    public ArrayList<ChatData> getAllChatsFromCursor(Cursor cursor) {
        ArrayList<ChatData> chatDataList = new ArrayList<>();

        while (!cursor.isAfterLast()) {
            //Logger.log("Looping cursor result");
            ChatData chatData = getChatFromCursor(cursor);

            if (chatData == null) {
                Logger.log("Null chatdata pulled");
                cursor.moveToNext();
                continue;
            }

            chatDataList.add(chatData);
            cursor.moveToNext();
        }

        return chatDataList;
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     *
     * @param cursor
     * @return chatData
     */
    public ChatData getChatFromCursor(Cursor cursor) {

        try {
            ChatData chatData = new ChatData();
            String strType = cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_TYPE));
            ChatData.MessageType messageType;

            try {
                messageType = ChatData.MessageType.valueOf(strType);
            } catch (IllegalArgumentException e) {
                Logger.log("Unknown message type: " + strType);
                return null;
            }

            chatData.setMessageType(messageType);
            chatData.setConversationId(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_CONVERSATION_ID)));
            chatData.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_MESSAGE_ID)));
            chatData.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_UNIQUE_ID)));
            chatData.setSender(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SENDER)));
            chatData.setIter_token(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_ITER_TOKEN)));
            chatData.setSeq_num(cursor.getLong(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SEQ_NUM)));
            chatData.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_TIMESTAMP)));

            String jsonRecipients = cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_RECIPIENT_GSON));
            List<String> recipients = gson.fromJson(jsonRecipients, List.class);
            chatData.setRecipients(recipients);

            if (messageType == ChatData.MessageType.TEXT) {
                chatData.setText(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_MESSAGE_TEXT)));
                return chatData;
            } else if (messageType == ChatData.MessageType.MEDIA) {
                String strMediaType = cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_MEDIA_TYPE));

                ChatData.MediaType mediaType;

                try {
                    mediaType = ChatData.MediaType.valueOf(strMediaType);
                } catch (IllegalArgumentException e) {
                    Logger.log("Unknown media type: " + strMediaType);
                    return null;
                }

                chatData.setMediaType(mediaType);
                chatData.setSnapKey(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SNAP_KEY)));
                chatData.setSnapIV(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SNAP_IV)));
                chatData.setMediaID(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_MEDIA_ID)));
                chatData.setMediaURL(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_MEDIA_URL)));
                chatData.setSnapDuration(cursor.getInt(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SNAP_DURATION)));
                chatData.setSnapDuration(cursor.getInt(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SNAP_DURATION)));
                chatData.setSnapWidth(cursor.getInt(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SNAP_WIDTH)));
                chatData.setSnapHeight(cursor.getInt(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SNAP_HEIGHT)));

                return chatData;
            }

            //Logger.log("Queried database for lens: " + lensData.mCode + " Active: " + lensData.mActive);
        } catch (Exception e) {
            Logger.log("Issue querying database", e);
            return null;
        }

        return null;
    }

    /**
     * Usage: getCallback("methodToCall", ParameterClassTypes...);
     *
     * @param methodName - The name of the method to call
     * @param classType  - The list of Classes called as the method parameters
     * @return CallbackHandler - The object holding the callback data
     */
    //TODO Generalize this to the CoreDBHandler
    public CallbackHandler getCallback(String methodName, Class... classType) {
        try {
            Logger.log("Trying to build callback method");
            return new CallbackHandler(this, ChatsDatabaseHelper.class.getMethod(methodName, classType));
        } catch (NoSuchMethodException e) {
            Logger.log("ERROR GETTING CALLBACK", e);
            return null;
        }
    }

    private static class ChatEntry implements BaseColumns {
        static final String TABLE_NAME = "ChatMessages";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_CONVERSATION_ID = "conversation_id";//statefulChatFeedItem.M_()
        static final String COLUMN_NAME_UNIQUE_ID = "unique_id";
        static final String COLUMN_NAME_MESSAGE_ID = "message_id";//statefulChatFeedItem.M_()
        static final String COLUMN_NAME_MESSAGE_TEXT = "text";
        static final String COLUMN_NAME_SENDER = "sender";//statefulChatFeedItem.am
        static final String COLUMN_NAME_RECIPIENT_GSON = "recipient";//statefulChatFeedItem.al List<String>
        static final String COLUMN_NAME_ITER_TOKEN = "iter_token";//Ie.l
        static final String COLUMN_NAME_SEQ_NUM = "seq_num";//Ie.z() - Long
        static final String COLUMN_NAME_TIMESTAMP = "timestamp";//Long.valueOf(statefulChatFeedItem.i())
        static final String COLUMN_NAME_SNAP_WIDTH = "snap_width";
        static final String COLUMN_NAME_SNAP_HEIGHT = "snap_height";
        static final String COLUMN_NAME_SNAP_IV = "snap_iv";
        static final String COLUMN_NAME_SNAP_KEY = "snap_key";
        static final String COLUMN_NAME_SNAP_DURATION = "snap_duration";
        static final String COLUMN_NAME_MEDIA_URL = "media_url";
        static final String COLUMN_NAME_MEDIA_ID = "media_id";
        static final String COLUMN_NAME_MEDIA_TYPE = "media_type";
    }

    private static class SnapEntry implements BaseColumns {
        static final String TABLE_NAME = "ChatSnaps";
        static final String COLUMN_NAME_CONVERSATION_ID = "conversation_id";
        static final String COLUMN_NAME_ES_ID = "es_id";
        static final String COLUMN_NAME_ID = "es_id";
        static final String COLUMN_NAME_CAPTION = "caption";
        static final String COLUMN_NAME_FILTER_ID = "filter_id";
        static final String COLUMN_NAME_ITER_TOKEN = "iter_token";
        static final String COLUMN_NAME_SENDER = "sender";//statefulChatFeedItem.am
        static final String COLUMN_NAME_DURATION = "duration";
    }
}
