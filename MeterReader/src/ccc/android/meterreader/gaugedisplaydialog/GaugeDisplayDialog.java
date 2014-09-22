package ccc.android.meterreader.gaugedisplaydialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewFlipper;

import ccc.android.meterdata.*;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.R;
import ccc.android.meterreader.helpfuls.FinishReceiver;
import ccc.android.meterreader.internaldata.InternalDialogDisplayData;
import ccc.android.meterreader.qrreading.BarcodeFragment;
import ccc.android.meterreader.qrreading.DigitReaderFragment;
import ccc.android.meterreader.statics.Statics;

public class GaugeDisplayDialog extends Activity implements android.view.View.OnClickListener 
{
	private MeterReaderKeyboard keyboard;
	private List<CccNumberPicker> pickers = new ArrayList<CccNumberPicker>();
	private ViewFlipper displayViewFlipper;
	private VerticalButton showDisplayBT;
	private VerticalButton showScannerBT;
	private VerticalButton showDigitReaderBT;
	private LinearLayout numLL;
	private RelativeLayout gaugeDisplayLL;
	private LinearLayout infoDisplayLL;
	private char[] value;
	private int firstNonDecimalDigitPos = -1;
	private CccNumberPicker lastSelected = null;
	private boolean isDecimalPlace = false;
	private boolean isFirstChange = true;
	private boolean valueChangeListenerOn = true;
	private static boolean isShowing = false;

	private int currentDeviceID;
	private int pickerScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	private int showViewFirst = Statics.GAUGE_DISPLAY_VIEW;
	private BarcodeFragment barcodePreviewFragment;
	private DigitReaderFragment digitReaderFragment;
	private InternalDialogDisplayData gaugeData;
	private IntentFilter openFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_GDR);
	private IntentFilter finishFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_FIN);
	private FinishReceiver finishRec = new FinishReceiver();
	private Bitmap debugImg = null;
	private EditText hiddenEditText;
		
	///DEBUG-STUFF
	public void setDebugPreviewImage(Bitmap bmp)
	{

		int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
		//Matrix matrix = new Matrix();
		//matrix.postRotate(90);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),true);
		//Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
		debugImg = scaledBitmap;
       	GaugeDisplayDialog.this.runOnUiThread(new Runnable() {              
            @Override
            public void run() {
            	ImageView imageView = (ImageView) findViewById(R.id.debugPreviewImg);
            	imageView.setImageBitmap(GaugeDisplayDialog.this.debugImg);
            }
        });
	}
	///DEBUG-STUFF-END

	public GaugeDisplayDialog()
	{}
		
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	if(isShowing)
    	{
    	    moveTaskToBack (true);
    	}
	    super.onCreate(savedInstanceState);
	    setTheme(android.R.style.Theme_Holo_Light);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.gaugedisplayview);
	    this.getWindow().setBackgroundDrawableResource(R.drawable.gaugedisplaybackground);
	    
	    keyboard = new MeterReaderKeyboard(this, R.xml.meterkeyboard, R.id.meterKeyboardView);
	   Intent i = getIntent();
	   i.setAction(Statics.ANDROID_INTENT_ACTION_GDR);
	    onNewIntent(i);
	    initializeViews();
	    
	    if(gaugeData != null)
	    {
	    	showViewFirst = Statics.GAUGE_INFO_VIEW;
	    	setValues(gaugeData);
	    }
	    else
	    	showViewFirst = Statics.QR_PREVIEW_VIEW;
	    this.getResources().updateConfiguration(Statics.getConfiguration(), this.getResources().getDisplayMetrics());
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

	private void initializeViews() {
		((View) this.findViewById(R.id.suspendPreviewBT)).setOnClickListener(suspendePreviewListener);
		((View) this.findViewById(R.id.suspendReaderBT)).setOnClickListener(suspendeReaderListener);
	    showScannerBT = ((VerticalButton) this.findViewById(R.id.scanBarcodeBT));
	    showDisplayBT = (VerticalButton) this.findViewById(R.id.showDisplayBT);
	    showDigitReaderBT = (VerticalButton) this.findViewById(R.id.scanMeterBT);
	    showDisplayBT.setOnClickListener(showDisplayListener);
	    showScannerBT.setOnClickListener(showPreviewListener);
	    showDigitReaderBT.setOnClickListener(showDigitReaderListener);
	    displayViewFlipper = ((ViewFlipper) this.findViewById(R.id.displayViewFlipper));
		this.findViewById(R.id.saveReadingBT).setOnClickListener(saveReadingClickListener);		
		this.findViewById(R.id.closeDiaBT).setOnClickListener(closeDisplayView);	
    	this.registerReceiver(finishRec, finishFilter);
		numLL = (LinearLayout)this.findViewById(R.id.numPickerLL);	    
		gaugeDisplayLL = (RelativeLayout)this.findViewById(R.id.gaugeDisplayLL);	
		infoDisplayLL = (LinearLayout)this.findViewById(R.id.infoDisplayLL);	

	    if(Statics.debugMode)
			((View) this.findViewById(R.id.debugPreviewLL)).setVisibility(View.VISIBLE);
	}

	private void setValues(InternalDialogDisplayData gaugeData) 
	{
		if(gaugeData == null || gaugeData.getDigitCount() == null || gaugeData.getDigitCount() < 1)
		{
			Statics.ShowToast(this, GaugeDisplayDialog.this.getResources().getString(ccc.android.meterreader.R.string.gauge_display_no_data_warning));
			this.moveTaskToBack (true);
			return;
		}
		//InfoDisplay
	    Statics.SetText(((TextView) this.findViewById(R.id.infoLastReadLA)), gaugeData.getRead());
	    Statics.SetText(((TextView) this.findViewById(R.id.infoLastReadUnitLA)), gaugeData.getUnit());
	    Statics.SetText(((TextView) this.findViewById(R.id.infoLastReadDateLA)), gaugeData.getUtcTo());
	    Statics.SetText(((TextView) this.findViewById(R.id.infoLocationLA)), gaugeData.getLocation());
	    Statics.SetText(((TextView) this.findViewById(R.id.infoDisplayTitleLA)), gaugeData.getGaugeName());
	    Statics.SetText(((TextView) this.findViewById(R.id.infoDisplayDescriptionLA)), gaugeData.getDescription());
	    
		//DaugeDisplay
		firstNonDecimalDigitPos = gaugeData.getDigitCount() - gaugeData.getDecimalPlaces() - 1;
		this.value = new char[gaugeData.getDigitCount()];

	    Statics.SetText(((TextView) this.findViewById(R.id.lastReadLA)), gaugeData.getRead());
	    Statics.SetText(((TextView) this.findViewById(R.id.lastReadUnitLA)), gaugeData.getUnit());
	    Statics.SetText(((TextView) this.findViewById(R.id.lastReadDateLA)), gaugeData.getUtcTo());
	    Statics.SetText(((TextView) this.findViewById(R.id.gaugeDisplayLocationLA)), gaugeData.getLocation());
	    Statics.SetText(((TextView) this.findViewById(R.id.gaugeDisplayTitleLA)), gaugeData.getGaugeName());
	    Statics.SetText(((TextView) this.findViewById(R.id.gaugeDisplayDescriptionLA)), gaugeData.getDescription());
	    		
		if (gaugeData.getDigitCount() != null && gaugeData.getDigitCount() > 0) 
		{
			insertNumberPickers();
		}
		
		TextView displayUnitLA = (TextView) numLL.getChildAt(numLL.getChildCount()-1);
		displayUnitLA.setText(String.valueOf((Object)gaugeData.getUnit()));
		lastSelected = pickers.get(firstNonDecimalDigitPos);
		currentDeviceID = gaugeData.getGaugeDeviceId();

	}
	
	@Override
	public void onUserInteraction(){ 
	    Statics.setLastInteraction(new Date());
	}
    
    @Override
    protected void onStart() 
    {
    	super.onStart();
    }
    
    @Override
    protected void onStop() 
    {
    	super.onStop();
    	this.unregisterReceiver(this.GaugeIntentReceiver);
    }
    
    @Override
    protected void onDestroy() 
    {
    	this.unregisterReceiver(finishRec);
    	super.onDestroy();
    }

    @Override
    protected void onResume() 
    {
    	super.onResume();
    	this.registerReceiver(GaugeIntentReceiver, openFilter);
		isShowing = true;
	    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(this.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    lp.height = WindowManager.LayoutParams.FILL_PARENT;
	    lp.gravity = Gravity.TOP;
	    lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
	    this.getWindow().setAttributes(lp);
    	setSelectTimer(500);
    	//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() 
    {
    	isShowing = false;
    	hideCameraPreviewFragment();
    	this.hideDigitReaderFragment();
    	super.onPause();
    }
    
    @Override
    protected void onNewIntent(Intent intent) 
    {
	    gaugeData = (InternalDialogDisplayData) intent.getParcelableExtra("data");
	    if(gaugeData != null)
	    {
	    	if(intent.getAction() == Statics.ANDROID_INTENT_ACTION_GDR)
	    	{
	    		this.findViewById(R.id.closeInfoBT).setOnClickListener(closeInfoToMain);	
	    	}
	    	else if(intent.getAction() == Statics.ANDROID_INTENT_ACTION_GID)
	    	{
	    		this.findViewById(R.id.closeInfoBT).setOnClickListener(closeInfoToQr);	
	    	}
	    	isDecimalPlace = false;
	    	isFirstChange = true;
	    	valueChangeListenerOn = true;
		    initializeViews();
	    	setValues(gaugeData);
	    	showViewFirst = Statics.GAUGE_INFO_VIEW;
	    }
	    else
	    	showViewFirst = Statics.QR_PREVIEW_VIEW;
    }
    
	public void openNumberPickerEditText() 
	{
		hiddenEditText.requestFocusFromTouch();
	}
	
	public void SetValue(char[] inp)
	{
		String input = String.valueOf(inp);
				
		int nonDec = gaugeData.getDigitCount() - gaugeData.getDecimalPlaces();
		int dec = gaugeData.getDecimalPlaces();
		String nonDecVal = null;
		String decVal = null;
		
		if(input.contains("."))
		{
			decVal = input.substring(input.indexOf('.')+1);
			nonDecVal = input.substring(0, input.indexOf('.'));
		}
		else
		{
			if(input.length() > nonDec)
				decVal = input.substring(nonDec);
			else
				decVal = "";
			nonDecVal = input.substring(0, Math.min(nonDec, input.length()));
		}
		char[] ndc = new char[nonDec];
		char[] dc = new char[dec];
		for(int i =nonDec-1; i>=0; i--)
		{
			if(nonDecVal.length() - nonDec + i >=0)
			{
				ndc[i] = nonDecVal.charAt(nonDecVal.length() - nonDec + i);
			}
			else
				ndc[i] = '0';
		}
		for(int i =0; i< dec; i++)
		{
			if(i < decVal.length())
				dc[i] = decVal.charAt(i);
			else
				dc[i] = '0';
		}
		StringBuilder sb = new StringBuilder();
		sb.append(ndc);
		sb.append(dc);
		value = sb.toString().toCharArray();
		showValue();
	}

	public void showValue() {
		valueChangeListenerOn = false;
			for (int i = 0; i < value.length; i++)
			{
				if(value[i] == '-')
					setValueForNumPicker(pickers.get(i), 10);
				else
					setValueForNumPicker(pickers.get(i), Integer.parseInt(String.valueOf(value[i])));
			}

		if (simplePlausibilityCheck())
			numLL.setBackgroundResource(R.drawable.rectbackgreen);
		else
			numLL.setBackgroundResource(R.drawable.rectbackred);
		valueChangeListenerOn = true;
	}
	
	public void setSelectTimer(int delay)
	{
    	//workaround: selectAll will not work directly after an input (for some unknown reason)
		Timer doT = new Timer();
		doT.schedule(new MyTimerTask(), delay);
	}
	
    private class MyTimerTask extends TimerTask{

        @Override
        public void run() {        
        	GaugeDisplayDialog.this.runOnUiThread(new Runnable() {              
                @Override
                public void run() {


                	if(showViewFirst == Statics.GAUGE_DISPLAY_VIEW)
                	{
                		if(isFirstChange)
                    		setDisplayView();
                		showValue();
                    	openNumberPickerEditText();
                    	
                	}
                	else if(showViewFirst == Statics.GAUGE_INFO_VIEW)
                	{
                		setInfoView();
                	}
                	else if(showViewFirst == Statics.QR_PREVIEW_VIEW)
                	{
                		setQrCodeView();
                	}
                }
            });
        }       
    }
    
	private android.view.View.OnClickListener showPreviewListener = new android.view.View.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			setQrCodeView();
		}

	};
	
	private android.view.View.OnClickListener showDisplayListener = new android.view.View.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			setDisplayView();
		}

	};
	
	private android.view.View.OnClickListener showDigitReaderListener = new android.view.View.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			setDigitReaderView();
		}

	};
	
	private android.view.View.OnClickListener suspendePreviewListener = new android.view.View.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			GaugeDisplayDialog.this.hideCameraPreviewFragment();
			if(showViewFirst == Statics.GAUGE_INFO_VIEW)
				setInfoView();
			else if(showViewFirst == Statics.QR_PREVIEW_VIEW)
				backToMainActivity();
		}

	};
	
	private android.view.View.OnClickListener suspendeReaderListener = new android.view.View.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			GaugeDisplayDialog.this.hideDigitReaderFragment();
			setInfoView();
		}

	};
	
    BroadcastReceiver GaugeIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	gaugeData = (InternalDialogDisplayData) intent.getParcelableExtra("data");
        	setValues(gaugeData);
        	isDecimalPlace = false;
        	isFirstChange = true;
        	valueChangeListenerOn = true;
        }
    };
	
    private void setInfoView()
    {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.findViewById(R.id.gaugeDisplayInternalLL).getLayoutParams();
		params.height = LayoutParams.FILL_PARENT;
		this.findViewById(R.id.gaugeDisplayInternalLL).setLayoutParams(params);
    	displayViewFlipper.setDisplayedChild(Statics.GAUGE_INFO_VIEW);
		keyboard.getmKeyboardView().setVisibility(View.GONE);	
		Statics.ChangeLayoutElementsVisibility(infoDisplayLL, View.INVISIBLE, View.VISIBLE);
		showDisplayBT.setVisibility(View.VISIBLE);
		showScannerBT.setVisibility(View.GONE);
		showDigitReaderBT.setVisibility(View.VISIBLE);
    }
    
	private void setDisplayView()
	{
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.findViewById(R.id.gaugeDisplayInternalLL).getLayoutParams();
		params.height = gaugeDisplayLL.getHeight() - this.keyboard.getmKeyboardView().getHeight();
		this.findViewById(R.id.gaugeDisplayInternalLL).setLayoutParams(params);
		displayViewFlipper.setDisplayedChild(Statics.GAUGE_DISPLAY_VIEW);		
		hiddenEditText = ((EditText) this.findViewById(R.id.hiddenValueTB));
		hiddenEditText.addTextChangedListener(new NumPickerTextWatcher(this, gaugeData.getDigitCount(), gaugeData.getDecimalPlaces()));
		keyboard.registerEditText(hiddenEditText);
		Statics.ChangeLayoutElementsVisibility(gaugeDisplayLL, View.INVISIBLE, View.VISIBLE);
		showDisplayBT.setVisibility(View.GONE);
		showScannerBT.setVisibility(View.VISIBLE);
		showDigitReaderBT.setVisibility(View.VISIBLE);
		keyboard.getmKeyboardView().setVisibility(View.VISIBLE);
		openNumberPickerEditText();
		prepareValueArray();
		NumPickerTextWatcher.SetActivateTimer(300);
	}
	
	private void setQrCodeView()
	{
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.findViewById(R.id.gaugeDisplayInternalLL).getLayoutParams();
		params.height = LayoutParams.FILL_PARENT;
		this.findViewById(R.id.gaugeDisplayInternalLL).setLayoutParams(params);
		showDisplayBT.setVisibility(View.GONE);
		showScannerBT.setVisibility(View.GONE);
		showDigitReaderBT.setVisibility(View.GONE);
		keyboard.getmKeyboardView().setVisibility(View.GONE);
		displayViewFlipper.setDisplayedChild(Statics.QR_PREVIEW_VIEW);
		getCameraPreviewFragment();
	}
	
	private void setDigitReaderView()
	{
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.findViewById(R.id.gaugeDisplayInternalLL).getLayoutParams();
		params.height = LayoutParams.FILL_PARENT;
		this.findViewById(R.id.gaugeDisplayInternalLL).setLayoutParams(params);
		showDisplayBT.setVisibility(View.VISIBLE);
		showScannerBT.setVisibility(View.GONE);
		showDigitReaderBT.setVisibility(View.GONE);
		keyboard.getmKeyboardView().setVisibility(View.GONE);
		displayViewFlipper.setDisplayedChild(Statics.DIGIT_READER_VIEW);
		getDigitReaderFragment();
	}
	
	private void getDigitReaderFragment() {
		if (displayViewFlipper.getDisplayedChild() == Statics.DIGIT_READER_VIEW)
		{
			FragmentManager fragmentManager = this.getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			digitReaderFragment = (DigitReaderFragment) fragmentManager.findFragmentByTag("digitReader");
			if(digitReaderFragment != null)
			{
				digitReaderFragment.InitializeCamera();
				fragmentTransaction.show(digitReaderFragment);
			}
			else
			{
				digitReaderFragment = new ccc.android.meterreader.qrreading.DigitReaderFragment();
				fragmentTransaction.add(R.id.digitPreviewLL, digitReaderFragment, "digitReader");
			}
			fragmentTransaction.commit();
		}
	}
		
	private void getCameraPreviewFragment() {
		if (displayViewFlipper.getDisplayedChild() == Statics.QR_PREVIEW_VIEW)
		{
			FragmentManager fragmentManager = this.getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			barcodePreviewFragment = (BarcodeFragment) fragmentManager.findFragmentByTag("cameraPreview");
			if(barcodePreviewFragment != null)
			{
				barcodePreviewFragment.InitializeCamera();
				fragmentTransaction.show(barcodePreviewFragment);
			}
			else
			{
				barcodePreviewFragment = new ccc.android.meterreader.qrreading.BarcodeFragment();
				fragmentTransaction.add(R.id.qrCodePreviewLL, barcodePreviewFragment, "cameraPreview");
			}
			fragmentTransaction.commit();
		}
	}
	
	private void hideCameraPreviewFragment() 
	{
		if(displayViewFlipper.getDisplayedChild() == Statics.QR_PREVIEW_VIEW)
		{
			FragmentManager fragmentManager = getFragmentManager();
	        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	        barcodePreviewFragment = (BarcodeFragment) fragmentManager.findFragmentByTag("cameraPreview");
	        if (barcodePreviewFragment != null) {
				barcodePreviewFragment.ReleaseCamera();
				fragmentTransaction.remove(barcodePreviewFragment);
				fragmentTransaction.commit();
			}
		}
	}
	
	private void hideDigitReaderFragment() 
	{
		if(displayViewFlipper.getDisplayedChild() == Statics.DIGIT_READER_VIEW)
		{
			FragmentManager fragmentManager = getFragmentManager();
	        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	        digitReaderFragment = (DigitReaderFragment) fragmentManager.findFragmentByTag("digitReader");
	        if (digitReaderFragment != null) {
	        	digitReaderFragment.ReleaseCamera();
				fragmentTransaction.remove(digitReaderFragment);
				fragmentTransaction.commit();
			}
		}
	}
    
    OnEditorActionListener editorActionListener = new OnEditorActionListener(){

		@Override
		public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			return false;
		}
    	
    };

	private void prepareValueArray() {
		String lastReadVal = "0.0";
		if (gaugeData != null && gaugeData.getRead() != null)
			lastReadVal = String.valueOf((Object)gaugeData.getRead());
//		int pointPos = lastReadVal.length()-1;
//		if(lastReadVal.indexOf('.') >= 0)
//			pointPos = lastReadVal.indexOf('.');
//		lastReadVal = lastReadVal.replace(".", "");
//		int start = gaugeData.getDigitCount() - lastReadVal.length() 
//				- (gaugeData.getDecimalPlaces() - (lastReadVal.length() - pointPos));
//		if(value != null)
//		{
//			lastReadVal.getChars(0, lastReadVal.length(), value, start);
//			
//			for(int i=0;i<value.length;i++)
//				if((Character)value[i]=='\u0000')
//					value[i]='0';
//		}
		SetValue(lastReadVal.toCharArray());
	}
	
	private void resetValue()
	{
		prepareValueArray();
		this.isFirstChange = true;
	}

	private void insertNumberPickers() 
	{
		pickers.clear();
		for(int i=numLL.getChildCount()-1;i>=0;i--)
		{
			if(numLL.getChildAt(i) instanceof CccNumberPicker)
				numLL.removeViewAt(i);
		}
		for (int i = 0; i < gaugeData.getDigitCount(); i++) {
			CccNumberPicker npN = new CccNumberPicker(this);
			//set background
			if(gaugeData.getDigitCount() - gaugeData.getDecimalPlaces() <= i)
			{
				LayoutParams lp = configureNumberPicker(npN, i);
				numLL.addView(npN, i+1, lp);
			}
			else
			{
				LayoutParams lp = configureNumberPicker(npN, i);
				numLL.addView(npN, i, lp);
			}
			pickers.add(npN);
		}
    	NumPickerTextWatcher.SetActivateTimer(500);
	}

	private LayoutParams configureNumberPicker(CccNumberPicker npN, int pos) 
	{
		npN.setMaxValue(9);
		npN.setMinValue(0);
		npN.setDisplayedValues(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-"});
		npN.setOnScrollListener(scrollListener);
		setClickListenerForNumPickerButtons(npN);
		EditText edit = getInputFieldFromNumberPicker(npN);
		edit.setInputType(InputType.TYPE_NULL);
		edit.setImeOptions(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		if(pos >= gaugeData.getDigitCount() - gaugeData.getDecimalPlaces())
			edit.setTextColor(Color.RED);
		else
			edit.setTextColor(Color.BLACK);
		
		edit.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				hiddenEditText.requestFocusFromTouch();
				return true;
			}
		});
		edit.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if(hasFocus)
					hiddenEditText.requestFocusFromTouch();
			}
		});
		int npWidth = Statics.DISPLAY_WIDTH / 15;
		LayoutParams lp = new LayoutParams(npWidth,200);
		char val = value[pos];
		for(int i=0;i<10;i++)
			if(npN.getDisplayedValues()[i].charAt(0) == val)
				npN.setValue(i);

		npN.setOnValueChangedListener(valueChangedListener);
		//edit.addTextChangedListener(new NumPickerTextWatcher(edit, this));
		return lp;
	}
	
	private OnScrollListener scrollListener = new OnScrollListener()
	{
		@Override
		public void onScrollStateChange(NumberPicker np, int scrollState) {
			GaugeDisplayDialog.this.pickerScrollState = scrollState;
			if(scrollState != OnScrollListener.SCROLL_STATE_IDLE)
			{
				NumPickerTextWatcher.isActive().set(false);
			}
			else
			{
				NumPickerTextWatcher.SetActivateTimer(500);
			}
		}
	};
	
	private EditText getInputFieldFromNumberPicker(CccNumberPicker picker)
	{
		EditText edit = null;
		for(int i=0;i<picker.getChildCount();i++)
		{
			if(picker.getChildAt(i) instanceof EditText)
				edit = (EditText) picker.getChildAt(i);
		}
		return edit;
	}
	
	private void setClickListenerForNumPickerButtons(CccNumberPicker picker)
	{
		for(int i=0;i<picker.getChildCount();i++)
		{
			if(picker.getChildAt(i) instanceof ImageButton)
			{
				((ImageButton) picker.getChildAt(i)).setOnClickListener(numPickerButtonUpListener);
				((ImageButton) picker.getChildAt(i+2)).setOnClickListener(numPickerButtonDownListener);
				return;
			}
		}
	}
	
	private Character getValueFromPicker(CccNumberPicker p)
	{
    	for(int i =0;i< pickers.size();i++)
    	{
    		if(p.equals(pickers.get(i)))
    		{
    			return value[i];
    		}
    	}
    	return ' ';
	}
	
	private int getPositionOfPicker(NumberPicker p)
	{
    	for(int i =0;i< pickers.size();i++)
    	{
    		if(p.equals(pickers.get(i)))
    		{
    			return i;
    		}
    	}
    	return -1;
	}
    
    private CccNumberPicker getNextPicker(CccNumberPicker p)
    {
    	int digitCount = gaugeData.getDigitCount();
    	for(int i =0;i< digitCount;i++)
    	{
    		if(p.equals(pickers.get(i)))
    		{
    			if(this.isDecimalPlace)
    			{
    				if(i == digitCount-1)
    				{
    					this.isDecimalPlace = false;
    					this.isFirstChange = true;
    					return pickers.get(this.firstNonDecimalDigitPos);
    				}
    				else
    					return pickers.get((i+1));
    			}
    			else
    				return pickers.get((i));
    		}
    	}
    	return null;
    }
    
    private void setNewValue(CccNumberPicker p, char val, boolean simpleValueChange)
    {
    	char[] value = this.value;
    	if(!simpleValueChange && isFirstChange)
    	{
    		for(int i =0; i< value.length;i++)
    		{
    			value[i] = '0';
    		}
    		isFirstChange = false;
    	}
    	for(int i =0;i< pickers.size();i++)
    	{
    		if(p == pickers.get(i))
    		{
    			if(simpleValueChange || i > this.firstNonDecimalDigitPos)
    				value[i] = val;
    			else
    			{
					char temp = ' ';
    				for(int j =i;j>=0;j--)
    				{
    					temp = value[j];
    					value[j] = val;
    					val = temp;
    				}
    			}
    		}
    	}
    	
    	this.value = value;
    }
    
    private CccNumberPicker.OnValueChangeListener valueChangedListener = new OnValueChangeListener()
    {
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal) 
		{
			if (valueChangeListenerOn) {
				int pos = getPositionOfPicker(picker);
				value[pos] = String.valueOf(newVal).charAt(0);
				showValue();
			}
		}
    };
    
    public void SetPreviousValue(EditText edit)
    {
    	if(NumPickerTextWatcher.isActive().getAndSet(false))
    	{
	    	CccNumberPicker la = (CccNumberPicker) edit.getParent();
	    	Character zz = (getValueFromPicker(la));
	    	//la.setValue(Integer.parseInt(String.valueOf(zz)));
	    	getInputFieldFromNumberPicker(la).setText(new char[]{zz}, 0, 1);
			lastSelected = getNextPicker(la);
			//setSelectTimer(30);
			NumPickerTextWatcher.isActive().set(true);
    	}
    }
    
    public void SetEditValue(EditText edit, Character value)
    {
    	if(NumPickerTextWatcher.isActive().getAndSet(false))
    	{
	    	CccNumberPicker thisPicker = ((CccNumberPicker)((ViewGroup)edit.getParent()));
			setNewValue(thisPicker, value, false);
			lastSelected = getNextPicker(thisPicker);
			//setSelectTimer(30);
			NumPickerTextWatcher.isActive().set(true);
    	}
    }
    
    private View.OnClickListener closeDisplayView = new View.OnClickListener()
    {
		@Override
		public void onClick(View arg0) {
			setInfoView();
		}
    };
    
    private View.OnClickListener closeInfoToMain = new View.OnClickListener()
    {
		@Override
		public void onClick(View arg0) {
			backToMainActivity();
		}
    };
    
    private View.OnClickListener closeInfoToQr = new View.OnClickListener()
    {
		@Override
		public void onClick(View arg0) {
			setQrCodeView();
		}
    };
    
    private View.OnClickListener numPickerButtonDownListener = new View.OnClickListener()
    {
		@Override
		public void onClick(View arg0) {
			NumPickerTextWatcher.isActive().set(false);
			//NumPickerTextWatcher.SetActivateTimer(100);
			CccNumberPicker picker = ((CccNumberPicker) arg0.getParent());
			int newVal = (picker.getValue() +1 ) % 10;
			picker.setValue(newVal);
			setNewValue((CccNumberPicker)picker, String.valueOf(newVal).charAt(0), true);
			NumPickerTextWatcher.isActive().set(true);
		}
    };
    
    private View.OnClickListener numPickerButtonUpListener = new View.OnClickListener()
    {
		@Override
		public void onClick(View arg0) {
			NumPickerTextWatcher.isActive().set(false);
			//NumPickerTextWatcher.SetActivateTimer(100);
			CccNumberPicker picker = ((CccNumberPicker) arg0.getParent());
			int newVal = (picker.getValue() -1 ) % 10;
			picker.setValue(newVal);
			setNewValue((CccNumberPicker)picker, String.valueOf(newVal).charAt(0), true);
			NumPickerTextWatcher.isActive().set(true);
		}
    };

	private void backToMainActivity() 
	{
		Intent getMainActivityBack = new Intent(GaugeDisplayDialog.this, MainActivity.class);
		getMainActivityBack.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(getMainActivityBack);
		finish();
	}
	
    private View.OnClickListener saveReadingClickListener = new View.OnClickListener()
    {
		@Override
		public void onClick(View v) 
		{
			sendNewRead ();
		}
    };
    
	public void sendNewRead() 
	{
		if(gaugeData == null)
		{
			Statics.ShowToast(GaugeDisplayDialog.this , GaugeDisplayDialog.this.getResources().getString(ccc.android.meterreader.R.string.gauge_display_read_warning_less));
			this.dismissDialog(0);
		}
		if(simplePlausibilityCheck())
		{
		    InternalDialogDisplayData data = gaugeData;
		    data.setRead(this.extractNewRead(String.valueOf(value)));
		    Intent sendNewRead = new Intent(Statics.ANDROID_INTENT_ACTION_NEW).putExtra("data", data);
			this.sendBroadcast(sendNewRead);
			backToMainActivity();
		}
		else
			Statics.ShowToast(GaugeDisplayDialog.this , GaugeDisplayDialog.this.getResources().getString(ccc.android.meterreader.R.string.gauge_display_read_warning_less));
		
	}
    
    private boolean simplePlausibilityCheck()
    {
    	//TODO '-' check
    	String stringValue = String.valueOf(value).replace('-', '0');
    	int firstMinus = stringValue.indexOf("-");
    	if(firstMinus >= 0)
    		for(int i = value.length-1; i>=0;i--)
    			if(Character.isDigit(value[i]))
    			{
    				if(i > firstMinus)
    					return false;
    				break;
    			}
    	float read = 0f;
    	if(firstMinus < 0)
    		read = extractNewRead(stringValue);
    	else
    		read = extractNewRead(stringValue.substring(0, firstMinus));
    	if (gaugeData != null && gaugeData.getRead() != null) {
			if (gaugeData.getRead() <= read)
				return true;
			else
				return false;
		}
    		return true;
    }
    
    private float extractNewRead(String stringValue)
    {
    	stringValue = stringValue.substring(0, value.length-gaugeData.getDecimalPlaces()) 
    			+ "." + stringValue.substring(value.length-gaugeData.getDecimalPlaces());
    	for(int i =0; i<stringValue.length();i++)
    	{
    		if(stringValue.substring(i).indexOf('0') != 0)
    		{
    			stringValue = stringValue.substring(i);
    			break;
    		}
    	}
    	float ret = Float.valueOf(stringValue);
    	return ret;
    }
    
    private void setValueForNumPicker(CccNumberPicker p, int val)
    {
    	if(val == 10)
    		p.setMaxValue(10);
    	else
    		p.setMaxValue(9);
    	p.setValue(val);
    }
    

	public boolean isDecimalPlace() {
		return isDecimalPlace;
	}

	public void setDecimalPlace(boolean isDecimalPlace) {
		this.isDecimalPlace = isDecimalPlace;
	}
	
	public int getPickerScrollState() {
		return pickerScrollState;
	}
	
	public static boolean isShowing() {
		return isShowing;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

	public int getCurrentDeviceID() {
		return currentDeviceID;
	}
}
