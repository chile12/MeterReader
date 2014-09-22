package ccc.java.digitextractor.exceptions;

/**
 * 
 * @author Chile
 *
 *should be throwen when no digit frame has been found
 */
public class FrameNotFoundException extends Exception 
{
	  public FrameNotFoundException() { super(); }
	  public FrameNotFoundException(String message) { super(message); }
	  public FrameNotFoundException(String message, Throwable cause) { super(message, cause); }
	  public FrameNotFoundException(Throwable cause) { super(cause); }
}
