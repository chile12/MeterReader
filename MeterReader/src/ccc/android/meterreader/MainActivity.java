package ccc.android.meterreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import ccc.android.meterdata.listtypes.PreferenceList;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.datamanagement.DataContextManager;
import ccc.android.meterreader.datamanagement.IDataContextEventListener;
import ccc.android.meterreader.deviceregistration.DeviceRegistrationFragment;
import ccc.android.meterreader.exceptions.UserNotInitializedException;
import ccc.android.meterreader.gaugedisplaydialog.GaugeDisplayDialog;
import ccc.android.meterreader.internaldata.InternalDialogDisplayData;
import ccc.android.meterreader.internaldata.Session;
import ccc.android.meterreader.statics.StaticPreferences;
import ccc.android.meterreader.statics.Statics;
import ccc.android.meterreader.statics.Statics.SyncState;
import ccc.android.meterreader.viewelements.ConnectionConfigDialog;
import ccc.android.meterreader.viewelements.GaugeListViewFragment;
import ccc.android.meterreader.viewelements.VerticalButton;

public class MainActivity extends Activity implements IDataContextEventListener
{
	public static Context ApplicationContext;
	
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
    private SyncState syncState = SyncState.Asynchron;
    
    
	private IntentFilter barcodeIntentFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_BAR);
	private IntentFilter barcodeForRegFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_NBFR);
	private IntentFilter readbarcodeForRegFilter = new IntentFilter(Statics.ANDROID_INTENT_ACTION_RBFR);
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
        ApplicationContext = getApplicationContext();
        StaticPreferences.setPreferences(new PreferenceList());
        Statics.initializeStatics(this);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        manager  = new DataContextManager(this);
        
        OptionsLL = (LinearLayout) findViewById(R.id.OptionLL);
        ButtonLL = (LinearLayout) findViewById(R.id.menuButtonsLL);
        OptionBT = (VerticalButton) findViewById(R.id.OptionBT);
        VF = (ViewFlipper) findViewById(R.id.MainFlipperVF);
        ((Button) findViewById(R.id.clearStacksBT)).setOnClickListener(clearSessionListener);
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
    	checkSystemConfigurations();

    }
    
    @Override
    protected void onStart()
    {
    	super.onStart();

    }
	
    @Override
    protected void onPause()
    {
    	Statics.stopPing();
    	Statics.pauseTimer();
    	super.onPause();
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	Statics.startPing();
    	Statics.resumeTimer();
    	this.registerReceiver(BarcodeReceiver, barcodeIntentFilter);
    	this.registerReceiver(BarcodeReceiver, barcodeForRegFilter);
    	this.registerReceiver(BarcodeReceiver, readbarcodeForRegFilter);
    	this.registerReceiver(newReadReceiver, newReadIntentFilter);
    	updateRedoStackView();
    	setSessionButtonView ();
    }

	private void updateRedoStackView()
	{
		if(manager.unDoStackSize() > 0)
    		((TextView) findViewById(R.id.mainTickerLA)).setText(manager.unDoStackPeek().getActionText(getManager().getData()));
    	else
    		((TextView) findViewById(R.id.mainTickerLA)).setText("");
	}
    
    @Override
    protected void onStop()
    {
		Intent i = new Intent(Statics.ANDROID_INTENT_ACTION_FIN);
		this.sendBroadcast(i);
    	this.unregisterReceiver(this.BarcodeReceiver);
    	this.unregisterReceiver(this.newReadReceiver);
   	   	super.onStop();
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    }
    
   @Override
    public boolean onPrepareOptionsMenu(Menu menu) { 
    	    MenuInflater inflater = getMenuInflater();
    	    inflater.inflate(R.menu.main, menu);
			menu.clear();
				if (Statics.isOnline() == true) {
					findViewById(R.id.newSessionBT).setBackgroundResource((R.drawable.buttonback));
					menu.add(0,  99, 0, Statics.getDefaultResources().getString(R.string.main_connected)).setIcon(getResources().getDrawable(R.drawable.wifi_icon_green)).setShowAsAction(2);
				} else {
					findViewById(R.id.newSessionBT).setBackgroundResource((R.drawable.buttonbackinactive));
					menu.add(0,  99, 0, Statics.getDefaultResources().getString(R.string.main_not_connected)).setIcon(getResources().getDrawable(R.drawable.wifi_icon_red)).setShowAsAction(2);
				}
				if(this.syncState == SyncState.Asynchron)
					menu.add(0, 100, 1, Statics.getDefaultResources().getString(R.string.main_data_asynchron)).setIcon(getResources().getDrawable(R.drawable.synchro_icon_red)).setShowAsAction(2);
				else if(this.syncState == SyncState.Desynchron)
					menu.add(0, 100, 1, Statics.getDefaultResources().getString(R.string.main_data_desynchron)).setIcon(getResources().getDrawable(R.drawable.synchro_icon_yellow)).setShowAsAction(2);
				else
					menu.add(0, 100, 1, Statics.getDefaultResources().getString(R.string.main_data_synchron)).setIcon(getResources().getDrawable(R.drawable.synchro_icon_green)).setShowAsAction(2);
					
    	return true;
    }
    
	public void openGaugeDisplayDialog(String androidIntentAction) 
	{
	    Intent startGaugeDisplay = new Intent(MainActivity.this, GaugeDisplayDialog.class);
	    startGaugeDisplay.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    startGaugeDisplay.putExtra("requestCode", androidIntentAction); 
	    startActivity(startGaugeDisplay);
	}
    
	public void openGaugeDisplayDialog(Gauge ga, GaugeDevice de, Reading rd) 
	{
		if(de == null)
		{
			Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.new_device_device_not_found));
			return;
		}
	    Intent startGaugeDisplay = new Intent(MainActivity.this, GaugeDisplayDialog.class);
	    InternalDialogDisplayData data = new InternalDialogDisplayData(ga, de, rd);
	    startGaugeDisplay.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    startGaugeDisplay.putExtra("data", data);
	    startGaugeDisplay.putExtra("requestCode", Statics.ANDROID_INTENT_ACTION_GDR); 
	    startActivity(startGaugeDisplay);
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
        		openGaugeDisplayDialog(Statics.ANDROID_INTENT_ACTION_BAR);
        	else
        		Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.main_sync_not_done), StaticPreferences.getPreference(Statics.SHORT_DIALOG_DURATION, Statics.SHORT_DIALOG_DURATION_DEFAULT));
        }
    };
    
    private OnClickListener loadSessionListener = new OnClickListener() {
        public void onClick(View v) 
        {    

	        	manager.LoadContextFromFile(StaticPreferences.getPreference(Statics.LAST_SESSION, Statics.LAST_SESSION_DEFAULT), true);
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
				Statics.ShowAlertDiaMsgWithBt("no user!", StaticPreferences.getPreference(Statics.INFINIT_DIALOG_DURATION, Statics.INFINIT_DIALOG_DURATION_DEFAULT));
			}
        }
    };
    
	public void loadContextFromDb() {
		if(Statics.isOnline())
        	manager.LoadFullContextFromDb();
    	else
    		Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.main_sync_not_successful), 
    				StaticPreferences.getPreference(Statics.INFINIT_DIALOG_DURATION, Statics.INFINIT_DIALOG_DURATION_DEFAULT));
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
    				Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.main_sync_not_done), 
    						StaticPreferences.getPreference(Statics.SHORT_DIALOG_DURATION, Statics.SHORT_DIALOG_DURATION_DEFAULT));
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
    		Gauge ga = manager.getData().getGauges().getByBarcode(barcode);
    		Reading lastRead = null;
    		GaugeDevice de = null;
    		if(ga != null)
    		{
    			lastRead = manager.getData().getReadings().getLastReadingById(ga.getGaugeId());
    			de = manager.getData().getDevices().getById(ga.getGaugeDeviceId());
    		}
    		
            if(intent.getAction() == Statics.ANDROID_INTENT_ACTION_NBFR && newDeviceFragment != null)
            {
        		if(ga == null)
        		{
        			newDeviceFragment.setBarcode(barcode, ga);
        		}
        		else
        		{
        			Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.new_device_barcode_in_use));
        			openGaugeDisplayDialog(Statics.ANDROID_INTENT_ACTION_NBFR);
        		}
            }
            else if(intent.getAction() == Statics.ANDROID_INTENT_ACTION_RBFR && newDeviceFragment != null)
            {
        		if(ga == null)
        		{
        			newDeviceFragment.setBarcode(barcode, ga);
        			newDeviceFragment.setGauge(ga);
        		}
        		else
        		{
        			newDeviceFragment.setGauge(ga);
        		}
            }
            else if(barcode != null && intent.getAction() == Statics.ANDROID_INTENT_ACTION_BAR)
        	{
        		if(ga != null)
        			openGaugeDisplayDialog(ga, de, lastRead);
        		else
        			openGaugeDisplayDialog(Statics.ANDROID_INTENT_ACTION_BAR);
        	}
        	else
        	{
        		Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.qr_not_recognized));
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
        	manager.SaveContextToFile(StaticPreferences.getPreference(Statics.DEBUG_SESSION, Statics.DEBUG_SESSION_DEFAULT), true);
        }
    };
    
    private OnClickListener clearSessionListener = new OnClickListener() {
        public void onClick(View v) {
        	manager.clearStacks();
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
        	manager.LoadContextFromFile(StaticPreferences.getPreference(Statics.DEBUG_SESSION, Statics.DEBUG_SESSION_DEFAULT), true);
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
	public void OnSessionUnsynchronized() {
		this.syncState = SyncState.Desynchron;
		invalidateOptionsMenu();
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonback);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonback);
        if(listViewFragment != null)
        	listViewFragment.RedoMappings();
        updateRedoStackView();
	}

	@Override
	public void OnSessionInitialized() {
		this.syncState = SyncState.Asynchron;
		invalidateOptionsMenu();
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
	        manager.SaveContext();
        }
        showSyncStatusToast();
		if(syncDias.size() == 0)
		{
			this.syncState = SyncState.Synchron;
			invalidateOptionsMenu();
		}
		this.manager.actionStackLoad(StaticPreferences.getPreference(Statics.UNDO_STACK_FILE, Statics.UNDO_STACK_FILE_DEFAULT));
		this.manager.applyUnDoStackToSession();
		updateRedoStackView();
	}

	@Override
	public void OnFileSynchronization(Session session)
	{
        findViewById(R.id.showAllStationsBT).setBackgroundResource(R.drawable.buttonback);
        findViewById(R.id.ScanGaugeBT).setBackgroundResource(R.drawable.buttonback);
        if(listViewFragment != null)
        	listViewFragment.RedoMappings();
        
        showSyncStatusToast(Statics.getDefaultResources().getString(R.string.main_sync_file), StaticPreferences.getPreference(Statics.NORMAL_DIALOG_DURATION, Statics.NORMAL_DIALOG_DURATION_DEFAULT));
		if(syncDias.size() == 0)
		{
			this.syncState = SyncState.Desynchron;
			invalidateOptionsMenu();
		}

		this.manager.actionStackLoad(StaticPreferences.getPreference(Statics.UNDO_STACK_FILE, Statics.UNDO_STACK_FILE_DEFAULT));
		this.manager.applyUnDoStackToSession();
		updateRedoStackView();
	}
	
	@Override
	public void OnFailedSessionSynchronization(String msg) {
		this.syncState = SyncState.Asynchron;
		invalidateOptionsMenu();
		closeSyncDialog();
		closeSyncDialog();
		closeSyncDialog();
		Statics.resumeTimer();
		Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.main_sync_not_successful) + "\n" + msg, 
				StaticPreferences.getPreference(Statics.INFINIT_DIALOG_DURATION, Statics.NORMAL_DIALOG_DURATION_DEFAULT));
	}
	
	private void showSyncStatusToast()
	{
		showSyncStatusToast(Statics.getDefaultResources().getString(R.string.main_sync_done), StaticPreferences.getPreference(Statics.SHORT_DIALOG_DURATION, Statics.SHORT_DIALOG_DURATION_DEFAULT));
	}
	
	private void showSyncStatusToast(String msg, int duration)
	{
			if(closeSyncDialog())
				Statics.ShowAlertDiaMsg(msg, duration);
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
					
					Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.main_sync_aborted), 
							StaticPreferences.getPreference(Statics.INFINIT_DIALOG_DURATION, Statics.INFINIT_DIALOG_DURATION_DEFAULT));
				}});
		AlertDialog d = builder.create();
		d.show();
		syncDias.add(d);
	}
	
	private boolean closeSyncDialog()
	{
		if(syncDias.size() > 0)
		{
			syncDias.get(0).dismiss();
			syncDias.remove(0);
			if(syncDias.size() == 0)
				return true;
		}
		return false;
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
				fragmentTransaction.commitAllowingStateLoss();
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
        	manager.AddNewRead(read);
        	
        	if(newDeviceFragment != null) //registration in progress
        	{
        		newDeviceFragment.setNewRead(read);
        	}
        }
    };

    private void checkSystemConfigurations()
    {
    	//TODO some more checks...
    	if(Math.min(Statics.DISPLAY_HEIGHT,  Statics.DISPLAY_WIDTH) < StaticPreferences.getPreference(Statics.MIN_DISPLAY_HEIGHT, Statics.MIN_DISPLAY_HEIGHT_DEFAULT))
    	{
    		Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.main_display_size_warning));
            new CountDownTimer(Toast.LENGTH_LONG, StaticPreferences.getPreference(Statics.NORMAL_DIALOG_DURATION, Statics.NORMAL_DIALOG_DURATION_DEFAULT)*1000) {

                public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                MainActivity.this.finish();
            }
            }.start();
    	}
    }

	public boolean isInitial()
	{
		return initial;
	}

	public void setInitial(boolean initial)
	{
		this.initial = initial;
	}
} 
