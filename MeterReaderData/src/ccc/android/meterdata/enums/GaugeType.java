package ccc.android.meterdata.enums;

public enum GaugeType
{
        Real(0), 
        Virtual(1),
        Manual(2);

        private int numVal;

        GaugeType(int numVal) {
            this.numVal = numVal;
        }

        public static GaugeType GetGaugeType(int id)
        {
			if(id == 0)
				return GaugeType.Real;
			else if(id == 1)
				return GaugeType.Virtual;
			else if(id == 2)
				return GaugeType.Manual;
			else
				return null;
        }
        
        public int getNumVal() {
            return numVal;
        }
}
