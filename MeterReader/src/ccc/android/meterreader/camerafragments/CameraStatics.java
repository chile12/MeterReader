package ccc.android.meterreader.camerafragments;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.widget.LinearLayout;

public class CameraStatics {
	
	public static String CAMERA_INDEX_KEY = "camId";
	public static String CAMERA_ORIENTATION_KEY = "camOrientation";
	public static int BACK_CAMERA_ID;
	public static int BACK_CAMERA_ORIENTATION;
	
	private static boolean previewing = false;
		
    public static Camera getCameraWithId(int CameraFacing) {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        Parameters camParams = null;
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == CameraFacing) {
                try {
                    cam = Camera.open(camIdx);
                    camParams = cam.getParameters();
                    BACK_CAMERA_ORIENTATION = cameraInfo.orientation;
                    BACK_CAMERA_ID = camIdx;
                    camParams.set(CAMERA_INDEX_KEY, camIdx);
                    camParams.set(CAMERA_ORIENTATION_KEY, cameraInfo.orientation);
                    cam.setParameters(camParams);
                } catch (RuntimeException e) {
                    Log.e("Camera", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }
    
    public static CameraInfo GetCamerainfo(Camera cam)
    {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    	Camera.getCameraInfo(cam.getParameters().getInt(CAMERA_INDEX_KEY), cameraInfo);
    	return cameraInfo;
    }

	public static boolean isPreviewing() {
		return previewing;
	}

	public static void setPreviewing(boolean previewing) {
		CameraStatics.previewing = previewing;
	}

}
