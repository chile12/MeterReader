package ccc.android.meterdata.internaltypes;

import org.codehaus.jackson.annotate.JsonIgnore;


import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.*;
import ccc.android.meterdata.interfaces.ICallbackList;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.ReadingList;
@JsonTypeName("InternalReadingList")
public class InternalReadingList  extends ReadingList implements ICallbackList
{
	private boolean isLoaded = false;

	@JsonIgnore
	public boolean getIsLoaded()
	{
		return isLoaded;
	}
	public void setIsLoaded(boolean loaded)
	{
		isLoaded = loaded;
	}
	public InternalReadingList()
	{
		super();
	}
	
	@Override
	public void callback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setReadingList(((ReadingList) list).getReadingList());
		}
	}
}
