package ccc.android.meterreader.deviceregistration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.R;
import ccc.android.meterreader.statics.Statics;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RadioButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.ViewFlipper;

@SuppressLint("UseSparseArrays")
public class DeviceRegistrationFragment extends DialogFragment
{
    private GaugeDevice device;
    private Gauge gauge;
    private Reading lastRead;
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
    private EditText nameTB;
    private Spinner gaugeDD;
    
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
    public void onStart() {
    	super.onStart();
    }

	private void initializeDevice()
	{
		this.device = new GaugeDevice();
        registrationSteps.put(0, false);
        registrationSteps.put(1, true);
        registrationSteps.put(2, false);
        registrationSteps.put(3, true);
        registrationSteps.put(4, false);
        registrationSteps.put(5, false);
        registrationSteps.put(6, true);
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    	mainLL = (LinearLayout)inflater.inflate(ccc.android.meterreader.R.layout.deviceregistration, containerView, false);
        flipper = (ViewFlipper) mainLL.findViewById(R.id.newDeviceVF);
        confirm = ((Button) mainLL.findViewById(R.id.newDeviceConfirmBT));
        confirm.setOnClickListener(confirmListener);
        ((Button) mainLL.findViewById(R.id.newDeviceAbortBT)).setOnClickListener(abortListener);
        ((Button) mainLL.findViewById(R.id.newDeviceStep0BarcodeBT)).setOnClickListener(barcodeScanListener);
        next = ((Button) mainLL.findViewById(R.id.newDeviceNextBT));
        next.setOnClickListener(nextListener);
        back = ((Button) mainLL.findViewById(R.id.newDeviceBackBT));
        back.setOnClickListener(backListener);
        darkShadeRB = (RadioButton) mainLL.findViewById(R.id.newDeviceStep5darkRB);
        darkShadeRB.setOnCheckedChangeListener(backGroundChanged);
        brightShadeRB = (RadioButton) mainLL.findViewById(R.id.newDeviceStep5brightRB);
        brightShadeRB.setOnCheckedChangeListener(backGroundChanged);
        nameTB = (EditText) mainLL.findViewById(R.id.newDeviceStep2NameTB);
        nameTB.setOnFocusChangeListener(editTextFocusChanged);
        ((EditText) mainLL.findViewById(R.id.newDeviceStep3ManufTB)).setOnFocusChangeListener(editTextFocusChanged);
        ((EditText) mainLL.findViewById(R.id.newDeviceStep3SerialTB)).setOnFocusChangeListener(editTextFocusChanged);
        ((EditText) mainLL.findViewById(R.id.newDeviceStep7DescriptionTB)).setOnFocusChangeListener(editTextFocusChanged);

        gaugeDD = (Spinner) mainLL.findViewById(R.id.newDeviceStep0GaugeDD);
        List<String> gaugeStrings = ((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getGauges().GetGaugeNames();
        gaugeStrings.add(0, "");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, gaugeStrings);
        //spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
			gauge = ((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getGauges().getGaugeList().get(position-1);

			
			//TODO
			//new gauge
			if(gauge.getGaugeDevice() == null)
			{
		        initializeDevice();
		        initializeNumPickers(mainLL);
			}
			//divice exchange
			else
			{
		        initializeDevice();
		        initializeNumPickers(mainLL);
			}
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
						isNotActive = false;
						brightShadeRB.setChecked(false);
						isNotActive = true;
					}
					else if (brightShadeRB == buttonView)
					{
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
			if(device.getDigitCount() > newVal+1)
				device.setDecimalPlaces(newVal+1);
			else
			{
				//TODO
				Statics.ShowToast(Statics.getDefaultResources().getString(R.string.new_device_decimal_warning));
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
        			if(et.getText().length() == 0)
        				continue;
        			if(et.getId() == R.id.newDeviceStep2NameTB)
        			{
        				device.setDeviceName(et.getText().toString());
        				registrationSteps.put(flipper.getDisplayedChild(), true);
        			}
        			else if(et.getId() == R.id.newDeviceStep3ManufTB)
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
        	if(flipper.getDisplayedChild() == 0)
        	{
        		if(gauge == null)
        		{
        			Statics.ShowToast(Statics.getDefaultResources().getString(R.string.new_device_step0_barcode_select_gauge));
        			return;
        		}
        		if(lastRead == null || Statics.getDateDiff(lastRead.getUtcTo(), new Date(), TimeUnit.MINUTES) < 16)
        		{
        			Statics.ShowToast(Statics.getDefaultResources().getString(R.string.new_device_step0_barcode_take_last_read));
        			((ccc.android.meterreader.MainActivity) getActivity()).openGaugeDisplayDialog(gauge, lastRead);
        			return;
        		}
        		registrationSteps.put(flipper.getDisplayedChild(), true);
        	}
        	if(!registrationSteps.get(flipper.getDisplayedChild())) //not!
        	{
        		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.new_device_next_step_warning));
        		return;
        	}
        	
        	if(flipper.getDisplayedChild() == 1)
        	{
        		flipper.setDisplayedChild(flipper.getDisplayedChild()+1);
        		nameTB.requestFocus();
        	}
        	else if(flipper.getDisplayedChild() < flipper.getChildCount()-2)
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
        }
    };
    
    private OnClickListener confirmListener = new OnClickListener() {
        public void onClick(View v) 
        {
        	int regStep = getUnfinishedRegistrationStep();
        	if(regStep == flipper.getDisplayedChild())
        	{
        		((ccc.android.meterreader.MainActivity) getActivity()).getManager().AddNewDevice(device);
        		resetRegistration();
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

    private OnClickListener barcodeScanListener = new OnClickListener() {
        public void onClick(View v) 
        {
        	((ccc.android.meterreader.MainActivity) getActivity()).openGaugeDisplayDialog(Statics.ANDROID_INTENT_ACTION_BFR);
        }
    };
    
	private void resetRegistration()
	{
		((ccc.android.meterreader.MainActivity) getActivity()).openOptionsPanelLayout();
		((ccc.android.meterreader.MainActivity) getActivity()).hideNewDeviceFragment();
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
		for(int i =1; i <= registrationSteps.size(); i++)
		{
			if(!registrationSteps.get(i))
				return i-1;
		}
		return registrationSteps.size()-1;
	}

	public Gauge getGauge()
	{
		return gauge;
	}

	public void setGauge(Gauge gauge)
	{
		this.gauge = gauge;
		if(gauge != null)
		{
			for(int i =1; i < gaugeDD.getCount(); i++)
				if(gaugeDD.getItemAtPosition(i) instanceof String && gaugeDD.getItemAtPosition(i).equals(gauge.getName()))
					gaugeDD.setSelection(i);
		}
		else
			Statics.ShowToast(Statics.getDefaultResources().getString(R.string.new_device_step0_gauge_not_found));
	}

	public Reading getLastRead()
	{
		return lastRead;
	}

	public void setLastRead(Reading lastRead)
	{
		this.lastRead = lastRead;
	}

	public String getBarcode()
	{
		return barcode;
	}

	public void setBarcode(String barcode, int gaugeId)
	{
		this.barcode = barcode;
		setGauge(((ccc.android.meterreader.MainActivity) getActivity()).getManager().getData().getGauges().getById(gaugeId));
	}
}