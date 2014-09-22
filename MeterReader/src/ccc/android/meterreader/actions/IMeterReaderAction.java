package ccc.android.meterreader.actions;

import java.util.Date;

import ccc.android.meterdata.interfaces.IGenericMember;

public interface IMeterReaderAction 
{
	String GetActionText();
	
	IGenericMember GetReferenceObject();
	
	Date GetDate();
	
	//future stuff
	void Undo();
	
	void Redo();
}
