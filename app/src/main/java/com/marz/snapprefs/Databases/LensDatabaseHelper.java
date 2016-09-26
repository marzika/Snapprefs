package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Util.LensData;

import java.util.ArrayList;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensDatabaseHelper extends CoreDatabaseHandler {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = Preferences.getSavePath() + "/Lenses.db";
    public static final String[] fullProjection = {
            LensEntry.COLUMN_NAME_MCODE,
            LensEntry.COLUMN_NAME_GPLAYID,
            LensEntry.COLUMN_NAME_MHINTID,
            LensEntry.COLUMN_NAME_MICONLINK,
            LensEntry.COLUMN_NAME_MID,
            LensEntry.COLUMN_NAME_MLENSLINK,
            LensEntry.COLUMN_NAME_MSIGNATURE,
            LensEntry.COLUMN_NAME_ACTIVE
    };
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LensEntry.TABLE_NAME;
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LensEntry.TABLE_NAME + " (" +
                    LensEntry.COLUMN_NAME_MCODE + TEXT_TYPE + " PRIMARY KEY," +
                    LensEntry.COLUMN_NAME_GPLAYID + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MHINTID + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MICONLINK + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MID + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MLENSLINK + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MSIGNATURE + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_ACTIVE + " INTEGER DEFAULT 0 )";

    public LensDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, SQL_CREATE_ENTRIES, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + LensEntry.TABLE_NAME +
                    " ADD COLUMN " + LensEntry.COLUMN_NAME_ACTIVE + " INTEGER DEFAULT 0");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long getRowCount() {
        return super.getRowCount(LensEntry.TABLE_NAME);
    }

    public void insertLens(LensData lensData) {
        Logger.log("Inserting new lens: " + lensData.mCode);
        long newRowId = super.insertValues(LensEntry.TABLE_NAME, lensData.getContent());
        Logger.log("New Lens Row ID: " + newRowId);
    }

    public boolean containsLens(String mCode) {
        Logger.log("Getting lens from database");

        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        return super.containsObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, sortOrder, fullProjection);
    }

    public boolean toggleLensActiveState(String mCode) throws Exception {
        Logger.log("Toggling lens state");
        boolean activeState = !getLensActiveState(mCode);
        Logger.log("Current state: " + activeState);

        ContentValues values = new ContentValues();
        values.put(LensEntry.COLUMN_NAME_ACTIVE, activeState ? 1 : 0);

        String[] selectionArgs = {mCode};

        super.updateObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, values);

        return activeState;
    }

    public boolean getLensActiveState(String mCode) throws Exception {
        Logger.log("Getting lens from database");

        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        String[] projection = {LensEntry.COLUMN_NAME_ACTIVE};
        Logger.log("Performing query: " + LensEntry.COLUMN_NAME_MCODE + mCode);

        ContentValues values = super.getContent(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, sortOrder, projection);
        int activeState = (int) values.get(LensEntry.COLUMN_NAME_ACTIVE);
        return activeState != 0;
    }

    public int getActiveLensCount() {
        Logger.log("Getting lens from database");

        String[] selectionArgs = {"1"};

        int count = super.getCount(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_ACTIVE, selectionArgs, fullProjection);

        Logger.log("Query count: " + count);

        return count;
    }

    public LensData getLens(String mCode) {
        Logger.log("Getting lens from database");

        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        CallbackHandler callback = getCallback("getLensFromCursor", LensDatabaseHelper.class, Cursor.class);
        LensData lensData = (LensData) super.getBuiltContent(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE,
                selectionArgs, sortOrder, fullProjection, callback);

        Logger.log("Queried database to get lens: " + lensData.mCode);
        return lensData;
    }

    // TODO Setup proper callback handling
    public ArrayList<Object> getAllExcept(ArrayList<String> blacklist) {
        CallbackHandler callback = getCallback("getAllLensesFromCursor", Cursor.class);

        return super.getAllBuiltObjectsExcept(LensEntry.TABLE_NAME,
                LensEntry.COLUMN_NAME_MCODE, blacklist, callback);
    }

    public ArrayList<Object> getAllActive() {
        CallbackHandler callback = getCallback("getAllLensesFromCursor", Cursor.class);

        String selection = LensEntry.COLUMN_NAME_ACTIVE + " = ?";
        String[] selectionArgs = {"1"};

        return super.performQueryForBuiltObjects(LensEntry.TABLE_NAME,
                selection, selectionArgs, fullProjection, null, callback);
    }

    public ArrayList<Object> getAllLenses() {
        Logger.log("Getting all lenses from database");
        CallbackHandler callback = getCallback("getAllLensesFromCursor", Cursor.class);

        return super.getAllBuiltObjects(LensEntry.TABLE_NAME, callback);
    }

    public void deleteLens(String mCode) {
        String[] selectionArgs = {mCode};

        super.deleteObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs);
    }

    public void updateLens(String mCode, ContentValues values) {
// Which row to update, based on the title
        String[] selectionArgs = {mCode};

        super.updateObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, values);
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     * @param cursor
     * @return lensDataList
     */
    public ArrayList<LensData> getAllLensesFromCursor(Cursor cursor) {
        ArrayList<LensData> lensDataList = new ArrayList<>();

        while (!cursor.isAfterLast()) {
            Logger.log("Looping cursor result");
            LensData lensData = getLensFromCursor(cursor);

            if (lensData == null) {
                Logger.log("Null lensdata pulled");
                continue;
            }

            lensDataList.add(lensData);
            cursor.moveToNext();
        }

        return lensDataList;
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     * @param cursor
     * @return lensData
     */
    public LensData getLensFromCursor(Cursor cursor) {
        LensData lensData = new LensData();

        try {
            lensData.mCode = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MCODE));
            lensData.mGplayIapId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_GPLAYID));
            lensData.mHintId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MHINTID));
            lensData.mIconLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MICONLINK));
            lensData.mId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MID));
            lensData.mLensLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MLENSLINK));
            lensData.mSignature = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MSIGNATURE));
            short activeState = cursor.getShort(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_ACTIVE));
            lensData.mActive = activeState != 0;

            Logger.log("Queried database for lens: " + lensData.mCode + " Active: " + lensData.mActive);
        } catch (IllegalArgumentException e) {
            Logger.log("Issue querying database", e);
            return null;
        }

        return lensData;
    }

    /**
     * Usage: getCallback("methodToCall", ParameterClassTypes...);
     * @param methodName - The name of the method to call
     * @param classType - The list of Classes called as the method parameters
     * @return CallbackHandler - The object holding the callback data
     */
    public CallbackHandler getCallback(String methodName, Class... classType) {
        try {
            Logger.log("Trying to build callback method");
            return new CallbackHandler(this, LensDatabaseHelper.class.getMethod(methodName, classType));
        } catch (NoSuchMethodException e) {
            Logger.log("ERROR GETTING CALLBACK", e);
            return null;
        }
    }

    public static class LensEntry implements BaseColumns {
        public static final String TABLE_NAME = "LensTable";
        public static final String COLUMN_NAME_MCODE = "mCode";
        public static final String COLUMN_NAME_GPLAYID = "mGplayIapId";
        public static final String COLUMN_NAME_MHINTID = "mHintId";
        public static final String COLUMN_NAME_MICONLINK = "mIconLink";
        public static final String COLUMN_NAME_MID = "mId";
        public static final String COLUMN_NAME_MLENSLINK = "mLensLink";
        public static final String COLUMN_NAME_MSIGNATURE = "mSignature";
        public static final String COLUMN_NAME_ACTIVE = "mActiveState";
    }
}