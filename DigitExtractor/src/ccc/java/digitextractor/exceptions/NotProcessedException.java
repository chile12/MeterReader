
package ccc.java.digitextractor.exceptions;

/**
 * 
 * @author Chile
 *
 *should be throwen when no digit frame has been found
 */
public class NotProcessedException extends Exception 
{
	  public NotProcessedException() { super(); }
	  public NotProcessedException(String message) { super(message); }
	  public NotProcessedException(String message, Throwable cause) { super(message, cause); }
	  public NotProcessedException(Throwable cause) { super(cause); }
}
