package com.adamson.miles.beastkeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MAXIMUM_PHOTOS = 5;

    DatabaseHelper db;
    DatabaseHelper.BeastProfile beastProfile;
    ArrayList<DatabaseHelper.PhotoAndID> photoAndIDs;

    ImageView imageBeastOne;
    ImageView imageBeastTwo;
    ImageView imageBeastThree;
    ImageView imageBeastFour;
    ImageView imageBeastFive;
    ImageView[] imageViews;

    TextView textViewBio;
    TextView textViewMedical;
    TextView textViewName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(getApplicationContext());

        // Check if Dusty is in the profiles table. If not, insert him and his photos
        if(!db.beastIdExists(DatabaseHelper.DUSTY_ID)){
            db.insertDusty(getApplicationContext());
            db.addPhoto(DatabaseHelper.DUSTY_ID, BitmapFactory.decodeResource(getResources(),
                    R.drawable.cat_one));
            db.addPhoto(DatabaseHelper.DUSTY_ID, BitmapFactory.decodeResource(getResources(),
                    R.drawable.cat_two));
            db.addPhoto(DatabaseHelper.DUSTY_ID, BitmapFactory.decodeResource(getResources(),
                    R.drawable.cat_three));
        }



        // Select Dusty's Profile
        beastProfile = db.selectProfile(DatabaseHelper.DUSTY_ID);

        // Initialize UI elements and set them to Dusty's profile
        initImageViews();

        textViewBio = findViewById(R.id.textViewBio);
        textViewBio.setText(beastProfile.getBio());

        textViewName = findViewById(R.id.textViewName);
        textViewName.setText(beastProfile.getName());

        textViewMedical = findViewById(R.id.textViewMedical);
        textViewMedical.setText(beastProfile.getMedical());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_photo:
                // Check if they are allow to add another photo
                if(db.photoCount(beastProfile.getID()) < MAXIMUM_PHOTOS){
                    // start the camera with intent to take picture, if the phone has a camera
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.photo_error), Toast.LENGTH_SHORT).show();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    // Called when returning from the camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri image = data.getData();
            // Ensure an image was taken successfully
            if(image != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                    try {
                        ExifInterface exifInterface = new ExifInterface(image.getPath());
                        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        // Rotate the image such that when displayed in an imageView,
                        // it will be right side up
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                bitmap = shrinkRotateImage(bitmap, 90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                bitmap = shrinkRotateImage(bitmap, 180);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                bitmap = shrinkRotateImage(bitmap, 270);
                                break;
                            case ExifInterface.ORIENTATION_UNDEFINED:
                                // My phone does not save exif data and likes to rotate 90 degrees
                                bitmap = shrinkRotateImage(bitmap, 90);
                                break;
                            default:
                                // Do not rotate
                        }
                    } catch (IOException e) {
                        System.err.println("An IOException was caught :" + e.getMessage());
                    }

                    db.addPhoto(DatabaseHelper.DUSTY_ID, bitmap);
                    photoAndIDs = db.selectPhotos(DatabaseHelper.DUSTY_ID);
                    setImagesFromArraylist();
                    bitmap.recycle();

                } catch (IOException e) {
                    System.err.println("An IOException was caught :" + e.getMessage());
                }
            }
        }
    }

    // Rotates a bitmap by an integer number of degrees.
    // Scales it down to 20% size.
    private static Bitmap shrinkRotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();

        int width = img.getWidth();
        int height = img.getHeight();
        float scaleWidth = ((float) width * 0.2f) / width;
        float scaleHeight = ((float) height * 0.2f) / height;

        matrix.postScale(scaleWidth, scaleHeight);
        matrix.postRotate(degree);

        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);

        // recycle img since it will no longer be used
        img.recycle();

        return rotatedImg;
    }

    // Initialize all ImageViews and the array that contains them. Set their images with
    // the images stored in the database
    private void initImageViews(){
        imageBeastOne = findViewById(R.id.imageBeastOne);
        imageBeastTwo = findViewById(R.id.imageBeastTwo);
        imageBeastThree = findViewById(R.id.imageBeastThree);
        imageBeastFour = findViewById(R.id.imageBeastFour);
        imageBeastFive = findViewById(R.id.imageBeastFive);

        imageViews = new ImageView[]{
                imageBeastOne,imageBeastTwo,imageBeastThree, imageBeastFour, imageBeastFive
        };

        photoAndIDs = db.selectPhotos(DatabaseHelper.DUSTY_ID);
        setImagesFromArraylist();
    }

    // Put the bitmaps in the photoAndIDs ArrayList into the ImageViews
    private void setImagesFromArraylist(){
        for(int i = 0; i < photoAndIDs.size() && i < imageViews.length; i++){
            imageViews[i].setImageBitmap(photoAndIDs.get(i).getBitmap());
        }
    }

}
