package ccc.android.meterdata.enums;

public enum RoutePrecision 
{
	EXACT(0),
	CLUTTER(1),
	PARTIAL(2),
	EXCESS(3),
	FUZZY(4);
	
    private int numVal;

    RoutePrecision(int numVal) {
        this.numVal = numVal;
    }
    
    protected RoutePrecision GetRoutePrecision(int id)
    {
    	RoutePrecision type = null;
    	switch(id){
    		case 0: type = RoutePrecision.EXACT;
    		case 1: type = RoutePrecision.CLUTTER;
    		case 2: type = RoutePrecision.PARTIAL;
    		case 3: type = RoutePrecision.EXCESS;
    		case 4: type = RoutePrecision.FUZZY;
    	}        		
    	return type;
    }
    
    public int getNumVal() {
        return numVal;
    }
}
