package ccc.android.meterdata.internaltypes;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.interfaces.ICallbackList;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeList;
@JsonTypeName("InternalGaugeList")
public class InternalGaugeList extends GaugeList implements ICallbackList
{
	private boolean isLoaded = false;

	@JsonIgnore
	public boolean getIsLoaded()
	{
		return isLoaded;
	}
	public void setIsLoaded(boolean loaded)
	{
		isLoaded = loaded;
	}
	public InternalGaugeList()
	{
		super();
	}
	
	@Override
	public void callback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setGaugeList(((GaugeList) list).getGaugeList());
		}
	}
}
