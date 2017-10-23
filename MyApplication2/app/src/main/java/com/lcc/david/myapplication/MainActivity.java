package com.lcc.david.myapplication;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button cameraButton;
    //private CameraManager mManager;
    //private String[] mCameraIds;
    private Camera mCamera;
    private CameraPreview mPreview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // sign out button
        Button signOut = (Button) findViewById(R.id.signOut_button);
        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                signOut();
            }
        });

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        String user = extras.getString(LogInActivity.USER);
        ((TextView)findViewById(R.id.logged_user)).setText(user);


        //mManager = (CameraManager)getSystemService(this.CAMERA_SERVICE);


        cameraButton = (Button) findViewById(R.id.open_camera);
        cameraButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                // open the camera
                /*try {
                    mCameraIds = mManager.getCameraIdList();
                }
                catch (CameraAccessException e)
                {
                    Toast t = Toast.makeText(v.getContext(),"Cannot open the camera",Toast.LENGTH_LONG);
                    t.show();
                }*/
                // Create an instance of Camera
                mCamera = getCameraInstance();

                // Create our Preview view and set it as the content of our activity.
                mPreview = new CameraPreview(v.getContext(), mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public void signOut() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        Intent i = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(i);
        // remove this activity from the back stack
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the back button is pressed then SignOut
        signOut();
    }
}
