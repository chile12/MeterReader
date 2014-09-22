package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.*;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalGaugeList")
public class InternalGaugeList extends ccc.android.meterdata.internaltypes.InternalGaugeList implements ICallbackList
{
	private IMeterDataContainer parentManager = null;
	public InternalGaugeList()
	{super();}
	public InternalGaugeList(IMeterDataContainer manager)
	{
		super();
		parentManager = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setGaugeList(((GaugeList) list).getGaugeList());
			parentManager.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(ServerError error) {
		parentManager.ReceiveErrorObject(error);
		
	}
}
