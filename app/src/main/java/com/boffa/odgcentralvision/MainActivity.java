package com.boffa.odgcentralvision;

import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final int VIEW_WIDTH_DEGREES = 30;

    private CameraBridgeViewBase mOpenCvCameraView;
    private double mImageZoom;

    private OCVProsthesisSettings mSettings;

    private Mat mImg;
    private Mat mProsMask;

    private SideControlBank mControls;

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

        // Initialize prosthesis settings.
        mSettings = new OCVProsthesisSettings();

        // Wire controls.
        initSideControls();

        new CountDownTimer(120000, 2000) {
            public void onTick(long l)
            {
                mControls.selectNext();
            }
            public void onFinish(){}
        }.start();

        new CountDownTimer(120000, 300) {
            public void onTick(long l)
            {
                mControls.incrementSelected();
            }
            public void onFinish(){}
        }.start();
    }

    private void initSideControls()
    {
        mControls = new SideControlBank();
        mControls.addControl((SideControlView)findViewById(R.id.control_size));
        mControls.addControl((SideControlView)findViewById(R.id.control_pixel));
        mControls.addControl((SideControlView)findViewById(R.id.control_gray));
        mControls.addControl((SideControlView)findViewById(R.id.control_black));
        mControls.addControl((SideControlView)findViewById(R.id.control_white));
        mControls.start(mSettings);
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
        mImg = new Mat(height, width, CvType.CV_8UC1, new Scalar(0));
        mProsMask = new Mat(height, width, CvType.CV_8UC1);

        generateProsthesisMask();
    }

    @Override
    public void onCameraViewStopped() {

    }

    private void generateProsthesisMask()
    {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        // Get input frame.
        Mat inp = inputFrame.gray();

        // Determine a sub-rectangle of the full image. This accounts for
        // both zooming the image (to line it up with the real world) and isolating
        // the prosthesis.

        // Size of the prosthesis data rectangle in the input frame.
        double pros_data_dim = (inp.width() / mImageZoom) * (mSettings.sizeDegrees * 1. / VIEW_WIDTH_DEGREES);
        Size pros_data_size = new Size(pros_data_dim, pros_data_dim);

        // The prosthesis data rectangle.
        Rect pros_data_rect = new Rect((int) (inp.width() / 2 - pros_data_size.width / 2),
                (int) (inp.height() / 2 - pros_data_size.height / 2),
                (int) pros_data_size.width,
                (int) pros_data_size.height);

        // Create a matrix containing just the prosthesis data.
        Mat pros_data = new Mat(inp, pros_data_rect);

        // Goal size of the prosthesis (when being displayed).
        Size pros_final_size = new Size(inp.width() * (mSettings.sizeDegrees * 1. / VIEW_WIDTH_DEGREES),
                inp.width() * (mSettings.sizeDegrees * 1. / VIEW_WIDTH_DEGREES));

        // Upscale the prosthesis to the final desired size.
        Mat pros_final = new Mat();
        Imgproc.resize(pros_data, pros_final, pros_final_size, 0, 0, Imgproc.INTER_LINEAR);

        Rect pros_final_rect = new Rect((int) (mImg.width() / 2 - pros_final_size.width / 2),
                (int) (mImg.height() / 2 - pros_final_size.height / 2),
                (int) pros_final_size.width,
                (int) pros_final_size.height);

        int output_pros_row = (int)(mImg.height() / 2. - pros_final_size.height / 2.);
        int output_pros_col = (int)(mImg.width() / 2. - pros_final_size.width / 2.);

        // Copy the final prosthesis to the larger image.
        Mat output_pros = mImg.submat(output_pros_row,
                output_pros_row + (int) pros_final_size.height,
                output_pros_col,
                output_pros_col + (int) pros_final_size.width);

        if (output_pros.type() != pros_final.type() || mImg.type() != pros_final.type())
        {
            Log.d("", "WRONG TYPE!");
        }
        pros_final.copyTo(output_pros);

        // Reduce resolution.
        //Mat downsc = new Mat();
        //Imgproc.resize(out, downsc, new Size(), 0.05, 0.05, Imgproc.INTER_LINEAR);
        //Imgproc.resize(downsc, out, out.size(), 0, 0, Imgproc.INTER_NEAREST);


        pros_data.release();
        pros_final.release();

        int dim1 = inp.dims();
        int dim2 = mImg.dims();

        return mImg;
    }
}
