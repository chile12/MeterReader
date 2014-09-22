package ccc.android.meterreader.helpfuls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;

public class FinishReceiver extends BroadcastReceiver 
{

	@Override
	public void onReceive(Context context, Intent arg1) {
		((Activity) context).finish();
		return;
	}

}
