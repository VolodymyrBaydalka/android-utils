package com.zv.androidutils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContentValues values = new ContentValues();

        values.put(CustomContract.Project.NAME, "TestProject");

        Uri projUri = getContentResolver().insert(CustomContract.Project.CONTENT_URI, values);

        values.clear();

        values.put(CustomContract.User.NAME, "TestUser");
        values.put(CustomContract.User.PROJECT_ID, ContentUris.parseId(projUri));

        Uri userUri = getContentResolver().insert(CustomContract.User.CONTENT_URI, values);
    }
}
