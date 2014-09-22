package ccc.android.meterdata.types;

public class MeasurementUnit 
{
	private int measurementUnitId;
	private String name;
	private String description;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getMeasurementUnitId() {
		return measurementUnitId;
	}
	public void setMeasurementUnitId(int measurementUnitId) {
		this.measurementUnitId = measurementUnitId;
	}
}
