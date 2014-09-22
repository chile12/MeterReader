package ccc.android.meterdata.types;

import java.util.Date;

import ccc.android.meterdata.interfaces.IGenericMember;

public class ServerError implements IGenericMember
{
	private String errorId;
	private String errorType;
	private String errorMsg;
	private Date errorTime;
	
	public ServerError(){
		errorTime = new Date();
	}
	public ServerError(String msg)
	{
		this.errorMsg = msg;
		errorTime = new Date();
	}
	public String getErrorId() {
		return errorId;
	}
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}
	public String getErrorType() {
		return errorType;
	}
	public void setErrorType(String errorType) {
		this.errorType = errorType;
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
	
}