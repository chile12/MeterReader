package ccc.android.meterdata.types;

import org.codehaus.jackson.annotate.JsonIgnore;

import ccc.android.meterdata.OfficialNaming;
import ccc.android.meterdata.enums.GaugeType;
import ccc.android.meterdata.interfaces.IGenericMember;

public class Gauge implements IGenericMember
{
	@OfficialNaming(Type = "Gauge", Field = "gauge id")
	private int gaugeId;
	@OfficialNaming(Type = "Gauge", Field = "description")
	private String description;
	@OfficialNaming(Type = "Gauge", Field = "name")
	private String name;
	@OfficialNaming(Type = "Gauge", Field = "medium")
	private String medium;
	@OfficialNaming(Type = "Gauge", Field = "unit")
	private MeasurementUnit unit;
	@OfficialNaming(Type = "Gauge", Field = "measurement name")
	private String measurementName;
	@OfficialNaming(Type = "Gauge", Field = "location")
	private String location;
	@OfficialNaming(Type = "Gauge", Field = "type")
	private GaugeType type;
	@OfficialNaming(Type = "Gauge", Field = "barcode")
	private String barCode;
	@OfficialNaming(Type = "Gauge", Field = "type description")
	private String typeDescription;
	@OfficialNaming(Type = "Gauge", Field = "hardware")
	private String hardware;
	@OfficialNaming(Type = "Gauge", Field = "device id")
	private Integer gaugeDeviceId;
	
	public Gauge(int id)
	{
		this.gaugeId = id;
	}
	public Gauge() {}

	public String getMeasurementName() {
		return measurementName;
	}
	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
	}
	public MeasurementUnit getUnit() {
		return unit;
	}
	public void setUnit(MeasurementUnit unit) {
		this.unit = unit;
	}
	public int getGaugeId() {
		return gaugeId;
	}
	public void setGaugeId(int gaugeId) {
		this.gaugeId = gaugeId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setMedium (String medium)
	{
		this.medium = medium;
	}
	public String getMedium() {
		return medium;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public int getGaugeTypeId() {
		return type.getNumVal();
	}
	@JsonIgnore
	public GaugeType getGaugeType() {
		return type;
	}
	public void setGaugeType(GaugeType type) {
		this.type = type;
	}
	public void setGaugeTypeId(int id) {
		this.type = GaugeType.GetGaugeType(id);
	}
	public String getBarcode() {
		return barCode;
	}
	public void setBarcode(String barCode) {
		this.barCode = barCode;
	}
	public String getTypeDescription() {
		return typeDescription;
	}
	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}
	public String getHardware() {
		return hardware;
	}
	public void setHardware(String hardware) {
		this.hardware = hardware;
	}
	public Integer getGaugeDeviceId()
	{
		return gaugeDeviceId;
	}
	public void setGaugeDeviceId(Integer gaugeDeviceId)
	{
		if(gaugeDeviceId == null || gaugeDeviceId < 0)
			this.gaugeDeviceId = null;
		else
			this.gaugeDeviceId = gaugeDeviceId;
	}
}
