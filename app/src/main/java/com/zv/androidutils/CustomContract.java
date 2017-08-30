package com.zv.androidutils;

import android.net.Uri;

import com.zv.android.content.ContractField;

/**
 * Created by Volodymyr on 8/30/2017.
 */

public class CustomContract {
    public final static String AUTHORITY = "test.authority";

    public interface User
    {
        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");

        @ContractField
        public final static String NAME = "name";

        @ContractField
        public final static String IMAGE = "image";
    }
}
