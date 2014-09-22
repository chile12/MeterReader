package ccc.android.meterreader.datamanagement.async;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import ccc.android.meterdata.types.User;
import ccc.android.meterreader.statics.Statics;
import ccc.android.meterreader.viewelements.UserAuthentificationDialog;
import ccc.java.restclient.ParameterMap;
import ccc.java.restclient.RestClient;

public class AsyncUserAuthentification  extends AsyncTask<Object, Void, User>
{
	UserAuthentificationDialog dia = null;
	@Override
	protected User doInBackground(Object... login) 
	{
		RestClient client = null;
		dia = (UserAuthentificationDialog)login[0];
		try {
			client = new RestClient (new URL(Statics.BASE_WS_URL));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("login", login[1].toString().trim());
			map.put("password", login[2].toString().trim());
			return client.GetSingleObject(User.class, new ParameterMap(map, "GetUserByLogin"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected void onPostExecute(User result) {
		if(dia != null)
		{
			dia.callback(result);
		}
	}
}
