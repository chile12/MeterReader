package ccc.android.meterreader.internaldata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import ccc.android.meterdata.errors.RestError;
import ccc.android.meterdata.interfaces.IGenericMemberList;
import ccc.android.meterdata.listtypes.ImageList;
import ccc.android.meterdata.types.Image;
@JsonTypeName("InternalStationList")
public class InternalImageList  extends ccc.android.meterdata.listtypes.ImageList implements ICallbackList
{
	private IMeterDataContainer container = null;
	private boolean isLoaded = false;
	private boolean isImageList = false;
	private List<InternalImage> internalList = new ArrayList<InternalImage>();

	@JsonIgnore
	public boolean isLoaded()
	{
		return isLoaded;
	}
	@JsonIgnore
	public void setIsLoaded(boolean loaded)
	{
		isLoaded = loaded;
	}
	
	public InternalImageList()
	{super();}
	public InternalImageList(IMeterDataContainer manager)
	{
		super();
		container = manager;
	}
	
	@Override
	public void ListCallback(IGenericMemberList list) 
	{
		if(list != null)
		{
			this.setImageList(((ImageList) list).getImageList());
			this.setImageType(((ImageList) list).getImageType());
			this.setSize(((ImageList) list).getSize());
			this.setIsLoaded(true);
			container.RegisterLoadedDataObject(this);
		}
	}
	
	@Override
	public void ErrorCallback(RestError error) {
		container.ReceiveErrorObject(error);
		
	}	
	
	@JsonIgnore
	@Override
	public IMeterDataContainer getDataContainer()
	{
		return container;
	}
	@JsonIgnore
	@Override
	public ICallbackList setDataContainer(IMeterDataContainer container)
	{
		this.container = container;
		return this;
	}
	@JsonIgnore
	public boolean isImageList()
	{
		return isImageList;
	}
	@JsonIgnore
	public void setIsImageList(boolean isImageList)
	{
		this.isImageList = isImageList;
	}
	
	@Override
	@JsonProperty("ImageList")
	public List<? extends Image> getImageList()
	{
		return internalList;
	}
	@Override
	@JsonProperty("ImageList")
	public void setImageList(List<? extends Image> imgList)
	{
		for(int i =0; i < imgList.size(); i++)
			if(imgList.get(i) instanceof InternalImage)
				internalList.add((InternalImage) imgList.get(i));
			else
			{
				internalList.add(new InternalImage(imgList.get(i)));
				imgList.get(i).setBinary(null);
				imgList.set(i, null);
			}
				
	}
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Image> iterator()
	{
		return ((List<Image>)(List<?>)internalList).iterator();
	}
	@Override
	public int size()
	{
		return internalList.size();
	}
	@Override
	public void clear()
	{
		internalList = new ArrayList<InternalImage>();
	}
}