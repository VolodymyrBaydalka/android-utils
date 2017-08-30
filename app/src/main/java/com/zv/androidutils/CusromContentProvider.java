package com.zv.androidutils;

/**
 * Created by Volodymyr on 8/30/2017.
 */

public class CusromContentProvider extends com.zv.android.content.SimpleContentProvider {
    public CusromContentProvider() {
        super(CustomContract.class, CustomContract.AUTHORITY, "database");
    }
}
