package io.p13i.glassnotes.camera;

import java.io.IOException;

import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import android.content.Context;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;

/**
 * A basic Camera Preview class
 * Jay Zuerndorfer: "Created by Jay Zuerndorfer on 2013-09-20. Based off of lisah0's demo code"
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    final static String TAG = CameraPreview.class.getName();

    private Camera mCamera;
    private PreviewCallback mPreviewCallback;
    private AutoFocusCallback mAutoFocusCallback;

    public CameraPreview(Context context, Camera camera, PreviewCallback previewCallback, AutoFocusCallback autoFocusCallback) {
        super(context);

        mCamera = camera;
        mPreviewCallback = previewCallback;
        mAutoFocusCallback = autoFocusCallback;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
         * If your preview can change or rotate, take care of those events here.
         * Make sure to stop the preview before resizing or reformatting it.
         */
        if (getHolder().getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        try {

            mCamera.setPreviewDisplay(getHolder());
            mCamera.setPreviewCallback(mPreviewCallback);

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(640, 480);
            mCamera.setParameters(parameters);

            mCamera.startPreview();
            mCamera.autoFocus(mAutoFocusCallback);

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}