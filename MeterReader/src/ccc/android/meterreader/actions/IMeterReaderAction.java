package ccc.android.meterreader.actions;

import java.util.Date;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterreader.datamanagement.DataContext;

public interface IMeterReaderAction 
{
	String getActionText(DataContext context);
	
	IGenericMember getReferenceObject();
	
	Date getDate();
	
	Class<? extends IMeterReaderAction> getType();
	
	//future stuff
	void Undo(DataContext context);
	
	void Redo(DataContext context);
}
