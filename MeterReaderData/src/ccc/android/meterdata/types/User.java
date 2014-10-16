package ccc.android.meterdata.types;

import ccc.android.meterdata.interfaces.IGenericMember;

public class User  implements IGenericMember
{
	
private int userId;
private String name;
private String firstName;

public User()
{}
public User(int id)
{
	//TODO check this
	this.userId = id;
}
public int getUserId() {
	return userId;
}
public void setUserId(int userId) {
	this.userId = userId;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getFirstName() {
	return firstName;
}
public void setFirstName(String firstName) {
	this.firstName = firstName;
}
}
