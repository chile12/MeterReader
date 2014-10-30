package ccc.android.meterdata.interfaces;

import java.util.List;


public interface IGenericMemberList<T extends IGenericMember> extends IGenericMember
{
	public String getRestriction();
	public void setRestriction(String restriction);
	public T getById(Object id);
	public void clear();
	public void add(T addi); 
	public void addAll(IGenericMemberList<T> list);
	public T get(int index);
	public List<T> getList();
}
