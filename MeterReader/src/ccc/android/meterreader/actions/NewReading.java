package ccc.android.meterreader.actions;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.R;
import ccc.android.meterreader.datamanagement.DataContext;
import ccc.android.meterreader.statics.Statics;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewReading implements IMeterReaderAction
{
	private Reading read;
	private Date date;
	
	public NewReading()
	{	}
	public NewReading(Reading ra)
	{
		this.read = ra;
		this.date = Statics.getUtcTime();
	}

	@JsonIgnore
	@Override
	public String getActionText(DataContext context) 
	{
		Gauge gauge = context.getGauges().getById(read.getGaugeId());
		return Statics.getDefaultResources().getString(R.string.action_new_reading, gauge.getName(), Statics.getLocaleDateFormat().format(date));
	}

	@Override
	public Class<? extends IMeterReaderAction> getType()
	{
		return this.getClass();
	}
	
	@Override
	public Date getDate() {
		return date;
	}

	@JsonIgnore
	@Override
	public IGenericMember getReferenceObject() {
		return read;
	}

	@JsonIgnore
	@Override
	public void Undo(DataContext context) {
		// TODO Auto-generated method stub
		
	}

	@JsonIgnore
	@Override
	public void Redo(DataContext context) {
		// TODO Auto-generated method stub
		
	}

	public Reading getRead()
	{
		return read;
	}

	public void setRead(Reading read)
	{
		this.read = read;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

}
