package ccc.android.meterdata.errors;

import java.lang.reflect.Method;
import java.util.Date;

public class ClientError extends RestError
{
	private Exception exception;
	private Method method;
	
	public ClientError(Exception ex)
	{
		this.setErrorTime(new Date());
		this.setErrorMsg(ex.getMessage());
		this.setErrorType(ex.getClass().getSimpleName());
		this.exception = ex;
	}

	public Exception getException()
	{
		return exception;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;
	}

	public Method getMethod()
	{
		return method;
	}

	public void setMethod(Method method)
	{
		this.method = method;
	}
}
