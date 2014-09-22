package ccc.java.digitextractor.statichelpers;

import java.util.Comparator;
import java.util.Map.Entry;

import org.bytedeco.javacpp.opencv_core.Rect;

import ccc.java.digitextractor.data.RectExt;
import ccc.java.kmeans.KPointCollection;

public class Comperators
{

	public static Comparator<Entry<?, Double>> EntryDoubleValueComp = new Comparator<Entry<?, Double>>()
	{
		@Override
		public int compare(Entry<?, Double> arg0, Entry<?, Double> arg1)
		{
			return Double.compare(arg0.getValue() * 1000, arg1.getValue() * 1000);
		}
	};

	public static Comparator<Entry<?, Entry<Integer, Double>>> EntryEntryDoubleValueComp = new Comparator<Entry<?, Entry<Integer, Double>>>()
	{
		@Override
		public int compare(Entry<?, Entry<Integer, Double>> arg0, Entry<?, Entry<Integer, Double>> arg1)
		{
			return Double.compare(arg0.getValue().getValue() * 1000, arg1.getValue().getValue() * 1000);
		}
	};

	public static Comparator<Rect> Height = new Comparator<Rect>()
	{
		@Override
		public int compare(Rect arg0, Rect arg1)
		{
			return arg0.height() - arg1.height();
		}
	};

	public static Comparator<Rect> Xcomp = new Comparator<Rect>()
	{
		@Override
		public int compare(Rect arg0, Rect arg1)
		{
			if (arg0 == null && arg1 != null)
				return arg1.x();
			else if (arg1 == null && arg0 != null)
				return arg0.x();
			else if (arg1 == null && arg0 == null)
				return 0;
			else
				return arg0.x() - arg1.x();
		}
	};

	public static Comparator<RectExt> BrightnessComparer = new Comparator<RectExt>()
	{
		@Override
		public int compare(RectExt arg0, RectExt arg1)
		{
			return arg0.getBrightness() - arg1.getBrightness();
		}
	};

	public static Comparator<Rect> WidthComparer = new Comparator<Rect>()
	{
		@Override
		public int compare(Rect arg0, Rect arg1)
		{
			return arg0.width() - arg1.width();
		}
	};

	public static Comparator<KPointCollection> KpointCollectionXComp = new Comparator<KPointCollection>()
	{
		@Override
		public int compare(KPointCollection arg0, KPointCollection arg1)
		{
			return (int) (arg0.getCentroid().getX() - arg1.getCentroid().getX());
		}
	};

	public static Comparator<KPointCollection> KpointCollectionYComp = new Comparator<KPointCollection>()
	{
		@Override
		public int compare(KPointCollection arg0, KPointCollection arg1)
		{
			return (int) (arg0.getCentroid().getY() - arg1.getCentroid().getY());
		}
	};
}
