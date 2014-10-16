package ccc.android.meterreader.actions;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterreader.R;
import ccc.android.meterreader.datamanagement.DataContext;
import ccc.android.meterreader.internaldata.InternalGaugeDevice;
import ccc.android.meterreader.statics.Statics;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewGaugeDevice implements IMeterReaderAction
{
	private InternalGaugeDevice device;
	private Date date;
	
	public NewGaugeDevice() {}
	
	public NewGaugeDevice(InternalGaugeDevice device)
	{
		this.device = device;
		this.date = Statics.getUtcTime();
	}
	
	@Override
	public String getActionText(DataContext context)
	{
		Gauge gauge = context.getGauges().getById(device.getGaugeId());
		if(gauge == null)
			return "";
		return Statics.getDefaultResources().getString(R.string.action_new_device, gauge.getName());
	}

	@Override
	public Class<? extends IMeterReaderAction> getType()
	{
		return this.getClass();
	}

	@Override
	public IGenericMember getReferenceObject()
	{
		return device;
	}

	@Override
	public Date getDate()
	{
		return date;
	}

	@Override
	public void Undo(DataContext context)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Redo(DataContext context)
	{
		// TODO Auto-generated method stub
		
	}

	public InternalGaugeDevice getDevice()
	{
		return device;
	}

	public void setDevice(InternalGaugeDevice device)
	{
		this.device = device;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}
}
