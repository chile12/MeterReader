package ccc.android.meterreader.actions;

import java.util.Date;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.R;
import ccc.android.meterreader.statics.Statics;

public class NewReading implements IMeterReaderAction
{
	private Gauge gauge;
	private Reading read;
	private Date date;
	
	public NewReading(Gauge ga, Reading ra)
	{
		this.gauge = ga;
		this.read = ra;
		this.date = Statics.getUtcTime();
	}

	@Override
	public String GetActionText() 
	{
		return Statics.getDefaultResources().getString(R.string.action_new_reading, gauge.getName(), Statics.getLocaleDateFormat().format(date));
	}

	@Override
	public Date GetDate() {
		return date;
	}

	@Override
	public IGenericMember GetReferenceObject() {
		return read;
	}

	@Override
	public void Undo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Redo() {
		// TODO Auto-generated method stub
		
	}

}
