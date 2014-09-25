package ccc.android.meterreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccc.android.meterdata.enums.GaugeMedium;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterdata.types.Session;
import ccc.android.meterreader.camerafragments.DigitReaderFragment;
import ccc.android.meterreader.datamanagement.DataContext;
import ccc.android.meterreader.datamanagement.DataContextManager;
import ccc.android.meterreader.datamanagement.IDataContextEventListener;
import ccc.android.meterreader.deviceregistration.DeviceRegistrationFragment;
import ccc.android.meterreader.exceptions.UserNotInitializedException;
import ccc.android.meterreader.gaugedisplaydialog.GaugeDisplayDialog;
import ccc.android.meterreader.internaldata.InternalDialogDisplayData;
import ccc.android.meterreader.statics.Statics;
import ccc.android.meterreader.statics.Statics.SyncState;
import ccc.android.meterreader.viewelements.ConnectionConfigDialog;
import ccc.android.meterreader.viewelements.GaugeListViewFragment;
import ccc.android.meterreader.viewelements.VerticalButton;
import android.os.Bundle;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.*;
import android.widget.*;
import android.os.CountDownTimer;

public class MainActivity extends Activity implements IDataContextEventListener
{

    private static final int SWIPE_MIN_DISTANCE = 200;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private boolean initial = true;
	private ViewFlipper VF;
    private LinearLayout ButtonLL;
    private LinearLayout OptionsLL;
    private VerticalButton OptionBT;
    private GaugeListViewFragment listViewFragment;
    private DataContextManager manager;
    private GestureDetector gestureDetector;
    private OnTouchListener gestureListener ;
    private List<AlertDialog> syncDias = new ArrayList<AlertDialog>();
    private DeviceRegistrationFragment newDeviceFragment;
    
	private IntentFilter barcodeIntentFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_BAR);
	private IntentFilter barcodeForRegFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_BFR);
	private IntentFilter newReadIntentFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_NEW);
    
	static
	  {
	      System.loadLibrary("tbb");
	      System.loadLibrary("opencv_core");
	      System.loadLibrary("jniopencv_core");
	  }
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);          
        setContentView(R.layout.activity_main);
        //DEBUG: language parameter only of interest while debugging!
        Statics.initializeStatics(this, Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro.ttf"), "de");
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        manager  = new DataContextManager(this);
        
        OptionsLL = (LinearLayout) findViewById(R.id.OptionLL);
        ButtonLL = (LinearLayout) findViewById(R.id.menuButtonsLL);
        OptionBT = (VerticalButton) findViewById(R.id.OptionBT);
        VF = (ViewFlipper) findViewById(R.id.MainFlipperVF);
        ((Button) findViewById(R.id.saveBT)).setOnClickListener(saveBT_listener);
        ((Button) findViewById(R.id.loadLastSessionBT)).setOnClickListener(loadFromLastSession);
        ((Button) findViewById(R.id.loadBaseDataBT)).setOnClickListener(this.loadFromMainSession);
        ((Button) findViewById(R.id.syncContextWithDbBT)).setOnClickListener(loadFromDbListener);
        findViewById(R.id.optionsBT).setOnClickListener(optionsButtonsListener);
        findViewById(R.id.newSessionBT).setOnClickListener(newSessionListener);
        findViewById(R.id.loadLastSessionBT).setOnClickListener(loadSessionListener);
        findViewById(R.id.showAllStationsBT).setOnClickListener(optionsButtonsListener);
        findViewById(R.id.connectionConfigBT).setOnClickListener(openConnectionConfigListener);
        findViewById(R.id.ScanGaugeBT).setOnClickListener(qrPreviewListener);
        findViewById(R.id.closeMainBT).setOnClickListener(finishListener);
        findViewById(R.id.newDeviceBT).setOnClickListener(newDeviceListener);
        OptionBT.setOnClickListener(optionBT_listener);
        OptionsLL.setVisibility(View.GONE);
        setOnTouchListeners(VF);
        setOnTouchListeners((ViewGroup)findViewById(R.id.menuButtonsLL));

        gestureDetector = new GestureDetector(this, onGuestureListener);
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        ButtonLL.setOnTouchListener(gestureListener);
        VF.setOnTouchListener(gestureListener);

    	this.manager.getData().setSyncStateUp();
    	checkSystemConfigurations();
    }
	
    @Override
    protected void onPause()
    {
    	Statics.stopPing();
    	super.onPause();
    	//TODO
 //   	Statics.pauseTimer();
    }
    
    @Override
    protected void onResume()
    {
    	Statics.startPing();
    	super.onResume();
    	this.registerReceiver(BarcodeReceiver, barcodeIntentFilter);
    	this.registerReceiver(BarcodeReceiver, barcodeForRegFilter);
    	this.registerReceiver(newReadReceiver, newReadIntentFilter);
    	if(manager.getUnDoStack().size() > 0)
    		((TextView) findViewById(R.id.mainTickerLA)).setText(manager.getUnDoStack().peek().GetActionText());
    	else
    		((TextView) findViewById(R.id.mainTickerLA)).setText("");
//    	if(! this.initial)
//    		Statics.resumeTimer();
    	setSessionButtonView ();
    }
    
    @Override
    protected void onStop()
    {
    	this.unregisterReceiver(this.BarcodeReceiver);
    	this.unregisterReceiver(this.newReadReceiver);
    	if(manager.getData().getSession() != null && manager.getData().getSession().hasNewDate())
    		manager.SaveContextToFile(Statics.LAST_SESSION, false);
    	else
    		manager.SaveEmptySession(Statics.LAST_SESSION);
   	   	super.onStop();
    }
    
    @Override
    protected void onDestroy()
    {
		Intent i = new Intent(Statics.ANDROID_INTENT_ACTION_FIN);
		this.sendBroadcast(i);
    	super.onDestroy();
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {	  
//	    if (initial) {
//		    MenuInflater inflater = getMenuInflater();
//		    inflater.inflate(R.menu.main, menu);
//			menu.add(0, 99, 0, "nicht verbunden").setIcon(getResources().getDrawable(R.drawable.wifi_icon_red)).setShowAsAction(2);
//			menu.add(0, 100, 1, "nicht synchroniziert").setIcon(getResources().getDrawable(R.drawable.synchro_icon_red)).setShowAsAction(2);
//			initial = false;
//		}
//	    return true;
//      
//    }
    
   @Override
    public boolean onPrepareOptionsMenu(Menu menu) { 
    	    MenuInflater inflater = getMenuInflater();
    	    inflater.inflate(R.menu.main, menu);
			menu.clear();
				if (Statics.isOnline() == true) {
					findViewById(R.id.newSessionBT).setBackgroundResource((R.drawable.buttonback));
					menu.add(0,  99, 0, "verbunden").setIcon(getResources().getDrawable(R.drawable.wifi_icon_green)).setShowAsAction(2);
				} else {
					findViewById(R.id.newSessionBT).setBackgroundResource((R.drawable.buttonbackinactive));
					menu.add(0,  99, 0, "nicht verbunden").setIcon(getResources().getDrawable(R.drawable.wifi_icon_red)).setShowAsAction(2);
				}
				if(manager.getData().getSyncState() == SyncState.Asynchron)
					menu.add(0, 100, 1, "nicht synchroniziert").setIcon(getResources().getDrawable(R.drawable.synchro_icon_red)).setShowAsAction(2);
				else if(manager.getData().getSyncState() == SyncState.Desynchron)
					menu.add(0, 100, 1, "Neue Werte noch nicht gespeichert").setIcon(getResources().getDrawable(R.drawable.synchro_icon_yellow)).setShowAsAction(2);
				else
					menu.add(0, 100, 1, "Daten sind synchroniziert").setIcon(getResources().getDrawable(R.drawable.synchro_icon_green)).setShowAsAction(2);
					
    	return true;
    }
    
    private void setDesctopIcon()
    {
        Intent shortcutIntent = new Intent(this, MainActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,Statics.getDefaultResources().getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.drawable.ic_launcher);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(intent);
    }

    
	public void openGaugeDisplayDialog() 
	{
	    Intent startGaugeDisplay = new Intent(MainActivity.this, GaugeDisplayDialog.class);
	    startGaugeDisplay.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    startGaugeDisplay.putExtra("requestCode", Statics.GAUGE_DISPLAY_REQUEST_CODE); 
	    startActivity(startGaugeDisplay);
	    
	}
    
	public void openGaugeDisplayDialog(Gauge st, Reading rd) 
	{
	    Intent startGaugeDisplay = new Intent(MainActivity.this, GaugeDisplayDialog.class);
	    InternalDialogDisplayData data = new InternalDialogDisplayData(st, rd);
	    startGaugeDisplay.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    startGaugeDisplay.putExtra("data", data);
	    startGaugeDisplay.putExtra("requestCode", Statics.GAUGE_DISPLAY_REQUEST_CODE); 
	    startActivity(startGaugeDisplay);
		Intent sendDataIntent = new Intent(Statics.ANDROID_INTENT_ACTION_GDR).putExtra("data", data);
		this.sendBroadcast(sendDataIntent);
	}
    
    private void setOnTouchListeners(ViewGroup init)
    {
		int childcount = ((ViewGroup)init).getChildCount();
        for (int i=0; i < childcount; i++)
        {
        	View v = ((ViewGroup)init).getChildAt(i);
        	if(v != null && (v.getTag() != null && !v.getTag().toString().equals("dontClose"))) //not!
        	{
	            v.setOnTouchListener(mainFlipper_listener);
	            if(v != null && v instanceof ViewGroup)
	            	setOnTouchListeners((ViewGroup)v);
        	}
        }
    }
    
    private OnClickListener qrPreviewListener = new OnClickListener() {
        public void onClick(View v) 
        {    
        	
        	//TODO rückgängig
        	if(manager.getData().isCompletelyLoaded())
        		openGaugeDisplayDialog();
        	else
        		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_not_done));
        }
    };
    
    private OnClickListener loadSessionListener = new OnClickListener() {
        public void onClick(View v) 
        {    

	        	manager.LoadContextFromFile(Statics.LAST_SESSION, false);
	        	setSessionButtonView ();
        	
        }
    };
    
    private OnClickListener newSessionListener = new OnClickListener() {
        public void onClick(View v) 
        {    
        	try {
				manager.getData().NewSession();
	        	setSessionButtonView ();
			} catch (UserNotInitializedException e) {
				Statics.ShowToast("no user!");
			}
        }
    };
    
	public void loadContextFromDb() {
		if(Statics.isOnline())
    	{
        	manager.LoadContextFromDb();
    	}
    	else
    	{
    		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_not_successful));
    	}
	}
    
    private void setSessionButtonView()
    {
        findViewById(R.id.newSessionBT).setVisibility(View.GONE);
        findViewById(R.id.loadSessionBT).setVisibility(View.GONE);
        findViewById(R.id.showAllStationsBT).setVisibility(View.VISIBLE);
        findViewById(R.id.optionsBT).setVisibility(View.VISIBLE);
        findViewById(R.id.ScanGaugeBT).setVisibility(View.VISIBLE);
    }

    private OnClickListener optionsButtonsListener = new OnClickListener() {
        public void onClick(View v) 
        {    
        	if(v.equals(MainActivity.this.findViewById(R.id.showAllStationsBT)))
        	{
    			if (manager.getData().isCompletelyLoaded()) 
    			{
					FragmentManager fragmentManager = MainActivity.this.getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					listViewFragment = new GaugeListViewFragment();
					listViewFragment.setContext(MainActivity.this);
					fragmentTransaction.add(R.id.listViewContainerLL, listViewFragment, "listView");
					fragmentTransaction.commit();
				}
    			else
    			{
    				Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_not_done));
    				return;
    			}
        	}
    		VF.setDisplayedChild(getChildIndexById(v.getId()));
    		closeOptionsPanelLayout();
        }
    };
    
    BroadcastReceiver BarcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //extract our message from intent
            String barcode = intent.getStringExtra("barcode");
            
            if(barcode != null && intent.getAction() == Statics.ANDROID_INTENT_ACTION_BFR)
            {
            	
            }
        	if(barcode != null && intent.getAction() == Statics.ANDROID_INTENT_ACTION_BAR)
        	{
        		Pattern gaugeIdPattern = Pattern.compile(Statics.BARCODE_GAUGEID);
        		Matcher m = gaugeIdPattern.matcher(barcode);
        		m.find();
        		int gaugeId = Integer.parseInt(m.group());
        		Gauge ga = manager.getData().getGauges().getById(gaugeId);
        		Reading lastRead = manager.getData().getReadings().getLastReadingById(gaugeId);
        		if(ga != null)
        			openGaugeDisplayDialog(ga, lastRead);
        	}
        	else
        	{
        		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.qr_not_recognized));
        	}
        }
    };
    
    private int getChildIndexById(int id)
    {
    	for(int i = 0;i< ButtonLL.getChildCount();i++)
    	{
    		View child = ButtonLL.getChildAt(i);
    		if(child.getId() == id)
    		{
    			return i;
    		}
    	}
    	return 0;
    }
    
    private OnClickListener optionBT_listener = new OnClickListener() {
        public void onClick(View v) {
        	openOptionsPanelLayout();
        }
    };
    
    private OnClickListener saveBT_listener = new OnClickListener() {
        public void onClick(View v) {
        	manager.SaveContextToFile(Statics.DEBUG_SESSION, true);
        }
    };
    
    private OnClickListener loadFromDbListener = new OnClickListener() {
        public void onClick(View v) {
        	manager.UploadToDataBase();
        	//manager.LoadContextFromDb();
        }
    };
    
    private OnClickListener loadFromMainSession = new OnClickListener() {
        public void onClick(View v) {
        	manager.LoadContextFromFile(null, true);
        	setSessionButtonView();
        }
    };
    
    private OnClickListener loadFromLastSession = new OnClickListener() {
        public void onClick(View v) {
        	manager.LoadContextFromFile(Statics.DEBUG_SESSION, true);
        }
    };
    
    private OnClickListener openConnectionConfigListener = new OnClickListener() {
        public void onClick(View v) {
        	ConnectionConfigDialog dialog = new ConnectionConfigDialog(MainActivity.this);
    		dialog.show();
        }
    };
    
    private OnClickListener finishListener = new OnClickListener() {
        public void onClick(View v) {
        	MainActivity.this.finish();
        	return;
        }
    };
    
    private OnClickListener newDeviceListener = new OnClickListener() {
        public void onClick(View v) {
        	getNewDeviceFragment();
        	VF.setDisplayedChild(getChildIndexById(v.getId()));
    		closeOptionsPanelLayout();
        }
    };
    
    private OnTouchListener mainFlipper_listener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
        	closeOptionsPanelLayout();
        	return false;
        }
    };
    
	public void openOptionsPanelLayout() 
	{
		if(OptionBT.getVisibility() == View.VISIBLE)
    	{
        	ButtonLL.setVisibility(View.VISIBLE);
    		OptionsLL.setVisibility(View.GONE);
    	}
	}
	
	private void closeOptionsPanelLayout() 
	{
		if(OptionsLL.getVisibility() == View.GONE)
		{
	    	ButtonLL.setVisibility(View.GONE);
			OptionsLL.setVisibility(View.VISIBLE);
		}
	}
    
    public DataContextManager getManager() {
		return manager;
	}
    
    SimpleOnGestureListener onGuestureListener = new SimpleOnGestureListener(){
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
            	//swiping
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	closeOptionsPanelLayout();
                }  
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	openOptionsPanelLayout();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
              return true;
        }
    };

	@Override
	public void OnDataContextUpdated(DataContext newContext) {
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonback);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonback);
        if(listViewFragment != null)
        	listViewFragment.RedoMappings();
	}

	@Override
	public void OnNewSessionInitialized() {
		
	}
	
	@Override
	public void onUserInteraction(){ 
	    Statics.setLastInteraction(new Date());
	}

	@Override
	public void OnSessionIsSynchronizing(Session session) {
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonbackinactive);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonbackinactive);
        showSyncDialog();
	}


	@Override
	public void OnSessionSynchronized(Session session) {
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonback);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonback);
        if(listViewFragment != null)
        	listViewFragment.RedoMappings();

        if(initial)
        {
	    	initial = false;
	        manager.LoadContext();
        }
        showSyncStatusToast();
	}
	
	@Override
	public void OnSessionLoaded(Session session) {
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonback);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonback);
        if(listViewFragment != null)
        	listViewFragment.RedoMappings();
        if(session == null)
			try {
				manager.getData().NewSession();
			} catch (UserNotInitializedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        else
        {
            manager.getData().setSyncStateDown();
        }
        showSyncStatusToast();
	}

	@Override
	public void OnSessionUpLoaded(Session session) 
	{
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonback);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonback);
        if(listViewFragment != null)
        	listViewFragment.RedoMappings();

        showSyncStatusToast();
	}
	
	@Override
	public void OnDataContextInvalidated() {
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonbackinactive);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonbackinactive);
	}
	
	@Override
	public void OnFailedSessionSynchronization(String msg) {
		closeSyncDialog();
		closeSyncDialog();
		closeSyncDialog();
		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_not_successful) + "\n" + msg);
	}

	@Override
	public void OnSynchronizationStateChanged() 
	{
		invalidateOptionsMenu();
	}
	
	private void showSyncStatusToast()
	{
			closeSyncDialog();
			Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_done));
	}
		
	public void showSyncDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(Statics.getDefaultResources().getString(R.string.main_sync_inprog))
		       .setCancelable(true)
		       .setNegativeButton(Statics.getDefaultResources().getString(R.string.main_sync_abort), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
					MainActivity.this.closeSyncDialog();
					MainActivity.this.closeSyncDialog();
					MainActivity.this.closeSyncDialog();
					
					Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_aborted));
				}});
		AlertDialog d = builder.create();
		d.show();
		syncDias.add(d);
	}
	
	private void closeSyncDialog()
	{
		if(syncDias.size() > 0)
		{
			syncDias.get(0).dismiss();
			syncDias.remove(0);
		}
	}
	
	private void getNewDeviceFragment() {
			FragmentManager fragmentManager = this.getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			this.newDeviceFragment = (DeviceRegistrationFragment) fragmentManager.findFragmentByTag("newDeviceFragment");
			
			if(newDeviceFragment != null)
			{
				fragmentTransaction.show(newDeviceFragment);
			}
			else
			{
				newDeviceFragment = new DeviceRegistrationFragment();
				newDeviceFragment.setContainerView((LinearLayout) findViewById(R.id.deviceRegistrationFragmentLL));
				fragmentTransaction.add(R.id.deviceRegistrationFragmentLL, newDeviceFragment, "newDeviceFragment");
			}
			fragmentTransaction.commit();
	}
	
	public void hideNewDeviceFragment() 
	{
			FragmentManager fragmentManager = getFragmentManager();
	        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	        newDeviceFragment = (DeviceRegistrationFragment) fragmentManager.findFragmentByTag("newDeviceFragment");
	        if (newDeviceFragment != null) {
				fragmentTransaction.remove(newDeviceFragment);
				fragmentTransaction.commit();
				newDeviceFragment = null;
			}
	}
	
    private BroadcastReceiver newReadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	InternalDialogDisplayData newReadData = (InternalDialogDisplayData) intent.getParcelableExtra("data");
        	Reading read = new Reading();
        	read.setGaugeId(newReadData.getGaugeId());
        	read.setRead(newReadData.getRead());
        	read.setSessionId(read.getSessionId());
        	read.setStationId(newReadData.getStationId());
        	read.setUtcTo(Statics.getUtcTime());
        	read.setUtcFrom(newReadData.getUtcTo());
        	read.setValType(1);
        	manager.InsertNewRead(read);
        }
    };

    private void checkSystemConfigurations()
    {
    	//TODO some more checks...
    	if(Math.min(Statics.DISPLAY_HEIGHT,  Statics.DISPLAY_WIDTH) < 540)
    	{
    		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_display_size_warning));
            new CountDownTimer(Toast.LENGTH_LONG, 1000) {

                public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                MainActivity.this.finish();
            }
            }.start();
    	}
    }
} 
