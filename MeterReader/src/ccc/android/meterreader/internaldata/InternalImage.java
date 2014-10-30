package ccc.android.meterreader.internaldata;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import ccc.android.meterdata.MeterDataUtils;
import ccc.android.meterdata.types.Image;
import ccc.java.simpleocr.NativeBinaryImage;

public class InternalImage extends Image
{
	private NativeBinaryImage nativeImage;
	
	public InternalImage(Image img)
	{
		this.setCaption(img.getCaption());
		this.setGaugeDeviceId(img.getGaugeDeviceId());
		this.setId(img.getId());
		this.setMedium(img.getMedium());
		nativeImage = new NativeBinaryImage(android.graphics.ImageFormat.JPEG, img.getBinary());
	}

	@JsonIgnore
	public NativeBinaryImage getNativeImage()
	{
		return nativeImage;
	}
	
	@JsonIgnore
	public Bitmap getBitmap()
	{
		byte[] data = nativeImage.getData();
        return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
	
	@JsonIgnore
	public int getWidth()
	{
		return nativeImage.getHeight();
	}
	
	@JsonIgnore
	public int getHeight()
	{
		return nativeImage.getHeight();
	}
	@Override
	public String getImgData()
	{
		return MeterDataUtils.encodeToBase64String(nativeImage.getData());
	}
}
