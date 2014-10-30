package ccc.android.meterreader.datamanagement.async;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.interfaces.IGenericPartialList;
import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.listtypes.ImageList;
import ccc.android.meterreader.internaldata.ICallbackList;
import ccc.java.restclient.ParameterMap;
import ccc.java.restclient.RestClient;

public class GenericPartialListAsyncDbLoader extends AsyncTask<Object, Void, IGenericMember>
{
	ICallbackList callableList;
	IGenericPartialList list = null;
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 * Params[0] = WS-Endpoint URL
	 * Params[1] = the result list to fill
	 * Params[2] = the the object to call back to
	 * params[3] = the server-side function to call
	 * Params[4] = parameter name for server-side function
	 * Params[5] = param value
	 * Params[6...] = like 4 & 5
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected IGenericMember doInBackground(Object... params) 
	{
		callableList = (ICallbackList)params[2];
		RestClient client = new RestClient ((URL)params[0]);
		Map<String, Object> map = new HashMap<String, Object>();
		if(params.length > 4)
		{
			for(int i = 4; i+1<params.length;i=i+2)
			{
				map.put(String.valueOf(params[i]), params[i+1]);
			}
		}
		ParameterMap where = new ParameterMap(map, (String)params[3]);
		try {
			list = (IGenericPartialList) client.GetMultipleObjects((Class<IGenericPartialList>)params[1], where);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(list == null) //collect server error
		{
			RestError err = client.getLatestServerError();
			return err;
		}
		
		while(list.getSkip() + list.getLimit() < list.getSize())
		{
			map.put("skip", list.getLimit() + list.getSkip());
			where = new ParameterMap(map, (String)params[3]);
			ImageList imgL = null;
			try {
				imgL = (ImageList) client.GetMultipleObjects((Class<ImageList>)params[1], where);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(imgL != null)
			{
				imgL.addAll(list);
				list = imgL;
			}
			else //collect server error
			{
				RestError err = client.getLatestServerError();
				return err;
			}
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
