package ccc.android.meterreader.datamanagement.async;

import java.net.MalformedURLException;
import java.net.URL;

import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.*;
import android.os.AsyncTask;

public class AsyncReadingSynchronizer extends AsyncTask<Reading, Void, Void>
{
	
	@Override
	protected Void doInBackground(Reading... params) 
	{
		Reading read = params[0];
		URL wsUrl;
		try {
			wsUrl = new URL(Statics.BASE_WS_URL + "/PostReading");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		RestClient client = new RestClient (wsUrl);

		try {
			client.PostSingleObjectToServer(read, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}