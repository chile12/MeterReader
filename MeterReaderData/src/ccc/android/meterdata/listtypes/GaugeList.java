package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.Gauge;

//import ccc.android.meterreader.internaldata.InternalGaugeList;
//@JsonSubTypes({  
//	@JsonSubTypes.Type(name = "InternalGaugeList", value = InternalGaugeList.class)
//})
public class GaugeList implements IGenericMemberList,  Iterable<Gauge> 
{
	List<Gauge> gaugeList = new ArrayList<Gauge>();
	private String restriction;
	
	public List<Gauge> getGaugeList() 
	{
		return gaugeList;
	}

	public void setGaugeList(List<Gauge> stationList) {
		this.gaugeList = stationList;
	}

	@Override
	public Iterator<Gauge> iterator() {
		return gaugeList.iterator();
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
		return gaugeList.size();
	}

	@Override
	public Gauge getById(Object id) {
		if(!(id.getClass().equals(Integer.class) || id.getClass().equals(int.class)))
			return null;
		for(Gauge ga : this.getGaugeList())
		{
			if(ga.getGaugeId() == ((Integer)id).intValue())
				return ga;
		}
		return null;
	}
	

	@Override
	public void clear() 
	{
		gaugeList.clear();
	}
}