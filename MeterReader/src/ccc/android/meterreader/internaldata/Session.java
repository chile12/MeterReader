package ccc.android.meterreader.internaldata;

import java.util.Date;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.listtypes.GaugeDeviceList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.listtypes.ReadingList;
import ccc.android.meterdata.listtypes.RouteList;
import ccc.android.meterdata.listtypes.StationList;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterdata.types.Route;

public class Session  implements IGenericMember
{
	private int sessionId;
	private Integer userId;
	private Route route;
	private Date startDate;
	private Date endDate;

	private GaugeDeviceList newDevices = new GaugeDeviceList();
	private GaugeList newGauges = new GaugeList();
	private ReadingList newReadings = new ReadingList();
	private RouteList newRoutes = new RouteList();
	private StationList newStations = new StationList();
	
	public int getSessionId() {
		return sessionId;
	}
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	public Route getRoute() {
		return route;
	}
	public void setRoute(Route route) {
		this.route = route;
	}
	public ReadingList getReadings() {
		return newReadings;
	}
	public void setReadings(ReadingList readings) {
		this.newReadings = readings;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public GaugeDeviceList getNewDevices() {
		return newDevices;
	}
	public void setNewDevices(GaugeDeviceList newDevices) {
		this.newDevices = newDevices;
	}
	public GaugeList getNewGauges() {
		return newGauges;
	}
	public void setNewGauges(GaugeList newGauges) {
		this.newGauges = newGauges;
	}
	public RouteList getNewRoutes() {
		return newRoutes;
	}
	public void setNewRoutes(RouteList newRoutes) {
		this.newRoutes = newRoutes;
	}
	public StationList getNewStations() {
		return newStations;
	}
	public void setNewStations(StationList newStations) {
		this.newStations = newStations;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getUserId() {
		return this.userId;
	}
	
	public void InsertNewReading(Reading read)
	{
		this.newReadings.getReadingList().add(read);
	}
	
	public void AddNewDevice(GaugeDevice device)
	{
		this.newDevices.getGaugeDeviceList().add(device);
	}
	
	public boolean hasNewDate()
	{
		if(this.newDevices.size() >0)
			return true;
		else if(this.newGauges.size() > 0)
			return true;
		else if(this.newReadings.size() > 0)
			return true;
		else if(this.newRoutes.size() > 0)
			return true;
		else if(this.newStations.size() > 0)
			return true;
		else
			return false;
	}
	
	public void clearAllData()
	{
		newDevices.clear();
		newGauges.clear();
		newReadings.clear();
		newRoutes.clear();
		newStations.clear();
	}
}
