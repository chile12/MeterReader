package ccc.android.meterreader.internaldata;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeDeviceList;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalGaugeDeviceList")
public class InternalGaugeDeviceList extends ccc.android.meterdata.listtypes.GaugeDeviceList implements ICallbackList
{
	private IMeterDataContainer container = null;
	private List<InternalGaugeDevice> list = new ArrayList<InternalGaugeDevice>();
	private boolean isLoaded = false;
	
	@JsonIgnore
	public boolean isLoaded()
	{
		return isLoaded;
	}
	public void setIsLoaded(boolean loaded)
	{
		isLoaded = loaded;
	}
	public InternalGaugeDeviceList()
	{
		super();
		}
	public InternalGaugeDeviceList(IMeterDataContainer manager)
	{
		super();
		container = manager;
	}
	
	@JsonIgnore
	@Override
	public void ListCallback(IGenericMemberList list) 
	{		
		if(list != null)
		{
			List<GaugeDevice> zw = (List<GaugeDevice>) ((GaugeDeviceList) list).getGaugeDeviceList();
			for(GaugeDevice device : zw)
			{
				this.getInternalGaugeDeviceList().add(new InternalGaugeDevice(device));
			}
			this.setIsLoaded(true);
			container.RegisterLoadedDataObject(this);
		}
	}
	
	@JsonIgnore
	@Override
	public void ErrorCallback(ServerError error) {
		container.ReceiveErrorObject(error);
		
	}
	
	@JsonIgnore
	@Override
	public IMeterDataContainer getDataContainer()
	{
		return container;
	}
	@JsonIgnore
	@Override
	public ICallbackList setDataContainer(IMeterDataContainer container)
	{
		this.container = container;
		return this;
	}
	@JsonIgnore
	public List<GaugeDevice> getSuperGaugeDeviceList()
	{
		return (List<GaugeDevice>) super.getGaugeDeviceList();
	}
	@JsonProperty("InternalGaugeDeviceList")
	public List<InternalGaugeDevice> getInternalGaugeDeviceList()
	{
		return this.list;
	}
	@JsonProperty("InternalGaugeDeviceList")
	public void setInternalGaugeDeviceList(List<InternalGaugeDevice> gaugeDeviceList)
	{
		this.list = (List<InternalGaugeDevice>) gaugeDeviceList;
	}
}
