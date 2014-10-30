package ccc.android.meterreader.internaldata;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.Reading;

public class InternalDialogDisplayData implements Parcelable
{
	private Float read;
	private Integer gaugeId;
	private Integer gaugeDeviceId;
	private Integer digitCount;
	private Integer decimalPlaces;
	private Integer sessionId;
	private Integer stationId;
	private Date utcTo;
	private String gaugeName;
	private String unit;
	private String unitLong;

	private String location;
	private String description;
	
	public Integer getSessionId() {
		return sessionId;
	}

	public void setSessionId(Integer sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getStationId() {
		return stationId;
	}

	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}

	public Float getRead() {
		return read;
	}

	public void setRead(Float read) {
		this.read = read;
	}

	public Integer getGaugeId() {
		return gaugeId;
	}

	public void setGaugeId(Integer gaugeId) {
		this.gaugeId = gaugeId;
	}

	public Integer getDigitCount() {
		return digitCount;
	}

	public void setDigitCount(Integer digitCount) {
		this.digitCount = digitCount;
	}

	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public Date getUtcTo() {
		return utcTo;
	}

	public void setUtcTo(Date utcTo) {
		this.utcTo = utcTo;
	}

	public String getGaugeName() {
		return gaugeName;
	}

	public void setGaugeName(String gaugeName) {
		this.gaugeName = gaugeName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public InternalDialogDisplayData(Gauge ga, GaugeDevice de, Reading re)
	{
		if(re != null)
		{
			this.setRead(re.getRead());
			this.setSessionId(re.getSessionId());
			this.setStationId(re.getStationId());
			this.setUtcTo(new Date(re.getUtcTo().getTime()));
		}
		this.setGaugeId(ga.getGaugeId());
		this.setGaugeDeviceId(de.getGaugeDeviceId());
		this.setDecimalPlaces(de.getDecimalPlaces());
		this.setDigitCount(de.getDigitCount());
		this.setLocation(ga.getLocation());
		this.setUnit(ga.getUnit().getName());
		this.setUnitLong(ga.getUnit().getDescription());
		this.setGaugeName(ga.getName());
		this.setDescription(ga.getDescription());
	}
	
	private InternalDialogDisplayData(Parcel re)
	{
		//reading values (may be null serialized as -1 -> writeToParcel)
		this.setRead(re.readFloat());
		if(this.getRead() < 0)
			this.setRead(null);
		this.setSessionId(re.readInt());
		if(this.getSessionId() < 0)
			this.setSessionId(null);
		this.setStationId(re.readInt());
		if(this.getStationId() < 0)
			this.setStationId(null);
		long zw = re.readLong();
		if(zw < 0)
			this.setUtcTo(null);
		else
			this.setUtcTo(new Date(zw));
		
		//gauge values
		this.setGaugeId(re.readInt());
		this.setGaugeDeviceId(re.readInt());
		this.setDecimalPlaces(re.readInt());
		this.setDigitCount(re.readInt());
		this.setLocation(readStringFromParcel(re));
		this.setUnit(readStringFromParcel(re));
		this.setUnitLong(readStringFromParcel(re));
		this.setGaugeName(readStringFromParcel(re));
		this.setDescription(readStringFromParcel(re));
	}

	@Override
	public int describeContents() {		return 0;	}  //ignore!

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeFloat(this.getRead() == null ? -1 : this.getRead());
		out.writeInt(this.getSessionId() == null ? -1 : this.getSessionId());
		out.writeInt(this.getStationId() == null ? -1 : this.getStationId());
		out.writeLong(this.getUtcTo() == null ? -1 : this.getUtcTo().getTime());
		out.writeInt(this.getGaugeId());
		out.writeInt(this.getGaugeDeviceId());
		out.writeInt(this.getDecimalPlaces() == null ? 3 : this.getDecimalPlaces());
		out.writeInt(this.getDigitCount() == null ? 8 : this.getDigitCount());
		writeStringToParcel(out, this.getLocation());
		writeStringToParcel(out, this.getUnit());
		writeStringToParcel(out, this.getUnitLong());
		writeStringToParcel(out, this.getGaugeName());
		writeStringToParcel(out, this.getDescription());
	}
	
    public static final Parcelable.Creator<InternalDialogDisplayData> CREATOR = new Parcelable.Creator<InternalDialogDisplayData>() {
        public InternalDialogDisplayData createFromParcel(Parcel in) {
        	InternalDialogDisplayData data = new InternalDialogDisplayData(in);
            return data ;
        }

        public InternalDialogDisplayData[] newArray(int size) {
            return new InternalDialogDisplayData[size];
        }
    };
    
    private void writeStringToParcel(Parcel p, String s) {
        p.writeInt((byte)(s != null ? 1 : 0));
        p.writeString(s != null ? s : "");
    }

    private String readStringFromParcel(Parcel p) {
        boolean isPresent = p.readInt() == 1;
        String s = p.readString();
        return isPresent ? s : null;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getGaugeDeviceId() {
		return gaugeDeviceId;
	}

	public void setGaugeDeviceId(Integer gaugeDeviceId) {
		this.gaugeDeviceId = gaugeDeviceId;
	}
	public String getUnitLong()
	{
		return unitLong;
	}

	public void setUnitLong(String unitLong)
	{
		this.unitLong = unitLong;
	}
}
