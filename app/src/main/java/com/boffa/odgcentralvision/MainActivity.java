package com.boffa.odgcentralvision;

import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

    public static final double VIEW_WIDTH_DEGREES = 17.8;
    public static final double MICRONS_PER_DEGREE = 300.;

    private CameraBridgeViewBase mOpenCvCameraView;
    private double mImageZoom;

    private OCVProsthesisSettings mSettings;
    public boolean mSettingsChanged;

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
        mImageZoom = 4.0;

        // Initialize prosthesis settings.
        mSettings = new OCVProsthesisSettings();
        mSettingsChanged = false;

        // Wire controls.
        initSideControls();

        /*new CountDownTimer(120000, 2000) {
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
        }.start();*/
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

        // Calculate how many prosthesis pixels should be in the prosthesis.
        Size pros_distort_size = new Size(mSettings.sizeDegrees * (MICRONS_PER_DEGREE / mSettings.pixelSizeMicrons),
                mSettings.sizeDegrees * (MICRONS_PER_DEGREE / mSettings.pixelSizeMicrons));

        // From the data matrix, create a matrix containing the data the prosthesis receives, i.e.
        // just the pixels that are resolvable. This is what will get upscaled.
        Mat pros_distort = new Mat();
        Imgproc.resize(pros_data, pros_distort, pros_distort_size, 0, 0, Imgproc.INTER_LINEAR);

        // Only do gray level calculations if the option is enabled.
        if (mSettings.grayLevels >= 2 && mSettings.grayLevels <= 10)
        {
            double dmin = 1000000., dmax = 0.;

            // Reduce levels of gray and confine between full black/white.
            for (int i = 0; i < pros_distort.rows(); i++) {
                for (int j = 0; j < pros_distort.cols(); j++) {
                    int pixelLightness = (int) pros_distort.get(i, j)[0];

                    // Force this value to one of the gray levels.
                    int grayLevel = pixelLightness / (256 / mSettings.grayLevels);

                    // Interpolate the gray level between the full black and full white value.
                    pixelLightness = mSettings.fullBlack_actual + (int) ((mSettings.fullWhite_actual - mSettings.fullBlack_actual) * (1. * grayLevel / (mSettings.grayLevels - 1.)));
                    pixelLightness = Math.max(Math.min(pixelLightness, 255), 0);

                    pros_distort.put(i, j, pixelLightness);
                }
            }
        }

        // Upscale the prosthesis to the final desired size.
        Mat pros_final = new Mat();
        Imgproc.resize(pros_distort, pros_final, pros_final_size, 0, 0, Imgproc.INTER_LINEAR);

        Rect pros_final_rect = new Rect((int) (mImg.width() / 2 - pros_final_size.width / 2),
                (int) (mImg.height() / 2 - pros_final_size.height / 2),
                (int) pros_final_size.width,
                (int) pros_final_size.height);

        int output_pros_row = (int)(mImg.height() / 2. - pros_final_size.height / 2.);
        int output_pros_col = (int)(mImg.width() / 2. - pros_final_size.width / 2.);

        // Make a submatrix of mImg for where we want to put the prosthesis.
        Mat output_pros = mImg.submat(output_pros_row,
                output_pros_row + (int) pros_final_size.height,
                output_pros_col,
                output_pros_col + (int) pros_final_size.width);

        // Copy into mImg.
        mImg.setTo(new Scalar(0));
        pros_final.copyTo(output_pros);

        pros_data.release();
        pros_distort.release();
        pros_final.release();

        return mImg;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            // Shortcut select.
            case KeyEvent.KEYCODE_BUTTON_1:
                mControls.select(0);
                return true;
            case KeyEvent.KEYCODE_BUTTON_2:
                mControls.select(1);
                return true;
            case KeyEvent.KEYCODE_BUTTON_3:
                mControls.select(2);
                return true;
            case KeyEvent.KEYCODE_BUTTON_4:
                mControls.select(3);
                return true;

            // Previous/next select.
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                mControls.selectPrevious();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mControls.selectNext();
                return true;

            // Increment and decrement.
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mControls.incrementSelected(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mControls.incrementSelected(1);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
