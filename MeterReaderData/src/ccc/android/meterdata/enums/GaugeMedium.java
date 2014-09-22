package ccc.android.meterdata.enums;

public enum GaugeMedium 
{
    Gas(1), 
    StadtWasser(2),
    DemiWasser(3),
    Druckluft(5), 
    Dampf(6), 
    Strom(7), 
    Geld(9);

    private int numVal;

    GaugeMedium(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
    
    public static GaugeMedium GetGaugeMedium(int id)
    {
    	if(id == 1)
    		return GaugeMedium.Gas;
    	else if(id == 2)
    		return GaugeMedium.StadtWasser;
    	else if(id == 3)
    		return GaugeMedium.DemiWasser;
    	else if(id == 5)
    		return GaugeMedium.Druckluft;
    	else if(id == 6)
    		return GaugeMedium.Dampf;
    	else if(id == 7)
    		return GaugeMedium.Strom;
    	else if(id == 9)
    		return GaugeMedium.Geld;
    	else
    		return null;
    }
}
