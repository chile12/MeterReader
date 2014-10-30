package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.interfaces.IGenericPartialList;
import ccc.android.meterdata.types.GaugeDeviceDigit;

public class GaugeDeviceDigitList  implements IGenericPartialList<GaugeDeviceDigit>,  Iterable<GaugeDeviceDigit> 
{
	List<GaugeDeviceDigit> digitList = Collections.synchronizedList(new ArrayList<GaugeDeviceDigit>());
	private String restriction;
	private int imageType; //0 == icon, 1 == meterImage
	private int skip;
	private int limit;
	private int size;
	
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
	public GaugeDeviceDigit getById(Object id) {
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
			digitList.clear();
	}

	public void add(GaugeDeviceDigit memb) 
	{
			digitList.add(memb);
	}

	@Override
	public void addAll(IGenericMemberList<GaugeDeviceDigit> list)
	{
		this.digitList.addAll(list.getList());
	}

	@Override
	public GaugeDeviceDigit get(int index)
	{
		return this.getList().get(index);
	}

	@Override
	@JsonIgnore
	public List<GaugeDeviceDigit> getList()
	{
		return this.digitList;
	}

	public int getImageType()
	{
		return imageType;
	}

	public void setImageType(int imageType)
	{
		this.imageType = imageType;
	}

	public int getSkip()
	{
		return skip;
	}

	public void setSkip(int skip)
	{
		this.skip = skip;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}
}

