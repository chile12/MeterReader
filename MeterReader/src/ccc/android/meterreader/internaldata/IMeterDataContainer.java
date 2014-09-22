package ccc.android.meterreader.internaldata;

import ccc.android.meterdata.types.ServerError;

public interface IMeterDataContainer 
{
	void RegisterLoadedDataObject(ICallbackList data);
	
	void ReceiveErrorObject(ServerError error);
}
