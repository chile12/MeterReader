package ccc.android.meterreader.datamanagement.async;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.AsyncTask;
import ccc.android.meterdata.*;
import ccc.android.meterreader.internaldata.ICallbackList;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterreader.internaldata.*;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.RestClient;

public class GenericMemberListAsyncFileLoader  extends AsyncTask<Object, Void, IGenericMemberList>
{
	ICallbackList callableList;
	RestClient client = new RestClient();
	
	@SuppressWarnings("unchecked")
	@Override
	protected IGenericMemberList doInBackground(Object... params) 
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
		return list;
	}
	
    protected void onPostExecute(IGenericMemberList result) {
    	if(result != null)
    	{
    		callableList.ListCallback(result);
    	}
    }
}
