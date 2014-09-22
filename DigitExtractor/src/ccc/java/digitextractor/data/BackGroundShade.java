package ccc.java.digitextractor.data;
public enum BackGroundShade
{
	Dark(0),
	Bright(1),
	Undef(2);

    private int numVal;

    BackGroundShade(int numVal) {
        this.numVal = numVal;
    }

    public static BackGroundShade GetBackGroundShade(int id)
    {
    		if(0 == id)
    			return BackGroundShade.Dark;
    		else if(id == 1)
    			return BackGroundShade.Bright;
    		else
    			return BackGroundShade.Undef;
    }
    
    public int getNumVal() {
        return numVal;
    }
}
