package ccc.android.meterreader.internaldata;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.*;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.GaugeList;
import ccc.android.meterdata.types.Gauge;
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
			parentManager.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(ServerError error) {
		parentManager.ReceiveErrorObject(error);
		
	}
}
