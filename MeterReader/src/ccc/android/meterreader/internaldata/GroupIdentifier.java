package ccc.android.meterreader.internaldata;

import android.app.Activity;

public class GroupIdentifier
{
	private String title;
	private Integer iconId;
		
	public GroupIdentifier(String title)
	{
		this.title = title;
	}
	
	public GroupIdentifier(String title, String drawableName, Activity context)
	{
		this.title = title;
        try {
			this.iconId = (Integer) context.getResources().getIdentifier(drawableName,"drawable", context.getPackageName());
		} catch (Exception e) {
			this.iconId = -1;
		}
        if(this.iconId < 0)
        	this.iconId = (Integer) context.getResources().getIdentifier("counter.png","drawable", context.getPackageName());
	}
	
	public boolean compare(GroupIdentifier comparee)
	{
		boolean zw = true;
		if(!this.GetTitle().equals(comparee.GetTitle()))
			return false;
		if(!this.GetImage().equals(comparee.GetImage()))
			return false;
		return zw;
	}
	
	public String GetTitle() {
		return title;
	}

	public Integer GetImage() {
		if(iconId == null)
			return null;
		return iconId;
	}

}
