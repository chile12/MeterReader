package ccc.android.meterreader.datamanagement.async;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import ccc.android.meterdata.errors.ClientError;
import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.errors.ServerError;
import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.types.PingCallback;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.RestClient;

public class AsyncPingBack  extends AsyncTask<Void, Void, IGenericMember>
{
	@Override
	protected IGenericMember doInBackground(Void... arg0) 
	{
		RestClient client = null;
		PingCallback err = new PingCallback();
		try {
			client = new RestClient (new URL(Statics.getWSURL()));
		} catch (MalformedURLException e1) {
			return new ClientError(e1);
		}
		try {
			return client.Ping();
		} catch (UnknownHostException u) {
			err.setError(u.getMessage());
		}catch (Exception e) {
			return new ClientError(e);
		}
		
		err.setPingResult(0);
		return err;
	}
	
    protected void onPostExecute(IGenericMember result) {
    	if(result != null && result instanceof PingCallback)
    	{
    		ccc.android.meterreader.statics.Statics.receivePingback((PingCallback)result);
    	}
    	else if(result != null && result instanceof RestError)
    	{
    		PingCallback err = new PingCallback();
    		err.setPingResult(0);
    		ccc.android.meterreader.statics.Statics.receivePingback(err);
    	}
    }
}
