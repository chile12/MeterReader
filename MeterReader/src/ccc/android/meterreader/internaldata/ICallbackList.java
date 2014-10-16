package ccc.android.meterreader.internaldata;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.ServerError;


public interface ICallbackList
{
	boolean isLoaded();
	
	void setIsLoaded(boolean loaded);
	
	void ListCallback(IGenericMemberList list);
	
	void ErrorCallback(ServerError error);
	
	IMeterDataContainer getDataContainer();
	
	ICallbackList setDataContainer(IMeterDataContainer container);
}
