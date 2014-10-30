package ccc.android.meterreader.datamanagement.async;

import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import ccc.android.meterreader.datamanagement.DataContextManager;
import ccc.android.meterreader.internaldata.Session;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.RestClient;

public class AsyncSessionSynchronizer extends AsyncTask<Object, Void, Boolean>
{
	DataContextManager manager = null;
	Session sess = null;
	@Override
	protected Boolean doInBackground(Object... params) 
	{
		sess = (Session)params[0];
		manager = (DataContextManager) params[1];
		URL wsUrl;
		try {
			wsUrl = new URL(Statics.getWSURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		RestClient client = new RestClient (wsUrl);

		try {
			return client.PostSingleObjectToServer(sess, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	protected void onPostExecute(Boolean result) {
		if(result != null)
		{
			manager.SessionSynchronizedCallback(sess);
		}
	}
}