package ccc.android.meterreader.statics;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
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
import ccc.android.meterreader.R.string;
import ccc.android.meterreader.datamanagement.PreferencesHelper;
import ccc.android.meterreader.datamanagement.async.AsyncPingBack;
import ccc.java.restclient.RestClient;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Camera.Size;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Statics 
{
	//DEBUG
	public static boolean debugMode = true;
	//ENDDEBUG
	private static PowerManager.WakeLock wl;
	private static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	private static Date lastUtcTime;
	private static long utcDiff;
	private static boolean isOnline = false;
	private static Timer pingTimer;
	private static Timer upSyncTimer;
	private static Timer downSyncTimer;
	private static int isOnlineTimerDelay = 1000;
	private static int offLineTimerDelay = 10000;
	private static int uploadSyncTimerDelay = 300000; 	//5 min
	private static int downloadSyncTimerDelay = 3600000;//1 h
	private static int userInactivityInMinutes = 1;
	private static MainActivity activity;
    private static boolean wasOnline = false;
    private static Typeface sansPro; 
    private static Resources LocaleResources;
    private static java.text.DateFormat localeDateFormat;
    //used for constants editable by user
    private static PreferencesHelper Preferences;
    
    private static Date lastInteraction;

	public static String BASE_WS_URL = null;
	public static final String WS_URL_KEY = "wsBaseUrl";
	public static final String MAIN_SESSION = "MainSession.txt";
	public static final String LAST_SESSION = "LastSession.txt";
	public static final String DEBUG_SESSION = "DebugSession.txt";
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
	public static final int NUM_OF_SETS_OF_PATTERNS = 10;
	
	public static int DISPLAY_HEIGHT;
	public static int DISPLAY_WIDTH;
	
	//TODO replace with gauge specific threshold values
	public static final int DAYSTORED = 3;
	public static final int DAYSTOYEL = 1;
	
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

		upSyncTimer.schedule(new UpSyncTask(), uploadSyncTimerDelay);
		
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
			Statics.ShowToast(LocaleResources.getString(R.string.no_base_ws_url_warning));
		
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
				if(activity.getManager().getUnDoStack().size() > 0 
						&& (lastInteraction == null || Statics.getDateDiff(lastInteraction, new Date(), TimeUnit.MINUTES) > userInactivityInMinutes))
				{
					activity.runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
							activity.getManager().UploadToDataBase();
			            }
			        });
				}
				upSyncTimer.schedule(new UpSyncTask(), uploadSyncTimerDelay);
			}
			else
				upSyncTimer.schedule(new UpSyncTask(), offLineTimerDelay);
	    }
	}
	
	private static class DownSyncTask extends TimerTask
	{
		@Override
		public void run() {
			downSyncTimer = new Timer();
			if(isOnline)
			{
				if(lastInteraction == null || Statics.getDateDiff(lastInteraction, new Date(), TimeUnit.MINUTES) > userInactivityInMinutes)
				{
					activity.runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
					        activity.getManager().LoadContextFromDb();
			            }
			        });
				}
				downSyncTimer.schedule(new DownSyncTask(), downloadSyncTimerDelay);
			}
			else
				downSyncTimer.schedule(new DownSyncTask(), offLineTimerDelay);
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
		upSyncTimer.schedule(new UpSyncTask(), offLineTimerDelay);
		downSyncTimer = new Timer();
		downSyncTimer.schedule(new DownSyncTask(), offLineTimerDelay);
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
		pingTimer.schedule(pingInTime, isOnlineTimerDelay, isOnlineTimerDelay);
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
			isOnline =  true;
		else
		{
			isOnline = false;
			if(res.getError() != null)
			{
				stopPing();
				ShowToast(res.getError());
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
			if(isOnline)
				downSyncTimer.schedule(new DownSyncTask(), 10);
		}
		wasOnline = isOnline;
	}
	
	public static void WriteRawFile(String fileName, List<String> records) throws IOException {

	    FileOutputStream fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
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
		notPlausible.setGravity(Gravity.TOP, 0, 300);
		notPlausible.show();
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
			Statics.ShowToast(LocaleResources.getString(R.string.generic_exception_warning) + e.getMessage());
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
