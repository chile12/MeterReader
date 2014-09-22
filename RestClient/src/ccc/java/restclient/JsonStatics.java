package ccc.java.restclient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonStatics 
{
	
	public static String JsonMvcTime(Object obj)
	{
		//set date format to MVC expected format
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setDateFormat(format);
		mapper.getDeserializationConfig().setDateFormat(format);
		mapper.setPropertyNamingStrategy(new UpperNamingStrategy());
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String JsonTimestamp(Object obj)
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(new UpperNamingStrategy());
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
