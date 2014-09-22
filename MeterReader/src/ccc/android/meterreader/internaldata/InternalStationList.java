package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.*;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.StationList;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalStationList")
public class InternalStationList  extends ccc.android.meterdata.internaltypes.InternalStationList implements ICallbackList
{
	private IMeterDataContainer parentManager = null;
	public InternalStationList()
	{super();}
	public InternalStationList(IMeterDataContainer manager)
	{
		super();
		parentManager = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setStationList(((StationList) list).getStationList());
			parentManager.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(ServerError error) {
		parentManager.ReceiveErrorObject(error);
		
	}
}
