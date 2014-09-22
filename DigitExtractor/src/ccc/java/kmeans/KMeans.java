package ccc.java.kmeans;

import java.util.ArrayList;
import java.util.List;

public class KMeans 
{
	private List<KPointCollection> allClusters = new ArrayList<KPointCollection>();
	private List<List<KPoint>> allGroups;
	private int clusterCount = -1;
	private int iterations = 5;
	private Integer maxVariationX = null;
	private Integer maxVariationY = null;
	private double width;
	private double height;
	private KPointCollection points;
	private KPointCollection altPoints = new KPointCollection();
	
	public KMeans(KPointCollection points, Double x, Double y, Double maxVariationX, Double maxVariationY) throws ClusteringException
	{
		if(maxVariationX != null && maxVariationX < 0 || maxVariationY != null && maxVariationY < 0)
			throw new ClusteringException("maxVariation must be greater than 0 or null");
		if(points == null || points.size() < 1)
			throw new ClusteringException("no points provided");
		this.width = x;
		this.height = y;
		if(maxVariationX != null && maxVariationX >= 0)
			this.maxVariationX = (int) (maxVariationX*width);
		if(maxVariationY != null && maxVariationY >= 0)
			this.maxVariationY = (int) (maxVariationY*height);
		this.points = points;
		allGroups = new ArrayList<List<KPoint>>();
		allGroups.add(points);
		KPointCollection cluster = new KPointCollection();
		cluster.addAll(points);
		cluster.setCentroid(points.get(points.size()/2));
		allClusters.add(cluster);
	}
	   
	public KMeans(KPointCollection points, int clusterCount) throws ClusteringException
	{
		if(clusterCount < 1)
			throw new ClusteringException("cluster count is less than 1");
		if(points == null || points.size() < 1)
			throw new ClusteringException("no points provided");
		this.clusterCount = clusterCount;
		this.points = points;
		
		allGroups = KPointCollection.SplitList(points, clusterCount);
		
	   for(List<KPoint> group : allGroups)
	   {
	   		KPointCollection cluster = new KPointCollection();
	   		cluster.addAll(group);
	   		if(cluster.size()>0)
	   			cluster.setCentroid(cluster.get((int)Math.floor(cluster.size()/2)));
	   		allClusters.add(cluster);
	   }
	}
	
	public List<KPointCollection> GetClusters(ClusterDimension dim) throws ClusteringException
	{
		boolean area1d = false;
		if(dim == ClusterDimension.Xaxis && this.maxVariationX == null)
			throw new ClusteringException("provide maxVariationX for Xaxis-clustering");
		if(dim == ClusterDimension.Yaxis && this.maxVariationY == null)
			throw new ClusteringException("provide maxVariationY for Yaxis-clustering");
		if(dim == ClusterDimension.Area && (this.maxVariationX == null 	|| this.maxVariationY == null))
			throw new ClusteringException("provide maxVariationX and maxVariationY for Area-clustering");
		if(dim == ClusterDimension.Area1d && (this.maxVariationX == null 	|| this.maxVariationY == null))
			throw new ClusteringException("provide maxVariationX and maxVariationY for Area1d-clustering");
		
		if(dim == ClusterDimension.Area1d)
		{
			maxVariationX = maxVariationX*maxVariationY;
			for(KPoint p : points)
			{
				altPoints.add(new KPoint(p.getId(), p.getX()*p.getY(), 0d));
			}
			KPointCollection cluster=new KPointCollection();
			cluster.addAll(altPoints);
			cluster.setCentroid(altPoints.get(points.size()/2));
			allClusters.clear();
			allClusters.add(cluster);
			area1d= true;
			dim = ClusterDimension.Xaxis;
		}
		if(clusterCount > 0)
		{
			for (int i = 0; i < iterations; i++) {
				doClustering();
				for (KPointCollection c : allClusters)
					c.reCalculateCentroidAndMaxDist();
			}
		}
		else
		{
			for (int i = 0; i < iterations; i++) {
				doClustering();
				
				for (int j = allClusters.size()-1; j >= 0; j--)
				{
					allClusters.get(j).reCalculateCentroidAndMaxDist();
					
					if(dim == ClusterDimension.Xaxis && allClusters.get(j).getMaxDistToCentroidX() > this.maxVariationX)
					{
						List<KPointCollection> zw = KPointCollection.SplitColletionByVariation(allClusters.get(j), this.maxVariationX, null);
						allClusters.remove(j);
						addToExistingClusters(zw);
					}
					else if(dim == ClusterDimension.Yaxis && allClusters.get(j).getMaxDistToCentroidY() > this.maxVariationY)					
					{
						List<KPointCollection> zw = KPointCollection.SplitColletionByVariation(allClusters.get(j), null, this.maxVariationY);
						allClusters.remove(j);
						addToExistingClusters(zw);
					}
					else if(dim == ClusterDimension.Area && (allClusters.get(j).getMaxDistToCentroidX() > this.maxVariationX
						|| allClusters.get(j).getMaxDistToCentroidY() > this.maxVariationY))
					{
						List<KPointCollection> zw = KPointCollection.SplitColletionByVariation(allClusters.get(j), this.maxVariationX, this.maxVariationY);
						allClusters.remove(j);
						addToExistingClusters(zw);
					}
				}
			}
		}
		//replace areasize with real x, y values
		if(area1d)
		{
			List<KPointCollection> zz = new ArrayList<KPointCollection>();
			for(KPointCollection coll : allClusters)
			{
				KPointCollection yy = new KPointCollection();
				for(KPoint p : coll)
				{
					KPoint zw = points.GetPointById(p.getId());
					yy.add(zw);
					points.remove(zw);
				}
				yy.reCalculateCentroidAndMaxDist();
				zz.add(yy);
			}
			allClusters = zz;
		}
		return allClusters;
	}
	
	private void addToExistingClusters(List<KPointCollection> list)
	{
		for(KPointCollection coll : list)
		{
			boolean added = false;
			for(KPointCollection l : allClusters)
			{
				if(this.maxVariationX != null && this.maxVariationY == null)
				{
					if(Math.abs(coll.getCentroid().getX() - l.getCentroid().getX()) <= this.maxVariationX)
					{
						l.addAll(coll);
						added = true;
						break;
					}
				}
				else if (this.maxVariationX == null && this.maxVariationY != null)
				{
					if(Math.abs(coll.getCentroid().getY() - l.getCentroid().getY()) <= this.maxVariationY)
					{
						l.addAll(coll);
						added = true;
						break;
					}
				}
				else if	(this.maxVariationX != null && this.maxVariationY != null)	
				{
					if(Math.abs(coll.getCentroid().getX() - l.getCentroid().getX()) <= this.maxVariationX
					&& Math.abs(coll.getCentroid().getY() - l.getCentroid().getY()) <= this.maxVariationY)
					{
						l.addAll(coll);
						added = true;
						break;
					}
				}
			}
			if(!added) //!not
				allClusters.add(coll);
		}
	}
	
	private void doClustering()
	{
	    boolean hasMoved = true;
	    while (hasMoved)
	    {
	    	hasMoved = false;

	        for (KPointCollection cluster : allClusters) //for all clusters
	        {
	            for (int pointIndex = 0; pointIndex < cluster.size(); pointIndex++) //for all points in each cluster
	            {
	                KPoint point = cluster.get(pointIndex);

	                int nearestCluster = FindNearestCluster(allClusters, point);
	                if (cluster != allClusters.get(nearestCluster)) //if point has moved
	                {
	                    if (cluster.size() > 1) //each cluster shall have minimum one point
	                    {
	                        cluster.remove(point);
	                        allClusters.get(nearestCluster).add(point);
	                        hasMoved = true;
	                    }
	                }
	            }
	        }
	    }
	}

	
    private int FindNearestCluster(List<KPointCollection> allClusters, KPoint point)
    {
        double minimumDistance = 0.0;
        int nearestClusterIndex = -1;

        for (int k = 0; k < allClusters.size(); k++) //find nearest cluster
        {
            double distance = KPoint.FindDistance(point, allClusters.get(k).getCentroid());
            if (k == 0)
            {
                minimumDistance = distance;
                nearestClusterIndex = 0;
            }
            else if (minimumDistance > distance)
            {
                minimumDistance = distance;
                nearestClusterIndex = k;
            }
        }

        return (nearestClusterIndex);
    }

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
}
