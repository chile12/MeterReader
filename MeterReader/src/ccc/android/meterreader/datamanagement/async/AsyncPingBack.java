package ccc.android.meterreader.datamanagement.async;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import ccc.android.meterdata.types.PingCallback;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.RestClient;

public class AsyncPingBack  extends AsyncTask<Void, Void, PingCallback>
{
	@Override
	protected PingCallback doInBackground(Void... arg0) 
	{
		RestClient client = null;
		PingCallback err = new PingCallback();
		try {
			client = new RestClient (new URL(Statics.BASE_WS_URL));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			return client.Ping();
		} catch (UnknownHostException u) {
			err.setError(u.getMessage());
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		err.setPingResult(0);
		return err;
	}
	
    protected void onPostExecute(PingCallback result) {
    	if(result != null)
    	{
    		ccc.android.meterreader.statics.Statics.receivePingback(result);
    	}
    }
}
