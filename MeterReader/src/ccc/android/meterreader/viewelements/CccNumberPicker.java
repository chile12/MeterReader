package ccc.android.meterreader.viewelements;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.NumberPicker;

public class CccNumberPicker extends NumberPicker 
{

	public CccNumberPicker(Context context) {
		super(context);
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) 
//	{
//
//	    switch (event.getAction() & MotionEvent.ACTION_MASK) {
//	        case MotionEvent.ACTION_DOWN:
//	        {
//	        	NumPickerTextWatcher.setActive(false);
//	            break;
//	        }
//
//	        case MotionEvent.ACTION_UP:
//	        {
//	        	NumPickerTextWatcher.SetActivateTimer(300);
//	            break;
//	        }
//
//	    }
//	    return false;
//	}

}
