/*
 * Barebones implementation of displaying camera preview.
 * 
 * Created by lisah0 on 2012-02-24
 */
package ccc.android.meterreader.camerafragments;

import java.io.IOException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;

import android.view.Display;
import android.view.View;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback previewCallback;
    private AutoFocusCallback autoFocusCallback;
    private Camera.Parameters parameters;
    private boolean scalePreviewToDisplaySize = true;
    
    
    public CameraPreview(Context context, Camera camera,PreviewCallback previewCb,AutoFocusCallback autoFocusCb) 
    {
        super(context);
        mCamera = camera;
        setCameraDisplayOrientation(mCamera);
        previewCallback = previewCb;
        autoFocusCallback = autoFocusCb;

        mHolder = getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.addCallback(this);
    }
    
    private void setCameraDisplayOrientation(Camera camera)
    {
        int rotation = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result= (CameraStatics.BACK_CAMERA_ORIENTATION - degrees + 360) % 360;
        setDisplayOrientation(camera, result);

    }
    
    private void setDisplayOrientation(Camera camera, int angle)
    {
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera.setPreviewDisplay(holder);
            //mCamera.autoFocus(autoFocusCallback);
        } catch (IOException e) {
            Log.d("DBG", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
    {
        try {
            mCamera.stopPreview();
        } catch (Exception e){
        }
        
        if(scalePreviewToDisplaySize)
        {
	        Parameters parameters = mCamera.getParameters();
	        parameters.setPreviewSize(width, height);
	        mCamera.setParameters(parameters);
        }
        
        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }


        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCallback);
        } catch (Exception e){
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }



	public boolean isScalePreviewToDisplaySize() {
		return scalePreviewToDisplaySize;
	}

	public void setScalePreviewToDisplaySize(boolean scalePreviewToDisplaySize) {
		this.scalePreviewToDisplaySize = scalePreviewToDisplaySize;
	}
	
}
