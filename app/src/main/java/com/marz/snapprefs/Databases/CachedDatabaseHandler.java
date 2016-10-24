package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Used to reduce calls to the database by storing contents in a hashmap
 * Created by Andre on 24/10/2016.
 */

public class CachedDatabaseHandler extends CoreDatabaseHandler {
    private boolean isCacheValid = false;
    private HashMap<String, Object> objectCache = new HashMap<>();

    public CachedDatabaseHandler(Context context, String databaseName, String[] entries, int DATABASE_VERSION) {
        super(context, databaseName, entries, DATABASE_VERSION);
    }

    public long insertValues(String tableName, ContentValues values) {
        return shouldInvalidateCache(getDatabase().insert(tableName, null, values));
    }

    public boolean containsObject(String tableName, String columnName, String[] selectionArgs,
                                  String sortOrder, String[] projection) {
        String key = String.format("%s%s%s%s", "containsObject", tableName, columnName, Arrays.toString(selectionArgs));

        if (isCacheValid) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return (boolean) cachedResult;
        }

        boolean result = super.containsObject(tableName, columnName, selectionArgs, sortOrder, projection);
        objectCache.put(key, result);

        return result;
    }

    public ContentValues getContent(String tableName, String columnName, String[] selectionArgs,
                                    String sortOrder, String[] projection) {
        String key = String.format("%s%s%s%s%s%s", "getContent", tableName, columnName, Arrays.toString(selectionArgs), sortOrder, Arrays.toString(projection));

        if (isCacheValid) {
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

        if (isCacheValid) {
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

        if (isCacheValid) {
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

        if (isCacheValid) {
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

        if (isCacheValid) {
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

        if (isCacheValid) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return cachedResult;
        }

        Object results = super.getAllBuiltObjects(tableName, where, orderBy, callbackHandler);
        objectCache.put(key, results);
        return results;
    }

    public Object performQueryForBuiltObjects(String tableName, String selection, String[] selectionArgs,
                                              String[] projection, String sortOrder, CallbackHandler callbackHandler) {
        String key = String.format("%s%s%s%s%s%s%s", "performQueryForBuiltObjects", tableName, selection, Arrays.toString(selectionArgs),
                Arrays.toString(projection), sortOrder, callbackHandler.toString);

        if (isCacheValid) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null)
                return cachedResult;
        }


        Object results = super.performQueryForBuiltObjects(tableName, selection, selectionArgs, projection, sortOrder, callbackHandler);
        objectCache.put(key, results);
        return results;
    }

    public int deleteObject(String tableName, String columnName, String[] selectionArgs) {
        String selection = columnName + " = ?";

        return (int) shouldInvalidateCache(super.deleteObject(tableName, selection, selectionArgs));
    }

    public int updateObject(String tableName, String columnName, String[] selectionArgs,
                             ContentValues values) {
        return (int) shouldInvalidateCache(super.updateObject(tableName, columnName, selectionArgs, values));
    }

    public ContentValues getValuesFromCursor(Cursor cursor, String[] arrProjection) {
        String key = String.format("%s%s", "getValuesFromCursor", Arrays.toString(arrProjection));

        if (isCacheValid) {
            Object cachedResult = objectCache.get(key);

            if (cachedResult != null && cachedResult instanceof  ContentValues)
                return (ContentValues) cachedResult;
        }


        ContentValues results = super.getValuesFromCursor(cursor, arrProjection);
        objectCache.put(key, results);
        return results;
    }

    public void invalidateCache() {
        objectCache.clear();
    }

    private long shouldInvalidateCache(long rowsAffected) {
        if (rowsAffected > 0)
            objectCache.clear();

        return rowsAffected;
    }
}
