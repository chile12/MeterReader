package ccc.android.meterreader.helpfuls;

import java.util.Date;

import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.R;
import ccc.android.meterreader.statics.Statics;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;

@SuppressLint("CommitPrefEdits")
public class PreferencesHelper// extends PreferenceFragment
{
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	public PreferencesHelper(Context context) 
	{
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		PreferenceManager.setDefaultValues(context, R.xml.constants, true);
		this.editor = sharedPreferences.edit(); 
	}
	
//    @Override
//    public void onCreate(Bundle savedInstanceState) 
//    {
//        super.onCreate(savedInstanceState);
////        getPreferenceManager().setSharedPreferencesName("constants");
////        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
//		PreferenceManager.setDefaultValues(this.getActivity(), R.xml.constants, false);
//        addPreferencesFromResource(R.xml.constants);
////		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
////		//PreferenceManager.setDefaultValues(this.getActivity().getApplicationContext(), "constants", Context.MODE_MULTI_PROCESS, R.xml.constants, false);
////		this.editor = sharedPreferences.edit(); 
//    }
    
	@SuppressWarnings("unchecked")
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
	
	public void EditPreferences(String key, Object value)
	{
		if(value.getClass() == Integer.class)
			editor.putInt(key, ((Integer)value).intValue());
		else if(value.getClass() == Boolean.class)
			editor.putBoolean(key, ((Boolean)value).booleanValue());
		else if(value.getClass() == Float.class)
			editor.putFloat(key, ((Float)value).floatValue());
		else if(value.getClass() == String.class)
			editor.putString(key, (String)value);
		else if(value.getClass() == Long.class)
			editor.putLong(key, ((Long)value).longValue());
		else
			editor.putString(key, (String)value.toString());
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
