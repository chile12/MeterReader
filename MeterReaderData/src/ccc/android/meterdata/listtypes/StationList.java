package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.Station;

//import ccc.android.meterreader.internaldata.InternalStationList;
//@JsonSubTypes({  
//	@JsonSubTypes.Type(name = "InternalStationList", value = InternalStationList.class)
//})
public class StationList implements IGenericMemberList,  Iterable<Station> 
{
	private List<Station> stationList = new ArrayList<Station>();
	private String restriction;
	
	public List<Station> getStationList() {
		return stationList;
	}

	public void setStationList(List<Station> stationList) {
		this.stationList = stationList;
	}

	@Override
	public Iterator<Station> iterator() {
		return stationList.iterator();
	}

	@Override
	@JsonIgnore
	public String getRestriction() {
		return restriction;
	}

	@Override
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
	
	public int size()
	{
		return stationList.size();
	}
	
	@Override
	public Station getById(Object id) {
		if(!(id.getClass().equals(Integer.class) || id.getClass().equals(int.class)))
			return null;
		for(Station ga : this.getStationList())
		{
			if(ga.getRouteStationId() == ((Integer)id).intValue())
				return ga;
		}
		return null;
	}
	
	public Station getByGaugeId(Object id) {
		if(!(id.getClass().equals(Integer.class) || id.getClass().equals(int.class)))
			return null;
		for(Station ga : this.getStationList())
		{
			if(ga.getGaugeId() == ((Integer)id).intValue())
				return ga;
		}
		return null;
	}

	@Override
	public void clear() 
	{
		stationList.clear();
	}
}
