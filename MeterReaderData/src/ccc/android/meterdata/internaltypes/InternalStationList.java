package ccc.android.meterdata.internaltypes;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.interfaces.ICallbackList;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.StationList;
@JsonTypeName("InternalStationList")
public class InternalStationList  extends StationList implements ICallbackList
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
	public InternalStationList()
	{
		super();
	}
	
	@Override
	public void callback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setStationList(((StationList) list).getStationList());
		}
	}
}
