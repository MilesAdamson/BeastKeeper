package com.adamson.miles.beastkeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

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

    public static final int DUSTY_ID = 1;

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

    // Returns the number of photos a beast currently has in the photos table.
    // Returns zero if the beast has no photos or doesn't exist in the table.
    public int photoCount(int beastID){
        SQLiteDatabase db = this.getReadableDatabase();
        String IdString = Integer.toString(beastID);
        int count = 0;

        Cursor cursor = db.rawQuery("SELECT "+T2_ID+" FROM "+T2+
                " WHERE "+T2_beastID+" = "+IdString+";", null);

        if (cursor.moveToFirst()){
            count = cursor.getCount();
        }

        cursor.close();
        return count;
    }

    // Inserts Dusty into the profiles table
    public void insertDusty(Context context){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        String bio = context.getResources().getString(R.string.beast_bio_default);
        String history = context.getResources().getString(R.string.beast_medical_default);
        String name = context.getResources().getString(R.string.beast_name_default);

        cv.put(T1_ID, DUSTY_ID);
        cv.put(T1_name, name);
        cv.put(T1_bio, bio);
        cv.put(T1_history, history);
        db.insert(T1, null, cv );
    }

    // Returns true if a beast is already in the table via its ID
    public boolean beastIdExists(int beastID){
        String IdString = Integer.toString(beastID);
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+T1+
                " WHERE "+T1_ID+" = "+IdString+";", null);

        if(cursor.moveToFirst()){
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

}
