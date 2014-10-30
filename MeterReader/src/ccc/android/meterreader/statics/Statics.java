package ccc.android.meterreader.statics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import ccc.android.meterdata.types.PingCallback;
import ccc.android.meterreader.BuildConfig;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.R;
import ccc.android.meterreader.datamanagement.async.AsyncPingBack;
import ccc.android.meterreader.helpfuls.PreferencesHelper;

@SuppressLint("DefaultLocale")
public class Statics 
{
	///DEBUG
	public static boolean debugMode = true;
	///DEBUG END
	
	public static final String WS_URL = "WS_URL";
	public static final String LAST_FULL_DOWN = "LAST_FULL_DOWN";
	public static final String UNDO_STACK_FILE_DEFAULT = "undo.txt";
	public static final String UNDO_STACK_FILE = "UNDO_STACK_FILE";
	public static final String REDO_STACK_FILE_DEFAULT = "redo.txt";
	public static final String REDO_STACK_FILE = "REDO_STACK_FILE";
	public static final String MAIN_SESSION_DEFAULT = "MainSession.txt";
	public static final String MAIN_SESSION = "MAIN_SESSION";
	public static final String LAST_SESSION_DEFAULT = "LastSession.txt";
	public static final String LAST_SESSION = "LAST_SESSION";
	public static final String DEBUG_SESSION_DEFAULT = "DebugSession.txt";
	public static final String DEBUG_SESSION = "DEBUG_SESSION";
	public static final String DIGIT_FILE_DEFAULT = "digits.txt";
	public static final String DIGIT_FILE = "DIGIT_FILE";
	public static final String IMAGE_FILE_DEFAULT = "images.txt";
	public static final String IMAGE_FILE = "IMAGE_FILE";
	public static final String ICON_FILE_DEFAULT = "icons.txt";
	public static final String ICON_FILE = "ICON_FILE";
	public static final int NUM_OF_PATTERN_SETS_DEFAULT = 10;
	public static final String NUM_OF_PATTERN_SETS = "NUM_OF_PATTERN_SETS";
	public static final int PINGS_TIL_CONTEXT_LOAD_DEFAULT = 10;
	public static final String PINGS_TIL_CONTEXT_LOAD = "PINGS_TIL_CONTEXT_LOAD";
	public static final int SHORT_DIALOG_DURATION_DEFAULT = 3;
	public static final String SHORT_DIALOG_DURATION = "SHORT_DIALOG_DURATION";
	public static final int NORMAL_DIALOG_DURATION_DEFAULT = 8;
	public static final String NORMAL_DIALOG_DURATION = "NORMAL_DIALOG_DURATION";
	public static final int LONG_DIALOG_DURATION_DEFAULT = 16;
	public static final String LONG_DIALOG_DURATION = "LONG_DIALOG_DURATION";
	public static final int INFINIT_DIALOG_DURATION_DEFAULT = 300;
	public static final String INFINIT_DIALOG_DURATION = "INFINIT_DIALOG_DURATION";
	public static final int DAYS_TIL_LIST_WARNING_RED_DEFAULT = 7;
	public static final String DAYS_TIL_LIST_WARNING_RED = "DAYS_TIL_LIST_WARNING_RED";
	public static final int DAYS_TIL_LIST_WARNING_YELLOW_DEFAULT = 3;
	public static final String DAYS_TIL_LIST_WARNING_YELLOW = "DAYS_TIL_LIST_WARNING_YELLOW";
	public static final int ONLINE_TIMER_DELAY_MS_DEFAULT = 1000;
	public static final String ONLINE_TIMER_DELAY_MS = "ONLINE_TIMER_DELAY_MS";
	public static final int OFFLINE_TIMER_DELAY_MS_DEFAULT = 30000;
	public static final String OFFLINE_TIMER_DELAY_MS = "OFFLINE_TIMER_DELAY_MS";
	public static final int UPLOAD_SYNC_TIMER_DELAY_MS_DEFAULT = 900000; 	//15 min
	public static final String UPLOAD_SYNC_TIMER_DELAY_MS = "UPLOAD_SYNC_TIMER_DELAY_MS";
	public static final int DOWNLOAD_SYNC_TIMER_DELAY_MS_DEFAULT = 3600000;//1 h
	public static final String DOWNLOAD_SYNC_TIMER_DELAY_MS = "DOWNLOAD_SYNC_TIMER_DELAY_MS";
	public static final int SYNC_TIMEOUT_DELAY_MS_DEFAULT = 300000;
	public static final String SYNC_TIMEOUT_DELAY_MS = "SYNC_TIMEOUT_DELAY_MS";
	public static final int USER_INACTIVITY_THRESHOLD_IN_MIN_DEFAULT = 6;
	public static final String USER_INACTIVITY_THRESHOLD_IN_MIN = "USER_INACTIVITY_THRESHOLD_IN_MIN";
	public static final int LAST_FULL_SYNC_THRESHOLD_IN_DAYS_DEFAULT = 6;
	public static final String LAST_FULL_SYNC_THRESHOLD_IN_DAYS = "LAST_FULL_SYNC_THRESHOLD_IN_DAYS";
	public static final int MIN_DISPLAY_HEIGHT_DEFAULT = 540;
	public static final String MIN_DISPLAY_HEIGHT = "MIN_DISPLAY_HEIGHT";

	//private static String BASE_WS_URL = null;
	public static final String EPVI_LIST_ITEM_EXPANDED = "epviListItemExpanded";
	public static final String EPVI_LIST_ITEM = "epviListItem";
	public static final String ANDROID_INTENT_ACTION_GDR = "android.intent.action.GaugeDisplayRequest";
	public static final String ANDROID_INTENT_ACTION_NEW = "android.intent.action.NewReading";
	public static final String ANDROID_INTENT_ACTION_BAR = "android.intent.action.Barcode";
	public static final String ANDROID_INTENT_ACTION_NBFR = "android.intent.action.NewBarcodeForReg";
	public static final String ANDROID_INTENT_ACTION_RBFR = "android.intent.action.ReadBarcodeForReg";
	public static final String ANDROID_INTENT_ACTION_FIN = "android.intent.action.Finish";
	public static final String BARCODE_GAUGEID = "[0-9]+(?=D)";
	public static final String BARCODE_PATTERN = "ID[A-Z]{3}[0-9]{5}D[0-9]{6}";
	public static final int GAUGE_DISPLAY_REQUEST_CODE = 99;
	public static final int GAUGE_INFO_VIEW = 0;
	public static final int GAUGE_DISPLAY_VIEW = 1;
	public static final int QR_PREVIEW_VIEW = 2;
	public static final int DIGIT_READER_VIEW = 3;

	public static int DISPLAY_HEIGHT;
	public static int DISPLAY_WIDTH;
		
	//private statics
	private static PowerManager.WakeLock wl;
	private static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	private static Date lastUtcTime;
	private static boolean isOnline = false;
    private static boolean wasOnline = false;
	private static Timer pingTimer;
	private static Timer upSyncTimer;
	private static Timer downSyncTimer;
	private static int pingsTilContextLoad;  //ergo  pingsTilContextLoad*isOnlineTimerDelay = time before context gets loaded from file and not database!
	private static MainActivity activity;
    private static Typeface sansPro; 
    private static Resources LocaleResources;
    private static java.text.DateFormat localeDateFormat;
//    private static List<UpSyncTask> upSyncs = new ArrayList<UpSyncTask>();
//    private static List<DownSyncTask> downSyncs = new ArrayList<DownSyncTask>();
    
    private static Date lastInteraction;
    
    private static SyncMode syncMode = SyncMode.Partial;
	
	public enum SyncState
	{
		Synchron,		//down & up synchronization is done
		Desynchron,		//up-synchronization has to be done
		Asynchron		//down- and up-synchronization is out of date
	}

	public enum SyncMode
	{
		Full,			//All data gets synchronized with the database (including images)
		Partial			//Images will not be synchronized with the database, but loaded from last session
	}
	
	public static void initializeStatics(MainActivity activ)
	{
		upSyncTimer = new Timer();
		downSyncTimer = new Timer();

		
		//TODO read settings file
		activity = activ;
		sansPro = Typeface.createFromAsset(activ.getAssets(), "fonts/SourceSansPro.ttf");
		
	    if (Statics.debugMode)
	    	LocaleResources = getLocalResources("en");
	    else
	    	LocaleResources = getLocalResources(activ.getResources().getConfiguration().locale.getCountry());
	    localeDateFormat = DateFormat.getDateFormat(activ);
	    
    	DisplayMetrics displayMetrics = new DisplayMetrics();
    	WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
    	wm.getDefaultDisplay().getMetrics(displayMetrics);
    	DISPLAY_WIDTH = displayMetrics.widthPixels;
    	DISPLAY_HEIGHT = displayMetrics.heightPixels;
    	
		String BASE_WS_URL = StaticPreferences.getPreference(WS_URL, null);
		if(BASE_WS_URL == null)
			Statics.ShowAlertDiaMsgWithBt(LocaleResources.getString(R.string.no_base_ws_url_warning), 300);

    	PowerManager pm = (PowerManager)activity.getSystemService(Context.POWER_SERVICE);
    	wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "partialWl");
		
    	Integer pings = StaticPreferences.getPreference(PINGS_TIL_CONTEXT_LOAD, PINGS_TIL_CONTEXT_LOAD_DEFAULT);
    	pings = pings == null ? 10 : pings;
		pingsTilContextLoad = pings;
    	
		Long l = StaticPreferences.getPreference(LAST_FULL_DOWN, null);
    	Date last = new Date();
		if(l != null)
			last = new Date(l);
    	Integer lastFullThres = StaticPreferences.getPreference(LAST_FULL_SYNC_THRESHOLD_IN_DAYS, LAST_FULL_SYNC_THRESHOLD_IN_DAYS_DEFAULT);
    	lastFullThres = lastFullThres == null ? 6 : lastFullThres;
    	if(last == null || Statics.getDateDiff(last, new Date(), TimeUnit.DAYS) > lastFullThres)
    		syncMode = SyncMode.Full;
    	
    	Integer upSyncDelay = StaticPreferences.getPreference(UPLOAD_SYNC_TIMER_DELAY_MS, UPLOAD_SYNC_TIMER_DELAY_MS_DEFAULT);
    	upSyncDelay = upSyncDelay == null ? 900000 : upSyncDelay;
		upSyncTimer.schedule(new UpSyncTask(), upSyncDelay);
	}
	
	public static String getWSURL()
	{
		return StaticPreferences.getPreference(WS_URL, null);
	}
	

	public static class SyncTimeoutTask extends TimerTask
	{
		@Override
		public void run() {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					activity.OnFailedSessionSynchronization(getDefaultResources().getString(R.string.main_sync_timeout_msg));
				}
			});
		}
	}
	
	private static class UpSyncTask extends TimerTask
	{
		@Override
		public void run() {
			upSyncTimer = new Timer();
			if(isOnline)
			{
				if(activity.getManager().unDoStackSize() > 0 
						&& (lastInteraction == null || Statics.getDateDiff(lastInteraction, new Date(), TimeUnit.MINUTES) > StaticPreferences.getPreference(USER_INACTIVITY_THRESHOLD_IN_MIN, USER_INACTIVITY_THRESHOLD_IN_MIN_DEFAULT)))
				{
					activity.runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
							activity.getManager().UploadToDataBase();
			            }
			        });
				}
				upSyncTimer.schedule(new UpSyncTask(), StaticPreferences.getPreference(UPLOAD_SYNC_TIMER_DELAY_MS, UPLOAD_SYNC_TIMER_DELAY_MS_DEFAULT));
			}
			else
				upSyncTimer.schedule(new UpSyncTask(), StaticPreferences.getPreference(OFFLINE_TIMER_DELAY_MS, OFFLINE_TIMER_DELAY_MS_DEFAULT));
	    }
	}
	
	private static class DownSyncTask extends TimerTask
	{
		@Override
		public void run() {
			downSyncTimer = new Timer();
			if(isOnline)
			{
				if(lastInteraction == null || Statics.getDateDiff(lastInteraction, new Date(), TimeUnit.MINUTES) > StaticPreferences.getPreference(USER_INACTIVITY_THRESHOLD_IN_MIN, USER_INACTIVITY_THRESHOLD_IN_MIN_DEFAULT))
				{
					activity.runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
 			            	if(syncMode == SyncMode.Full || Statics.debugMode)
			            		activity.getManager().LoadFullContextFromDb();
			            	else
			            		activity.getManager().LoadContextFromDb();
			            }
			        });
				}
				downSyncTimer.schedule(new DownSyncTask(), StaticPreferences.getPreference(DOWNLOAD_SYNC_TIMER_DELAY_MS, DOWNLOAD_SYNC_TIMER_DELAY_MS_DEFAULT));
			}
			else
				downSyncTimer.schedule(new DownSyncTask(), StaticPreferences.getPreference(OFFLINE_TIMER_DELAY_MS, OFFLINE_TIMER_DELAY_MS_DEFAULT));
	    }
	}
	
	private static class FileSyncTask extends TimerTask
	{
		@Override
		public void run() 
		{
			downSyncTimer = new Timer();
			activity.runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
			        activity.getManager().LoadContextFromFile();
	            }
	        });
			
			downSyncTimer.schedule(new DownSyncTask(), StaticPreferences.getPreference(DOWNLOAD_SYNC_TIMER_DELAY_MS, DOWNLOAD_SYNC_TIMER_DELAY_MS_DEFAULT));
	    }
	}
	
	public static void pauseTimer()
	{
		upSyncTimer.cancel();
		downSyncTimer.cancel();
	}
	
	public static void resumeTimer()
	{
		upSyncTimer.cancel();
		downSyncTimer.cancel();
		upSyncTimer = new Timer();
		upSyncTimer.schedule(new UpSyncTask(), StaticPreferences.getPreference(OFFLINE_TIMER_DELAY_MS, OFFLINE_TIMER_DELAY_MS_DEFAULT));
		downSyncTimer = new Timer();
		downSyncTimer.schedule(new DownSyncTask(), StaticPreferences.getPreference(OFFLINE_TIMER_DELAY_MS, OFFLINE_TIMER_DELAY_MS_DEFAULT));
	}
	
	public static void syncNow()
	{
		lastInteraction = null;
		downSyncTimer.cancel();
		downSyncTimer = new Timer();
		downSyncTimer.schedule(new DownSyncTask(), 10);
		upSyncTimer.cancel();
		upSyncTimer = new Timer();
		upSyncTimer.schedule(new UpSyncTask(), 10);
	}
	
	public static Configuration getConfiguration()
	{
	    // Change locale settings in the app.
	    return LocaleResources.getConfiguration();
	}
	
	public static Resources getLocalResources (String countryCode)
	{
		if(activity.getResources() == null)
			return null;
		Configuration config = activity.getResources().getConfiguration();
		if(countryCode != null)
		{
			for(Locale loc : Locale.getAvailableLocales())
			{
				if(loc.getCountry().toLowerCase().equals(countryCode.toLowerCase()))
				{
					config.locale = loc;
					Locale.setDefault(loc);
				}
			}
		}
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		activity.getResources().updateConfiguration(config, metrics);
		return new Resources(activity.getAssets(), metrics, config);
	}

	public static void startPing() {
		pingTimer = new Timer();
		TimerTask pingInTime = new TimerTask()
		{
			@Override
			public void run() {
				activity.runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		            	//TODO
		            	initializePing();
		            }
		        });
			}
		};
		int onlineTimerDelay = StaticPreferences.getPreference(ONLINE_TIMER_DELAY_MS, ONLINE_TIMER_DELAY_MS_DEFAULT);
		pingTimer.schedule(pingInTime, onlineTimerDelay, onlineTimerDelay);
	}
	
	private static void initializePing()
	{
		AsyncPingBack gml = new AsyncPingBack();
		gml.execute();
	}
	
	public static void stopPing() {
		pingTimer.cancel();
	}

	public static void receivePingback(PingCallback res)
	{
		if(res.getPingResult()==1)
		{
			isOnline =  true;
		}
		else
		{
			isOnline = false;
			pingsTilContextLoad--;
			if(res.getError() != null)
			{
				stopPing();
				ShowAlertDiaMsgWithBt(res.getError(), StaticPreferences.getPreference(INFINIT_DIALOG_DURATION, INFINIT_DIALOG_DURATION_DEFAULT));
			}
			if(pingsTilContextLoad == 0)			//load from file
			{
				ShowAlertDiaMsg(activity, Statics.getDefaultResources().getString(R.string.main_sync_load_from_file_msg), 
						StaticPreferences.getPreference(OFFLINE_TIMER_DELAY_MS,OFFLINE_TIMER_DELAY_MS_DEFAULT), loadFromFileListener, waitListener);
			}
		}
		
		if(res.getUtc() != null)
		{
			@SuppressWarnings("deprecation")
			int timezoneOffset = res.getUtc().getTimezoneOffset() * 60 * 1000;
			lastUtcTime = new Date(res.getUtc().getTime() + timezoneOffset);
			calendar.setTimeInMillis(lastUtcTime.getTime());
		}
		if(wasOnline != isOnline && activity != null)
		{
			activity.invalidateOptionsMenu();
			if(isOnline)											//load from db
			{
				downSyncTimer.cancel();
				downSyncTimer = new Timer();
				downSyncTimer.schedule(new DownSyncTask(), 10);
			}
		}
		wasOnline = isOnline;
	}
	
	private static DialogInterface.OnClickListener loadFromFileListener = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface arg0, int arg1) 
		{
			downSyncTimer.schedule(new FileSyncTask(), 10);
		}
		
		@Override
		public String toString() 
		{
			return Statics.getDefaultResources().getString(R.string.common_ok);
		}
	};
	
	private static DialogInterface.OnClickListener waitListener = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface arg0, int arg1) 
		{
			pingsTilContextLoad = StaticPreferences.getPreference(PINGS_TIL_CONTEXT_LOAD, PINGS_TIL_CONTEXT_LOAD_DEFAULT)*2;
		}
		
		@Override
		public String toString() 
		{
			return Statics.getDefaultResources().getString(R.string.common_wait);
		}
	};
	
	public static void WriteRawFile(String fileName, boolean append, String... records) throws IOException {

	    FileOutputStream fos;
	    if(append)
	    	fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE|Context.MODE_APPEND);
	    else
	    	fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
	    
	    String separator = System.getProperty("line.separator");
	    OutputStreamWriter osw = new OutputStreamWriter(fos);

	    for(String str : records)
	    {
		    osw.append(str);
		    osw.append(separator); // this will add new line ;
	    }
	    osw.flush();
	    osw.close();
	    fos.close();
	}
	
	public static void RemoveLastLineOfFile(String fileName)
	{
		RandomAccessFile f;
		try
		{
			f = new RandomAccessFile(fileName, "rw");

			long length = f.length() - 1;
			byte b = ' ';
			do {                     
			  length -= 1;
			  f.seek(length);
			  b = f.readByte();
			} while(b != 10 && length > 0);
			f.setLength(length+1);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static BufferedReader ReadFile(String path) throws FileNotFoundException
	{
		InputStreamReader reader;
		InputStream targetFile = null;
		try {
			if(!path.equals(StaticPreferences.getPreference(Statics.MAIN_SESSION, Statics.MAIN_SESSION_DEFAULT))) //not!
			{
				targetFile = activity.openFileInput(path);
			}
			else
				targetFile = activity.getAssets().open(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			reader = new InputStreamReader(targetFile);
			return new BufferedReader(reader );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void WriteToExternal(BufferedReader br, String subPath) throws IOException {
		if(!subPath.startsWith("/"))  //not!
			subPath = "/" + subPath;
		  File directory = Environment.getExternalStorageDirectory();
		  File file = new File(directory + subPath);
	    BufferedWriter bw = null;
	    try {
	        bw = new BufferedWriter(new FileWriter(file));

	        int in;
	        while ((in = br.read()) != -1) {
	            bw.write(in);
	        }
	    } finally {
	        if (bw != null) {
	            bw.close();
	        }
	        br.close();
	    }
	}
	
	public BufferedReader ReadFileFromExternal(String subPath) 
	{
		if(!subPath.startsWith("/"))  //not!
			subPath = "/" + subPath;
		  File directory = Environment.getExternalStorageDirectory();
		  File file = new File(directory + subPath);
		  if (!file.exists()) {
		    throw new RuntimeException("File not found");
		  }
		  BufferedReader reader = null;
		  try {
		    reader = new BufferedReader(new FileReader(file));
		    return reader;
		  } catch (Exception e) {
		    e.printStackTrace();
		  } finally {
		    if (reader != null) {
		      try {
		        reader.close();
		      } catch (IOException e) {
		        e.printStackTrace();
		      }
		   }
		    
		}
		  return null;
	} 
	
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
	/**
	 * Generate a value suitable for use in {@link #setId(int)}.
	 * This value will not collide with ID values generated at build time by aapt for R.id.
	 *
	 * @return a generated ID value
	 */
	public static int generateViewId() {
	    for (;;) {
	        final int result = sNextGeneratedId.get();
	        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	        int newValue = result + 1;
	        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	        if (sNextGeneratedId.compareAndSet(result, newValue)) {
	            return result;
	        }
	    }
	}
	
	public static boolean isOnline()
	{
		return isOnline;
	}
	
	public static void ShowToast(String message)
	{
		ShowToast(activity, message);
	}
	
	public static void ShowToast(Context context, String message)
	{
		if(message == null)
			return;
		Toast notPlausible = Toast.makeText(context, message , Toast.LENGTH_LONG);
		notPlausible.setGravity(Gravity.BOTTOM, 0, 300);
		notPlausible.show();
	}
	
	private static DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface arg0, int arg1) 
		{
			arg0.dismiss();
		}
		@Override
		public String toString() 
		{
			return Statics.getDefaultResources().getString(R.string.common_ok);
		}
	};
	
	public static void ShowAlertDiaMsgWithBt(Context context, String message)
	{
		ShowAlertDiaMsg(context, message, StaticPreferences.getPreference(NORMAL_DIALOG_DURATION, NORMAL_DIALOG_DURATION_DEFAULT), okListener, null);
	}
	
	public static void ShowAlertDiaMsgWithBt(String message)
	{
		ShowAlertDiaMsg(activity, message, StaticPreferences.getPreference(NORMAL_DIALOG_DURATION, NORMAL_DIALOG_DURATION_DEFAULT), okListener, null);
	}
	
	public static void ShowAlertDiaMsgWithBt(String message, int seconds)
	{
		ShowAlertDiaMsg(activity, message, seconds, okListener, null);
	}
	
	public static void ShowAlertDiaMsg(Context context, String message, int seconds)
	{
		ShowAlertDiaMsg(context, message, seconds, null, null);
	}
	
	public static void ShowAlertDiaMsg(Context context, String message)
	{
		ShowAlertDiaMsg(context, message, StaticPreferences.getPreference(NORMAL_DIALOG_DURATION, NORMAL_DIALOG_DURATION_DEFAULT), null, null);
	}
	
	public static void ShowAlertDiaMsg(String message)
	{
		ShowAlertDiaMsg(activity, message, StaticPreferences.getPreference(NORMAL_DIALOG_DURATION, NORMAL_DIALOG_DURATION_DEFAULT), null, null);
	}
	
	public static void ShowAlertDiaMsg(String message, int seconds)
	{
		ShowAlertDiaMsg(activity, message, seconds, null, null);
	}
	
	private static void ShowAlertDiaMsg(Context context, String message, int seconds, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener caListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		
		if(okListener != null)
		{
			builder.setPositiveButton(okListener.toString(), okListener);
		}
		
		if(caListener != null)
		{
			builder.setNegativeButton(caListener.toString(), caListener);
		}
		
		final AlertDialog d = builder.create();
		d.show();
		
		// Hide after some seconds
		final Handler handler  = new Handler();
		final Runnable runnable = new Runnable() {
		    @Override
		    public void run() {
		        if (d.isShowing()) {
		            try
					{
						d.dismiss();
					}
					catch (IllegalArgumentException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    }
		};

		d.setOnDismissListener(new DialogInterface.OnDismissListener() {
		    @Override
		    public void onDismiss(DialogInterface dialog) {
		        handler.removeCallbacks(runnable);
		    }
		});

		handler.postDelayed(runnable, seconds*1000);
	}
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	public static void SetText(TextView view, Object... value)
	{
		String text = "";
		for (int i = 0; i < value.length; i++) {
			if(i > 0)
				text += ", ";
			if (value[i] == null)
				text += "";
			else if (value[i].getClass() == Date.class)
				text += localeDateFormat.format((Date) value[i]);
			else
				text += String.valueOf(value[i]);
		}
		view.setText(text);
		view.setTypeface(Statics.sansPro);
	}

	public static Resources getDefaultResources() {
		return LocaleResources;
	}
	
	public static int daysSince(Date in)
	{
		Date today = Statics.getUtcTime();
		long ms = today.getTime() - in.getTime();
		ms = ms/(1000*60*60*24);
		return (int)ms;
	}

	public static java.text.DateFormat getLocaleDateFormat() {
		return localeDateFormat;
	}

	public static Date getLastInteraction() {
		return lastInteraction;
	}

	public static void setLastInteraction(Date lastInteraction) {
		Statics.lastInteraction = lastInteraction;
	}

    public static Comparator<Size> previewSizeComp = new Comparator<Size>() 
    {
		@Override
		public int compare(Size arg0, Size arg1) {
			return (int) (arg1.width - arg0.width);
		}
    };
    
    public static void ChangeLayoutElementsVisibility(View layout, int fromState, int toState) 
    {
    	if(layout instanceof ViewGroup)
    	{
	        for (int i = 0; i < ((ViewGroup)layout).getChildCount(); i++) {
	            View v = ((ViewGroup)layout).getChildAt(i);
	        	if(v instanceof ViewGroup)
	        		ChangeLayoutElementsVisibility(v, fromState, toState);
	        	if(v.getVisibility() == fromState)
	            	v.setVisibility(toState);
	        }
    	}
    }
    
    @SuppressWarnings("unchecked")
	public static <T extends View> List<T> GetSubElementsOfType(View layout, Class<T> t) 
    {
    	List<T> list = new ArrayList<T>();
    	if(layout instanceof ViewGroup)
    	{
	        for (int i = 0; i < ((ViewGroup)layout).getChildCount(); i++) {
	            View v = ((ViewGroup)layout).getChildAt(i);
	        	if(v instanceof ViewGroup)
	        		list.addAll(GetSubElementsOfType(v, t));
	        	else if(t.isAssignableFrom(v.getClass()))
	        		list.add((T) v);
	        }
    	}
    	return list;
    }
    
    public static Date getUtcTime()
    {
    	Date now = calendar.getTime();
//    	if(lastUtcTime != null)
//    		now = new Date(now.getTime() + utcDiff);
    	return now;    }

	public static PowerManager.WakeLock getWl() {
		return wl;
	}

	public static SyncMode getSyncMode()
	{
		return syncMode;
	}

	public static void setSyncMode(SyncMode syncMode)
	{
		Statics.syncMode = syncMode;
	}
}
