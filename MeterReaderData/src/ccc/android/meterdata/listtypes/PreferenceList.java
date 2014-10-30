package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.Preference;

public class PreferenceList implements IGenericMemberList<Preference>,  Iterable<Preference> 
{
	private List<Preference> preferenceList = new ArrayList<Preference>();
	private String restriction;
	
	@Override
	public Iterator<Preference> iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRestriction()
	{
		return restriction;
	}

	@Override
	public void setRestriction(String restriction)
	{
		this.restriction = restriction;
	}

	@Override
	public Preference getById(Object id)
	{
		if(!(id.getClass().equals(String.class)))
			return null;
		for(Preference ga : this.getPreferenceList())
		{
			if(ga.getKey().equals(id.toString()))
				return ga;
		}
		return null;
	}

	@Override
	public void clear()
	{
		preferenceList.clear();
	}

	public List<Preference> getPreferenceList()
	{
		return preferenceList;
	}

	public void setPreferenceList(List<Preference> preferenceList)
	{
		this.preferenceList = preferenceList;
	}

	@Override
	public void add(Preference addi)
	{
		this.preferenceList.add(addi);
	}

	@Override
	public void addAll(IGenericMemberList<Preference> list)
	{
		this.preferenceList.addAll(list.getList());
	}

	@Override
	public Preference get(int index)
	{
		return this.preferenceList.get(index);
	}

	@Override
	public List<Preference> getList()
	{
		return this.preferenceList;
	}

}
