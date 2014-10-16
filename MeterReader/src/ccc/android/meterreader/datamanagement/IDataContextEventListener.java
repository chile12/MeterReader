package ccc.android.meterreader.datamanagement;

import ccc.android.meterreader.internaldata.Session;

public interface IDataContextEventListener 
{
	void OnSessionUnsynchronized();
	void OnSessionInitialized();
	void OnSessionIsSynchronizing(Session session);
	void OnSessionSynchronized(Session session);
	void OnFileSynchronization(Session session);
	void OnFailedSessionSynchronization(String error);
}
