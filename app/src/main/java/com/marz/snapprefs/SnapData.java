package com.marz.snapprefs;

import android.graphics.Bitmap;

import com.marz.snapprefs.Saving.MediaType;
import com.marz.snapprefs.Saving.SnapType;
import com.marz.snapprefs.Util.BitmapCache;

import java.io.FileInputStream;
import java.util.ArrayList;

public class SnapData {
    private String mId;
    private String mKey;
    private String strSender;
    private String strTimestamp;
    private FileInputStream inputStream;
    private MediaType mediaType;
    private SnapType snapType;
    private ArrayList<FlagState> flags = new ArrayList<>();

    private static final BitmapCache imageCache = new BitmapCache(6);

    public SnapData(String mKey) {
        this.mKey = mKey;
    }

    public void setHeader(String mId, String mKey, String strSender, String strTimestamp,
                          SnapType snapType) {
        this.mId = mId;
        this.mKey = mKey;
        this.strSender = strSender;
        this.strTimestamp = strTimestamp;
        this.snapType = snapType;
        this.addFlag(FlagState.HEADER);
        this.checkForCompletion();
    }

    public boolean setPayload(Object payload) {
        if (payload instanceof Bitmap) {
            imageCache.addBitmapToMemoryCache(mKey, (Bitmap) payload);
            mediaType = MediaType.IMAGE;
        } else if (payload instanceof FileInputStream) {
            inputStream = (FileInputStream) payload;
            mediaType = MediaType.VIDEO;
        } else
            return false;

        this.addFlag(FlagState.PAYLOAD);
        this.checkForCompletion();
        return true;
    }

    public Object getPayload() {
        if (mediaType == MediaType.IMAGE) {
            Bitmap bmpImage = imageCache.get(mKey);
            return bmpImage == null || bmpImage.isRecycled() ? null : bmpImage;
        }
        else if (mediaType == MediaType.VIDEO)
            return inputStream;

        return null;
    }

    /***
     * Used to remove any unnecessary data
     * Should save memory in the long run
     */
    public void wipePayload() {
        if(mediaType == null)
            return;

        if(mediaType == MediaType.IMAGE)
            imageCache.remove(mKey);
        else
            this.inputStream = null;
    }

    public void setSaved()
    {
        flags.clear();
        flags.add(FlagState.SAVED);
    }

    private void checkForCompletion() {
        if (hasFlag(FlagState.COMPLETED))
            return;

        boolean isComplete =
                flags.contains(FlagState.HEADER) && flags.contains(FlagState.PAYLOAD);

        if (isComplete) {
            flags.clear();
            flags.add(FlagState.COMPLETED);
        }
    }

    public String toString()
    {
        return "mID: " + getmId() +
                "\nmKey: " + getmKey() +
                "\nSnapType: " + getSnapType() +
                "\nMediaType: " + getMediaType() +
                "\nSender: " + getStrSender() +
                "\nPayload: " + (getPayload() != null ) +
                "\nFlagCount: " + getFlags().size();
    }

    // ### GETTERS & SETTERS ### \\

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getStrSender() {
        return strSender;
    }

    public void setStrSender(String strSender) {
        this.strSender = strSender;
    }

    public String getStrTimestamp() {
        return strTimestamp;
    }

    public void setStrTimestamp(String strTimestamp) {
        this.strTimestamp = strTimestamp;
    }

    public FileInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(FileInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Bitmap getBmpImage() {
        return imageCache.get(mKey);
    }

    public void setBmpImage(Bitmap bmpImage) {
        imageCache.put(mKey, bmpImage);
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public SnapType getSnapType() {
        return snapType;
    }

    public void setSnapType(SnapType snapType) {
        this.snapType = snapType;
    }

    public ArrayList<FlagState> getFlags() {
        return flags;
    }

    public void setFlags(ArrayList<FlagState> flags) {
        this.flags = flags;
    }

    public void addFlag(FlagState flag) {
        this.flags.add(flag);
    }

    public void removeFlag(FlagState flag)
    {
        this.flags.remove(flag);
    }

    public boolean hasFlag(FlagState flag) {
        return getFlags().contains(flag);
    }
    // ### Enums ### \\

    public enum FlagState {
        HEADER, PAYLOAD, PROCESSING, COMPLETED, SAVED, FAILED
    }
}
