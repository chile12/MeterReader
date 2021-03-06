package ccc.java.restclient;
 
import java.io.*;
import java.net.*;
import java.util.*;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import ccc.android.meterdata.listtypes.*;
import ccc.android.meterdata.types.*;
import ccc.android.meterdata.errors.ClientError;
import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.errors.ServerError;
import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.GaugeDeviceDigit;
import ccc.android.meterdata.types.PingCallback;

public class RestClient
{
	private URL connectionUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int normalTimeout = 1000000;
    private List<RestError> errors = new ArrayList<RestError>();

    public RestClient(URL baseUrl) {
    	
        mapper.setPropertyNamingStrategy(new UpperNamingStrategy());
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        connectionUrl = baseUrl;
    }
    
    public RestClient() {
    	
        mapper.setPropertyNamingStrategy(new UpperNamingStrategy());
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    @SuppressWarnings("unchecked")
	public <T> T GetSingleObject(Class <T> type, String jsonData) throws Exception
    {
    	Object returnObject = null;
        try {
        	if(jsonData != null)
        		returnObject = type.cast(mapper.readValue(jsonData, type));
        	
		} catch (Exception e) {
			errors.add( new ClientError(e));
		}    	
    	return (T) returnObject;
    }
    
    public <T extends IGenericMember> IGenericMember GetSingleObject(Class <T> type, ParameterMap restriction)
    {
    	if(connectionUrl==null)
    	{
    		errors.add(new ClientError(new MalformedURLException("no connection-url provided")));
    		return null;
    	}
    	
    	String path = GetSubPath(type, restriction, HttpMethod.Get);
		
    	String jsonList = null;
		try
		{
			jsonList = HandleHttpResponse(path);
	        return type.cast(mapper.readValue(jsonList, type));
		}
		catch (IOException e1)
		{
			errors.add( new ClientError(e1));
		} 
		return null;
    }
    
    public <T extends IGenericMember> IGenericMember GetMultipleObjects(Class <T> type) throws Exception
    {
    	return GetMultipleObjects(type, new ParameterMap(null));
    }
    
    public <T extends IGenericMember> IGenericMember GetMultipleObjects(Class <T> type, ParameterMap restriction)
    {
    	if(connectionUrl==null)
    		errors.add( new ClientError(new MalformedURLException("no connection-url provided")));
    	
    	String path = GetSubPath(type, restriction, HttpMethod.Get);
    	String jsonList = null;
		try
		{
			jsonList = HandleHttpResponse(path);
			return GetMultipleObjects(type, jsonList);
		}
		catch (IOException e1)
		{
			errors.add( new ClientError(e1));
		} 
		return null;
    }
    
	public boolean PostSingleObjectToServer(IGenericMember obj, ParameterMap map) throws IOException
	{
    	String path = GetSubPath(obj.getClass(), map, HttpMethod.Post);
		HttpURLConnection conn = getHttpPostConnection(path);
		int status = 0;
		try {
			OutputStream os = conn.getOutputStream();
			byte[] output = JsonStatics.JsonMvcTime(obj).getBytes();
			os.write(output);
			os.close();
			status = conn.getResponseCode();
		} catch (IOException e) {
			errors.add(new ClientError(e));
			return false;
		}
		conn.disconnect();
		if(status == 200 || status == 201)
			return true;
		else
			return false;
	}
	
	public <T extends IGenericMember> boolean  PostListOfObject(Class<? extends IGenericMember> type, List<GaugeDeviceDigit> digits, ParameterMap map)
	{
    	String path = GetSubPath(type ,map, HttpMethod.Post, true);
		int status = 0;
		HttpURLConnection conn = null;
		try {
			conn = getHttpPostConnection(path);
			OutputStream os = conn.getOutputStream();
			byte[] output = JsonStatics.JsonMvcTime(digits).getBytes();
			os.write(output);
			os.close();
			status = conn.getResponseCode();
		} catch (IOException e) {
			errors.add( new ClientError(e));
		}
		finally
		{
			if(conn != null)
				conn.disconnect();
		}
		if(status == 200 || status == 201)
			return true;
		else
			return false;		
	}
	
	public boolean PostMultipleObjectsToServer(IGenericMemberList obj, ParameterMap map)
	{
    	String path = GetSubPath(obj.getClass(), map, HttpMethod.Post);
		HttpURLConnection conn = null;
		int status = 0;
		try {
			conn = getHttpPostConnection(path);
			OutputStream os = conn.getOutputStream();
			byte[] output = JsonStatics.JsonMvcTime(obj).getBytes();
			os.write(output);
			os.close();
			status = conn.getResponseCode();
		} catch (IOException e) {
			errors.add( new ClientError(e));
		}
		finally
		{
			conn.disconnect();
		}
		if(status == 200 || status == 201)
			return true;
		else
			return false;
	}
	public <T> String GetSubPath(Class<T> type, ParameterMap restriction, HttpMethod method) 
	{
		return GetSubPath(type, restriction,  method, false);
	}

	public <T> String GetSubPath(Class<T> type, ParameterMap restriction, HttpMethod method, boolean isList) 
	{
		String path="";
		if(restriction == null)
			return method.toString() + type.getSimpleName() + (isList ? "s" : "");
		if(restriction.getMethodName() == null)//|| restriction.getMethodName().length() > 0)
			path+= method.toString() + type.getSimpleName();
		else
			path+= restriction.getMethodName();
		
		if(isList)
			path+= "s";
		
		if(restriction.getId() != null)
			path += "/" + String.valueOf(restriction.getId());
		if(restriction.getRestrictions() != null && restriction.getRestrictions().size() > 0)
		{
			path += "?";
			for(String key : restriction.getRestrictions().keySet())
			{
				Object val = restriction.getRestrictions().get(key);
				if(val.getClass() == Date.class)
				{
					path += key + "=" + getMvcDateString((Date)val) + "&";
				}
				else
					//TODO escape chars!
					path += key + "=" + String.valueOf(val) + "&";
			}
		}
		if(path.endsWith("&"))
			path = path.substring(0, path.length()-1);
		if(path.length() < 5)
			return "";
		
		return path;
	}
	
	public PingCallback Ping() throws IOException
	{
		HttpURLConnection conn = getHttpConnection("Ping", HttpMethod.Get, 2000);
		try {
			String json = ReadJsonResponse(conn);
			PingCallback val = GetSingleObject(PingCallback.class, json);
			conn.disconnect();
			return val;
		} catch (IOException io) {
			errors.add( new ClientError(io));
		} catch (Exception e){
			errors.add( new ClientError(e));
		}
		conn.disconnect();
		PingCallback err = new PingCallback();
		err.setPingResult(0);
		return err;
	}

	private String getMvcDateString(Date date) 
	{
		int milliseconds = (int) (date.getTime() % 1000l);
		milliseconds =  milliseconds<0 ? milliseconds+1000 : milliseconds;
		@SuppressWarnings("deprecation")
		String ret = (date.getDay()+1) + "-" + (date.getMonth()+1) + "-" + (date.getYear()+1900) + 
				"%20" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "." + milliseconds;
		return ret;
	}
    
    public <T extends IGenericMember> T GetMultipleObjects(Class <T> type, String jsonData)
    {
    	T returnObjects = null;
        	if(jsonData != null)
				try {
					returnObjects = type.cast(mapper.readValue(jsonData, type));
				} catch (JsonParseException e) {
					tryGetError(jsonData);
				} catch (JsonMappingException e) {
					tryGetError(jsonData);
				} catch (IOException e) {
					tryGetError(jsonData);
				}
        	   	
    	return (T) returnObjects;
    }

	private void tryGetError(String jsonData) {
		RestError err;
		try {
			err = ServerError.class.cast(mapper.readValue(jsonData, ServerError.class));
		} catch (JsonParseException e1) {
			err = new ClientError(e1);
		} catch (JsonMappingException e1) {
			err = new ClientError(e1);
		} catch (IOException e1) {
			err = new ClientError(e1);
		}
		errors.add(err);
	}

	public String HandleHttpResponse(String path) throws IOException
	{
    	HttpURLConnection conn = getHttpGetConnection(path);
		String json = null;
		int status =0;
		try {
			status = conn.getResponseCode();
			json = ReadJsonResponse(conn);
		} catch (IOException e) {
			errors.add( new ClientError(e));	
			}

	        conn.disconnect();
			return json;
	}

	private String ReadJsonResponse(HttpURLConnection conn) throws IOException 
	{
		StringBuilder sb;
		BufferedReader br = null;
		
		if (conn.getResponseCode() >= 400) {
			br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		} else {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		}
		sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) 
		{
			line = line.replace("\\/Date(", "").replace(")\\/", ""); //handle Date(472382436328) to 472382436328
			sb.append(line + "\n");
		}
		br.close();
		return sb.toString();
	}
	
	private HttpURLConnection getHttpPostConnection(String path) throws IOException
	{
		return getHttpConnection(path, HttpMethod.Post, normalTimeout);
	}
	
	private HttpURLConnection getHttpGetConnection(String path) throws IOException
	{
		return getHttpConnection(path, HttpMethod.Get, normalTimeout);
	}
	
    private HttpURLConnection getHttpConnection(String path, HttpMethod requestMethod, int timeout) throws IOException 
    {
    	HttpURLConnection conn = null;
    	path = (!(connectionUrl.getPath().endsWith("/")) ? "/" : "") + path;
		URL uri = new URL(connectionUrl + path);
		conn = (HttpURLConnection) uri.openConnection();
		conn.setUseCaches(false);
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout);
		conn.setRequestProperty("Content-Type", "application/json");			
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestMethod(requestMethod.toString().toUpperCase());
		if(requestMethod == HttpMethod.Post)
		{
			conn.setDoInput (true);
			conn.setDoOutput (true);
		}
		conn.connect();
	    return conn;
    }

	public ObjectMapper getMapper() {
		return mapper;
	}
	
	public RestError getLatestServerError()
	{
		if(errors.size() > 0)
			return errors.get(errors.size()-1);
		else 
			return null;
	}
}
