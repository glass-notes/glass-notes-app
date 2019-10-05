package io.p13i.glassnotes.utilities;

/*
 * Copyright (C) 2012 ZXing authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.hardware.Camera;
import android.util.Log;

public final class CameraUtilities {


    private static final String TAG = CameraUtilities.class.getName();
    private static final int MAX_WAIT_TIME = 10000;

    private CameraUtilities() {
    }

    /**
     * Opens a rear-facing camera with {@link Camera#open(int)}, if one exists,
     * or opens camera 0.
     *
     * @throws InterruptedException
     */
    public static Camera open() throws InterruptedException {

        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.w(TAG, "No cameras!");
            return null;
        }

        int index = 0;
        while (index < numCameras) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                break;
            }
            index++;
        }

        Camera camera = null;
        long timeout = System.currentTimeMillis() + MAX_WAIT_TIME;
        int attempt = 0;
        while (camera == null && System.currentTimeMillis() < timeout) {
            attempt++;
            Log.v(TAG, "Sleeping 100ms - attempt " + attempt);
            Thread.sleep(100);

            try {
                if (index < numCameras) {
                    Log.i(TAG, "Opening camera #" + index);
                    camera = Camera.open(index);
                } else {
                    Log.i(TAG, "No camera facing back; returning camera #0");
                    camera = Camera.open(0);
                }
            } catch (RuntimeException e) {
                Log.w(TAG, "RuntimeException: " + e.getMessage());
            }
        }

        return camera;
    }
}
