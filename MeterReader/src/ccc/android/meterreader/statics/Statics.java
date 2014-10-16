package ccc.android.meterreader.statics;

import java.io.*;
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

import ccc.android.meterdata.types.PingCallback;
import ccc.android.meterreader.BuildConfig;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.R;
import ccc.android.meterreader.datamanagement.async.AsyncPingBack;
import ccc.android.meterreader.helpfuls.PreferencesHelper;

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

public class Statics 
{
	public static final String WS_URL_KEY = "WS_URL_KEY";
	public static final String LAST_FULL_DOWN_KEY = "LAST_FULL_DOWN_KEY";
	public static String BASE_WS_URL = null;
	public static final String UNDOSTACK = "undo.txt";
	public static final String REDOSTACK = "redo.txt";
	public static final String MAIN_SESSION = "MainSession.txt";
	public static final String LAST_SESSION = "LastSession.txt";
	public static final String DEBUG_SESSION = "DebugSession.txt";
	public static final String DIGIT_FILE = "digits.txt";
	public static final int NUM_OF_SETS_OF_PATTERNS = 10;
	public static final int PINGS_TIL_CONTEXT_LOAD = 5;
	public static final int SHORT_DIALOG_DURATION = 3;
	public static final int NORMAL_DIALOG_DURATION = 8;
	public static final int LONG_DIALOG_DURATION = 16;
	public static final int INFINIT_DIALOG_DURATION = 300;

	//TODO replace with gauge specific threshold values
	public static final int DAYS_FOR_WARNING_COLOR_RED = 7;
	public static final int DAYS_FOR_WARNING_COLOR_YEL = 3;
	
	public static final int IS_ONLINE_TIMER_DELAY = 1000;
	public static final int OFFLINE_TIMER_DELAY = 10000;
	public static final int UPLOAD_SYNC_TIMER_DELAY = 600000; 	//10 min
	public static final int DOWNLOAD_SYNC_TIMER_DELAY = 3600000;//1 h
	public static final int USER_INACTIVITY_THRESHOLD_IN_MIN = 3;
	
	public static final String EPVI_LIST_ITEM_EXPANDED = "epviListItemExpanded";
	public static final String EPVI_LIST_ITEM = "epviListItem";
	public static final String ANDROID_INTENT_ACTION_GDR = "android.intent.action.GaugeDisplayRequest";
	public static final String ANDROID_INTENT_ACTION_NEW = "android.intent.action.NewReading";
	public static final String ANDROID_INTENT_ACTION_BAR = "android.intent.action.Barcode";
	public static final String ANDROID_INTENT_ACTION_BFR = "android.intent.action.BarcodeForReg";
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
	
	
	//DEBUG
	public static boolean debugMode = true;
	//ENDDEBUG
	
	//private statics
	private static PowerManager.WakeLock wl;
	private static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	private static Date lastUtcTime;
	private static long utcDiff;
	private static boolean isOnline = false;
	private static Timer pingTimer;
	private static Timer upSyncTimer;
	private static Timer downSyncTimer;
	private static int pingsTilContextLoad = PINGS_TIL_CONTEXT_LOAD;  //ergo  pingsTilContextLoad*isOnlineTimerDelay = time before context gets loaded from file and not database!
	private static MainActivity activity;
    private static boolean wasOnline = false;
    private static Typeface sansPro; 
    private static Resources LocaleResources;
    private static java.text.DateFormat localeDateFormat;
    //used for constants editable by user
    private static PreferencesHelper Preferences;
    
    private static Date lastInteraction;
	
	public enum SyncState
	{
		Synchron,		//down & up synchronization is done
		Desynchron,		//up-synchronization has to be done
		Asynchron		//down- and up-synchronization is out of date
	}

	
	public static void initializeStatics(MainActivity activ,Typeface tf, String language)
	{
		upSyncTimer = new Timer();
		downSyncTimer = new Timer();

		upSyncTimer.schedule(new UpSyncTask(), UPLOAD_SYNC_TIMER_DELAY);
		
		//TODO read settings file
		activity = activ;
		sansPro = tf;
		Preferences = new PreferencesHelper(activ);
	    if (BuildConfig.DEBUG)
	    	LocaleResources = getLocalResources(language);
	    else
	    	LocaleResources = getLocalResources(null);
	    localeDateFormat = DateFormat.getDateFormat(activ);
		BASE_WS_URL = (String) Preferences.GetPreferences(WS_URL_KEY, String.class);
		if(BASE_WS_URL == null)
			Statics.ShowAlertDiaMsgWithBt(LocaleResources.getString(R.string.no_base_ws_url_warning), INFINIT_DIALOG_DURATION);
		
    	DisplayMetrics displayMetrics = new DisplayMetrics();
    	WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
    	wm.getDefaultDisplay().getMetrics(displayMetrics);
    	DISPLAY_WIDTH = displayMetrics.widthPixels;
    	DISPLAY_HEIGHT = displayMetrics.heightPixels;
    	
    	PowerManager pm = (PowerManager)activity.getSystemService(Context.POWER_SERVICE);
    	wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "partialWl");
    	wl.setReferenceCounted(true);
		//TODO remove
		//activity.invalidateOptionsMenu();
	}
	
	private static class UpSyncTask extends TimerTask
	{
		@Override
		public void run() {
			upSyncTimer = new Timer();
			if(isOnline)
			{
				if(activity.getManager().unDoStackSize() > 0 
						&& (lastInteraction == null || Statics.getDateDiff(lastInteraction, new Date(), TimeUnit.MINUTES) > USER_INACTIVITY_THRESHOLD_IN_MIN))
				{
					activity.runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
							activity.getManager().UploadToDataBase();
			            }
			        });
				}
				upSyncTimer.schedule(new UpSyncTask(), UPLOAD_SYNC_TIMER_DELAY);
			}
			else
				upSyncTimer.schedule(new UpSyncTask(), OFFLINE_TIMER_DELAY);
	    }
	}
	
	private static class DownSyncTask extends TimerTask
	{
		@Override
		public void run() {
			downSyncTimer = new Timer();
			if(isOnline)
			{
				if(lastInteraction == null || Statics.getDateDiff(lastInteraction, new Date(), TimeUnit.MINUTES) > USER_INACTIVITY_THRESHOLD_IN_MIN)
				{
					activity.runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
			            	Date last = new Date(Preferences.GetPreferences(LAST_FULL_DOWN_KEY, long.class));
			            	if(last == null || Statics.getDateDiff(last, new Date(), TimeUnit.DAYS) > 6)
			            		activity.getManager().LoadContextFromDb(true);
			            	else
			            		activity.getManager().LoadContextFromDb(true);
			            }
			        });
				}
				downSyncTimer.schedule(new DownSyncTask(), DOWNLOAD_SYNC_TIMER_DELAY);
			}
			else
				downSyncTimer.schedule(new DownSyncTask(), OFFLINE_TIMER_DELAY);
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
			
			downSyncTimer.schedule(new DownSyncTask(), DOWNLOAD_SYNC_TIMER_DELAY);
	    }
	}
	
	public static void pauseTimer()
	{
		upSyncTimer.cancel();
		downSyncTimer.cancel();
	}
	
	public static void resumeTimer()
	{
		upSyncTimer = new Timer();
		upSyncTimer.schedule(new UpSyncTask(), OFFLINE_TIMER_DELAY);
		downSyncTimer = new Timer();
		downSyncTimer.schedule(new DownSyncTask(), OFFLINE_TIMER_DELAY);
	}
	
	public static void downSyncNow()
	{
		lastInteraction = null;
		downSyncTimer.cancel();
		downSyncTimer = new Timer();
		downSyncTimer.schedule(new DownSyncTask(), 10);
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
		pingTimer.schedule(pingInTime, IS_ONLINE_TIMER_DELAY, IS_ONLINE_TIMER_DELAY);
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
				ShowAlertDiaMsgWithBt(res.getError(), INFINIT_DIALOG_DURATION);
			}
			if(pingsTilContextLoad == 1)			//load from file
			{
				downSyncTimer.schedule(new FileSyncTask(), 10);
			}
		}
		
		if(res.getUtc() != null)
		{
			int timezoneOffset = res.getUtc().getTimezoneOffset() * 60 * 1000;
			lastUtcTime = new Date(res.getUtc().getTime() + timezoneOffset);
			calendar.setTimeInMillis(lastUtcTime.getTime());
			utcDiff = Statics.getDateDiff(lastUtcTime, new Date(), TimeUnit.MILLISECONDS);
		}
		if(wasOnline != isOnline && activity != null)
		{
			activity.invalidateOptionsMenu();
			if(isOnline)											//load from db
				downSyncTimer.schedule(new DownSyncTask(), 10);
		}
		wasOnline = isOnline;
	}
	
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
			if(!path.equals(Statics.MAIN_SESSION)) //not!
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
	
	
	public static void ShowAlertDiaMsgWithBt(Context context, String message)
	{
		ShowAlertDiaMsg(context, message, NORMAL_DIALOG_DURATION, true);
	}
	
	public static void ShowAlertDiaMsgWithBt(String message)
	{
		ShowAlertDiaMsg(activity, message, NORMAL_DIALOG_DURATION, true);
	}
	
	public static void ShowAlertDiaMsgWithBt(String message, int seconds)
	{
		ShowAlertDiaMsg(activity, message, seconds, true);
	}
	
	public static void ShowAlertDiaMsg(Context context, String message, int seconds)
	{
		ShowAlertDiaMsg(context, message, seconds, false);
	}
	
	public static void ShowAlertDiaMsg(Context context, String message)
	{
		ShowAlertDiaMsg(context, message, NORMAL_DIALOG_DURATION, false);
	}
	
	public static void ShowAlertDiaMsg(String message)
	{
		ShowAlertDiaMsg(activity, message, NORMAL_DIALOG_DURATION, false);
	}
	
	public static void ShowAlertDiaMsg(String message, int seconds)
	{
		ShowAlertDiaMsg(activity, message, seconds, false);
	}
	
	private static void ShowAlertDiaMsg(Context context, String message, int seconds, boolean withButton)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		
		if(withButton)
			builder.setPositiveButton(Statics.getDefaultResources().getString(R.string.common_ok), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
					arg0.dismiss();
				}});
		
		final AlertDialog d = builder.create();
		d.show();
		
		// Hide after some seconds
		final Handler handler  = new Handler();
		final Runnable runnable = new Runnable() {
		    @Override
		    public void run() {
		        if (d.isShowing()) {
		            d.dismiss();
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

	public static PreferencesHelper getPreferences() {
		return Preferences;
	}

	public static void setBASE_WS_URL(String bASE_WS_URL) {
		BASE_WS_URL = bASE_WS_URL;
		try {
			Preferences.EditPreferences(WS_URL_KEY, bASE_WS_URL);
			startPing();
		} catch (Exception e) {
			Statics.ShowAlertDiaMsgWithBt(LocaleResources.getString(R.string.generic_exception_warning) + e.getMessage(), INFINIT_DIALOG_DURATION);
			e.printStackTrace();
		}
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

	
//	public static class LoadFromFile extends TimerTask
//	{
//		@Override
//		public void run() {
//			activity.runOnUiThread(new Runnable() {
//	            @Override
//	            public void run() {
//
//	            }
//			});
//		};
//	}
}
