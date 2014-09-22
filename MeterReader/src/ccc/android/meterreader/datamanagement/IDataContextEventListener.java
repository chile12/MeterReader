package ccc.android.meterreader.datamanagement;

import ccc.android.meterdata.types.ServerError;
import ccc.android.meterdata.types.Session;
import ccc.android.meterreader.statics.Statics;

public interface IDataContextEventListener 
{
	void OnDataContextInvalidated();
	void OnDataContextUpdated(DataContext newContext);
	void OnNewSessionInitialized();
	void OnSessionIsSynchronizing(Session session);
	void OnSessionSynchronized(Session session);
	void OnSessionLoaded(Session session);
	void OnSessionUpLoaded(Session session);
	void OnFailedSessionSynchronization(String error);
	void OnSynchronizationStateChanged();
}
