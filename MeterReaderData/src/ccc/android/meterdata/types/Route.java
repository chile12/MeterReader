package ccc.android.meterdata.types;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import ccc.android.meterdata.interfaces.IGenericMember;

public class Route implements IGenericMember
{
	private int routeId;
	private String name;
	private List<Integer> stations;
	//private RoutePrecision precision;  ?????
	private Integer userIdCreated;
	private Date utcCreated;
	private Date utcModified;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getUserIdCreated() {
		return userIdCreated;
	}
	public void setUserIdCreated(Integer userIdCreated) {
		this.userIdCreated = userIdCreated;
	}
	public Date getUtcCreated() {
		return utcCreated;
	}
	public void setUtcCreated(Date utcCreated) {
		this.utcCreated = utcCreated;
	}
	public Date getUtcModified() {
		return utcModified;
	}
	public void setUtcModified(Date utcModified) {
		this.utcModified = utcModified;
	}
	public int getRouteId() {
		return routeId;
	}
	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	@JsonIgnore
	public List<Integer> getStations() {
		return stations;
	}
	@JsonIgnore
	public void setStations(List<Integer> stations) {
		this.stations = stations;
	}
	@JsonIgnore
	public void setStations(int[] stations) {
		this.stations = new ArrayList<Integer>();
		for(int st : stations)
		{
			this.stations.add(st);
		}
	}
//	public RoutePrecision getPrecision() {
//		return precision;
//	}
//	public void setPrecision(RoutePrecision precision) {
//		this.precision = precision;
//	}
}
