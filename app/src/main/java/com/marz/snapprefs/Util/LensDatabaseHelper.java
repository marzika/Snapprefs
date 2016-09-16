package com.marz.snapprefs.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.marz.snapprefs.HookMethods;
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
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = Preferences.mSavePath + "/Lenses.db";

    public static final String[] fullProjection = {
            LensEntry.COLUMN_NAME_MCODE,
            LensEntry.COLUMN_NAME_GPLAYID,
            LensEntry.COLUMN_NAME_MHINTID,
            LensEntry.COLUMN_NAME_MICONLINK,
            LensEntry.COLUMN_NAME_MID,
            LensEntry.COLUMN_NAME_MLENSLINK,
            LensEntry.COLUMN_NAME_MSIGNATURE,
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
                    LensEntry.COLUMN_NAME_MSIGNATURE + TEXT_TYPE + " )";

    public LensDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String formatArrayList(ArrayList<String> list) {
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

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertLens(LensData lensData) {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return;
        }
        Logger.log("Inserting new lens: " + lensData.mCode);

        SQLiteDatabase db = HookMethods.lensDBHelper.getWritableDatabase();

        long newRowId = db.insert(LensEntry.TABLE_NAME, null, lensData.getContent());
        Logger.log("New Lens Row ID: " + newRowId);
    }

    public boolean containsLens(String mCode) {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return false;
        }
        Logger.log("Getting lens from database");

        SQLiteDatabase db = HookMethods.lensDBHelper.getReadableDatabase();

        String selection = LensEntry.COLUMN_NAME_MCODE + " = ?";
        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        Logger.log("Performing query: " + selection + mCode);

        Cursor cursor = db.query(
                LensEntry.TABLE_NAME,                     // The table to query
                fullProjection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        Logger.log("Query count: " + cursor.getCount());

        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    public LensData getLens(String mCode) {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return null;
        }
        Logger.log("Getting lens from database");

        SQLiteDatabase db = HookMethods.lensDBHelper.getReadableDatabase();

        String selection = LensEntry.COLUMN_NAME_MCODE + " = ?";
        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        Logger.log("Performing query: " + selection + mCode);

        Cursor cursor = db.query(
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

        LensData lensData = new LensData();

        try {
            lensData.mCode = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MCODE));
            lensData.mGplayIapId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_GPLAYID));
            lensData.mHintId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MHINTID));
            lensData.mIconLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MICONLINK));
            lensData.mId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MID));
            lensData.mLensLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MLENSLINK));
            lensData.mSignature = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MSIGNATURE));
        } catch (IllegalArgumentException e) {
            Logger.log("Issue querying database", e);
            cursor.close();
            return null;
        }

        cursor.close();
        Logger.log("Queried database to get lens: " + lensData.mCode);
        return lensData;
    }

    public ArrayList<LensData> getAllExcept(ArrayList<String> blacklist) {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return null;
        }

        Logger.log("Getting all lenses from database");

        String strBlacklist = formatArrayList(blacklist);
        String query = "select * from " + LensEntry.TABLE_NAME +
                " where " + LensEntry.COLUMN_NAME_MCODE + " not in (" + strBlacklist + ")";

        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        Logger.log("Performing query: " + query);

        SQLiteDatabase db = HookMethods.lensDBHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        /*Cursor cursor = db.query(
                LensEntry.TABLE_NAME,                     // The table to query
                fullProjection,                               // The columns to return
                query,                                // The columns for the WHERE clause
                blacklist,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );*/

        Logger.log("Query size: " + cursor.getCount());
        ArrayList<LensData> lensDataHashMap = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Logger.log("Looping cursor result");
                LensData lensData = new LensData();

                try {
                    lensData.mCode = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MCODE));
                    lensData.mGplayIapId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_GPLAYID));
                    lensData.mHintId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MHINTID));
                    lensData.mIconLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MICONLINK));
                    lensData.mId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MID));
                    lensData.mLensLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MLENSLINK));
                    lensData.mSignature = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MSIGNATURE));
                    Logger.log("Queried database for lens: " + lensData.mCode);
                } catch (IllegalArgumentException e) {
                    Logger.log("Issue querying database", e);
                    continue;
                }

                lensDataHashMap.add(lensData);
                cursor.moveToNext();
            }

            Logger.log("Completed getting lenses");
        }

        cursor.close();

        return lensDataHashMap;
    }

    public ArrayList<LensData> getAllLenses() {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return null;
        }

        Logger.log("Getting all lenses from database");
        SQLiteDatabase db = HookMethods.lensDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + LensEntry.TABLE_NAME, null);

        Logger.log("Query size: " + cursor.getCount());
        ArrayList<LensData> lensList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Logger.log("Looping cursor result");
                LensData lensData = new LensData();

                try {
                    lensData.mCode = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MCODE));
                    lensData.mGplayIapId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_GPLAYID));
                    lensData.mHintId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MHINTID));
                    lensData.mIconLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MICONLINK));
                    lensData.mId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MID));
                    lensData.mLensLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MLENSLINK));
                    lensData.mSignature = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MSIGNATURE));
                    Logger.log("Queried database for lens: " + lensData.mCode);
                } catch (IllegalArgumentException e) {
                    Logger.log("Issue querying database", e);
                    continue;
                }

                lensList.add(lensData);
                cursor.moveToNext();
            }
            db.close();
            Logger.log("Completed getting lenses");
        }

        cursor.close();

        return lensList;
    }

    public void deleteLens(String mCode) {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return;
        }

        SQLiteDatabase db = HookMethods.lensDBHelper.getWritableDatabase();
        String selection = LensEntry.COLUMN_NAME_MCODE + " LIKE ?";
        String[] selectionArgs = {mCode};

        db.delete(LensEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void replaceLens(LensData lensData) {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return;
        }

        deleteLens(lensData.mCode);
        insertLens(lensData);
    }

    public void updateLens(String strTitle, String strValue) {
        if (HookMethods.lensDBHelper == null) {
            Logger.log("No database initialised");
            return;
        }

        SQLiteDatabase db = HookMethods.lensDBHelper.getReadableDatabase();

// New value for one column
        ContentValues values = new ContentValues();
        values.put(strTitle, strValue);

// Which row to update, based on the title
        String selection = strTitle + " = ?";
        String[] selectionArgs = {strValue};

        int count = db.update(
                LensEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }
}
