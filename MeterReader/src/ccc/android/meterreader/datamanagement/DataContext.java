package ccc.android.meterreader.datamanagement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.GaugeDeviceDigit;
import ccc.android.meterdata.types.Route;
import ccc.android.meterdata.types.User;
import ccc.android.meterreader.R;
import ccc.android.meterreader.exceptions.UserNotInitializedException;
import ccc.android.meterreader.internaldata.ICallbackList;
import ccc.android.meterreader.internaldata.IMeterDataContainer;
import ccc.android.meterreader.internaldata.InternalGaugeDevice;
import ccc.android.meterreader.internaldata.InternalGaugeDeviceDigitList;
import ccc.android.meterreader.internaldata.InternalGaugeDeviceList;
import ccc.android.meterreader.internaldata.InternalGaugeList;
import ccc.android.meterreader.internaldata.InternalImageList;
import ccc.android.meterreader.internaldata.InternalPreferenceList;
import ccc.android.meterreader.internaldata.InternalReadingList;
import ccc.android.meterreader.internaldata.InternalRouteList;
import ccc.android.meterreader.internaldata.InternalStationList;
import ccc.android.meterreader.internaldata.Session;
import ccc.android.meterreader.statics.StaticGaugeLibrary;
import ccc.android.meterreader.statics.StaticIconLibrary;
import ccc.android.meterreader.statics.StaticImageLibrary;
import ccc.android.meterreader.statics.StaticPreferences;
import ccc.android.meterreader.statics.Statics;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataContext implements IMeterDataContainer
{
	Timer SyncTimeout;
	private InternalGaugeDeviceList gaugedevicelist = new InternalGaugeDeviceList(this);
	private InternalStationList stationlist = new InternalStationList(this);
	private InternalReadingList readinglist = new InternalReadingList(this);
	private InternalRouteList routelist = new InternalRouteList(this);
	private InternalGaugeList gaugelist = new InternalGaugeList(this);
	private InternalPreferenceList preferences = new InternalPreferenceList(this);
	private List<ICallbackList> containers = new ArrayList<ICallbackList>();
	//private InternalGaugeDeviceDigitList digitlist = new InternalGaugeDeviceDigitList(this);
	//private List<GaugeDeviceDigit> digits = Collections.synchronizedList(new ArrayList<GaugeDeviceDigit>());

	private Session session;
	private User currentUser;

	private int waitForPatterns;// = StaticPreferences.getPreference("NUM_OF_PATTERN_SETS", Integer.class);
	protected List<IDataContextEventListener> listenerList = new ArrayList<IDataContextEventListener>();
	
	public DataContext ()
	{
		//TODO user stuff
		currentUser = new User(1);
		containers.add(gaugedevicelist.setDataContainer(this));
		//containers.add(digitlist.setDataContainer(this));
		containers.add(gaugelist.setDataContainer(this));
		containers.add(routelist.setDataContainer(this));
		containers.add(readinglist.setDataContainer(this));
		containers.add(stationlist.setDataContainer(this));
		containers.add(preferences.setDataContainer(this));
	}	
	
	public void NewSession(Route route)  throws UserNotInitializedException
	{
		Session ses = new Session();
		ses.setStartDate(Statics.getUtcTime());
		ses.setRoute(route);
		ses.setUserId(currentUser.getUserId());
		this.session = ses;
		this.NewSessionInitialized();
	}
	
	public void NewSession() throws UserNotInitializedException
	{
		Session ses = new Session();
		ses.setStartDate(Statics.getUtcTime());
		ses.setUserId(currentUser.getUserId());
		this.session = ses;
		this.NewSessionInitialized();
	}
	
	@JsonIgnore
	private void setListDynamically(IGenericMemberList list)
	{
		Class<?> c = this.getClass();
		try{
			Field field = c.getField(c.getClass().getSimpleName().toLowerCase());
			ICallbackList internalList = (ICallbackList) (field.get(this));
			field = internalList.getClass().getField(c.getClass().getSimpleName());
			field.set(internalList, list);
			field = internalList.getClass().getField("isLoaded");
			field.setBoolean(internalList, true);
		}
		catch(Exception e){
			return;
		}	
	}

	@Override
	public void ReceiveErrorObject(RestError error) {
		this.SessionFailedSynchronization(error.getErrorMsg());
	}
	
	
	@Override
	public void RegisterLoadedDataObject(ccc.android.meterreader.internaldata.ICallbackList data) 
	{		
		if(data.getClass() == InternalGaugeDeviceList.class)
		{
			this.gaugedevicelist = (InternalGaugeDeviceList)data;
			fillGaugeLibrary();
		}
		if(data.getClass() == InternalStationList.class)
		{
			this.stationlist = (InternalStationList)data;
		}
		if(data.getClass() == InternalReadingList.class)
		{
			this.readinglist = (InternalReadingList)data;
		}
		if(data.getClass() == InternalRouteList.class)
		{
			this.routelist = (InternalRouteList)data;
		}
		if(data.getClass() == InternalGaugeList.class)
		{
			this.gaugelist = (InternalGaugeList)data;
		}
		if(data.getClass() == InternalPreferenceList.class)
		{
			this.preferences = (InternalPreferenceList)data;
			StaticPreferences.setPreferences(this.preferences);
		}
		if(data.getClass() == InternalImageList.class)
		{
			InternalImageList list = (InternalImageList)data;
			if(list.getImageType() == 1)
			{
				StaticImageLibrary.setImages(list);
			}
			else if(list.getImageType() == 0)
			{
				StaticIconLibrary.setIcons(list);
			}
		}
		if(data.getClass() == InternalGaugeDeviceDigitList.class)
		{
			GaugeDevice de = new GaugeDevice();
			de.setGaugeDeviceId(0);
			de.setDigitPatterns((InternalGaugeDeviceDigitList)data);
			insertPatterns(de);
//			synchronized(digits)
//			{
//				//TODO add expected list count! InternalGaugeDeviceDigitList 
//				digits.addAll(((InternalGaugeDeviceDigitList)data).getDigitList());
//				if(--waitForPatterns == 0)  //received all sets
//				{
//					this.getDigitlist().getDigitList().addAll(digits);
// 					this.getDigitlist().setIsLoaded(true);
//					
//					//TODO save other patterns!!!
//					//save standard digit patterns
//	
//
//					digitlist.clear();
//					digits.clear();
//				}
//			}
		}
		
		if(!isCompletelyLoaded()) //not!
				return;
		
		SessionSynchronized(this.session);
	}

	private void insertPatterns(GaugeDevice gaugeDevice) {
		if(gaugeDevice.getGaugeDeviceId() < 1)
			this.getDevices().getInternalGaugeDeviceList().add(new InternalGaugeDevice(gaugeDevice));
		StaticGaugeLibrary.getGauges().put(gaugeDevice.getGaugeDeviceId(), new InternalGaugeDevice(gaugeDevice));
		//TODO get this right!
		waitForPatterns = StaticPreferences.getPreference(Statics.NUM_OF_PATTERN_SETS, Statics.NUM_OF_PATTERN_SETS_DEFAULT);
	}

	void fillGaugeLibrary() {
		StaticGaugeLibrary.getGauges().clear();
		for(GaugeDevice device : this.getDevices().getGaugeDeviceList().size() == 0 ? this.getDevices().getInternalGaugeDeviceList() : this.getDevices().getGaugeDeviceList())
		{
			InternalGaugeDevice de = new InternalGaugeDevice(device);
			StaticGaugeLibrary.getGauges().put(device.getGaugeDeviceId(), de);
		}
	}

	public void AddContextEventListener(IDataContextEventListener listener)
	{
		listenerList.add(listener);
	}
	
	public void RemoveContextEventListener(IDataContextEventListener listener)
	{
		listenerList.remove(listener);
	}
	
	//Context Events
	void SessionUnsynchronized()
	{
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionUnsynchronized();
	}
	void NewSessionInitialized()
	{
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionInitialized();
	}
	void SessionSynchronized(Session session)
	{
		SyncTimeout.cancel();
		if(Statics.getWl().isHeld())
			Statics.getWl().release();
		this.setSession(session);
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionSynchronized(this.getSession());
	}
	void SessionFileSynchronized(Session session)
	{
		if(Statics.getWl().isHeld())
			Statics.getWl().release();
		this.setSession(session);
		for(IDataContextEventListener listener : listenerList)
			listener.OnFileSynchronization(this.getSession());
	}
	void SessionIsSynchronizing()
	{
		SyncTimeout = new Timer();
		SyncTimeout.schedule(new Statics.SyncTimeoutTask(), StaticPreferences.getPreference(Statics.SYNC_TIMEOUT_DELAY_MS, Statics.SYNC_TIMEOUT_DELAY_MS_DEFAULT));
		Statics.getWl().acquire();
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionIsSynchronizing(this.getSession());
	}
	void SessionFailedSynchronization(String msg)
	{
		SyncTimeout.cancel();
		if(Statics.getWl().isHeld())
			Statics.getWl().release();
		for(IDataContextEventListener listener : listenerList)
			listener.OnFailedSessionSynchronization(msg);
	}
	
	public boolean isCompletelyLoaded() {
		for(ICallbackList l : this.containers)
		{
			if(!l.isLoaded()) //!not
				return false;
		}
		return true;
	}
	

	void invalidateData() 
	{
		//digits.clear();
		for(ICallbackList l : this.containers)
		{
			l.setIsLoaded(false);
		}
	}
	
	void validateData() 
	{
		for(ICallbackList l : this.containers)
		{
			l.setIsLoaded(true);
		}
	}

	public User getCurrentUser() {
		return currentUser;
	}
	@JsonProperty("Gaugelist")
	public InternalGaugeList getGauges() {
		return gaugelist;
	}
	@JsonProperty("Gaugelist")
	public void setGauges(InternalGaugeList gauges) {
		this.gaugelist = gauges;
	}
	@JsonProperty("Gaugedevicelist")
	public InternalGaugeDeviceList getDevices() {
		return gaugedevicelist;
	}
	@JsonProperty("Gaugedevicelist")
	public void setDevices(InternalGaugeDeviceList devices) {
		gaugedevicelist =  devices;
	}
	@JsonProperty("Stationlist")
	public InternalStationList getStations() {
		return stationlist;
	}
	@JsonProperty("Stationlist")
	public void setStations(InternalStationList stations) {
		stationlist =  stations;
	}
	@JsonProperty("Readinglist")
	public InternalReadingList getReadings() {
		return readinglist;
	}
	@JsonProperty("Readinglist")
	public void setReadings(InternalReadingList readings) {
		readinglist = readings;
	}

	@JsonProperty("RouteList")
	public InternalRouteList getRoutes() {
		return routelist;
	}
	@JsonProperty("RouteList")
	public void setRoutes(InternalRouteList routes) {
		routelist = routes;
	}
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}
	@JsonIgnore
	public int getWaitForPatterns()
	{
		return waitForPatterns;
	}

	@JsonIgnore
	public void setWaitForPatterns(int waitForPatterns)
	{
		this.waitForPatterns = waitForPatterns;
	}

	public InternalPreferenceList getPreferences()
	{
		return preferences;
	}

	public void setPreferences(InternalPreferenceList preferences)
	{
		this.preferences = preferences;
	}
}
