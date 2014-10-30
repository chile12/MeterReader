package ccc.android.meterreader.internaldata;

import ccc.android.meterdata.errors.RestError;

public interface IMeterDataContainer 
{
	void RegisterLoadedDataObject(ICallbackList data);
	
	void ReceiveErrorObject(RestError error);
}
