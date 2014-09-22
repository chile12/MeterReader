package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.*;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.RouteList;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalRouteList")
public class InternalRouteList  extends ccc.android.meterdata.internaltypes.InternalRouteList implements ICallbackList
{
	private IMeterDataContainer parentManager = null;
	public InternalRouteList()
	{super();}
	public InternalRouteList(IMeterDataContainer manager)
	{
		super();
		parentManager = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setRouteList(((RouteList) list).getRouteList());
			parentManager.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(ServerError error) {
		parentManager.ReceiveErrorObject(error);
		
	}
}
