package ccc.android.meterreader.deviceregistration;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.Image;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.R;
import ccc.android.meterreader.camerafragments.CameraFragment;
import ccc.android.meterreader.camerafragments.CameraStatics;
import ccc.android.meterreader.internaldata.InternalImage;
import ccc.android.meterreader.statics.StaticPreferences;
import ccc.android.meterreader.statics.Statics;

@SuppressLint("UseSparseArrays")
public class DeviceRegistrationFragment extends DialogFragment
{
    private GaugeDevice device;
    private Gauge gauge;
    private Reading lastRead;
    private Reading firstRead;
    private boolean expectFirstRead = false;
    private boolean isExchange = false;
    private String barcode;
	private Map<Integer, Boolean> registrationSteps = new HashMap<Integer, Boolean>();
	private LinearLayout mainLL;
    private ViewGroup containerView;
    private ViewFlipper flipper;
    private NumberPicker digitNP;
    private NumberPicker decimalNP;
    private RadioButton darkShadeRB;
    private RadioButton brightShadeRB;
    private Button next;
    private Button back;
    private Button confirm;
    private Spinner gaugeDD;
    
    private CameraFragment cameraFragment;
    
    public ViewGroup getContainerView() {
		return containerView;
	}

	public void setContainerView(ViewGroup containerView) {
		this.containerView = containerView;
	}
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
	public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    }

	private void initializeDevice(Gauge ga)
	{
		this.gauge = ga;
		GaugeDevice de = ((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getDevices().getById(gauge.getGaugeDeviceId());
		this.device = new GaugeDevice();
		device.setGaugeId(gauge.getGaugeId());
		device.setUtcInstallation(Statics.getUtcTime());
        registrationSteps.put(0, false);
        registrationSteps.put(1, false);
        registrationSteps.put(2, true);
        registrationSteps.put(3, false);
        registrationSteps.put(4, false);
        registrationSteps.put(5, true);
        registrationSteps.put(6, true);
        
		//TODO
		//new gauge
		if(de != null)
		{
			isExchange = true;
			lastRead = ((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getReadings().getLastReadingById(gauge.getGaugeId());
		}
        initializeNumPickers(mainLL);
	}
    
    @Override
    public void onResume() {
    	super.onResume();
		Statics.ChangeLayoutElementsVisibility(mainLL, View.INVISIBLE, View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    	mainLL = (LinearLayout)inflater.inflate(ccc.android.meterreader.R.layout.deviceregistration, containerView, false);
    	((LinearLayout) mainLL.findViewById(R.id.cameraPreviewLL)).setOnClickListener(takePictureListener);
        ((Button) mainLL.findViewById(R.id.takePictureBT)).setOnClickListener(this.takePictureListener);
        flipper = (ViewFlipper) mainLL.findViewById(R.id.newDeviceVF);
        confirm = ((Button) mainLL.findViewById(R.id.newDeviceConfirmBT));
        confirm.setOnClickListener(confirmListener);
        ((Button) mainLL.findViewById(R.id.newDeviceAbortBT)).setOnClickListener(abortListener);
        ((Button) mainLL.findViewById(R.id.tryAgainBT)).setOnClickListener(tryAgainListener);
        ((Button) mainLL.findViewById(R.id.newDeviceStep0BarcodeBT)).setOnClickListener(barcodeScanListener);
        next = ((Button) mainLL.findViewById(R.id.newDeviceNextBT));
        next.setOnClickListener(nextListener);
        back = ((Button) mainLL.findViewById(R.id.newDeviceBackBT));
        back.setOnClickListener(backListener);
        darkShadeRB = (RadioButton) mainLL.findViewById(R.id.newDeviceStep5darkRB);
        darkShadeRB.setOnCheckedChangeListener(backGroundChanged);
        brightShadeRB = (RadioButton) mainLL.findViewById(R.id.newDeviceStep5brightRB);
        brightShadeRB.setOnCheckedChangeListener(backGroundChanged);
        ((EditText) mainLL.findViewById(R.id.newDeviceStep3ManufTB)).setOnFocusChangeListener(editTextFocusChanged);
        ((EditText) mainLL.findViewById(R.id.newDeviceStep3SerialTB)).setOnFocusChangeListener(editTextFocusChanged);
        ((EditText) mainLL.findViewById(R.id.newDeviceStep7DescriptionTB)).setOnFocusChangeListener(editTextFocusChanged);

        gaugeDD = (Spinner) mainLL.findViewById(R.id.newDeviceStep0GaugeDD);
        List<String> gaugeStrings = ((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getGauges().GetGaugeNames();
        gaugeStrings.add(0, "");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, gaugeStrings);
        gaugeDD.setAdapter(spinnerArrayAdapter);
        gaugeDD.setOnItemSelectedListener(gaugeSelectedListener);

		return mainLL;
    }

	private void initializeNumPickers(LinearLayout ll)
	{
		digitNP = (NumberPicker) ll.findViewById(R.id.newDeviceStep4DigitNP);
        decimalNP = (NumberPicker) ll.findViewById(R.id.newDeviceStep4DecimalNP);
        digitNP.setMaxValue(9);
        digitNP.setMinValue(0);
        digitNP.setDisplayedValues(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        digitNP.setValue(device.getDigitCount());
        digitNP.setOnValueChangedListener(digitChanged);
        decimalNP.setMaxValue(5);
        decimalNP.setMinValue(0);
        decimalNP.setDisplayedValues(new String[]{"0", "1", "2", "3", "4", "5"});
        decimalNP.setValue(device.getDecimalPlaces());
        decimalNP.setOnValueChangedListener(decimalChanged);
	}

    @Override
    public void onPause() {
        super.onPause();     
    }
    
    private OnFocusChangeListener editTextFocusChanged = new OnFocusChangeListener()
    {
		@Override
		public void onFocusChange(View arg0, boolean arg1)
		{
			EditText et = (EditText) arg0;
			if(arg1)
			{
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
			}
			else
			{
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
			}
		}
    };
    
    private OnItemSelectedListener gaugeSelectedListener = new OnItemSelectedListener()
    {
		@Override
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
		{
			if(position == 0)
				return;
	        initializeDevice(((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getGauges().getGaugeList().get(position-1));
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
			// TODO Auto-generated method stub
			
		}
    };
    
    private OnCheckedChangeListener backGroundChanged = new OnCheckedChangeListener()
    {
    	boolean isNotActive = true;
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if(isNotActive)
			{
				if(isChecked)
				{
					registrationSteps.put(flipper.getDisplayedChild(), true);
					if(darkShadeRB == buttonView)
					{
						device.setBackGround(2);
						isNotActive = false;
						brightShadeRB.setChecked(false);
						isNotActive = true;
					}
					else if (brightShadeRB == buttonView)
					{
						device.setBackGround(1);
						isNotActive = false;
						darkShadeRB.setChecked(false);
						isNotActive = true;
					}
				}
				else
					buttonView.setChecked(true);
			}
		}
    };
    
    private OnValueChangeListener digitChanged = new OnValueChangeListener()
    {
		@Override
		public void onValueChange(NumberPicker arg0, int oldVal, int newVal)
		{
			registrationSteps.put(flipper.getDisplayedChild(), true);
			device.setDigitCount(newVal+1);
		}
    };
    
    private OnValueChangeListener decimalChanged = new OnValueChangeListener()
    {
		@Override
		public void onValueChange(NumberPicker arg0, int oldVal, int newVal)
		{
			if(device.getDigitCount() > newVal)
				device.setDecimalPlaces(newVal);
			else
			{
				//TODO
				Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.new_device_decimal_warning), 
						StaticPreferences.getPreference(Statics.SHORT_DIALOG_DURATION, Statics.SHORT_DIALOG_DURATION_DEFAULT));
				arg0.setValue(0);
			}
		}
    };
    
    private OnClickListener nextListener = new OnClickListener() {
        public void onClick(View v) {
        	back.setVisibility(View.VISIBLE);
        	ViewGroup flipperChild = (ViewGroup) flipper.getChildAt(flipper.getDisplayedChild());
    		List<EditText> edits = Statics.GetSubElementsOfType(flipperChild, EditText.class);
        	for(int i =0; i < edits.size(); i++)
        	{
        			EditText et = edits.get(i);
        			fillNewDevice(et);
        	}
        	if(flipper.getDisplayedChild() == 0)
        	{
        		if(gauge == null)
        		{
        			Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.new_device_step0_barcode_select_gauge), 
        					StaticPreferences.getPreference(Statics.SHORT_DIALOG_DURATION, Statics.SHORT_DIALOG_DURATION_DEFAULT));
        			return;
        		}
        		if(isExchange)
        		{
        			if(lastRead == null || Statics.getDateDiff(lastRead.getUtcTo(), Statics.getUtcTime(), TimeUnit.MINUTES) > 15)
        			{
	        			Statics.ShowToast(Statics.getDefaultResources().getString(R.string.new_device_take_last_read));
	        			GaugeDevice de = ((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getDevices().getById(gauge.getGaugeDeviceId());
	        			((ccc.android.meterreader.MainActivity) getActivity()).openGaugeDisplayDialog(gauge, de, lastRead);
	        			return;
        			}
        		}
        		else
        		{
        			expectFirstRead = true;
        			if(barcode == null || barcode.trim() == "")
        			{
	        			((ccc.android.meterreader.MainActivity) getActivity()).openGaugeDisplayDialog(Statics.ANDROID_INTENT_ACTION_NBFR);
	        			return;
        			}
        		}
        		registrationSteps.put(flipper.getDisplayedChild(), true);
        	}
        	if(!registrationSteps.get(flipper.getDisplayedChild())) //not!
        	{
        		Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.new_device_next_step_warning));
        		return;
        	}
            nextView();
        }
    };
    
	private void fillNewDevice(EditText et)
	{
		if(et.getText().length() == 0)
			return;
		if(et.getId() == R.id.newDeviceStep3ManufTB)
		{
			device.setManufacturer(et.getText().toString());
		}
		else if(et.getId() == R.id.newDeviceStep3SerialTB)
		{
			device.setSerialNumber(et.getText().toString());
		}
		else if(et.getId() == R.id.newDeviceStep7DescriptionTB)
		{
			device.setComment(et.getText().toString());
		}
	}
    
    private OnClickListener confirmListener = new OnClickListener() {
        public void onClick(View v) 
        {
        	int regStep = getUnfinishedRegistrationStep();
        	if(regStep == flipper.getDisplayedChild())
        	{
        		if(barcode != null && barcode.trim().length() > 0)
        			device.setBarcode(barcode);
    			Statics.ShowToast(Statics.getDefaultResources().getString(R.string.new_device_take_first_read));
        		((ccc.android.meterreader.MainActivity) getActivity()).openGaugeDisplayDialog(gauge, device, null);
        		Statics.ChangeLayoutElementsVisibility(mainLL, View.VISIBLE, View.INVISIBLE);
        	}
        	else
        	{
        		next.setVisibility(View.VISIBLE);
        		confirm.setVisibility(View.INVISIBLE);
        		flipper.setDisplayedChild(regStep);
        	}
        }
    };    
    
    private OnClickListener abortListener = new OnClickListener() {
        public void onClick(View v) 
        {
        	resetRegistration();
        }
    };
    
    private OnClickListener tryAgainListener = new OnClickListener() {
        public void onClick(View v) 
        {
        	if(cameraFragment != null)
        	{
        		cameraFragment.restartPreview();
        	}
        }
    };
    
    private OnClickListener takePictureListener = new OnClickListener() {
        public void onClick(View v) 
        {
        	if(cameraFragment != null)
        	{
        		cameraFragment.setPictureCallback(pCallback);
        		cameraFragment.takePicture();
        	}
        }
    };
    
    PictureCallback pCallback = new PictureCallback()
    {
		@Override
		public void onPictureTaken(byte[] data, Camera arg1)
		{
            Bitmap bit = BitmapFactory.decodeByteArray(data, 0, data.length);
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        	Camera.getCameraInfo(CameraStatics.BACK_CAMERA_ID, cameraInfo);
            int w = bit.getWidth();
            int h = bit.getHeight();
            Matrix mtx = new Matrix();
            mtx.postRotate(cameraInfo.orientation);
            bit = Bitmap.createBitmap(bit, 0, 0, w, h, mtx, true);
            
			Image img = new Image();

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bit.compress(Bitmap.CompressFormat.PNG, 100, stream);
			img.setBinary(stream.toByteArray());
			device.setImage(img);
			registrationSteps.put(flipper.getDisplayedChild(), true);
		}
    };

    private OnClickListener barcodeScanListener = new OnClickListener() {
        public void onClick(View v) 
        {
        	((ccc.android.meterreader.MainActivity) getActivity()).openGaugeDisplayDialog(Statics.ANDROID_INTENT_ACTION_RBFR);
        }
    };
    
	private void resetRegistration()
	{
		((ccc.android.meterreader.MainActivity) getActivity()).openOptionsPanelLayout();
		((ccc.android.meterreader.MainActivity) getActivity()).hideNewDeviceFragment();
	}
	
	private void nextView()
	{
    	if(flipper.getDisplayedChild() == 1)
    	{
    		((EditText) mainLL.findViewById(R.id.newDeviceStep3ManufTB)).requestFocus();
    	}
    	if(flipper.getDisplayedChild() == 4)
    	{
    		((EditText) mainLL.findViewById(R.id.newDeviceStep7DescriptionTB)).requestFocus();
    	}
    	if(flipper.getDisplayedChild() < flipper.getChildCount()-2)
    	{
    		next.setVisibility(View.VISIBLE);
    		confirm.setVisibility(View.INVISIBLE);
    		flipper.setDisplayedChild(flipper.getDisplayedChild()+1);
    	}
    	else if(flipper.getDisplayedChild() < flipper.getChildCount()-1)
    	{
    		next.setVisibility(View.INVISIBLE);
    		confirm.setVisibility(View.VISIBLE);
    		flipper.setDisplayedChild(flipper.getDisplayedChild()+1);
    	}
    	if(flipper.getDisplayedChild() == 1)
    		getCameraFragment();
    	else
    		hideCameraFragment();
    	if(flipper.getDisplayedChild() == 6)
    	{
    		if(device.getImage() != null)
    		{
    			InternalImage img = new InternalImage(device.getImage());
    			((ImageView) mainLL.findViewById(R.id.newDeviceStep8DeviceIV)).setImageBitmap(img.getBitmap());
    		}
    		if(gauge != null)
    		{
    			Statics.SetText(((TextView) mainLL.findViewById(R.id.newDeviceStep8GaugeNamelLA)), gauge.getName());
    			Statics.SetText(((TextView) mainLL.findViewById(R.id.newDeviceStep8BarcodeLA)), device.getBarcode() == null ? gauge.getBarcode() : device.getBarcode());
    		}
    		if(barcode != null)
    			Statics.SetText(((TextView) mainLL.findViewById(R.id.newDeviceStep8BarcodeLA)), barcode);
    		
			Statics.SetText(((TextView) mainLL.findViewById(R.id.newDeviceStep8GaugeIdlLA)), device.getGaugeId());
			Statics.SetText(((TextView) mainLL.findViewById(R.id.newDeviceStep8CommentLA)), device.getComment());
			Statics.SetText(((TextView) mainLL.findViewById(R.id.newDeviceStep8DecimalLA)), device.getDecimalPlaces());
			Statics.SetText(((TextView) mainLL.findViewById(R.id.newDeviceStep8DigitsLA)), device.getDigitCount());
			if(device.getBackGround() == 1)
    			((ImageView) mainLL.findViewById(R.id.newDeviceStep8BackgroundIV)).setImageResource(R.drawable.darkone);
			if(device.getBackGround() == 2)
    			((ImageView) mainLL.findViewById(R.id.newDeviceStep8BackgroundIV)).setImageResource(R.drawable.brightone);
    	}
	}
    
    private OnClickListener backListener = new OnClickListener() {
        public void onClick(View v) {
    		next.setVisibility(View.VISIBLE);
    		confirm.setVisibility(View.INVISIBLE);
        	if(flipper.getDisplayedChild() > 1)
        		flipper.setDisplayedChild(flipper.getDisplayedChild()-1);
        	else if(flipper.getDisplayedChild() > 0)
        	{
        		back.setVisibility(View.INVISIBLE);
        		flipper.setDisplayedChild(flipper.getDisplayedChild()-1);
        	}
        	if(flipper.getDisplayedChild() == 1)
        		getCameraFragment();
        	else
        		hideCameraFragment();
        }
    };

	public GaugeDevice getDevice()
	{
		return device;
	}

	public void setDevice(GaugeDevice device)
	{
		this.device = device;
	}
	
	private int getUnfinishedRegistrationStep()
	{
		for(int i =0; i < registrationSteps.size(); i++)
		{
			if(!registrationSteps.get(i))
				return i;
		}
		return registrationSteps.size()-1;
	}

	public Gauge getGauge()
	{
		return gauge;
	}

	public void setGauge(Gauge gauge)
	{
		if(gauge != null)
		{
			this.initializeDevice(gauge);
			for(int i =1; i < gaugeDD.getCount(); i++)
				if(gaugeDD.getItemAtPosition(i) instanceof String && gaugeDD.getItemAtPosition(i).equals(gauge.getName()))
					gaugeDD.setSelection(i);
		}
		else
			Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.new_device_step0_gauge_not_found));
	}

	public Reading getLastRead()
	{
		return lastRead;
	}

	public void setNewRead(Reading read)
	{
		if(expectFirstRead)  //complete registration
		{
			this.firstRead = read;
			if(lastRead != null)
				device.setValueOffset(device.getValueOffset() + lastRead.getRead() - firstRead.getRead());
			else
				device.setValueOffset(device.getValueOffset());
    		((ccc.android.meterreader.MainActivity) getActivity()).getManager().AddNewDevice(device);
    		resetRegistration();
		}
		else
		{
			this.lastRead = read;
			expectFirstRead = true;
		}
	}

	public String getBarcode()
	{
		return barcode;
	}

	public void setBarcode(String barcode, Gauge ga)
	{
		this.barcode = barcode;
		if(ga != null)
		{
			ga.setBarcode(barcode);
		}
	}

	public Reading getFirstRead()
	{
		return firstRead;
	}
	
	private void getCameraFragment() {
		FragmentManager fragmentManager = this.getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		cameraFragment = (CameraFragment) fragmentManager.findFragmentByTag("devicePicture");
		if(cameraFragment != null)
		{
			cameraFragment.InitializeCamera();
			fragmentTransaction.show(cameraFragment);
		}
		else
		{
			cameraFragment = new ccc.android.meterreader.camerafragments.CameraFragment();
			fragmentTransaction.add(R.id.cameraPreviewLL, cameraFragment, "devicePicture");
		}
		fragmentTransaction.commit();
	}
	
	private void hideCameraFragment() 
	{
		if(cameraFragment == null)
			return;
		FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        cameraFragment = (CameraFragment) fragmentManager.findFragmentByTag("devicePicture");
        if (cameraFragment != null) {
        	cameraFragment.ReleaseCamera();
			fragmentTransaction.remove(cameraFragment);
			fragmentTransaction.commit();
			cameraFragment = null;
		}
	}
}