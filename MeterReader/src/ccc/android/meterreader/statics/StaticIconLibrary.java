package ccc.android.meterreader.statics;

import ccc.android.meterreader.internaldata.InternalImageList;

public class StaticIconLibrary
{
	protected static InternalImageList icons;
	
	public static InternalImageList getIcons()
	{
		return icons;
	}

	public static void setIcons(InternalImageList icons)
	{
		StaticIconLibrary.icons = icons;
	}
}
