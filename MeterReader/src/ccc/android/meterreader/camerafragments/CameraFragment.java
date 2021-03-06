/*


 * 
 * , , files.add(Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * Created by lisah0 on 2012-02-24
 */
package ccc.android.meterreader.camerafragments;

import java.util.Collections;
import java.util.List;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.DialogFragment;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import ccc.android.meterreader.statics.Statics;
/* Import ZBar Class files */

public class CameraFragment extends DialogFragment
{
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private ViewGroup containerView;
    private String targetIntent;
    private PictureCallback pictureCallback;

	public ViewGroup getContainerView() {
		return containerView;
	}

	public void setContainerView(ViewGroup containerView) {
		this.containerView = containerView;
	}

	ImageScanner scanner;

    private boolean previewing = true;

    static {
        System.loadLibrary("iconv");
    } 
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autoFocusHandler = new Handler();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	InitializeCamera();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    		return inflater.inflate(ccc.android.meterreader.R.layout.cmerapreviewll, containerView);
    }

    @Override
    public void onPause() {
        super.onPause();        
        ReleaseCamera();
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    public void ReleaseCamera() {
        if (mCamera != null) {
    		Parameters p = mCamera.getParameters();
        	p.setFlashMode(Parameters.FLASH_MODE_OFF);
    		mCamera.setParameters(p);
            previewing = false;
            mPreview.getHolder().removeCallback(mPreview);
	        LinearLayout preview = (LinearLayout)getActivity().findViewById(ccc.android.meterreader.R.id.camerapreview);
	        preview.removeView(mPreview);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mPreview = null;
        }
    }
    
	public void InitializeCamera() {
		if (mCamera == null) 
		{
			mCamera = CameraStatics.getCameraWithId(CameraInfo.CAMERA_FACING_BACK);
			Parameters p = mCamera.getParameters();
			p.setPictureSize(640, 480);
		    p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		    List<Camera.Size> pSizes = p.getSupportedPreviewSizes();
		    Collections.sort(pSizes, Statics.previewSizeComp);
		    p.setPreviewSize(pSizes.get(1).width, pSizes.get(1).height);
		    mCamera.setParameters(p);
	        previewing = true;
			mPreview = new CameraPreview(getActivity(), mCamera, previewCb, autoFocusCB);
			mPreview.setScalePreviewToDisplaySize(false);
	        LinearLayout preview = (LinearLayout)getActivity().findViewById(ccc.android.meterreader.R.id.camerapreview);
	        preview.addView(mPreview);
	        mCamera.startPreview();
		}
	}

    private Runnable doAutoFocus = new Runnable() {
            public void run() {
                if (previewing)
                {
					mCamera.autoFocus(autoFocusCB);
                }
            }
        };

    PreviewCallback previewCb = new PreviewCallback() 
    {
        public void onPreviewFrame(byte[] data, Camera camera) {
        	if(pictureCallback == null)
        	{
				Camera.Parameters parameters = camera.getParameters();
				Size size = parameters.getPreviewSize();
				Image barcode = new Image(size.width, size.height, "Y800");
				barcode.setData(data);
				int result = scanner.scanImage(barcode);
				if (targetIntent != null && result != 0) {
					SymbolSet syms = scanner.getResults();
					for (Symbol sym : syms) {
						Intent i = new Intent(targetIntent).putExtra("barcode", sym.getData());
						CameraFragment.this.getActivity().sendBroadcast(i);
						getActivity().finish();
					}
				}
			}
        }
    };
    
    public void takePicture()
    {
    	if(pictureCallback != null)
    	{
    		mCamera.takePicture(null, null, pictureCallback);
    	}
    }
    
    public void restartPreview()
    {
    	ReleaseCamera();
    	mCamera = null;
    	InitializeCamera();
    }
        
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() 
    {
        public void onAutoFocus(boolean success, Camera camera) {
        	if(success)
        		autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    public String getTargetIntent()
	{
		return targetIntent;
	}

	public void setTargetIntent(String targetIntent)
	{
		this.targetIntent = targetIntent;
	}
	public PictureCallback getPictureCallback()
	{
		return pictureCallback;
	}

	public void setPictureCallback(PictureCallback pCallback)
	{
		this.pictureCallback = pCallback;
	}
}
