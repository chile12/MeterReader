package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.Station;

//import ccc.android.meterreader.internaldata.InternalStationList;
//@JsonSubTypes({  
//	@JsonSubTypes.Type(name = "InternalStationList", value = InternalStationList.class)
//})
public class StationList implements IGenericMemberList<Station>,  Iterable<Station> 
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

	@Override
	public void add(Station addi)
	{
		this.stationList.add(addi);
	}

	@Override
	public void addAll(IGenericMemberList<Station> list)
	{
		this.stationList.addAll(list.getList());
	}

	@Override
	public Station get(int index)
	{
		return this.stationList.get(index);
	}

	@Override
	public List<Station> getList()
	{
		return this.stationList;
	}
}
