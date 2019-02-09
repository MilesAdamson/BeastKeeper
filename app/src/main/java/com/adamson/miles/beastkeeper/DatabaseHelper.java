package com.adamson.miles.beastkeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    private static final String T1 = "profile";
    private static final String T1_ID = "ID";
    private static final String T1_name = "name";
    private static final String T1_bio = "bio";
    private static final String T1_history= "history";

    private static final String T2 = "photos";
    private static final String T2_ID = "ID";
    private static final String T2_beastID = "beastID";
    private static final String T2_photo = "photo";

    public DatabaseHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + T1 + " (" +
                T1_ID + " INT PRIMARY KEY, " +
                T1_name + " TEXT NOT NULL, " +
                T1_bio + " TEXT, " +
                T1_history + " TEXT);"
        );

        database.execSQL("CREATE TABLE " + T2 + " (" +
                T2_ID + " INT PRIMARY KEY, " +
                T2_beastID + " INT NOT NULL, " +
                T2_photo + " BLOB NOT NULL);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + T1);
        database.execSQL("DROP TABLE IF EXISTS " + T2);
        onCreate(database);
    }

    // Adds a photo to the photos table, with the foreign key of the beasts profile
    // and the bitmap of the image
    public void addPhoto(int id, Bitmap bitmap){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();

        // convert from bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);

        // insert
        cv.put(T2_beastID, id);
        cv.put(T2_photo, stream.toByteArray());
        db.insert(T2, null, cv );
    }


}
