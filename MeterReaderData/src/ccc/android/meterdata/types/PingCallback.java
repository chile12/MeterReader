package ccc.android.meterdata.types;

import java.util.Date;

import ccc.android.meterdata.interfaces.IGenericMember;

public class PingCallback implements IGenericMember{
	private int pingResult;
	private Date utc;
	private String error;

	public int getPingResult() {
		return pingResult;
	}

	public void setPingResult(int pingResult) {
		this.pingResult = pingResult;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Date getUtc() {
		return utc;
	}

	public void setUtc(Date utc) {
		this.utc = utc;
	}

}
