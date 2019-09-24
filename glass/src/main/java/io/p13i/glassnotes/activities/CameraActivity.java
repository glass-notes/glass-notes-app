package io.p13i.glassnotes.activities;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import io.p13i.glassnotes.R;
import io.p13i.glassnotes.camera.CameraPreview;

import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;


public class CameraActivity extends GlassNotesActivity {
    private static final String TAG = CameraActivity.class.getName();
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private Camera mCamera;
    private ImageScanner scanner;
    private boolean previewing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getActionBar().hide();

        autoFocusHandler = new Handler();

        //For some reason, right after launching from the "ok, glass" menu the camera is locked
        //Try 3 times to grab the camera, with a short delay in between.
        for(int i=0; i < 3; i++)
        {
            mCamera = getCameraInstance();
            if(mCamera != null) break;

            Log.d(TAG, "Couldn't lock camera, will try " + (2-i) + " more times...");

            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        if(mCamera == null)
        {
            Toast.makeText(this, "Camera cannot be locked", Toast.LENGTH_SHORT).show();
            finish();
        }

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        super.onCreate(savedInstanceState);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
            Log.d(TAG, "getCamera = " + c);
        } catch (Exception e){
            Log.d(TAG, e.toString());
        }
        return c;
    }

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    String text = sym.getData();
                    Log.d(TAG, text);
                    break;
                }
            }
        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };
}
