package com.marz.snapprefs.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.marz.snapprefs.Lens;
import com.marz.snapprefs.Lens.LensEntry;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = Preferences.getString(Preferences.Prefs.SAVE_PATH) + "/Lenses.db";
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
            "DROP TABLE IF EXISTS " + Lens.LensEntry.TABLE_NAME;
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

    private boolean requiresUpdate = true;
    private ArrayList<LensData> lensCache = new ArrayList<>();
    private ArrayList<LensData> excludedLensCache = new ArrayList<>();
    private SQLiteDatabase writeableDatabase;

    public LensDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String formatExclusionList(ArrayList<String> list) {
        StringBuilder builder = new StringBuilder();

        Iterator<String> iterator = list.iterator();

        while (iterator.hasNext()) {
            String str = iterator.next();
            builder.append("'");
            builder.append(str);
            builder.append("'");

            if (iterator.hasNext())
                builder.append(",");
        }
        return builder.toString();
    }

    public void createIfNotExisting() {
        if (writeableDatabase == null)
            writeableDatabase = this.getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + LensEntry.TABLE_NAME +
                    " ADD COLUMN " + LensEntry.COLUMN_NAME_ACTIVE + " INTEGER DEFAULT 0");
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long getRowCount() {
        createIfNotExisting();
        return DatabaseUtils.queryNumEntries(writeableDatabase, LensEntry.TABLE_NAME);
    }

    public void insertLens(LensData lensData) {
        createIfNotExisting();
        Logger.log("Inserting new lens: " + lensData.mCode);

        long newRowId = writeableDatabase.insert(LensEntry.TABLE_NAME, null, lensData.getContent());
        Logger.log("New Lens Row ID: " + newRowId);
        requiresUpdate = true;
    }

    public boolean containsLens(String mCode) {
        createIfNotExisting();
        Logger.log("Getting lens from database");

        String selection = LensEntry.COLUMN_NAME_MCODE + " = ?";
        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        Logger.log("Performing query: " + selection + mCode);

        Cursor cursor = writeableDatabase.query(
                LensEntry.TABLE_NAME,                     // The table to query
                fullProjection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        Logger.log("Query count: " + cursor.getCount());

        cursor.close();

        return cursor.getCount() != 0;
    }

    public boolean toggleLensActiveState(String mCode) throws Exception {
        createIfNotExisting();
        Logger.log("Toggling lens state");
        boolean activeState = !getLensActiveState(mCode);
        Logger.log("Current state: " + activeState);
        ContentValues values = new ContentValues();
        values.put(LensEntry.COLUMN_NAME_ACTIVE, activeState ? 1 : 0);

        updateLens(mCode, values);

        return activeState;
    }

    public boolean getLensActiveState(String mCode) throws Exception {
        createIfNotExisting();
        Logger.log("Getting lens from database");

        String selection = LensEntry.COLUMN_NAME_MCODE + "=?";
        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        String[] projection = {LensEntry.COLUMN_NAME_ACTIVE};
        Logger.log("Performing query: " + selection + mCode);

        Cursor cursor = writeableDatabase.query(
                LensEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        Logger.log("Query count: " + cursor.getCount());

        if (cursor.getCount() == 0) {
            cursor.close();
            throw new Exception("No lens found");
        }

        cursor.moveToFirst();

        short activeState = cursor.getShort(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_ACTIVE));
        cursor.close();
        return activeState != 0;
    }

    public LensData getLens(String mCode) {
        createIfNotExisting();
        Logger.log("Getting lens from database");

        String selection = LensEntry.COLUMN_NAME_MCODE + " = ?";
        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        Logger.log("Performing query: " + selection + mCode);

        Cursor cursor = writeableDatabase.query(
                LensEntry.TABLE_NAME,                     // The table to query
                fullProjection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        Logger.log("Query count: " + cursor.getCount());

        if (cursor.getCount() == 0)
            return null;

        cursor.moveToFirst();

        LensData lensData = getLensFromCursor(cursor);

        if (lensData == null)
            return null;

        cursor.close();
        Logger.log("Queried database to get lens: " + lensData.mCode);
        return lensData;
    }

    public ArrayList<LensData> getAllExcept(ArrayList<String> blacklist) {
        createIfNotExisting();
        Logger.log("Getting all lenses from database");

        if (!requiresUpdate) {
            Logger.log("Using lens cache");
            return excludedLensCache;
        }

        String strBlacklist = formatExclusionList(blacklist);
        String query = "select * from " + LensEntry.TABLE_NAME +
                " where " + LensEntry.COLUMN_NAME_MCODE + " not in (" + strBlacklist + ")";

        Logger.log("Performing query: " + query);

        Cursor cursor = writeableDatabase.rawQuery(query, null);

        Logger.log("Query size: " + cursor.getCount());
        ArrayList<LensData> lensDataList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Logger.log("Looping cursor result");
                LensData lensData = getLensFromCursor(cursor);

                if (lensData == null)
                    continue;

                lensDataList.add(lensData);
                cursor.moveToNext();
            }

            Logger.log("Completed getting lenses");
        }

        cursor.close();
        excludedLensCache = new ArrayList<>(lensDataList);
        requiresUpdate = false;
        return lensDataList;
    }

    public int getActiveLensCount() {
        createIfNotExisting();
        Logger.log("Getting lens from database");

        String selection = LensEntry.COLUMN_NAME_ACTIVE + " = ?";
        String[] selectionArgs = {"1"};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        Logger.log("Performing query: " + selection + "1");

        Cursor cursor = writeableDatabase.query(
                LensEntry.TABLE_NAME,                     // The table to query
                fullProjection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        int count = cursor.getCount();
        cursor.close();
        Logger.log("Query count: " + count);

        return count;
    }

    public ArrayList<LensData> getAllLenses() {
        createIfNotExisting();
        if (!requiresUpdate) {
            Logger.log("Using lens cache");
            return lensCache;
        }

        Logger.log("Getting all lenses from database");
        Cursor cursor = writeableDatabase.rawQuery("select * from " + LensEntry.TABLE_NAME, null);

        Logger.log("Query size: " + cursor.getCount());
        ArrayList<LensData> lensList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Logger.log("Looping cursor result");
                LensData lensData = getLensFromCursor(cursor);

                if (lensData == null)
                    continue;

                lensList.add(lensData);
                cursor.moveToNext();
            }
            Logger.log("Completed getting lenses");
        }

        cursor.close();

        lensCache = new ArrayList<>(lensList);
        requiresUpdate = false;
        return lensList;
    }

    public void deleteLens(String mCode) {
        createIfNotExisting();
        String selection = LensEntry.COLUMN_NAME_MCODE + " LIKE ?";
        String[] selectionArgs = {mCode};

        int rowsDeleted = writeableDatabase.delete(LensEntry.TABLE_NAME, selection, selectionArgs);

        if (rowsDeleted > 0)
            requiresUpdate = true;
    }

    public void replaceLens(LensData lensData) {
        createIfNotExisting();
        deleteLens(lensData.mCode);
        insertLens(lensData);
    }

    public void updateLens(String mCode, ContentValues values) {
        createIfNotExisting();
// Which row to update, based on the title
        String selection = LensEntry.COLUMN_NAME_MCODE + " = ?";
        String[] selectionArgs = {mCode};

        int rowsUpdated = writeableDatabase.update(
                LensEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        if (rowsUpdated > 0)
            requiresUpdate = true;
    }

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
}
