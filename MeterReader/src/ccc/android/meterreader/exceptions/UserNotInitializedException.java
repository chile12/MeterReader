package ccc.android.meterreader.exceptions;

public class UserNotInitializedException extends Exception 
{
	  public UserNotInitializedException() { super(); }
	  public UserNotInitializedException(String message) { super(message); }
	  public UserNotInitializedException(String message, Throwable cause) { super(message, cause); }
	  public UserNotInitializedException(Throwable cause) { super(cause); }
}
