package ccc.android.meterreader.helpfuls;

import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import ccc.android.meterdata.OfficialNaming;
import ccc.android.meterdata.enums.GaugeType;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.datamanagement.DataContext;
import ccc.android.meterreader.internaldata.GroupIdentifier;
import ccc.android.meterreader.internaldata.InternalImage;
import ccc.android.meterreader.internaldata.ListItemIdentifier;
import ccc.android.meterreader.statics.StaticIconLibrary;
import ccc.android.meterreader.statics.Statics;

@SuppressLint({ "DefaultLocale", "UseSparseArrays" })
public class EpviMeterListMapper
{
	private DataContext data;
	private String[] groupKeys;
	private String[] filterKeys;
	int currentGroupKey = 0;
	private Map<Integer, Entry<GroupIdentifier, List<Gauge>>> groupDict;			//Position in List - GroupIdentifier (e.g. Medium) - List of Gauges
	private Map<Integer, List<Boolean>> childFilterMap;		//indicates if child at a given position is filtered out by current filter
	private Map<Integer, Reading> lastReadings;										//GaugeId  - last Reading of this Gauge
	private Map<Integer, Boolean> viewExpanded;										//ViewId - is expanded View?
	private Map<Integer, Entry<Gauge, Reading>> viewDict;							//ViewId - <Gauge, last Reading>
	
	public EpviMeterListMapper(DataContext context)
	{
		Resources resources = Statics.getLocalResources("us");
		groupKeys = resources.getStringArray(ccc.android.meterreader.R.array.group_array);
		filterKeys = resources.getStringArray(ccc.android.meterreader.R.array.filter_array);
		
		this.data = context;
		groupDict = new HashMap<Integer, Entry<GroupIdentifier, List<Gauge>>>();
		lastReadings = new HashMap<Integer, Reading>();
		viewDict = new HashMap<Integer, Entry<Gauge, Reading>>();
		viewExpanded = new HashMap<Integer, Boolean>();
		childFilterMap = new HashMap<Integer, List<Boolean>>();
	}

	public void DoDataMapping(long groupKey)
	{
		currentGroupKey = Integer.parseInt(String.valueOf(groupKey));
		lastReadings.clear();
		groupDict.clear();
		for(Reading r : data.getReadings())
		{
				Date to = null;
				if(lastReadings.get(r.getGaugeId()) != null)
					to = lastReadings.get(r.getGaugeId()).getUtcTo();
				if(to == null || r.getUtcTo().after(to))
				{
					lastReadings.put(r.getGaugeId(), r);
				}
		}
		for(Gauge st : data.getGauges())
		{
			if(st.getGaugeType() == GaugeType.Manual)
			{ 
				if(getGroupGaugeList(st) == null)
				{
					groupDict.put(groupDict.size(), (Entry<GroupIdentifier, List<Gauge>>)new SimpleEntry<GroupIdentifier, List<Gauge>>(getGroupIdentifier(st), new ArrayList<Gauge>()));
				}
				addToGroupGaugeList(st);
			}
		}
	}
	
	private GroupIdentifier getGroupIdentifier(Gauge ga)
	{
		if(groupKeys[currentGroupKey].equals("medium"))
			return new GroupIdentifier(ga.getMedium(), (InternalImage) StaticIconLibrary.getIcons().getByMedium(ga.getMedium()));
		if(groupKeys[currentGroupKey].equals("name"))
		{
			String ret = prettyUp(ga.getName(), 0,1);
			return new GroupIdentifier(ret.toUpperCase());
		}
		if(groupKeys[currentGroupKey].equals("location"))
		{
			String ret = prettyUp(ga.getLocation(), 0,12);
			return new GroupIdentifier(ret);
		}
		if(groupKeys[currentGroupKey].equals("description"))
		{
			String ret = prettyUp(ga.getDescription(), 0,1);
			return new GroupIdentifier(ret.toUpperCase());
		}
		if(groupKeys[currentGroupKey].equals("last reading date"))
		{
			Reading ra = getLastReadingFromGaugeId(ga.getGaugeId());
			if(ra != null)
				return new GroupIdentifier(Statics.getLocaleDateFormat().format(ra.getUtcTo()));
			return new GroupIdentifier("--");
		}
		if(groupKeys[currentGroupKey].equals("days since reading"))
		{
			Reading ra = getLastReadingFromGaugeId(ga.getGaugeId());
			if(ra != null)
				return new GroupIdentifier(String.valueOf(Statics.getDateDiff(ra.getUtcTo(), Statics.getUtcTime(), TimeUnit.DAYS)));
			return new GroupIdentifier("--");
		}
		return null;
	}
	
	private String prettyUp(String input, int start, int end)
	{
		if(input == null || input == "")
			return "";
		input = input.toLowerCase();
		return input.substring(start, Math.min(end, input.length()));
	}
	
	public void ResetFilter()
	{
		childFilterMap = new HashMap<Integer, List<Boolean>>();
	}
	
	public void SetNewFilter(int filterSubjectKey, String filterTerm)
	{
		String subject = filterKeys[filterSubjectKey];
		SetNewFilter(subject, filterTerm);
	}
	
	public void SetNewFilter(String filterSubject, String filterTerm)
	{
		
		childFilterMap.clear();
		for(Integer group : groupDict.keySet())
		{
			childFilterMap.put(group, new ArrayList<Boolean>());
			for(Gauge ga : getGroupGaugeList(group))
			{
				String subject = getFilterSubject(filterSubject, ga);
				if(subject == null)
				{
					subject = getFilterSubject(filterSubject, getLastReadingFromGaugeId(ga.getGaugeId()));
				}
				if(subject != null)
				{
					if(filterTerm == null || filterTerm == "" 
							|| subject != null && subject.toLowerCase().contains(filterTerm.toLowerCase()))
						childFilterMap.get(group).add(true);
					else
						childFilterMap.get(group).add(false);
				}
			}
		}
	}
	
	private String getFilterSubject(String subject, Object object)
	{
		if(subject == "" || object == null)
			return null;
		try {
			Field[] fields = object.getClass().getDeclaredFields();
			for(Field f : fields)
			{
				f.setAccessible(true);
				OfficialNaming anno = f.getAnnotation(OfficialNaming.class);
				String test = anno.Field();
				if(anno != null && test.equals(subject))
				{
					Object zw = f.get(object);
					return String.valueOf(zw);
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void addToGroupGaugeList(Gauge ga)
	{
		GroupIdentifier gId = getGroupIdentifier(ga);
		for(int i =0;i<groupDict.size();i++) 
		{
			if(gId.compare(groupDict.get(i).getKey()))
			{
				groupDict.get(i).getValue().add(ga);
				return;
			}
		}
	}

	public List<Gauge> getGroupGaugeList(Gauge ga)
	{
		GroupIdentifier gId = getGroupIdentifier(ga);
		for(int i =0;i<groupDict.size();i++) 
		{
			if(gId.compare(groupDict.get(i).getKey()))
				return getGroupGaugeList(i);
		}
		return null;
	}
	
	public List<Gauge> getGroupGaugeList(Integer pos)
	{
		List<Gauge> filtered = new ArrayList<Gauge>();
		List<Gauge> base = groupDict.get(pos).getValue();
		if(childFilterMap.size() == 0)
			return base;
		
		for(int i =0; i < base.size();i++)
		{
			if(childFilterMap.get(pos).size()<=i || childFilterMap.get(pos).get(i))
				filtered.add(base.get(i));
		}
		return filtered;
	}
	
	public int getGroupCount()
	{
		return groupDict.size();
	}
	
	public GroupIdentifier getGroupIdentifier(Integer pos)
	{
		if(childFilterMap.size() == 0) 
			return groupDict.get(pos).getKey();
		if(getGroupGaugeList(pos).size()>0)
			return groupDict.get(pos).getKey();
		return null;
	}
	
	public Reading getLastReadingFromGaugeId(int gaugeId)
	{
		return lastReadings.get(gaugeId);
	}
	
	public Boolean isChildViewExpanded(int viewId)
	{
		return viewExpanded.get(viewId);
	}
	
	public void setChildViewExpanded(Integer viewId, Boolean exp)
	{
		viewExpanded.put(viewId, exp);
	}
	
	public void resetChildViewExpanded()
	{
		for(Integer ent : viewExpanded.keySet())
			viewExpanded.put(ent, false);
	}


	public Gauge getGaugeForViewId(int id)
	{
		try {
			return viewDict.get(id).getKey();
		} catch (Exception e) {
			return null;
		}
	}
	
	public Reading getReadingForViewId(int id)
	{
		try {
			return viewDict.get(id).getValue();
		} catch (Exception e) {
			return null;
		}
	}
	
	public void setGaugeReadingForViewId(int id, Gauge ga, Reading ra)
	{
		viewDict.put(id, (Entry<Gauge, Reading>)new SimpleEntry<Gauge, Reading>(ga,ra));
	}
	
	public ListItemIdentifier getListItemIdentifier (int groupPos, int childPos)
	{
		Gauge gauge = getGroupGaugeList(groupPos).get(childPos);
		Reading reading = getLastReadingFromGaugeId(gauge.getGaugeId());
		ListItemIdentifier id = new ListItemIdentifier();
		
		id.setTitle(gauge.getName());
		id.setBelowTitleLeft(gauge.getDescription());
		id.setAboveBottomLeft(gauge.getLocation());
		if(reading != null)
		{
			id.setBottomLeft(String.valueOf(reading.getRead()) + " " + gauge.getUnit().getName());
			id.setBottomRight(Statics.getLocaleDateFormat().format(reading.getUtcTo()));
		}

		return id;
	}
}
