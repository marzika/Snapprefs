package com.marz.snapprefs.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andre on 20/10/2016.
 */

public class ChatData {
    private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm\ndd MMM\nyyyy");
    private String conversationId;
    private String messageId;
    private String text;
    private String sender;
    private String friendName;
    private String formattedDate;
    private long timestamp;

    public ChatData() {
    }


    // ### GETTERS & SETTERS ### \\
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) throws Exception {
        if (conversationId == null)
            throw new Exception(("Null Conversation ID"));

        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) throws Exception {
        if (messageId == null)
            throw new Exception(("Null Message ID"));

        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) throws Exception {
        if (text == null)
            throw new Exception("Null Message Text");

        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) throws Exception {
        if (sender == null)
            throw new Exception("Null Sender");

        this.sender = sender;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) throws Exception {
        if( friendName == null )
            throw new Exception("Null friend name");

        this.friendName = friendName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) throws Exception {
        if (timestamp == null || timestamp == 0L)
            throw new Exception("Null Timestamp");

        this.timestamp = timestamp;
        formattedDate = sdf.format(new Date(timestamp));
    }

    public String getFormattedDate() {
        return this.formattedDate;
    }
}
