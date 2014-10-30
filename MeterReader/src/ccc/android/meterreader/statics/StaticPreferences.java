package ccc.android.meterreader.statics;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import ccc.android.meterdata.listtypes.PreferenceList;
import ccc.android.meterdata.types.Preference;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.helpfuls.PreferencesHelper;

public class StaticPreferences
{
	protected static PreferenceList prefs;
    private static PreferencesHelper helper;

	public static PreferenceList getPreferences()
	{
		return prefs;
	}

	public static void setPreferences(PreferenceList prefs)
	{
		StaticPreferences.prefs = prefs;
		StaticPreferences.helper = new PreferencesHelper(MainActivity.ApplicationContext);
		synchronizePreferences(prefs);
	}

	private static void synchronizePreferences(PreferenceList prefs)
	{
		for(String k : helper.getSharedPreferences().getAll().keySet())
		{
			if(prefs.getById(k) == null)
			{
				try
				{
					StaticPreferences.prefs.getPreferenceList().add(new Preference(k, helper.GetPreferences(k, null), false));
				}
				catch (JsonGenerationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (JsonMappingException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for(Preference p : prefs.getPreferenceList())
		{
			StaticPreferences.helper.EditPreferences(p.getKey(), p.getTypedValue(null));
		}
	}
	
	public static <T> T getPreference(String key, T defaultVal)
	{
		Class<T> cl = (Class<T>) String.class;
		if(defaultVal != null)
			cl = (Class<T>) defaultVal.getClass();
		T zw = StaticPreferences.helper.GetPreferences(key, defaultVal);
		if(zw == null)
			zw = defaultVal;
		return zw;
	}
	
	public static <T> void setPreference(String key, T value)
	{
		StaticPreferences.helper.EditPreferences(key, value);
		boolean global = false;
		Preference p = (Preference) prefs.getById(key);
		if(p != null)
		{
			global = p.isGlobal();
			helper.remove(key);
		}
		try
		{
			p = new Preference(key, value, global);
		}
		catch (JsonGenerationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JsonMappingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		prefs.getPreferenceList().add(p);
	}
}
