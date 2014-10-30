package ccc.android.meterdata.types;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.ClassUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import ccc.android.meterdata.interfaces.IGenericMember;

public class Preference implements IGenericMember
{
	public static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
	public static ObjectMapper mapper = new ObjectMapper();
	
	private String key;
	private String value;
	private String javaType;
	private String comment;
	private boolean global = true;	//if false -> will not get uploaded to db
	
	@SuppressWarnings("deprecation")
	public Preference()
	{
		mapper.getSerializationConfig().setDateFormat(format);
		mapper.getDeserializationConfig().setDateFormat(format);
	}
	
	@SuppressWarnings("deprecation")
	public Preference(String key, Object value, boolean global) throws JsonGenerationException, JsonMappingException, IOException
	{
		mapper.getSerializationConfig().setDateFormat(format);
		mapper.getDeserializationConfig().setDateFormat(format);
		this.key = key;
		setTypedValue(value);
		this.global = global;
	}
	
	public String getKey()
	{
		return key;
	}
	public void setKey(String key)
	{
		this.key = key;
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
	public String getJavaType()
	{
		return javaType;
	}
	public void setJavaType(String javaType)
	{
		this.javaType = javaType;
	}
	public String getComment()
	{
		return comment;
	}
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	
	@SuppressWarnings({ "unchecked" })
	@JsonIgnore
	public <T> T getTypedValue(T defaultVal)
	{		
		if(this.value == null)
			return null;
		T zw = defaultVal;
		
		try
		{
			zw = (T) mapper.readValue(value, Class.forName(javaType));
		}
		catch (ClassNotFoundException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return zw;
	}
	
	@JsonIgnore
	public <T> void setTypedValue(T value) throws JsonGenerationException, JsonMappingException, IOException
	{
		if(value == null)
		{
			this.value = null;
			return;
		}
		Class<?> clazz = ClassUtils.wrapperToPrimitive(value.getClass());
		if(clazz != null || value.getClass() == String.class)
		{
			this.javaType = mapper.writeValueAsString(ClassUtils.primitiveToWrapper(value.getClass())).replace("\"", "");
			this.value = mapper.writeValueAsString(value);//.replace("\"", "");
		}
	}

	public boolean isGlobal()
	{
		return global;
	}

	public void setGlobal(boolean global)
	{
		this.global = global;
	}
}
