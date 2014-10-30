package ccc.android.meterdata.errors;

public class ServerError extends RestError
{
	private String errorId;
	
	public String getErrorId()
	{
		return errorId;
	}
	public void setErrorId(String errorId)
	{
		this.errorId = errorId;
	}
}
