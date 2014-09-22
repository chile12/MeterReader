package ccc.android.meterdata.types;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import ccc.android.meterdata.MeterDataUtils;
import ccc.android.meterdata.interfaces.IGenericMember;

public class GaugeDeviceDigit  implements IGenericMember
{
	private int gaugeDeviceId;
	private int digit;
	private int set;
	private boolean digital;
	private String grayscale;
	
	public boolean isDigital() {
		return digital;
	}
	@JsonProperty("Digital")
	public void setDigital(boolean digital) {
		this.digital = digital;
	}
	public int getGaugeDeviceId() {
		return gaugeDeviceId;
	}
	@JsonProperty("GaugeDeviceId")
	public void setGaugeDeviceId(int gaugeDeviceId) {
		this.gaugeDeviceId = gaugeDeviceId;
	}
	public int getDigit() {
		return digit;
	}
	@JsonProperty("Digit")
	public void setDigit(int digit) {
		this.digit = digit;
	}
	@JsonIgnore
	public byte[] getBinary() {
		return MeterDataUtils.decodeToImage(grayscale);
	}
	public String getGrayscale() {
		return grayscale;
	}
	@JsonIgnore
	public void setBinary(byte[] grayScale) {
		this.grayscale = MeterDataUtils.encodeToBase64String(grayScale);
	}
	@JsonProperty("Grayscale")
	public void setGrayscale(String grayScale) {
		this.grayscale = grayScale;
	}
	@JsonProperty("Set")
	public int getSet() {
		return set;
	}
	@JsonProperty("Set")
	public void setSet(int set) {
		this.set = set;
	}
}
