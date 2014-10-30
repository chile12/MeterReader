package ccc.android.meterreader.helpfuls;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import ccc.android.meterdata.types.Preference;
import ccc.android.meterreader.R;

@SuppressLint("CommitPrefEdits")
public class PreferencesHelper
{
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	public PreferencesHelper(Context context) 
	{
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		PreferenceManager.setDefaultValues(context, R.xml.constants, true);
		this.editor = sharedPreferences.edit(); 
		
		for(String key : sharedPreferences.getAll().keySet())
		{
			if(!(sharedPreferences.getAll().get(key) instanceof Set<?>))
			{
				editor.remove(key);
			}
		}
		//EditPreferences("WS_URL", "http://192.168.2.191/mobilegaugereading");
		editor.commit();
		
//		sharedPreferences.getAll().clear();
//		editor.remove("WS_URL");
//		EditPreferences("WS_URL", "http://192.168.2.191/mobilegaugereading");
//		editor.remove("WS_URL_KEY");
//		editor.remove("LAST_FULL_DOWN_KEY");
//		editor.commit();
	}
    
	public <T> T GetPreferences(String key, T defaultVal)
	{
		T retVal = defaultVal;
		String defVal = defaultVal == null ? null : defaultVal.toString();
		Set<String> result = sharedPreferences.getStringSet(key, new HashSet<String>());
		if(result == null || result.size() == 0)
			return retVal;
		
		try
		{
			String[] zw = result.toArray(new String[2]);
			retVal = (T) Preference.mapper.readValue(zw[0], Class.forName(zw[1]));
		}
		catch (JsonParseException e)
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
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	public void EditPreferences(String key, Object value)
	{
		if(value == null)
			return;
		try
		{
			Set<String> input = new HashSet<String>();
			input.add(Preference.mapper.writeValueAsString(value));
			input.add(Preference.mapper.writeValueAsString(value.getClass()).replace("\"", ""));
			editor.putStringSet(key, input);
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
		editor.commit();
	}
	
	public void remove(String key)
	{
		editor.remove(key);
		editor.commit();
	}

	public SharedPreferences getSharedPreferences()
	{
		return sharedPreferences;
	}

	public Editor getEditor()
	{
		return editor;
	}
}  
