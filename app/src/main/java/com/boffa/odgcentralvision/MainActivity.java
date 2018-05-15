package com.boffa.odgcentralvision;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private double mImageZoom;

    private Mat mImg;

    private ArrayList<SideControlView> mControls;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the camera view.
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Use landscape, keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Set zoom factor.
        mImageZoom = 3.8;

        // Wire controls.
        initSideControls();
    }

    private void initSideControls()
    {
        mControls = new ArrayList<>();
        mControls.add((SideControlView)findViewById(R.id.control_size));
        mControls.add((SideControlView)findViewById(R.id.control_pixel));
        mControls.add((SideControlView)findViewById(R.id.control_gray));
        mControls.add((SideControlView)findViewById(R.id.control_black));
        mControls.add((SideControlView)findViewById(R.id.control_white));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this, mLoaderCallback);
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        // Allocate a Mat for use in onCameraFrame().
        mImg = new Mat();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        // Get input frame.
        Mat inp = inputFrame.gray();

        // Zoom in on the image so it lines up with the real world in the glasses.
        if (mImageZoom > 1.)
        {
            Size zoomsize = new Size(inp.width() / mImageZoom, inp.height() / mImageZoom);
            Rect zoomrect = new Rect((int) (inp.width() / 2 - zoomsize.width / 2),
                    (int) (inp.height() / 2 - zoomsize.height / 2),
                    (int) zoomsize.width,
                    (int) zoomsize.height);
            Mat subregion = new Mat(inp, zoomrect);

            Imgproc.resize(subregion, mImg, inp.size(), 0, 0, Imgproc.INTER_LINEAR);

            subregion.release();
        }
        else
        {
            return inp;
        }

        // Reduce resolution.
        //Mat downsc = new Mat();
        //Imgproc.resize(out, downsc, new Size(), 0.05, 0.05, Imgproc.INTER_LINEAR);
        //Imgproc.resize(downsc, out, out.size(), 0, 0, Imgproc.INTER_NEAREST);

        inp.release();
        return mImg;
    }
}
