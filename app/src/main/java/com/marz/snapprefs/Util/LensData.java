package com.marz.snapprefs.Util;

import android.content.ContentValues;

import com.marz.snapprefs.Databases.LensDatabaseHelper.LensEntry;
import com.marz.snapprefs.Logger;

/**
 * Created by Andre on 12/09/2016.
 */
public class LensData implements Comparable{
    public String mCode;
    //public String mGplayIapId;
    public String mHintId;
    //public Map<String, String> mHintTranslations;
    public String mIconLink;
    public String mId;
    //public boolean mIsBackSection;
    //public boolean mIsFeatured;
    //public transient boolean mIsLoading;
    //public boolean mIsSponsored;
    public String mLensLink;
    //public int mPriority;
    public String mSignature;
    public boolean mActive;
    public long selTime;

    //public Object mType;
    //public Bitmap mLensIcon;

    public ContentValues getContent()
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LensEntry.COLUMN_NAME_MCODE, mCode);
        //contentValues.put(LensEntry.COLUMN_NAME_GPLAYID, mGplayIapId);
        contentValues.put(LensEntry.COLUMN_NAME_MHINTID, mHintId);
        contentValues.put(LensEntry.COLUMN_NAME_MICONLINK, mIconLink);
        contentValues.put(LensEntry.COLUMN_NAME_MID, mId);
        contentValues.put(LensEntry.COLUMN_NAME_MLENSLINK, mLensLink);
        contentValues.put(LensEntry.COLUMN_NAME_MSIGNATURE, mSignature);
        contentValues.put(LensEntry.COLUMN_NAME_ACTIVE, mActive);
        contentValues.put(LensEntry.COLUMN_NAME_SEL_TIME, selTime);
        return contentValues;
    }

    @Override
    public int compareTo(Object o) {
        LensData lensDataObj = (LensData) o;
        long compareTime = lensDataObj.selTime;
        if(lensDataObj.mActive) {
            Logger.log("compareTime: " + compareTime);
            Logger.log("this.selTime: " + this.selTime);
        }
        if(compareTime < this.selTime) {
            return 1;
        } else {
            return -1;
        }
    }
}
