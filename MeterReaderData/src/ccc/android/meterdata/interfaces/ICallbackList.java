package ccc.android.meterdata.interfaces;



public interface ICallbackList
{
	boolean getIsLoaded();
	void setIsLoaded(boolean loaded);
	void callback(IGenericMemberList list);
}
