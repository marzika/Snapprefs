package com.marz.snapprefs.Util;

import com.marz.snapprefs.ChatData;

/**
 * Created by Andre on 15/09/2016.
 */
public class ChatMessageItem {
    private ChatData chatData;

    public ChatMessageItem(ChatData chatData) {
        this.chatData = chatData;
    }

    public String getMSender() {
        return chatData.getmSender();
    }

    public String getRecipient() {
        return chatData.getmRecipient();
    }

    public String getmTimestamp() {
        return "" + chatData.getmTimestamp();
    }

    public String getUserText() {
        return chatData.getmUserText();
    }
}
