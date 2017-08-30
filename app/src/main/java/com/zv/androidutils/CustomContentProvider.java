package com.zv.androidutils;

/**
 * Created by Volodymyr on 8/30/2017.
 */

public class CustomContentProvider extends com.zv.android.content.SimpleContentProvider {
    public CustomContentProvider() {
        super(CustomContract.class, CustomContract.AUTHORITY, "database");
    }
}
