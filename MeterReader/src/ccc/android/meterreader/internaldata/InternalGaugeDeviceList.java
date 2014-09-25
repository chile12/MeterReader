package ccc.android.meterreader.internaldata;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeDeviceList;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalGaugeDeviceList")
public class InternalGaugeDeviceList extends ccc.android.meterdata.internaltypes.InternalGaugeDeviceList implements ICallbackList
{
	private IMeterDataContainer parentManager = null;
//	public InternalGaugeDeviceList()
//	{
//		super();
//		}
	public InternalGaugeDeviceList(IMeterDataContainer manager)
	{
		super();
		parentManager = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{		
		if(list != null)
		{
			this.setGaugeDeviceList(new ArrayList<GaugeDevice>());
			List<GaugeDevice> zw = ((GaugeDeviceList) list).getGaugeDeviceList();
			for(GaugeDevice device : zw)
			{
				this.getGaugeDeviceList().add(new InternalGaugeDevice(device));
			}
			parentManager.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(ServerError error) {
		parentManager.ReceiveErrorObject(error);
		
	}
}
