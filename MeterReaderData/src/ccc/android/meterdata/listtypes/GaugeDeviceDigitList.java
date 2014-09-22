package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.codehaus.jackson.annotate.JsonProperty;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.GaugeDeviceDigit;

public class GaugeDeviceDigitList  implements IGenericMemberList,  Iterable<GaugeDeviceDigit> 
{
	List<GaugeDeviceDigit> digitList = Collections.synchronizedList(new ArrayList<GaugeDeviceDigit>());
	private String restriction;
	
	@JsonProperty("DigitList")
	public List<GaugeDeviceDigit> getDigitList() {
		synchronized(this)
		{
			return digitList;
		}
	}

	@JsonProperty("DigitList")
	public void setDigitList(List<GaugeDeviceDigit> digitList) {
		synchronized(this)
		{
			this.digitList = digitList;
		}
	}
	
	@Override
	public Iterator<GaugeDeviceDigit> iterator() {
		synchronized(this)
		{
			return digitList.iterator();
		}
	}

	@JsonProperty("Restriction")
	@Override
	public String getRestriction() {
			return restriction;
	}

	@JsonProperty("Restriction")
	@Override
	public void setRestriction(String restriction) {
		this.restriction = restriction;
		
	}

	@Override
	public IGenericMember getById(Object id) {
		if(!(id.getClass().equals(Integer.class) || id.getClass().equals(int.class)))
			return null;
		synchronized(this)
		{
			for(GaugeDeviceDigit ga : this.getDigitList())
			{
				if(ga.getDigit() == ((Integer)id).intValue())
					return ga;
			}
			return null;
		}
	}

	@Override
	public void clear() {
		synchronized(this)
		{
			digitList.clear();
		}
	}

	public void add(GaugeDeviceDigit memb) 
	{
		synchronized(this)
		{
			digitList.add(memb);
		}
	}
	
	public void addAll(GaugeDeviceDigitList list) 
	{
		synchronized(this)
		{
			digitList.addAll(list.getDigitList());
		}
	}
}

