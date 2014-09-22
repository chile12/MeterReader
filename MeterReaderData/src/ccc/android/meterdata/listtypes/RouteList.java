package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.Route;

//import ccc.android.meterreader.internaldata.InternalRouteList;
//@JsonSubTypes({  
//	@JsonSubTypes.Type(name = "InternalRouteList", value = InternalRouteList.class)
//})
public class RouteList implements IGenericMemberList,  Iterable<Route> 
{
	List<Route> routeList = new ArrayList<Route>();
	public List<Route> getRouteList() {
		return routeList;
	}

	public void setRouteList(List<Route> routeList) {
		this.routeList = routeList;
	}

	private String restriction;
	
	@Override
	public Iterator<Route> iterator() {
		return routeList.iterator();
	}

	@Override
	@JsonIgnore
	public String getRestriction() {
		return restriction;
	}

	@Override
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
	
	public int size()
	{
		return routeList.size();
	}

	@Override
	public Route getById(Object id) {
		if(!(id.getClass().equals(Integer.class) || id.getClass().equals(int.class)))
			return null;
		for(Route ga : this.getRouteList())
		{
			if(ga.getRouteId() == ((Integer)id).intValue())
				return ga;
		}
		return null;
	}
	

	@Override
	public void clear() 
	{
		routeList.clear();
	}
}