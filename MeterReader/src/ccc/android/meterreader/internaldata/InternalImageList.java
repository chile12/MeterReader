package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.ImageList;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalStationList")
public class InternalImageList  extends ccc.android.meterdata.listtypes.ImageList implements ICallbackList
{
	private IMeterDataContainer container = null;
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
	
	public InternalImageList()
	{super();}
	public InternalImageList(IMeterDataContainer manager)
	{
		super();
		container = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setImageList(((ImageList) list).getImageList());
			this.setIsLoaded(true);
			container.RegisterLoadedDataObject(this);
		}
	}
	
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
}