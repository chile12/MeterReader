package ccc.android.meterreader.datamanagement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressLint("CommitPrefEdits")
public class PreferencesHelper 
{
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	public PreferencesHelper(Context context) 
	{
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.editor = sharedPreferences.edit(); 
	}
	
	public <T> T GetPreferences(String key, Class<T> type)
	{
		if (type == int.class)
			return (T)((Integer)sharedPreferences.getInt(key, 0));
		else if (type == boolean.class)
			return (T)(Boolean)sharedPreferences.getBoolean(key, true);
		else if (type == float.class)
			return (T)(Float)sharedPreferences.getFloat(key, (Float) 0f);
		else if (type == long.class)
			return (T)(Long) sharedPreferences.getLong(key, (Long) 0L);
		else if (type == String.class)
			return (T)(String) sharedPreferences.getString(key, (String) "");
		else
			return null;
	}
	
	public void EditPreferences(String key, Object value) throws Exception
	{
		if(value.getClass() == int.class)
			editor.putInt(key, (Integer)value);
		else if(value.getClass() == boolean.class)
			editor.putBoolean(key, (Boolean)value);
		else if(value.getClass() == float.class)
			editor.putFloat(key, (Float)value);
		else if(value.getClass() == String.class)
			editor.putString(key, (String)value);
		else if(value.getClass() == long.class)
			editor.putLong(key, (Long)value);
		else
			throw new Exception("object type not supported for preferences");
		editor.commit();
	}
}  
