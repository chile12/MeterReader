package ccc.android.meterreader.datamanagement;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.Timer;

import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.listtypes.GaugeDeviceList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.listtypes.ImageList;
import ccc.android.meterdata.listtypes.PreferenceList;
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
import ccc.android.meterreader.datamanagement.async.GenericPartialListAsyncDbLoader;
import ccc.android.meterreader.exceptions.UserNotInitializedException;
import ccc.android.meterreader.internaldata.ICallBack;
import ccc.android.meterreader.internaldata.InternalGaugeDevice;
import ccc.android.meterreader.internaldata.InternalGaugeDeviceDigitList;
import ccc.android.meterreader.internaldata.InternalImageList;
import ccc.android.meterreader.internaldata.Session;
import ccc.android.meterreader.statics.StaticGaugeLibrary;
import ccc.android.meterreader.statics.StaticIconLibrary;
import ccc.android.meterreader.statics.StaticImageLibrary;
import ccc.android.meterreader.statics.StaticPreferences;
import ccc.android.meterreader.statics.Statics;
import ccc.android.meterreader.statics.Statics.SyncMode;
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
		//get current Preferences
		data.getSession().setPreferences(StaticPreferences.getPreferences());
		readingSync.execute(new Object[]{data.getSession(), this});
		Statics.syncNow();
	}

	private URL getWsUrl() {
		URL wsURL = null;
		String url = StaticPreferences.getPreference(Statics.WS_URL, null);		
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
    	LoadContextFromFile(StaticPreferences.getPreference(Statics.LAST_SESSION, Statics.LAST_SESSION_DEFAULT), true);
	}
	
	public void LoadContextFromFile(String file, boolean context) 
	{
		data.SessionIsSynchronizing();
//		this.actionStackLoad(StaticPreferences.getPreference(Statics.UNDO_STACK_FILE, Statics.UNDO_STACK_FILE_DEFAULT));
//		applyUnDoStackToSession();
 		if(file == null)
			file = StaticPreferences.getPreference(Statics.MAIN_SESSION, Statics.MAIN_SESSION_DEFAULT);
		loadContextFromFile(file, context);
		data.setWaitForPatterns(1);
		loadListFromFile(StaticPreferences.getPreference(Statics.DIGIT_FILE, Statics.DIGIT_FILE_DEFAULT));
		loadListFromFile(StaticPreferences.getPreference(Statics.IMAGE_FILE, Statics.IMAGE_FILE_DEFAULT));
		loadListFromFile(StaticPreferences.getPreference(Statics.ICON_FILE, Statics.ICON_FILE_DEFAULT));
	}
	
	public void LoadFullContextFromDb()
	{
//		if(Statics.getSyncMode() == SyncMode.Partial)
//		{
//			LoadContextFromDb();
//			return;
//		}
		data.setWaitForPatterns(StaticPreferences.getPreference(Statics.NUM_OF_PATTERN_SETS, Statics.NUM_OF_PATTERN_SETS_DEFAULT));
		this.loadContextFromDb(true);
		Statics.setSyncMode(SyncMode.Partial);
		StaticPreferences.setPreference(Statics.LAST_FULL_DOWN, new Date().getTime());
	}
	
	public void LoadContextFromDb()
	{
			data.setWaitForPatterns(1);
			loadListFromFile(StaticPreferences.getPreference(Statics.DIGIT_FILE, Statics.DIGIT_FILE_DEFAULT));
			loadListFromFile(StaticPreferences.getPreference(Statics.IMAGE_FILE, Statics.IMAGE_FILE_DEFAULT));
			loadListFromFile(StaticPreferences.getPreference(Statics.ICON_FILE, Statics.ICON_FILE_DEFAULT));
		this.loadContextFromDb(false);
	}
	
	public void SaveContext()
	{
		this.SaveContextToFile(StaticPreferences.getPreference(Statics.LAST_SESSION, Statics.LAST_SESSION_DEFAULT), true);
		if(Statics.getSyncMode() == SyncMode.Full)
		{
			this.SaveListToFile(StaticPreferences.getPreference(Statics.DIGIT_FILE, Statics.DIGIT_FILE_DEFAULT));
			this.SaveListToFile(StaticPreferences.getPreference(Statics.IMAGE_FILE, Statics.IMAGE_FILE_DEFAULT));
			this.SaveListToFile(StaticPreferences.getPreference(Statics.ICON_FILE, Statics.ICON_FILE_DEFAULT));
		}
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
	
//	public boolean SaveListToFile(String fileName)
//	{
//		String[] dataLines = new String[2];
//		dataLines[1] = JsonStatics.JsonTimestamp(this.data.getDigitlist()); 
//		dataLines[0] = "true";
//
//		try {
//			Statics.WriteRawFile(fileName, false, dataLines);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			return false;
//		}
//		return true;
//	}
	
	public boolean SaveListToFile(String fileName)
	{
		String[] dataLines = new String[2];
		if(fileName.equals(StaticPreferences.getPreference(Statics.DIGIT_FILE, Statics.DIGIT_FILE_DEFAULT)))
			dataLines[1] = JsonStatics.JsonTimestamp(StaticGaugeLibrary.getGauges()); 
		if(fileName.equals(StaticPreferences.getPreference(Statics.IMAGE_FILE, Statics.IMAGE_FILE_DEFAULT)))
			dataLines[1] = JsonStatics.JsonTimestamp(StaticImageLibrary.getImages()); 
		if(fileName.equals(StaticPreferences.getPreference(Statics.ICON_FILE, Statics.ICON_FILE_DEFAULT)))
			dataLines[1] = JsonStatics.JsonTimestamp(StaticIconLibrary.getIcons()); 
		dataLines[0] = "true";

		try {
			Statics.WriteRawFile(fileName, false, dataLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	
	private void loadListFromFile(String fileName)
	{
		//data.getDigitlist().clear();
		GenericMemberListAsyncFileLoader gml = new GenericMemberListAsyncFileLoader();
		if(fileName.equals(StaticPreferences.getPreference(Statics.DIGIT_FILE, Statics.DIGIT_FILE_DEFAULT)))
			gml.execute(new Object[] { fileName, InternalGaugeDeviceDigitList.class, new InternalGaugeDeviceDigitList(this.data)});
		if(fileName.equals(StaticPreferences.getPreference(Statics.IMAGE_FILE, Statics.IMAGE_FILE_DEFAULT)))
		{
			InternalImageList images = new InternalImageList(this.data);
			images.setIsImageList(true);
			gml.execute(new Object[] { fileName, ImageList.class, images});
		}
		if(fileName.equals(StaticPreferences.getPreference(Statics.ICON_FILE, Statics.ICON_FILE_DEFAULT)))
			gml.execute(new Object[] { fileName, ImageList.class, new InternalImageList(this.data)});
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
			if(e != null)
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
		WriteEmptyFile(StaticPreferences.getPreference(Statics.UNDO_STACK_FILE, Statics.UNDO_STACK_FILE_DEFAULT));
		this.reDoStack.clear();
		WriteEmptyFile(StaticPreferences.getPreference(Statics.REDO_STACK_FILE, Statics.REDO_STACK_FILE_DEFAULT));
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
				gml = new GenericMemberListAsyncDbLoader();
				gml.execute(new Object[] { wsURL, PreferenceList.class, data.getPreferences(), "GetPreferenceList"});
				if (withImageData)
				{
					GenericPartialListAsyncDbLoader pll = new GenericPartialListAsyncDbLoader();
					pll.execute(new Object[] { wsURL, ImageList.class, new InternalImageList(this.data), "GetImageListPartial", "type", 0}); //type = 0 == icons

					pll = new GenericPartialListAsyncDbLoader();
					pll.execute(new Object[] { wsURL, ImageList.class, new InternalImageList(this.data), "GetImageListPartial", "type", 1}); //type = 1 == images
					
					pll = new GenericPartialListAsyncDbLoader();
					pll.execute(new Object[] { wsURL, GaugeDeviceDigitList.class, new InternalGaugeDeviceDigitList(data), "GetGaugeDeviceDigitList" });
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
			read.setUtcFrom(read.getUtcTo());
		
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
			Statics.WriteRawFile(StaticPreferences.getPreference(Statics.UNDO_STACK_FILE, Statics.UNDO_STACK_FILE_DEFAULT), true, json);
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
    	Statics.RemoveLastLineOfFile(StaticPreferences.getPreference(Statics.UNDO_STACK_FILE, Statics.UNDO_STACK_FILE_DEFAULT));
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
			 if(reader == null)
				 return;
			 String line = null;
			 try
			{
				while((line = reader.readLine()) != null)
				 {
					IMeterReaderAction act = ActionFactory.CreateAction(line);
					if(act != null && stackFile.equals(StaticPreferences.getPreference(Statics.UNDO_STACK_FILE, Statics.UNDO_STACK_FILE_DEFAULT)))
						this.unDoStack.push(act);
					else if(act != null && stackFile.equals(StaticPreferences.getPreference(Statics.REDO_STACK_FILE, Statics.REDO_STACK_FILE_DEFAULT)));
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
    
    public void applyUnDoStackToSession()
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
