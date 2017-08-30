package com.zv.androidutils;

import android.net.Uri;
import android.provider.BaseColumns;

import com.zv.android.content.ContractField;

/**
 * Created by Volodymyr on 8/30/2017.
 */

public class CustomContract {
    public final static String AUTHORITY = "test.authority";

    public interface Project
    {
        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/project");

        public final static String ID = BaseColumns._ID;

        @ContractField
        public final static String NAME = "name";
    }

    public interface User
    {
        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");

        public final static String ID = BaseColumns._ID;

        @ContractField(foreignKey = "project(_id)")
        public final static String PROJECT_ID = "project_id";

        @ContractField
        public final static String NAME = "[name]";

        @ContractField
        public final static String IMAGE = "image";
    }
}
