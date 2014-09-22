package ccc.android.meterreader.gaugedisplaydialog;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

public class NumPickerTextWatcher implements TextWatcher{

	private static AtomicBoolean isActive = new AtomicBoolean();
	private int digitCount;
	private int decimals;
    private GaugeDisplayDialog dia;
    private boolean zeroJustAdded = false;

	public NumPickerTextWatcher(GaugeDisplayDialog dia, int digitCount, int decimales) {
        this.dia = dia;
        this.digitCount = digitCount;
        this.decimals = decimales;
		isActive.set(false);
    }

    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) 
    {
    }
    
    public void onTextChanged(CharSequence charSequence, int start, int count, int after) 
    {
    	if(after > count && (charSequence.charAt(charSequence.length()-1) == '0' || charSequence.charAt(charSequence.length()-1) == '-'))
    		zeroJustAdded = true;
    }

    public void afterTextChanged(Editable s) 
    {
    	if(!isActive.get())  //not!
    	{
    	}
    	else
    	{
	    	s.setFilters(new InputFilter[]{});
			if (dia.getPickerScrollState() == 0 && isActive.get()) {
				char[] input = new char[Math.min(s.length(), digitCount)];
				if(s.length()>0)
				{
					s.getChars(0, Math.min(s.length(), digitCount), input, 0);
					String newEditVal = String.valueOf(input);
					boolean onlyZeros = true;
					for(char chr : input)
						if(chr != '0' && chr != '-')
							onlyZeros = false;
					if(onlyZeros && !zeroJustAdded)
					{
						newEditVal = "";
						dia.SetValue(new char[0]);
					}
					else if (Character.isDigit(input[input.length-1]) || input[input.length-1] == '-') {
						dia.SetValue(input);
					}
					else if(input[input.length-1] == '.' || input[input.length-1] == ',' || input[input.length-1] == '#')
					{
						int zeros = digitCount - decimals - s.length();
						char[] zw = new char[zeros+1];
						for(int i = zeros; i >= 0; i--)
						{
							zw[i] = '0';
						}
						StringBuilder sb = new StringBuilder();
						sb.append(zw);
						sb.append(s.subSequence(0, s.length()-1));
						input = sb.toString().toCharArray();
						newEditVal = String.valueOf(input);
						dia.setDecimalPlace(true);
						dia.SetValue(input);
					}
					
					isActive.set(false);	//prevent stackoverflow
					s.replace(0, s.length(), newEditVal , 0, newEditVal.length());
					isActive.set(true);
				}
				else
					dia.SetValue(input);

			}
    	}
    }
	
    public static AtomicBoolean isActive() {
		return isActive;
	}
	
	public static void SetActivateTimer(int delay)
	{
		Timer doT = new Timer();
		doT.schedule(new MyTimerTask(), delay);
	}
	
    private static class MyTimerTask extends TimerTask{

        @Override
        public void run() {
			isActive.set(true);
        };
    }
}
