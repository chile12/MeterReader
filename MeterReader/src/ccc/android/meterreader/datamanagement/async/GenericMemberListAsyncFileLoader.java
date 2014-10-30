package ccc.android.meterreader.datamanagement.async;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.AsyncTask;
import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterreader.internaldata.ICallbackList;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.RestClient;

public class GenericMemberListAsyncFileLoader  extends AsyncTask<Object, Void, IGenericMember>
{
	ICallbackList callableList;
	RestClient client = new RestClient();
	
	@SuppressWarnings("unchecked")
	@Override
	protected IGenericMember doInBackground(Object... params) 
	{
		BufferedReader br = null;
		String json = null;
		try
		{
			br = Statics.ReadFile((String)params[0]);
			json = br.readLine();
			if(json.equals("true"))
				json = br.readLine();
			else
				json = null;
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block8
			e.printStackTrace();
		}
		
		
		callableList = (ICallbackList)params[2];
		IGenericMemberList list = client.GetMultipleObjects((Class<IGenericMemberList>)params[1], json);
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
    		if(result instanceof RestError)
    			callableList.ErrorCallback((RestError) result);
    		else
    			callableList.ListCallback((IGenericMemberList) result);
    	}
    }
}
