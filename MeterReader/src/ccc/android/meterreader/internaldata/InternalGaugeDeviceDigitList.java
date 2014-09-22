package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalGaugeDeviceDigitList")
public class InternalGaugeDeviceDigitList extends ccc.android.meterdata.internaltypes.InternalGaugeDeviceDigitList implements ICallbackList
{
	private IMeterDataContainer parentManager = null;
	public InternalGaugeDeviceDigitList()
	{super();}
	public InternalGaugeDeviceDigitList(IMeterDataContainer manager)
	{
		super();
		parentManager = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{		
		if(list != null)
		{
			this.setDigitList(((GaugeDeviceDigitList) list).getDigitList());
			parentManager.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(ServerError error) {
		parentManager.ReceiveErrorObject(error);
		
	}
}
