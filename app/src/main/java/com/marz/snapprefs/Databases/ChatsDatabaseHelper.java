package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Util.ChatData;
import com.marz.snapprefs.Util.ConversationItem;

import java.util.ArrayList;

import static com.marz.snapprefs.Databases.CoreDatabaseHandler.CallbackHandler.getCallback;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class ChatsDatabaseHelper extends CoreDatabaseHandler {
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = Preferences.getContentPath() + "/ChatMessages.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String[] SQL_CREATE_ENTRIES = {
            "CREATE TABLE " + ConversationEntry.TABLE_NAME + " (" +
                    ConversationEntry.COLUMN_NAME_CONVERSATION_ID + TEXT_TYPE + " PRIMARY KEY," +
                    ConversationEntry.COLUMN_NAME_FRIEND_NAME + TEXT_TYPE + " NOT NULL )",

            "CREATE TABLE " + ChatEntry.TABLE_NAME + " (" +
                    ChatEntry.COLUMN_NAME_MESSAGE_ID + TEXT_TYPE + " PRIMARY KEY," +
                    ChatEntry.COLUMN_NAME_CONVERSATION_ID + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_MESSAGE_TEXT + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_SENDER + TEXT_TYPE + COMMA_SEP +
                    ChatEntry.COLUMN_NAME_TIMESTAMP + INT_TYPE + " )",
    };
    private static byte[] keyBytes;
    private Gson gson;
    private String[] idHolder = new String[1];
    private String[] keyProjection = {ChatEntry.COLUMN_NAME_MESSAGE_ID};
    private String[] convKeyProjection = {ConversationEntry.COLUMN_NAME_CONVERSATION_ID};

    public ChatsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, SQL_CREATE_ENTRIES, DATABASE_VERSION);
        gson = new Gson();
    }

    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.log(String.format("Upgrading ChatDB from v%s to v%s", oldVersion, newVersion), LogType.DATABASE);

        if (oldVersion < 3) {
            db.execSQL("DROP TABLE ChatMessages");
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long getRowCount() {
        return super.getRowCount(ChatEntry.TABLE_NAME);
    }

    public long getRowCount(String tableName) {
        return super.getRowCount(tableName);
    }

    public boolean insertChat(ChatData chatData) {
        if (this.containsChat(chatData.getMessageId())) {
            Logger.log("Already contains chat: " + chatData.getText(), LogType.DATABASE);
            return false;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatEntry.COLUMN_NAME_CONVERSATION_ID, chatData.getConversationId());
        contentValues.put(ChatEntry.COLUMN_NAME_MESSAGE_ID, chatData.getMessageId());
        contentValues.put(ChatEntry.COLUMN_NAME_MESSAGE_TEXT, chatData.getText());
        contentValues.put(ChatEntry.COLUMN_NAME_SENDER, chatData.getSender());
        contentValues.put(ChatEntry.COLUMN_NAME_TIMESTAMP, chatData.getTimestamp());

        long rowsInserted = super.insertValues(ChatEntry.TABLE_NAME, contentValues);

        if (rowsInserted >= 0)
            Logger.log("Inserted chat object", LogType.DATABASE);

        if (!this.containsConversation(chatData.getConversationId())) {
            contentValues = new ContentValues();
            contentValues.put(ConversationEntry.COLUMN_NAME_CONVERSATION_ID, chatData.getConversationId());
            contentValues.put(ConversationEntry.COLUMN_NAME_FRIEND_NAME, chatData.getFriendName());

            long conversationRow = super.insertValues(ConversationEntry.TABLE_NAME, contentValues);

            if (conversationRow > 0)
                Logger.log("Created new Conversation", LogType.DATABASE);
        }

        return rowsInserted >= 0;
    }

    public boolean removeChat(String messageId) {
        idHolder[0] = messageId;

        int rowAffected = super.deleteObject(ChatEntry.TABLE_NAME, ChatEntry.COLUMN_NAME_MESSAGE_ID, idHolder);

        return rowAffected >= 0;
    }

    public boolean removeConversation(String conversationId) {
        idHolder[0] = conversationId;

        int rowAffected = super.deleteObject(ConversationEntry.TABLE_NAME, ConversationEntry.COLUMN_NAME_CONVERSATION_ID, idHolder);
        rowAffected += super.deleteObject(ChatEntry.TABLE_NAME, ChatEntry.COLUMN_NAME_CONVERSATION_ID, idHolder);

        return rowAffected >= 0;
    }

    public boolean containsChat(String chatMessageId) {
        if (chatMessageId == null) {
            Logger.log("Supplied null ID for DB Contains Check", LogType.DATABASE);
            return false;
        }

        idHolder[0] = chatMessageId;
        return super.containsObject(ChatEntry.TABLE_NAME, ChatEntry.COLUMN_NAME_MESSAGE_ID,
                idHolder);
    }

    public boolean containsConversation(String conversationId) {
        if (conversationId == null) {
            Logger.log("Supplied null ID for DB Contains Check", LogType.DATABASE);
            return false;
        }

        idHolder[0] = conversationId;

        return super.containsObject(ConversationEntry.TABLE_NAME, ConversationEntry.COLUMN_NAME_CONVERSATION_ID,
                idHolder);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getAllChats() {
        CallbackHandler callback = getCallback(this, "getAllChatsFromCursor", Cursor.class);
        return (ArrayList<Object>) getAllBuiltObjects(ChatEntry.TABLE_NAME, callback);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getAllChatsFrom(String conversationId) {
        CallbackHandler callback = getCallback(this, "getAllChatsFromCursor", Cursor.class);
        return (ArrayList<Object>) getAllBuiltObjects(ChatEntry.TABLE_NAME,
                ChatEntry.COLUMN_NAME_CONVERSATION_ID + " = '" + conversationId + "'",
                ChatEntry.COLUMN_NAME_TIMESTAMP + " DESC", callback);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getAllChatsFromExcept(String conversationId, String formattedBlacklist) {
        CallbackHandler callback = getCallback(this, "getAllChatsFromCursor", Cursor.class);

        return (ArrayList<Object>) getAllBuiltObjects(
                ChatEntry.TABLE_NAME,
                ChatEntry.COLUMN_NAME_CONVERSATION_ID + " = '" + conversationId + "'" +
                        " AND " + ChatEntry.COLUMN_NAME_MESSAGE_ID + " NOT IN " + formattedBlacklist,
                ChatEntry.COLUMN_NAME_TIMESTAMP + " ASC",
                callback);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getAllConversations() {
        Logger.log("Getting all conversations", LogType.DATABASE);
        CallbackHandler callback = getCallback(this, "getAllConversationsFromCursor", Cursor.class);

        return (ArrayList<Object>) getAllBuiltObjects(
                ConversationEntry.TABLE_NAME, null, null, callback);
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     *
     * @param cursor
     * @return chatDataList
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public ArrayList<ChatData> getAllChatsFromCursor(Cursor cursor) {
        ArrayList<ChatData> chatDataList = new ArrayList<>();

        while (!cursor.isAfterLast()) {
            //Logger.log("Looping cursor result");
            ChatData chatData = getChatFromCursor(cursor);

            if (chatData == null) {
                Logger.log("Null chatdata pulled", LogType.DATABASE);
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
    @SuppressWarnings({"unused", "WeakerAccess", "unchecked"})
    public ChatData getChatFromCursor(Cursor cursor) {

        try {
            ChatData chatData = new ChatData();
            chatData.setConversationId(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_CONVERSATION_ID)));
            chatData.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_MESSAGE_ID)));
            chatData.setSender(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_SENDER)));
            chatData.setText(cursor.getString(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_MESSAGE_TEXT)));
            chatData.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ChatEntry.COLUMN_NAME_TIMESTAMP)));

            return chatData;
            //Logger.log("Queried database for lens: " + lensData.mCode + " Active: " + lensData.mActive);
        } catch (Exception e) {
            Logger.log("Issue querying database", e);
            return null;
        }
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     *
     * @param cursor
     * @return chatDataList
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public ArrayList<ConversationItem> getAllConversationsFromCursor(Cursor cursor) {
        ArrayList<ConversationItem> conversationList = new ArrayList<>();

        while (!cursor.isAfterLast()) {
            //Logger.log("Looping cursor result");

            try {
                ConversationItem conversationItem = getConversationFromCursor(cursor);

                if (conversationItem == null) {
                    Logger.log("Null conversation pulled", LogType.DATABASE);
                    cursor.moveToNext();
                    continue;
                }

                conversationList.add(conversationItem);
            } catch( Exception e ){
                Logger.log("Error creating conversation", e, LogType.DATABASE);
            }

            cursor.moveToNext();
        }

        return conversationList;
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     *
     * @param cursor
     * @return chatData
     */
    @SuppressWarnings({"unused", "WeakerAccess", "unchecked"})
    public ConversationItem getConversationFromCursor(Cursor cursor) {

        try {
            ConversationItem conversation = new ConversationItem();
            conversation.conversationId = cursor.getString(cursor.getColumnIndexOrThrow(ConversationEntry.COLUMN_NAME_CONVERSATION_ID));
            conversation.friendName = cursor.getString(cursor.getColumnIndexOrThrow(ConversationEntry.COLUMN_NAME_FRIEND_NAME));

            if (conversation.conversationId != null) {
                ArrayList<Object> messageList = this.getAllChatsFrom(conversation.conversationId);

                if (messageList != null) {
                    conversation.messageList = messageList;

                    return conversation;
                }
            }
        } catch (Exception e) {
            Logger.log("Issue querying database", e, LogType.DATABASE);
            return null;
        }

        return null;
    }

    private static class ConversationEntry implements BaseColumns {
        static final String TABLE_NAME = "Conversations";
        static final String COLUMN_NAME_FRIEND_NAME = "friend_name";
        static final String COLUMN_NAME_CONVERSATION_ID = "conversation_id";
    }

    private static class ChatEntry implements BaseColumns {
        static final String TABLE_NAME = "ChatMessages";
        static final String COLUMN_NAME_CONVERSATION_ID = "conversation_id";
        static final String COLUMN_NAME_MESSAGE_ID = "message_id";//statefulChatFeedItem.M_()
        static final String COLUMN_NAME_MESSAGE_TEXT = "text";
        static final String COLUMN_NAME_SENDER = "sender";//statefulChatFeedItem.am
        static final String COLUMN_NAME_TIMESTAMP = "timestamp";//Long.valueOf(statefulChatFeedItem.i())
    }
}
