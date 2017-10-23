package zyx0124.com.camera_intent;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static java.util.Calendar.DATE;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private ImageView mPhotoCapturedImageView;
    private String mImageFileLocation = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhotoCapturedImageView=(ImageView) findViewById(R.id.capturePhotoImageView);


    }

    public void takePhoto(View view){
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile=null;
        try {
            photoFile = createImageFile();
        }catch (IOException e) {
            e.printStackTrace();
        }
        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(callCameraApplicationIntent,ACTIVITY_START_CAMERA_APP);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            //Toast.makeText(this, "Picture taken successfully", Toast.LENGTH_SHORT).show();
            //Bundle extras = data.getExtras();
            //Bitmap photoCaptureBitmap = (Bitmap) extras.get("data");
            //mPhotoCapturedImageView.setImageBitmap(photoCaptureBitmap);
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageFileLocation);
            mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
        }

    }


    File createImageFile() throws IOException{
        String timeStamp = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        }
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image= File.createTempFile(imageFileName,".jpg",storageDirectory);
        mImageFileLocation = image.getAbsolutePath();

        return image;
    }

//         void setReducedImageSize(){
//        int targetImageViewWidth =mPhotoCapturedImageView.getWidth();
//        int targetImageViewHeight=mPhotoCapturedImageView.getHeight();
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(mImageFileLocation,bmOptions);
//        int cameraImageWidth = bmOptions.outWidth;
//       int cameraImageHeight= bmOptions.outHeight;
//        int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth,cameraImageHeight/targetImageViewHeight);
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inJustDecodeBounds = false;
//        Bitmap photoReducedSizeBitmap = BitmapFactory.decodeFile(mImageFileLocation,bmOptions);
//        mPhotoCapturedImageView.setImageBitmap(photoReducedSizeBitmap);
//    }
}
