package ccc.android.meterreader.datamanagement.async;

import java.net.URL;
import java.util.*;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.ServerError;
import ccc.android.meterreader.internaldata.ICallbackList;
import ccc.java.restclient.*;
import android.os.AsyncTask;

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
			ServerError err = client.getLatestServerError();
			return err;
		}
		return list;
	}
	
    protected void onPostExecute(IGenericMember result) {
    	if(result != null)
    	{
    		if(result instanceof IGenericMemberList)
    			callableList.ListCallback((IGenericMemberList)result);
    		else if(result instanceof ServerError)
    			callableList.ErrorCallback((ServerError) result);
    	}
    	else
    		callableList.ErrorCallback(new ServerError());
    }
}
