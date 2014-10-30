package ccc.android.meterreader.statics;

import java.util.HashMap;
import java.util.Map;

import ccc.android.meterreader.internaldata.InternalGaugeDevice;

///makes loaded gauges available in a static manner
public class StaticGaugeLibrary 
{
	protected static Map<Integer, InternalGaugeDevice> gauges = new HashMap<Integer, InternalGaugeDevice>();

	public static InternalGaugeDevice getGauge(int id) {
		return gauges.get(id);
	}

	public static Map<Integer, InternalGaugeDevice> getGauges() {
		return gauges;
	}
}
