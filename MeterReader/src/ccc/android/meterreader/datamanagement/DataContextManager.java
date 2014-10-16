package ccc.android.meterreader.datamanagement;

import java.io.*;
import java.net.*;
import java.util.*;

import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.listtypes.GaugeDeviceList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.listtypes.ImageList;
import ccc.android.meterdata.listtypes.ReadingList;
import ccc.android.meterdata.listtypes.RouteList;
import ccc.android.meterdata.listtypes.StationList;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterdata.types.User;
import ccc.android.meterreader.R;
import ccc.android.meterreader.actions.ActionFactory;
import ccc.android.meterreader.actions.IMeterReaderAction;
import ccc.android.meterreader.actions.NewGaugeDevice;
import ccc.android.meterreader.actions.NewReading;
import ccc.android.meterreader.datamanagement.async.AsyncSessionSynchronizer;
import ccc.android.meterreader.datamanagement.async.DataContextAsyncFileLoader;
import ccc.android.meterreader.datamanagement.async.GenericMemberListAsyncDbLoader;
import ccc.android.meterreader.datamanagement.async.GenericMemberListAsyncFileLoader;
import ccc.android.meterreader.exceptions.UserNotInitializedException;
import ccc.android.meterreader.internaldata.ICallBack;
import ccc.android.meterreader.internaldata.InternalGaugeDevice;
import ccc.android.meterreader.internaldata.InternalGaugeDeviceDigitList;
import ccc.android.meterreader.internaldata.Session;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.JsonStatics;
import ccc.java.restclient.RestClient;

public class DataContextManager implements ICallBack
{
	private DataContext data;
	private Stack<IMeterReaderAction> unDoStack = new Stack<IMeterReaderAction>();
	private Stack<IMeterReaderAction> reDoStack = new Stack<IMeterReaderAction>();
	private boolean areStacksBlocked = false;
		
	public DataContextManager(IDataContextEventListener listener)
	{
		this.data = new DataContext();
		this.data.AddContextEventListener(listener);
		try
		{
			this.data.NewSession();
		}
		catch (UserNotInitializedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.data.NewSessionInitialized();
	}

	public DataContext getData() 
	{
		return data;
	}
	
	public void UploadToDataBase() {

		data.SessionIsSynchronizing();
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
	
	public void LoadContextFromFile()
	{
    	LoadContextFromFile(Statics.LAST_SESSION, true);
		this.actionStackLoad(Statics.UNDOSTACK);
		applyUnDoStackToSession();
	}
	
	public void LoadContextFromDb(boolean withImageData)
	{
		if(!withImageData)  //not!!
		{
			data.setWaitForPatterns(1);
			this.loadDigitsFromFile(Statics.DIGIT_FILE);
		}
		this.loadContextFromDb(withImageData);
	}
	
	public void SaveContext()
	{
		this.SaveContextToFile(Statics.LAST_SESSION, true);
		this.SaveDigitsToFile(Statics.DIGIT_FILE);
		this.actionStackLoad(Statics.UNDOSTACK);
		applyUnDoStackToSession();
	}
	
	public void WriteEmptyFile(String fileName)
	{
		String[] dataLines = new String[1];
		dataLines[0] = "";
		try {
			Statics.WriteRawFile(fileName, false, dataLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public boolean SaveContextToFile(String fileName, boolean context)
	{
		String[] dataLines = new String[2];
		if(context)
			dataLines[1] = JsonStatics.JsonTimestamp(data); 
		else
			dataLines[1] = JsonStatics.JsonTimestamp(data.getSession());
		dataLines[0] = "true";

		try {
			Statics.WriteRawFile(fileName, false, dataLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	
	public boolean SaveDigitsToFile(String fileName)
	{
		String[] dataLines = new String[2];
		dataLines[1] = JsonStatics.JsonTimestamp(this.data.getDigitlist()); 
		dataLines[0] = "true";

		try {
			Statics.WriteRawFile(fileName, false, dataLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	
	private void loadDigitsFromFile(String fileName)
	{
		data.getDigitlist().clear();
		GenericMemberListAsyncFileLoader gml = new GenericMemberListAsyncFileLoader();
		gml.execute(new Object[] { fileName, InternalGaugeDeviceDigitList.class, new InternalGaugeDeviceDigitList(this.data)});
	}
	
	public void LoadContextFromFile(String file, boolean context) 
	{
		data.SessionIsSynchronizing();
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
					DataContextAsyncFileLoader dcafl = new DataContextAsyncFileLoader();
					dcafl.execute(json, this);
				}
				else
				{
					//TODO async!
					loadedSes = Session.class.cast(new RestClient().getMapper().readValue(json, Session.class));
					data.getDevices().getGaugeDeviceList().addAll(loadedSes.getNewDevices().getGaugeDeviceList());
					data.getGauges().getGaugeList().addAll(loadedSes.getNewGauges().getGaugeList());
					data.getReadings().getReadingList().addAll(loadedSes.getReadings().getReadingList());
					data.getRoutes().getRouteList().addAll(loadedSes.getNewRoutes().getRouteList());
					data.getStations().getStationList().addAll(loadedSes.getNewStations().getStationList());
				}
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void SessionUploaded(Session ses)
	{
		clearStacks();
		this.data.SessionSynchronized(ses);
	}
	
	//TODO remove
	public void clearStacks()
	{
		this.unDoStack.clear();
		WriteEmptyFile(Statics.UNDOSTACK);
		this.reDoStack.clear();
		WriteEmptyFile(Statics.REDOSTACK);
	}
	
	private boolean loadContextFromDb(boolean withImageData) 
	{
		this.data.invalidateData();
		data.SessionIsSynchronizing();
		URL wsURL = getWsUrl();
			try {
				GenericMemberListAsyncDbLoader gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, GaugeDeviceList.class, data.getDevices() });
				gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, StationList.class, data.getStations() });
				gml = new GenericMemberListAsyncDbLoader();

				gml.execute(new Object[] { wsURL, ReadingList.class, data.getReadings(), "GetReadingListFrom", "utcFrom", Statics.getUtcTime()});
				gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, RouteList.class, data.getRoutes(), "GetRouteList"});
				gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, GaugeList.class, data.getGauges()});
				if (withImageData)
				{
					data.isFullUpdate = true;

					gml = new GenericMemberListAsyncDbLoader();
					gml.execute(new Object[] { wsURL, ImageList.class, data.getImagelist(), "GetIconList"});
					
					for (int i = 1; i <= Statics.NUM_OF_SETS_OF_PATTERNS; i++)
					{
						gml = new GenericMemberListAsyncDbLoader();
						gml.execute(new Object[] { wsURL, GaugeDeviceDigitList.class, data.getDigitlist(), "GetGaugeDeviceDigitList", "set", i });
					}
				}
			} catch (Exception e) {
				return false;
			}
			return true;
	}
	
	public void AddNewDevice(GaugeDevice device)
	{
		InternalGaugeDevice d = new InternalGaugeDevice(device);
		//TODO new Action!!!
		data.getDevices().getInternalGaugeDeviceList().add(d);
		data.getSession().AddNewDevice(d);
		
		unDoStackAdd(new NewGaugeDevice(d));
		this.data.SessionUnsynchronized();
	}
	
    public void AddNewRead (Reading read) 
    {      	
		if(data.getSession() != null)
			read.setSessionId(data.getSession().getSessionId());
		if(read.getStationId() == null && data.getStations().getByGaugeId(read.getGaugeId()) != null)
			read.setStationId(data.getStations().getByGaugeId(read.getGaugeId()).getRouteStationId());
		if(read.getUtcFrom() == null)
			read.setUtcFrom(data.getGauges().getById(read.getGaugeId()).getGaugeDevice().getUtcInstallation());
		
		data.getReadings().getReadingList().add(read);
		data.getSession().InsertNewReading(read);
		
		unDoStackAdd(new NewReading(read));
		this.data.SessionUnsynchronized();
    }
    
    protected void unDoStackAdd(IMeterReaderAction action)
    {
    	if(areStacksBlocked)
    		return;
    	this.unDoStack.push(action);
    	String json = JsonStatics.JsonTimestamp(action);
    	try
		{
			Statics.WriteRawFile(Statics.UNDOSTACK, true, json);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public IMeterReaderAction unDoStackPeek()
    {
    	return this.unDoStack.peek();
    }
    
    protected IMeterReaderAction unDoStackPop()
    {
    	Statics.RemoveLastLineOfFile(Statics.UNDOSTACK);
    	return this.unDoStack.pop();
    }
    
    public int unDoStackSize()
    {
    	return this.unDoStack.size();
    }
    
    public void actionStackLoad(String stackFile)
    {
    	BufferedReader reader = null;
    	try
		{
			 reader = Statics.ReadFile(stackFile);
			 String line = null;
			 try
			{
				while((line = reader.readLine()) != null)
				 {
					IMeterReaderAction act = ActionFactory.CreateAction(line);
					if(act != null && stackFile.equals(Statics.UNDOSTACK))
						this.unDoStack.push(act);
					else if(act != null && stackFile.equals(Statics.REDOSTACK));
						this.reDoStack.push(act);
				 }
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void applyUnDoStackToSession()
    {
    	areStacksBlocked = true;
    	List<IMeterReaderAction> acts = new ArrayList<IMeterReaderAction>(unDoStack);
    	for(int i =0; i < acts.size();i++)
    	{
    		if(acts.get(i) instanceof NewReading)
    		{
    			NewReading r = (NewReading)acts.get(i);
    			this.AddNewRead(r.getRead());
    		}
    		else if(acts.get(i) instanceof NewGaugeDevice)
    		{
    			NewGaugeDevice r = (NewGaugeDevice)acts.get(i);
    			this.AddNewDevice(r.getDevice());
    		}
    	}
    	areStacksBlocked = false;
    }

	@Override
	public void DataContextLoadedCallback(DataContext context)
	{
		if(context != null)
		{
			for(IDataContextEventListener cont : this.data.listenerList)
				context.AddContextEventListener(cont);
			this.data = context;
			data.fillGaugeLibrary();
			data.setCompletelyLoaded(true);
			data.SessionFileSynchronized(data.getSession());
		}
		else
			this.data.SessionFailedSynchronization("");
	}

	@Override
	public void SessionSynchronizedCallback(Session ses)
	{
		if(ses != null)
		{
			ses.clearAllData();

			SessionUploaded(ses);
		}
		else
			this.data.SessionFailedSynchronization(Statics.getDefaultResources().getString(R.string.main_sync_faild_upload));
	}

	@Override
	public void UserLoggedInCallback(User user)
	{
		this.data.setCurrentUser(user);
	}

//	public Stack<IMeterReaderAction> getUnDoStack() {
//		return unDoStack;
//	}
//
//	public Stack<IMeterReaderAction> getReDoStack() {
//		return reDoStack;
//	}

}
