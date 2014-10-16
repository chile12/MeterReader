package ccc.android.meterreader.camerafragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.ImageScanner;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterreader.R;
import ccc.android.meterreader.gaugedisplaydialog.GaugeDisplayDialog;
import ccc.android.meterreader.internaldata.InternalGaugeDevice;
import ccc.android.meterreader.statics.StaticGaugeLibrary;
import ccc.android.meterreader.statics.Statics;
import ccc.android.meterreader.viewelements.CccNumberPicker;
import ccc.java.digitextractor.DigitReadingProcessor;
import ccc.java.digitextractor.ImageStatics;
import ccc.java.digitextractor.MeterImage;
import ccc.java.digitextractor.data.BackGroundShade;
import ccc.java.digitextractor.exceptions.FrameNotFoundException;
import ccc.java.simpleocr.DigitOcr;

public class DigitReaderFragment extends DialogFragment
{
	private ImageScanner scanner;
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private ViewGroup containerView;
    private static DigitOcr standardOcr;
    private GaugeDevice device;
    private int callCount = 0;
    private boolean isFocused = false;
    private List<String> readings = new ArrayList<String>();
	private List<CccNumberPicker> readerPickers = new ArrayList<CccNumberPicker>();


	private DigitReadingProcessor processor;
    private Rect frame;
    private int count =0;
    
    public ViewGroup getContainerView() {
		return containerView;
	}

	public void setContainerView(ViewGroup containerView) {
		this.containerView = containerView;
	}


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
        
        try{
	        int deviceId = ((ccc.android.meterreader.gaugedisplaydialog.GaugeDisplayDialog)(this.getActivity())).getCurrentDeviceID();
			InternalGaugeDevice zeroDevice = StaticGaugeLibrary.getGauge(0);
			device = StaticGaugeLibrary.getGauges().get(deviceId);
			processor = new DigitReadingProcessor(zeroDevice.getBinaryPatterns(), ImageFormat.NV21);
			processor.initializeReadingSession(BackGroundShade.GetBackGroundShade(device.getBackGround()), device.getDigitCount(), device.getDecimalPlaces());
			processor.setCameraOrientation(DigitReadingProcessor.CAMERA_ORIENT_PORTRAIT);
		} catch (Exception e) {
			if(e.getMessage() == null || !e.getMessage().contains("java.awt")) //not!!
				e.getMessage();
		}
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	InitializeCamera();
    	readings.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
		    List<Camera.Size> pSizes = p.getSupportedPreviewSizes();
		    Collections.sort(pSizes, Statics.previewSizeComp);
		    p.setPreviewSize(pSizes.get(1).width, pSizes.get(1).height);
            int x = (int)(pSizes.get(1).width*((float)3/(float)12));
            int y = 0;
    		int x2 =(int)(pSizes.get(1).width*((float)2/(float)12));
    		int y2 = pSizes.get(1).height-1;
			frame = new Rect(x, y, x2, y2);
	        
		    p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		    p.setPreviewFormat(ImageFormat.NV21);
			mCamera.setParameters(p);
	        //mCamera.setPreviewCallback(previewCb);
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

    PreviewCallback previewCb = new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
            	callCount++;
            	if(callCount%3 != 0)
            		return;
            	
					Camera.Parameters parameters = camera.getParameters();					
					if(data != null)
					{
						try
						{
				            int w = parameters.getPreviewSize().width;
				            int h = parameters.getPreviewSize().height;
				            
				            processor.addReadingImage(data, w, h, frame);
							if(processor.getCurrent().GetSharpness() < 100)
							{
								doAutoFocus.run();
								return;
							}

				            processor.processLastEntry();
				            processor.alignResults();

							String aligned = processor.getAligner().getCurrentResult();
							String result = aligned == null ? processor.getCurrent().getPreciseChars() : aligned;
							result = result.substring(0, Math.min(result.length(), device.getDigitCount()));
							
							((GaugeDisplayDialog)getActivity()).SetValue(result.toCharArray());
							((GaugeDisplayDialog)getActivity()).showValue();
				            
//							Mat bgra = new Mat(h, w, org.bytedeco.javacpp.opencv_core.CV_8UC3);
//							Mat b565= new Mat(h + (h/2), w, org.bytedeco.javacpp.opencv_core.CV_8UC1);
//			                int x = (int)(w*((float)7/(float)16));
//			                int y = (int)(h*((float)1/(float)9));
//			        		w =(int)(w*((float)1/(float)8));
//			        		h = (int)(h*((float)7/(float)9));
//			    			frame = new org.bytedeco.javacpp.opencv_core.Rect(x, y, w, h);
//			    			
//							b565.getByteBuffer().put(data);							
//							org.bytedeco.javacpp.opencv_imgproc.cvtColor(b565, bgra, org.bytedeco.javacpp.opencv_imgproc.CV_YUV2BGR_NV21);
//						    bgra = ImageStatics.CropImage(bgra, frame);		
//							Mat trans = new Mat(h, w, bgra.type() );
//						    org.bytedeco.javacpp.opencv_core.transpose(bgra, trans);
//						    org.bytedeco.javacpp.opencv_core.flip(trans, trans, 1);
//							MeterImage imProc = new MeterImage(trans, device.getDigitCount(), BackGroundShade.GetBackGroundShade(device.getBackGround()),standardOcr, false);
//							if(imProc.GetSharpness() < 100)
//							{
//								doAutoFocus.run();
//								imProc.release();
//								return;
//							}
//	
//							///DEBUG-STUFF
//				            Mat grayscale = processor.getCurrent().getGrayScale();
//				            
//							Mat rgba = new Mat(grayscale.size().height(), grayscale.size().width(), org.bytedeco.javacpp.opencv_core.CV_8UC4);
//							org.bytedeco.javacpp.opencv_imgproc.cvtColor(grayscale, rgba, org.bytedeco.javacpp.opencv_imgproc.CV_GRAY2RGBA);
//							byte[] zz = new byte[rgba.getByteBuffer().capacity()];
//							rgba.getByteBuffer().get(zz);
//							Bitmap bitmap = Bitmap.createBitmap(rgba.size().width(), rgba.size().height(), Bitmap.Config.ARGB_8888);
//							bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(zz));
//							((GaugeDisplayDialog)(getActivity())).setDebugPreviewImage(bitmap);
//							File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "debug");
//
//					        try {
//					            FileOutputStream outstream = new FileOutputStream(new File(f, "dbg.jpg"));
//					            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
//					            outstream.flush();
//					            outstream.close();
//					        } catch (FileNotFoundException e) {
//					            //
//					        } catch (IOException e) {
//					            //
//					        }
//							rgba.release();
//							///DEBUG-STUFF-END
//							Date d = new Date();
//							imProc.ExtractDigits();
//							imProc.release();
//							//readings.add(imProc.getPreciseChars());
//							Log.d("ExecTime", String.valueOf(Statics.getDateDiff(d, new Date(), TimeUnit.MILLISECONDS)));
				            
//							View zw = (((GaugeDisplayDialog)(getActivity())).findViewById(R.id.debugDigitLA));
//							count++;
//							((android.widget.TextView)zw).setText(count + " - " + result);
				            
				            
						//} catch (FrameNotFoundException e) {
						} catch (Exception e) {	
							if(e.getMessage() == null || !e.getMessage().contains("java.awt")) //not!!
								e.getMessage();
						}
						//TODO
						
					}
            }
        };
        
        public List<CccNumberPicker> getReaderPickers()
    	{
    		return readerPickers;
    	}

    	public void setReaderPickers(List<CccNumberPicker> readerPickers)
    	{
    		this.readerPickers = readerPickers;
    	}
    	
        public static Bitmap IplImageToBitmap(IplImage src) {
            int width = src.width();
            int height = src.height();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for(int r=0;r<height;r++) {
                for(int c=0;c<width;c++) {
                    CvScalar gray = org.bytedeco.javacpp.opencv_core.cvGet2D(src,r,c);
                    bitmap.setPixel(c, r, Color.argb(255, (int)gray.get(0), (int)gray.get(0), (int)gray.get(0)));
                }
            }
            return bitmap;
        }
        
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
//                autoFocusHandler.postDelayed(doAutoFocus, 3000);
//                if(success)
//                {
//                	isFocused = true;
//                	Log.d("focus", "1");
//                }
//                else
//                {
//                	isFocused = false;
//                	Log.d("focus", "0");
//                }
            }
        };
}
