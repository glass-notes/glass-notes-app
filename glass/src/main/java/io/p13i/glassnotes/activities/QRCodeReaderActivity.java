package io.p13i.glassnotes.activities;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.p13i.glassnotes.R;
import io.p13i.glassnotes.camera.CameraPreview;
import io.p13i.glassnotes.utilities.CameraUtilities;

import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

import java.util.Iterator;

/**
 * Reads a QR code read the camera.
 * Based on https://github.com/jzplusplus/GlassWifiConnect/blob/96d8d67f79c209e204d5b728498cd4899c2640c0/src/com/jzplusplus/glasswificonnect/MainActivity.java
 */
public class QRCodeReaderActivity extends GlassNotesActivity {
    private static final String TAG = QRCodeReaderActivity.class.getName();


    private CameraPreview mCameraPreview;
    private Handler mAutoFocusHandler;
    private Camera mCamera;
    private ImageScanner mScanner;
    private boolean mIsPreviewing;

    public static final String INTENT_RESULT_KEY = "qrCodeResultData";

    @BindView(R.id.camera_preview)
    FrameLayout mCameraPreviewFrameLayout;

    LayoutInflater controlInflater = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qr_code_reader);
        ButterKnife.bind(this);

        // Keep screen on
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Full screen
        if (this.getActionBar() != null) {
            this.getActionBar().hide();
        }

        mAutoFocusHandler = new Handler();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        try {
            Log.i(TAG, "Cameras available " + Camera.getNumberOfCameras());
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);
                Log.i(TAG, "Camera: facing " + cameraInfo.facing + ", orientation " + cameraInfo.orientation);
            }

            mCamera = CameraUtilities.open();

        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to acquire camera", e);
            mCamera = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to acquire camera: interrrupted", e);
            mCamera = null;
        }

        if (mCamera == null) {
            Toast.makeText(this, "Failed to acquire camera", Toast.LENGTH_SHORT).show();
            finish();
        }

        /* Instance barcode scanner */
        mScanner = new ImageScanner() {{
            setConfig(0, Config.X_DENSITY, 3);
            setConfig(0, Config.Y_DENSITY, 3);
        }};

        mCameraPreview = new CameraPreview(this, mCamera, mCameraPreviewCallback, mAutoFocusCallBack);

        mCameraPreviewFrameLayout.addView(mCameraPreview);

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.qr_code_reader_bounding_box, null);
        WindowManager.LayoutParams layoutParamsControl
                = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

        super.onCreate(savedInstanceState);
    }

    // Mimic continuous auto-focusing
    private Camera.AutoFocusCallback mAutoFocusCallBack = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(mPerformAutoFocusRunnable, 1000);
        }
    };

    private Runnable mPerformAutoFocusRunnable = new Runnable() {
        public void run() {
            if (mIsPreviewing)
                mCamera.autoFocus(mAutoFocusCallBack);
        }
    };

    private Camera.PreviewCallback mCameraPreviewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(final byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, /* color format: */ "Y800") {{
                setData(data);
            }};

            int result = mScanner.scanImage(barcode);

            if (result == 0) {
                return;
            }

            mIsPreviewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();

            SymbolSet symbolSet = mScanner.getResults();
            Iterator<Symbol> symbolIterator = symbolSet.iterator();
            if (!symbolIterator.hasNext()) {
                return;
            }

            final String text = symbolIterator.next().getData();

            setResult(RESULT_OK, new Intent() {{
                putExtra(INTENT_RESULT_KEY, text);
            }});

            finish();
        }
    };

    @Override
    public void finish() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        super.finish();
    }
}
