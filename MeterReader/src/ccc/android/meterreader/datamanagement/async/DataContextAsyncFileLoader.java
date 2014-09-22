package ccc.android.meterreader.datamanagement.async;

import android.os.AsyncTask;
import ccc.android.meterreader.R;
import ccc.android.meterreader.datamanagement.DataContext;
import ccc.android.meterreader.datamanagement.DataContextManager;
import ccc.android.meterreader.statics.Statics;
import ccc.java.restclient.RestClient;

public class DataContextAsyncFileLoader   extends AsyncTask<Object, Void, DataContext>
{
	DataContextManager manager;
	RestClient client = new RestClient ();
	
	@Override
	protected DataContext doInBackground(Object... params) 
	{
		manager = (DataContextManager)params[1]; 
		DataContext context = null;
		try {
			context = client.GetSingleObject(DataContext.class, (String)params[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return context;
	}
	
    protected void onPostExecute(DataContext result) {
    	if(result != null)
    	{
    		manager.callback(result);
    		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_done));
    	}
    	else
    		Statics.ShowToast(Statics.getDefaultResources().getString(R.string.main_sync_not_successful));
    }
}
