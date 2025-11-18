package com.example.btqt02;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "customer.db";
    private static final int DB_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Customers ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "phone TEXT, "
                + "point INTEGER, "
                + "note TEXT, "
                + "createdAt TEXT, "
                + "updatedAt TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS Customers";
        db.execSQL(sql);

        onCreate(db);
    }
}
