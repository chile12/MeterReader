package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.types.Image;
import ccc.android.meterdata.types.Station;

public class ImageList implements IGenericMemberList,  Iterable<Image> 
{
	private List<Image> imageList = new ArrayList<Image>();
	private String restriction;
	
	public List<Image> getImageList() {
		return imageList;
	}

	public void setImageList(List<Image> stationList) {
		this.imageList = stationList;
	}

	@Override
	public Iterator<Image> iterator() {
		return imageList.iterator();
	}

	@Override
	@JsonIgnore
	public String getRestriction() {
		return restriction;
	}

	@Override
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
	
	public int size()
	{
		return imageList.size();
	}
	
	@Override
	public Image getById(Object id) {
		if(!(id.getClass().equals(Integer.class) || id.getClass().equals(int.class)))
			return null;
		for(Image ga : this.getImageList())
		{
			if(ga.getId() == ((Integer)id).intValue())
				return ga;
		}
		return null;
	}

	@Override
	public void clear() 
	{
		imageList.clear();
	}
}
