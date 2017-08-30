package com.zv.android.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Volodymyr on 10.01.14.
 */
public class SimpleContentProvider extends ContentProvider
{
    public static final String ARG_REPLACE = "replace";

    protected Class<?>[] contractClasses;
    protected final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    protected String databaseName;
    protected String[] tableNames;
    protected String[] types;
    protected SQLiteOpenHelper openHelper;

    public SimpleContentProvider(Class<?> contractClass, String authority, String database)
    {
        this(SimpleOpenHelper.findTableClasses(contractClass), authority, database);
    }

    public SimpleContentProvider(Class<?>[] contractClasses, String authority, String database)
    {
        ArrayList<String> tablesList = new ArrayList<String>();
        ArrayList<String> typesList = new ArrayList<String>();
        int index = 0;

        for (Class<?> tableClass : contractClasses)
        {
            String tableName = SimpleOpenHelper.getTableName(tableClass);

            uriMatcher.addURI(authority, tableName, index++);
            uriMatcher.addURI(authority, tableName + "/#", index++);

            typesList.add("vnd.android.cursor.dir/" + authority + "." + tableName);
            typesList.add("vnd.android.cursor.item/" + authority + "." + tableName);

            tablesList.add(tableName);
        }

        this.contractClasses = contractClasses;
        this.tableNames = tablesList.toArray(new String[tablesList.size()]);
        this.types = typesList.toArray(new String[typesList.size()]);
        this.databaseName = database;
    }

    @Override
    public boolean onCreate() {
        openHelper = new SimpleOpenHelper(getContext(), this.contractClasses, this.databaseName, null,
                SimpleOpenHelper.findVersion(this.contractClasses));
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
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int len = values.length;
        int match = uriMatcher.match(uri);
        int conflictAlgorithm = SQLiteDatabase.CONFLICT_NONE;

        if(match == UriMatcher.NO_MATCH)
            throw new IllegalArgumentException("uri");

        if("true".equals(uri.getQueryParameter(ARG_REPLACE)))
            conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;

        String tableName = tableNames[match / 2];
        SQLiteDatabase db = openHelper.getWritableDatabase();

        db.beginTransaction();

        for (int i = 0; i < len; i++)
            if(db.insertWithOnConflict(tableName, null, values[i], conflictAlgorithm) == -1)
                break;

        db.setTransactionSuccessful();
        db.endTransaction();

        getContext().getContentResolver().notifyChange(uri, null);

        return len;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH)
            throw new IllegalArgumentException("uri");

        String tableName = tableNames[match / 2];

        if(match % 2 == 1) {
            if(TextUtils.isEmpty(selection))
                selection = "_id=" + uri.getLastPathSegment();
            else
                selection = String.format("( %s ) AND ( _id=%s )", selection, uri.getLastPathSegment());
        }

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

        if(match % 2 == 1) {
            if(TextUtils.isEmpty(selection))
                selection = "_id=" + uri.getLastPathSegment();
            else
                selection = String.format("( %s ) AND ( _id=%s )", selection, uri.getLastPathSegment());
        }

        int result = openHelper.getWritableDatabase()
                .update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return result;
    }
}