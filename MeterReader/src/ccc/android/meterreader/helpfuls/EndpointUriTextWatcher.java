package ccc.android.meterreader.helpfuls;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

public class EndpointUriTextWatcher implements TextWatcher {

	@Override
	public void afterTextChanged(Editable s) {

    	s.setFilters(new InputFilter[]{});
//    	char[] zw = new char[s.length()];
//    	s.getChars(0, s.length(), zw, 0);
//    	String ss = new String(zw);
//    	if(ss.contains(" "))
//    	{
//    		s.
//    		ss = ss.replace(" ", "");
//    		s.replace(0,  s.length(),  ss, 0, ss.length());
//    	}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		
	}

}
