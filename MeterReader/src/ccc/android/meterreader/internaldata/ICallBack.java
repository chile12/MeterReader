package ccc.android.meterreader.internaldata;

import ccc.android.meterdata.types.User;
import ccc.android.meterreader.datamanagement.DataContext;

public interface ICallBack
{
	void DataContextLoadedCallback(DataContext context);
	
	void SessionSynchronizedCallback(Session ses);
	
	void UserLoggedInCallback(User user);
}
