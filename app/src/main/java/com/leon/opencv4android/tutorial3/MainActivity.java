package com.leon.opencv4android.tutorial3;

import android.annotation.SuppressLint;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "MainActivity";
    private MyCameraView mCameraView;

    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mCameraView = (MyCameraView) findViewById(R.id.surfaceCamera);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setOnTouchListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mCameraView.getEffectList();
        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        int effectSize = effects.size();
        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effectSize];
        for (int i = 0; i < effectSize; i++) {
            mEffectMenuItems[i] = mColorEffectsMenu.add(1, i, Menu.NONE, effects.get(i));
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];
        for (int i = 0; i < mResolutionList.size(); i++) {
            Size size = mResolutionList.get(i);
            mResolutionMenuItems[i] = mResolutionMenu.add(2, i, Menu.NONE, size.width + "x" + size.height);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getGroupId()) {
            case 1:
                mCameraView.setEffect(item.getTitle().toString());
                Toast.makeText(MainActivity.this, mCameraView.getEffect(), Toast.LENGTH_SHORT).show();
                break;
            case 2:
                mCameraView.setResolution(mResolutionList.get(item.getItemId()));
                Size resolution = mCameraView.getResolution();
                Toast.makeText(MainActivity.this, resolution.width + "x" + resolution.height, Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath()
                + "/" + currentDate + ".jpg";
        mCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }
}
