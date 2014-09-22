package ccc.android.meterdata.interfaces;


public interface IGenericMemberList extends IGenericMember
{
	public String getRestriction();
	public void setRestriction(String restriction);
	public IGenericMember getById(Object id);
	public void clear();
}
