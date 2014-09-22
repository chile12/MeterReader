package ccc.android.meterreader.datamanagement;

import java.util.*;
import java.lang.reflect.*;

import org.codehaus.jackson.annotate.JsonProperty;

import ccc.android.meterdata.interfaces.ICallbackList;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterreader.internaldata.InternalGaugeDeviceDigitList;
import ccc.android.meterreader.internaldata.InternalGaugeDeviceList;
import ccc.android.meterreader.internaldata.InternalGaugeList;
import ccc.android.meterreader.internaldata.InternalReadingList;
import ccc.android.meterreader.internaldata.InternalRouteList;
import ccc.android.meterreader.internaldata.InternalStationList;
import ccc.android.meterdata.listtypes.GaugeDeviceList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.listtypes.ReadingList;
import ccc.android.meterdata.listtypes.RouteList;
import ccc.android.meterdata.listtypes.StationList;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.GaugeDeviceDigit;
import ccc.android.meterdata.types.Route;
import ccc.android.meterdata.types.ServerError;
import ccc.android.meterdata.types.Session;
import ccc.android.meterdata.types.User;
import ccc.android.meterreader.exceptions.UserNotInitializedException;
import ccc.android.meterreader.internaldata.*;
import ccc.android.meterreader.statics.StaticGaugeLibrary;
import ccc.android.meterreader.statics.Statics;
import ccc.android.meterreader.statics.Statics.SyncState;

public class DataContext implements IMeterDataContainer
{
	private InternalGaugeDeviceList gaugedevicelist = new InternalGaugeDeviceList(this);
	private InternalStationList stationlist = new InternalStationList(this);
	private InternalReadingList readinglist = new InternalReadingList(this);
	private InternalRouteList routelist = new InternalRouteList(this);
	private InternalGaugeList gaugelist = new InternalGaugeList(this);
	//TODO remove InternalGaugeDeviceDigitList
	private InternalGaugeDeviceDigitList digitlist = new InternalGaugeDeviceDigitList(this);
	private List<GaugeDeviceDigit> digitList = Collections.synchronizedList(new ArrayList<GaugeDeviceDigit>());

	private Session session;
	private User currentUser;

	private int waitForPatterns = Statics.NUM_OF_SETS_OF_PATTERNS;
	private static int syncState = 2;
	private boolean isCompletelyLoaded = false;
	private Map<ICallbackList, Boolean> loadedMetadata = new HashMap<ICallbackList, Boolean>();
	protected List<IDataContextEventListener> listenerList = new ArrayList<IDataContextEventListener>();
	
	public DataContext ()
	{
		//TODO user stuff
		currentUser = new User(1);
	}	
	
	public DataContext (GaugeDeviceList gdl, GaugeList gl, ReadingList rl, RouteList rol, StationList sl)
	{
		setListDynamically(gdl);
		setListDynamically(gl);
		setListDynamically(rl);
		setListDynamically(sl);
		setListDynamically(rol);
		session = new Session();
	}

	public Session getSession() {
		return session;
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

	void invalidateData() 
	{
		loadedMetadata.clear();
		this.setCompletelyLoaded(false);
		this.DataContextInvalidated();
	}

	@Override
	public void ReceiveErrorObject(ServerError error) {
		this.SessionFailedSynchronization(error.getErrorMsg());
	}
	
	@Override
	public void RegisterLoadedDataObject(ccc.android.meterreader.internaldata.ICallbackList data) 
	{
		if(this.isCompletelyLoaded)
		{
			//loadedMetadata.clear();		
			this.invalidateData();
		}
		
		if(data.getClass() == InternalGaugeDeviceList.class)
		{
			this.gaugedevicelist = (InternalGaugeDeviceList)data;
			loadedMetadata.put(this.getDevices(), true);
			fillGaugeLibrary();
		}
		if(data.getClass() == InternalStationList.class)
		{
			this.stationlist = (InternalStationList)data;
			loadedMetadata.put(this.getStations(), true);
		}
		if(data.getClass() == InternalReadingList.class)
		{
			this.readinglist = (InternalReadingList)data;
			loadedMetadata.put(this.getReadings(), true);
		}
		if(data.getClass() == InternalRouteList.class)
		{
			this.routelist = (InternalRouteList)data;
			loadedMetadata.put(this.getRoutes(), true);
		}
		if(data.getClass() == InternalGaugeList.class)
		{
			this.gaugelist = (InternalGaugeList)data;
			loadedMetadata.put(this.getGauges(), true);
		}
		if(data.getClass() == InternalGaugeDeviceDigitList.class)
		{
			synchronized(digitList)
			{
				//TODO remove InternalGaugeDeviceDigitList
				digitList.addAll(((InternalGaugeDeviceDigitList)data).getDigitList());
				if(--waitForPatterns == 0)  //received all sets
				{
					this.getDigitlist().getDigitList().addAll(digitList);
					loadedMetadata.put(this.getDigitlist(), true);
					//save standard digit patterns
	
					GaugeDevice de = new GaugeDevice();
					de.setGaugeDeviceId(0);
					de.setDigitPatterns(this.digitlist);
					insertPatterns(de);
				}
			}
		}
		
		if(this.loadedMetadata.size() == 6)
			for(ICallbackList l : this.loadedMetadata.keySet())
			{
				if(!loadedMetadata.get(l)) //!not
					return;
			}
		else
			return;

		this.setCompletelyLoaded(true);
		SessionSynchronized();
	}

	private void insertPatterns(GaugeDevice gaugeDevice) {
		if(gaugeDevice.getGaugeDeviceId() < 1)
			this.getDevices().getGaugeDeviceList().add(gaugeDevice);
		StaticGaugeLibrary.getGauges().put(gaugeDevice.getGaugeDeviceId(), new InternalGaugeDevice(gaugeDevice));
		waitForPatterns = Statics.NUM_OF_SETS_OF_PATTERNS;
	}

	void fillGaugeLibrary() {
		StaticGaugeLibrary.getGauges().clear();
		for(GaugeDevice device : this.getDevices())
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
	void DataContextUpdated()
	{
		if(this.getSyncState() == SyncState.Synchron)
			setSyncStateDown();
		for(IDataContextEventListener listener : listenerList)
			listener.OnDataContextUpdated(this);
	}
	void DataContextInvalidated()
	{
		setSyncStateDown();
		for(IDataContextEventListener listener : listenerList)
			listener.OnDataContextInvalidated();
	}
	void NewSessionInitialized()
	{
		for(IDataContextEventListener listener : listenerList)
			listener.OnNewSessionInitialized();
	}
	void SessionUploaded(Session session)
	{
		Statics.getWl().release();
		this.setSession(session);
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionUpLoaded(this.getSession());
		setSyncStateUp();
	}
	void SessionLoaded(Session session)
	{
		Statics.getWl().release();
		this.setSession(session);
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionLoaded(this.getSession());
		setSyncStateUp();
	}
	void OnSessionIsSynchronizing()
	{
		Statics.getWl().acquire();
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionIsSynchronizing(this.getSession());
	}
	void SessionSynchronized()
	{
		Statics.getWl().release();
		setSyncStateUp();
		for(IDataContextEventListener listener : listenerList)
			listener.OnSessionSynchronized(this.getSession());
	}
	void SessionFailedSynchronization(String msg)
	{
		Statics.getWl().release();
		for(IDataContextEventListener listener : listenerList)
			listener.OnFailedSessionSynchronization(msg);
	}
	
	public boolean isCompletelyLoaded() {
		return isCompletelyLoaded;
	}

	public void setCompletelyLoaded(boolean isCompletelyLoaded) {
		this.isCompletelyLoaded = isCompletelyLoaded;
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

	@JsonProperty("DigitList")
	public InternalGaugeDeviceDigitList getDigitlist() {
		return digitlist;
	}

	@JsonProperty("DigitList")
	public void setDigitlist(InternalGaugeDeviceDigitList digitlist) {
		this.digitlist = digitlist;
	}
	

	public SyncState getSyncState() {
		if(syncState ==2)
			return SyncState.Synchron;
		else if(syncState == 1)
			return SyncState.Desynchron;
		else
			return SyncState.Asynchron;
	}
	
	public void setSyncStateUp() {
		if(syncState < 2)
		{
			syncState++;
			for(IDataContextEventListener listener : listenerList)
				listener.OnSynchronizationStateChanged();
		}
	}
	
	public void setSyncStateDown() {
		if(syncState > 0)
		{
			syncState--;
			for(IDataContextEventListener listener : listenerList)
				listener.OnSynchronizationStateChanged();
		}
	}
}
