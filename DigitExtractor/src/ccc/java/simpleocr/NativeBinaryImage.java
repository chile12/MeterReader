package ccc.java.simpleocr;

import org.bytedeco.javacpp.opencv_core.Mat;

import ccc.java.digitextractor.ImageStatics;

public class NativeBinaryImage
{
	private Mat pattern;
	private int width;
	private int height;

	public NativeBinaryImage()
	{}

	public NativeBinaryImage(int androidImageFormat, byte[] data)
	{
		try
		{
			if (androidImageFormat == 17)
				this.setPattern(ImageStatics.CreateMatContainer(data));
			if (androidImageFormat == 256)
				this.setPattern(ImageStatics.CreateMatContainer(data));
			this.height = this.getPattern().size().height();
			this.width = this.getPattern().size().width();
		}
		catch (Exception e)
		{
			if (e.getMessage() == null || !e.getMessage().contains("java.awt")) // not!!
				e.toString();
			else
				throw e;
		}
	}

	public Mat getPattern()
	{
		return pattern;
	}

	public void setPattern(Mat pattern)
	{
		this.pattern = pattern;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public byte[] getData()
	{
		byte[] zz = new byte[pattern.getByteBuffer().capacity()];
		pattern.getByteBuffer().get(zz);
		return zz;
	}
}
