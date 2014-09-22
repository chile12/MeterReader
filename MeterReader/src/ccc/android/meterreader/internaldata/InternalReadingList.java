package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.*;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.ReadingList;
import ccc.android.meterdata.types.ServerError;
@JsonTypeName("InternalReadingList")
public class InternalReadingList  extends ccc.android.meterdata.internaltypes.InternalReadingList implements ICallbackList
{
	private IMeterDataContainer parentManager = null;
	public InternalReadingList()
	{super();}
	public InternalReadingList(IMeterDataContainer manager)
	{
		super();
		parentManager = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setReadingList(((ReadingList) list).getReadingList());
			parentManager.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(ServerError error) {
		parentManager.ReceiveErrorObject(error);
		
	}
}
