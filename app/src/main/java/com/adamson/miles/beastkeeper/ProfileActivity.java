package com.adamson.miles.beastkeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    DatabaseHelper db;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView photo4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(getApplicationContext());
        photo4 = findViewById(R.id.imageBeastFour);
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
                // start the camera with intent to take picture, if the phone has a camera
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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
                    db.addPhoto(1, bitmap);
                    photo4.setImageBitmap(bitmap);
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

}
