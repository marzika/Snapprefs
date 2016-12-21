package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.marz.snapprefs.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

class CachedDatabaseHandler extends CoreDatabaseHandler {
    private HashMap<String, Object> objectCache = new HashMap<>();

    CachedDatabaseHandler(Context context, String databaseName, String[] entries, int DATABASE_VERSION) {
        super(context, databaseName, entries, DATABASE_VERSION);
    }

    public boolean containsObject(String tableName, String columnName, String[] selectionArgs) {
        String key = String.format("%s%s%s%s", "containsObject", tableName, columnName, Arrays.toString(selectionArgs));

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return (boolean) cachedResult;
        }

        boolean result = super.containsObject(tableName, columnName, selectionArgs);
        objectCache.put(key, result);
        return result;
    }

    public ContentValues getContent(String tableName, String columnName, String[] selectionArgs,
                                    String sortOrder, String[] projection) {
        String key = String.format("%s%s%s%s%s%s", "getContent", tableName, columnName, Arrays.toString(selectionArgs), sortOrder, Arrays.toString(projection));

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return (ContentValues) cachedResult;
        }

        ContentValues results = super.getContent(tableName, columnName, selectionArgs, sortOrder, projection);
        objectCache.put(key, results);
        return results;
    }

    public Object getBuiltContent(String tableName, String columnName, String[] selectionArgs,
                                  String sortOrder, String[] projection, CallbackHandler callbackHandler) {
        String key = String.format("%s%s%s%s%s%s%s", "getBuiltContent", tableName, columnName, Arrays.toString(selectionArgs), sortOrder, Arrays.toString(projection), callbackHandler.toString);

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return cachedResult;
        }

        Object results = super.getBuiltContent(tableName, columnName, selectionArgs, sortOrder, projection, callbackHandler);
        objectCache.put(key, results);
        return results;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ContentValues> getAllContentExcept(String tableName, String columnName, String[] projection,
                                                        ArrayList<String> blacklist) {
        String key = String.format("%s%s%s%s%s", "getAllContentExcept", tableName, columnName, Arrays.toString(projection), blacklist);

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return (ArrayList<ContentValues>) cachedResult;
        }

        Object results = super.getAllContentExcept(tableName, columnName, projection, blacklist);
        objectCache.put(key, results);
        return (ArrayList<ContentValues>) results;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ContentValues> getAllContent(String tableName, String[] projection) {
        String key = String.format("%s%s%s", "getAllContent", tableName, Arrays.toString(projection));

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return (ArrayList<ContentValues>) cachedResult;
        }

        ArrayList<ContentValues> results = super.getAllContent(tableName, projection);
        objectCache.put(key, results);
        return results;
    }

    public Object getAllBuiltObjectsExcept(String tableName, String columnName, ArrayList<String> blacklist,
                                           CallbackHandler callbackHandler) {
        return getAllBuiltObjectsExcept(tableName, columnName, null, blacklist, callbackHandler);
    }

    public Object getAllBuiltObjectsExcept(String tableName, String columnName, String orderBy, ArrayList<String> blacklist,
                                           CallbackHandler callbackHandler) {
        String key = String.format("%s%s%s%s%s%s", "getAllBuiltObjectsExcept", tableName, columnName, orderBy, blacklist, callbackHandler.toString);

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return cachedResult;
        }

        Object results = super.getAllBuiltObjectsExcept(tableName, columnName, orderBy, blacklist, callbackHandler);

        objectCache.put(key, results);
        return results;
    }

    public Object getAllBuiltObjects(String tableName, CallbackHandler callbackHandler) {
        return getAllBuiltObjects(tableName, null, null, callbackHandler);
    }

    public Object getAllBuiltObjects(String tableName, String where, String orderBy, CallbackHandler callbackHandler) {
        String key = String.format("%s%s%s%s", "getAllBuiltObjects", tableName, where, callbackHandler.toString);

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null) {
                Logger.log("Getting cached results", Logger.LogType.DATABASE);
                return cachedResult;
            }
        }

        Object results = super.getAllBuiltObjects(tableName, where, orderBy, callbackHandler);
        objectCache.put(key, results);
        return results;
    }

    public Object performQueryForBuiltObjects(String tableName, String selection, String[] selectionArgs,
                                              String[] projection, String sortOrder, CallbackHandler callbackHandler) {
        String key = String.format("%s%s%s%s%s%s%s", "performQueryForBuiltObjects", tableName, selection, Arrays.toString(selectionArgs),
                Arrays.toString(projection), sortOrder, callbackHandler.toString);

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return cachedResult;
        }


        Object results = super.performQueryForBuiltObjects(tableName, selection, selectionArgs, projection, sortOrder, callbackHandler);
        objectCache.put(key, results);
        return results;
    }

    public long insertValues(String tableName, ContentValues values) {
        return shouldInvalidateCache(super.insertValues(tableName, values));
    }

    public int deleteObject(String tableName, String columnName, String[] selectionArgs) {
        return (int) shouldInvalidateCache(super.deleteObject(tableName, columnName, selectionArgs));
    }

    public int updateObject(String tableName, String columnName, String[] selectionArgs,
                            ContentValues values) {
        return (int) shouldInvalidateCache(super.updateObject(tableName, columnName, selectionArgs, values));
    }

    public ContentValues getValuesFromCursor(Cursor cursor, String[] arrProjection) {
        String key = String.format("%s%s", "getValuesFromCursor", Arrays.toString(arrProjection));

        if (!objectCache.isEmpty()) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null && cachedResult instanceof  ContentValues)
                return (ContentValues) cachedResult;
        }


        ContentValues results = super.getValuesFromCursor(cursor, arrProjection);
        objectCache.put(key, results);
        return results;
    }

    void invalidateCache() {
        objectCache.clear();
        Logger.log("Cache invalidated", Logger.LogType.DATABASE);
    }

    private long shouldInvalidateCache(long rowsAffected) {
        Logger.log("Cache rows affected: " + rowsAffected, Logger.LogType.DATABASE);

        if (rowsAffected != 0)
            invalidateCache();

        return rowsAffected;
    }
}
