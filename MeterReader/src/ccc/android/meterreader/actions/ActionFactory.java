package ccc.android.meterreader.actions;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import ccc.java.restclient.RestClient;

public class ActionFactory
{
	public static IMeterReaderAction CreateAction(String jsonString)
	{
		try
		{
			 LinkedHashMap<String, Object> ses = new RestClient().getMapper().readValue(jsonString, new LinkedHashMap<String, Object>().getClass());
			 Object clName = ses.get("Type");
			 if(clName == null)
				 throw new JsonMappingException("no action type found");
			return (IMeterReaderAction) new RestClient().getMapper().readValue(jsonString, Class.forName(clName.toString()));
		}
		catch (JsonParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JsonMappingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null; 
	}
}
