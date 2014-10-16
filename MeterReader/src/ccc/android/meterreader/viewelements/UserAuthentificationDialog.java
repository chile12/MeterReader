package ccc.android.meterreader.viewelements;


import ccc.android.meterreader.datamanagement.async.*;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.R;
import ccc.android.meterreader.internaldata.ICallBack;
import ccc.android.meterreader.helpfuls.DialogCallbackListener;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class UserAuthentificationDialog  extends Dialog implements android.view.View.OnClickListener
{
	private TextView logInTB;
	private TextView passwordTB;
	public DialogCallbackListener callbackListener;
	private MainActivity main;

	public UserAuthentificationDialog(MainActivity context, DialogCallbackListener listener)
	{
		super(context);
		this.main = context;
		this.callbackListener = listener;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.userauthentification);
	    
	    this.logInTB = (TextView) this.findViewById(R.id.logInTB);
	    this.logInTB.setOnFocusChangeListener(textViewFocusListener);
	    this.passwordTB = (TextView) this.findViewById(R.id.passWordTB);
	    this.passwordTB.setOnFocusChangeListener(textViewFocusListener);
	    this.passwordTB.setOnKeyListener(onEnterListener);
		this.findViewById(R.id.confirmBT).setOnClickListener(confirmLogInListener);
		this.findViewById(R.id.dismissBT).setOnClickListener(dismissListener);
		this.logInTB.requestFocus();
    }
    
    private View.OnClickListener confirmLogInListener = new View.OnClickListener()
    {
		@Override
		public void onClick(View v) 
		{
			executeAsyncUserAuthentification();
		}
    };
    
    private View.OnClickListener dismissListener = new View.OnClickListener()
    {
		@Override
		public void onClick(View v) 
		{
			UserAuthentificationDialog.this.dismiss();
		}
    };
    
    private View.OnKeyListener onEnterListener = new View.OnKeyListener()
    {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent keyEvent) 
		{
			if(keyEvent.getAction()==0)
			{
				if(keyCode == KeyEvent.KEYCODE_ENTER) 
				{
					executeAsyncUserAuthentification();
				}
			}
			return true;
		}
    };
    
    private void executeAsyncUserAuthentification()
    {
		AsyncUserAuthentification ua = new AsyncUserAuthentification();
		ua.execute(new Object[]{ this, this.logInTB.getText(), this.passwordTB.getText() });
    }
    
    View.OnFocusChangeListener textViewFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
            	UserAuthentificationDialog.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    };
   
	@Override
	public void onClick(View arg0) 
	{
		// TODO Auto-generated method stub
		
	}
	public DialogCallbackListener getCallbackListener() {
		return callbackListener;
	}
}
