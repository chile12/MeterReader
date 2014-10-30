package ccc.android.meterreader.statics;

import ccc.android.meterreader.internaldata.InternalImageList;

public class StaticImageLibrary
{
	protected static InternalImageList images;

	public static InternalImageList getImages()
	{
		return images;
	}

	public static void setImages(InternalImageList images)
	{
		StaticImageLibrary.images = images;
	}
}

