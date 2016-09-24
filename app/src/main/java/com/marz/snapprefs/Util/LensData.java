package com.marz.snapprefs.Util;

import android.content.ContentValues;

import com.marz.snapprefs.Databases.LensDatabaseHelper.LensEntry;

/**
 * Created by Andre on 12/09/2016.
 */
public class LensData {
    public String mCode;
    public String mGplayIapId;
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
    //public Object mType;
    //public Bitmap mLensIcon;

    public ContentValues getContent()
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LensEntry.COLUMN_NAME_MCODE, mCode);
        contentValues.put(LensEntry.COLUMN_NAME_GPLAYID, mGplayIapId);
        contentValues.put(LensEntry.COLUMN_NAME_MHINTID, mHintId);
        contentValues.put(LensEntry.COLUMN_NAME_MICONLINK, mIconLink);
        contentValues.put(LensEntry.COLUMN_NAME_MID, mId);
        contentValues.put(LensEntry.COLUMN_NAME_MLENSLINK, mLensLink);
        contentValues.put(LensEntry.COLUMN_NAME_MSIGNATURE, mSignature);
        contentValues.put(LensEntry.COLUMN_NAME_ACTIVE, mActive);
        return contentValues;
    }
}
