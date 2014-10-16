package ccc.android.meterreader.gaugedisplaydialog;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import ccc.android.meterreader.statics.Statics;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MeterReaderKeyboard 
{
	private Keyboard mKeyboard;
    private KeyboardView mKeyboardView;
    private GaugeDisplayDialog  activity;
    private EditText edittext = null;
    

    public MeterReaderKeyboard(GaugeDisplayDialog activity, int keyboardId, int keyboardViewId) 
    {
        this.activity = activity;
        
	    // Create the Keyboard
	    mKeyboard= new Keyboard(activity, keyboardId);
	    // Lookup the KeyboardView
	    mKeyboardView= (KeyboardView)activity.findViewById(keyboardViewId);
	    // Attach the keyboard to the view
	    mKeyboardView.setKeyboard( mKeyboard );
	    // Do not show the preview balloons
	    mKeyboardView.setPreviewEnabled(false);
	    mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
    }

    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() 
    {
    	private Date clearPressed;
    	private Editable editable;
    	    	
    	@Override
    	public void onKey(int primaryCode, int[] keyCodes) 
    	{
    		if(edittext != null)
    		{
	    	    
	    	    editable = edittext.getText();
	    	    		
	    		if(primaryCode == KeyEvent.FLAG_EDITOR_ACTION)
	    			activity.sendNewRead();
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_BACK && editable.length() > 0)
	    			editable.replace(editable.length()-1, editable.length(), "" , 0, 0);
	    				
	    		if(primaryCode == KeyEvent.KEYCODE_MINUS)
	    			editable.replace(editable.length(), editable.length(), "-" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_COMMA)
	    			editable.replace(editable.length(), editable.length(), "," , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_0)
	    			editable.replace(editable.length(), editable.length(), "0" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_1)
	    			editable.replace(editable.length(), editable.length(), "1" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_2)
	    			editable.replace(editable.length(), editable.length(), "2" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_3)
	    			editable.replace(editable.length(), editable.length(), "3" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_4)
	    			editable.replace(editable.length(), editable.length(), "4" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_5)
	    			editable.replace(editable.length(), editable.length(), "5" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_6)
	    			editable.replace(editable.length(), editable.length(), "6" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_7)
	    			editable.replace(editable.length(), editable.length(), "7" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_8)
	    			editable.replace(editable.length(), editable.length(), "8" , 0, 1);
	    		
	    		if(primaryCode == KeyEvent.KEYCODE_NUMPAD_9)
	    			editable.replace(editable.length(), editable.length(), "9" , 0, 1);
    		}
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
		public void onText(CharSequence arg0) {
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
    };

    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void showCustomKeyboard( View v ) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if( v!=null ) 
        	((InputMethodManager)activity.getSystemService(GaugeDisplayDialog.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    public void registerEditText(final EditText edittext) 
    {
        //edittext.setInputType(InputType.TYPE_NULL);
        // Make the custom keyboard appear
    	this.edittext = edittext;
        edittext.setOnFocusChangeListener(
        	new OnFocusChangeListener() 
        	{
            @Override public void onFocusChange(View v, boolean hasFocus) {
//                if( hasFocus ) 
//                	showCustomKeyboard(v); 
//                else 
//                {
                	edittext.requestFocusFromTouch();
 //               }
            }
        });
        edittext.setOnClickListener(
        	new OnClickListener() 
        	{
            @Override public void onClick(View v) {
                //showCustomKeyboard(v);
            }
        });
        // Disable standard keyboard hard way
        edittext.setOnTouchListener(
        	new OnTouchListener() 
        	{
            @Override public boolean onTouch(View v, MotionEvent event) 
            {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType( edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS );
    }

//	public KeyboardView getmKeyboardView() {
//		return mKeyboardView;
//	}
	
	public int getKeyboardHeight()
	{
		return mKeyboard.getHeight();
	}
}
