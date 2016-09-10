package com.marz.snapprefs;

/**
 * Created by Andre on 27/08/2016.
 */
public class ChatData
{
    private String mId;
    private int mIdHash;
    private String mRecipient;
    private String mSender;
    private String mOtherUser;
    private boolean mIsSavedBySender;
    private boolean mIsSavedByRecipient;
    private long mTimestamp;
    private String mUserText;

    private ChatState state;

    public ChatData(String mId, String mRecipient, String mSender, String mOtherUser, boolean mIsSavedBySender, boolean mIsSavedByRecipient, long mTimestamp, String mUserText) {
        this.mId = mId;
        this.mIdHash = mId.hashCode();
        this.mRecipient = mRecipient;
        this.mSender = mSender;
        this.mOtherUser = mOtherUser;
        this.mIsSavedBySender = mIsSavedBySender;
        this.mIsSavedByRecipient = mIsSavedByRecipient;
        this.mTimestamp = mTimestamp;
        this.mUserText = mUserText;
        this.state = ChatState.Ready;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
        this.mIdHash = mId.hashCode();
    }

    public int getmIdHash() {
        return mIdHash;
    }

    public int getHashMod( int mod, boolean abs )
    {
        return ( abs ? Math.abs( mIdHash ) : mIdHash ) % mod;
    }

    public String getmRecipient() {
        return mRecipient;
    }

    public void setmRecipient(String mRecipient) {
        this.mRecipient = mRecipient;
    }

    public String getmSender() {
        return mSender;
    }

    public void setmSender(String mSender) {
        this.mSender = mSender;
    }

    public String getmOtherUser(){ return mOtherUser; }

    public boolean ismIsSavedBySender() {
        return mIsSavedBySender;
    }

    public void setmIsSavedBySender(boolean mIsSavedBySender) {
        this.mIsSavedBySender = mIsSavedBySender;
    }

    public boolean ismIsSavedByRecipient() {
        return mIsSavedByRecipient;
    }

    public void setmIsSavedByRecipient(boolean mIsSavedByRecipient) {
        this.mIsSavedByRecipient = mIsSavedByRecipient;
    }

    public long getmTimestamp() {
        return mTimestamp;
    }

    public void setmTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public String getmUserText() {
        return mUserText;
    }

    public void setmUserText(String mUserText) {
        this.mUserText = mUserText;
    }

    public enum ChatState{
        Ready,
        Handled
    }
}
