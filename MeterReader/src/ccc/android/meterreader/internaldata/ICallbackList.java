package ccc.android.meterreader.internaldata;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.ServerError;


public interface ICallbackList
{
	void ListCallback(IGenericMemberList list);
	
	void ErrorCallback(ServerError error);
}
