package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.Reading;

public class ReadingList  implements IGenericMemberList,  Iterable<Reading> 
{
	private List<Reading> readingList = new ArrayList<Reading>();
	private String restriction;
	
	public List<Reading> getReadingList() {
		return readingList;
	}

	public void setReadingList(List<Reading> readingList) {
		this.readingList = readingList;
	}

	@Override
	public Iterator<Reading> iterator() {
		return readingList.iterator();
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
		return readingList.size();
	}

	@Override
	public Reading getById(Object id) 
	{
		if(!id.getClass().equals(Object[].class) || ((Object[])id).length != 2 || !(((Object[])id)[0].getClass().equals(Integer.class) 
				|| ((Object[])id)[0].getClass().equals(int.class)) || !((Object[])id)[1].getClass().equals(Date.class))
			return null;
		for(Reading ga : this.getReadingList())
		{
			if(ga.getGaugeId() == ((Integer)((Object[])id)[0]).intValue() && ga.getUtcFrom().compareTo((Date)((Object[])id)[1]) == 0)
				return ga;
		}
		return null;
	}
	
	public Reading getLastReadingById(int id) 
	{
		Reading retGa = null;
		for(Reading ga : this.getReadingList())
		{
			if(ga.getGaugeId() == id)
			{
				if(retGa == null)
					retGa = ga;
				else if(retGa.getUtcTo().before(ga.getUtcTo()))
				{
					retGa = ga;
				}
			}
		}
		return retGa;
	}
	

	@Override
	public void clear() 
	{
		readingList.clear();
	}
}
