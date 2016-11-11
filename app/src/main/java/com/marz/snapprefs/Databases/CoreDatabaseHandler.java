package com.marz.snapprefs.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static com.marz.snapprefs.Databases.CoreDatabaseHandler.DBUtils.formatExclusionList;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

class CoreDatabaseHandler extends SQLiteOpenHelper {
    private String DATABASE_NAME;
    private String[] SQL_CREATE_ENTRIES;
    private SQLiteDatabase writableDatabase;

    CoreDatabaseHandler(Context context, String databaseName, String[] entries, int DATABASE_VERSION) {
        super(context, databaseName, null, DATABASE_VERSION);
        DATABASE_NAME = databaseName;
        SQL_CREATE_ENTRIES = entries;
        writableDatabase = getDatabase();
    }

    SQLiteDatabase getDatabase() {
        createIfNotExisting();
        return writableDatabase;
    }


    private void createIfNotExisting() {
        if (writableDatabase == null || !writableDatabase.isOpen())
            writableDatabase = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String entry : SQL_CREATE_ENTRIES)
            db.execSQL(entry);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    long getRowCount(String tableName) {
        return DatabaseUtils.queryNumEntries(getDatabase(), tableName);
    }

    boolean checkIfColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor emptyCursor = null;

        Logger.log(String.format("Checking if column '%s' exists in table '%s'", columnName, tableName), LogType.DATABASE);

        try {
            emptyCursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);

            Logger.log("Index Number: " + emptyCursor.getColumnIndex(columnName), LogType.DATABASE);

            return emptyCursor.getColumnIndex(columnName) != -1;
        } catch (Exception e) {
            Logger.log("Problem checking if cursor exists!", e, LogType.DATABASE);
        } finally {
            if (emptyCursor != null)
                emptyCursor.close();
        }

        return false;
    }

    public long insertValues(String tableName, ContentValues values) {
        return getDatabase().insert(tableName, null, values);
    }

    public boolean containsObject(String tableName, String columnName, String[] selectionArgs) {
        String selection = columnName + " = ?";
        Cursor cursor = getDatabase().query(
                tableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean contains = cursor.getCount() != 0;

        Logger.log(String.format("Is %s in [Table:%s][Column:%s]: %s", Arrays.toString(selectionArgs), tableName, columnName, contains ? "YES" : "NO"), LogType.DATABASE);

        cursor.close();

        return contains;
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
        Logger.log(String.format("Count of %s in [Table:%s][Column:%s] = [%s]", Arrays.toString(selectionArgs), tableName, columnName, count), LogType.DATABASE);
        return count;
    }

    public ContentValues getContent(String tableName, String columnName, String[] selectionArgs,
                                    String sortOrder, String[] projection) {
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

        Logger.log(String.format("Getting content %s from [Table:%s][Column:%s]", Arrays.toString(selectionArgs), tableName, columnName), LogType.DATABASE);

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row", LogType.DATABASE);
            return null;
        }

        ContentValues content = getValuesFromCursor(cursor, projection);
        cursor.close();

        if (content == null) {
            Logger.log("Null content", LogType.DATABASE);
            return null;
        }

        Logger.log(String.format("Retried [%s] content objects from DB", content.size()), LogType.DATABASE);
        return content;
    }

    public Object getBuiltContent(String tableName, String columnName, String[] selectionArgs,
                                  String sortOrder, String[] projection, CallbackHandler callbackHandler) {
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

        Logger.log(String.format("Getting built Object %s from [Table:%s][Column:%s]", Arrays.toString(selectionArgs), tableName, columnName), LogType.DATABASE);


        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row", LogType.DATABASE);
            return null;
        }

        Object invocationResponse = invokeCallback(callbackHandler);

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName(), LogType.DATABASE);
            return null;
        }

        cursor.close();
        return invocationResponse;
    }

    public ArrayList<ContentValues> getAllContentExcept(String tableName, String columnName, String[] projection,
                                                        ArrayList<String> blacklist) {
        Logger.log("Getting all content from database", LogType.DATABASE);

        String strBlacklist = formatExclusionList(blacklist);
        String query = "SELECT * FROM " + tableName +
                " WHERE " + columnName + " NOT IN (" + strBlacklist + ")";

        Logger.log("Performing query: " + query, LogType.DATABASE);

        Cursor cursor = getDatabase().rawQuery(query, null);

        Logger.log("Query size: " + cursor.getCount(), LogType.DATABASE);

        ArrayList<ContentValues> content = new ArrayList<>();

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row", LogType.DATABASE);
            return null;
        }

        while (!cursor.isAfterLast()) {
            ContentValues value = getValuesFromCursor(cursor, projection);

            if (value.size() > 0)
                content.add(value);

            cursor.moveToNext();
        }

        Logger.log(String.format("Retrieved [%s] content objects", content.size()), LogType.DATABASE);

        cursor.close();
        return content;
    }

    public ArrayList<ContentValues> getAllContent(String tableName, String[] projection) {
        Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + tableName, null);

        Logger.log("Query size: " + cursor.getCount(), LogType.DATABASE);

        ArrayList<ContentValues> contentList = new ArrayList<>();

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row", LogType.DATABASE);
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

        Logger.log("Performing query: " + query, LogType.DATABASE);
        Cursor cursor = getDatabase().rawQuery(query, null);
        callbackHandler.addParams(cursor);

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row", LogType.DATABASE);
            return null;
        }

        Logger.log("Query size: " + cursor.getCount(), LogType.DATABASE);

        Object invocationResponse = invokeCallback(callbackHandler);

        cursor.close();

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName(), LogType.DATABASE);
            return null;
        }

        return invocationResponse;
    }

    Object getAllBuiltObjects(String tableName, CallbackHandler callbackHandler) {
        return getAllBuiltObjects(tableName, null, null, callbackHandler);
    }

    Object getAllBuiltObjects(String tableName, String where, String orderBy, CallbackHandler callbackHandler) {
        Cursor cursor = getDatabase().rawQuery(
                "SELECT * FROM " + tableName +
                        (where != null ? " WHERE " + where : "") +
                        (orderBy != null ? " ORDER BY " + orderBy : ""), null);

        Logger.log(String.format("Building [%s] queried objects", cursor.getCount()), LogType.DATABASE);

        callbackHandler.addParams(cursor);

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row", LogType.DATABASE);
            return null;
        }

        Object invocationResponse = invokeCallback(callbackHandler);

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName(), LogType.DATABASE);
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

        Logger.log("Query count: " + cursor.getCount(), LogType.DATABASE);

        if (!cursor.moveToFirst()) {
            Logger.log("Error moving cursor to first row", LogType.DATABASE);
            return null;
        }

        Object invocationResponse = invokeCallback(callbackHandler);

        if (invocationResponse == null) {
            Logger.log("Null response from the invoked method: " + callbackHandler.method.getName(), LogType.DATABASE);
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
                Logger.log("Error getting object from cursor", e, LogType.DATABASE);
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
                    Logger.log("Tried to get null type from cursor", LogType.DATABASE);
                    break;
                default:
                    Logger.log("Unknown type passed to cursor", LogType.DATABASE);
            }
        }

        return contentValues;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private Object invokeCallback(CallbackHandler callbackHandler) {
        try {
            return callbackHandler.method.invoke(callbackHandler.caller, callbackHandler.parameters);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("Error building callback method", e, LogType.DATABASE);
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
                return new CallbackHandler(clazz, clazz.getClass().getMethod(methodName, classType));
            } catch (NoSuchMethodException e) {
                Logger.log("Error creating callback method", e, LogType.DATABASE);
                return null;
            }
        }

        Object[] addParams(Object... newParams) {
            int newParamLength = newParams.length;

            Object[] newParamList = new Object[parameters.length + newParamLength];

            System.arraycopy(parameters, 0, newParamList, 0, parameters.length);
            System.arraycopy(newParams, 0, newParamList, parameters.length, newParamLength);

            this.parameters = newParamList;

            return this.parameters;
        }
    }
}