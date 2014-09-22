package ccc.android.meterreader.gaugedisplaydialog;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import ccc.android.meterreader.statics.Statics;

import android.app.Activity;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class MeterReaderKeyboardActionListener implements KeyboardView.OnKeyboardActionListener 
{
	private GaugeDisplayDialog activity;
	private EditText editText;
	private Editable editable;
	private Date clearPressed;
	
	public MeterReaderKeyboardActionListener(GaugeDisplayDialog activity, EditText editText)
	{
		this.activity = activity;
		this.editText = editText;
	}
	
	@Override
	public void onKey(int primaryCode, int[] keyCodes) 
	{
	    View focusCurrent = activity.getWindow().getCurrentFocus();
	    if( focusCurrent==null || focusCurrent.getClass()!=EditText.class ) 
	    	return;
	    editText = (EditText) focusCurrent;
	    editable = editText.getText();
	    int start = editText.getSelectionStart();
	    		
		if(primaryCode == KeyEvent.FLAG_EDITOR_ACTION)
			activity.sendNewRead();
		
		if(primaryCode == KeyEvent.KEYCODE_BACK && editable.length() > 0)
			editable.replace(editable.length()-1, editable.length(), "" , 0, 0);
				
		if(primaryCode == KeyEvent.KEYCODE_MINUS)
			editable.replace(editable.length(), editable.length(), "-" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_COMMA)
			editable.replace(editable.length(), editable.length(), "," , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_0)
			editable.replace(editable.length(), editable.length(), "0" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_1)
			editable.replace(editable.length(), editable.length(), "1" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_2)
			editable.replace(editable.length(), editable.length(), "2" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_3)
			editable.replace(editable.length(), editable.length(), "3" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_4)
			editable.replace(editable.length(), editable.length(), "4" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_5)
			editable.replace(editable.length(), editable.length(), "5" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_6)
			editable.replace(editable.length(), editable.length(), "6" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_7)
			editable.replace(editable.length(), editable.length(), "7" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_8)
			editable.replace(editable.length(), editable.length(), "8" , 0, 1);
		
		if(primaryCode == KeyEvent.KEYCODE_9)
			editable.replace(editable.length(), editable.length(), "9" , 0, 1);
	}

	@Override
	public void onPress(int primaryCode) 
	{
		if(primaryCode == KeyEvent.KEYCODE_CLEAR)
			clearPressed = new Date();
	}

	@Override
	public void onRelease(int primaryCode) 
	{
		if(primaryCode == KeyEvent.KEYCODE_CLEAR)
			if(clearPressed != null && Statics.getDateDiff(clearPressed, new Date(), TimeUnit.MICROSECONDS) >= 1000)
				editable.replace(0, editable.length(), "" , 0, 0);
	}

	@Override
	public void onText(CharSequence text) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub
		
	}
}
