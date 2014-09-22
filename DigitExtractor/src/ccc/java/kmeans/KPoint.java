package ccc.java.kmeans;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.Rect;

public class KPoint 
{
	public int Id;
	public double X;
	public double Y;
	public int Cluster;
	private Object reference;
	
	public KPoint(int id, double x, double y)
	{
		this.Id = id;
		this.X = x;
		this.Y = y;
	}

	
	public KPoint(int id, double x, double y, Object ref)
	{
		this.Id = id;
		this.X = x;
		this.Y = y;
		this.reference = ref;
	}
	
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public double getX() {
		return X;
	}
	public void setX(double x) {
		X = x;
	}
	public double getY() {
		return Y;
	}
	public void setY(double y) {
		Y = y;
	}
	
	public static double FindDistance(KPoint pt1, KPoint pt2)
	{
	    double x1 = pt1.X, y1 = pt1.Y;
	    double x2 = pt2.X, y2 = pt2.Y;

	    //find euclidean distance
	    double distance = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
	    return (distance);
	}
	
	@Override
	public String toString()
	{
		return "ID=" + String.valueOf(Id) + "Cluster=" + String.valueOf(Cluster) + ", X=" + String.valueOf(X) + ", Y=" + String.valueOf(Y);
	}

	public int getCluster() {
		return Cluster;
	}

	public void setCluster(int cluster) {
		Cluster = cluster;
	}


	public Object getReference() {
		return reference;
	}
}
