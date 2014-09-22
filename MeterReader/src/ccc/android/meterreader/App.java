package ccc.android.meterreader;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

public class App extends Application {
	public static volatile Context context;
	
    public void onCreate(){
        super.onCreate();
        App.context = getApplicationContext();
    }    	
	    
	protected static void setContext(Context cont)
	{
		context = cont;
	}
	
	public static Context getContext()
	{
		return context;
	}
	
	public static int DpToPx(int dp) {
	    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
}
