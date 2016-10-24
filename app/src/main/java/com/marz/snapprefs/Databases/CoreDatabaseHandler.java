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

class CoreDatabaseHandler extends SQLiteOpenHelper {
    private static String DATABASE_NAME;
    private static String[] SQL_CREATE_ENTRIES;

    private SQLiteDatabase writableDatabase;

    CoreDatabaseHandler(Context context, String databaseName, String[] entries, int DATABASE_VERSION) {
        super(context, databaseName, null, DATABASE_VERSION);
        DATABASE_NAME = databaseName;
        SQL_CREATE_ENTRIES = entries;
    }

    SQLiteDatabase getDatabase() {
        createIfNotExisting();
        return writableDatabase;
    }


    private void createIfNotExisting() {
        if (writableDatabase == null || !writableDatabase.isOpen())
            writableDatabase = this.getWritableDatabase();
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

    long getRowCount(String tableName) {
        return DatabaseUtils.queryNumEntries(getDatabase(), tableName);
    }

    public long insertValues(String tableName, ContentValues values) {
        return getDatabase().insert(tableName, null, values);
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

        Object invocationResponse = invokeCallback(callbackHandler);

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
        return content;
    }

    public ArrayList<ContentValues> getAllContent(String tableName, String[] projection) {
        Logger.log("Getting all lenses from database");
        Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + tableName, null);

        Logger.log("Query size: " + cursor.getCount());

        ArrayList<ContentValues> contentList = new ArrayList<>();

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        while (!cursor.isAfterLast())
            contentList.add(this.getValuesFromCursor(cursor, projection));

        return contentList;
    }

    public Object getAllBuiltObjectsExcept(String tableName, String columnName, ArrayList<String> blacklist,
                                           CallbackHandler callbackHandler) {
        return getAllBuiltObjectsExcept(tableName, columnName, null, blacklist, callbackHandler);
    }

    public Object getAllBuiltObjectsExcept(String tableName, String columnName, String orderBy, ArrayList<String> blacklist,
                                           CallbackHandler callbackHandler) {
        String strBlacklist = formatExclusionList(blacklist);

        String query = "SELECT * FROM " + tableName +
                " WHERE " + columnName + " NOT IN " + "(" + strBlacklist + ")" + (orderBy != null ? " ORDER BY " + orderBy : "");

        Logger.log("Performing query: " + query);
        Cursor cursor = getDatabase().rawQuery(query, null);
        callbackHandler.addParams(cursor);

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row");
            return null;
        }

        Logger.log("Query size: " + cursor.getCount());

        Object invocationResponse = invokeCallback(callbackHandler);

        cursor.close();

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName());
            return null;
        }

        return invocationResponse;
    }

    Object getAllBuiltObjects(String tableName, CallbackHandler callbackHandler) {
        return getAllBuiltObjects(tableName, null, null, callbackHandler);
    }

    Object getAllBuiltObjects(String tableName, String where, String orderBy, CallbackHandler callbackHandler) {
        Logger.log("Getting all lenses from database");
        Cursor cursor = getDatabase().rawQuery(
                "SELECT * FROM " + tableName +
                        (where != null ? " WHERE " + where : "") +
                        (orderBy != null ? " ORDER BY " + orderBy : ""), null);

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

    public int deleteObject(String tableName, String columnName, String[] selectionArgs) {
        String selection = columnName + " = ?";

        return getDatabase().delete(tableName, selection, selectionArgs);
    }

    public int updateObject(String tableName, String columnName, String[] selectionArgs,
                             ContentValues values) {
        String selection = columnName + " = ?";

        return getDatabase().update(
                tableName,
                values,
                selection,
                selectionArgs);
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

    @SuppressWarnings("TryWithIdenticalCatches")
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

    static class DBUtils {
        static String formatExclusionList(ArrayList<String> list) {
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

    static class CallbackHandler {
        public String toString;
        Object caller;
        Method method;
        Object[] parameters;

        CallbackHandler(Object caller, Method method, Object... parameters) {
            this.caller = caller;
            this.method = method;
            this.parameters = parameters;
            this.toString = caller.toString() + method.getName() + Arrays.toString(parameters);
        }

        /**
         * Usage: getCallback("methodToCall", ParameterClassTypes...);
         *
         * @param methodName - The name of the method to call
         * @param classType  - The list of Classes called as the method parameters
         * @return CallbackHandler - The object holding the callback data
         */
        static CallbackHandler getCallback(Object clazz, String methodName, Class<?>... classType) {
            try {
                Logger.log("Trying to build callback method");
                return new CallbackHandler(clazz, clazz.getClass().getMethod(methodName, classType));
            } catch (NoSuchMethodException e) {
                Logger.log("ERROR GETTING CALLBACK", e);
                return null;
            }
        }

        Object[] addParams(Object... newParams) {
            int newParamLength = newParams.length;

            Object[] newParamList = new Object[parameters.length + newParamLength];

            System.arraycopy(parameters, 0, newParamList, 0, parameters.length);
            System.arraycopy(newParams, 0, newParamList, parameters.length, newParamLength);

            this.parameters = newParamList;

            for (Object param : parameters)
                Logger.log("New parameter: " + param.getClass().getCanonicalName());

            return newParamList;
        }
    }
}