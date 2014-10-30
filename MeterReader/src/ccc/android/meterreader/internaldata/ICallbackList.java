package ccc.android.meterreader.internaldata;

import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMemberList;


public interface ICallbackList
{
	boolean isLoaded();
	
	void setIsLoaded(boolean loaded);
	
	void ListCallback(IGenericMemberList list);
	
	void ErrorCallback(RestError error);
	
	IMeterDataContainer getDataContainer();
	
	ICallbackList setDataContainer(IMeterDataContainer container);
}
