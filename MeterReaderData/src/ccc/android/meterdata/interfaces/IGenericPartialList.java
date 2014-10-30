package ccc.android.meterdata.interfaces;

public interface IGenericPartialList<T extends IGenericMember> extends IGenericMemberList<T>
{
	int getSkip();
	
	void setSkip(int skip);
	
	int getLimit();
	
	void setLimit(int limit);
	
	int getSize();
	
	void setSize(int size);
}
