package ccc.java.restclient;

import java.util.Map;

public class ParameterMap 
{	
	private Map<String, Object> restrictions;
	private String methodName;
	private Integer id = null;

	public ParameterMap(Map<String, Object> restrmap, String method)
	{
		restrictions = restrmap;
		methodName = method;
	}
	public ParameterMap(Map<String, Object> restrmap)
	{
		restrictions = restrmap;
	}
	public ParameterMap(int id)
	{
		this.id = id;
	}
	public ParameterMap(int id, String method)
	{
		this.id = id;
		methodName = method;
	}
	public Map<String, Object> getRestrictions() {
		return restrictions;
	}
	public String getMethodName() {
		return methodName;
	}
	public Integer getId() {
		return id;
	}
}
