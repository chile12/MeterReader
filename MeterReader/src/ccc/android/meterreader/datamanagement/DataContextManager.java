package ccc.android.meterreader.datamanagement;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.listtypes.GaugeDeviceList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.listtypes.ReadingList;
import ccc.android.meterdata.listtypes.RouteList;
import ccc.android.meterdata.listtypes.StationList;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterdata.types.Session;
import ccc.android.meterdata.types.User;
import ccc.android.meterreader.R;
import ccc.android.meterreader.actions.IMeterReaderAction;
import ccc.android.meterreader.actions.NewReading;
import ccc.android.meterreader.datamanagement.async.AsyncSessionSynchronizer;
import ccc.android.meterreader.datamanagement.async.GenericMemberListAsyncDbLoader;
import ccc.android.meterreader.internaldata.ICallBack;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.JsonStatics;
import ccc.java.restclient.RestClient;

public class DataContextManager implements ICallBack
{
	private DataContext data;
	private Stack<IMeterReaderAction> unDoStack = new Stack<IMeterReaderAction>();
	private Stack<IMeterReaderAction> reDoStack = new Stack<IMeterReaderAction>();
		
	public DataContextManager(IDataContextEventListener context)
	{
		this.data = new DataContext();
		this.data.AddContextEventListener(context);
		this.data.NewSessionInitialized();
		this.data.invalidateData();
	}

	public DataContext getData() 
	{
		return data;
	}
	
	public void UploadToDataBase() {

		data.OnSessionIsSynchronizing();
		AsyncSessionSynchronizer readingSync = new AsyncSessionSynchronizer();
		readingSync.execute(new Object[]{data.getSession(), this});
		Statics.downSyncNow();
	}

	private URL getWsUrl() {
		URL wsURL = null;
		String url = Statics.getPreferences().GetPreferences(Statics.WS_URL_KEY, String.class);		
		try 
		{
			wsURL = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return wsURL;
	}
	
	public void LoadContext()
	{
    	if(!Statics.debugMode)
    	{
    		data.OnSessionIsSynchronizing();
    		LoadContextFromFile(Statics.LAST_SESSION, false);
    	}
    	else
    	{
    		LoadContextFromFile(Statics.MAIN_SESSION, true);
    	}
	}
	
	public void SaveEmptySession(String fileName)
	{
		List<String> dataLines = new ArrayList<String>();
		dataLines.add("false");
		try {
			Statics.WriteRawFile(fileName, dataLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public boolean SaveContextToFile(String fileName, boolean context)
	{
		List<String> dataLines = new ArrayList<String>();
		String zw = null;
		if(context)
			zw = JsonStatics.JsonTimestamp(data);
		else
			zw = JsonStatics.JsonTimestamp(data.getSession());
		dataLines.add("true");
		dataLines.add(zw);

		try {
			Statics.WriteRawFile(fileName, dataLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	
	public void LoadContextFromFile(String file, boolean context) 
	{
		data.OnSessionIsSynchronizing();
 		if(file == null)
			file = Statics.MAIN_SESSION;
		loadContextFromFile(file, context);
	}
	
	private void loadContextFromFile(String path, boolean context)
	{
		this.data.invalidateData();
		BufferedReader br = null;
		String json = null;
		Session loadedSes = null;


		try {
			try {
				br = Statics.ReadFile(path);
				json = br.readLine();
				if(json.equals("true"))
					json = br.readLine();
				else
					json = null;
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(json != null)
			{
				if(context)
				{
					DataContext ses = DataContext.class.cast(new RestClient().getMapper().readValue(json, DataContext.class));
					data.invalidateData();
					data.RegisterLoadedDataObject(ses.getDevices());
					data.RegisterLoadedDataObject(ses.getDigitlist());
					data.RegisterLoadedDataObject(ses.getGauges());
					data.RegisterLoadedDataObject(ses.getReadings());
					data.RegisterLoadedDataObject(ses.getRoutes());
					data.RegisterLoadedDataObject(ses.getStations());
					data.fillGaugeLibrary();
					br.close();
				}
				else
				{
					loadedSes = Session.class.cast(new RestClient().getMapper().readValue(json, Session.class));
					data.getDevices().getGaugeDeviceList().addAll(loadedSes.getNewDevices().getGaugeDeviceList());
					data.getGauges().getGaugeList().addAll(loadedSes.getNewGauges().getGaugeList());
					data.getReadings().getReadingList().addAll(loadedSes.getReadings().getReadingList());
					data.getRoutes().getRouteList().addAll(loadedSes.getNewRoutes().getRouteList());
					data.getStations().getStationList().addAll(loadedSes.getNewStations().getStationList());
					br.close();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if(loadedSes == null)
				loadedSes = data.getSession();
			data.setCompletelyLoaded(true);
			data.SessionLoaded(loadedSes);
		}
	}
	

	@Override
	public void callback(Object callbackContext) 
	{
		if(callbackContext.getClass() == DataContext.class)		//DataContext loaded (used for file loading)
		{
			DataContext temp = (DataContext)callbackContext;
			for(IDataContextEventListener context : this.data.listenerList)
				temp.AddContextEventListener(context);
			this.data = temp;
			this.data.SessionUploaded(((DataContext)callbackContext).getSession());
			//TODO check position
			unDoStack.clear();
			reDoStack.clear();
		}
		if(callbackContext.getClass() == User.class)			//user logged in
		{
			this.data.setCurrentUser((User)callbackContext);
		}
		if(callbackContext.getClass() == SimpleEntry.class)		//session synchronized
		{
			if(((Entry<Session, Boolean>) callbackContext).getValue())
			{
				Session ses = ((Entry<Session, Boolean>)callbackContext).getKey();
				ses.clearAllData();

				this.data.SessionUploaded(ses);
			}
			else
				this.data.SessionFailedSynchronization(Statics.getDefaultResources().getString(R.string.main_sync_faild_upload));
		}
	}
	
	public boolean LoadContextFromDb() 
	{
		this.data.invalidateData();
		data.OnSessionIsSynchronizing();
		URL wsURL = getWsUrl();
			try {
				GenericMemberListAsyncDbLoader gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, GaugeDeviceList.class, data.getDevices() });
				gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, StationList.class, data.getStations() });
				gml = new GenericMemberListAsyncDbLoader();
				//TODO
		        Calendar cal = Calendar.getInstance();
		    	cal.set(2013, Calendar.SEPTEMBER, 1);
				gml.execute(new Object[] { wsURL, ReadingList.class, data.getReadings(), "GetReadingListFrom", "utcFrom", cal.getTime()});
				gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, RouteList.class, data.getRoutes()});
				gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, GaugeList.class, data.getGauges()});
				for(int i =1; i <=Statics.NUM_OF_SETS_OF_PATTERNS; i++)
				{
					gml = new GenericMemberListAsyncDbLoader();
					gml.execute(new Object[] { wsURL, GaugeDeviceDigitList.class, data.getDigitlist(), "GetGaugeDeviceDigitList", "set", i});
				}
			} catch (Exception e) {
				return false;
			}
			return true;
	}
	
    public void InsertNewRead (Reading read) 
    {      	
		if(data.getSession() != null)
			read.setSessionId(data.getSession().getSessionId());
		if(read.getStationId() == null && data.getStations().getByGaugeId(read.getGaugeId()) != null)
			read.setStationId(data.getStations().getByGaugeId(read.getGaugeId()).getRouteStationId());
		if(read.getUtcFrom() == null)
			read.setUtcFrom(data.getGauges().getById(read.getGaugeId()).getGaugeDevice().getUtcInstallation());

		Gauge ga= data.getGauges().getById(read.getGaugeId());  
		this.unDoStack.push(new NewReading(ga, read));
		
		data.getReadings().getReadingList().add(read);
		data.getSession().InsertNewReading(read);
		this.data.DataContextUpdated();
    }

	public Stack<IMeterReaderAction> getUnDoStack() {
		return unDoStack;
	}

	public Stack<IMeterReaderAction> getReDoStack() {
		return reDoStack;
	}

}
