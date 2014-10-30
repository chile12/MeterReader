package ccc.android.meterdata.internaltypes;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.interfaces.ICallbackList;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.RouteList;
@JsonTypeName("InternalRouteList")
public class InternalRouteList  extends RouteList implements ICallbackList
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
	public InternalRouteList()
	{
		super();
	}
	
	@Override
	public void callback(IGenericMemberList list) 
	{
		if(list != null)
		{

			this.setRouteList(((RouteList) list).getRouteList());
		}
	}
}
