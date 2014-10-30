package ccc.android.meterreader.internaldata;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.types.Gauge;
@JsonTypeName("InternalGaugeList")
public class InternalGaugeList extends ccc.android.meterdata.listtypes.GaugeList implements ICallbackList
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
	
	public InternalGaugeList()
	{super();}
	public InternalGaugeList(IMeterDataContainer manager)
	{
		super();
		container = manager;
	}
	
	public List<String> GetGaugeNames()
	{
		List<String> names = new ArrayList<String>();
		for(Gauge g : this.getGaugeList())
		{
			names.add(g.getName());
		}
		return names;
	}
	
	public List<String> GetGaugeLocations()
	{
		List<String> names = new ArrayList<String>();
		for(Gauge g : this.getGaugeList())
		{
			names.add(g.getName() + " - " + g.getLocation());
		}
		return names;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setGaugeList(((GaugeList) list).getGaugeList());
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
