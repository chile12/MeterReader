package ccc.android.meterdata.types;

import java.util.Date;

import ccc.android.meterdata.OfficialNaming;
import ccc.android.meterdata.interfaces.IGenericMember;


public class Reading implements IGenericMember
{
	@OfficialNaming(Type = "Reading", Field = "session id")
	private Integer sessionId;
	@OfficialNaming(Type = "Reading", Field = "station id")
	private Integer stationId;
	@OfficialNaming(Type = "Reading", Field = "previous reading date")
	private Date utcFrom;
	@OfficialNaming(Type = "Reading", Field = "last reading date")
	private Date utcTo;
	@OfficialNaming(Type = "Reading", Field = "reading value")
	private float read;
	@OfficialNaming(Type = "Reading", Field = "gauge id")
	private int gaugeId;
	@OfficialNaming(Type = "Reading", Field = "value type")
	private int valType;

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
	public int getGaugeId() {
		return gaugeId;
	}
	public void setGaugeId(int gaugeId) {
		this.gaugeId = gaugeId;
	}
	public int getValType() {
		return valType;
	}
	public void setValType(int valType) {
		this.valType = valType;
	}
	public float getRead() {
		return read;
	}
	public void setRead(float read) {
		this.read = read;
	}
	public Date getUtcFrom() {
		return utcFrom;
	}
	public void setUtcFrom(Date from) {
		this.utcFrom = from;
	}
	public Date getUtcTo() {
		return utcTo;
	}
	public void setUtcTo(Date to) {
		this.utcTo = to;
	}
}
