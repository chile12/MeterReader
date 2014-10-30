package ccc.android.meterdata.types;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

import ccc.android.meterdata.interfaces.IGenericMember;
import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;

public class GaugeDevice implements IGenericMember
{
	private int gaugeDeviceId;
	private String deviceName;
	private int gaugeId;
	private Date utcInstallation;
	private String manufacturer;
	private float valueOffset;
	private String serialNumber;
	private String comment;
	private int digitCount;
	private int decimalPlaces;
	private int backGroundShade;
	private String barcode;
	private GaugeDeviceDigitList digitPatterns;
	private Image image;

	@JsonProperty("SerialNumber")
	public String getSerialNumber() {
		return serialNumber;
	}
	@JsonProperty("SerialNumber")
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	@JsonProperty("Comment")
	public String getComment() {
		return comment;
	}
	@JsonProperty("Comment")
	public void setComment(String comment) {
		this.comment = comment;
	}

	@JsonProperty("GaugeDeviceId")
	public int getGaugeDeviceId() {
		return gaugeDeviceId;
	}

	@JsonProperty("GaugeDeviceId")
	public void setGaugeDeviceId(int gaugeDeviceId) {
		this.gaugeDeviceId = gaugeDeviceId;
	}
	
	@JsonProperty("DigitCount")
	public int getDigitCount() {
		return digitCount;
	}
	@JsonProperty("DigitCount")
	public void setDigitCount(int digitCount) {
		this.digitCount = digitCount;
	}

	@JsonProperty("DecimalPlaces")
	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	@JsonProperty("DecimalPlaces")
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	@JsonProperty("UtcInstallation")
	public Date getUtcInstallation() {
		return utcInstallation;
	}

	@JsonProperty("UtcInstallation")
	public void setUtcInstallation(Date utcInstallation) {
		this.utcInstallation = utcInstallation;
	}

	@JsonProperty("Manufacturer")
	public String getManufacturer() {
		return manufacturer;
	}

	@JsonProperty("Manufacturer")
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	@JsonProperty("ValueOffset")
	public float getValueOffset() {
		return valueOffset;
	}

	@JsonProperty("ValueOffset")
	public void setValueOffset(float valueOffset) {
		this.valueOffset = valueOffset;
	}
	public GaugeDeviceDigitList getDigitPatterns() {
		return digitPatterns;
	}
	public void setDigitPatterns(GaugeDeviceDigitList digitPatterns) {
		this.digitPatterns = digitPatterns;
	}
	@JsonProperty("Background")
	public int getBackGround() {
		return backGroundShade;
	}
	@JsonProperty("Background")
	public void setBackGround(int backGroundShade) {
		this.backGroundShade = backGroundShade;
	}
	@JsonProperty("GaugeId")
	public int getGaugeId()
	{
		return gaugeId;
	}
	@JsonProperty("GaugeId")
	public void setGaugeId(int gaugeId)
	{
		this.gaugeId = gaugeId;
	}
	@JsonProperty("DeviceName")
	public String getDeviceName()
	{
		return deviceName;
	}
	@JsonProperty("DeviceName")
	public void setDeviceName(String deviceName)
	{
		this.deviceName = deviceName;
	}
	@JsonProperty("Barcode")
	public String getBarcode()
	{
		return barcode;
	}
	@JsonProperty("Barcode")
	public void setBarcode(String barcode)
	{
		this.barcode = barcode;
	}
	@JsonProperty("Image")
	public Image getImage()
	{
		return image;
	}
	@JsonProperty("Image")
	public void setImage(Image image)
	{
		this.image = image;
	}
}
