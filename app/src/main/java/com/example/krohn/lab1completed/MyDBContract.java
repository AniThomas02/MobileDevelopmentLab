package com.example.krohn.lab1completed;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Ani Thomas on 11/19/2016.
 * Used to create a local database
 */
public class MyDBContract {
    //inner class that defines the schema
    public static abstract class DBEntry implements BaseColumns{
        public static final String TABLE_NAME = "Users";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password";
    }

    private static final String TEXT_TYPE = " TEXT ";
    private static final String COMMA_SEP = " ,";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DBEntry.TABLE_NAME + " ("
            + DBEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + DBEntry.COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP
            + DBEntry.COLUMN_NAME_PASSWORD + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBEntry.TABLE_NAME;

    //Inner helper class for myDBContract
    public static class MyDbHelper extends SQLiteOpenHelper{
        //if the schema changes, the version must change
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "CardGame.db";

        public MyDbHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            //onUpgrade, just start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
