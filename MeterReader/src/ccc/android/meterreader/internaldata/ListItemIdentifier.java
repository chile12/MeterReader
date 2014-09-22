package ccc.android.meterreader.internaldata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class ListItemIdentifier 
{
	
	@Target(value = ElementType.FIELD)
	@Retention(value = RetentionPolicy.RUNTIME)
	public @interface Order {
	    int    o();
	} 

	@Order(o=0)
	private String title;
	@Order(o=1)
	private String subTitle;
	@Order(o=2)
	private String belowTitleLeft;
	@Order(o=3)
	private String belowTitleRight;
	@Order(o=4)
	private String aboveBottomLeft;
	@Order(o=5)
	private String aboveBottomRight;
	@Order(o=6)
	private String bottomLeft;
	@Order(o=7)
	private String bottomRight;
	
	private static Field[] orderedFields = null;
	
	public String getTitle() {
		return title;
	}
	public String getSubTitle() {
		return subTitle;
	}
	public String getBelowTitleLeft() {
		return belowTitleLeft;
	}
	public String getBelowTitleRight() {
		return belowTitleRight;
	}
	public String getBottomLeft() {
		return bottomLeft;
	}
	public String getBottomRight() {
		return bottomRight;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	public void setBelowTitleLeft(String belowTitleLeft) {
		this.belowTitleLeft = belowTitleLeft;
	}
	public void setBelowTitleRight(String belowTitleRight) {
		this.belowTitleRight = belowTitleRight;
	}
	public void setBottomLeft(String bottomLeft) {
		this.bottomLeft = bottomLeft;
	}
	public void setBottomRight(String bottomRight) {
		this.bottomRight = bottomRight;
	}
	
	public String getFieldByNumber(int fieldNo)
	{
		if(orderedFields == null)
		{
			Field[] allFields = this.getClass().getDeclaredFields();
			orderedFields = new Field[allFields.length-1];
			for(Field field : allFields)
			{
				if(field.getAnnotation(Order.class) != null)
					orderedFields[field.getAnnotation(Order.class).o()] = field;
			}
		}
		if(fieldNo < orderedFields.length)
		{
			try {
				return String.valueOf(orderedFields[fieldNo].get(this));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}
	public String getAboveBottomLeft() {
		return aboveBottomLeft;
	}
	public void setAboveBottomLeft(String aboveBottomLeft) {
		this.aboveBottomLeft = aboveBottomLeft;
	}
	public String getAboveBottomRight() {
		return aboveBottomRight;
	}
	public void setAboveBottomRight(String aboveBottomRight) {
		this.aboveBottomRight = aboveBottomRight;
	}

}
