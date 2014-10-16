package ccc.android.meterreader.viewelements;

import java.util.*;

import ccc.android.meterdata.*;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.R;
import ccc.android.meterreader.helpfuls.EpviMeterListMapper;
import ccc.android.meterreader.internaldata.GroupIdentifier;
import ccc.android.meterreader.internaldata.ListItemIdentifier;
import ccc.android.meterreader.statics.Statics;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.*;
import android.widget.*;

public class EpviExpandeableListViewAdapter  extends BaseExpandableListAdapter
{
	private Activity context;
	private Resources res;
	private EpviMeterListMapper mapper;
	
	public EpviExpandeableListViewAdapter(Activity context) 
	{
		this.context = context;
		this.res = context.getResources();
	}

	public void setMapper(EpviMeterListMapper mapper, int goupkey) {
		this.mapper = mapper;
		this.mapper.DoDataMapping(goupkey);
	}
	
	public void setMapper(EpviMeterListMapper mapper) {
		this.mapper = mapper;
	}
		
	private Object getResource(String viewId, String resourceType)
	{
		return res.getIdentifier(viewId, resourceType, context.getPackageName());
	}
	
	private void setAllText(View parent, int[] ids, ListItemIdentifier values)
	{
		for(int i=0; i<ids.length;i++)
		{
			setSimpleText(parent, ids[i], values.getFieldByNumber(i));
		}
	}

	private void setSimpleText(View parent, int id, Object value) {
		Object zz = parent.findViewById(id);
		if (zz.getClass() == TextView.class) {
			TextView child = (TextView) zz;
			String zw = "";
			try {
				zw = String.valueOf(value);
				if (zw == "null")
					zw = "";
				Statics.SetText(child, zw);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public Gauge getChild(int group, int child) 
	{
		return (Gauge) mapper.getGroupGaugeList(group).get(child);
	}

	@Override
	public long getChildId(int group, int child) 
	{
		Gauge gauge = getChild(group, child);
		return gauge.getGaugeId();
	}

	@Override
	public View getChildView(int group, int child, boolean isLastChild, View convertView, ViewGroup parent) 
	{
		Gauge gauge = getChild(group, child);
		Reading lastRead = (Reading) mapper.getLastReadingFromGaugeId(gauge.getGaugeId());
		ListItemIdentifier listItem = mapper.getListItemIdentifier(group, child);

		int viewId = gauge.getGaugeId();
		boolean isExpanded = GetExpandedStateFormViewId(viewId);
		mapper.setGaugeReadingForViewId(viewId, gauge,lastRead);
		LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(isExpanded)
			convertView = infalInflater.inflate(R.layout.gaugelistitemexpanded, null);
		else
			convertView = infalInflater.inflate(R.layout.gaugelistitem, null);

		mapper.setChildViewExpanded(viewId, isExpanded);
		convertView.setId(viewId);
		if(!isExpanded) //not!
		{
			int[] ids = new int[2];
			ids[0] =  R.id.gaugeTitleLA;
			ids[1] =  R.id.gaugeLastReadLA;
			setAllText(convertView, ids, listItem);

	        ImageView imgListChild = (ImageView) convertView.findViewById(R.id.gaugeListItemImage);
	        imgListChild.setFocusable(false);
			imgListChild.setImageResource(context.getResources().getIdentifier(gauge.getMedium().toLowerCase(),"drawable", context.getPackageName()));
		}
		else
		{
			int[] ids = new int[8];
			ids[0] =  R.id.gaugeTitleExLA;
			ids[1] =  R.id.gaugeSubTItleExLA;
			ids[2] =  R.id.gaugeBelowTitleLeftExLA;
			ids[3] =  R.id.gaugeBelowTitleRightExLA;
			ids[4] =  R.id.gaugeAboveBottomLeftExLA;
			ids[5] =  R.id.gaugeAboveBottomLRightExLA;
			ids[6] =  R.id.gaugeBottomLeftExLA;
			ids[7] =  R.id.gaugeBottomRightExLA;
			setAllText(convertView, ids, listItem);
			
	        ImageView imgListChild = (ImageView) convertView.findViewById(R.id.gaugeListItemExImage);
	        imgListChild.setFocusable(false);
			imgListChild.setImageResource(context.getResources().getIdentifier(gauge.getMedium().toLowerCase(),"drawable", context.getPackageName()));
				
		}
		
		
		//set background by number of days since last reading
		if(lastRead == null || lastRead.getUtcTo() == null || Statics.daysSince(lastRead.getUtcTo()) >= Statics.DAYS_FOR_WARNING_COLOR_RED)
			((LinearLayout) convertView).getChildAt(0).setBackgroundColor(Statics.getDefaultResources().getColor(R.color.OrangeBack));
//		else if(Statics.daysSince(lastRead.getUtcTo()) >= Statics.DAYSTOYEL)
//			((LinearLayout) convertView).getChildAt(0).setBackgroundColor(Statics.getDefaultResources().getColor(R.color.YellowBack));
		else
			((LinearLayout) convertView).getChildAt(0).setBackgroundColor(Statics.getDefaultResources().getColor(R.color.WhiteSmoke));
		
		return convertView;
	}
	
	public Gauge GetGaugeFormViewId(int id)
	{
		return mapper.getGaugeForViewId(id);
	}
	
	public boolean GetExpandedStateFormViewId(Integer id)
	{
		if(id == null)
			return false;
		if(mapper.isChildViewExpanded(id) == null)
			return false;
		return mapper.isChildViewExpanded(id);
	}
	
	public void SetExpandedStateFormViewId(Integer id, Boolean state)
	{
		mapper.setChildViewExpanded(id, state);
	}
	
	public Reading GetReadingFromViewId(int id)
	{
		return mapper.getReadingForViewId(id);
	}

	@Override
	public int getChildrenCount(int group) 
	{
		return mapper.getGroupGaugeList(group).size();
	}

	@Override
	public List<Gauge> getGroup(int group) 
	{
		return mapper.getGroupGaugeList(group);
	}

	@Override
	public int getGroupCount() 
	{
		return mapper.getGroupCount();
	}

	@Override
	public long getGroupId(int group) 
	{
		return group;
	}

	@Override
	public View getGroupView(int group, boolean isExpanded, View convertView, ViewGroup parent) 
	{
        LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = infalInflater.inflate(R.layout.stationgroupitem, null);
            
        GroupIdentifier id = mapper.getGroupIdentifier(group);
		if (id != null) {
			setSimpleText(convertView, R.id.groupLA, id.GetTitle());
			ImageView imgListChild = (ImageView) convertView.findViewById(R.id.groupIcon);
			imgListChild.setFocusable(false);
			imgListChild.setImageResource(mapper.getGroupIdentifier(group).GetImage());
			return convertView;
		}
		else
		{
			return infalInflater.inflate(R.layout.emptygrouplayout, null);
		}
	}

	@Override
	public boolean hasStableIds() 
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int group, int child) 
	{
		return true;
	}

	public EpviMeterListMapper getMapper() {
		return mapper;
	}

}
