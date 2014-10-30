package ccc.android.meterdata.listtypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.interfaces.IGenericPartialList;
import ccc.android.meterdata.types.Image;

public class ImageList implements IGenericPartialList<Image>, Iterable<Image> 
{
	private List<Image> imageList = new ArrayList<Image>();
	private String restriction;
	private int imageType; //0 == icon, 1 == meterImage
	private int skip;
	private int limit;
	private int size;
	
	@JsonProperty("ImageList")
	public List<? extends Image> getImageList() {
		return imageList;
	}
	@JsonProperty("ImageList")
	public void setImageList(List<? extends Image> stationList) {
		this.imageList = (List<Image>) stationList;
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
	@JsonIgnore
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
	
	@JsonIgnore
	public Image getByCaption(String caption)
	{
		for(Image ga : this.getImageList())
		{
			if(ga.getCaption().toLowerCase().trim().equals(caption.toLowerCase().trim()))
				return ga;
		}
		return null;
	}
	
	@JsonIgnore
	public Image getByMedium(String medium)
	{
		for(Image ga : this.getImageList())
		{
			if(ga.getMedium() == null)
				continue;
			if(ga.getMedium().toLowerCase().trim().equals(medium.toLowerCase().trim()))
				return ga;
		}
		return null;
	}
	
	@JsonIgnore
	public Image getByDevice(int deId)
	{
		for(Image ga : this.getImageList())
		{
			if(ga.getGaugeDeviceId() == null)
				continue;
			if(ga.getGaugeDeviceId().intValue() == deId )
				return ga;
		}
		return null;
	}

	@Override
	public void clear() 
	{
		imageList.clear();
	}
	@Override
	public int getSkip()
	{
		return skip;
	}
	@Override
	public void setSkip(int skip)
	{
		this.skip = skip;
	}
	@Override
	public int getLimit()
	{
		return limit;
	}
	@Override
	public void setLimit(int limit)
	{
		this.limit = limit;
	}
	@Override
	public int getSize()
	{
		return size;
	}
	@Override
	public void setSize(int size)
	{
		this.size = size;
	}
	public int getImageType()
	{
		return imageType;
	}
	public void setImageType(int imageType)
	{
		this.imageType = imageType;
	}
	@Override
	public Image get(int index)
	{
		return this.getImageList().get(index);
	}
	@Override
	@JsonIgnore
	//TODO remove ignore!
	public List<Image> getList()
	{
		return this.imageList;
	}
	@Override
	public void add(Image addi)
	{
		this.imageList.add(addi);
	}
	@Override
	public void addAll(IGenericMemberList<Image> list)
	{
		this.imageList.addAll(list.getList());
	}
}
