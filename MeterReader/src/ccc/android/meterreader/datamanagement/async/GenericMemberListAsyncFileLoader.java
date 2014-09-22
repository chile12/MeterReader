package ccc.android.meterreader.datamanagement.async;

import android.os.AsyncTask;
import ccc.android.meterdata.*;
import ccc.android.meterreader.internaldata.ICallbackList;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterreader.internaldata.*;
import ccc.java.restclient.RestClient;

public class GenericMemberListAsyncFileLoader  extends AsyncTask<Object, Void, IGenericMemberList>
{
	ICallbackList callableList;
	RestClient client = new RestClient ();
	
	@SuppressWarnings("unchecked")
	@Override
	protected IGenericMemberList doInBackground(Object... params) 
	{
		callableList = (ICallbackList)params[2];
		IGenericMemberList list = client.GetMultipleObjects((Class<IGenericMemberList>)params[1], (String)params[0]);
		return list;
	}
	
    protected void onPostExecute(IGenericMemberList result) {
    	if(result != null)
    	{
    		callableList.ListCallback(result);
    	}
    }
}
