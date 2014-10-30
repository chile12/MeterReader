package ccc.android.meterdata.errors;

import java.util.Date;

import ccc.android.meterdata.interfaces.IGenericMember;

public class RestError implements IGenericMember
{
	private String errorMsg;
	private Date errorTime;
	private String errorType;
	
	public RestError(){
		errorTime = new Date();
	}
	public RestError(String type, String msg)
	{
		this.errorType = type;
		this.errorMsg = msg;
		errorTime = new Date();
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public Date getErrorTime() {
		return errorTime;
	}
	public String getErrorType()
	{
		return errorType;
	}
	public void setErrorType(String errorType)
	{
		this.errorType = errorType;
	}
	public void setErrorTime(Date errorTime)
	{
		this.errorTime = errorTime;
	}
	
}