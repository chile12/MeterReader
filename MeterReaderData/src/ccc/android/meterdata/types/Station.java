package ccc.android.meterdata.types;

import ccc.android.meterdata.interfaces.IGenericMember;

public class Station implements IGenericMember
{
	private int routeStationId;
	private int routeId;
	private int gaugeId;
	private int stationNo;
	private String readingNotes;
	
	public int getRouteStationId() {
		return routeStationId;
	}
	public void setRouteStationId(int routeStationId) {
		this.routeStationId = routeStationId;
	}
	public int getRouteId() {
		return routeId;
	}
	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	public int getGaugeId() {
		return gaugeId;
	}
	public void setGaugeId(int gaugeId) {
		this.gaugeId = gaugeId;
	}
	public int getStationNo() {
		return stationNo;
	}
	public void setStationNo(int stationNo) {
		this.stationNo = stationNo;
	}
	public String getReadingNotes() {
		return readingNotes;
	}
	public void setReadingNotes(String readingNotes) {
		this.readingNotes = readingNotes;
	}
}
