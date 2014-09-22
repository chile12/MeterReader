package ccc.java.kmeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KPointCollection extends ArrayList<KPoint>
{
	private String label;
	private KPoint Centroid;
	private double maxDistToCentroidX;
	private double maxDistToCentroidY;

	public KPoint getCentroid() {
		return Centroid;
	}
	
	public void setCentroid(KPoint centroid) {
		Centroid = centroid;
	}
	
    private static Comparator<KPoint> X = new Comparator<KPoint>() 
    {
		@Override
		public int compare(KPoint arg0, KPoint arg1) {
			return (int) (arg0.getX() - arg1.getX());
		}
    };
    
    private static Comparator<KPoint> Y = new Comparator<KPoint>() 
    {
		@Override
		public int compare(KPoint arg0, KPoint arg1) {
			return (int) (arg0.getY() - arg1.getY());
		}
    };
    
    public static boolean IsSimilarCluster(KPointCollection coll1, KPointCollection coll2, double maxDifference)
    {
    	double centroidDist = KPoint.FindDistance(coll1.getCentroid(), coll2.getCentroid());
    	if(centroidDist > maxDifference)
    		return false;
    	return true;
    }
	
	public void reCalculateCentroidAndMaxDist()
	{
		this.setMaxDistToCentroidX(0);
		this.setMaxDistToCentroidY(0);
		List<KPoint> zw = (List<KPoint>) this.clone();
		Collections.sort(zw, X);
		double medianX = zw.get(zw.size()/2).getX();
		Collections.sort(zw, Y);
		double medianY = zw.get(zw.size()/2).getY();
		
		KPoint help = new KPoint(-1, medianX, medianY);
		double minDist = Double.MAX_VALUE;
		
		for(KPoint point : this)
		{
			double dist = KPoint.FindDistance(point,  help);
			if(dist < minDist)
			{
				minDist = dist;
				setCentroid(point);
			}
			if(Math.abs(point.getX() - help.getX()) > this.maxDistToCentroidX)
				this.setMaxDistToCentroidX(Math.abs(point.getX() - help.getX()));
			if(Math.abs(point.getY() - help.getY()) > this.maxDistToCentroidY)
				this.setMaxDistToCentroidY(Math.abs(point.getY() - help.getY()));
		}
	}
	
	private double calculateDensity()
	{
		double retVal = 0d;
		
		for(int i =0; i < this.size(); i++)
		{
			KPoint tt = this.get(i);
			if(this.getCentroid() != tt)
				retVal += KPoint.FindDistance(tt, this.getCentroid());
			
		}
		return retVal/(this.size()-1);
	}
	
	public double calculateYDensity()
	{
		double retVal = 0d;
		
		for(int i =0; i < this.size(); i++)
		{
			KPoint tt = this.get(i);
			if(this.getCentroid() != tt)
				retVal += KPoint.FindDistance(new KPoint(0, 0, tt.getY()), new KPoint(0, 0, this.getCentroid().getY()));
			
		}
		return retVal/(this.size()-1);
	}
	
	
	///Statics
	public static List<KPointCollection> SplitColletionByVariation(KPointCollection coll, Integer variationX, Integer variationY)
	{
		List<KPointCollection> ret = new ArrayList<KPointCollection>();
		ret.add(new KPointCollection());	//lower x Values
		ret.add(new KPointCollection());	//higher x Values
		ret.add(new KPointCollection());	//centroid near Values
		ret.add(new KPointCollection());	//lower y Values
		ret.add(new KPointCollection());	//higher y Values
		for(KPoint point : coll)
		{
			if(variationX != null && coll.getCentroid().getX() - point.getX() > variationX)
				ret.get(0).add(point);
			else if(variationX != null && point.getX() - coll.getCentroid().getX() > variationX)
				ret.get(1).add(point);
			else if(variationY != null && coll.getCentroid().getY() - point.getY() > variationY)
				ret.get(3).add(point);
			else if(variationY != null && point.getY() - coll.getCentroid().getY() > variationY)
				ret.get(4).add(point);
			else
				ret.get(2).add(point);
		}
		for(int i = ret.size()-1; i >=0; i--)
		{
			if(ret.get(i).size() == 0)
				ret.remove(i);
			else
				ret.get(i).reCalculateCentroidAndMaxDist();
		}
		return ret;
	}
	
	public static <T> List<List<T>> SplitList(List<T> items, int groupCount)
	{
	    List<List<T>> allGroups = new ArrayList<List<T>>();
		
	    //split the list into equal groups
	    int startIndex = 0;
	    int groupLength = (int)Math.round((double)items.size() / (double)groupCount);
	    while (startIndex < items.size())
	    {
	        List<T> group = new ArrayList<T>();
	        group.addAll(items.subList(startIndex, startIndex + groupLength));
	        startIndex += groupLength;
			
	        //adjust group-length for last group
	        if (startIndex + groupLength > items.size())
	        {
	            groupLength = items.size() - startIndex;
	        }
			
	        allGroups.add(group);
	    }
		
	    //merge last two groups, if more than required groups are formed
	    if (allGroups.size() > groupCount && allGroups.size() > 2)
	    {
	        allGroups.get(allGroups.size() - 2).addAll(allGroups.get(allGroups.size()-1));
	        allGroups.remove(allGroups.size() - 1);
	    }
		
	    return (allGroups);
	}

	public double getMaxDistToCentroidX() {
		return maxDistToCentroidX;
	}

	protected void setMaxDistToCentroidX(double maxDistToCentroidX) {
		this.maxDistToCentroidX = maxDistToCentroidX;
	}

	public double getMaxDistToCentroidY() {
		return maxDistToCentroidY;
	}

	protected void setMaxDistToCentroidY(double maxDistToCentroidY) {
		this.maxDistToCentroidY = maxDistToCentroidY;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public double getDensity() {
		return calculateDensity();
	}
	
	public KPoint GetPointById(int Id)
	{
		for(KPoint p : this)
		{
			if(p.getId() == Id)
				return p;
		}
		return null;
	}
	
	@Override
	public String toString()
	{
		return "Size=" + String.valueOf(this.size()) + ", Density=" + String.valueOf(this.getDensity()) + ", Centroid:(" + this.Centroid.toString() + ")";
	}
}
