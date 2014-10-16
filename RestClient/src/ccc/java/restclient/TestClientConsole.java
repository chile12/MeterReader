package ccc.java.restclient;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import ccc.android.meterdata.*;
import ccc.android.meterdata.listtypes.ReadingList;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterdata.types.Route;
import ccc.android.meterdata.types.Station;

public class TestClientConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		RestClient client = null;
		try {
			client = new RestClient(new URL("http://abas/MobileGaugeReading"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
//	       StationList stations = client.GetMultipleObjects(null, StationList.class);
//	       
//	       for(Station st : stations)
//	       {
//	    	   Station g = (Station) st;
//		       System.out.println(g.getMedium() + ":");
//		       System.out.println(g.getName());
//	       }
	       
       try {
//    	   Map<String,Object> map = new HashMap<String, Object>();
    	   Calendar cal = Calendar.getInstance();
    	   cal.set(2013, Calendar.NOVEMBER, 1);
//    	   map.put("gaugeId", 36);
    	   Reading read = new Reading();
    	   read.setGaugeId(28);
    	   read.setRead(8933941.999f);
    	   read.setStationId(4);
    	   read.setUtcFrom(cal.getTime());
    	   cal.set(2013, Calendar.NOVEMBER, 30);
    	   read.setUtcTo(cal.getTime());
    	   //System.out.println("Route " + read.getRouteId() + ":" + read.getGaugeId() + " - " + read.getRead());
    	   
    	   Station st = new Station();
    	   st.setGaugeId(29);
    	   st.setReadingNotes("lalala");
    	   st.setStationNo(1);
    	   st.setRouteId(3);
    	   st.setRouteStationId(4);
    	   
    	   Route rt = new Route();
    	   rt.setRouteId(3);
    	   rt.setUtcCreated(new Date());
    	   rt.setName("testRoute2");
    	   rt.setUserIdCreated(1);
    	   
    	   ReadingList list = new ReadingList();
    	   list.getReadingList().add(read);

    	   //boolean res = client.PostSingleObjectToServer(se, null);
//		       
		   //int station = client.Ping();
		   //System.out.println(station);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
