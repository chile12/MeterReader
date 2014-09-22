package ccc.java.digitextractor.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.Rect;

public class RectExt extends Rect
{
	private static int currentId =0;
	private int brightness;
	private int id;
	
	public RectExt(Rect rec, int brightness)
	{
		super(new Pointer(rec));
		this.brightness = brightness;
		this.id = getCurrentId();
	}
	
	public RectExt(CvRect rec, int brightness)
	{
		super(new Pointer(rec));
		this.brightness = brightness;
		this.id = getCurrentId();
	}
	
	@Override
	public String toString()
	{
		return "[x=" + this.x() + ", y=" + this.y() + ", w=" + this.width() + ", h=" + this.height() + "], b=" + this.getBrightness(); 
	}
	
	public static List<RectExt> ConvertList(List<Rect> input, int brightness)
	{
		List<RectExt> output = new ArrayList<RectExt>();
		for(Rect rec : input)
		{
			output.add(new RectExt(rec, brightness));
		}
		return output;
	}
	
	public static List<RectExt> ConvertCvRectList(List<CvRect> input, int brightness)
	{
		List<RectExt> output = new ArrayList<RectExt>();
		for(CvRect rec : input)
		{
			output.add(new RectExt(rec, brightness));
		}
		return output;
	}

	public int getId() {
		return id;
	}

	public int getBrightness() {
		return brightness;
	}

	public static int getCurrentId() {
		return ++currentId;
	}
}
