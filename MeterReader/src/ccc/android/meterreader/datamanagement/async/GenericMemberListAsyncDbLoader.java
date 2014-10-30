package ccc.android.meterreader.datamanagement.async;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterreader.internaldata.ICallbackList;
import ccc.java.restclient.ParameterMap;
import ccc.java.restclient.RestClient;

public class GenericMemberListAsyncDbLoader extends AsyncTask<Object, Void, IGenericMember>
{
	ICallbackList callableList;
	
	@SuppressWarnings("unchecked")
	@Override
	protected IGenericMember doInBackground(Object... params) 
	{
		callableList = (ICallbackList)params[2];
		RestClient client = new RestClient ((URL)params[0]);
		Map<String, Object> map = new HashMap<String, Object>();
		ParameterMap where;
		if(params.length > 4)
		{
			for(int i = 4; i+1<params.length;i=i+2)
			{
				map.put(String.valueOf(params[i]), params[i+1]);
			}
		}
		if(params.length > 3 && params[3] != null)
			where = new ParameterMap(map, (String)params[3]);
		else
			where = new ParameterMap(map);
		IGenericMemberList list =null;
		try {
			list = (IGenericMemberList) client.GetMultipleObjects((Class<IGenericMemberList>)params[1], where);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(list == null) //collect server error
		{
			RestError err = client.getLatestServerError();
			return err;
		}
		return list;
	}
	
    protected void onPostExecute(IGenericMember result) {
    	if(result != null)
    	{
    		if(result instanceof IGenericMemberList)
    			callableList.ListCallback((IGenericMemberList)result);
    		else if(result instanceof RestError)
    			callableList.ErrorCallback((RestError) result);
    	}
    	else
    		callableList.ErrorCallback(new RestError("UnknowenError", "an unknowen Error occurred"));
    }
}
