package ccc.android.meterreader.viewelements;


import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.R;
import ccc.android.meterreader.helpfuls.EndpointUriTextWatcher;
import ccc.android.meterreader.statics.StaticPreferences;
import ccc.android.meterreader.statics.Statics;

@SuppressLint("DefaultLocale")
public class ConnectionConfigDialog  extends Dialog //implements android.view.View.OnClickListener, ICallBack
{
	private EditText endpointTB=null;
	public ConnectionConfigDialog(MainActivity context)
	{
		super(context);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
	    super.onCreate(savedInstanceState);
    	setContentView(R.layout.connectionconfigdialog);
        LayoutParams params = getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    this.findViewById(R.id.connectionDiaConfirmBT).setOnClickListener(confirmListener);
	    this.findViewById(R.id.connectionDiaAbortBT).setOnClickListener(dismissListener);
	    
	    endpointTB = ((EditText)this.findViewById(R.id.connectionDiaAddressTB));
	    //endpointTB.setOnKeyListener(onEnterListener);
	    endpointTB.setInputType(InputType.TYPE_CLASS_TEXT);
	    endpointTB.setFilters(new InputFilter[]{});
	    endpointTB.addTextChangedListener(new EndpointUriTextWatcher());
    }
    
    @Override
    protected void onStart()
    {
    	super.onStart();
    	String address = StaticPreferences.getPreference(Statics.WS_URL, null);
    	if(address == null || address == "")
    		address = Statics.getDefaultResources().getString(R.string.connection_dia_no_endpoint);
    	endpointTB.setText(address);
    	endpointTB.selectAll();
    }
    
    private View.OnClickListener confirmListener = new View.OnClickListener()
    {
		@SuppressWarnings("unused")
		@Override
		public void onClick(View v) 
		{
			EditText tb = (EditText) (ConnectionConfigDialog.this.findViewById(R.id.connectionDiaAddressTB));
			String test = tb.getText().toString();
			if((test.startsWith("http://") && (test.toLowerCase().trim().endsWith("mobilegaugereading") || test.toLowerCase().trim().endsWith("mobilegaugereading/"))))
			{
				try {
					//check if string is a URL
					URL testUrl = new URL(test);
				} catch (MalformedURLException e) {
					Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.connection_dia_malformed_url));
				}
				StaticPreferences.setPreference(Statics.WS_URL, tb.getText().toString().replace(" ", ""));
				Statics.syncNow();
				ConnectionConfigDialog.this.dismiss();
				return;
			}
			Statics.ShowAlertDiaMsgWithBt(Statics.getDefaultResources().getString(R.string.connection_dia_malformed_url));
		}
    };
    
    private View.OnClickListener dismissListener = new View.OnClickListener()
    {
		@Override
		public void onClick(View v) 
		{
			ConnectionConfigDialog.this.dismiss();
			return;
		}
    };
}