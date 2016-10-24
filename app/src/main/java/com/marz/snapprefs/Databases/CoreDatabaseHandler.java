package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.marz.snapprefs.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static com.marz.snapprefs.Databases.CoreDatabaseHandler.DBUtils.formatExclusionList;

/**
 * Created by Andre on 23/09/2016.
 */

public class CoreDatabaseHandler extends SQLiteOpenHelper {
    public static String DATABASE_NAME;
    private static String[] SQL_CREATE_ENTRIES;

    private SQLiteDatabase writeableDatabase;
    private ArrayList<ContentValues> contentCache = new ArrayList<>();
    private boolean contentCacheNeedsUpdate = true;
    private ArrayList<ContentValues> excludedContentCache = new ArrayList<>();
    private boolean excludedContentCacheNeedsUpdate = true;
    private ArrayList<Object> objectCache = new ArrayList<>();
    private boolean objectCacheNeedsUpdate = true;
    private ArrayList<Object> excludedObjectCache = new ArrayList<>();
    private boolean excludedObjectCacheNeedsUpdate = true;

    public CoreDatabaseHandler(Context context, String databaseName, String[] entries, int DATABASE_VERSION) {
        super(context, databaseName, null, DATABASE_VERSION);
        DATABASE_NAME = databaseName;
        SQL_CREATE_ENTRIES = entries;
    }

    public SQLiteDatabase getDatabase() {
        createIfNotExisting();
        return writeableDatabase;
    }

    public void createIfNotExisting() {
        if (writeableDatabase == null || !writeableDatabase.isOpen())
            writeableDatabase = this.getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        for (String entry : SQL_CREATE_ENTRIES)
            db.execSQL(entry);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long getRowCount(String tableName) {
        return DatabaseUtils.queryNumEntries(getDatabase(), tableName);
    }

    public long insertValues(String tableName, ContentValues values) {
        long newRowID = getDatabase().insert(tableName, null, values);
        invalidateCache();

        return newRowID;
    }

    public boolean containsObject(String tableName, String columnName, String[] selectionArgs,
                                  String sortOrder, String[] projection) {
        Logger.log("Checking if object is in database");

        String selection = columnName + " = ?";
        Cursor cursor = getDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        Logger.log("Query count: " + cursor.getCount());

        cursor.close();

        return cursor.getCount() != 0;
    }

    public int getCount(String tableName, String columnName, String[] selectionArgs, String[] projection) {
        String selection = columnName + " = ?";

        Cursor cursor = getDatabase().query(
                tableName,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        int count = cursor.getCount();
        cursor.close();
        Logger.log("Query count: " + count);
        return count;
    }

    public ContentValues getContent(String tableName, String columnName, String[] selectionArgs,
                                    String sortOrder, String[] projection) {
        Logger.log("Getting lens from database");

        String selection = columnName + " = ?";
        Cursor cursor = getDatabase().query(
                tableName,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        Logger.log("Query count: " + cursor.getCount());

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        ContentValues content = getValuesFromCursor(cursor, projection);
        cursor.close();

        if (content == null) {
            Logger.log("Null content");
            return null;
        }

        Logger.log("Queried database to get content: " + content.size());
        return content;
    }

    public Object getBuiltContent(String tableName, String columnName, String[] selectionArgs,
                                  String sortOrder, String[] projection, CallbackHandler callbackHandler) {
        Logger.log("Getting lens from database");

        String selection = columnName + " = ?";
        Cursor cursor = getDatabase().query(
                tableName,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        callbackHandler.addParams(cursor);

        Logger.log("Query count: " + cursor.getCount());

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        Object invocationResponse;

        try {
            Logger.log("Performing invocation of builder method: " + callbackHandler.method.getName() + "|" + callbackHandler.parameters);
            invocationResponse = callbackHandler.method.invoke(callbackHandler.caller, callbackHandler.parameters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            cursor.close();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            cursor.close();
            return null;
        }

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName());
            return null;
        }

        cursor.close();
        return invocationResponse;
    }

    public ArrayList<ContentValues> getAllContentExcept(String tableName, String columnName, String[] projection,
                                                        ArrayList<String> blacklist) {
        Logger.log("Getting all content from database");

        if (!excludedContentCacheNeedsUpdate && excludedObjectCache.size() > 0) {
            Logger.log("Using query cache");
            return excludedContentCache;
        }

        String strBlacklist = formatExclusionList(blacklist);
        String query = "SELECT * FROM " + tableName +
                " WHERE " + columnName + " NOT IN (" + strBlacklist + ")";

        Logger.log("Performing query: " + query);

        Cursor cursor = getDatabase().rawQuery(query, null);

        Logger.log("Query size: " + cursor.getCount());

        ArrayList<ContentValues> content = new ArrayList<>();

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        while (!cursor.isAfterLast()) {
            Logger.log("Looping cursor result");
            ContentValues value = getValuesFromCursor(cursor, projection);

            if (value.size() > 0)
                content.add(value);

            cursor.moveToNext();
        }

        Logger.log("Completed getting lenses");


        cursor.close();
        excludedContentCache = new ArrayList<>(content);
        excludedContentCacheNeedsUpdate = false;
        return content;
    }

    public ArrayList<ContentValues> getAllContent(String tableName, String[] projection) {

        if (!contentCacheNeedsUpdate && contentCache.size() > 0) {
            Logger.log("Using cached content");
            return contentCache;
        }
        Logger.log("Getting all lenses from database");
        Cursor cursor = getDatabase().rawQuery("select * from " + tableName, null);

        Logger.log("Query size: " + cursor.getCount());

        ArrayList<ContentValues> contentList = new ArrayList<>();

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        while (!cursor.isAfterLast())
            contentList.add(this.getValuesFromCursor(cursor, projection));


        cursor.close();
        contentCache = new ArrayList<>(contentList);
        contentCacheNeedsUpdate = false;
        return contentList;
    }

    public ArrayList<Object> getAllBuiltObjectsExcept(String tableName, String columnName, ArrayList<String> blacklist,
                                                      CallbackHandler callbackHandler) {
        return getAllBuiltObjectsExcept(tableName, columnName, blacklist, callbackHandler, false);
    }

    public ArrayList<Object> getAllBuiltObjectsExcept(String tableName, String columnName, ArrayList<String> blacklist,
                                                      CallbackHandler callbackHandler, boolean bypassCache) {
        return getAllBuiltObjectsExcept(tableName, columnName, null, blacklist, callbackHandler, bypassCache);
    }

    public ArrayList<Object> getAllBuiltObjectsExcept(String tableName, String columnName, String orderBy, ArrayList<String> blacklist,
                                                      CallbackHandler callbackHandler) {
        return getAllBuiltObjectsExcept(tableName, columnName, orderBy, blacklist, callbackHandler, false);
    }

    public ArrayList<Object> getAllBuiltObjectsExcept(String tableName, String columnName, String orderBy, ArrayList<String> blacklist,
                                                      CallbackHandler callbackHandler, boolean bypassCache) {
        Logger.log("Getting all content from database");
        if (!excludedObjectCacheNeedsUpdate && !bypassCache) {
            Logger.log("Using query cache");
            return excludedObjectCache;
        }
        String strBlacklist = formatExclusionList(blacklist);
        String query = "select * from " + tableName +
                " where " + columnName + " not in (" + strBlacklist + ")" + (orderBy != null ? " ORDER BY " + orderBy : "");
        Logger.log("Performing query: " + query);
        Cursor cursor = getDatabase().rawQuery(query, null);
        callbackHandler.addParams(cursor);
        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }
        Logger.log("Query size: " + cursor.getCount());
        Object invocationResponse = invokeCallback(callbackHandler);

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName());
            return null;
        }
        cursor.close();
        excludedObjectCache = (ArrayList<Object>) invocationResponse;
        excludedObjectCacheNeedsUpdate = false;
        return (ArrayList<Object>) invocationResponse;
    }

    public Object getAllBuiltObjects(String tableName, CallbackHandler callbackHandler) {
        return getAllBuiltObjects(tableName, null, callbackHandler, false);
    }

    public Object getAllBuiltObjects(String tableName, CallbackHandler callbackHandler, boolean bypassCache) {
        return getAllBuiltObjects(tableName, null, callbackHandler, bypassCache);
    }

    public Object getAllBuiltObjects(String tableName, String where, CallbackHandler callbackHandler) {
        return getAllBuiltObjects(tableName, where, callbackHandler, false);
    }

    public Object getAllBuiltObjects(String tableName, String where, CallbackHandler callbackHandler, boolean bypassCache) {
        if (!objectCacheNeedsUpdate && !bypassCache) {
            Logger.log("Using lens cache");
            return objectCache;
        }

        Logger.log("Getting all lenses from database");
        Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + tableName + (where != null ? " WHERE " + where : ""), null);
        Logger.log("Query size: " + cursor.getCount());

        callbackHandler.addParams(cursor);

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        Object invocationResponse = invokeCallback(callbackHandler);

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName());
            return null;
        }

        cursor.close();

        objectCache = new ArrayList<>(invocationResponse);
        objectCacheNeedsUpdate = false;
        return invocationResponse;
    }

    public Object performQueryForBuiltObjects(String tableName, String selection, String[] selectionArgs,
                                                         String[] projection, String sortOrder, CallbackHandler callbackHandler) {
        Cursor cursor = getDatabase().query(
                tableName,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        callbackHandler.addParams(cursor);

        Logger.log("Query count: " + cursor.getCount());

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        Object invocationResponse = invokeCallback(callbackHandler);

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName());
            return null;
        }

        cursor.close();
        return invocationResponse;
    }

    public void deleteObject(String tableName, String columnName, String[] selectionArgs) {
        String selection = columnName + " = ?";

        int rowsDeleted = getDatabase().delete(tableName, selection, selectionArgs);

        if (rowsDeleted > 0)
            invalidateCache();
    }

    public void updateObject(String tableName, String columnName, String[] selectionArgs,
                             ContentValues values) {
        String selection = columnName + " = ?";

        int rowsUpdated = getDatabase().update(
                tableName,
                values,
                selection,
                selectionArgs);

        if (rowsUpdated > 0)
            invalidateCache();
    }

    public ContentValues getValuesFromCursor(Cursor cursor, String[] arrProjection) {
        ContentValues contentValues = new ContentValues();

        for (String projection : arrProjection) {
            int index;
            try {
                index = cursor.getColumnIndexOrThrow(projection);
            } catch (IllegalArgumentException e) {
                Logger.log("Error getting object from cursor", e);
                continue;
            }

            int type = cursor.getType(index);

            switch (type) {
                case Cursor.FIELD_TYPE_STRING:
                    contentValues.put(projection, cursor.getString(index));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    contentValues.put(projection, cursor.getInt(index));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    contentValues.put(projection, cursor.getFloat(index));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    contentValues.put(projection, cursor.getBlob(index));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    Logger.log("Tried to get null type from cursor");
                    break;
                default:
                    Logger.log("Unknown type passed to cursor");
            }
        }

        return contentValues;
    }

    public void invalidateCache() {
        excludedContentCacheNeedsUpdate = true;
        contentCacheNeedsUpdate = true;
        objectCacheNeedsUpdate = true;
        excludedObjectCacheNeedsUpdate = true;
    }

    private Object invokeCallback(CallbackHandler callbackHandler) {
        try {
            Logger.log("Performing invocation of builder method: " + callbackHandler.method.getName() + "|" + Arrays.toString(callbackHandler.parameters));
            return callbackHandler.method.invoke(callbackHandler.caller, callbackHandler.parameters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class DBUtils {
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
    }

    public static class CallbackHandler {
        Object caller;
        Method method;
        Object[] parameters;

        CallbackHandler(Object caller, Method method, Object... parameters) {
            this.caller = caller;
            this.method = method;
            this.parameters = parameters;
        }

        /**
         * Usage: getCallback("methodToCall", ParameterClassTypes...);
         *
         * @param methodName - The name of the method to call
         * @param classType  - The list of Classes called as the method parameters
         * @return CallbackHandler - The object holding the callback data
         */
        public static CallbackHandler getCallback(Class<?> clazz, String methodName, Class... classType) {
            try {
                Logger.log("Trying to build callback method");
                return new CallbackHandler(clazz, clazz.getMethod(methodName, classType));
            } catch (NoSuchMethodException e) {
                Logger.log("ERROR GETTING CALLBACK", e);
                return null;
            }
        }

        public Object[] addParams(Object... newParams) {
            int newParamLength = newParams.length;

            Object[] newParamList = new Object[parameters.length + newParamLength];

            System.arraycopy(parameters, 0, newParamList, 0, parameters.length);
            System.arraycopy(newParams, 0, newParamList, parameters.length, newParamLength);

            this.parameters = newParamList;

            for (Object param : parameters)
                Logger.log("New parameter: " + param.getClass().getCanonicalName());

            return newParamList;
        }

        public Cursor getCursorParam() {
            for (Object param : parameters) {
                if (param instanceof Cursor)
                    return (Cursor) param;
            }

            return null;
        }
    }
}