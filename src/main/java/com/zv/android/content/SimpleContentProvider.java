package com.zv.android.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Volodymyr on 10.01.14.
 */
public class SimpleContentProvider extends ContentProvider
{
    protected Class<?> contractClass;
    protected final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    protected String databaseName;
    protected String[] tableNames;
    protected String[] types;
    protected SQLiteOpenHelper openHelper;

    public SimpleContentProvider(Class<?> contractClass, String authority, String database)
    {
        ArrayList<String> tablesList = new ArrayList<String>();
        ArrayList<String> typesList = new ArrayList<String>();
        int index = 0;

        for (Class<?> tableClass : findTableClasses(contractClass))
        {
            String tableName = getTableName(tableClass);

            uriMatcher.addURI(authority, tableName, index++);
            uriMatcher.addURI(authority, tableName + "/#", index++);

            typesList.add("vnd.android.cursor.dir/" + authority + "." + tableName);
            typesList.add("vnd.android.cursor.item/" + authority + "." + tableName);

            tablesList.add(tableName);
        }

        this.contractClass = contractClass;
        this.tableNames = tablesList.toArray(new String[tablesList.size()]);
        this.types = typesList.toArray(new String[typesList.size()]);
        this.databaseName = database;
    }

    @Override
    public boolean onCreate() {
        openHelper = new SimpleOpenHelper(getContext(), this.contractClass, this.databaseName, null, 1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int match = uriMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH)
            throw new IllegalArgumentException("uri");

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(this.tableNames[match / 2]);

        if(match % 2 == 1)
            qBuilder.appendWhere("_id=" + uri.getLastPathSegment());

        Cursor c = qBuilder.query(openHelper.getWritableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH)
            throw new IllegalArgumentException("uri");

        return types[match];
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int match = uriMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH)
            throw new IllegalArgumentException("uri");

        String tableName = tableNames[match / 2];

        long id = openHelper.getWritableDatabase().insert(tableName, null, values);
        Uri resultUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(resultUri, null);

        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH)
            throw new IllegalArgumentException("uri");

        String tableName = tableNames[match / 2];

        int result = openHelper.getWritableDatabase().delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH)
            throw new IllegalArgumentException("uri");

        String tableName = tableNames[match / 2];

        int result = openHelper.getWritableDatabase()
                .update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return result;
    }

    protected static List<Class<?>> findTableClasses(Class<?> contractClass)
    {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>();

        for (Class<?> clazz : contractClass.getDeclaredClasses())
        {
            result.add(clazz);
        }

        return result;
    }

    protected static boolean getColumnFromField(Field field, String[] results)
    {
        ContractFiled contractFiled = field.getAnnotation(ContractFiled.class);

        if(contractFiled == null)
            return false;

        try
        {
            results[0] = (String)field.get(null);
            results[1] = contractFiled.type();
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    protected static String getTableName(Class<?> tableClass)
    {
        return tableClass.getSimpleName().toLowerCase();
    }

    static class SimpleOpenHelper extends SQLiteOpenHelper
    {
        private Class<?> contractClass;

        public SimpleOpenHelper(Context context, Class<?> contractClass, String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);

            this.contractClass = contractClass;
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            String[] columnData = new String[2];

            for (Class<?> tableClass : findTableClasses(this.contractClass))
            {
                StringBuilder qBuilder = new StringBuilder("CREATE TABLE ");

                qBuilder.append(getTableName(tableClass));
                qBuilder.append("(_ID INTEGER PRIMARY KEY");

                for(Field field : tableClass.getDeclaredFields())
                {
                    if(getColumnFromField(field, columnData))
                    {
                        qBuilder.append(",").append(columnData[0]).append(" ").append(columnData[1]);
                    }
                }

                qBuilder.append(")");
                db.execSQL(qBuilder.toString());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }
}
