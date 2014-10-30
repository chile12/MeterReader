package ccc.android.meterreader.internaldata;

import ccc.android.meterdata.types.Image;

public class GroupIdentifier
{
	private String title;
	private InternalImage icon;
		
	public GroupIdentifier(String title)
	{
		this.title = title;
	}
	
	public GroupIdentifier(String title, InternalImage icon)
	{
		this.title = title;
		this.icon = icon;
//        try {
//			this.icon = (Integer) context.getResources().getIdentifier(drawableName,"drawable", context.getPackageName());
//		} catch (Exception e) {
//			this.iconId = -1;
//		}
//        if(this.iconId < 0)
//        	this.iconId = (Integer) context.getResources().getIdentifier("counter.png","drawable", context.getPackageName());
	}
	
	public boolean compare(GroupIdentifier comparee)
	{
		if(!this.GetTitle().equals(comparee.GetTitle()))
			return false;
		return true;
	}
	
	public String GetTitle() {
		return title;
	}

	public Image getIcon()
	{
		return icon;
	}

}
