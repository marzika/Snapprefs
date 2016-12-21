package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Util.LensData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.marz.snapprefs.Databases.CoreDatabaseHandler.CallbackHandler.getCallback;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class LensDatabaseHelper extends CachedDatabaseHandler {
    private static final int DATABASE_VERSION = 7;
    private static final String[] fullProjection = {
            LensEntry.COLUMN_NAME_MCODE,
            LensEntry.COLUMN_NAME_GPLAYID,
            LensEntry.COLUMN_NAME_TYPE,
            LensEntry.COLUMN_NAME_MHINTID,
            LensEntry.COLUMN_NAME_MICONLINK,
            LensEntry.COLUMN_NAME_MID,
            LensEntry.COLUMN_NAME_MLENSLINK,
            LensEntry.COLUMN_NAME_MSIGNATURE,
            LensEntry.COLUMN_NAME_ACTIVE,
            LensEntry.COLUMN_NAME_SEL_TIME,
            LensEntry.COLUMN_NAME_LENS_NAME
    };
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String DEF_SEL_TIME_VAL = "2000000000";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LensEntry.TABLE_NAME;
    private static final String[] SQL_CREATE_ENTRIES = {
            "CREATE TABLE " + LensEntry.TABLE_NAME + " (" +
                    LensEntry.COLUMN_NAME_MCODE + TEXT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    LensEntry.COLUMN_NAME_GPLAYID + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_TYPE + TEXT_TYPE + " DEFAULT 'SCHEDULED'," +
                    LensEntry.COLUMN_NAME_MHINTID + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MICONLINK + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MID + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MLENSLINK + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_MSIGNATURE + TEXT_TYPE + COMMA_SEP +
                    LensEntry.COLUMN_NAME_ACTIVE + INT_TYPE + " DEFAULT 0," +
                    LensEntry.COLUMN_NAME_SEL_TIME + INT_TYPE + " DEFAULT " + DEF_SEL_TIME_VAL + COMMA_SEP +
                    LensEntry.COLUMN_NAME_LENS_NAME + TEXT_TYPE + " )"};
    private static String DEFAULT_DB_NAME = Preferences.getContentPath() + "/Lenses.db";
    private final String DATABASE_NAME;

    public LensDatabaseHelper(Context context) {
        super(context, DEFAULT_DB_NAME, SQL_CREATE_ENTRIES, DATABASE_VERSION);
        this.DATABASE_NAME = DEFAULT_DB_NAME;
    }

    public LensDatabaseHelper(Context context, String DATABASE_NAME) {
        super(context, DATABASE_NAME, SQL_CREATE_ENTRIES, DATABASE_VERSION);
        this.DATABASE_NAME = DATABASE_NAME;
    }

    public static int mergeLensDatabases(LensDatabaseHelper masterDB, LensDatabaseHelper slaveDB) {
        LinkedHashMap<String, Object> slaveMap = (LinkedHashMap<String, Object>) slaveDB.getAllLenses();

        int mergedLenses = 0;
        for (Object lens : slaveMap.values()) {
            try {
                LensData lensData = (LensData) lens;

                if (!masterDB.containsLens(lensData.mCode)) {
                    masterDB.insertLens(lensData);
                    mergedLenses++;
                }
            } catch (Exception e) {
                Logger.log("Error merging databases", e, LogType.DATABASE);
            }
        }

        slaveDB.close();

        return mergedLenses;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.log(String.format("Upgrading LensDB from v%s to v%s", oldVersion, newVersion), LogType.DATABASE.setForced());

        if (oldVersion == 4) {
            Logger.log("Performing deep db upgrade", LogType.DATABASE);
            onUpgrade(db, 1, newVersion);
            return;
        }

        if (oldVersion <= 1 && !super.checkIfColumnExists(db, LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_ACTIVE)) {
            try {
                db.execSQL("ALTER TABLE " + LensEntry.TABLE_NAME +
                        " ADD COLUMN " + LensEntry.COLUMN_NAME_ACTIVE + " INTEGER DEFAULT 0");

                Logger.log(String.format("Added column %s to table %s", LensEntry.COLUMN_NAME_ACTIVE, LensEntry.TABLE_NAME), LogType.DATABASE);
            } catch (Exception e) {
                Logger.log("Error altering table", e, LogType.DATABASE);
            }
        }

        if (oldVersion <= 2 && !super.checkIfColumnExists(db, LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_SEL_TIME)) {
            try {
                db.execSQL("ALTER TABLE " + LensEntry.TABLE_NAME +
                        " ADD COLUMN " + LensEntry.COLUMN_NAME_SEL_TIME + " INTEGER DEFAULT " + DEF_SEL_TIME_VAL);

                Logger.log(String.format("Added column %s to table %s", LensEntry.COLUMN_NAME_SEL_TIME, LensEntry.TABLE_NAME), LogType.DATABASE);
            } catch (Exception e) {
                Logger.log("Error altering table", e, LogType.DATABASE);
            }
        }

        if (oldVersion <= 3 && !super.checkIfColumnExists(db, LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_TYPE)) {
            try {
                db.execSQL("ALTER TABLE " + LensEntry.TABLE_NAME +
                        " ADD COLUMN " + LensEntry.COLUMN_NAME_TYPE + " TEXT DEFAULT 'SCHEDULED'");

                Logger.log(String.format("Added column %s to table %s", LensEntry.COLUMN_NAME_TYPE, LensEntry.TABLE_NAME), LogType.DATABASE);
            } catch (Exception e) {
                Logger.log("Error altering table", e, LogType.DATABASE);
            }
        }

        if (oldVersion <= 6 && !super.checkIfColumnExists(db, LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_LENS_NAME)) {
            try {
                db.execSQL("ALTER TABLE " + LensEntry.TABLE_NAME +
                        " ADD COLUMN " + LensEntry.COLUMN_NAME_LENS_NAME);

                Logger.log(String.format("Added column %s to table %s", LensEntry.COLUMN_NAME_LENS_NAME, LensEntry.TABLE_NAME), LogType.DATABASE);
            } catch (Exception e) {
                Logger.log("Error altering table", e, LogType.DATABASE);
            }
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
        Logger.log("Inserting new lens: " + lensData.mCode, LogType.DATABASE);
        long newRowId = super.insertValues(LensEntry.TABLE_NAME, lensData.getContent());
        Logger.log("New Lens Row ID: " + newRowId, LogType.DATABASE);
    }

    public boolean containsLens(String mCode) {
        Logger.log("Getting lens from database", LogType.DATABASE);

        String[] selectionArgs = {mCode};

        return super.containsObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs);
    }

    public boolean toggleLensActiveState(String mCode) throws Exception {
        Logger.log("Toggling lens state", LogType.DATABASE);
        boolean activeState = !getLensActiveState(mCode);
        Logger.log("Current state: " + activeState, LogType.DATABASE);

        ContentValues values = new ContentValues();
        values.put(LensEntry.COLUMN_NAME_ACTIVE, activeState ? 1 : 0);
        values.put(LensEntry.COLUMN_NAME_SEL_TIME, (activeState ? Long.toString(System.currentTimeMillis()) : Integer.toString(2000000000)));

        String[] selectionArgs = {mCode};

        super.updateObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, values);

        return activeState;
    }

    private boolean getLensActiveState(String mCode) throws Exception {
        Logger.log("Getting lens from database", LogType.DATABASE);

        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        String[] projection = {LensEntry.COLUMN_NAME_ACTIVE};
        Logger.log("Performing query: " + LensEntry.COLUMN_NAME_MCODE + mCode, LogType.DATABASE);

        ContentValues values = super.getContent(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, sortOrder, projection);
        int activeState = (int) values.get(LensEntry.COLUMN_NAME_ACTIVE);
        return activeState != 0;
    }

    public int getActiveLensCount() {
        //Logger.log("Getting lens from database");

        String[] selectionArgs = {"1"};

        //Logger.log("Query count: " + count);

        return super.getCount(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_ACTIVE, selectionArgs, fullProjection);
    }

    public void setActiveStateOfAllLenses(boolean newState) {

        getDatabase().execSQL("UPDATE " + LensDatabaseHelper.LensEntry.TABLE_NAME + " SET " +
                LensDatabaseHelper.LensEntry.COLUMN_NAME_ACTIVE + "=" + (newState ? "1" : "0"));

        super.invalidateCache();
    }

    public LensData getLens(String mCode) {
        //Logger.log("Getting lens from database");

        String[] selectionArgs = {mCode};
        String sortOrder =
                LensEntry.COLUMN_NAME_MCODE + " DESC";

        CallbackHandler callback = getCallback(this, "getLensFromCursor", LensDatabaseHelper.class, Cursor.class);

        //Logger.log("Queried database to get lens: " + lensData.mCode);
        return (LensData) super.getBuiltContent(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE,
                selectionArgs, sortOrder, fullProjection, callback);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllExcept(ArrayList<String> blacklist) {
        CallbackHandler callback = getCallback(this, "getAllLensesFromCursor", Cursor.class);

        String orderBy = Preferences.getBool(Preferences.Prefs.LENSES_SORT_BY_SEL) ?
                LensEntry.COLUMN_NAME_SEL_TIME + " ASC" : null;

        return (Map<String, Object>) super.getAllBuiltObjectsExcept(LensEntry.TABLE_NAME,
                LensEntry.COLUMN_NAME_MCODE, orderBy, blacklist, callback);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllOfType(LensData.LensType type) {
        CallbackHandler callback = getCallback(this, "getAllLensesFromCursor", Cursor.class);

        String orderBy = Preferences.getBool(Preferences.Prefs.LENSES_SORT_BY_SEL) ?
                LensEntry.COLUMN_NAME_SEL_TIME + " ASC" : null;

        return (Map<String, Object>) super.getAllBuiltObjects(
                LensEntry.TABLE_NAME,
                LensEntry.COLUMN_NAME_TYPE + " = '" + type + "'",
                orderBy,
                callback);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllWithPartial(String partialCode) {
        CallbackHandler callback = getCallback(this, "getAllLensesFromCursor", Cursor.class);

        String orderBy = Preferences.getBool(Preferences.Prefs.LENSES_SORT_BY_SEL) ?
                LensEntry.COLUMN_NAME_SEL_TIME + " ASC" : null;

        return (Map<String, Object>) super.getAllBuiltObjects(
                LensEntry.TABLE_NAME,
                LensEntry.COLUMN_NAME_MCODE + " LIKE '%" + partialCode + "%' OR " +
                LensEntry.COLUMN_NAME_LENS_NAME + " LIKE '%" + partialCode + "%'",
                orderBy,
                callback);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllActive() {
        CallbackHandler callback = getCallback(this, "getAllLensesFromCursor", Cursor.class);

        String selection = LensEntry.COLUMN_NAME_ACTIVE + " = ?";
        String[] selectionArgs = {"1"};

        return (Map<String, Object>) super.performQueryForBuiltObjects(LensEntry.TABLE_NAME,
                selection, selectionArgs, fullProjection, null, callback);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllLenses() {
        Logger.log("Getting all lenses from database", LogType.DATABASE);
        CallbackHandler callback = getCallback(this, "getAllLensesFromCursor", Cursor.class);

        String orderBy = Preferences.getBool(Preferences.Prefs.LENSES_SORT_BY_SEL) ?
                LensEntry.COLUMN_NAME_SEL_TIME + " ASC" : null;

        return (Map<String, Object>) super.getAllBuiltObjects(LensEntry.TABLE_NAME, null, orderBy, callback);
    }

    public boolean deleteLens(String mCode) {
        String[] selectionArgs = {mCode};

        int rowsAffected = super.deleteObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs);
        Logger.log("Removed " + rowsAffected + " lenses", LogType.DATABASE);
        return rowsAffected > 0;
    }

    public boolean updateLens(String mCode, ContentValues values) {
// Which row to update, based on the title
        String[] selectionArgs = {mCode};

        return super.updateObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, values) > 0;
    }

    public boolean updateLenses(String[] selectionArgs, ContentValues values) {
        return super.updateObject(LensEntry.TABLE_NAME, LensEntry.COLUMN_NAME_MCODE, selectionArgs, values) > 0;
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     *
     * @param cursor
     * @return lensDataList
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, LensData> getAllLensesFromCursor(Cursor cursor) {
        Map<String, LensData> lensDataList = new LinkedHashMap<>();

        while (!cursor.isAfterLast()) {
            LensData lensData = getLensFromCursor(cursor);

            if (lensData == null) {
                Logger.log("Null lensdata pulled", LogType.DATABASE);
                cursor.moveToNext();
                continue;
            }

            lensDataList.put(lensData.mCode, lensData);
            cursor.moveToNext();
        }

        return lensDataList;
    }

    /**
     * DO NOT REMOVE - It is used as a callback from the core handler
     *
     * @param cursor
     * @return lensData
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public LensData getLensFromCursor(Cursor cursor) {
        LensData lensData = new LensData();

        try {
            lensData.mCode = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MCODE));
            //lensData.mGplayIapId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_GPLAYID));
            lensData.mHintId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MHINTID));
            lensData.mIconLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MICONLINK));
            lensData.mId = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MID));
            lensData.mLensLink = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MLENSLINK));
            lensData.mSignature = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_MSIGNATURE));
            lensData.name = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_LENS_NAME));
            short activeState = cursor.getShort(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_ACTIVE));
            lensData.mActive = activeState != 0;
            lensData.selTime = cursor.getInt(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_SEL_TIME));

            String strLensType = cursor.getString(cursor.getColumnIndexOrThrow(LensEntry.COLUMN_NAME_TYPE));

            boolean hasAssignedType = false;
            if (strLensType != null) {
                try {
                    lensData.mType = LensData.LensType.valueOf(strLensType);
                    hasAssignedType = true;
                } catch (Exception e) {
                    Logger.log("Unknown Lens type: " + strLensType, LogType.DATABASE);
                }
            }

            if (!hasAssignedType)
                lensData.mType = LensData.LensType.SCHEDULED;

            //Logger.log("Queried database for lens: " + lensData.mCode + " Active: " + lensData.mActive);
        } catch (IllegalArgumentException e) {
            Logger.log("Issue querying database", e, LogType.DATABASE);
            return null;
        }

        return lensData;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class LensEntry implements BaseColumns {
        public static final String TABLE_NAME = "LensTable";
        public static final String COLUMN_NAME_MCODE = "mCode";
        public static final String COLUMN_NAME_TYPE = "mType";
        public static final String COLUMN_NAME_GPLAYID = "mGplayIapId";
        public static final String COLUMN_NAME_MHINTID = "mHintId";
        public static final String COLUMN_NAME_MICONLINK = "mIconLink";
        public static final String COLUMN_NAME_MID = "mId";
        public static final String COLUMN_NAME_MLENSLINK = "mLensLink";
        public static final String COLUMN_NAME_MSIGNATURE = "mSignature";
        public static final String COLUMN_NAME_ACTIVE = "mActiveState";
        public static final String COLUMN_NAME_SEL_TIME = "selDateTime";
        public static final String COLUMN_NAME_LENS_NAME = "name";
    }
}