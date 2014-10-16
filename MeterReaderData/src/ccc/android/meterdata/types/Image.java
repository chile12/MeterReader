package ccc.android.meterdata.types;

import org.codehaus.jackson.annotate.JsonIgnore;

import ccc.android.meterdata.MeterDataUtils;
import ccc.android.meterdata.interfaces.IGenericMember;

public class Image implements IGenericMember
{
	private int id;
	private String caption;
	private int height;
	private int width;
	private byte[] imgData;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getCaption()
	{
		return caption;
	}
	public void setCaption(String caption)
	{
		this.caption = caption;
	}
	public int getHeight()
	{
		return height;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}
	public int getWidth()
	{
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}
	@JsonIgnore
	public byte[] getImg()
	{
		return imgData;
	}
	@JsonIgnore
	public void setImg(byte[] imgData)
	{
		this.imgData = imgData;
	}
	public String getImgData()
	{
		return MeterDataUtils.encodeToBase64String(imgData);
	}
	public void setImgData(String dataString)
	{
		this.imgData = MeterDataUtils.decodeToImage(dataString);
	}
}
