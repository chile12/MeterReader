package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.listtypes.ImageList;
@JsonTypeName("InternalGaugeDeviceDigitList")
public class InternalGaugeDeviceDigitList extends ccc.android.meterdata.listtypes.GaugeDeviceDigitList implements ICallbackList
{
	private IMeterDataContainer container = null;
	private boolean isLoaded = false;
	
	public InternalGaugeDeviceDigitList()
	{super();}
	
	@JsonIgnore
	public boolean isLoaded()
	{
		return isLoaded;
	}
	public void setIsLoaded(boolean loaded)
	{
		isLoaded = loaded;
	}
	
	public InternalGaugeDeviceDigitList(IMeterDataContainer manager)
	{
		super();
		container = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{		
		if(list != null)
		{
			this.setDigitList(((GaugeDeviceDigitList) list).getDigitList());
			this.setImageType(((GaugeDeviceDigitList) list).getImageType());
			this.setSize(((GaugeDeviceDigitList) list).getSize());
			this.setIsLoaded(true);
			container.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(RestError error) {
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
}
