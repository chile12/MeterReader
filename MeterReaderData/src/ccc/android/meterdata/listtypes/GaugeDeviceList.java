package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.*;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.GaugeDevice;

//import ccc.android.meterreader.internaldata.InternalGaugeDeviceList;
//@JsonSubTypes({  
//	@JsonSubTypes.Type(name = "InternalGaugeDeviceList", value = InternalGaugeDeviceList.class)
//})
public class GaugeDeviceList implements IGenericMemberList,  Iterable<GaugeDevice> 
{
	private List<GaugeDevice> gaugeDeviceList = new ArrayList<GaugeDevice>();
	private String restriction;

	@JsonProperty("GaugeDeviceList")
	public List<GaugeDevice> getGaugeDeviceList() {
		return gaugeDeviceList;
	}

	@JsonProperty("GaugeDeviceList")
	public void setGaugeDeviceList(List<GaugeDevice> gaugeDevices) {
		this.gaugeDeviceList = gaugeDevices;
	}

	@Override
	public Iterator<GaugeDevice> iterator() {
		return gaugeDeviceList.iterator();
	}

	@Override
	@JsonProperty("Restriction")
	public String getRestriction() {
		return restriction;
	}

	@Override
	@JsonProperty("Restriction")
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
	
	public int size()
	{
		return gaugeDeviceList.size();
	}

	@Override
	public GaugeDevice getById(Object id) 
	{
		if(!(id.getClass().equals(Integer.class) || id.getClass().equals(int.class)))
			return null;
		for(GaugeDevice ga : this.getGaugeDeviceList())
		{
			if(ga.getGaugeDeviceId() == ((Integer)id).intValue())
				return ga;
		}
		return null;
	}

	@Override
	public void clear() 
	{
		gaugeDeviceList.clear();
	}
}
