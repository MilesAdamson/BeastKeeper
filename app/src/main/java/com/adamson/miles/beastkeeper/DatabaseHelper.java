package com.adamson.miles.beastkeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    private static final int T1_ID_INDEX = 0;
    private static final int T1_NAME_INDEX = 1;
    private static final int T1_BIO_INDEX = 2;
    private static final int T1_HISTORY_INDEX = 3;

    private static final String T2 = "photos";
    private static final String T2_ID = "ID";
    private static final String T2_beastID = "beastID";
    private static final String T2_photo = "photo";

    private static final int T2_ID_INDEX = 0;
    private static final int T2_BEAST_ID_INDEX = 1;
    private static final int T2_PHOTO_INDEX = 2;

    public static final int DUSTY_ID = 1;
    public static final int UPDATE_BIO = 0;
    public static final int UPDATE_NAME = 1;
    public static final int UPDATE_HISTORY = 2;

    public DatabaseHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + T1 + " (" +
                T1_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                T1_name + " TEXT NOT NULL, " +
                T1_bio + " TEXT, " +
                T1_history + " TEXT);"
        );

        database.execSQL("CREATE TABLE " + T2 + " (" +
                T2_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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

    // Returns an array of PhotoAndID objects for every photo a beast has.
    // If there are no photos, returns an arraylist of length zero
    public ArrayList<PhotoAndID> selectPhotos(int beastID){
        SQLiteDatabase db = this.getReadableDatabase();
        String IdString = Integer.toString(beastID);
        ArrayList<PhotoAndID> photoAndIDs = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM "+T2+
                " WHERE "+T2_beastID+" = "+IdString+";", null);

        if (cursor.moveToFirst()){
            for(int i = 0; i < cursor.getCount(); i++){
                photoAndIDs.add(new PhotoAndID(cursor.getInt(T2_ID_INDEX),
                        cursor.getBlob(T2_PHOTO_INDEX)));
                cursor.moveToNext();
            }
        }

        cursor.close();
        return photoAndIDs;
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


    // Returns a BeastProfile for a beast based on ID. Returns null if a beast doesn't exist
    public BeastProfile selectProfile(int beastID){
        SQLiteDatabase db = this.getReadableDatabase();
        String IdString = Integer.toString(beastID);

        Cursor cursor = db.rawQuery("SELECT * FROM "+T1+
                " WHERE "+T1_ID+" = "+IdString+";", null);

        if (cursor.moveToFirst()){
           BeastProfile beastProfile = new BeastProfile(
                   cursor.getInt(T1_ID_INDEX),
                   cursor.getString(T1_NAME_INDEX),
                   cursor.getString(T1_BIO_INDEX),
                   cursor.getString(T1_HISTORY_INDEX)
           );
           cursor.close();
           return beastProfile;
        } else {
            cursor.close();
            return null;
        }
    }

    public Boolean deletePhoto(int photoID){
        SQLiteDatabase db = this.getReadableDatabase();

        String photoIdString = Integer.toString(photoID);

        return db.delete(T2, T2_ID+"="+photoIdString, null) > 0;
    }

    // A class to contain a beasts profile, to return from select profile queries
    public class BeastProfile{
        private int beastID;
        private String bio;
        private String medical;
        private String name;

        public BeastProfile(int beastID, String name, String bio, String medical){
            this.beastID = beastID;
            this.bio = bio;
            this.medical = medical;
            this.name = name;
        }

        public String getName(){return name;}
        public String getBio(){return bio;}
        public String getMedical(){return medical;}
        public int getID(){return beastID;}
    }

    // This class contains a photo from the db and its id. Its contructor
    // takes the byte array from the db, and converts it to a bitmap
    public class PhotoAndID{
        private int photoID;
        private byte[] blob;

        public PhotoAndID(int photoID, byte[] blob) {
            this.photoID = photoID;
            this.blob = blob;
        }

        public Bitmap getBitmap(){
            return BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }

        public int getPhotoID(){return photoID;}
    }

    // Updates a single field of a beasts profile
    public void updateProfileField(int beastID, int field, String content){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        String beastIdString = Integer.toString(beastID);
        switch (field){
            case UPDATE_BIO:
                cv.put(T1_bio, content);
                db.update(T1, cv, T1_ID+"="+beastIdString, null);
                break;
            case UPDATE_NAME:
                cv.put(T1_name, content);
                db.update(T1, cv, T1_ID+"="+beastIdString, null);
                break;
            case UPDATE_HISTORY:
                cv.put(T1_history, content);
                db.update(T1, cv, T1_ID+"="+beastIdString, null);
                break;
        }

    }
}
