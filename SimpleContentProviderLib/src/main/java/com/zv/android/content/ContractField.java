package com.zv.android.content;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Volodymyr on 10.01.14.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContractField {

    public final static String TYPE_NULL = "NULL";
    public final static String TYPE_INTEGER = "INTEGER";
    public final static String TYPE_REAL = "REAL";
    public final static String TYPE_TEXT = "TEXT";
    public final static String TYPE_BLOB = "BLOB";

    public String type() default TYPE_TEXT;
    public int version() default 1;
    public String foreignKey() default "";
}