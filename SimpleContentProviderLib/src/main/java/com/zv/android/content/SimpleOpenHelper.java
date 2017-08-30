package com.zv.android.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Volodymyr on 30.04.2014.
 */
public class SimpleOpenHelper extends SQLiteOpenHelper
{
    private Class<?>[] contractClasses;

    public SimpleOpenHelper(Context context, Class<?> contractClass, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        this(context, findTableClasses(contractClass), name, factory, version);
    }

    public SimpleOpenHelper(Context context, Class<?>[] contractClasses, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);

        this.contractClasses = contractClasses;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String[] columnData = new String[2];

        for (Class<?> tableClass : this.contractClasses)
        {
            StringBuilder qBuilder = new StringBuilder("CREATE TABLE ");

            qBuilder.append(getTableName(tableClass));
            qBuilder.append("(_id INTEGER PRIMARY KEY");

            for(Field field : tableClass.getDeclaredFields())
            {
                if(getColumnFromField(field, columnData))
                {
                    qBuilder.append(",[").append(columnData[0]).append("] ").append(columnData[1]);
                }
            }

            qBuilder.append(")");
            db.execSQL(qBuilder.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for (Class<?> tableClass : this.contractClasses) {
            db.execSQL("DROP TABLE IF EXISTS [" + getTableName(tableClass) + "]");
        }

        onCreate(db);
    }

    static int findVersion(Class<?>[] classes)
    {
        int version = 1;

        for (int i = 0; i < classes.length; i++) {
            for (Field field : classes[i].getFields())
            {
                ContractField contractFiled = field.getAnnotation(ContractField.class);

                if(contractFiled != null)
                    version = Math.max(contractFiled.version(), version);
            }
        }

        return version;
    }

    static Class<?>[] findTableClasses(Class<?> contractClass)
    {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>();

        for (Class<?> clazz : contractClass.getDeclaredClasses())
        {
            result.add(clazz);
        }

        return result.toArray(new Class<?>[result.size()]);
    }

    static String getTableName(Class<?> tableClass)
    {
        return tableClass.getSimpleName().toLowerCase();
    }

    static boolean getColumnFromField(Field field, String[] results)
    {
        ContractField contractFiled = field.getAnnotation(ContractField.class);

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
}
