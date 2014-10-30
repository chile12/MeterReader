package ccc.android.meterdata.types;

import org.codehaus.jackson.annotate.JsonIgnore;

import ccc.android.meterdata.MeterDataUtils;
import ccc.android.meterdata.interfaces.IGenericMember;

public class Image implements IGenericMember
{
	private int id;
	private String caption;
	private String medium;
	private byte[] imgData;
	private Integer GaugeDeviceId;
	
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
	@JsonIgnore
	public byte[] getBinary()
	{
		return imgData;
	}
	@JsonIgnore
	public void setBinary(byte[] imgData)
	{
		this.imgData = imgData;
	}
	public String getImgData()
	{
		return MeterDataUtils.encodeToBase64String(imgData);
	}
	public void setImgData(String dataString)
	{
		this.setBinary(MeterDataUtils.decodeToImage(dataString));
	}
	public String getMedium()
	{
		return medium;
	}
	public void setMedium(String medium)
	{
		this.medium = medium;
	}
	public Integer getGaugeDeviceId()
	{
		return GaugeDeviceId;
	}
	public void setGaugeDeviceId(Integer gaugeDeviceId)
	{
		GaugeDeviceId = gaugeDeviceId;
	}
}
