package com.marz.snapprefs.Util;

import java.util.List;

/**
 * Created by Andre on 20/10/2016.
 */

public class ChatData {
    private String conversationId;
    private String uniqueId;
    private String messageId;
    private String text;
    private String sender;
    private List<String> recipient;
    private String iter_token;
    private long seq_num;
    private long timestamp;

    private MessageType messageType;
    private MediaType mediaType;

    private int snapWidth;
    private int snapHeight;
    private String snapIV;
    private String snapKey;

    private float snapDuration;
    private String mediaID;
    private String mediaURL;

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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) throws Exception {
        if (uniqueId == null)
            throw new Exception(("Null Unique ID"));

        this.uniqueId = uniqueId;
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

    public List<String> getRecipients() {
        return recipient;
    }

    public void setRecipients(List<String> recipient) throws Exception {
        if (recipient == null)
            throw new Exception("Null Recipients");

        this.recipient = recipient;
    }

    public String getIter_token() {
        return iter_token;
    }

    public void setIter_token(String iter_token) throws Exception {
        this.iter_token = iter_token;
    }

    public long getSeq_num() {
        return seq_num;
    }

    public void setSeq_num(long seq_num) throws Exception {
        this.seq_num = seq_num;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) throws Exception {
        if (timestamp == 0L)
            throw new Exception("Null Timestamp");

        this.timestamp = timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) throws Exception {
        if (messageType == null)
            throw new Exception("Null Message Type");

        this.messageType = messageType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) throws Exception {
        if (mediaType == null)
            throw new Exception("Null Media Type");

        this.mediaType = mediaType;
    }

    public int getSnapWidth() {
        return snapWidth;
    }

    public void setSnapWidth(int snapWidth) {
        this.snapWidth = snapWidth;
    }

    public int getSnapHeight() {
        return snapHeight;
    }

    public void setSnapHeight(int snapHeight) {
        this.snapHeight = snapHeight;
    }

    public String getSnapIV() {
        return snapIV;
    }

    public void setSnapIV(String snapIV) throws Exception {
        if (snapIV == null)
            throw new Exception("Null Snap IV");

        this.snapIV = snapIV;
    }

    public String getSnapKey() {
        return snapKey;
    }

    public void setSnapKey(String snapKey) throws Exception {
        if (snapKey == null)
            throw new Exception("Null Snap Key");

        this.snapKey = snapKey;
    }

    public float getSnapDuration() {
        return snapDuration;
    }

    public void setSnapDuration(float snapDuration) {
        this.snapDuration = snapDuration;
    }

    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) throws Exception {
        this.mediaID = mediaID;
    }

    public String getMediaURL() {
        return mediaID;
    }

    public void setMediaURL(String mediaURL) throws Exception {
        this.mediaURL = mediaURL;
    }

    public enum MediaType {
        IMAGE, VIDEO
    }

    public enum MessageType {
        TEXT("text"), MEDIA("media");

        public String value;

        MessageType(String value) {
            this.value = value;
        }
    }
}